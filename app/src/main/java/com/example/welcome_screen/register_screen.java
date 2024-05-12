package com.example.welcome_screen;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
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

public class register_screen extends Activity{
    private FirebaseAuth mAuth;

    private MyFirebaseMessagingService test;
    private FirebaseFirestore db;
    private static final String TAG = "EmailPassword";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();


    }



    public void goToHomeScreen(View view){
        EditText emailET = (EditText) findViewById(R.id.EmailAddressInput);
        EditText passwordET= (EditText) findViewById(R.id.PasswordInput);
        EditText confirmPasswordET = (EditText) findViewById(R.id.confirmPasswordText);
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String confirmPassword = confirmPasswordET.getText().toString();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            if (user != null) {
                                String userId = user.getUid();
                                // Get FCM token
                                FirebaseMessaging.getInstance().getToken()
                                        .addOnCompleteListener(new OnCompleteListener<String>() {
                                            @Override
                                            public void onComplete(@NonNull Task<String> task) {
                                                if (task.isSuccessful() && task.getResult() != null) {
                                                    String fcmToken = task.getResult();
                                                    createFirestoreCollection(userId, fcmToken);
                                                } else {
                                                    Log.w(TAG, "Fetching FCM registration token failed", task.getException());
                                                }
                                            }
                                        });
                            }
                            Intent sendToHomeScreen = new Intent(register_screen.this, FragmentMain.class);
                            startActivity(sendToHomeScreen);

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(register_screen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END create_user_with_email]

        mAuth.createUserWithEmailAndPassword(email,password);




    }

    private void createFirestoreCollection(String userId, String token) {
        // Accessing the Firestore "users" collection
        CollectionReference usersCollectionRef = db.collection("users");

        // Create a document with user ID as the document name
        DocumentReference userDocRef = usersCollectionRef.document(userId);

        // Define data to be stored in the document (you can add more fields as needed)
        Map<String, Object> userData = new HashMap<>();
        userData.put("email", mAuth.getCurrentUser().getEmail());
        userData.put("token", token);
        userData.put("admin", false); // Example additional field

        // Set the data in the document with the provided options
        userDocRef.set(userData, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User document added with ID: " + userId);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }


}
