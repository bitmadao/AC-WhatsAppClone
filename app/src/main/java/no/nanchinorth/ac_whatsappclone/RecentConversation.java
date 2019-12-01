package no.nanchinorth.ac_whatsappclone;

import com.parse.ParseObject;

import java.text.DateFormat;
import java.util.Locale;

public class RecentConversation {

    private String conversationOpponent;
    private String lastMessage;
    private String lastMessageDate;

    public RecentConversation(String currentUser, String contactUsername, ParseObject messageObject){
        this.conversationOpponent = contactUsername;
        StringBuilder stringBuilder = new StringBuilder();

        if(messageObject.getString("sender").equals(currentUser)){
            stringBuilder.append("You: ");
        } else {
            stringBuilder.append(messageObject.getString("sender")).append(": ");
        }

        if(messageObject.getString("message").length() > 35) {
            stringBuilder.append(messageObject.getString("message").substring(0, 34)).append("...");
        } else {
            stringBuilder.append(messageObject.getString("message"));
        }

        this.lastMessage = stringBuilder.toString();

        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.JAPANESE);
        this.lastMessageDate = dateFormat.format(messageObject.getCreatedAt());

    }

    public String getConversationOpponent() {
        return conversationOpponent;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }
}
