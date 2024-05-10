package com.example.welcome_screen;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
    private TextView displayDate;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;

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


        // Call changeDate to set up CalendarView and TextView
        changeDate(rootView);

        return rootView;
    }


    public void changeDate(View view){
        calendarView = (CalendarView) view.findViewById(R.id.calendarView);
        displayDate = (TextView) view.findViewById(R.id.displayDate);
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        String currentDate = dateFormat.format(new Date());
        displayDate.setText(currentDate);
        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView caview, int year, int month, int dayOfMonth) {

                // Format the selected date into mm/dd/yyyy format
                String selectedDate = String.format("%02d/%02d/%04d", month + 1, dayOfMonth, year);

                displayDate.setText(selectedDate);

                // Get the current user's ID
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    // Query Firestore for workouts on the selected date
                    DocumentReference userDocRef = db.collection("users").document(userId);
                    CollectionReference workoutsCollectionRef = userDocRef.collection("workouts");

                    // checks the collection for matching date
                    workoutsCollectionRef.whereEqualTo("date", selectedDate)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                StringBuilder workoutText = new StringBuilder();
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    // Here you can access workout data for the selected date
                                    String exerciseType = document.getString("exerciseType");

                                    // Check if the exercise type is aerobic or non-aerobic
                                    if (exerciseType.equals("Aerobic")) {
                                        // Append data for aerobic workouts
                                        workoutText.append("Aerobic Exercise\n");
                                        // gets the duration of exercise and distance from document
                                        String aerobicData = document.getString("distance");
                                        String duration = document.getString("time");
                                        workoutText.append("Distance: ").append(aerobicData).append('\n');
                                        workoutText.append("Duration: ").append(duration).append("\n\n");

                                    } else {
                                        // Append data for non-aerobic workouts
                                        workoutText.append("Non-aerobic Exercise\n");
                                        // gets the reps from document
                                        String reps = document.getString("reps");
                                        workoutText.append("Reps: ").append(reps).append("\n\n");
                                    }

                                }
                                //this means that there is no matching date in collection
                                if (workoutText.length() == 0) {
                                    workoutText.append("No workouts found for the selected date.");
                                }
                                //changes the text that will be displayed on screen
                                TextView workoutTextView = view.findViewById(R.id.workoutTextView);
                                workoutTextView.setText(workoutText.toString());
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors
                                Log.e("HomeFragment", "Error getting documents: " + e.getMessage());
                                TextView workoutTextView = view.findViewById(R.id.workoutTextView);
                                workoutTextView.setText("Error: " + e.getMessage());
                            });
                }
            }



        });
    }
}
