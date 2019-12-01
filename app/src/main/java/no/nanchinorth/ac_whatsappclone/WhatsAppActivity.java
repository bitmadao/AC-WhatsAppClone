package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logAndFancyToastException;
import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logoutParseUser;

public class WhatsAppActivity extends AppCompatActivity implements
        View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {

    private boolean isListViewPopulated;
    private ArrayList<String> listedMessageIds;

    ArrayList<String> inContactArrayList;

    private ArrayList<RecentConversation> recentConversationArrayList;
    private RecentConversationAdapter recentConversationAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView listView;

    private ParseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app);

        if(ParseUser.getCurrentUser() != null){
            currentUser = ParseUser.getCurrentUser();
            inContactArrayList = new ArrayList<>();
            setTitle(String.format("Conversations: %s", currentUser.getUsername()));
        } else {
            transitionToLogin();
        }

        isListViewPopulated = false ;

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutWhatsAppActivityRoot);
        swipeRefreshLayout.setOnRefreshListener(WhatsAppActivity.this);

        listView = findViewById(R.id.listViewWhatsAppActivity);

        checkForNewContacts();




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_whats_app_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch(item.getItemId()){
            case R.id.menuItemWhatsAppActivityLogout:
                logoutParseUser(WhatsAppActivity.this,this);
                break;

            case R.id.menuItemWhatsAppActivityChat:

                break;

            case R.id.menuItemWhatsAppActivityUserDirectory:
                transitionToUserDirectoryActivity();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        populateListView();
        swipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent conversationActivityIntent = new Intent(WhatsAppActivity.this, ConversationActivity.class);
        conversationActivityIntent.putExtra("oppositeUsername", inContactArrayList.get(position));

        startActivity(conversationActivityIntent);
    }

    private void checkForNewContacts(){
        inContactArrayList = new ArrayList<>();
        if(currentUser.getList("inContact") != null){
            inContactArrayList.addAll(currentUser.<String>getList("inContact"));
            // Sort ArrayList to ensure alphabetic listing of conversations...
            Collections.sort(inContactArrayList, new Comparator<String>() {
                @Override
                public int compare(String s1, String s2) {
                    return s1.compareToIgnoreCase(s2);
                }
            });
        }
        final ArrayList<String> newContactsList = new ArrayList<>();
        ParseQuery<ParseObject> checkForNewContactsQuery = ParseQuery.getQuery("Message");
        checkForNewContactsQuery.whereEqualTo("receiver", currentUser.getUsername());
        checkForNewContactsQuery.whereNotContainedIn("sender", inContactArrayList);

        checkForNewContactsQuery.orderByDescending("createdAt");
        checkForNewContactsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0) {
                        for(ParseObject object : objects) {
                            if (!newContactsList.contains(object.getString("sender"))){
                                newContactsList.add(object.getString("sender"));
                            }
                        }

                        currentUser.addAll("inContact",newContactsList);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e != null){
                                    logAndFancyToastException(WhatsAppActivity.this, e);
                                } else {
                                    inContactArrayList = new ArrayList<>();
                                    inContactArrayList.addAll(currentUser.<String>getList("inContact"));
                                    // Sort ArrayList to ensure alphabetic listing of conversations...
                                    Collections.sort(inContactArrayList, new Comparator<String>() {
                                        @Override
                                        public int compare(String s1, String s2) {
                                            return s1.compareToIgnoreCase(s2);
                                        }
                                    });

                                    populateListView();
                                }
                            }
                        });
                    } else {
                        populateListView();
                    }
                } else {
                    logAndFancyToastException(WhatsAppActivity.this, e);
                }
            }
        });
    }

    private void populateListView(){
        if(currentUser.getList("inContact") != null){

            recentConversationArrayList = new ArrayList<>();
            listedMessageIds = new ArrayList<>();

            for(final String contactUsername: inContactArrayList){
                List<String> conversationPartiesList = Arrays.asList(currentUser.getUsername(), contactUsername);

                ParseQuery<ParseObject> conversationQuery = ParseQuery.getQuery("Message");
                conversationQuery.whereContainedIn("sender", conversationPartiesList);
                conversationQuery.whereContainedIn("receiver", conversationPartiesList);
                conversationQuery.orderByDescending("createdAt");
                conversationQuery.setLimit(1);

                conversationQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if(objects.size() > 0){
                            ParseObject messageObject = objects.get(0);
                            listedMessageIds.add(messageObject.getObjectId());
                            recentConversationArrayList.add(new RecentConversation(currentUser.getUsername(), contactUsername, messageObject));


                            if(!isListViewPopulated) {
                                isListViewPopulated = true;

                                recentConversationAdapter = new RecentConversationAdapter(WhatsAppActivity.this, recentConversationArrayList);
                                listView.setAdapter(recentConversationAdapter);
                                listView.setOnItemClickListener(WhatsAppActivity.this);
                            } else {
                                // sort ArrayList so conversations are listed alphabetically dependent on conversationOpponent
                                Collections.sort(recentConversationArrayList, new Comparator<RecentConversation>() {
                                    @Override
                                    public int compare(RecentConversation rc1, RecentConversation rc2) {
                                        return rc1.getConversationOpponent().compareToIgnoreCase(rc2.getConversationOpponent());
                                    }
                                });
                                recentConversationAdapter.notifyDataSetChanged();
                            }

                        }

                    }
                });
            }

        } else {
            final ArrayList<String> newContactsList = new ArrayList<>();
            ParseQuery<ParseObject> checkForNewContactsQuery = ParseQuery.getQuery("Message");
            checkForNewContactsQuery.whereEqualTo("receiver", currentUser.getUsername());

            checkForNewContactsQuery.orderByDescending("createdAt");
            checkForNewContactsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() > 0) {
                            for(ParseObject object : objects) {
                                if (!newContactsList.contains(object.getString("sender"))){
                                    newContactsList.add(object.getString("sender"));
                                }
                            }

                            currentUser.addAll("inContact",newContactsList);
                            currentUser.saveInBackground(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e != null){
                                        logAndFancyToastException(WhatsAppActivity.this, e);
                                    }
                                }
                            });
                        }
                    } else {
                        logAndFancyToastException(WhatsAppActivity.this, e);
                    }
                }
            });

            String[] noConversations = {"No conversations yet"};
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    WhatsAppActivity.this,
                    android.R.layout.simple_list_item_1,
                    noConversations);
            listView.setAdapter(arrayAdapter);

        }


    }

    private void transitionToUserDirectoryActivity(){
        startActivity(new Intent(WhatsAppActivity.this, UserDirectoryActivity.class));
        finish();

    }

    private void transitionToLogin(){
        startActivity(new Intent(WhatsAppActivity.this,LoginActivity.class));
        finish();
    }
}
