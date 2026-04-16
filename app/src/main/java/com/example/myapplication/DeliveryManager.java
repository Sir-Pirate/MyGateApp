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
 * DeliveryManager.java
 *
 * Central Firebase backend for ALL delivery operations.
 * Used by: DeliveryLogActivity (Guard), DeliveryActivity (Resident list + pickup)
 *
 * Firestore structure:
 *   deliveries/
 *     {docId}/
 *       courierName, courierPhone, flatNumber,
 *       residentId, residentEmail, status,
 *       loggedAt, pickedUpAt, guardId
 */
public class DeliveryManager {

    private static final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private static final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private static final String COLLECTION = "deliveries";

    // ── Callback Interfaces ────────────────────────────────────────────────────
    public interface OnSuccessCallback    { void onSuccess(); }
    public interface OnFailureCallback    { void onFailure(String errorMsg); }
    public interface OnDeliveryListLoaded { void onLoaded(List<DeliveryModel> deliveries); }

    // ── 1. Log a New Delivery (Guard) ─────────────────────────────────────────
    /**
     * Guard logs a delivery at the gate.
     * Creates a new Firestore document with status = "pending".
     */
    public static void logDelivery(
            String courierName,
            String courierPhone,
            String flatNumber,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        FirebaseUser guard = auth.getCurrentUser();
        if (guard == null) { onFailure.onFailure("Not logged in"); return; }

        Map<String, Object> data = new HashMap<>();
        data.put("courierName",   courierName);
        data.put("courierPhone",  courierPhone);
        data.put("flatNumber",    flatNumber);
        data.put("residentId",    "");          // can be resolved later via flat lookup
        data.put("residentEmail", "");
        data.put("status",        "pending");
        data.put("loggedAt",      System.currentTimeMillis());
        data.put("pickedUpAt",    0L);
        data.put("guardId",       guard.getUid());

        db.collection(COLLECTION)
            .add(data)
            .addOnSuccessListener(docRef -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 2. Get All Pending Deliveries (Guard dashboard) ───────────────────────
    /**
     * Fetches all deliveries with status = "pending", newest first.
     */
    public static void getPendingDeliveries(
            OnDeliveryListLoaded onLoaded,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .whereEqualTo("status", "pending")
            .orderBy("loggedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<DeliveryModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    DeliveryModel d = doc.toObject(DeliveryModel.class);
                    d.setId(doc.getId());
                    list.add(d);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 3. Get All Deliveries (Guard — all statuses) ──────────────────────────
    public static void getAllDeliveries(
            String statusFilter,
            OnDeliveryListLoaded onLoaded,
            OnFailureCallback onFailure) {

        Query query = db.collection(COLLECTION)
                .orderBy("loggedAt", Query.Direction.DESCENDING);

        if (statusFilter != null) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.get()
            .addOnSuccessListener(snapshot -> {
                List<DeliveryModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    DeliveryModel d = doc.toObject(DeliveryModel.class);
                    d.setId(doc.getId());
                    list.add(d);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 4. Get Deliveries for Current Resident (by flat) ──────────────────────
    /**
     * Resident sees only deliveries for their flat number.
     * Matches by the logged-in user's email stored in the delivery doc.
     */
    public static void getMyDeliveries(
            String flatNumber,
            OnDeliveryListLoaded onLoaded,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .whereEqualTo("flatNumber", flatNumber)
            .orderBy("loggedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<DeliveryModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    DeliveryModel d = doc.toObject(DeliveryModel.class);
                    d.setId(doc.getId());
                    list.add(d);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 5. Confirm Pickup (Resident) ──────────────────────────────────────────
    /**
     * Resident marks their delivery as picked up.
     * Updates status to "pickedup" and records the timestamp.
     */
    public static void confirmPickup(
            String deliveryId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status",     "pickedup");
        updates.put("pickedUpAt", System.currentTimeMillis());

        db.collection(COLLECTION)
            .document(deliveryId)
            .update(updates)
            .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 6. Delete a Delivery Record ───────────────────────────────────────────
    public static void deleteDelivery(
            String deliveryId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .document(deliveryId)
            .delete()
            .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }
}
