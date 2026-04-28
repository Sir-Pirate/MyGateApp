package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

/**
 * NotificationHelper.java
 *
 * Handles:
 *   1. Saving this device's FCM token to Firestore (so server knows where to send)
 *   2. Writing a "notificationRequest" document to Firestore which triggers
 *      a Cloud Function to fan-out the push notification to all devices.
 *
 * Firestore structure:
 *
 *   userTokens/
 *     {userId}/
 *       token     → FCM token string
 *       userId    → Firebase UID
 *       email     → user email
 *       updatedAt → timestamp
 *
 *   notificationRequests/     ← Cloud Function listens here
 *     {auto-id}/
 *       title     → notification title
 *       message   → notification body
 *       type      → "sos" | "announcement" | "maintenance"
 *       sentBy    → userId
 *       topic     → "all" | "guards"
 *       timestamp → millis
 *       sent      → false (Cloud Function sets to true after sending)
 */
public class NotificationHelper {

    private static final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private static final FirebaseAuth      auth = FirebaseAuth.getInstance();

    // ── 1. Save FCM Token to Firestore ────────────────────────────────────────
    /**
     * Called from MyFirebaseMessagingService.onNewToken()
     * and also on app launch to ensure token is always fresh.
     */
    public static void saveTokenToFirestore(String token) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("token",     token);
        data.put("userId",    user.getUid());
        data.put("email",     user.getEmail());
        data.put("updatedAt", System.currentTimeMillis());

        db.collection("userTokens")
            .document(user.getUid())
            .set(data); // fire and forget
    }

    // ── 2. Fetch and Save Token on App Launch ─────────────────────────────────
    /**
     * Call this from homeactivity.onCreate() once user is logged in.
     * Ensures token is saved/refreshed every session.
     */
    public static void refreshAndSaveToken() {
        FirebaseMessaging.getInstance().getToken()
            .addOnSuccessListener(token -> {
                if (token != null) saveTokenToFirestore(token);
            });
    }

    // ── 3. Request SOS Notification (writes to Firestore) ─────────────────────
    /**
     * Writes a notificationRequest doc to Firestore.
     * A Firebase Cloud Function watches this collection and sends
     * FCM push notifications to all registered devices.
     *
     * Topic "all"    → everyone gets it
     * Topic "guards" → only guards get it (implement role filter in Cloud Function)
     */
    public static void sendSOSNotification(
            String postedBy,
            String flatNumber,
            Runnable onSuccess,
            Runnable onFailure) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { onFailure.run(); return; }

        String flat = (flatNumber != null && !flatNumber.isEmpty())
                ? " from Flat " + flatNumber : "";

        Map<String, Object> request = new HashMap<>();
        request.put("title",     "🆘 EMERGENCY SOS");
        request.put("message",   "SOS alert triggered by " + postedBy + flat + ". Respond immediately!");
        request.put("type",      "sos");
        request.put("sentBy",    user.getUid());
        request.put("topic",     "all");      // send to everyone
        request.put("timestamp", System.currentTimeMillis());
        request.put("sent",      false);      // Cloud Function sets this to true

        db.collection("notificationRequests")
            .add(request)
            .addOnSuccessListener(ref -> onSuccess.run())
            .addOnFailureListener(e -> onFailure.run());
    }

    // ── 4. Request General Alert Notification ─────────────────────────────────
    public static void sendAlertNotification(
            String title,
            String message,
            String type) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> request = new HashMap<>();
        request.put("title",     title);
        request.put("message",   message);
        request.put("type",      type);
        request.put("sentBy",    user.getUid());
        request.put("topic",     "all");
        request.put("timestamp", System.currentTimeMillis());
        request.put("sent",      false);

        db.collection("notificationRequests")
            .add(request); // fire and forget
    }
}
