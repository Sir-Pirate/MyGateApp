package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService
        extends FirebaseMessagingService {

    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
    }

    @Override
    public void onMessageReceived(
            RemoteMessage remoteMessage
    ) {

        String title =
                remoteMessage.getNotification().getTitle();

        String body =
                remoteMessage.getNotification().getBody();

        showNotification(title, body);
    }

    private void showNotification(
            String title,
            String body) {

        String CHANNEL_ID = "visitor_alerts";

        NotificationManager manager =
                getSystemService(
                        NotificationManager.class
                );

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel =
                    new NotificationChannel(
                            CHANNEL_ID,
                            "Visitor Alerts",
                            NotificationManager.IMPORTANCE_HIGH
                    );

            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        this,
                        CHANNEL_ID
                )
                        .setContentTitle(title)
                        .setContentText(body)
                        .setSmallIcon(
                                android.R.drawable.ic_dialog_info
                        )
                        .setAutoCancel(true);

        manager.notify(1, builder.build());
    }
}