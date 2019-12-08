package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
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
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logAndFancyToastException;
import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logoutParseUser;

public class WhatsAppActivity extends AppCompatActivity implements
        View.OnClickListener,
        SwipeRefreshLayout.OnRefreshListener,
        AdapterView.OnItemClickListener {

    private boolean isListViewPopulated;

    final String currentUserAliasString = getString(R.string.recent_conversation_sb_you);

    private ArrayList<String> inContactArrayList;

    private TreeMap<String, RecentConversation> recentConversationTreeMap;
    private HashMap<String, Date> recentConversationLastMessageDateHashMap;
    private RecentConversationTreeMapAdapter recentConversationTreeMapAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    private ListView listView;

    private ParseUser currentUser;
    private ParseLiveQueryClient parseLiveQueryClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_whats_app);

        if(ParseUser.getCurrentUser() != null){
            currentUser = ParseUser.getCurrentUser();
            inContactArrayList = new ArrayList<>();
            setTitle(String.format(getString(R.string.title_activity_whatsapp), currentUser.getUsername()));
        } else {
            transitionToLogin();
        }


        parseLiveQueryClient = null;

        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(getString(R.string.back4app_server_url_wss)));
        } catch (Exception e) {
            Log.i("APPTAG", e.getMessage());
        }

        isListViewPopulated = false ;

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutWhatsAppActivityRoot);
        swipeRefreshLayout.setOnRefreshListener(WhatsAppActivity.this);

        listView = findViewById(R.id.listViewWhatsAppActivity);

        checkForNewContacts();

        queryForConversationUpdates();


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
        if(isListViewPopulated){
            updateListView();
        } else {
            populateListView();
        }
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

    private void queryForConversationUpdates(){
        if(parseLiveQueryClient != null){
            ParseQuery<ParseObject> parseQuery = new ParseQuery<>("Message");
            parseQuery.whereEqualTo("receiver", currentUser.getUsername());

            SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

            subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                @Override
                public void onEvent(ParseQuery<ParseObject> query, final ParseObject messageObject) {

                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            if(!inContactArrayList.contains(messageObject.getString("sender"))){
                                inContactArrayList.add(messageObject.getString("sender"));
                                currentUser.add("inContact",messageObject.getString("sender"));
                                currentUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        if(e != null){
                                            logAndFancyToastException(WhatsAppActivity.this, e);
                                        }
                                    }
                                });

                                recentConversationLastMessageDateHashMap.put(
                                        messageObject.getString("sender"),
                                        messageObject.getCreatedAt()
                                );

                                recentConversationTreeMap.put(
                                        messageObject.getString("sender"),
                                        new RecentConversation(
                                                currentUser.getUsername(),
                                                messageObject.getString("sender"),
                                                messageObject,
                                                currentUserAliasString
                                        )
                                );

                                recentConversationTreeMapAdapter.notifyDataSetInvalidated();
                                recentConversationTreeMapAdapter.notifyDataSetChanged();
                            }

                        }
                    });

                }
            });
        }
    }

    private void checkForNewContacts(){
        inContactArrayList = new ArrayList<>();
        if(currentUser.getList("inContact") != null){
            inContactArrayList.addAll(currentUser.<String>getList("inContact"));
            if(!isSorted(inContactArrayList.toArray())){
                // Sort ArrayList to ensure alphabetic listing of conversations...
                Collections.sort(inContactArrayList, new Comparator<String>() {
                    @Override
                    public int compare(String s1, String s2) {
                        return s1.compareToIgnoreCase(s2);
                    }
                });

                currentUser.remove("inContact");
                currentUser.put("inContact",inContactArrayList);
                currentUser.saveInBackground();
            }
        }
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
                            if (!inContactArrayList.contains(object.getString("sender"))){
                                inContactArrayList.add(object.getString("sender"));
                            }
                        }

                        // Sort ArrayList to ensure alphabetic listing of conversations...
                        Collections.sort(inContactArrayList, new Comparator<String>() {
                            @Override
                            public int compare(String s1, String s2) {
                                return s1.compareToIgnoreCase(s2);
                            }
                        });

                        currentUser.remove("inContact");
                        currentUser.put("inContact",inContactArrayList);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e != null){
                                    logAndFancyToastException(WhatsAppActivity.this, e);
                                } else {
                                    if(isListViewPopulated){
                                        updateListView();
                                    } else {
                                        populateListView();
                                    }
                                }
                            }
                        });
                    } else {
                        if(isListViewPopulated){
                            updateListView();
                        } else {
                            populateListView();
                        }
                    }
                } else {
                    logAndFancyToastException(WhatsAppActivity.this, e);
                }
            }
        });
    }

    private void populateListView(){
        if(currentUser.getList("inContact") != null){

            recentConversationTreeMap = new TreeMap<>();
            recentConversationLastMessageDateHashMap = new HashMap<>();


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
                        if(e == null) {
                            if (objects.size() > 0) {
                                isListViewPopulated = true;

                                ParseObject messageObject = objects.get(0);

                                recentConversationLastMessageDateHashMap.put(contactUsername, messageObject.getCreatedAt());
                                recentConversationTreeMap.put(
                                        contactUsername,
                                        new RecentConversation(
                                                currentUser.getUsername(),
                                                contactUsername,
                                                messageObject,
                                                currentUserAliasString
                                        )
                                );



                            }
                            if(inContactArrayList.indexOf(contactUsername) == (inContactArrayList.size() - 1)) {

                                Log.i("APPTAG", "populateList Stopping at index " + inContactArrayList.indexOf(contactUsername));
                                recentConversationTreeMapAdapter = new RecentConversationTreeMapAdapter(WhatsAppActivity.this, recentConversationTreeMap);
                                listView.setAdapter(recentConversationTreeMapAdapter);

                                listView.setOnItemClickListener(WhatsAppActivity.this);
                            }
                        } else {
                            logAndFancyToastException(WhatsAppActivity.this, e);
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

                            currentUser.addAll("inContact", newContactsList);
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

            String[] noConversations = {getString(R.string.listview_placeholder_activity_whatsapp)};
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    WhatsAppActivity.this,
                    android.R.layout.simple_list_item_1,
                    noConversations);
            listView.setAdapter(arrayAdapter);

        }


    }

    private boolean isSorted(Object[] array){
        for(int i = 0; i < array.length -1; i++){
            String one = (String) array[i];
            String two = (String) array[i+1];
            if(one.compareTo(two) > 0){
                return false;
            }
        }
        return true;
    }

    private void updateListView(){
        for(final String contactUsername: inContactArrayList) {
            List<String> conversationPartiesList = Arrays.asList(currentUser.getUsername(), contactUsername);

            ParseQuery<ParseObject> conversationQuery = ParseQuery.getQuery("Message");
            conversationQuery.whereContainedIn("sender", conversationPartiesList);
            conversationQuery.whereContainedIn("receiver", conversationPartiesList);
            conversationQuery.orderByDescending("createdAt");
            conversationQuery.whereGreaterThan("createdAt", recentConversationLastMessageDateHashMap.get(contactUsername));
            conversationQuery.setLimit(1);

            conversationQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if(e == null){
                        if(objects.size() > 0){
                            ParseObject messageObject = objects.get(0);

                            recentConversationLastMessageDateHashMap.put(contactUsername, messageObject.getCreatedAt());
                            recentConversationTreeMap.put(contactUsername,
                                    new RecentConversation(currentUser.getUsername(), contactUsername, messageObject, currentUserAliasString));

                        }

                        if(inContactArrayList.indexOf(contactUsername) == (inContactArrayList.size() - 1)){
                            Log.i("APPTAG", "updateList Stopping at index " + inContactArrayList.indexOf(contactUsername));
                            recentConversationTreeMapAdapter.notifyDataSetChanged();
                        }

                    } else {
                        logAndFancyToastException(WhatsAppActivity.this, e);
                    }
                }
            });
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
