package no.nanchinorth.ac_whatsappclone;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.parse.livequery.ParseLiveQueryClient;
import com.parse.livequery.SubscriptionHandling;
import com.shashank.sony.fancytoastlib.FancyToast;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static no.nanchinorth.ac_whatsappclone.ACWACHelperTools.logAndFancyToastException;

public class ConversationActivity extends AppCompatActivity implements View.OnClickListener {

    private ListView listViewMessages;

    private EditText edtMessage;
    private Button btnSend;

    private boolean isConversationHistoryArrayListPopulated;
    private ArrayList<String> conversationObjectIdsArrayList;

    private ArrayList<ConversationMessage>  conversationMessageArrayList;
    private ConversationMessageAdapter conversationMessageAdapter;

    private ParseUser currentUser;
    private String oppositeUsername;
    private ParseLiveQueryClient parseLiveQueryClient ;

    private String[] conversationPartiesArray;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        if(ParseUser.getCurrentUser() != null && getIntent().getStringExtra("oppositeUsername") != null){
            currentUser = ParseUser.getCurrentUser();
            oppositeUsername = getIntent().getStringExtra("oppositeUsername");
            setTitle(String.format(getString(R.string.title_activity_conversation), oppositeUsername));

        }else if (ParseUser.getCurrentUser() != null) {
            startActivity(new Intent(ConversationActivity.this, WhatsAppActivity.class));
            finish();

        } else {
            startActivity(new Intent(ConversationActivity.this, LoginActivity.class));
            finish();
        }

        parseLiveQueryClient = null;

        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(getString(R.string.back4app_server_url_wss)));
        } catch (Exception e) {
            Log.i("APPTAG", e.getMessage());
        }


        listViewMessages = findViewById(R.id.listViewConversationActivity);

        isConversationHistoryArrayListPopulated = false;

        edtMessage = findViewById(R.id.edtConversationActivityMessage);
        btnSend = findViewById(R.id.btnConversationActivitySend);
        btnSend.setOnClickListener(ConversationActivity.this);

        conversationPartiesArray = new String[]{currentUser.getUsername(), oppositeUsername};

        populateMessagesListView();

        queryForNewMessages();
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
                    getString(R.string.toast_activity_conversation_message_req),
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
        parseQuery.orderByAscending("createdAt");

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0) {

                        isConversationHistoryArrayListPopulated = true;
                        conversationMessageArrayList = new ArrayList<>();
                        conversationObjectIdsArrayList = new ArrayList<>();
                        for (ParseObject object : objects) {
                            conversationMessageArrayList.add(new ConversationMessage(currentUser.getUsername(),object));
                            conversationObjectIdsArrayList.add(object.getObjectId());

                        }

                        conversationMessageAdapter = new ConversationMessageAdapter(ConversationActivity.this,conversationMessageArrayList);
                        listViewMessages.setAdapter(conversationMessageAdapter);

                    } else {

                        ArrayAdapter<String> dummyAdapter = new ArrayAdapter<>(ConversationActivity.this,android.R.layout.simple_list_item_1,
                                new String[]{(String.format(getString(R.string.activity_conversation_no_history),oppositeUsername))});
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
        parseQuery.whereNotContainedIn("objectId",(List<String>)conversationObjectIdsArrayList);
        parseQuery.orderByAscending("createdAt");

        parseQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if(e == null){
                    if(objects.size() > 0){
                        for(ParseObject object : objects){
                            conversationMessageArrayList.add(new ConversationMessage(currentUser.getUsername(), object));
                            conversationObjectIdsArrayList.add(object.getObjectId());
                        }

                        sortConversationArrayList();
                        conversationMessageAdapter.notifyDataSetChanged();
                    }
                } else {
                    logAndFancyToastException(ConversationActivity.this, e);
                }
            }
        });

    }

    private void queryForNewMessages(){
        if(parseLiveQueryClient != null){
            ParseQuery<ParseObject> parseQuery = new ParseQuery("Message");
            parseQuery.whereEqualTo("sender", oppositeUsername);
            parseQuery.whereEqualTo("receiver", currentUser.getUsername());

            SubscriptionHandling<ParseObject> subscriptionHandling = parseLiveQueryClient.subscribe(parseQuery);

            subscriptionHandling.handleEvent(SubscriptionHandling.Event.CREATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                @Override
                public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {


                    Handler handler = new Handler(Looper.getMainLooper());
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            liveUpdateFromOpponent(object);
                        }
                    });


                }
            });
        }
    }

    private void liveUpdateFromOpponent(ParseObject messageObject){
        if(isConversationHistoryArrayListPopulated) {

            conversationMessageArrayList.add(new ConversationMessage(currentUser.getUsername(), messageObject));
            sortConversationArrayList();
            conversationObjectIdsArrayList.add(messageObject.getObjectId());
            conversationMessageAdapter.notifyDataSetChanged();

        } else {
            populateMessagesListView();
        }
    }

    private void sortConversationArrayList(){
        Collections.sort(conversationMessageArrayList, new Comparator<ConversationMessage>() {
            @Override
            public int compare(ConversationMessage o1, ConversationMessage o2) {
                return o1.getMessageDate().compareTo(o2.getMessageDate());
            }
        });
    }
}
