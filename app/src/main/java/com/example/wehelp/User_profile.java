package com.example.wehelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.ServerError;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class User_profile extends AppCompatActivity {
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirestoreRecyclerAdapter adapter;
    private String user_id="", uemail="", ucontact="";

    //    widgets
    private Button btn_edit_profile,btn_call, brn_text, btn_email;
    private CircleImageView profile_image_view;
    private Uri mainImageURI = null;
    private TextView profile_username, profile_email, profile_contact, profile_address;
    private RecyclerView posts_recycler;
    public String getProfile_pic_url() {
        return profile_pic_url;
    }

    public void setProfile_pic_url(String profile_pic_url) {
        this.profile_pic_url = profile_pic_url;
    }

    private String profile_pic_url;

    public String getName() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        getSupportActionBar().hide();
        //instantiation
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        profile_username = findViewById(R.id.profile_username);
        profile_email = findViewById(R.id.profile_email);
        profile_contact = findViewById(R.id.profile_contact);
        profile_image_view = findViewById(R.id.img_user_profile);
        posts_recycler = findViewById(R.id.profile_posts_lists);
        btn_edit_profile=findViewById(R.id.btn_profile_edit);
        btn_call=findViewById(R.id.btn_profile_user_call);
        brn_text=findViewById(R.id.btn_profile_user_text);
        btn_email=findViewById(R.id.btn_profile_user_contact);
        //
        btn_edit_profile.setVisibility(View.GONE);
        user_id=getIntent().getStringExtra("user_id");
        if (user_id.isEmpty() && !mAuth.getCurrentUser().getUid().isEmpty()) {
            user_id = mAuth.getCurrentUser().getUid();
        }
        if(mAuth.getCurrentUser()!=null)
        {
            if(user_id.equals(mAuth.getCurrentUser().getUid()))
            {
                btn_edit_profile.setVisibility(View.VISIBLE);
                btn_edit_profile.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editprofile = new Intent(User_profile.this, SetupAccount.class);
                        startActivity(editprofile);
                    }
                });

                btn_call.setEnabled(false);
                brn_text.setEnabled(false);
                btn_email.setEnabled(false);
            }
        }
        //query to view user details
        db.collection("users").whereEqualTo("user_id", user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        //setAccount_id(document.getId());
                        String fname = document.getString("firstname");
                        String lname = document.getString("lastname");
                        String contact = document.getString("contact");
                        String email = document.getString("email");
                        String profile_image = document.getString("profile_image");
                        mainImageURI = Uri.parse(profile_image);
                        Picasso.with(User_profile.this).load(mainImageURI).fit()
                                .placeholder(R.drawable.default_profile_img)
                                .into(profile_image_view);
                        String fullname = fname + " " + lname;
                        profile_username.setText(fullname);
                        profile_email.setText(email);
                        profile_contact.setText(contact);
                        uemail=email;
                        ucontact=contact;

                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(User_profile.this, "(User not found) : " + error, Toast.LENGTH_LONG).show();
                }

            }
        });

        //disable button if the contact  no. is null
        if(ucontact.length()<0)
        {
            btn_call.setEnabled(false);
            brn_text.setEnabled(false);
        }
        //call text email
        btn_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent=new Intent(Intent.ACTION_SEND);
                    String[] recipients={uemail};
                    intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                    intent.putExtra(Intent.EXTRA_SUBJECT,"From WeHelp");
                    intent.putExtra(Intent.EXTRA_TEXT,"");
                    intent.setType("text/html");
                    intent.setPackage("com.google.android.gm");
                    startActivity(Intent.createChooser(intent, "Send mail"));
                }
                catch (SecurityException e)
                {
                    Toast.makeText(User_profile.this, "Unable to send email", Toast.LENGTH_LONG).show();
                }
            }
        });

        btn_call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri calluri= Uri.parse("tel:" + ucontact);
                Intent i = new Intent(Intent.ACTION_DIAL, calluri);
                try {
                    startActivity(i);
                }catch (SecurityException e)
                {
                    Toast.makeText(User_profile.this, "Unable to make call", Toast.LENGTH_LONG).show();
                }
            }
        });

        brn_text.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri sms_uri = Uri.parse("smsto:"+ucontact);
                Intent sms_intent = new Intent(Intent.ACTION_SENDTO, sms_uri);
                sms_intent.putExtra("sms_body", "From WeHelp: ");
                try {
                    startActivity(sms_intent);
                }catch (SecurityException e)
                {
                    Toast.makeText(User_profile.this, "Unable to send sms", Toast.LENGTH_LONG).show();
                }
            }
        });
        final Query post_query = db.collection("user_posts").whereEqualTo("user_id", user_id).orderBy("date_added", Query.Direction.DESCENDING);
        //Firebase recycler
        FirestoreRecyclerOptions<PostsModel> lists = new FirestoreRecyclerOptions.Builder<PostsModel>()
                .setQuery(post_query, PostsModel.class)
                .build();
//        FirebaseRecyclerAdapter
        adapter = new FirestoreRecyclerAdapter<PostsModel, User_profile.PostsViewHolder>(lists) {
            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_posts_list, parent, false);

                return new User_profile.PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull PostsModel model) {
                //getting post id
                final String post_id= getSnapshots().getSnapshot(position).getId();
                final String post_category= model.getCategory();
                final String post_desc = model.getDescription();
                PostsViewHolder holder2=holder;
                holder.btn_deletepost.setVisibility(View.GONE);
                holder.btn_post_user_contact.setVisibility(View.VISIBLE);
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
                getUserEmail(new Callback() {
                    @Override
                    public void firebaseResponseCallback(String result) {
                        final String res =result;
                        //contact the post user
                        holder.btn_post_user_contact.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent=new Intent(Intent.ACTION_SEND);
                                String[] recipients={res};
                                intent.putExtra(Intent.EXTRA_EMAIL, recipients);
                                intent.putExtra(Intent.EXTRA_SUBJECT,"Replying to : "+post_category+": "+ post_desc);
                                intent.putExtra(Intent.EXTRA_TEXT,"");
                                intent.setType("text/html");
                                intent.setPackage("com.google.android.gm");
                                startActivity(Intent.createChooser(intent, "Send mail"));
                            }
                        });
                    }
                } , uid);

                //to show delete button to the current user if their post exists
                if(mAuth.getCurrentUser()!=null)
                {
                    if(model.getUser_id().equals(mAuth.getCurrentUser().getUid()))
                    {
                        holder.btn_post_user_contact.setVisibility(View.GONE);
                        holder.btn_deletepost.setVisibility(View.VISIBLE);
                        holder.btn_deletepost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(User_profile.this);
                                builder.setTitle(R.string.app_name);
                                builder.setIcon(R.drawable.ic_launcher);
                                builder.setMessage("Delete post forever?")
                                        .setCancelable(false)
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                db.collection("user_posts").document(post_id)
                                                        .delete()
                                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                            @Override
                                                            public void onSuccess(Void aVoid) {
                                                                Toast.makeText(User_profile.this,"Deleted",Toast.LENGTH_LONG).show();
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
                }

                //call back method call finished
                if(model.getPhoto_url()!="")
                {
                    //image view
                    String profile_image = model.getPhoto_url();
                    Uri mainImageURI = Uri.parse(profile_image);
                    Picasso.with(User_profile.this).load(mainImageURI).fit()
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
                        Picasso.with(User_profile.this).load(ppurl).fit().placeholder(R.drawable.default_profile_img)
                                .into(holder.post_user_image);
                    }
                }, uid);

            }
        };

        posts_recycler.setLayoutManager(new LinearLayoutManager(User_profile.this));
        posts_recycler.setAdapter(adapter);

    }
    private class PostsViewHolder extends RecyclerView.ViewHolder {
        // widget variables
        private CircleImageView post_user_image;
        private TextView username, date, category, description;
        private ImageView post_image;
        private Button btn_deletepost, btn_post_user_contact;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            post_user_image = itemView.findViewById(R.id.user_post_profile_image);
            username=itemView.findViewById(R.id.blog_user_name);
            date=itemView.findViewById(R.id.blog_date);
            category=itemView.findViewById(R.id.post_category);
            description=itemView.findViewById(R.id.posted_description);
            post_image=itemView.findViewById(R.id.post_image);
            btn_deletepost=itemView.findViewById(R.id.btn_deletepost);
            btn_post_user_contact=itemView.findViewById(R.id.btn_post_user_contact);
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
        db.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
        db.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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

    public String getUemail() {
        return uemail;
    }

    public void setUemail(String uemail) {
        this.uemail = uemail;
    }

    ///get post user email
    public void getUserEmail(final Callback callback, String user_id)
    {
        //to get user name
        db.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String email= document.getString("email");
                        setUemail(email);
                    }
                }
                callback.firebaseResponseCallback(getUemail());
            }
        });
    }

}


