package com.example.wehelp.categories;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wehelp.Callback;
import com.example.wehelp.PostsModel;
import com.example.wehelp.R;
import com.firebase.ui.firestore.FirestoreRecyclerAdapter;
import com.firebase.ui.firestore.FirestoreRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class Category_post extends AppCompatActivity {
    private RecyclerView posts_recycler;
    private CircleImageView current_user_image;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirestoreRecyclerAdapter adapter;

    public String getName() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;

    public String getProfile_pic_url() {
        return profile_pic_url;
    }

    public void setProfile_pic_url(String profile_pic_url) {
        this.profile_pic_url = profile_pic_url;
    }

    private String profile_pic_url;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cateogry_post);
        String session_category= getIntent().getStringExtra("category");
        getSupportActionBar().setTitle(session_category);
        firestore= FirebaseFirestore.getInstance();
        mAuth=  FirebaseAuth.getInstance();

        posts_recycler= findViewById(R.id.cat_recyclerview);

        //Query to lists the posts
        Query post_query= firestore.collection("user_posts").whereEqualTo("category", session_category).orderBy("date_added", Query.Direction.DESCENDING);
        //Firebase recycler
        FirestoreRecyclerOptions<PostsModel> lists=  new FirestoreRecyclerOptions.Builder<PostsModel>()
                .setQuery(post_query,PostsModel.class)
                .build();
//        FirebaseRecyclerAdapter
        adapter = new FirestoreRecyclerAdapter<PostsModel, Category_post.PostsViewHolder>(lists) {


            @NonNull
            @Override
            public Category_post.PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_posts_list, parent, false);

                return new Category_post.PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final Category_post.PostsViewHolder holder, int position, @NonNull PostsModel model) {

                //Display date into string format
                SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                Date d= model.getDate_added();
                holder.date.setText(simpleFormat.format(d));

                holder.category.setText(model.getCategory());
                holder.description.setText(model.getDescription());
                String uid= model.getUser_id();
                //to set the user name on the post list
                getUsername(new Callback() {
                    @Override
                    public void firebaseResponseCallback(String result) {
                        holder.username.setText(result);
                    }
                } , uid);

                //call back method call finished
                if(model.getPhoto_url()!="")
                {
                    //image view
                    String profile_image = model.getPhoto_url();
                    Uri mainImageURI = Uri.parse(profile_image);
                    Picasso.with(Category_post.this).load(mainImageURI).fit()
                            .into(holder.post_image);
                }
                else
                {
                    holder.post_image.setVisibility(View.GONE);
                }

                //user_profile_image
                getProfileImage(new Callback(){
                    @Override
                    public void firebaseResponseCallback(String result) {
                        String post_profile_image = result;
                        Uri ppurl = Uri.parse(post_profile_image);
                        Picasso.with(Category_post.this).load(ppurl).fit().placeholder(R.drawable.default_profile_img)
                                .into(holder.post_user_image);
                    }
                }, uid);


            }
        };

//        posts_recycler.setHasFixedSize(true);
        posts_recycler.setLayoutManager(new LinearLayoutManager(Category_post.this));
        posts_recycler.setAdapter(adapter);

    }


    private class PostsViewHolder extends RecyclerView.ViewHolder {
        // widget variables
        private CircleImageView post_user_image;
        private TextView username, date, category, description;
        private ImageView post_image;
        private Button btn_1;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            post_user_image = itemView.findViewById(R.id.user_post_profile_image);
            username=itemView.findViewById(R.id.blog_user_name);
            date=itemView.findViewById(R.id.blog_date);
            category=itemView.findViewById(R.id.post_category);
            description=itemView.findViewById(R.id.posted_description);
            post_image=itemView.findViewById(R.id.post_image)   ;
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



    public void getUsername(final Callback callback, String user_id)
    {
        //to get user name
        firestore.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String fname= document.getString("firstname");
                        String lname= document.getString("lastname");
                        String fullname= fname+" "+lname;
                        setUsername(fullname);
                    }
                }
                callback.firebaseResponseCallback(getName());
            }
        });

        //firestore get username finish
    }

    //USER_POST_PROFILE IMAGE
    public void getProfileImage(final Callback callback, String user_id)
    {
        //to get user name
        firestore.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String url= document.getString("profile_image");
                        setProfile_pic_url(url);
                    }
                }
                callback.firebaseResponseCallback(getProfile_pic_url());
            }
        });

        //firestore get username finish
    }

    }

