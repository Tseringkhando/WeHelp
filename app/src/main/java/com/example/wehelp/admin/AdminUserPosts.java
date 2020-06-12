package com.example.wehelp.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wehelp.Callback;
import com.example.wehelp.PostsModel;
import com.example.wehelp.R;
import com.example.wehelp.categories.Category_post;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminUserPosts extends AppCompatActivity {
private String user_id;
    private RecyclerView posts_recycler;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirestoreRecyclerAdapter adapter;
    private TextView usernamepost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_posts);
        user_id=getIntent().getStringExtra("userid");
        usernamepost=findViewById(R.id.usernamepost);
        firestore= FirebaseFirestore.getInstance();
        mAuth=  FirebaseAuth.getInstance();
        posts_recycler= findViewById(R.id.userpostRecycler);

        //Query to lists the posts
        Query post_query= firestore.collection("user_posts").whereEqualTo("user_id", user_id).orderBy("date_added", Query.Direction.DESCENDING);
        //Firebase recycler
        FirestoreRecyclerOptions<PostsModel> lists=  new FirestoreRecyclerOptions.Builder<PostsModel>()
                .setQuery(post_query,PostsModel.class)
                .build();
//        FirebaseRecyclerAdapter
        adapter = new FirestoreRecyclerAdapter<PostsModel, AdminUserPosts.PostsViewHolder>(lists) {


            @NonNull
            @Override
            public AdminUserPosts.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_posts_list, parent, false);

                return new AdminUserPosts.PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final AdminUserPosts.PostsViewHolder holder, int position, @NonNull PostsModel model) {
                final String post_id= getSnapshots().getSnapshot(position).getId();
                final String post_category= model.getCategory();
                final String post_desc = model.getDescription();
                holder.btn_post_user_contact.setVisibility(View.GONE);
                //Display date into string format
                SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                Date d= model.getDate_added();
                holder.date.setText(simpleFormat.format(d));
                holder.category.setText(model.getCategory());
                holder.description.setText(model.getDescription());
                //to set the user name on the post list
                firestore.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String fname= document.getString("firstname");
                                String title=fname+"'s Posts";
                                String lname= document.getString("lastname");
                                String fullname= fname+" "+lname;
                               holder.username.setText(fullname);
                               usernamepost.setText(title);
                            }
                        }

                    }
                });

                //call back method call finished
                if(model.getPhoto_url()!="")
                {
                    //image view
                    String profile_image = model.getPhoto_url();
                    Uri mainImageURI = Uri.parse(profile_image);
                    Picasso.with(AdminUserPosts.this).load(mainImageURI).fit()
                            .into(holder.post_image);
                }
                else
                {
                    holder.post_image.setVisibility(View.GONE);
                }


                firestore.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful())
                        {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String url= document.getString("profile_image");
                                Uri ppurl = Uri.parse(url);
                                Picasso.with(AdminUserPosts.this).load(ppurl).fit().placeholder(R.drawable.default_profile_img)
                                        .into(holder.post_user_image);
                            }
                        }
                    }
                });


                        holder.btn_deletepost.setVisibility(View.VISIBLE);
                        holder.btn_deletepost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(AdminUserPosts.this);
                                builder.setTitle(R.string.app_name);
                                builder.setIcon(R.drawable.ic_launcher);
                                builder.setMessage("Delete post forever?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                firestore.collection("user_posts").document(post_id)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(AdminUserPosts.this,"Deleted",Toast.LENGTH_LONG).show();
                                                                finish();
                                                                startActivity(getIntent());
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull Exception e) {

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
        };

//        posts_recycler.setHasFixedSize(true);
        posts_recycler.setLayoutManager(new LinearLayoutManager(AdminUserPosts.this));
        posts_recycler.setAdapter(adapter);

    }


    private class PostsViewHolder extends RecyclerView.ViewHolder {
        // widget variables
        private CircleImageView post_user_image;
        private TextView username, date, category, description;
        private ImageView post_image;
        private Button btn_deletepost;
        private Button btn_post_user_contact;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            post_user_image = itemView.findViewById(R.id.user_post_profile_image);
            username=itemView.findViewById(R.id.blog_user_name);
            date=itemView.findViewById(R.id.blog_date);
            category=itemView.findViewById(R.id.post_category);
            description=itemView.findViewById(R.id.posted_description);
            post_image=itemView.findViewById(R.id.post_image);
            btn_deletepost=itemView.findViewById(R.id.btn_deletepost);
            btn_post_user_contact= itemView.findViewById(R.id.btn_post_user_contact);
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