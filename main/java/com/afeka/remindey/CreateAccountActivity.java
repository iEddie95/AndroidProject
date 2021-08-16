package com.afeka.remindey;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.afeka.remindey.util.RemindeyApi;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * CreateAccountActivity creates an account for a new user.
 * The user data is saved in Firestore DB.
 */

public class CreateAccountActivity extends AppCompatActivity {

    public final static String USER_ID = "USER_ID";
    public final static String USER_NAME = "USER_NAME";
    public final static String USER_EMAIL = "USER_EMAIL";
    public final static String USERS_TABLE = "USERS";


    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseUser currentUser;

    //Firestore connection
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CollectionReference collectionReference = db.collection(USERS_TABLE);

    //UI components
    private EditText usernameEditText;
    private EditText emailEditText;
    private EditText passwordEditText;
    private ProgressBar progressBar;
    private Button createAccountButton;

    private String email;
    private String password;
    private String usernameText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        firebaseAuth = FirebaseAuth.getInstance();

        createAccountButton = findViewById(R.id.email_create_account_button);
        progressBar = findViewById(R.id.create_acct_progress);
        usernameEditText = findViewById(R.id.username_account);
        emailEditText = findViewById(R.id.email_account);
        passwordEditText = findViewById(R.id.password_account);

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
                currentUser = firebaseAuth.getCurrentUser();
                if (currentUser != null) {
                    // user is already logged in
                } else {
                    // user not logged in
                }
            }
        };
        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(emailEditText.getText().toString()) && !TextUtils.isEmpty(passwordEditText.getText().toString()) && !TextUtils.isEmpty(usernameEditText.getText().toString())) {
                    email = emailEditText.getText().toString().trim();
                    password = passwordEditText.getText().toString().trim();
                    usernameText = usernameEditText.getText().toString().trim();
                } else {
                    Toast.makeText(CreateAccountActivity.this, "Empty fields error", Toast.LENGTH_SHORT).show();
                }
                createUserEmailAccount(email, password, usernameText);
            }
        });
    }

    private void createUserEmailAccount(String email, String password, String username) {
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(username)) {

            progressBar.setVisibility(View.VISIBLE);

            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                // We create new user and open MainActivity
                                currentUser = firebaseAuth.getCurrentUser();
                                assert currentUser != null;
                                String currentUserId = currentUser.getUid();

                                // Create user map for db
                                Map<String, String> userObj = new HashMap<>();
                                userObj.put(USER_ID, currentUserId);
                                userObj.put(USER_NAME, username);
                                userObj.put(USER_EMAIL, email);

                                // save
                                collectionReference.add(userObj)
                                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                documentReference.get()
                                                        .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                                                                if (task.getResult().exists()) {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                    String name = task.getResult().getString(USER_NAME);

                                                                    RemindeyApi remindeyApi = RemindeyApi.getInstance();
                                                                    remindeyApi.setUserId(currentUserId);
                                                                    remindeyApi.setUsername(name);
                                                                    remindeyApi.setUserEmail(email);

                                                                    Intent intent = new Intent(CreateAccountActivity.this, MainActivity.class);
                                                                    intent.putExtra(USER_NAME, name);
                                                                    intent.putExtra(USER_ID, currentUserId);
                                                                    intent.putExtra(USER_EMAIL, email);
                                                                    startActivity(intent);
                                                                    finish();
                                                                } else {
                                                                    progressBar.setVisibility(View.INVISIBLE);
                                                                }
                                                            }
                                                        });
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull @NotNull Exception e) {

                                            }
                                        });
                            } else {
                                // Something went wrong
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull @NotNull Exception e) {

                        }
                    });

        } else {

        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = firebaseAuth.getCurrentUser();
        firebaseAuth.addAuthStateListener(authStateListener);

    }
}