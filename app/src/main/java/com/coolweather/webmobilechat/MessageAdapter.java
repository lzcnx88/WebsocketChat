package com.coolweather.webmobilechat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {
    private List<Message> messagesItems;

    static class ViewHolder extends RecyclerView.ViewHolder{
        LinearLayout leftLayout;
        LinearLayout rightLayout;
        TextView leftMame, rightName, leftMessage, rightMessage;

        public ViewHolder(View view){
            super(view);
            leftLayout = (LinearLayout) view.findViewById(R.id.left_message_layout);
            rightLayout = (LinearLayout) view.findViewById(R.id.right_message_layout);
            leftMame = (TextView) view.findViewById(R.id.leftMsgFrom);
            leftMessage = (TextView) view.findViewById(R.id.leftTxtMsg);
            rightName = (TextView) view.findViewById(R.id.rightMsgFrom);
            rightMessage = (TextView) view.findViewById(R.id.rightTxtMsg);
        }
    }

    public MessageAdapter(List<Message> messagesItems){
        this.messagesItems = messagesItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Message msg = messagesItems.get(position);
        if(!msg.isSelf()){
            holder.leftLayout.setVisibility(View.VISIBLE);
            holder.rightLayout.setVisibility(View.GONE);
            holder.leftMame.setText(msg.getFromName());
            holder.leftMessage.setText(msg.getMessage());
        }else{
            holder.rightLayout.setVisibility(View.VISIBLE);
            holder.leftLayout.setVisibility(View.GONE);
            holder.rightName.setText(msg.getFromName());
            holder.rightMessage.setText(msg.getMessage());
        }
    }

    @Override
    public int getItemCount() {
        return messagesItems.size();
    }
}
