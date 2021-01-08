package com.adnan.photoblog;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    //Variables Declaration
    private androidx.appcompat.widget.Toolbar maintoolbar;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firebaseFirestore;
    private FloatingActionButton addPostBtn;
    private String currentUser_id;
    BottomNavigationView bottomNavigationView;
    HomeFragment homeFragment;
    NotificationFragment notificationFragment;
    AccountFragment accountFragment;
    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //variables Initialisation



        //ToolBar
        maintoolbar = (androidx.appcompat.widget.Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(maintoolbar);
        getSupportActionBar().setTitle("Photo Blog");
        mAuth = FirebaseAuth.getInstance();
     if(mAuth.getCurrentUser() !=null) {
            //FireBase

            firebaseFirestore = FirebaseFirestore.getInstance();
            //BottomNavigationItemView
            bottomNavigationView = findViewById(R.id.mainActivity_bottomNavigationView);
            //Fragments Initialisation
            homeFragment = new HomeFragment();
            notificationFragment = new NotificationFragment();
            accountFragment = new AccountFragment();

          initializeFragment();

            //home is default fragment
          //  replaceFragment(homeFragment);
            // navigation fragments
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                   Fragment currentFragment=getSupportFragmentManager().findFragmentById(R.id.mainActivity_framLayout);

                    switch (item.getItemId()) {
                        case R.id.mainActivity_bottomNavigationView_home:
                            replaceFragment(homeFragment,currentFragment);
                            return true;
                        case R.id.mainActivity_bottomNavigationView_notification:
                            replaceFragment(notificationFragment,currentFragment);
                            return true;
                        case R.id.mainActivity_bottomNavigationView_account:
                            replaceFragment(accountFragment,currentFragment);
                            return true;
                        default:
                            return false;

                    }
                }
            });

            // bottomNavigationItemView.setOnIt
            //Flotation Button
            addPostBtn = (FloatingActionButton) findViewById(R.id.add_post_btn);
            addPostBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendToNewPostActivity();

                }
            });
        }

  }



    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {

            //navigate to Login Page when the app start
            sendToLogin();
        } else {

            currentUser_id=mAuth.getCurrentUser().getUid();
            firebaseFirestore.collection("usersPhotoBlog").document(currentUser_id).get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().exists()) {


                                    sendTosetupSetting();
                                }
                            } else {
                                String error = task.getException().getMessage();
                                Toast.makeText(MainActivity.this, "(FIRESTORE Retrieve Error) : " + error, Toast.LENGTH_LONG).show();
                            }

                        }
                    });
        }
    }

    //Add Menu To Main Activity

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.main_action_search_btn:
                return true;
            case R.id.main_action_setting_btn:
                sendTosetupSetting();
                return true;
            case R.id.main_action_logout_btn:
                logout();
                return true;
            default:
                return false;

        }

    }
    private void logout() {

        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
    private void sendTosetupSetting() {
        Intent intent = new Intent(MainActivity.this, SetupAccountActivity.class);
        startActivity(intent);
     // finish();
    }

    private void sendToNewPostActivity() {
        Intent intent = new Intent(MainActivity.this, NewPostActivity.class);
        startActivity(intent);
        // finish();
    }
//for Fragment
    private void replaceFragment(Fragment fragment,Fragment currentFragment){
        if (fragment != null) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
              if(fragment==homeFragment){
                  fragmentTransaction.hide(notificationFragment);
                  fragmentTransaction.hide(accountFragment);
              }
            if(fragment==notificationFragment){
                fragmentTransaction.hide(homeFragment);
                fragmentTransaction.hide(accountFragment);
            }
            if(fragment==accountFragment){
                fragmentTransaction.hide(homeFragment);
                fragmentTransaction.hide(notificationFragment);
            }

           fragmentTransaction.show(fragment);
            fragmentTransaction.commit();
        }

    }
    private void initializeFragment() {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();



    fragmentTransaction.add(R.id.mainActivity_framLayout, homeFragment);


            fragmentTransaction.add(R.id.mainActivity_framLayout, notificationFragment);


            fragmentTransaction.add(R.id.mainActivity_framLayout, accountFragment);

        fragmentTransaction.hide(notificationFragment);
        fragmentTransaction.hide(accountFragment);
      fragmentTransaction.commit();

    }




}