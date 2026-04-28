package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

/**
 * MyFirebaseMessagingService.java
 *
 * Handles all incoming FCM push notifications.
 * Shows a heads-up notification with sound when an SOS or alert is received.
 *
 * Place in: java/com/example/myapplication/
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String SOS_CHANNEL_ID    = "sos_channel";
    private static final String ALERT_CHANNEL_ID  = "alert_channel";

    // ── Called when a message is received while app is in foreground ──────────
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title   = "MyGate Alert";
        String message = "You have a new notification";
        String type    = "announcement";

        // Read data payload
        if (remoteMessage.getData().size() > 0) {
            if (remoteMessage.getData().containsKey("title"))
                title = remoteMessage.getData().get("title");
            if (remoteMessage.getData().containsKey("message"))
                message = remoteMessage.getData().get("message");
            if (remoteMessage.getData().containsKey("type"))
                type = remoteMessage.getData().get("type");
        }

        // Also read notification payload as fallback
        if (remoteMessage.getNotification() != null) {
            if (remoteMessage.getNotification().getTitle() != null)
                title = remoteMessage.getNotification().getTitle();
            if (remoteMessage.getNotification().getBody() != null)
                message = remoteMessage.getNotification().getBody();
        }

        showNotification(title, message, type);
    }

    // ── Called when FCM token is refreshed ────────────────────────────────────
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        // Save new token to Firestore so server can target this device
        NotificationHelper.saveTokenToFirestore(token);
    }

    // ── Show Notification ─────────────────────────────────────────────────────
    private void showNotification(String title, String message, String type) {
        NotificationManager manager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        // Create channels (required for Android 8+)
        createChannels(manager);

        // SOS gets a louder, high-priority notification
        boolean isSOS = "sos".equals(type);
        String channelId = isSOS ? SOS_CHANNEL_ID : ALERT_CHANNEL_ID;

        // Tap notification → open AlertsActivity
        Intent intent = new Intent(this, AlertsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        Uri soundUri = RingtoneManager.getDefaultUri(
                isSOS ? RingtoneManager.TYPE_ALARM : RingtoneManager.TYPE_NOTIFICATION
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setAutoCancel(true)
                .setSound(soundUri)
                .setContentIntent(pendingIntent)
                .setPriority(isSOS
                        ? NotificationCompat.PRIORITY_MAX
                        : NotificationCompat.PRIORITY_HIGH);

        // SOS vibrates strongly
        if (isSOS) {
            builder.setVibrate(new long[]{0, 500, 200, 500, 200, 500});
        }

        manager.notify((int) System.currentTimeMillis(), builder.build());
    }

    // ── Create Notification Channels (Android 8+) ─────────────────────────────
    private void createChannels(NotificationManager manager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            // SOS channel — maximum importance, alarm sound
            NotificationChannel sosChannel = new NotificationChannel(
                    SOS_CHANNEL_ID,
                    "Emergency SOS",
                    NotificationManager.IMPORTANCE_HIGH
            );
            sosChannel.setDescription("Emergency SOS alerts from the community");
            sosChannel.enableVibration(true);
            sosChannel.setVibrationPattern(new long[]{0, 500, 200, 500});
            manager.createNotificationChannel(sosChannel);

            // General alerts channel
            NotificationChannel alertChannel = new NotificationChannel(
                    ALERT_CHANNEL_ID,
                    "Community Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            alertChannel.setDescription("General community announcements and maintenance notices");
            manager.createNotificationChannel(alertChannel);
        }
    }
}
