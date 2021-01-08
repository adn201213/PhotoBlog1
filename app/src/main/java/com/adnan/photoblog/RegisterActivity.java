package com.adnan.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class RegisterActivity extends AppCompatActivity {
    //Variables Declaration
    private EditText regEmailText;
    private EditText regPassText;
    private EditText confirmPassText;
    private Button regBtn;
    private Button regLoginBtn;
    private ProgressBar regprogressBar;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //Variables Initialisation
        mAuth = FirebaseAuth.getInstance();
        regEmailText = (EditText) findViewById(R.id.register_et_email);
        regPassText = (EditText) findViewById(R.id.register_et_password);
        confirmPassText = (EditText) findViewById(R.id.register_et_confirm_password);
        regBtn = (Button) findViewById(R.id.register_reg_btn);
        regLoginBtn = (Button) findViewById(R.id.register_login_btn);
        regprogressBar = (ProgressBar) findViewById(R.id.register_progressBar);

        //back to login page if the user have already an account
        regLoginBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        //create new account
        regBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String regEmail = regEmailText.getText().toString();
                String regPassword = regPassText.getText().toString();
                String confirmPassWord = confirmPassText.getText().toString();
                if (regEmail.isEmpty() || regPassword.isEmpty() || confirmPassWord.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "Please, fill data, All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!regPassword.equals(confirmPassWord)) {
                    Toast.makeText(RegisterActivity.this, "Please, The PassWord and confirmed Password must match", Toast.LENGTH_SHORT).show();
                    return;
                }
                regprogressBar.setVisibility(View.VISIBLE);
//Create a new User the first way
//                mAuth.createUserWithEmailAndPassword(regEmail, regPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
//                    @Override
//                    public void onComplete(@NonNull Task<AuthResult> task) {
//                        if(task.isSuccessful()){
//                            sendToSetupActivity();
//
//                        }
//                        else{
//                            String errorMessage=task.getException().getMessage();
//                            Toast.makeText(RegisterActivity.this, "Error" + errorMessage, Toast.LENGTH_SHORT).show();
//
//
//
//                        }

//Create a new User the second way
                mAuth.createUserWithEmailAndPassword(regEmail, regPassword).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        sendToSetupActivity();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        Toast.makeText(RegisterActivity.this, "Failure", Toast.LENGTH_LONG).show();
                        regprogressBar.setVisibility(View.INVISIBLE);
                    }
                });

            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {

            sendToMain();
        }
    }

    private void sendToMain() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void sendToSetupActivity() {
        Intent setupAccountIntent = new Intent(RegisterActivity.this, SetupAccountActivity.class);
        startActivity(setupAccountIntent);
        finish();
    }
}