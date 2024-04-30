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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class register_screen extends Activity{
    private FirebaseAuth mAuth;
    private static final String TAG = "EmailPassword";
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.register_screen);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        Intent intent = getIntent();


    }

    private void createAccount(String email, String password) {
        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();

                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(register_screen.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }
                });
        // [END create_user_with_email]
    }

    public void goToHomeScreen(View view){
        EditText emailET = (EditText) findViewById(R.id.EmailAddressInput);
        EditText passwordET= (EditText) findViewById(R.id.PasswordInput);
        EditText confirmPasswordET = (EditText) findViewById(R.id.confirmPasswordText);
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        String confirmPassword = confirmPasswordET.getText().toString();
        mAuth.createUserWithEmailAndPassword(email,password);
        Intent sendToHomeScreen = new Intent(this, FragmentMain.class);
        startActivity(sendToHomeScreen);


    }




}
