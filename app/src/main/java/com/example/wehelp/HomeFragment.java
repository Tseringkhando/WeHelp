package com.example.wehelp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

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

import java.text.SimpleDateFormat;
import java.util.Date;

import de.hdodenhof.circleimageview.CircleImageView;
import com.example.wehelp.newpost.NewPost;

public class HomeFragment extends Fragment {
    private RecyclerView posts_recycler;
    private CircleImageView current_user_image;
    private EditText edit_post;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private FirestoreRecyclerAdapter adapter;
    private String username;
    private  String uemail;
    private String profile_pic_url;
    private String contactNo;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        firestore= FirebaseFirestore.getInstance();
        mAuth=  FirebaseAuth.getInstance();
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        posts_recycler= root.findViewById(R.id.newsfeed_list);
        current_user_image = root.findViewById(R.id.userimage);
        edit_post =  root.findViewById(R.id.edit_post);

        //MAKING THE POST OPTION AVAILABLE TO THE LOGGED IN USERS ONLY
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()){
            current_user_image.setVisibility(View.VISIBLE);
            current_user_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myprofile= new Intent(getContext(), User_profile.class);
                    myprofile.putExtra("user_id", "");
                    startActivity(myprofile);
                }
            });

            firestore.collection("users").whereEqualTo("user_id",mAuth.getUid()).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful())
                    {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String profile_image = document.getString("profile_image");
                            Uri profile_image_uri = Uri.parse(profile_image);
                            Picasso.with(getContext()).load(profile_image_uri).fit().placeholder(R.drawable.default_profile_img)
                                    .into(current_user_image);
                        }
                    }
                }
            });

            //edti post
            edit_post.setVisibility(View.VISIBLE);

            edit_post.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(getContext(), NewPost.class);
                    startActivity(i);
                }
            });
        }
        else{
            current_user_image.setVisibility(View.GONE);
            edit_post.setVisibility(View.GONE);
        }

        //Query to lists the posts
        final Query post_query= firestore.collection("user_posts").orderBy("date_added", Query.Direction.DESCENDING);
        //Firebase recycler
        FirestoreRecyclerOptions<PostsModel> lists=  new FirestoreRecyclerOptions.Builder<PostsModel>()
                .setQuery(post_query,PostsModel.class)
                .build();
//        FirebaseRecyclerAdapter
        adapter = new FirestoreRecyclerAdapter<PostsModel, PostsViewHolder>(lists) {


            @NonNull
            @Override
            public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_posts_list, parent, false);

                return new PostsViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull final PostsViewHolder holder, int position, @NonNull final PostsModel model) {
                //getting post id
                final String post_id= getSnapshots().getSnapshot(position).getId();
                final String post_category= model.getCategory();
                final String post_desc = model.getDescription();
                holder.btn_deletepost.setVisibility(View.GONE);
                holder.contact_btns.setVisibility(View.VISIBLE);
                //Display date into string format
                SimpleDateFormat simpleFormat = new SimpleDateFormat("dd MMM yyyy, hh:mm:ss a");
                Date d= model.getDate_added();
                holder.date.setText(simpleFormat.format(d));
                holder.category.setText(model.getCategory());
                holder.description.setText(model.getDescription());
                final String uid= model.getUser_id();
                //to set the user name on the post list
                getUsername(new Callback() {
                    @Override
                    public void firebaseResponseCallback(String result) {
                        holder.username.setText(result);
                    }
                } , uid);
                //user email
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

                //call or text user if the contact no. is given
                getUserContact(new Callback() {
                    @Override
                    public void firebaseResponseCallback(final String result) {
                        if(result.length()>0)
                        {
                            final String number= result;
                            holder.btn_post_usr_text.setEnabled(true);
                            holder.btn_post_user_call.setEnabled(true);
                            //call
                            holder.btn_post_user_call.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri calluri= Uri.parse("tel:" + number);
                                    Intent i = new Intent(Intent.ACTION_DIAL, calluri);
                                    try {
                                        startActivity(i);
                                    }catch (SecurityException e)
                                    {
                                        Toast.makeText(getContext(), "Unable to make call", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                            //message
                            holder.btn_post_usr_text.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Uri sms_uri = Uri.parse("smsto:"+result);
                                    Intent sms_intent = new Intent(Intent.ACTION_SENDTO, sms_uri);
                                    sms_intent.putExtra("sms_body", "Replying to :"+post_category+": "+ post_desc);
                                    try {
                                        startActivity(sms_intent);
                                    }catch (SecurityException e)
                                    {
                                        Toast.makeText(getContext(), "Unable to send sms", Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                        }
                        else
                        {
                            holder.btn_post_usr_text.setEnabled(false);
                            holder.btn_post_user_call.setEnabled(false);
                        }
                    }
                },uid);
                //open user profile when the user's profile picture and name are clicked
                holder.post_user_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openUserProfile(uid);
                    }
                });
                holder.username.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openUserProfile(uid);
                        Toast.makeText(getContext(),"clicked",Toast.LENGTH_LONG).show();
                    }
                });

                //to show delete button to the current user if their post exists
                if(mAuth.getCurrentUser()!=null)
                {
                    if(model.getUser_id().equals(mAuth.getCurrentUser().getUid()))
                    {
                        holder.contact_btns.setVisibility(View.GONE);
                        holder.btn_deletepost.setVisibility(View.VISIBLE);
                        holder.btn_deletepost.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
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
                                                                Toast.makeText(getContext(),"Deleted",Toast.LENGTH_LONG).show();
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
                    Picasso.with(getContext()).load(mainImageURI).fit()
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
                    Picasso.with(getContext()).load(ppurl).fit().placeholder(R.drawable.default_profile_img)
                            .into(holder.post_user_image);
                }
                }, uid);


            }
        };

//        posts_recycler.setHasFixedSize(true);
        posts_recycler.setLayoutManager(new LinearLayoutManager(getContext()));
        posts_recycler.setAdapter(adapter);
            return root;
    }

    public String getUemail() {
        return uemail;
    }

    public void setUemail(String uemail) {
        this.uemail = uemail;
    }




    private class PostsViewHolder extends RecyclerView.ViewHolder {
        // widget variables
        private CircleImageView post_user_image;
        private TextView username, date, category, description;
        private ImageView post_image;
        private Button btn_deletepost;
        private Button btn_post_user_contact,btn_post_user_call, btn_post_usr_text;
        private LinearLayout contact_btns;

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
            btn_post_user_call=itemView.findViewById(R.id.btn_post_user_call);
            btn_post_usr_text=itemView.findViewById(R.id.btn_post_user_text);
            contact_btns=itemView.findViewById(R.id.linear_contact);
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

    ///get post user email
    public void getUserEmail(final Callback callback, String user_id)
    {
        //to get user name
        firestore.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
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
    //get contact no.
    public void getUserContact(final Callback callback, String user_id)
    {
        //to get user name
        firestore.collection("users").whereEqualTo("user_id",user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if(task.isSuccessful())
                {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String contact= document.getString("contact");
                        setContactNo(contact);
                    }
                }
                callback.firebaseResponseCallback(getContactNo());
            }
        });
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

    //redirect to user profile when clicked on username and profiel picture
    public void openUserProfile(String user_id)
    {
        Intent myprofile= new Intent(getContext(), User_profile.class);
        myprofile.putExtra("user_id", user_id);
        startActivity(myprofile);
    }

    public String getName() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getProfile_pic_url() {
        return profile_pic_url;
    }
    public void setProfile_pic_url(String profile_pic_url) {
        this.profile_pic_url = profile_pic_url;
    }
    public String getContactNo() { return contactNo; }
    public void setContactNo(String contactNo) { this.contactNo = contactNo; }

}
