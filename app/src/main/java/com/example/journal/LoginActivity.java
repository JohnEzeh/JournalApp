package com.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.journal.util.JournalApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private Button loginBtn;
    private Button wannacreateAccountbtn;
    private AutoCompleteTextView emailEdit;
    private EditText passwordEdit;
    private ProgressBar loginProgressBar;


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    //firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();

        //this code is to remove the shadow under the action bar
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);
        

        emailEdit = findViewById(R.id.email_et);
        passwordEdit = findViewById(R.id.password_et);
        loginBtn = findViewById(R.id.login_btn);
        loginProgressBar = findViewById(R.id.login_progress);
     wannacreateAccountbtn = findViewById(R.id.login_create_account_btn);

        wannacreateAccountbtn.setOnClickListener(view -> {
            startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
            finish();
        });

        loginBtn.setOnClickListener(view -> {

            loginEmailPassword(emailEdit.getText().toString().trim(), passwordEdit.getText().toString().trim());

        });
    }

    private void loginEmailPassword(String email, String pwd) {
        loginProgressBar.setVisibility(View.VISIBLE);

        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(pwd)){

            firebaseAuth.signInWithEmailAndPassword(email,pwd).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    user = firebaseAuth.getCurrentUser();
                    assert user != null;
                    String currentUserId = user.getUid();

                    collectionReference.whereEqualTo("userId", currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                        @Override
                        public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                            if (error != null){

                            }
                            assert value != null;
                            if (!value.isEmpty()){
                                loginProgressBar.setVisibility(View.INVISIBLE);
                                for (QueryDocumentSnapshot snapshot: value){
                                    JournalApi journalApi = JournalApi.getInstance();
                                    journalApi.setUserName(snapshot.getString("userName"));
                                    journalApi.setUserId(snapshot.getString("userId"));
                                    Toast.makeText(LoginActivity.this, "log in successful", Toast.LENGTH_SHORT).show();
                                    //go to postJournal Activity
                                    startActivity(new Intent(LoginActivity.this, PostJournalActivity.class));
                                }
                            }
                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    loginProgressBar.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginActivity.this, "error occured due to" + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });

        } else{

            loginProgressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "please sir fill in your email and password", Toast.LENGTH_SHORT).show();
        }

    }


}