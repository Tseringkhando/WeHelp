package com.example.wehelp.chatbot;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.wehelp.R;

import java.util.ArrayList;

public class Chat_adapter extends RecyclerView.Adapter<Chat_adapter.MyViewHolder> {
    private Context context;
    protected Activity activity;
    private int SELF = 100;
    private ArrayList<BotMessage> messageArrayList;

    public Chat_adapter(ArrayList<BotMessage> messageArrayList) {
        this.messageArrayList = messageArrayList;

    }
    public Chat_adapter(Context ct)
    {this.context=ct;}


    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        if (viewType == SELF) {
            // self message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_chat_self_bubble, parent, false);
        } else {
            // bot message
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_chat_bot_bubble, parent, false);
        }
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull  MyViewHolder holder, int position) {
        BotMessage message = messageArrayList.get(position);
        switch (message.type) {
            case TEXT:
                holder.message.setText(message.getMessage());
                break;
            case IMAGE:
                holder.message.setVisibility(View.GONE);
                ImageView iv = holder.image;
                Glide
                        .with(iv.getContext())
                        .load(message.getUrl())
                        .into(iv);
        }
    }

    @Override
    public int getItemCount() {
        return messageArrayList.size();
    }

    @Override
    public int getItemViewType(int position) {
        BotMessage message = messageArrayList.get(position);
        if (message.getId() != null && message.getId().equals("1")) {
            return SELF;
        }

        return position;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        ImageView image;

        public MyViewHolder(@NonNull View context) {
            super(context);
            message = (TextView) itemView.findViewById(R.id.message);
            image = (ImageView) itemView.findViewById(R.id.image);
        }
    }
}
