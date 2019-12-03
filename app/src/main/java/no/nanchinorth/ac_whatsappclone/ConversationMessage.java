package no.nanchinorth.ac_whatsappclone;

import com.parse.ParseObject;

import java.util.Date;

public class ConversationMessage {
    private String messageSender;
    private String message;
    private Date messageDate;

    public ConversationMessage(String currentUsername, ParseObject messageObject){

        if(messageObject.getString("sender").equals(currentUsername)){
            this.messageSender = "You";

        }else {
            
            this.messageSender = messageObject.getString("sender");
        }

        this.message = messageObject.getString("message");
        this.messageDate = messageObject.getCreatedAt();

    }

    public String getMessageSender() {
        return messageSender;
    }

    public String getMessage() {
        return message;
    }

    public Date getMessageDate() {
        return messageDate;
    }
}
