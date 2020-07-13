package com.example.wehelp.admin;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.wehelp.MainActivity;
import com.example.wehelp.R;
import com.example.wehelp.Signin;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import de.hdodenhof.circleimageview.CircleImageView;

public class AdminDashboard extends AppCompatActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;
    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        if (mAuth.getCurrentUser() == null) {
            startActivity(new Intent(AdminDashboard.this, MainActivity.class));
        } else {
            setContentView(R.layout.activity_admin_drawer);
            DrawerLayout drawer = findViewById(R.id.adminDrawer);
            NavigationView navigationView = findViewById(R.id.nav_admin_view);
            View nav = navigationView.getHeaderView(0);
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_dashboard, R.id.nav_admin_categories, R.id.nav_users, R.id.nav_admins, R.id.nav_add_admin)
                    .setDrawerLayout(drawer)
                    .build();


            NavController navController = Navigation.findNavController(AdminDashboard.this, R.id.admin_fragment);
            NavigationUI.setupActionBarWithNavController(AdminDashboard.this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);
        }

    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.admin_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }


}