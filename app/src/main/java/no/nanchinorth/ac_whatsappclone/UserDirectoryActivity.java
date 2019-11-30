package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.parse.ParseUser;

public class UserDirectoryActivity extends AppCompatActivity {

    ParseUser currentUser;

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

    private void transitionToWhatsAppActivity(){
        startActivity(new Intent(UserDirectoryActivity.this, LoginActivity.class));
        finish();
    }

    private void transitionToLogin(){
        startActivity(new Intent(UserDirectoryActivity.this, LoginActivity.class));
        finish();
    }
}
