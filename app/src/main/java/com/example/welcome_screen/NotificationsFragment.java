package com.example.welcome_screen;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.util.Log;

import androidx.annotation.NonNull;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class NotificationsFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    private ArrayList<String> items;
    private ArrayAdapter<String> itemsAdapter;
    private ListView lvItems;

    private FirebaseUser currentUser;
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;
    private static final String TAG = "NotificationsFragment";

    public NotificationsFragment() {
        // Required empty public constructor
    }

    public static NotificationsFragment newInstance(String param1, String param2) {
        NotificationsFragment fragment = new NotificationsFragment();
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
        View view = inflater.inflate(R.layout.notification_screen, container, false);
        lvItems = view.findViewById(R.id.lvItems);
        items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        currentUser = mAuth.getCurrentUser();
        loadTasksFromFirestore();
        setupListViewListener();
        return view;
    }

    private void setupListViewListener() {
        lvItems.setOnItemLongClickListener(
                new AdapterView.OnItemLongClickListener() {
                    @Override
                    public boolean onItemLongClick(AdapterView<?> adapter, View item, int pos, long id) {
                        items.remove(pos);
                        itemsAdapter.notifyDataSetChanged();
                        deleteTaskFromFirestore(pos);
                        return true;
                    }
                }
        );
    }

    private void loadTasksFromFirestore() {
        // Assuming userID is obtained from some authentication mechanism
        String userID = currentUser.getUid(); // Obtain userID here

        db.collection("users").document(userID).collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String task = "You were reminded to excersise on " + documentSnapshot.getString("date");
                        items.add(task);
                    }
                    itemsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading document", e);
                });
    }

    private void deleteTaskFromFirestore(int pos) {
        // Similar to loadTasksFromFirestore, modify the reference to point to the user's notifications
        String userID = currentUser.getUid();; // Obtain userID here

        db.collection("users").document(userID).collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty() && pos < queryDocumentSnapshots.size()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(pos);
                        String taskId = documentSnapshot.getId();

                        db.collection("users").document(userID).collection("notifications")
                                .document(taskId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Task deleted successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error deleting task", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting documents", e);
                });
    }


}
