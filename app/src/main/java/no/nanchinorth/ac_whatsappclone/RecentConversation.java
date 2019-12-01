package no.nanchinorth.ac_whatsappclone;

import com.parse.ParseObject;

public class RecentConversation {

    private String conversationOpponent;
    private String lastMessage;
    private String lastMessageDate;

    public RecentConversation(String currentUser, String contactUsername, ParseObject messageObject){
        this.conversationOpponent = contactUsername;

        if(messageObject.getString("sender").equals(currentUser)){
            this.lastMessage = String.format("You: %s", messageObject.getString("message"));
        } else {
            this.lastMessage = String.format("%s: %s", messageObject.getString("sender"), messageObject.getString("message"));
        }

        this.lastMessageDate = messageObject.getCreatedAt().toString();


    }
}
