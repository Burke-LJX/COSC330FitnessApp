package com.example.welcome_screen;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

public class notificationScreen extends AppCompatActivity {
    ArrayList<String> items;

    private static final String TAG = "Test";
    ArrayAdapter<String> itemsAdapter;
    ListView lvItems;
    FirebaseFirestore db;

    private FirebaseAuth mAuth;

    private FirebaseUser currentUser;
    String userID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notification_screen);
        lvItems = findViewById(R.id.lvItems);
        items = new ArrayList<>();
        itemsAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, items);
        lvItems.setAdapter(itemsAdapter);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        loadTasksFromFirestore();
        setupListViewListener();
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
        currentUser = mAuth.getCurrentUser();
        assert currentUser != null;
        userID = currentUser.getUid(); // Set your user ID here
        db.collection("users")
                .document(userID)
                .collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    for (DocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String timestamp = documentSnapshot.getString("date");
                        if (timestamp != null) {
                            // You can format the timestamp as needed
                            String task = "Notification at " + timestamp;
                            items.add(task);
                        }
                    }
                    itemsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error loading document", e);
                });
    }

    private void deleteTaskFromFirestore(int pos) {
        db.collection("users")
                .document(userID)
                .collection("notifications")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty() && pos < queryDocumentSnapshots.size()) {
                        DocumentSnapshot documentSnapshot = queryDocumentSnapshots.getDocuments().get(pos);
                        String notificationId = documentSnapshot.getId();

                        db.collection("users")
                                .document(userID)
                                .collection("notifications")
                                .document(notificationId)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Notification deleted successfully");
                                })
                                .addOnFailureListener(e -> {
                                    Log.w(TAG, "Error deleting notification", e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w(TAG, "Error getting documents", e);
                });
    }


}
