package com.example.wehelp;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;


import com.example.wehelp.admin.AdminDashboard;
import com.example.wehelp.chatbot.ChatBotMessage;
import com.example.wehelp.search.SearchList;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.MenuItem;
import android.view.View;


import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.*;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.squareup.picasso.Picasso;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.Menu;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private SwipeRefreshLayout swipeHome;
    private boolean isUserVerified=false, isUserAdmin=false;
    public void setUserVerified(boolean b) {this.isUserVerified=b;}
    public boolean getUserVerified(){return  this.isUserVerified;}
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth= FirebaseAuth.getInstance();
        firestore=FirebaseFirestore.getInstance();
        if(mAuth.getCurrentUser()!=null)
        {
            if(mAuth.getCurrentUser().isEmailVerified())
            {
                setUserVerified(true);
            }
//            https://firebase.google.com/docs/auth/android/firebaseui
            firestore.collection("users").whereEqualTo("user_id",mAuth.getUid())
                    .get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if(task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                               isUserAdmin=document.getBoolean("isAdmin");
                        }
                    }
                    if(isUserAdmin)
                    {
                        startActivity(new Intent(MainActivity.this, AdminDashboard.class));
                    }
                    else
                    {
                        callLayout();
                    }

                }
            });
        }
        else
        {
            callLayout();
        }



    }

    public void callLayout()
    {
        setContentView(R.layout.activity_main);

        swipeHome=findViewById(R.id.swipeHome);
        swipeHome.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                swipeHome.setRefreshing(false);
                finish();
                startActivity(getIntent());
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View nav = navigationView.getHeaderView(0);
        final CircleImageView current_user_image= nav.findViewById(R.id.current_user_image);

        //check if the user is loggedin or not
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified())
        {
            setUserVerified(true);
            isUserVerified=true;

            current_user_image.setVisibility(View.VISIBLE);
            current_user_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent myprofile= new Intent(MainActivity.this, User_profile.class);
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
                            Picasso.with(MainActivity.this).load(profile_image_uri).fit().placeholder(R.drawable.default_profile_img)
                                    .into(current_user_image);
                        }
                    }
                }
            });
        }


        //chatbot button
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent chatbot = new Intent(MainActivity.this, ChatBotMessage.class);
                startActivity(chatbot);    }
        });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_categories)
                .setDrawerLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item3 = menu.findItem(R.id.app_bar_search);
        item3.setVisible(true);
        MenuItem setting = menu.findItem(R.id.action_settings);
        MenuItem item = menu.findItem(R.id.action_login);
        MenuItem item2 = menu.findItem(R.id.action_register);
        MenuItem logout = menu.findItem(R.id.action_signout);


        if(getUserVerified())
        {
            setting.setVisible(true);
            logout.setVisible(true);
            item.setVisible(false);
            item2.setVisible(false);

        }
        else
        {
            setting.setVisible(false);
            logout.setVisible(false);
        }
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case R.id.action_login:
                Intent loginIntent = new Intent(MainActivity.this, Signin.class);
                startActivity(loginIntent);
                return true;

            case R.id.action_settings:

                Intent settingsIntent = new Intent(MainActivity.this, SetupAccount.class);
                startActivity(settingsIntent);

                return true;

            case  R.id.action_signout:
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                return  true;

            case R.id.action_register:
                register();
                return true;

            case R.id.app_bar_search:
                Intent searchIntent = new Intent(MainActivity.this, SearchList.class);
                startActivity(searchIntent);
                return  true;

            default:
                return false;


        }

    }
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

//open login page
    private void logOut() {
        mAuth.signOut();
       Intent openLogin = new Intent(MainActivity.this, Signin.class);
       finish();
       startActivity(openLogin);
    }
//open register page
    private  void register()
    {
        Intent openRegister = new Intent(MainActivity.this, RegisterActivity.class);
        finish();
        startActivity(openRegister);
    }

}
