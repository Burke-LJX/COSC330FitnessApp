package com.example.welcome_screen;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link WorkoutFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class WorkoutFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private RadioGroup workouts;

    public WorkoutFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static WorkoutFragment newInstance(String param1, String param2) {
        WorkoutFragment fragment = new WorkoutFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }
    public void aerobicExercise() {
        Intent sendToAerobic = new Intent(requireContext(), AerobicExercise.class);
        sendToAerobic.putExtra("exerciseType", "Aerobic");
        startActivity(sendToAerobic);
    }

    public void nonAerobicExercise() {
        Intent sendToNonAerobic = new Intent(requireContext(), nonAerobicExercise.class);
        sendToNonAerobic.putExtra("exerciseType", "Non-Aerobic");
        startActivity(sendToNonAerobic);
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
        return inflater.inflate(R.layout.fragment_workout, container, false);
    }

    public void exerciseSelected(View view) {
        workouts = (RadioGroup) getView().findViewById(R.id.radioGroup);
        Integer radioId = workouts.getCheckedRadioButtonId();
        RadioButton exercise = (RadioButton) getView().findViewById(radioId);
        String buttonText = exercise.getText().toString();
        String nonAerobic = "Weight-Lifting";
        if(buttonText.equals(nonAerobic)){
            nonAerobicExercise();
        }
        else
            aerobicExercise();
    }
}