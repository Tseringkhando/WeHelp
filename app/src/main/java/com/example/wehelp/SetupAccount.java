package com.example.wehelp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupAccount extends AppCompatActivity {
    private Button btn_dob, btn_save_account, btn_not_now;
    private TextView view_dob;
    private TextInputEditText contact_no_field, firstname_field, lastname_field;
    private RadioGroup userGender;
    private RadioButton genderVal,male,female,others;
    private int genderId;
    private Boolean image_changed = false;
    private CircleImageView profile_image_view;
    private Uri mainImageURI = null;
    private String uploadedPhoto_url = "";
    private String user_id, account_id;
    private boolean isChanged = false;
    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private Date dob;
    //for progress dialog to be shown
    private ProgressBar progressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setup_account);
        getSupportActionBar().setTitle("Edit Your Profile");
        btn_dob = findViewById(R.id.btn_dob);
        view_dob = findViewById(R.id.view_dob);
        contact_no_field = findViewById(R.id.acc_contact);
        profile_image_view = findViewById(R.id.upload_profile);
        btn_save_account = findViewById(R.id.btn_save_account);
        btn_not_now = findViewById(R.id.btn_not_now);
        firstname_field = findViewById(R.id.acc_firstname);
        lastname_field = findViewById(R.id.acc_lastname);
        progressBar=findViewById(R.id.progressBar2);
        userGender= findViewById(R.id.radioGroupGenderUser);
        male=findViewById(R.id.radio_male);

        female=findViewById(R.id.radio_female);
        others=findViewById(R.id.radio_others);
        genderId=userGender.getCheckedRadioButtonId();
        genderVal=(RadioButton)findViewById(genderId);
        userGender.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                genderId=userGender.getCheckedRadioButtonId();
                genderVal=(RadioButton)findViewById(genderId);
                System.out.println(genderId + ", "+ genderVal.getText()+", ");
            }
        });

        firebaseAuth = FirebaseAuth.getInstance();
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        calendar.clear();

        //MaterialDatePicker
        MaterialDatePicker.Builder builder = MaterialDatePicker.Builder.datePicker();

        builder.setTitleText("SELECT A DATE");
        final MaterialDatePicker picker = builder.build();

        btn_dob.setOnClickListener(new View.OnClickListener() {
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
                DatePickerDialog dateDialog = new DatePickerDialog(SetupAccount.this, datePickerListener, mYear, mMonth, mDay);
                dateDialog.getDatePicker().setMaxDate(maxDate);
                dateDialog.getDatePicker().setMinDate(minDate);
                dateDialog.show();
            }
        });

        //to view the user's existing name
        firebaseFirestore.collection("users").whereEqualTo("user_id", user_id).get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        setAccount_id(document.getId());
                        String fname = document.getString("firstname");
                        String lname = document.getString("lastname");
                        String contact = document.getString("contact");
                        Date dob_user = document.getDate("dob");
                        firstname_field.setText(fname);
                        lastname_field.setText(lname);
                        contact_no_field.setText(contact);
                        SimpleDateFormat simpleFormat = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
                        view_dob.setText(simpleFormat.format(dob_user));
                        setDob(dob_user);

                        String profile_image = document.getString("profile_image");
                        uploadedPhoto_url= profile_image;

                        mainImageURI = Uri.parse(profile_image);
                        Picasso.with(SetupAccount.this).load(mainImageURI).fit()
                                .placeholder(R.drawable.default_profile_img)
                                .into(profile_image_view);
                        progressBar.setVisibility(View.GONE);
                        String gender = document.getString("gender");
                        if(gender.toLowerCase().equals("male"))
                        {

                        }
                        else if(gender.toLowerCase().equals("female"))
                        {
                            

                        }
                        else{
                        }
                    }
                } else {
                    String error = task.getException().getMessage();
                    Toast.makeText(SetupAccount.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                }

            }
        });

        ///////UPLOADING IMAGE////////////////////
        profile_image_view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startCropImageActivity();
            }
        });


        //FOR SAVE BUTTON
        btn_save_account.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneregex= "^\\+[0-9]{10,13}$";
                String fname= firstname_field.getText().toString();
                 String lasname= lastname_field.getText().toString();
                 String contact=contact_no_field.getText().toString();

                    if(fname.equals("") || lasname.equals(""))
                    {alertBox("Enter full name");}
                        else if(calculateAge(getDob().getTime())<18)
                        { alertBox("ERROR: User must be 18+"); }

                           else if(!contact.matches(phoneregex))
                            { alertBox("ERROR:Invalid contact no.");}
                                //if the user selects new profile picture, picture needs to be uploaded too
                else {
                                if (getImage_changed()) {
                                    //PHOTO UPLOAD
                                    String imagename = UUID.randomUUID().toString() + "." + getExtension(mainImageURI);
                                    final StorageReference imageRef = storageReference.child("user_profile_image/" + imagename);

                                    UploadTask imageUpload = imageRef.putFile(mainImageURI);
                                    imageUpload.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                                        @Override
                                        public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                            if (!task.isSuccessful()) {
                                                throw task.getException();
                                            }
                                            return imageRef.getDownloadUrl();
                                        }
                                    }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Uri> task) {
                                            if (task.isSuccessful()) {
                                                storeToDatabase(task, user_id, firstname_field.getText().toString(), lastname_field.getText().toString(), getDob(), contact_no_field.getText().toString());
                                            }
                                        }
                                    });
                                } else {
                                    storeToDatabase(null, user_id, fname, lasname, getDob(), contact);
                                }
                            }

                    }



            });

        //not now button
        btn_not_now.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

        private void startCropImageActivity () {
            CropImage.activity()
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setMinCropResultSize(512, 512)
                    .setAspectRatio(1, 1)
                    .start(SetupAccount.this);

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
                view_dob.setText(format);
            }
            else
            {
                Toast.makeText(SetupAccount.this, "User must be 18+", Toast.LENGTH_LONG).show();
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

    //DOb is private

        public Date getDob () {
            return dob;
        }

        public void setDob (Date dob){
            this.dob = dob;
        }

        @Override
        protected void onActivityResult ( int requestCode, int resultCode, Intent data){
            super.onActivityResult(requestCode, resultCode, data);

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {

                    mainImageURI = result.getUri();
                    profile_image_view.setImageURI(mainImageURI);
                    setImage_changed(true);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                    Exception error = result.getError();

                }
            }

        }

        public Boolean getImage_changed () {
            return image_changed;
        }

        public void setImage_changed (Boolean image_changed){
            this.image_changed = image_changed;
        }


    //TO GET THE EXTENSION OF IMAGE UPLOADED BY THE USER
    private String getExtension(Uri uri) {
        try {
            ContentResolver objContent = getContentResolver();
            MimeTypeMap objMime = MimeTypeMap.getSingleton();
            return objMime.getMimeTypeFromExtension(objContent.getType(uri));
        }catch(Exception e){
            Toast.makeText(SetupAccount.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return "";
    }


    //METHOD TO STORE THE DATA IN THE FIREBASE DATABASE
    private  void storeToDatabase( Task<Uri> task,  String user_id, String fname, String lname, Date dob, String contact)
    {
        if(task != null) {
            uploadedPhoto_url = task.getResult().toString();
        }
            Map <String , Object> obj = new HashMap<>();
            obj.put("contact",contact);
            obj.put("gender",genderVal.getText().toString());
            obj.put("dob",dob);
            obj.put("firstname",fname);
            obj.put("lastname",lname);
            obj.put("profile_image",uploadedPhoto_url);
                final String u = user_id;
            firebaseFirestore.collection("users").document(getAccount_id()).update(obj).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    progressBar.setVisibility(View.GONE );
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    System.out.println(e.getMessage());
                }
            });
    }


    public String getAccount_id() {
        return account_id;
    }

    public void setAccount_id(String account_id) {
        this.account_id = account_id;
    }

    public void alertBox(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(SetupAccount.this);
        builder.setTitle(R.string.app_name);
        builder.setIcon(R.drawable.ic_launcher);
        builder.setMessage(msg)
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }
}

