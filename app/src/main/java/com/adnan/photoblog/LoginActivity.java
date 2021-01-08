package com.adnan.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
public class LoginActivity extends AppCompatActivity {
   //Variables Declaration
    private EditText loginEmailText;
    private EditText loginPassText;
    private Button loginBtn;
    private Button loginRegBtn;
    private ProgressBar progressBar;
    FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        //Variables Initialisation
        mAuth=FirebaseAuth.getInstance();
        loginEmailText=(EditText) findViewById(R.id.login_et_email);
        loginPassText=(EditText) findViewById(R.id.login_et_password);
        loginBtn=(Button)findViewById(R.id.login_btn_login);
        loginRegBtn=(Button) findViewById(R.id.login_btn_register);
        progressBar=(ProgressBar) findViewById(R.id.login_progressBar);
        loginRegBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendToRegister();
            }
        });
        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String loginEmail=loginEmailText.getText().toString();
                String loginPassword=loginPassText.getText().toString();
                if(loginEmail.isEmpty() || loginPassword.isEmpty()){
                    Toast.makeText(LoginActivity.this, "Please, fill data", Toast.LENGTH_SHORT).show();
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                mAuth.signInWithEmailAndPassword(loginEmail, loginPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                sendToMain();
                            }
                            else{
                          String errorMessage=task.getException().getMessage();
                                Toast.makeText(LoginActivity.this, "Error" + errorMessage, Toast.LENGTH_SHORT).show();
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                    }
                });
            }
        });
    }
    //navigate to MainActivity if the user already login
    private void sendToMain() {
        Intent mainIntent =new Intent(LoginActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }
    //navigate to MainActivity if the user already login
    private void sendToRegister() {
        Intent regIntent =new Intent(LoginActivity.this, RegisterActivity.class);
        startActivity(regIntent);
        finish();
    }
    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            sendToMain();
        }
    }
}