package com.example.welcome_screen;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CalendarView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AdminUserHistory extends Activity {

    private CalendarView calendarView;
    private TextView displayDate;
    private TextView workoutAllUsersTextView;
    private FirebaseFirestore db;
    int totalWorkoutCount = 0;
    long totalDurationInMinutes = 0;
    int totalReps = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_user_history);

        calendarView = findViewById(R.id.calendarView2);
        displayDate = findViewById(R.id.displayDate2);
        workoutAllUsersTextView = findViewById(R.id.workoutAllUsersTextView);
        db = FirebaseFirestore.getInstance();

        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = dateFormat.format(new Date());
        displayDate.setText(currentDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView caview, int year, int month, int dayOfMonth) {
                String selectedDate = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year);
                displayDate.setText(selectedDate);

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                calendar.add(Calendar.DAY_OF_MONTH, Calendar.SUNDAY - dayOfWeek);
                Date startDate = calendar.getTime();
                calendar.add(Calendar.DAY_OF_MONTH, 6);
                Date endDate = calendar.getTime();

                retrieveWorkoutsForWeek(dateFormat.format(startDate), dateFormat.format(endDate));
            }
        });
    }

    private void retrieveWorkoutsForWeek(String startDate, String endDate) {
        CollectionReference usersCollectionRef = db.collection("users");

        // Variables to store total workout count and durations/reps
        totalWorkoutCount = 0;
        totalDurationInMinutes = 0;
        totalReps = 0;

        // Retrieve all users
        usersCollectionRef.get().addOnSuccessListener(queryDocumentSnapshots -> {
                    StringBuilder workoutsText = new StringBuilder();

                    // Iterate over each user
                    for (DocumentSnapshot userDoc : queryDocumentSnapshots.getDocuments()) {
                        // Get the user's email
                        String userEmail = userDoc.getString("email");

                        // Get the workouts collection for the current user
                        CollectionReference workoutsCollectionRef = userDoc.getReference().collection("workouts");

                        // Query workouts for the specified date range
                        workoutsCollectionRef.whereGreaterThanOrEqualTo("date", startDate)
                                .whereLessThanOrEqualTo("date", endDate)
                                .get()
                                .addOnSuccessListener(workoutsQueryDocumentSnapshots -> {
                                    // Iterate over each workout document
                                    for (DocumentSnapshot workoutDoc : workoutsQueryDocumentSnapshots.getDocuments()) {
                                        // Extract workout details
                                        String exerciseType = workoutDoc.getString("exerciseType");
                                        String workoutDate = workoutDoc.getString("date");
                                        String duration = workoutDoc.getString("time");
                                        String reps = workoutDoc.getString("reps");

                                        // Append individual workout details to workoutsText
                                        workoutsText.append("User Email: ").append(userEmail).append("\n");
                                        workoutsText.append("Workout Date: ").append(workoutDate).append("\n");
                                        workoutsText.append("Exercise Type: ").append(exerciseType).append("\n");
                                        if (exerciseType.equals("Aerobic")) {
                                            workoutsText.append("Distance: ").append(workoutDoc.getString("distance")).append('\n');
                                            workoutsText.append("Duration: ").append(duration).append("\n\n");
                                            if (duration != null) {
                                                String[] durationParts = duration.split(":");
                                                int minutes = Integer.parseInt(durationParts[0]);
                                                int seconds = Integer.parseInt(durationParts[1]);
                                                totalDurationInMinutes += minutes + (seconds / 60); // Convert seconds to minutes
                                            }
                                        } else {
                                            workoutsText.append("Reps: ").append(reps).append("\n\n");
                                            if (reps != null) {
                                                totalReps += Integer.parseInt(reps);
                                            }
                                        }
                                        // Increment total workout count
                                        totalWorkoutCount++;
                                    }

                                    // Update the TextView with workouts for the selected week
                                    workoutAllUsersTextView.setText("Workouts for the Week of " + startDate + " - " + endDate + ":\n\n" +
                                            workoutsText.toString());

                                    // Update the TextView with averages
                                    double averageDuration = totalDurationInMinutes / (double) totalWorkoutCount;
                                    double averageReps = totalReps / (double) totalWorkoutCount;
                                    workoutAllUsersTextView.append("Average Duration: " + averageDuration + " minutes\n");
                                    workoutAllUsersTextView.append("Average Reps: " + averageReps);
                                })
                                .addOnFailureListener(e -> {
                                    // Handle failure
                                    workoutAllUsersTextView.setText("Error retrieving workouts: " + e.getMessage());
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    // Handle failure
                    workoutAllUsersTextView.setText("Error retrieving users: " + e.getMessage());
                });
    }

    public void goBackToUserView(View view) {
        Intent sendToFragmentMain = new Intent(this, FragmentMain.class);
        startActivity(sendToFragmentMain);
    }

    public void goToNotifs(View view) {
        Intent sendToFragmentMain = new Intent(this, NotificationsSend.class);
        startActivity(sendToFragmentMain);
    }
}
