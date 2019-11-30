package no.nanchinorth.ac_whatsappclone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.parse.ParseUser;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.*;

public class WhatsAppActivity extends AppCompatActivity implements View.OnClickListener{


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

        listView = findViewById(R.id.listViewWhatsAppActivity);
        String[] noConversations = {"No conversations yet"};
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                WhatsAppActivity.this,
                android.R.layout.simple_list_item_1,
                noConversations);
        listView.setAdapter(arrayAdapter);




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

                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    private void transitionToLogin(){
        startActivity(new Intent(WhatsAppActivity.this,LoginActivity.class));
        finish();
    }
}
