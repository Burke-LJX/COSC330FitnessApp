package com.example.welcome_screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private CalendarView calendarView;
    int totalReps = 0;
    long totalDurationInMinutes = 0;
    int workoutCount = 0;
    private TextView displayDate;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

    private boolean isAdmin = false;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        checkAdminStatus();
        // Call changeDate to set up CalendarView and TextView
        changeDate(rootView);

        return rootView;
    }

    private void checkAdminStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String userId = currentUser.getUid();
            DocumentReference userDocRef = db.collection("users").document(userId);
            userDocRef.get().addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    isAdmin = documentSnapshot.getBoolean("admin");
                    // Update UI based on admin status
                    updateUI();
                }
            }).addOnFailureListener(e -> Log.e("HomeFragment", "Error checking admin status: " + e.getMessage()));
        }
    }

    private void updateUI() {
        Button adminButton = requireView().findViewById(R.id.adminButton);
        if (isAdmin) {
            adminButton.setVisibility(View.VISIBLE);
        } else {
            adminButton.setVisibility(View.GONE);
        }
    }


    public void changeDate(View view) {
        calendarView = view.findViewById(R.id.calendarView);
        displayDate = view.findViewById(R.id.displayDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = dateFormat.format(new Date());
        displayDate.setText(currentDate);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView caview, int year, int month, int dayOfMonth) {
                totalReps = 0;
                totalDurationInMinutes = 0;
                workoutCount = 0;

                String selectedDate = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year);
                displayDate.setText(selectedDate);

                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    DocumentReference userDocRef = db.collection("users").document(userId);
                    CollectionReference workoutsCollectionRef = userDocRef.collection("workouts");

                    // Define the start and end dates of the selected week
                    Calendar calendar = Calendar.getInstance();
                    calendar.set(Calendar.YEAR, year);
                    calendar.set(Calendar.MONTH, month);
                    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                    int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
                    calendar.add(Calendar.DAY_OF_MONTH, Calendar.SUNDAY - dayOfWeek);
                    Date startDate = calendar.getTime();
                    calendar.add(Calendar.DAY_OF_MONTH, 6);
                    Date endDate = calendar.getTime();

                    workoutsCollectionRef.whereGreaterThanOrEqualTo("date", dateFormat.format(startDate))
                            .whereLessThanOrEqualTo("date", dateFormat.format(endDate))
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                StringBuilder workoutText = new StringBuilder();
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    String exerciseType = document.getString("exerciseType");
                                    String workoutDate = document.getString("date"); // Get workout date
                                    workoutText.append("Workout Date: ").append(workoutDate).append("\n"); // Append workout date
                                    workoutText.append("Workout Type: ").append(exerciseType).append("\n");
                                    if (exerciseType.equals("Aerobic")) {
                                        String aerobicData = document.getString("distance");
                                        String duration = document.getString("time");
                                        workoutText.append("Distance: ").append(aerobicData).append('\n');
                                        workoutText.append("Duration: ").append(duration).append("\n\n");
                                        if (duration != null) {
                                            String[] durationParts = duration.split(":");
                                            int minutes = Integer.parseInt(durationParts[0]);
                                            int seconds = Integer.parseInt(durationParts[1]);
                                            totalDurationInMinutes += minutes + (seconds / 60.0); // Convert seconds to minutes
                                        }
                                    } else {
                                        String reps = document.getString("reps");
                                        workoutText.append("Reps: ").append(reps).append("\n\n");
                                        totalReps += Integer.parseInt(reps);
                                    }
                                    workoutCount++;
                                }

                                double averageDuration = workoutCount > 0 ? totalDurationInMinutes / workoutCount : 0;
                                double averageReps = workoutCount > 0 ? totalReps / (double) workoutCount : 0;

                                TextView workoutTextView = view.findViewById(R.id.workoutTextView);
                                workoutTextView.setText("Workouts for the Week of " + dateFormat.format(startDate) + " - " + dateFormat.format(endDate) + ":\n\n" +
                                        workoutText.toString() +
                                        "\nAverage Duration for the Week: " + averageDuration + " minutes\n" +
                                        "Average Reps for the Week: " + averageReps);
                            })
                            .addOnFailureListener(e -> {
                                Log.e("HomeFragment", "Error getting documents: " + e.getMessage());
                                TextView workoutTextView = view.findViewById(R.id.workoutTextView);
                                workoutTextView.setText("Error: " + e.getMessage());
                            });
                }
            }

        });
    }










}
