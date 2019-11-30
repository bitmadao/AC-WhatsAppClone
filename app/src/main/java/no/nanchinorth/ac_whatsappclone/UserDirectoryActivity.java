package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import java.util.List;

public class UserDirectoryActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {

    private ParseUser currentUser;

    private SwipeRefreshLayout swipeRefreshLayout;
    private ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_directory);

        if(ParseUser.getCurrentUser() != null){
            currentUser = ParseUser.getCurrentUser();
            setTitle("User Directory");
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
        populateListView();
        swipeRefreshLayout.setRefreshing(false);
    }

    private void populateListView(){
        ParseQuery<ParseUser> parseQuery = ParseUser.getQuery();
        parseQuery.whereNotEqualTo("objectId",currentUser.getObjectId());
        parseQuery.orderByAscending("username");

        parseQuery.findInBackground(new FindCallback<ParseUser>() {
            @Override
            public void done(List<ParseUser> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0) {
                        ArrayList<String> userNames = new ArrayList<>();

                        for(ParseUser parseUser: objects){
                            userNames.add(parseUser.getUsername());
                        }

                        listView.setAdapter(
                                new ArrayAdapter<>(
                                        UserDirectoryActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        userNames
                                )
                        );
                    } else {
                        listView.setAdapter(
                                new ArrayAdapter<>(UserDirectoryActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        new String[]{"No users yet..."})
                        );
                    }
                } else {
                    Log.i("APPTAG", e.getMessage());
                    FancyToast.makeText(
                            UserDirectoryActivity.this,
                            getString(R.string.toast_generic_error),
                            FancyToast.LENGTH_LONG,FancyToast.ERROR,true)
                        .show();
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
}
