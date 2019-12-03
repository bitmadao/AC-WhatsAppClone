package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.parse.SaveCallback;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logAndFancyToastException;

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    private SwipeRefreshLayout swipeRefreshLayoutMessages;
    private ListView listViewMessages;

    private EditText edtMessage;
    private Button btnSend;

    private boolean isConversationHistoryArrayListPopulated;
    private ArrayList<String> conversationHistoryArrayList;
    private ArrayList<String> conversationObjectIdsArrayList;
    private ArrayAdapter<String> conversationHistoryArrayAdapter;

    private ArrayList<ConversationMessage>  conversationMessageArrayList;
    private ConversationMessageAdapter conversationMessageAdapter;

    private ParseUser currentUser;
    private String oppositeUsername;

    private String[] conversationPartiesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        if(ParseUser.getCurrentUser() != null && getIntent().getStringExtra("oppositeUsername") != null){
            currentUser = ParseUser.getCurrentUser();
            oppositeUsername = getIntent().getStringExtra("oppositeUsername");
            setTitle(String.format("Conversation with %s", oppositeUsername)); // todo strings.xml

        }else if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(ConversationActivity.this, WhatsAppActivity.class));
            finish();

        } else {
            startActivity(new Intent(ConversationActivity.this, LoginActivity.class));
            finish();
        }

        listViewMessages = findViewById(R.id.listViewConversationActivity);

        isConversationHistoryArrayListPopulated = false;

        edtMessage = findViewById(R.id.edtConversationActivityMessage);
        btnSend = findViewById(R.id.btnConversationActivitySend);
        btnSend.setOnClickListener(ConversationActivity.this);

        conversationPartiesArray = new String[]{currentUser.getUsername(), oppositeUsername};

        populateMessagesListView();
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == btnSend.getId()){
            btnSendTapped();
        }
    }

    private void btnSendTapped(){
        if(edtMessage.getText().toString().isEmpty() || edtMessage.getText().toString().trim().length() <= 3){
            FancyToast.makeText(ConversationActivity.this,
                    "Message must be longer than 3 non-whitespace characters", //todo strings.xml
                    FancyToast.LENGTH_SHORT,
                    FancyToast.INFO,
                    true)
                    .show();
            return;

        }

        ParseObject parseMessage = new ParseObject("Message");
        parseMessage.put("sender", currentUser.getUsername());
        parseMessage.put("receiver", oppositeUsername);
        parseMessage.put("message", edtMessage.getText().toString().trim());

        parseMessage.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {

                    if(isConversationHistoryArrayListPopulated){
                        edtMessage.setText("");
                        updateMessagesListView();
                    } else {
                        currentUser.add("inContact", oppositeUsername);
                        currentUser.saveInBackground(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if(e != null){
                                    logAndFancyToastException(ConversationActivity.this, e);
                                }
                            }
                        });
                        populateMessagesListView();
                        edtMessage.setText("");
                    }


                } else {
                    logAndFancyToastException(ConversationActivity.this, e);
                }
            }
        });

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

                        isConversationHistoryArrayListPopulated = true;
                        conversationHistoryArrayList = new ArrayList<>();
                        conversationMessageArrayList = new ArrayList<>();
                        conversationObjectIdsArrayList = new ArrayList<>();
                        for (ParseObject object : objects) {
                            conversationMessageArrayList.add(new ConversationMessage(currentUser.getUsername(),object));
/*
                            conversationHistoryArrayList.add(String.format(
                                    "%s says:\n %s", //todo strings.xml
                                    object.getString("sender"),
                                    object.getString("message")));
                            conversationObjectIdsArrayList.add(object.getObjectId());

 */
                        }
/*
                        conversationHistoryArrayAdapter =
                                new ArrayAdapter<>(
                                        ConversationActivity.this,
                                        android.R.layout.simple_list_item_1,
                                        conversationHistoryArrayList
                                );

                        listViewMessages.setAdapter(conversationHistoryArrayAdapter);

 */
                        conversationMessageAdapter = new ConversationMessageAdapter(ConversationActivity.this,conversationMessageArrayList);
                        listViewMessages.setAdapter(conversationMessageAdapter);

                    } else {

                        ArrayAdapter<String> dummyAdapter = new ArrayAdapter<>(ConversationActivity.this,android.R.layout.simple_list_item_1,
                                new String[]{(String.format("No conversation history with %s yet.",oppositeUsername))}); //todo strings.xml
                        listViewMessages.setAdapter(dummyAdapter);
                    }
                } else {
                    logAndFancyToastException(ConversationActivity.this, e);
                }
            }
        });

    }

    private void updateMessagesListView(){
        ParseQuery<ParseObject> parseQuery = ParseQuery.getQuery("Message");
        parseQuery.whereContainedIn("sender",Arrays.asList(conversationPartiesArray));
        parseQuery.whereContainedIn("receiver",Arrays.asList(conversationPartiesArray));
        parseQuery.whereNotContainedIn("objectId",conversationObjectIdsArrayList);

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0){
                        for(ParseObject object : objects){
/*
                            conversationHistoryArrayList.add(String.format(
                                    "%s says:\n %s", // todo strings.xml
                                    object.getString("sender"),
                                    object.getString("message")));

 */
                            conversationMessageArrayList.add(new ConversationMessage(currentUser.getUsername(), object));
                            conversationObjectIdsArrayList.add(object.getObjectId());
                        }

                        conversationMessageAdapter.notifyDataSetChanged();
//                        conversationHistoryArrayAdapter.notifyDataSetChanged();
                    }
                } else {
                    logAndFancyToastException(ConversationActivity.this, e);
                }
            }
        });

    }
}
