package com.afeka.remindey;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.afeka.remindey.util.RemindeyApi;
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

import org.jetbrains.annotations.NotNull;

/**
 * LoginActivity manages the login screen.
 */

public class LoginActivity extends AppCompatActivity {

    private Button loginButton;
    private Button createButton;
    private AutoCompleteTextView emailAddress;
    private EditText password;
    private ProgressBar progressBar;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection(CreateAccountActivity.USERS_TABLE);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    currentUser = firebaseAuth.getCurrentUser();
                    String currentUserId = currentUser.getUid();

                    collectionReference.whereEqualTo(CreateAccountActivity.USER_ID, currentUserId)
                            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                @Override
                                public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                                    if (error != null) {
                                        return;
                                    }
                                    String name;

                                    if (!value.isEmpty()) {
                                        for (QueryDocumentSnapshot snapshot : value) {
                                            RemindeyApi remindeyApi = RemindeyApi.getInstance();
                                            remindeyApi.setUserId(snapshot.getString(CreateAccountActivity.USER_ID));
                                            remindeyApi.setUsername(snapshot.getString(CreateAccountActivity.USER_NAME));
                                            remindeyApi.setUserEmail(snapshot.getString(CreateAccountActivity.USER_EMAIL));
                                        }
                                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                        finish();
                                    }
                                }
                            });
                } else {

                }
            }
        };

        loginButton = findViewById(R.id.email_sign_in_button);
        createButton = findViewById(R.id.email_sign_up_button);
        emailAddress = findViewById(R.id.email);
        password = findViewById(R.id.password);
        progressBar = findViewById(R.id.login_progressbar);

        firebaseAuth = FirebaseAuth.getInstance();

        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoginActivity.this, CreateAccountActivity.class));
                finish();
            }
        });

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (loginEmailPassword(emailAddress.getText().toString().trim(), password.getText().toString().trim()))
                    finish();
            }
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
        if (firebaseAuth != null) {
            firebaseAuth.removeAuthStateListener(authStateListener);
        }
    }

    private boolean loginEmailPassword(String email, String password) {
        progressBar.setVisibility(View.VISIBLE);
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            currentUser = firebaseAuth.getCurrentUser();
                            if (currentUser == null) {
                                return;
                            }

                            String currentUserId = currentUser.getUid();

                            collectionReference.whereEqualTo(CreateAccountActivity.USER_ID, currentUserId)
                                    .addSnapshotListener(new EventListener<QuerySnapshot>() {
                                        @Override
                                        public void onEvent(@Nullable @org.jetbrains.annotations.Nullable QuerySnapshot value, @Nullable @org.jetbrains.annotations.Nullable FirebaseFirestoreException error) {
                                            if (error != null) {

                                            } else {

                                                return;
                                            }
                                            if (value == null) {
                                                Toast.makeText(LoginActivity.this, "User not found, please check Email and password", Toast.LENGTH_SHORT).show();
                                                return;
                                            }
                                            if (!value.isEmpty()) {
                                                progressBar.setVisibility(View.INVISIBLE);
                                                for (QueryDocumentSnapshot snapshot : value) {
                                                    RemindeyApi remindeyApi = RemindeyApi.getInstance();
                                                    remindeyApi.setUsername(snapshot.getString(CreateAccountActivity.USER_NAME));
                                                    remindeyApi.setUserEmail(snapshot.getString(CreateAccountActivity.USER_EMAIL));
                                                    remindeyApi.setUserId(snapshot.getString(CreateAccountActivity.USER_ID));

                                                    // Now login and go to MainActivity
                                                    startActivity(new Intent(LoginActivity.this, MainActivity.class));
                                                }
                                            }
                                        }
                                    });

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {
                            progressBar.setVisibility(View.INVISIBLE);

                        }
                    });

        } else {
            progressBar.setVisibility(View.INVISIBLE);
            Toast.makeText(this, "Please enter Email and password", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (currentUser != null) {
            return true;
        } else {
            return false;
        }
    }
}