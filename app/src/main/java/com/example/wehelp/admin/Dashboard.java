package com.example.wehelp.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.example.wehelp.R;
import com.example.wehelp.SetupAccount;
import com.example.wehelp.Signin;
import com.example.wehelp.User_profile;
import com.example.wehelp.admin.search.AdminList;
import com.example.wehelp.admin.search.Admin_users_list;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Dashboard#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Dashboard extends Fragment{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String currentAdminId="";
    private TextView adminusername;
    private CircleImageView adminProfilePicture;
    private Button btnSignOut,btnEditProfile;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private CardView card_users,card_admins, card_categories, card_addAdmin, card_addCat,card_posts;

    public Dashboard() {
        // Required empty public constructor
    }

    public static Dashboard newInstance(String param1, String param2) {
        Dashboard fragment = new Dashboard();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        View view =inflater.inflate(R.layout.fragment_dashboard, container, false);
        mAuth= FirebaseAuth.getInstance();
        currentAdminId=mAuth.getCurrentUser().getUid();
        firestore=FirebaseFirestore.getInstance();
        adminusername=view.findViewById(R.id.admin_username);
        adminProfilePicture=view.findViewById(R.id.adminImage);
        card_users=view.findViewById(R.id.card_users);
        card_admins=view.findViewById(R.id.card_admins);
        card_categories=view.findViewById(R.id.card_cat);
        card_addAdmin=view.findViewById(R.id.card_addAdmin);
        card_addCat=view.findViewById(R.id.card_addCat);
        card_posts=view.findViewById(R.id.card_posts);
        btnEditProfile=view.findViewById(R.id.btn_edit);
//        card functionalities
        card_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new Admin_users_list());
            }
        });
        card_admins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new AdminList());
            }
        });

        card_categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new AdminCategories());
            }
        });

//        sign out button
        btnSignOut=view.findViewById(R.id.btn_admin_signout);
        btnSignOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setTitle(R.string.app_name);
                builder.setIcon(R.drawable.ic_launcher);
                builder.setMessage("Confirm sign out?")
                        .setCancelable(false)
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                logOut();
                                //finish();
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

//        edit admins profile
        btnEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myprofile= new Intent(getContext(), SetupAccount.class);
                myprofile.putExtra("user_id", "");
                startActivity(myprofile);
            }
        });

        //admin name and photo
        //MAKING THE POST OPTION AVAILABLE TO THE LOGGED IN USERS ONLY
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified()) {

            adminProfilePicture.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myprofile= new Intent(getContext(), User_profile.class);
                    myprofile.putExtra("user_id", "");
                    startActivity(myprofile);
                }
            });

            firestore.collection("users").whereEqualTo("user_id", currentAdminId).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String profile_image = document.getString("profile_image");
                            Uri profile_image_uri = Uri.parse(profile_image);
                            Picasso.with(getContext()).load(profile_image_uri).fit().placeholder(R.drawable.default_profile_img)
                                    .into(adminProfilePicture);
                            String fname = document.getString("firstname");
                            String lname = document.getString("lastname");
                            String fullname = fname + " " + lname;
                            adminusername.setText(fullname);
                        }
                    }
                }
            });
        }




        // Inflate the layout for this fragment
        return view;
    }


    //open login page
    private void logOut() {
        mAuth.signOut();
        if(mAuth.getCurrentUser()==null)
        {
            Intent openLogin = new Intent(getContext(), Signin.class);
            startActivity(openLogin);
        }

    }

    //change fragment on card click
    private void changeFragment(Fragment f)
    {
        Fragment newFragment = f;
        FragmentTransaction transaction = getParentFragmentManager().beginTransaction();
        transaction.replace(R.id.admin_fragment, newFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }



}