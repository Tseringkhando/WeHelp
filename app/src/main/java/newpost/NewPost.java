package newpost;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wehelp.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class NewPost extends AppCompatActivity {
    //UI WIDGETS
    private Spinner categoryDropdown;
    private TextView textview_username;
    private ImageView uploadedImageView;
    private EditText post_description;
    private Button btn_image;
    private Button btn_post;
    private String post_category;
    private Uri photo_url = null;
    private String uploadedPhoto_url="";
    private CircleImageView user_image_view;
    //DATABASE VARIABLES
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private StorageReference storage;
    private CollectionReference categoryCollection ;

    private List<String> categoryList ;
    private ArrayAdapter<String> categoryAdpater;
    private String currentUserId;
    private String currentUserName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        //all the required fields
        uploadedImageView= findViewById(R.id.uploadedImageView);
        uploadedImageView.setVisibility(View.GONE);
        btn_image = findViewById(R.id.imageButton);
        btn_post= findViewById(R.id.btn_post);
        post_description= findViewById(R.id.post_description);
        storage = FirebaseStorage.getInstance().getReference();
        user_image_view=findViewById(R.id.user_image_view);

        //FOR USER NAME
        mAuth= FirebaseAuth.getInstance();
        currentUserId=mAuth.getCurrentUser().getUid();
        textview_username = (TextView) findViewById(R.id.usernameView);

        //load the categories in to dropdown list from the database
        //https://stackoverflow.com/questions/54988533/how-to-populate-a-spinner-with-the-result-of-a-firestore-query
        db = FirebaseFirestore.getInstance();
        categoryCollection = db.collection("categories");
        categoryDropdown = (Spinner) findViewById(R.id.category_dropdown);
        categoryList = new ArrayList<>();
        categoryAdpater = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_spinner_dropdown_item , categoryList);

        categoryAdpater.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categoryDropdown.setAdapter(categoryAdpater);
        categoryCollection.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        String subject = document.getString("category");
                        categoryList.add(subject);
                    }
                    categoryAdpater.notifyDataSetChanged();
                }
            }
        });

      ///CURRENT USER NAME
        if(mAuth.getCurrentUser()!=null && mAuth.getCurrentUser().isEmailVerified())
        {
            db.collection("users")
                    .whereEqualTo("user_id", currentUserId)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                for (QueryDocumentSnapshot document : task.getResult()) {
                                    String  fname = document.getString("firstname");
                                    String lname = document.getString("lastname");
                                    currentUserName= fname.substring(0,1).toUpperCase()+fname.substring(1) +" "+ lname.substring(0,1).toUpperCase()+lname.substring(1);
                                    textview_username.setText(currentUserName);
                                    String post_profile_image = document.getString("profile_image");
                                    Uri ppurl = Uri.parse(post_profile_image);
                                    Picasso.with(NewPost.this).load(ppurl).fit().placeholder(R.drawable.default_profile_img)
                                            .into(user_image_view);
                                }
                            } else {
                                currentUserName="";

                            }
                        }
                    });
         }

        //if the user is not logged in
        else{

        }


        ///////UPLOADING IMAGE////////////////////
        btn_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

               CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setMinCropResultSize(512, 512)
                        .setAspectRatio(3, 2)
                        .start(NewPost.this);

               // uploadedImageView.setVisibility(View.VISIBLE);


            }
        });


        //WHEN THE POST BUTTON IS CLICKED
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(post_description.getText().length()!=0)
                {
                    //with photo
                    if(photo_url!=null)
                    {
                        //PHOTO UPLOAD
                        String imagename=UUID.randomUUID().toString()+"."+getExtension(photo_url);
                        final StorageReference imageRef =storage.child("user_posts/"+imagename);

                        UploadTask  imageUpload= imageRef.putFile(photo_url);
                        imageUpload.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if(!task.isSuccessful()){
                                        System.out.println("photo could not be uploaded");
                                    throw  task.getException();}
                                return imageRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if(task.isSuccessful())
                                {
                                    uploadedPhoto_url=task.getResult().toString();
                                    post_category= categoryDropdown.getSelectedItem().toString();
                                    Map <String , Object> obj = new HashMap<>();
                                    obj.put("category", post_category);
                                    obj.put("user_id",currentUserId);
                                    obj.put("description", post_description.getText().toString());
                                    obj.put("date_added", FieldValue.serverTimestamp());
                                    obj.put("photo_url",uploadedPhoto_url);

                                    db.collection("user_posts").add(obj)
                                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                                    if(task.isSuccessful())
                                                    {
                                                        Toast.makeText(NewPost.this,"successful",Toast.LENGTH_LONG).show();
                                                    }
                                                    else
                                                    {
                                                        Toast.makeText(NewPost.this,"not successful",Toast.LENGTH_LONG).show();
                                                    }
                                                }
                                            });
                                }
                                else{

                                }
                            }
                        });
                    }
                    else{

//                    //without photo
                    post_category= categoryDropdown.getSelectedItem().toString();
                    Map <String , Object> obj = new HashMap<>();
                    obj.put("category", post_category);
                    obj.put("user_id",currentUserId);
                    obj.put("description", post_description.getText().toString());
                    obj.put("date_added", FieldValue.serverTimestamp());
                    obj.put("photo_url",uploadedPhoto_url);

                    db.collection("user_posts").add(obj)
                            .addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                                @Override
                                public void onComplete(@NonNull Task<DocumentReference> task) {
                                    if(task.isSuccessful())
                                    {

                                        Toast.makeText(NewPost.this,"successful",Toast.LENGTH_LONG).show();
                                    }
                                    else
                                    {
                                        Toast.makeText(NewPost.this,"not successful",Toast.LENGTH_LONG).show();
                                    }
                                }
                            });
                    }
                }else{
                    Toast.makeText(NewPost.this,"Description is empty",Toast.LENGTH_LONG).show();
                }


            }



        });



    }
    //back button
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

//TO GET THE EXTENSION OF IMAGE UPLOADED BY THE USER
    private String getExtension(Uri uri) {
        try {
            ContentResolver objContent = getContentResolver();
            MimeTypeMap objMime = MimeTypeMap.getSingleton();
            return objMime.getMimeTypeFromExtension(objContent.getType(uri));
        }catch(Exception e){
            Toast.makeText(NewPost.this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
        return "";
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                uploadedImageView.setVisibility(View.VISIBLE);
                photo_url = result.getUri();
                uploadedImageView.setImageURI(photo_url);

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {

                Exception error = result.getError();

            }
        }

    }

}
