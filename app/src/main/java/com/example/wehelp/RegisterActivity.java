package com.example.wehelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class RegisterActivity extends AppCompatActivity {

    private TextInputEditText reg_email, reg_pass, confirm_pass, firstname, lastname;
    private Button regBtn, loginBtn, btn_reg_dob;
    private Date dob;
    private TextView view_reg_dob;
    private ProgressBar regProgress;
    private FirebaseAuth mAuth;
    private  FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        loginBtn = findViewById(R.id.btn_loginPage);
        reg_email= findViewById(R.id.reg_email);
        reg_pass =findViewById(R.id.reg_password);
        confirm_pass = findViewById(R.id.reg_confirm_password);
        regBtn=findViewById(R.id.btn_register);
        regProgress = findViewById(R.id.progressBar);
        firstname=findViewById(R.id.reg_fname);
        lastname=findViewById(R.id.reg_lname);
        btn_reg_dob= findViewById(R.id.btn_register_dob);
        view_reg_dob=findViewById(R.id.view_reg_dob);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        //check if the user is loggedin or not
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified())
        {
            Intent i = new Intent(RegisterActivity.this, MainActivity.class);
            startActivity(i);

        }
        //if the user already has an account
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent gotoLoginIntent = new Intent(RegisterActivity.this, Signin.class);
                startActivity(gotoLoginIntent);
                finish();
            }
        });


        //when the dob button is clicked
        btn_reg_dob.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar c = Calendar.getInstance();
                int mYear = c.get(Calendar.YEAR);
                int mMonth = c.get(Calendar.MONTH);
                int mDay = c.get(Calendar.DAY_OF_MONTH);
                c.add( Calendar.YEAR, -100 ); // Subtract 100 years
                long minDate = c.getTime().getTime(); // Twice!

                final Calendar cx = Calendar.getInstance();
                int mYear2 = c.get(Calendar.YEAR);
                int mMonth2 = c.get(Calendar.MONTH);
                int mDay2 = c.get(Calendar.DAY_OF_MONTH);
                cx.add( Calendar.YEAR, -18);
                long maxDate = cx.getTime().getTime();
                DatePickerDialog dateDialog = new DatePickerDialog(RegisterActivity.this, datePickerListener, mYear, mMonth, mDay);
                dateDialog.getDatePicker().setMaxDate(maxDate);
                dateDialog.getDatePicker().setMinDate(minDate);
                dateDialog.show();
            }
        });

        //when the register button is clicked
        regBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              final String  emailid= reg_email.getText().toString();
              String pass = reg_pass.getText().toString();
              String confirmPass =confirm_pass.getText().toString();
                String fname= firstname.getText().toString();
                String lname = lastname.getText().toString();
                        if(!TextUtils.isEmpty(emailid) && !TextUtils.isEmpty(fname) && !TextUtils.isEmpty(lname) && !TextUtils.isEmpty(pass) && !TextUtils.isEmpty(confirmPass)){
                            if(pass.equals(confirmPass)) {
                                if (calculateAge(getDob().getTime()) < 18) {
                                    Toast.makeText(RegisterActivity.this, "ERROR: User must be 18+", Toast.LENGTH_LONG).show();
                                } else {
                                    regProgress.setVisibility(View.VISIBLE);
                                    mAuth.createUserWithEmailAndPassword(emailid, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                        @Override
                                        public void onComplete(@NonNull Task<AuthResult> task) {
                                            if (task.isSuccessful()) {
                                                FirebaseUser user = mAuth.getCurrentUser();
                                                user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            createUserinDb(emailid, mAuth.getCurrentUser().getUid());
                                                        } else {
                                                            Toast.makeText(RegisterActivity.this, "Invalid email address", Toast.LENGTH_LONG).show();
                                                        }
                                                    }
                                                });
                                            } else {
                                                String errorMessage = task.getException().getMessage();
                                                Toast.makeText(RegisterActivity.this, "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                                            }
                                            regProgress.setVisibility(View.INVISIBLE);
                                        }
                                    });
                                }
                            }else {
                                Toast.makeText(RegisterActivity.this, "Confirm Password and Password Field doesn't match.", Toast.LENGTH_LONG).show();
                            }
                        }
                else{
                    Toast.makeText(RegisterActivity.this, "Enter complete information", Toast.LENGTH_LONG).show();
                      }
                    }
                });


    }
    //this inserts the data in the database
    private void createUserinDb(String email_id, String uid)
    {
        firstname=findViewById(R.id.reg_fname);
        lastname=findViewById(R.id.reg_lname);
        String fname= firstname.getText().toString();
        String lname = lastname.getText().toString();
        Date dob=getDob();

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("firstname", fname);
        user.put("lastname", lname);
        user.put("isAdmin", false);
        user.put("email", email_id);
        user.put("user_id",uid);
        user.put("dob",dob);
        user.put("contact","");
        user.put("profile_image","");
        user.put("datejoined", FieldValue.serverTimestamp());

// Add a new document with a generated ID
        db.collection("users")
                .add(user)
                .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentReference> task) {
                        if(task.isSuccessful())
                        {
                            openDialog();

                            regProgress.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            regProgress.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }


    protected void onStart() {
        super.onStart();
    }

    private void openDialog()
    {
        EmailRegister dialog = new EmailRegister();
        dialog.show(getSupportFragmentManager(), "Verify your email");
    }


    //gettter and setter for dob
    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    private DatePickerDialog.OnDateSetListener datePickerListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker datePicker, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.YEAR, year);
            c.set(Calendar.MONTH, month);
            c.set(Calendar.DAY_OF_MONTH, day);
            if(calculateAge(c.getTimeInMillis())>=18)
            {
                String format = new SimpleDateFormat("MM/dd/yyyy").format(c.getTime());
                setDob(c.getTime());
                view_reg_dob.setText(format);
            }
            else
            {
                Toast.makeText(RegisterActivity.this, "User must be 18+", Toast.LENGTH_LONG).show();
            }
            // tvAge.setText(Integer.toString(calculateAge(c.getTimeInMillis())));
        }
    };

    //calculate age
    //http://www.deboma.com/article/mobile-development/5/android-datepicker-and-age-calculation/
    private int calculateAge(long date){
        Calendar dob = Calendar.getInstance();
        dob.setTimeInMillis(date);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
        if(today.get(Calendar.DAY_OF_MONTH) < dob.get(Calendar.DAY_OF_MONTH)){
            age--;
        }
        return age;
    }
}
