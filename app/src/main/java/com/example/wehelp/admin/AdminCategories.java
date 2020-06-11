package com.example.wehelp.admin;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wehelp.R;
import com.example.wehelp.categories.Categories;
import com.example.wehelp.categories.Category_post;
import com.example.wehelp.categories.Categorylist_model;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link AdminCategories#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AdminCategories extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    public AdminCategories() {
    }

    //
    private TextInputEditText newCat,newCatDesc;
    private Button btnAddnewCat;
    private RecyclerView adminCatAdapter;
    private String currentAdminId;
    private FirebaseFirestore firestore;
    private FirebaseAuth mAuth;
    private FirestoreRecyclerAdapter adapter;

    public static AdminCategories newInstance(String param1, String param2) {
        AdminCategories fragment = new AdminCategories();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_admin_categories, container, false);
        adminCatAdapter=(RecyclerView) view.findViewById(R.id.adminlistCatRecycler);
        newCat=view.findViewById(R.id.addNewCat);
        newCatDesc=view.findViewById(R.id.newCatDesc);
        btnAddnewCat=view.findViewById(R.id.btnAddCat);
        mAuth=FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        currentAdminId=mAuth.getCurrentUser().getUid();
        //list the categories
        Query cat_query =firestore.collection("categories");
        FirestoreRecyclerOptions<Categorylist_model> lists=
                new FirestoreRecyclerOptions.Builder<Categorylist_model>()
                        .setQuery(cat_query, Categorylist_model.class).build();

        adapter = new FirestoreRecyclerAdapter<Categorylist_model, AdminCategories.AdminCategoryViewHolder>(lists) {
            @Override
            protected void onBindViewHolder(@NonNull AdminCategoryViewHolder holder, int position, @NonNull Categorylist_model model) {

                holder.cat_name.setText(model.getCategory());
                holder.cat_desc.setText(model.getDescription());
                final String documentId = getSnapshots().getSnapshot(position).getId();

                holder.btn_delete_category.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle(R.string.app_name);
                        builder.setIcon(R.drawable.ic_launcher);
                        builder.setMessage("Delete category forever?")
                                .setCancelable(false)
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        firestore.collection("categories").document(documentId)
                                                .delete()
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        Toast.makeText(getContext(),"Deleted",Toast.LENGTH_LONG).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(),"Error!!!",Toast.LENGTH_LONG).show();
                                                    }
                                                });

                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });
                        AlertDialog alert = builder.create();
                        alert.show();
                    }
                });
            }

            @NonNull
            @Override
            public AdminCategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_admin_category_single_view, parent, false);
                return new AdminCategoryViewHolder(view);
            }


        };
        adminCatAdapter.setAdapter(adapter);
//        adminCatAdapter.setHasFixedSize(true);
        adminCatAdapter.setLayoutManager(new LinearLayoutManager(getContext()));


        //saving new category
        btnAddnewCat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String cat = newCat.getText().toString();
                String desc = newCatDesc.getText().toString();
                if(cat.length()>0 && desc.length()>0 && currentAdminId.length()>0)
                {
                    saveCategory(cat,desc,currentAdminId);
                }
                else
                {
                    if(cat.length()<=0)
                        Toast.makeText(getContext(),"Enter Category Title", Toast.LENGTH_LONG).show();
                    else if (desc.length()<=0)
                        Toast.makeText(getContext(),"Enter Category Description", Toast.LENGTH_LONG).show();
                    else
                        Toast.makeText(getContext(),"Unauthorised attempt", Toast.LENGTH_LONG).show();
                }
            }
        });


        // Inflate the layout for this fragment
        return view;
    }

    private class AdminCategoryViewHolder extends RecyclerView.ViewHolder {
        //the the widgets in the resource file
        private TextView cat_name, cat_desc;
        private CardView cat_card;
        private Button btn_delete_category;
        public AdminCategoryViewHolder(@NonNull View itemView) {
            super(itemView);

            cat_name = itemView.findViewById(R.id.admin_cat_title);
            cat_desc= itemView.findViewById(R.id.admin_cat_desc);
            cat_card=itemView.findViewById(R.id.adminCatCard);
            btn_delete_category=itemView.findViewById(R.id.btn_delete_category);

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
    //this inserts the data in the database
    private void saveCategory(String cat, String desc, String user_id)
    {
          // Create a new user with a first and last name
        final Map<String, Object> newCatMap = new HashMap<>();
        newCatMap.put("category", cat);
        newCatMap.put("description", desc);
        newCatMap.put("addedby", user_id);
        newCatMap.put("dateadded", FieldValue.serverTimestamp());

// Add a new document with a generated ID
        firestore.collection("categories")
                .add(newCatMap)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful())
                        {
                        Toast.makeText(getContext(), "Category Added", Toast.LENGTH_LONG).show();
                        newCat.setText("");
                        newCatDesc.setText("");
                            }
                        else
                        {
                            Toast.makeText(getContext(), "Category Not Added", Toast.LENGTH_LONG).show();
                        }
                    }
                });

    }
}