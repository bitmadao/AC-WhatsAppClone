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

public class RecentConversationAdapter extends ArrayAdapter<RecentConversation> {

    private Context context;

    public RecentConversationAdapter(Context context, ArrayList<RecentConversation> recentConversationArrayList){
        super(context, 0, recentConversationArrayList);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        RecentConversation recentConversation = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.item_recent_conversation, parent, false);
        }

        TextView txtOpponent = convertView.findViewById(R.id.txtItemRecentConversationOpponent);
        TextView txtLastMessage = convertView.findViewById(R.id.txtItemRecentConversationLatestMessage);
        TextView txtLastMessageDate = convertView.findViewById(R.id.txtItemRecentConversationLastMessageDate);

        txtOpponent.setText(recentConversation.getmConversationOpponent());
        txtLastMessage.setText(recentConversation.getmLastMessage());
        txtLastMessageDate.setText(recentConversation.getmLastMessageDate());

        return convertView ;
    }
}
