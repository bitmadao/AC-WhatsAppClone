package no.nanchinorth.ac_whatsappclone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class ConversationMessageAdapter extends ArrayAdapter<ConversationMessage> {

    private Context context;
    public ConversationMessageAdapter(Context context, ArrayList<ConversationMessage> conversationMessageArrayList){
        super(context,0, conversationMessageArrayList);

        this.context = context;

    }


    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ConversationMessage conversationMessage = getItem(position);

        if(convertView != null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_conversation_message, parent, false);
        }

        TextView txtSender = convertView.findViewById(R.id.txtItemConversationMessageUsername);
        TextView txtDateTime = convertView.findViewById(R.id.txtItemConversationMessageDateTime);
        TextView txtMessage = convertView.findViewById(R.id.txtItemConversationMessageMessage);

        txtSender.setText(conversationMessage.getMessage());
        txtDateTime.setText(conversationMessage.getMessageDate().toString());
        txtMessage.setText(conversationMessage.getMessage());

        return convertView;
    }
}
