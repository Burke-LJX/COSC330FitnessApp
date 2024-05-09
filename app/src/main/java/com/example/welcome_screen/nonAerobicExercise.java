package com.example.welcome_screen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class nonAerobicExercise extends Activity {

    private EditText editTextTime;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.non_aerobic_exercises);
        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        editTextTime = findViewById(R.id.editTextText);



        Intent intent = getIntent();
    }
    public void saveExerciseData(View view) {
        // Get the exercise type from the previous activity
        String exerciseType = getIntent().getStringExtra("exerciseType");

        // Get the exercise time and distance entered by the user
        String reps = editTextTime.getText().toString();

        // Get the current user's ID
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            // Access the Firestore "users" collection
            DocumentReference userDocRef = db.collection("users").document(userId);
            // Access the subcollection "workouts" under the user's document
            CollectionReference workoutsCollectionRef = userDocRef.collection("workouts");

            // Create a new document in the "workouts" subcollection
            Map<String, Object> exerciseData = new HashMap<>();
            exerciseData.put("exerciseType", exerciseType);
            exerciseData.put("reps", reps);

            // Add the exercise data as a document in the "workouts" subcollection
            workoutsCollectionRef.add(exerciseData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(nonAerobicExercise.this, "Exercise data saved successfully!", Toast.LENGTH_SHORT).show();
                        // Optionally, go back to the previous fragment/activity
                        goToWorkoutFragment();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(nonAerobicExercise.this, "Error saving exercise data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToWorkoutFragment() {
        finish();
    }

}