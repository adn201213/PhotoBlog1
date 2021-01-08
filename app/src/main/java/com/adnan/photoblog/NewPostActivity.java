
package com.adnan.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;


import id.zelory.compressor.Compressor;

public class NewPostActivity extends AppCompatActivity {
    //Variable Declaration
    private static int MAX_LENGTH = 100;
    private androidx.appcompat.widget.Toolbar newPostActivityToolbar;
    private ImageView newPostImage;
    private EditText newPostEditTextDescription;
    private Button newPostButton;
    private Uri originalpostImageUri = null;
    private ProgressBar progressBar;
    private StorageReference mStorageRef;
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;
    private String currentUser_id;
    Bitmap compressedImageFile;
    String downloadthumbUri;
    Uri downloadThumbUri1;
    //StorageReference fileThumbPath;
    Map<String, Object> postMap;
    private static final String TAG = "NewPostActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);
        newPostActivityToolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.newPostActivity_toolbar);
        setSupportActionBar(newPostActivityToolbar);
        getSupportActionBar().setTitle("Add New Post");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        //FireBase and variables Initialasation
        firebaseAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseFirestore = FirebaseFirestore.getInstance();
        progressBar = (ProgressBar) findViewById(R.id.newPostActivity_progressBar);
        newPostImage = (ImageView) findViewById(R.id.newPostActivity_tv_newPostImage);
        newPostEditTextDescription = (EditText) findViewById(R.id.newPostActivity_et_newPostText);
        newPostButton = (Button) findViewById(R.id.newPostActivit_btn_postBlog);

        //get user id
        currentUser_id = firebaseAuth.getCurrentUser().getUid();

        newPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BringImagePicker();
            }
        });
        //save post
        newPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //get text description
                String desc = newPostEditTextDescription.getText().toString();
                //validation so the text and image is not empty
                if (desc.isEmpty() || originalpostImageUri == null) {
                    Toast.makeText(NewPostActivity.this,
                            "the text or Image are empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                //progress bar
                progressBar.setVisibility(View.VISIBLE);
                //  String randomName= random();
                String randomName = UUID.randomUUID().toString();
                //   upload the image to Firebase Storage
                StorageReference fileOriginalPath = mStorageRef.child("post_images/original").child(randomName + ".jpg");
                fileOriginalPath.putFile(originalpostImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        //    Log.i(TAG, "onSuccessuri: "+fileOriginalPath.getDownloadUrl());
                        return fileOriginalPath.getDownloadUrl();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        //  get comprees image thumb
                        File newImageFile = new File(originalpostImageUri.getPath());
                        try {
                            compressedImageFile = new Compressor(NewPostActivity.this)
                                    .setMaxHeight(125)
                                    .setMaxWidth(125)
                                    .setQuality(50)
                                    .compressToBitmap(newImageFile);
                            //  .compressToF(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                        byte[] thumbData = baos.toByteArray();
                        //upload the compressed thumb image to Firebase Storage
                        final StorageReference fileThumbPath = mStorageRef.child("post_images/thumbs").child(randomName + ".jpg");
                        UploadTask uploadTaskfileThumbPath = fileThumbPath.putBytes(thumbData);
                        progressBar.setVisibility(View.INVISIBLE);
                        originalpostImageUri = uri;
                        //   Toast.makeText(NewPostActivity.this, "new"+uri, Toast.LENGTH_SHORT).show();
                        uploadTaskfileThumbPath.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                //      Log.i(TAG, "onSuccessuri: "+fileOriginalPath.getDownloadUrl());
                                return fileThumbPath.getDownloadUrl();
                            }
                        }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                downloadThumbUri1 = uri;
                                postMap = new HashMap<>();
                                postMap.put("imageOriginalUri", originalpostImageUri.toString());
                                postMap.put("imageThumbUri", downloadThumbUri1.toString());
                                postMap.put("desc", desc);
                                postMap.put("user_id", currentUser_id);
                                postMap.put("timestamp", FieldValue.serverTimestamp());
                                uploadToCloud(postMap);

                                //progress Bar to Invisible
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                });

            }
        });


    }

    //this method to get uri and display the image in imageView
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                originalpostImageUri = result.getUri();
                newPostImage.setImageURI(originalpostImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    //this method to get the photo from galary
    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(NewPostActivity.this);
    }

    //this method to send to main activity
    private void sendToMain() {
        Intent mainIntent = new Intent(NewPostActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }


    //This method to save the post information in FireBase Store
    public void uploadToCloud(Map<String, Object> postMap) {
        FirebaseFirestore firebaseFirestore2 = FirebaseFirestore.getInstance();
        firebaseFirestore2.collection("posts").add(postMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(NewPostActivity.this, "Post was added Successfully", Toast.LENGTH_SHORT).show();
                        sendToMain();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorString = e.getMessage().toString();
                Toast.makeText(NewPostActivity.this, "FireStore Error: " + errorString, Toast.LENGTH_SHORT).show();
            }
        });


    }
}


//this method for random String, note I did not use it
//    public static String random() {
//        Random generator = new Random();
//        StringBuilder randomStringBuilder = new StringBuilder();
//        int randomLength = generator.nextInt(MAX_LENGTH);
//        char tempChar;
//        for (int i = 0; i < randomLength; i++){
//            tempChar = (char) (generator.nextInt(96) + 32);
//            randomStringBuilder.append(tempChar);
//        }
//        return randomStringBuilder.toString();
//    }