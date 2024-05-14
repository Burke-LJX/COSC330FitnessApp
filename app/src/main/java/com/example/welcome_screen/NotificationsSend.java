package com.example.welcome_screen;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.converter.gson.GsonConverterFactory;

public class NotificationsSend extends AppCompatActivity {

    private ListView userListView;
    private Button sendNotificationButton;
    private List<User> users = new ArrayList<>();
    private Set<User> selectedUsers = new HashSet<>();
    private UserAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications_send);

        userListView = findViewById(R.id.user_list_view);
        sendNotificationButton = findViewById(R.id.send_notifs_button);

        // Retrofit setup
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("http://100.115.92.206:3000")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        NotificationService service = retrofit.create(NotificationService.class);

        // Query Firestore for users with both email and token fields
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereGreaterThan("email", "")
                .orderBy("token")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String email = document.getString("email");
                                String token = document.getString("token");
                                if (email != null && token != null) {
                                    users.add(new User(email, token));
                                }
                            }
                            // Populate the ListView
                            adapter = new UserAdapter(users);
                            userListView.setAdapter(adapter);
                        } else {
                            Toast.makeText(NotificationsSend.this, "Error getting documents: " + task.getException(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

        sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!selectedUsers.isEmpty()) {
                    for (User user : selectedUsers) {
                        NotificationRequest request = new NotificationRequest(user.token, "Move For Wellness", "Make sure to workout today!");
                        service.sendNotification(request).enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                if (response.isSuccessful()) {
                                    Toast.makeText(NotificationsSend.this, "Notification sent to " + user.email, Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(NotificationsSend.this, "Failed to send notification", Toast.LENGTH_SHORT).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Toast.makeText(NotificationsSend.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } else {
                    Toast.makeText(NotificationsSend.this, "No users selected", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void sendNotificationToToken(String token) {
        FirebaseMessaging firebaseMessaging = FirebaseMessaging.getInstance();
        RemoteMessage remoteMessage = new RemoteMessage.Builder(token)
                .setMessageId(Integer.toString((int) System.currentTimeMillis()))
                .addData("title", "Move For Wellness")
                .addData("message", "Make sure to workout today!")

                .build();

        // Simulate a successful send by using a Toast message (since actual FCM success/failure cannot be captured on client-side directly)
        firebaseMessaging.send(remoteMessage);
        Toast.makeText(this, "Notification sent to " + token, Toast.LENGTH_SHORT).show();
    }

    private class UserAdapter extends BaseAdapter {

        private List<User> users;

        public UserAdapter(List<User> users) {
            this.users = users;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        @Override
        public User getItem(int position) {
            return users.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.spinner_item_checkbox, parent, false);
                holder = new ViewHolder();
                holder.textView = convertView.findViewById(R.id.text1);
                holder.checkBox = convertView.findViewById(R.id.checkbox);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            final User user = getItem(position);
            holder.textView.setText(user.email);

            // Set the checkbox state based on the selectedUsers set
            holder.checkBox.setOnCheckedChangeListener(null); // Unbind the listener first to prevent unwanted behaviors
            holder.checkBox.setChecked(selectedUsers.contains(user));

            // Attach a listener to the checkbox
            holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                if (isChecked) {
                    selectedUsers.add(user);
                } else {
                    selectedUsers.remove(user);
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView textView;
            CheckBox checkBox;
        }
    }

    private static class User {
        String email;
        String token;

        User(String email, String token) {
            this.email = email;
            this.token = token;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            User user = (User) obj;
            return email.equals(user.email) && token.equals(user.token);
        }

        @Override
        public int hashCode() {
            return Objects.hash(email, token);
        }
    }
}


interface NotificationService {
    @POST("/send-notification")
    Call<Void> sendNotification(@Body NotificationRequest notificationRequest);
}

class NotificationRequest {
    String token;
    String title;
    String message;

    public NotificationRequest(String token, String title, String message) {
        this.token = token;
        this.title = title;
        this.message = message;
    }
}
