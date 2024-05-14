package com.example.welcome_screen;
import static android.content.ContentValues.TAG;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class login extends Activity {
    private FirebaseAuth mAuth;

    private FirebaseFirestore db;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_screen);
        Intent intent = getIntent();
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    public void validateLogin(View view){
        EditText emailET = (EditText) findViewById(R.id.EmailAddressInput);
        EditText passwordET= (EditText) findViewById(R.id.PasswordInput);
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                // Get FCM token
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (task.isSuccessful() && task.getResult() != null) {
                                                    String fcmToken = task.getResult();
                                                    String userId = user.getUid();
                                                    checkAndAddTokenToDatabase(userId, fcmToken);
                                                } else {
                                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                                }
                                            }
                                        });
                            }
                            Intent sendToHomeScreen = new Intent(login.this, FragmentMain.class);
                            startActivity(sendToHomeScreen);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(login.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void checkAndAddTokenToDatabase(String userId, String token) {
        // Check if token already exists for the user
        db.collection("users").document(userId).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // User document exists, check if token is already added

                                    // Token does not exist, add it
                                    addTokenToDatabase(userId, token);

                            } else {
                                // User document does not exist, create it and add token
                                addTokenToDatabase(userId, token);
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
    }

    private void addTokenToDatabase(String userId, String token) {
        // Accessing the Firestore "users" collection
        CollectionReference usersCollectionRef = db.collection("users");

        // Create a document with user ID as the document name
        DocumentReference userDocRef = usersCollectionRef.document(userId);

        // Define data to be stored in the document
        Map<String, Object> userData = new HashMap<>();
        userData.put("token", token);

        // Set the data in the document with the provided options
        userDocRef.set(userData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User token added to database");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding token to database", e);
                    }
                });
    }



    public void homeScreen(View view) {
        Intent sendToHomeScreen = new Intent(this, FragmentMain.class);
        startActivity(sendToHomeScreen);
    }
}