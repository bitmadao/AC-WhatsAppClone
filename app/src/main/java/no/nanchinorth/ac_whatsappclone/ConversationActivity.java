package no.nanchinorth.ac_whatsappclone;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;

import com.parse.ParseUser;

public class ConversationActivity extends AppCompatActivity {

    private ParseUser currentUser;
    private String oppositeUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        if(ParseUser.getCurrentUser() != null){
            currentUser = ParseUser.getCurrentUser();
            oppositeUsername = getIntent().getStringExtra("oppositeUsername");
        } else {
            startActivity(new Intent(ConversationActivity.this, LoginActivity.class));
            finish();
        }
    }
}
