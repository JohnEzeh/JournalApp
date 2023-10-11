package com.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.journal.util.JournalApi;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.type.Color;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private Button getStartedbutton;
    private FirebaseAuth firebaseAuth;
    private FirebaseUser currentUser;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private CollectionReference collectionReference = db.collection("Users");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();

        getStartedbutton = findViewById(R.id.get_started_btn);

        //this code is to remove the shadow under the action bar
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
             currentUser = firebaseAuth.getCurrentUser();
             if (currentUser != null){

                 currentUser = firebaseAuth.getCurrentUser();
                 String currentUserId = currentUser.getUid();

                 collectionReference.whereEqualTo("userId",currentUserId).addSnapshotListener(new EventListener<QuerySnapshot>() {
                     @Override
                     public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                         if (error != null){
                             return;
                         }

                         String name;
                         if (!value.isEmpty()){
                             for (QueryDocumentSnapshot snapshot: value){
                                 JournalApi journalApi = JournalApi.getInstance();
                                 journalApi.setUserId(snapshot.getString("userId"));
                                 journalApi.setUserName(snapshot.getString("userName"));
                                 startActivity(new Intent(MainActivity.this, JournalListActivity.class));
                                 finish();

                             }
                         }

                     }
                 });

             } else {

             }

            }
        };

        getStartedbutton.setOnClickListener(view -> {

          startActivity(new Intent(MainActivity.this, LoginActivity.class));
          finish();
//            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
//            startActivity(loginIntent);

        });


    }

    @Override
    protected void onStart() {
        super.onStart();
    currentUser = firebaseAuth.getCurrentUser();
    firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}