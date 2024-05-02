package com.example.welcome_screen;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class login extends Activity {
    private FirebaseAuth mAuth;

    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in_screen);
        Intent intent = getIntent();
        mAuth = FirebaseAuth.getInstance();
    }

    public void validateLogin(View view){
        EditText emailET = (EditText) findViewById(R.id.EmailAddressInput);
        EditText passwordET= (EditText) findViewById(R.id.PasswordInput);
        String email = emailET.getText().toString();
        String password = passwordET.getText().toString();
        mAuth.signIn(email,password);

    }

   private void signIn(String email, String password) {
        // [START sign_in_with_email]
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            Intent sendToHomeScreen = new Intent(this, FragmentMain.class);
                            startActivity(sendToHomeScreen);
                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(EmailPasswordActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            
                        }
                    }
                });
        // [END sign_in_with_email]
    }

    public void homeScreen(View view) {
        Intent sendToHomeScreen = new Intent(this, FragmentMain.class);
        startActivity(sendToHomeScreen);
    }
}
