package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logoutParseUser;

public class WhatsAppActivity extends AppCompatActivity implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private boolean isListViewPopulated;
    private ArrayList<String> listedMessageIds;

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
            setTitle(String.format("Conversations: %s", currentUser.getUsername()));
        } else {
            transitionToLogin();
        }

        isListViewPopulated = false ;

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutWhatsAppActivityRoot);
        swipeRefreshLayout.setOnRefreshListener(WhatsAppActivity.this);

        listView = findViewById(R.id.listViewWhatsAppActivity);

        populateListView();




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

    private void populateListView(){
        if(currentUser.getList("inContact") != null){
            List<String> usersInContact = currentUser.getList("inContact");

            recentConversationArrayList = new ArrayList<>();
            listedMessageIds = new ArrayList<>();

            for(final String contactUsername: usersInContact){

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
                                Log.i("APPTAG", objects.get(0).getString("message"));
                            } else {
                                recentConversationAdapter.notifyDataSetChanged();
                            }

                        }

                    }
                });

            }


        } else {
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
