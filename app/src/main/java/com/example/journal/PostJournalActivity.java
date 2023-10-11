package com.example.journal;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.journal.model.Journal;
import com.example.journal.util.JournalApi;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.Date;
import java.util.Locale;
import java.util.Objects;

public class PostJournalActivity extends AppCompatActivity implements View.OnClickListener {
    private static final int GALLER_CODE = 1;
    private ImageView post_imageView;
    private ImageView post_camera_btn;
    private TextView current_user_textview;
    private TextView currnt_date_textview;
    private EditText post_description_et;
    private EditText post_thoughts_et;
    private ProgressBar post_progressbar;
    private Button post_save_btn;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser user;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference storageReference;

    private CollectionReference collectionReference = db.collection("Journal");

   private String currentUserId;
    private String currentUserName;
    private Uri imageUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
   \     super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_journal);

        //this code is to remove the shadow under the action bar
        Objects.requireNonNull(getSupportActionBar()).setElevation(0);

        firebaseAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
r

        post_imageView = findViewById(R.id.post_imageView);
        post_camera_btn = findViewById(R.id.post_camera_btn);
        current_user_textview = findViewById(R.id.post_username_textview);
        currnt_date_textview = findViewById(R.id.post_date_textview);
        post_description_et = findViewById(R.id.post_description_et);
        post_thoughts_et = findViewById(R.id.post_thoughts_et);
        post_progressbar = findViewById(R.id.post_progressbar);
        post_save_btn = findViewById(R.id.post_save_btn);

        post_save_btn.setOnClickListener(this);
        post_camera_btn.setOnClickListener(this);

        //checking if the journal api != null so as to get user name from firebase and show it
        if (JournalApi.getInstance() != null){

            currentUserId = JournalApi.getInstance().getUserId();
            currentUserName = JournalApi.getInstance().getUserName();

            current_user_textview.setText(currentUserName);
        }

        authStateListener = new FirebaseAuth.AuthStateListener()  {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null){

                }else{

                }
            }
        };



    }//oncreate...................


    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.post_camera_btn:
                animateMyCameraBtn();
                Intent gallerIntent = new Intent(Intent.ACTION_GET_CONTENT);
                gallerIntent.setType("image/*");
                startActivityForResult(gallerIntent, GALLER_CODE);

                break;

            case R.id.post_save_btn:
                saveJournal();

                break;

        }
    }

    private void saveJournal() {
        String title = post_description_et.getText().toString().trim();
        String thoughts = post_thoughts_et.getText().toString().trim();


        if (!TextUtils.isEmpty(title) && !TextUtils.isEmpty(thoughts) && imageUri != null){

            post_progressbar.setVisibility(View.VISIBLE);

            final StorageReference filePath = storageReference.child("Journal_images").child("myimage" + Timestamp.now().getSeconds());
            filePath.putFile(imageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                    filePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            String imageUrl = uri.toString();

                            //create a journal object model
                            Journal journal = new Journal();
                            journal.setTitle(title);
                            journal.setThoughts(thoughts);
                            journal.setImageUri(imageUrl);
                            journal.setTimeAdded(new Timestamp(new Date()));
                            journal.setUsername(currentUserName);
                            journal.setUserId(currentUserId);

                            // to invoke collection reference
                            collectionReference.add(journal).addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                @Override
                                public void onSuccess(DocumentReference documentReference) {
                                post_progressbar.setVisibility(View.INVISIBLE);
                                    Toast.makeText(PostJournalActivity.this, "image sucessfully uploaded", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(PostJournalActivity.this, JournalListActivity.class));
                                finish();

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    post_progressbar.setVisibility(View.INVISIBLE);
                                }
                            });

                        }
                    });

                }
            }).addOnFailureListener(e -> {
                post_progressbar.setVisibility(View.INVISIBLE);
                Toast.makeText(this, "error occured" + e.getMessage(), Toast.LENGTH_SHORT).show();
            });

        } else {
            post_progressbar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "empty field", Toast.LENGTH_SHORT).show();
        }
    }

    private void animateMyCameraBtn(){
        Animation anim = new AlphaAnimation(0.0f, 1.0f);
        anim.setDuration(500); //You can manage the blinking time with this parameter
        anim.setStartOffset(500);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(Animation.INFINITE);
        post_camera_btn.startAnimation(anim);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLER_CODE && resultCode == RESULT_OK){
            if (data != null){
                imageUri = data.getData();
                post_imageView.setImageURI(imageUri);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        animateMyCameraBtn();
        user = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (firebaseAuth != null){
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }
}