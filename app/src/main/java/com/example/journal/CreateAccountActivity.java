package com.example.journal;

import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.journal.util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class CreateAccountActivity extends AppCompatActivity {
    private Button createAccountBtn;
    private Button needLoginBtn;
    private EditText userName;
    private EditText accountEmail;
    private EditText accountPassword;
    private EditText confirmPassword;
    private ProgressBar accountProgress;


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentuser;

    //firestore connection
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        //this code is to remove the shadow under the action bar
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();

        createAccountBtn = findViewById(R.id.create_account_btn);
        needLoginBtn = findViewById(R.id.need_login_btn);
        userName = findViewById(R.id.user_name);
        accountEmail = findViewById(R.id.account_email);
        accountPassword = findViewById(R.id.account_password);
        confirmPassword = findViewById(R.id.confirm_password);
        accountProgress = findViewById(R.id.account_progress);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                currentuser = firebaseAuth.getCurrentUser();

                if (currentuser != null){
                    //user already login
                } else{
                    // no user logged in
                }
            }
        };

        needLoginBtn.setOnClickListener(view -> {
            startActivity(new Intent(CreateAccountActivity.this, LoginActivity.class));
        });

        createAccountBtn.setOnClickListener(view -> {

            String email = accountEmail.getText().toString().trim();
            String password = accountPassword.getText().toString().trim();
            String username = userName.getText().toString().trim();
            String confirmpswd = confirmPassword.getText().toString().trim();

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){

                if (confirmpswd.equals(password)){

                    createAccountEmailPassword(email,password,username);
                } else {

                    accountPassword.setError("i did not match confirm password");
                    confirmPassword.setError("i did not match password");
                    Toast.makeText(this, "confirm password do not match password", Toast.LENGTH_SHORT).show();
                }

            } else {

                 Toast.makeText(CreateAccountActivity.this, "fields cannot be empty", Toast.LENGTH_LONG).show();
            }



        });


    }//ONCREATE......


    private void createAccountEmailPassword(String email, String password, String username){

            if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)){

                accountProgress.setVisibility(View.VISIBLE);

                firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        if (task.isSuccessful()){
                            //we take user to adjournal activity
                            currentuser = firebaseAuth.getCurrentUser();

                            assert currentuser != null;
                            String currentUserId = currentuser.getUid();

                            //create user map so we can create a user in the user collection
                            Map<String, String> userObj = new HashMap<>();

                            userObj.put("userId", currentUserId);
                            userObj.put("userName", username);
                            userObj.put("password", password);

                            //saving data to our firestore database
                            collectionReference.add(userObj).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {

                                    documentReference.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                                            if (task.getResult().exists()){

                                                accountProgress.setVisibility(View.INVISIBLE);

                                                JournalApi journalApi = JournalApi.getInstance();
                                                journalApi.setUserName(username);
                                                journalApi.setUserId(currentUserId);

                                                Toast.makeText(CreateAccountActivity.this, "account creation successful", Toast.LENGTH_SHORT).show();

                                                String name = task.getResult().getString("username");
                                                Intent intent = new Intent(CreateAccountActivity.this, PostJournalActivity.class);
                                                intent.putExtra("username", username);
                                                intent.putExtra("userId", currentUserId);


                                                startActivity(intent);
                                                finish();
                                            } else {

                                                accountProgress.setVisibility(View.INVISIBLE);
                                            }
                                        }
                                    });
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {

                                }
                            });
                            
                        } else{
                            //something went wrong
                        }
                    }
                }).addOnFailureListener(e -> {

                });

            } else{


            }
    }




    @Override
    protected void onStart() {
        super.onStart();

        currentuser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }
}