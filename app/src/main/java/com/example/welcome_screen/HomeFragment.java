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

import java.util.Calendar;


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

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String selectedDate = month + "/" + dayOfMonth + "/" + year;
                displayDate.setText(selectedDate);

                // Get the current user's ID
                FirebaseUser currentUser = mAuth.getCurrentUser();
                if (currentUser != null) {
                    String userId = currentUser.getUid();
                    // Query Firestore for workouts on the selected date
                    DocumentReference userDocRef = db.collection("users").document(userId);
                    CollectionReference workoutsCollectionRef = userDocRef.collection("workouts");

                    // Assuming you have a field 'date' in your workout documents
                    workoutsCollectionRef.whereEqualTo("date", selectedDate)
                            .get()
                            .addOnSuccessListener(queryDocumentSnapshots -> {
                                for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                    // Here you can access workout data for the selected date
                                    String exerciseType = document.getString("exerciseType");
                                    String reps = document.getString("reps");
                                    TextView workoutTextView = view.findViewById(R.id.workoutTextView);
                                    String workoutText = "Exercise Type: " + exerciseType + "\nReps: " + reps;
                                    workoutTextView.setText(workoutText);
                                }
                            })
                            .addOnFailureListener(e -> {
                                // Handle errors
                                Log.e("HomeFragment", "Error getting documents: " + e.getMessage());
                            });
                }
            }
        });
    }
}
