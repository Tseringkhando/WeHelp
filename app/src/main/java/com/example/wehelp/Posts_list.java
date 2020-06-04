package com.example.wehelp;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class Posts_list extends RecyclerView.Adapter<Posts_list.ViewHolder> {
    public List<PostsModel> blog_list;
    //DATABASE VARIABLES
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;


    @NonNull
    @Override
    public Posts_list.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(@NonNull Posts_list.ViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}

