package com.example.wehelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

public class Signin extends AppCompatActivity {

    private Button loginbtn , register, forgot;
    private EditText email, password;
    private ProgressBar progressBar;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);
        getSupportActionBar().hide();

        mAuth= FirebaseAuth.getInstance();

        email = findViewById(R.id.email);
        password = findViewById(R.id.password);
        loginbtn = findViewById(R.id.btn_login);
        register = findViewById(R.id.btn_register);
        progressBar = findViewById(R.id.progressBar);
        //check if the user is loggedin or not
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified())
        {
            Intent i = new Intent(Signin.this, MainActivity.class);
            startActivity(i);

        }

//        when the login button is pressed
        loginbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email_id = email.getText().toString();
                String loginPass = password.getText().toString();

                if(!TextUtils.isEmpty(email_id) && !TextUtils.isEmpty(loginPass)){
                    //to show the progress bar while logging in into the app
                    progressBar.setVisibility(View.VISIBLE);

                    mAuth.signInWithEmailAndPassword(email_id, loginPass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                   if( mAuth.getCurrentUser().isEmailVerified())
                                {
                                    openUserMain();
                                }
                                else
                                {
                                    openDialog();
                                }

                            } else {

                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(Signin.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();


                            }

                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });

                }
                //if the fields are empty
                else
                {
                    Toast.makeText(Signin.this, "Enter email and password", Toast.LENGTH_LONG).show();
                }
            }
        });

        //register page
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent regpage = new Intent(Signin.this, RegisterActivity.class);
                startActivity(regpage);
                //finish();
            }
        });
    }


//    create the open new page after logging in
    public void openUserMain(){
        Intent mainpage = new Intent(Signin.this, MainActivity.class);
        startActivity(mainpage);
        finish();
    }
//show verify email dialog
    private void openDialog()
    {
        ShowDialogBox dialog = new ShowDialogBox("Verify your email first.");
        dialog.show(getSupportFragmentManager(), "Login failed");
    }
}
