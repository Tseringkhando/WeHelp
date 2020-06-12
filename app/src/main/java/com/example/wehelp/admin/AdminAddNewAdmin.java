package com.example.wehelp.admin;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wehelp.MainActivity;
import com.example.wehelp.R;
import com.example.wehelp.RegisterActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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
import java.util.Map;

public class AdminAddNewAdmin extends Fragment {
    private TextInputEditText adminFname,adminLname,adminEmail, adminPassword, adminContact;
    private RadioGroup adminGender;
    private RadioButton genderVal;
    private int genderId;
    private RadioButton btnAdminGender;
    private Button adminDob,saveAdmin;
    private TextView viewAdminDob;
    private Date dob;
    private ProgressBar regProgress;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_admin_add_new_admin,container,false);
        //instantiate
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        adminFname=v.findViewById(R.id.adminFirstname);
        adminLname=v.findViewById(R.id.adminLastname);
        adminEmail=v.findViewById(R.id.adminEmail);
        adminContact=v.findViewById(R.id.adminContact);
        adminPassword=v.findViewById(R.id.adminPw);
        adminGender= v.findViewById(R.id.adminGenderGroup);
        genderId=adminGender.getCheckedRadioButtonId();
        genderVal=(RadioButton)v.findViewById(genderId);
        btnAdminGender=(RadioButton)v.findViewById(genderId);
        adminDob=v.findViewById(R.id.btn_register_admin_dob);
        saveAdmin=v.findViewById(R.id.btn_save_admin);
        viewAdminDob=v.findViewById(R.id.adminDobText);

        //check if the user is loggedin or not
        //if not logged in redirect to homepage
        if(mAuth.getCurrentUser()==null)
        {
            startActivity(new Intent(getContext(), MainActivity.class));
        }

        //material datepicker
        //when the dob button is clicked
        adminDob.setOnClickListener(new View.OnClickListener() {
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
                DatePickerDialog dateDialog = new DatePickerDialog(getContext(), datePickerListener, mYear, mMonth, mDay);
                dateDialog.getDatePicker().setMaxDate(maxDate);
                dateDialog.getDatePicker().setMinDate(minDate);
                dateDialog.show();
            }
        });

        //save admin
        saveAdmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String  emailid= adminEmail.getText().toString();
                String pass = adminPassword.getText().toString();
                String fname= adminFname.getText().toString();
                String lname = adminFname.getText().toString();

                if(!TextUtils.isEmpty(emailid) && !TextUtils.isEmpty(fname) && !TextUtils.isEmpty(lname) && !TextUtils.isEmpty(pass) && getDob()!=null ){
                        if (calculateAge(getDob().getTime()) < 18) {
                            Toast.makeText(getContext(), "ERROR: User must be 18+", Toast.LENGTH_LONG).show();
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
                                                    Toast.makeText(getContext(), "Invalid email address", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        Toast.makeText(getContext(), "Error : " + errorMessage, Toast.LENGTH_LONG).show();
                                    }
                                    regProgress.setVisibility(View.INVISIBLE);
                                }
                            });
                        }

                }
                else{
                    Toast.makeText(getContext(), "Enter complete information", Toast.LENGTH_LONG).show();
                }
         }
        });
        return v;
    }

    ///date listener method
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
                viewAdminDob.setText(format);
            }
            else
            {
                Toast.makeText(getContext(), "User must be 18+", Toast.LENGTH_LONG).show();
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

    public Date getDob() {
        return dob;
    }

    public void setDob(Date dob) {
        this.dob = dob;
    }

    //this inserts the data in the database
    private void createUserinDb(String email_id, String uid)
    {

        String fname= adminFname.getText().toString();
        String lname = adminLname.getText().toString();
        Date dob=getDob();

        // Create a new user with a first and last name
        Map<String, Object> user = new HashMap<>();
        user.put("firstname", fname);
        user.put("lastname", lname);
        user.put("isAdmin", true);
        user.put("email", email_id);
        user.put("user_id",uid);
        user.put("dob",dob);
        user.put("contact","");
        user.put("gender",genderVal.getText());
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
//                            openDialog();

                            regProgress.setVisibility(View.INVISIBLE);
                        }
                        else
                        {
                            regProgress.setVisibility(View.VISIBLE);
                        }
                    }
                });

    }

}