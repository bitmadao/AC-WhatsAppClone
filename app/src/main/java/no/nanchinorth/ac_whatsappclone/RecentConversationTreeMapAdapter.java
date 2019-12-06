package no.nanchinorth.ac_whatsappclone;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.TreeMap;

public class RecentConversationTreeMapAdapter extends BaseAdapter {

    private TreeMap<String, RecentConversation> mTreeMap;
    private String[] mKeys;
    private Context mContext;


    public RecentConversationTreeMapAdapter(Context context, TreeMap<String, RecentConversation> treeMap){
        mContext = context;
        mTreeMap = treeMap;
        mKeys = mTreeMap.keySet().toArray(new String[0]);

    }
    @Override
    public int getCount() {
        return mTreeMap.size();
    }

    @Override
    public RecentConversation getItem(int position) {
        return mTreeMap.get(mKeys[position]);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        RecentConversation recentConversation = getItem(position);

        if(convertView == null){
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_recent_conversation, parent, false);
        }

        TextView txtOpponent = convertView.findViewById(R.id.txtItemRecentConversationOpponent);
        TextView txtLastMessage = convertView.findViewById(R.id.txtItemRecentConversationLatestMessage);
        TextView txtLastMessageDate = convertView.findViewById(R.id.txtItemRecentConversationLastMessageDate);

        txtOpponent.setText(recentConversation.getConversationOpponent());
        txtLastMessage.setText(recentConversation.getLastMessage());
        txtLastMessageDate.setText(recentConversation.getLastMessageDate());

        return convertView;
    }
}
