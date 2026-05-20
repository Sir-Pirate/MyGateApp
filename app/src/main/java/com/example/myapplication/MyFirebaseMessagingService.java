package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService
        extends FirebaseMessagingService {

    private static final String CHANNEL_ID =
            "smart_security_alerts";

    // Called when token refreshes
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);

        Log.d("FCM_TOKEN", token);

        // Optional:
        // Save updated token to Firestore here later
    }

    // Called when notification received
    @Override
    public void onMessageReceived(
            RemoteMessage remoteMessage
    ) {

        super.onMessageReceived(remoteMessage);

        String title = "Alert";
        String body = "New Notification";

        // Notification Payload
        if (remoteMessage.getNotification() != null) {

            if (remoteMessage.getNotification().getTitle() != null) {
                title =
                        remoteMessage.getNotification().getTitle();
            }

            if (remoteMessage.getNotification().getBody() != null) {
                body =
                        remoteMessage.getNotification().getBody();
            }
        }

        showNotification(title, body);
    }

    // Show Local Notification
    private void showNotification(
            String title,
            String body
    ) {

        NotificationManager manager =
                (NotificationManager)
                        getSystemService(
                                NOTIFICATION_SERVICE
                        );

        // Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Smart Security Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            channel.setDescription(
                    "Notifications for visitors, deliveries and emergencies"
            );

            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setContentTitle(title)
                        .setContentText(body)
                        .setPriority(
                                NotificationCompat.PRIORITY_HIGH
                        )
                        .setAutoCancel(true);

        manager.notify(
                (int) System.currentTimeMillis(),
                builder.build()
        );
    }
}