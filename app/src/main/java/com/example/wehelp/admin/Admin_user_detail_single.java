package com.example.wehelp.admin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wehelp.R;
import com.example.wehelp.User_profile;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class Admin_user_detail_single extends AppCompatActivity {
    private TextView fname,lname, email, contact, gender, datejoined,dob;
    private ImageView profileimage_view;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String user_id="",accountId;
    private Uri mainImageURI = null;
    private Button btnDel,btnViewPosts,btnAddAdmin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_user_detail_single);
        user_id=getIntent().getStringExtra("user_id");
        fname= findViewById(R.id.fnameview);
        lname=findViewById(R.id.lnameview);
        email=findViewById(R.id.email_view);
        gender=findViewById(R.id.gender_view);
        dob=findViewById(R.id.dob_view);
        datejoined=findViewById(R.id.datejoin_view);
        contact=findViewById(R.id.contact_view);
        profileimage_view=findViewById(R.id.profileimage_view);
        btnDel=findViewById(R.id.btn_delete_user);
        btnAddAdmin=findViewById(R.id.btnAddAsAdmin);
        btnViewPosts=findViewById(R.id.btn_view_userposts);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        //query to view user details
        db.collection("users").whereEqualTo("user_id", user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        setAccountId(document.getId());
                        fname.setText(document.getString("firstname"));
                        lname.setText(document.getString("lastname"));
                        contact.setText(document.getString("contact"));
                        email.setText(document.getString("email"));
                        gender.setText(document.getString("gender"));

                        Timestamp d= document.getTimestamp("dob");
                        Timestamp dj = document.getTimestamp("datejoined");
                        SimpleDateFormat dobformat= new SimpleDateFormat("dd MMM, yyyy");
                        SimpleDateFormat sfd = new SimpleDateFormat("dd MMM yyyy, HH:mm:ss");
                        Date dobdate = d.toDate();
                        Date djoined = dj.toDate();
                        dob.setText(dobformat.format(dobdate));
                        datejoined.setText(sfd.format(djoined));
                        String profile_image = document.getString("profile_image");
                        if(profile_image.length()>0)
                        {
                            mainImageURI = Uri.parse(profile_image);
                            Picasso.with(Admin_user_detail_single.this).load(mainImageURI).fit()
                                    .placeholder(R.drawable.default_profile_img)
                                    .into(profileimage_view);
                        }

                        if(document.getBoolean("isAdmin")){
                            btnAddAdmin.setText("Remove Admin");
                            btnAddAdmin.setTextSize(14);
                            btnAddAdmin.setTextColor(Color.parseColor("#F44336"));
                            btnAddAdmin.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    final  Map<String, Object> user = new HashMap<>();
                                    user.put("isAdmin", false);
                                    AlertDialog.Builder builder = new AlertDialog.Builder(Admin_user_detail_single.this);
                                    builder.setTitle(R.string.app_name);
                                    builder.setIcon(R.drawable.ic_launcher);
                                    builder.setMessage("Are you sure to remove Admin?")
                                            .setCancelable(false)
                                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog, int id) {
                                                    db.collection("users").document(getAccountId())
                                                            .update(user)
                                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                @Override
                                                                public void onComplete(@NonNull Task<Void> task) {
                                                                    Toast.makeText(Admin_user_detail_single.this,user_id,Toast.LENGTH_LONG).show();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
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
                        else
                        {
                            btnAddAdmin.setEnabled(true);
                        }


                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(Admin_user_detail_single.this, "(User not found) : " + error, Toast.LENGTH_LONG).show();
                }

            }
        });

        //delete user
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(Admin_user_detail_single.this);
                builder.setTitle(R.string.app_name);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("Are you sure to delete user?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.collection("users").document(user_id)
                                        .delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Toast.makeText(Admin_user_detail_single.this,"User Deleted",Toast.LENGTH_LONG).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Admin_user_detail_single.this,"Error!!!",Toast.LENGTH_LONG).show();
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

        //view user's posts
        btnViewPosts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent viewposts= new Intent(Admin_user_detail_single.this, AdminUserPosts.class);
                viewposts.putExtra("userid",user_id);
                startActivity(viewposts);

            }
        });
        //set user as admin
        btnAddAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final  Map<String, Object> user = new HashMap<>();
                user.put("isAdmin", true);
                AlertDialog.Builder builder = new AlertDialog.Builder(Admin_user_detail_single.this);
                builder.setTitle(R.string.app_name);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("Are you sure to add user as Admin?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                db.collection("users").document(getAccountId())
                                        .update(user)
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(Admin_user_detail_single.this,user_id,Toast.LENGTH_LONG).show();
                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
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

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }
}