package com.example.welcome_screen;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "MessageReceived";
    private FirebaseFirestore db;

    private FirebaseAuth mAuth;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "notify",
                    "Move4Wellness",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        Log.d(TAG, "Message Received");
        if (remoteMessage.getNotification() != null) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
            String currentDate = dateFormat.format(new Date());

            // Get the current user's ID
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Access the Firestore "users" collection
                DocumentReference userDocRef = db.collection("users").document(userId);

                // Access the subcollection "workouts" under the user's document
                CollectionReference workoutsCollectionRef = userDocRef.collection("notifications");

                // Create a new document in the "workouts" subcollection
                Map<String, Object> notifData = new HashMap<>();
                notifData.put("exerciseType", "Reminder to Excersise!");
                notifData.put("date", currentDate);
                workoutsCollectionRef.add(notifData);
            }
                getFirebaseMessage(remoteMessage.getNotification().getTitle(), remoteMessage.getNotification().getBody());
        } else {
            Log.d(TAG, "No notification data in the message");
        }
    }

    private void getFirebaseMessage(String title, String body) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "notify")
                .setSmallIcon(R.drawable.notification_bell)
                .setContentTitle(title)
                .setContentText(body)
               
                .setAutoCancel(true);
        Log.d(TAG, "Notification Created");
        NotificationManagerCompat manager = NotificationManagerCompat.from(this);
        manager.notify(0, builder.build());
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);

        mAuth = FirebaseAuth.getInstance(); // Initialize mAuth

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String userId = user.getUid();
            // Update Firestore document with user's token
            DocumentReference userDocRef = db.collection("users").document(userId);
            userDocRef
                    .update("token", token)
                    .addOnSuccessListener(aVoid -> {
                        // Token updated successfully
                        // You can add logging or additional actions here
                    })
                    .addOnFailureListener(e -> {
                        // Handle errors
                        // You can add logging or additional actions here
                    });
        } else {
            Log.d(TAG, "User is null");
        }
    }
}
