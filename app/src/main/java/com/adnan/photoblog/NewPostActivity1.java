
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

public class NewPostActivity1 extends AppCompatActivity {
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
                String desc = newPostEditTextDescription.getText().toString();
                if (desc.isEmpty() || originalpostImageUri == null) {
                    Toast.makeText(NewPostActivity1.this,
                            "the text or Image are empty", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                //  String randomName= random();
                String randomName = UUID.randomUUID().toString();

                StorageReference fileOriginalPath = mStorageRef.child("post_images/original").child(randomName + ".jpg");
                fileOriginalPath.putFile(originalpostImageUri).continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }
                        Log.i(TAG, "onSuccessuri: "+fileOriginalPath.getDownloadUrl());
                        return fileOriginalPath.getDownloadUrl();
                    }
                }).addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        File newImageFile = new File(originalpostImageUri.getPath());
                        try {
                            compressedImageFile = new Compressor(NewPostActivity1.this)
                                    .setMaxHeight(200)
                                    .setMaxWidth(200)
                                    .setQuality(100)
                                    .compressToBitmap(newImageFile);
                            //  .compressToF(newImageFile);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        compressedImageFile.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                        byte[] thumbData = baos.toByteArray();
                        final StorageReference fileThumbPath = mStorageRef.child("post_images/thumbs").child(randomName + ".jpg");
                        UploadTask uploadTaskfileThumbPath =  fileThumbPath.putBytes(thumbData);
                        progressBar.setVisibility(View.INVISIBLE);
                        originalpostImageUri=uri;
                        Toast.makeText(NewPostActivity1.this, "new"+uri, Toast.LENGTH_SHORT).show();
                        uploadTaskfileThumbPath.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                Log.i(TAG, "onSuccessuri: "+fileOriginalPath.getDownloadUrl());
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
                                Log.i(TAG, "onSuccessuri: " + postMap.get("imageOriginalUri"));
                                Log.i(TAG, "onSuccessuri: " + postMap.get("imageThumbUri"));
                                Log.i(TAG, "onSuccess: "+originalpostImageUri);
                                Log.i(TAG, "onSuccess: "+downloadThumbUri1);
                                uploadToCloud(postMap);
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });

                    }
                });

            }
        });


    }

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

    private void BringImagePicker() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMinCropResultSize(512, 512)
                .setAspectRatio(1, 1)
                .start(NewPostActivity1.this);
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(NewPostActivity1.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
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


    public void uploadToCloud( Map<String, Object> postMap){
        FirebaseFirestore firebaseFirestore2=FirebaseFirestore.getInstance();
        firebaseFirestore2.collection("post").add(postMap)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(NewPostActivity1.this, "Post was added Successfully", Toast.LENGTH_SHORT).show();
                        //   Toast.makeText(NewPostActivity.this, "onSuccess: inside" + downloadThumbUri1, Toast.LENGTH_SHORT).show();
                        //   Log.i(TAG, "onComplete1: " +"hello hello"+downloadThumbUri1);
                        sendToMain();
                    }
                }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String errorString = e.getMessage().toString();
                Toast.makeText(NewPostActivity1.this, "FireStore Error: " + errorString, Toast.LENGTH_SHORT).show();
            }
        });



    }
}

//    private void downloadImageProfileUrl() {
//        storageReference.child(PROFILE_IMAGES)
//                .child(auth.getCurrentUser().getUid())
//                .getDownloadUrl()
//                .addOnCompleteListener(new OnCompleteListener<Uri>() {
//                    @Override
//                    public void onComplete(@NonNull Task<Uri> task) {
//                        if (task.isSuccessful()) {
//                            String imageProfileUrl = task.getResult().toString();
//                            user.setProfileImageUrl(imageProfileUrl);
//
//                            print("Download success : " + imageProfileUrl);
//                            print(user.toString());
//
//                            setUserDataToCloud();
//                        } else {
//                            showExceptions(Objects.requireNonNull(task.getException()));
//                        }
//                    }
//                });
//    }
