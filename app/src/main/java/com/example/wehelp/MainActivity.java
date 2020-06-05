package com.example.wehelp;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;


import com.example.wehelp.admin.AdminAddNewAdmin;
import com.example.wehelp.chatbot.ChatBotMessage;
import com.example.wehelp.search.SearchList;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import android.view.MenuItem;
import android.view.View;


import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.*;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.widget.SearchView;

import de.hdodenhof.circleimageview.CircleImageView;

public class MainActivity extends AppCompatActivity {
   // private CircleImageView current_user_image;
    private AppBarConfiguration mAppBarConfiguration;
    private FirebaseAuth mAuth;
    private boolean isUserVerified=false;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mAuth= FirebaseAuth.getInstance();
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        View nav = navigationView.getHeaderView(0);
        CircleImageView current_user_image= nav.findViewById(R.id.current_user_image);

        //check if the user is loggedin or not
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified())
            {
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
            }

        //chatbot button
        FloatingActionButton fab = findViewById(R.id.floatingActionButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Uri gmmIntentUri = Uri.parse("geo:0,0?q=");
//                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
//                mapIntent.setPackage("com.google.android.apps.maps");
//                startActivity(mapIntent);
//                Intent chatbot = new Intent(MainActivity.this, ChatBotMessage.class);
//                startActivity(chatbot);
                Intent openadmin = new Intent(MainActivity.this, AdminAddNewAdmin.class);
                startActivity(openadmin);

            }
        });

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_categories,R.id.nav_organisations,R.id.nav_friends,R.id.nav_profile)
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
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView =
                (SearchView) menu.findItem(R.id.app_bar_search).getActionView();
       // searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setQueryHint(getResources().getString(R.string.search_hint));
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Intent searchIntent = new Intent(MainActivity.this, SearchList.class);
                searchIntent.putExtra("searchText", query);
                startActivity(searchIntent);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {

                return false;
            }
        });


        if(isUserVerified==true)
        {
            MenuItem item = menu.findItem(R.id.action_login);
            item.setVisible(false);

            MenuItem item2 = menu.findItem(R.id.action_register);
            item2.setVisible(false);


        }
        else
        {
            MenuItem setting = menu.findItem(R.id.action_settings);
            setting.setVisible(false);

            MenuItem logout = menu.findItem(R.id.action_signout);
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
       startActivity(openLogin);
    }
//open register page
    private  void register()
    {
        Intent openRegister = new Intent(MainActivity.this, RegisterActivity.class);
        startActivity(openRegister);
    }

}
