package com.originalstocks.groupchatfirebase.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.originalstocks.groupchatfirebase.Models.ChatMessage;
import com.originalstocks.groupchatfirebase.Models.User;
import com.originalstocks.groupchatfirebase.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.originalstocks.groupchatfirebase.Activities.MainActivity.accessToken;

public class ChatMessageRecyclerAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_MESSAGE_SENT = 1;
    private static final int VIEW_TYPE_MESSAGE_RECEIVED = 2;

    private ArrayList<ChatMessage> mMessages = new ArrayList<>();
    private ArrayList<User> mUsers = new ArrayList<>();
    private Context mContext;

    public ChatMessageRecyclerAdapter(ArrayList<ChatMessage> messages, ArrayList<User> users, Context context) {
        this.mMessages = messages;
        this.mUsers = users;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {

        if (accessToken.equals(mMessages.get(position).getUser().getUser_id())) {
            // If the current user is the sender of the message
            return VIEW_TYPE_MESSAGE_SENT;
        } else {
            //If some other user sent the message
            return VIEW_TYPE_MESSAGE_RECEIVED;
        }

    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       View view;

        if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageHolder(view);
        } else if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageHolder(view);
        }
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        switch (holder.getItemViewType()) {
            case VIEW_TYPE_MESSAGE_SENT:
                final SentMessageHolder sentMessageHolder = (SentMessageHolder) holder;
                sentMessageHolder.txMessageText.setText(mMessages.get(position).getMessage());
               // Date currentTime = Calendar.getInstance().getTime();
                String dateString = new SimpleDateFormat("HH:mm").format(new Date());
                sentMessageHolder.txTimeText.setText(dateString);
                break;

            case VIEW_TYPE_MESSAGE_RECEIVED:
                final ReceivedMessageHolder receivedMessageHolder = (ReceivedMessageHolder) holder;
                long timeRx = mMessages.get(position).getTimestamp().getTime();
                String dateStringRx = new SimpleDateFormat("HH:mm").format(new Date(timeRx));
                receivedMessageHolder.rxTimeText.setText(dateStringRx);

                receivedMessageHolder.messageText.setText(mMessages.get(position).getMessage());
                receivedMessageHolder.userName.setText(mMessages.get(position).getUser().getUsername());

                break;
        }

    }


    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    private class ReceivedMessageHolder extends RecyclerView.ViewHolder {

        TextView messageText;
        TextView userName;
        TextView rxTimeText;

        ReceivedMessageHolder(View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.rx_message_user_name);
            messageText = itemView.findViewById(R.id.rx_message_texts);
            rxTimeText = itemView.findViewById(R.id.rx_message_time);
        }
    }

    private class SentMessageHolder extends RecyclerView.ViewHolder {

        TextView txMessageText, txTimeText;

        SentMessageHolder(View itemView) {
            super(itemView);

            txMessageText = itemView.findViewById(R.id.tx_message_texts);
            txTimeText = itemView.findViewById(R.id.tx_message_time);

        }
    }


}
















