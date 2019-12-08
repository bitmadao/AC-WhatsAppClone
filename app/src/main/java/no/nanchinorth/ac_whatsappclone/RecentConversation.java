package no.nanchinorth.ac_whatsappclone;

import com.parse.ParseObject;

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class RecentConversation {

    private String mConversationOpponent;
    private String mLastMessage;
    private Date mLastMessageDate;

    public RecentConversation(String currentUser, String contactUsername, ParseObject messageObject, String currentUserAlias){
        this.mConversationOpponent = contactUsername;
        StringBuilder stringBuilder = new StringBuilder();

        if(messageObject.getString("sender").equals(currentUser)){
            stringBuilder.append(currentUserAlias);
        } else {
            stringBuilder.append(messageObject.getString("sender")).append(": ");
        }

        if(messageObject.getString("message").length() > 35) {
            stringBuilder.append(messageObject.getString("message").substring(0, 34)).append("...");
        } else {
            stringBuilder.append(messageObject.getString("message"));
        }

        this.mLastMessage = stringBuilder.toString();

        this.mLastMessageDate = messageObject.getCreatedAt();

    }

    public String getmConversationOpponent() {
        return mConversationOpponent;
    }

    public String getmLastMessage() {
        return mLastMessage;
    }

    public String getmLastMessageDate() {
        DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, Locale.JAPANESE);
        return dateFormat.format(this.mLastMessageDate);
    }
}
