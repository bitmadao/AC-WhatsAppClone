package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    private SwipeRefreshLayout swipeRefreshLayoutMessages;
    private ListView listViewMessages;

    private EditText edtMessage;
    private Button btnSend;

    ArrayList<HashMap<String, String>> conversationHistoryArrayList;

    private ParseUser currentUser;
    private String oppositeUsername;

    String[] conversationPartiesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        if(ParseUser.getCurrentUser() != null && getIntent().getStringExtra("oppositeUsername") != null){
            currentUser = ParseUser.getCurrentUser();
            oppositeUsername = getIntent().getStringExtra("oppositeUsername");

        }else if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(ConversationActivity.this, WhatsAppActivity.class));
            finish();

        } else {
            startActivity(new Intent(ConversationActivity.this, LoginActivity.class));
            finish();
        }

        edtMessage = findViewById(R.id.edtConversationActivityMessage);
        btnSend = findViewById(R.id.btnConversationActivitySend);
        btnSend.setOnClickListener(ConversationActivity.this);

        conversationPartiesArray = new String[]{currentUser.getUsername(), oppositeUsername};

        populateMessagesListView();
    }

    @Override
    public void onClick(View v) {

    }

    private void populateMessagesListView(){
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Message");
        parseQuery.whereContainedIn("sender", Arrays.asList(conversationPartiesArray));
        parseQuery.whereContainedIn("receiver",Arrays.asList(conversationPartiesArray));

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0) {
                        for (ParseObject object : objects) {
                            Log.i("APPTAG",
                                    String.format(
                                            "%s says: %s",
                                            object.getString("sender"),
                                            object.getString("message")
                                    )
                            );
                        }
                    }
                }
            }
        });

    }
}
