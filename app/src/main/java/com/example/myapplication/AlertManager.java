package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * AlertManager.java
 *
 * Central Firebase backend for ALL alert operations.
 * Used by: AlertsActivity (list + post alerts)
 *
 * Firestore structure:
 *   alerts/
 *     {docId}/
 *       title, message, type, postedBy, postedById,
 *       flatNumber, timestamp, isResolved
 */
public class AlertManager {

    private static final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private static final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private static final String COLLECTION = "alerts";

    // ── Callback Interfaces ────────────────────────────────────────────────────
    public interface OnSuccessCallback  { void onSuccess(); }
    public interface OnFailureCallback  { void onFailure(String errorMsg); }
    public interface OnAlertsLoaded     { void onLoaded(List<AlertModel> alerts); }

    // ── 1. Post a New Alert ───────────────────────────────────────────────────
    /**
     * Posts a new alert to Firestore.
     * type: "sos" | "announcement" | "maintenance"
     */
    public static void postAlert(
            String title,
            String message,
            String type,
            String flatNumber,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { onFailure.onFailure("Not logged in"); return; }

        String postedBy = user.getEmail() != null
                ? user.getEmail().split("@")[0] : "Unknown";

        Map<String, Object> data = new HashMap<>();
        data.put("title",      title);
        data.put("message",    message);
        data.put("type",       type);
        data.put("postedBy",   postedBy);
        data.put("postedById", user.getUid());
        data.put("flatNumber", flatNumber);
        data.put("timestamp",  System.currentTimeMillis());
        data.put("isResolved", false);

        db.collection(COLLECTION)
            .add(data)
            .addOnSuccessListener(ref -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 2. Get All Active Alerts (not resolved) ───────────────────────────────
    public static void getActiveAlerts(
            OnAlertsLoaded onLoaded,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .whereEqualTo("isResolved", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<AlertModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    AlertModel a = doc.toObject(AlertModel.class);
                    a.setId(doc.getId());
                    list.add(a);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 3. Get Alerts by Type ─────────────────────────────────────────────────
    public static void getAlertsByType(
            String type,
            OnAlertsLoaded onLoaded,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .whereEqualTo("type", type)
            .whereEqualTo("isResolved", false)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<AlertModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    AlertModel a = doc.toObject(AlertModel.class);
                    a.setId(doc.getId());
                    list.add(a);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 4. Get All Alerts (including resolved) ────────────────────────────────
    public static void getAllAlerts(
            OnAlertsLoaded onLoaded,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<AlertModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    AlertModel a = doc.toObject(AlertModel.class);
                    a.setId(doc.getId());
                    list.add(a);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 5. Resolve / Close an Alert ───────────────────────────────────────────
    public static void resolveAlert(
            String alertId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .document(alertId)
            .update("isResolved", true)
            .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 6. Delete an Alert ────────────────────────────────────────────────────
    public static void deleteAlert(
            String alertId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .document(alertId)
            .delete()
            .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }
}
