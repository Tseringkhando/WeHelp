package com.example.wehelp.categories;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wehelp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Categories extends Fragment {

    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;
    private RecyclerView cat_recycler;
    private FirestoreRecyclerAdapter adapter;
    //id of the category
    private String documentId;
    //  private HomeViewModel homeViewModel;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_categories, container, false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        cat_recycler= (RecyclerView) root.findViewById(R.id.cat_recyclerview);

        //Query the db
        Query cat_query =firebaseFirestore.collection("categories");
        FirestoreRecyclerOptions <Categorylist_model> lists=  new FirestoreRecyclerOptions.Builder<Categorylist_model>().setQuery(cat_query, Categorylist_model.class).build();

         adapter = new FirestoreRecyclerAdapter<Categorylist_model, CategoryViewHolder>(lists) {
            @NonNull
            @Override
            public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.categorylist, parent, false);
                return new CategoryViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull CategoryViewHolder holder, int position, @NonNull Categorylist_model model) {
                final String name= model.getCategory();

                holder.cat_name.setText(model.getCategory());
                holder.cat_desc.setText(model.getDescription());
                documentId = getSnapshots().getSnapshot(position).getId();

                holder.cat_card.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), Category_post.class);
                        intent.putExtra("category", name);
                        startActivity(intent);
                    }
                });

            }

        };
         cat_recycler.setHasFixedSize(true);
         cat_recycler.setLayoutManager(new LinearLayoutManager(getContext() ));
         cat_recycler.setAdapter(adapter);
        return root;
    }

    private class CategoryViewHolder extends RecyclerView.ViewHolder {
        //the the widgets in the resource file
         private TextView cat_name, cat_desc;
            private CardView cat_card;
        public CategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            cat_name = itemView.findViewById(R.id.category_title);
            cat_desc= itemView.findViewById(R.id.category_subtitle);
            cat_card=itemView.findViewById(R.id.cat_card);
            cat_name.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        adapter.startListening();

    }

    @Override
    public void onStop() {
        super.onStop();
        adapter.stopListening();
    }
}