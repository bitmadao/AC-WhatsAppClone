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
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logAndFancyToastException;

public class UserDirectoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener, AdapterView.OnItemClickListener {

    private ParseUser currentUser;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;

    private boolean isUserArrayPopulated;
    private ArrayList<String> userArray;
    private ArrayAdapter<String> userArrayAdapter;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_directory);

        isUserArrayPopulated = false ;

        if(ParseUser.getCurrentUser() != null){
            currentUser = ParseUser.getCurrentUser();
            setTitle(getString(R.string.title_activity_user_directory));
        } else {
            transitionToLogin();
        }

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayoutUserDirectoryActivityRoot);
        listView = findViewById(R.id.listViewUserDirectoryActivity);

        swipeRefreshLayout.setOnRefreshListener(UserDirectoryActivity.this);
        populateListView();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_user_directory_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.menuItemUserDirectoryIdTBD)
            transitionToWhatsAppActivity();

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRefresh() {
        if(isUserArrayPopulated) {
            updateListView();
        } else {
            populateListView();

        }
        swipeRefreshLayout.setRefreshing(false);
    }

    private void populateListView(){
        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.whereNotEqualTo("username", currentUser.getUsername());
        parseQuery.orderByAscending("username");

        parseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0) {
                        isUserArrayPopulated = true;

                        userArray = new ArrayList<>();

                        for(ParseUser parseUser: objects){
                            userArray.add(parseUser.getUsername());
                        }

                        userArrayAdapter = new ArrayAdapter<>(
                                UserDirectoryActivity.this,
                                android.R.layout.simple_list_item_1,
                                userArray
                        );

                        listView.setAdapter(userArrayAdapter);
                        listView.setOnItemClickListener(UserDirectoryActivity.this);
                    } else {

                        String noUsersString = getString(R.string.activity_user_directory_no_users);
                        listView.setAdapter(
                                new ArrayAdapter<>(UserDirectoryActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        new String[]{noUsersString})
                        );
                    }
                } else {
                    logAndFancyToastException(UserDirectoryActivity.this, e);
                }
            }
        });
    }

    public void updateListView(){
        ParseQuery<ParseUser> updateUserArrayQuery = ParseUser.getQuery();
        updateUserArrayQuery.whereNotEqualTo("username", currentUser.getUsername());
        updateUserArrayQuery.whereNotContainedIn("username",userArray);

        updateUserArrayQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null) {
                    if(objects.size() > 0){
                        for(ParseUser parseUser : objects){
                            userArray.add(parseUser.getUsername());
                        }

                        Collections.sort(userArray, new Comparator<String>() {
                            @Override
                            public int compare(String o1, String o2) {
                                return o1.compareToIgnoreCase(o2);
                            }
                        });

                        userArrayAdapter.notifyDataSetChanged();
                    } else {
                        FancyToast.makeText(
                                UserDirectoryActivity.this,
                                getString(R.string.toast_activity_user_directory_up_to_date),
                                FancyToast.LENGTH_SHORT,
                                FancyToast.INFO,
                                true)
                            .show();
                    }
                } else {
                    logAndFancyToastException(UserDirectoryActivity.this, e);
                }
            }
        });

    }

    private void transitionToWhatsAppActivity(){
        startActivity(new Intent(UserDirectoryActivity.this, LoginActivity.class));
        finish();
    }

    private void transitionToLogin(){
        startActivity(new Intent(UserDirectoryActivity.this, LoginActivity.class));
        finish();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent conversationActivityIntent = new Intent(UserDirectoryActivity.this, ConversationActivity.class);
        conversationActivityIntent.putExtra("oppositeUsername",userArray.get(position));
        startActivity(conversationActivityIntent);
    }
}
