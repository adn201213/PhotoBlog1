package com.adnan.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class SetupAccountActivity extends AppCompatActivity {
    private androidx.appcompat.widget.Toolbar setupAccounttoolbar;

    CircleImageView setupImage;
    private Uri userImageUrI = null;
    private EditText setupName;
    private Button setupButton;
    private String user_id;
    private boolean isChanged = false;
    private StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;
    FirebaseFirestore firebaseFirestore;
    private ProgressBar progressBar;
    private static final String TAG = "SetupAccountActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //initialisation
        setContentView(R.layout.activity_setup_account);
        //FireBase Initialasation
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        //ToolBar
        setupAccounttoolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.setupActivity_toolbar);
        setSupportActionBar(setupAccounttoolbar);
        getSupportActionBar().setTitle("Account Setup");
        //variables Initialasation
        setupName = (EditText) findViewById(R.id.setupSettingAccount_et_yourName);
        setupButton = (Button) findViewById(R.id.setupsettingAccount_btn_saveSettingAccount);
        progressBar = (ProgressBar) findViewById(R.id.setupSettingActivity_progressBar);
        progressBar.setVisibility(View.VISIBLE);
        setupButton.setEnabled(false);
        //retrive data from Firebase
        user_id = firebaseAuth.getCurrentUser().getUid();
        firebaseFirestore.collection("usersPhotoBlog").document(user_id).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            if (task.getResult().exists()) {
                                String name = task.getResult().getString("name");
                                String image = task.getResult().getString("image");
                                Toast.makeText(SetupAccountActivity.this, "name" + name, Toast.LENGTH_LONG).show();
                                userImageUrI = Uri.parse(image);
                                setupName.setText(name);
                                RequestOptions placeholderRequest = new RequestOptions();
                                placeholderRequest.placeholder(R.drawable.default_image);
                                Glide.with(SetupAccountActivity.this).setDefaultRequestOptions(placeholderRequest)
                                        .load(image).into(setupImage);
                            }
                        } else {
                            String error = task.getException().getMessage();
                            Toast.makeText(SetupAccountActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                        }
                        progressBar.setVisibility(View.INVISIBLE);
                        setupButton.setEnabled(true);
                    }
                });

        //handel the data text and image
        setupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final String userName = setupName.getText().toString();
                if (userName.isEmpty() || userImageUrI == null) {
                    Toast.makeText(SetupAccountActivity.this,
                            "the text or Image are empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isChanged) {
                    progressBar.setVisibility(View.VISIBLE);
                    //get usr id
                    user_id = firebaseAuth.getCurrentUser().getUid();

                    //create storage folders in Firebase
                    StorageReference image_path = mStorageRef.child("profile_images").child(user_id + ".jpg");
                    //Upload the photo to Firbase Srtorage
                    image_path.putFile(userImageUrI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                            if (task.isSuccessful()) {
                                storeFireStore(task, userName);
                            } else {
                                String errorMessage = task.getException().getMessage();
                                Toast.makeText(SetupAccountActivity.this, "Image Error"
                                        + errorMessage, Toast.LENGTH_SHORT).show();

                            }
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });
                } else {
                    storeFireStore(null, userName);
                }
            }
        });

        setupImage = findViewById(R.id.setupArtivity_profile_image);
        setupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

                    if (ContextCompat.checkSelfPermission(SetupAccountActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(SetupAccountActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);


                    } else {

                        // start picker to get image for cropping and then use the image in cropping activity

                        BringImagePicker();

                    }


                } else {
                    BringImagePicker();

                }
            }
        });


    }

    //This method to save new settings or update previous settings
    private void storeFireStore(@NonNull Task<UploadTask.TaskSnapshot> task, String userName) {

        //download the photo uri from the Firebase and save it in a variable named download_uri
        Uri download_uri = userImageUrI;
        if (task != null) {
            Task<Uri> result = task.getResult().getMetadata().getReference().getDownloadUrl();
         //   Log.i(TAG, "storeFireStore: "+result.getResult().toString());
            result.addOnSuccessListener(new OnSuccessListener<Uri>() {
                @Override
                public void onSuccess(Uri uri) {
                    String downloadUriStringLink = uri.toString();
                    Log.i(TAG, "downloadUriStringLink: "+downloadUriStringLink);
                    userImageUrI = uri;
                    saveToCloude(uri ,userName);
                }
            });
        } else {
            download_uri = userImageUrI;
            saveToCloude(download_uri ,userName);
        }



    }
    private void saveToCloude(Uri uri1,String userName){

        Map<String, String> userMap = new HashMap<>();
        userMap.put("name", userName);
        userMap.put("image", uri1.toString());
        firebaseFirestore.collection("usersPhotoBlog").document(user_id).set(userMap)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(SetupAccountActivity.this, "User Setting is updated", Toast.LENGTH_SHORT).show();
                            sendToMain();

                        } else {
                            String errorMessage1 = task.getException().getMessage();
                            Toast.makeText(SetupAccountActivity.this, "FireStore Error" + errorMessage1, Toast.LENGTH_SHORT).show();

                        }

                    }
                });


    }

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
               .setAspectRatio(1, 1)
                .start(SetupAccountActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                userImageUrI = result.getUri();
                isChanged = true;
                setupImage.setImageURI(userImageUrI);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(SetupAccountActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
}