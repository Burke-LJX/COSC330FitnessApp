package com.example.welcome_screen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AerobicExercise extends Activity {

    private EditText editTextTime;
    private EditText editTextDistance;

    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aerobic_exercises);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // Get references to EditText fields
        editTextTime = findViewById(R.id.editTextTime);
        editTextDistance = findViewById(R.id.editTextText);

        Intent intent = getIntent();
    }

    public void saveExerciseData(View view) {
        // Get the exercise type from the previous activity
        String exerciseType = getIntent().getStringExtra("exerciseType");

        // Get the exercise time and distance entered by the user
        String time = editTextTime.getText().toString();
        String distance = editTextDistance.getText().toString();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = dateFormat.format(new Date());

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
            exerciseData.put("time", time);
            exerciseData.put("distance", distance);
            exerciseData.put("date", currentDate);

            // Add the exercise data as a document in the "workouts" subcollection
            workoutsCollectionRef.add(exerciseData)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(AerobicExercise.this, "Exercise data saved successfully!", Toast.LENGTH_SHORT).show();
                        // Optionally, go back to the previous fragment/activity
                        goToWorkoutFragment();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(AerobicExercise.this, "Error saving exercise data: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToWorkoutFragment() {
        finish();
    }
}
