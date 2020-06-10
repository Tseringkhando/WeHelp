package com.example.wehelp.admin;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

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
import com.example.wehelp.Signin;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

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
    private TextView adminusername;
    private CircleImageView adminProfilePicture;
    private Button btnSignOut;
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
        firestore=FirebaseFirestore.getInstance();
        adminusername=view.findViewById(R.id.admin_username);
        adminProfilePicture=view.findViewById(R.id.adminImage);
        card_users=view.findViewById(R.id.card_users);
        card_admins=view.findViewById(R.id.card_admins);
        card_categories=view.findViewById(R.id.card_cat);
        card_addAdmin=view.findViewById(R.id.card_addAdmin);
        card_addCat=view.findViewById(R.id.card_addCat);
        card_posts=view.findViewById(R.id.card_posts);
        card_users.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new Admin_users_list_adapter());
            }
        });
        card_admins.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new AdminListAdapter());
            }
        });

        card_categories.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(new AdminCategories());
            }
        });

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
        // Inflate the layout for this fragment
        return view;
    }


    //open login page
    private void logOut() {
        mAuth.signOut();
        Intent openLogin = new Intent(getContext(), Signin.class);
        startActivity(openLogin);
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