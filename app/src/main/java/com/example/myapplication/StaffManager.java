package com.example.myapplication;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * StaffManager.java
 *
 * Central Firebase backend for ALL staff operations.
 * Used by: StaffCheckInActivity, StaffActivity (view list)
 *
 * Firestore structure:
 *   staff/
 *     {phone}/           ← phone is used as the document ID for easy lookup
 *       name, phone, staffType, flatNumber,
 *       status, entryTime, exitTime, registeredAt
 *
 *   staffLog/            ← separate collection for daily entry/exit history
 *     {auto-id}/
 *       staffPhone, staffName, staffType, action, timestamp
 */
public class StaffManager {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final String COLLECTION     = "staff";
    private static final String LOG_COLLECTION = "staffLog";

    // ── Callback Interfaces ────────────────────────────────────────────────────
    public interface OnSuccessCallback  { void onSuccess(); }
    public interface OnFailureCallback  { void onFailure(String errorMsg); }
    public interface OnStaffFound       { void onFound(StaffModel staff); }
    public interface OnStaffNotFound    { void onNotFound(); }
    public interface OnStaffListLoaded  { void onLoaded(List<StaffModel> staffList); }

    // ── 1. Register Staff (first time check-in) ───────────────────────────────
    /**
     * Registers a new staff member and marks them as "inside".
     * Uses phone as document ID so we can look them up instantly.
     */
    public static void registerAndCheckIn(
            String name,
            String phone,
            String staffType,
            String flatNumber,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        long now = System.currentTimeMillis();

        Map<String, Object> data = new HashMap<>();
        data.put("name",         name);
        data.put("phone",        phone);
        data.put("staffType",    staffType);
        data.put("flatNumber",   flatNumber);
        data.put("status",       "inside");
        data.put("entryTime",    now);
        data.put("exitTime",     0L);
        data.put("registeredAt", now);

        // Use phone as doc ID for instant lookup
        db.collection(COLLECTION)
            .document(phone)
            .set(data)
            .addOnSuccessListener(aVoid -> {
                logAction(phone, name, staffType, "check-in");
                onSuccess.onSuccess();
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 2. Check In (returning staff) ─────────────────────────────────────────
    /**
     * Updates an existing staff member's status to "inside" with new entry time.
     */
    public static void checkIn(
            String phone,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status",    "inside");
        updates.put("entryTime", System.currentTimeMillis());
        updates.put("exitTime",  0L);

        db.collection(COLLECTION)
            .document(phone)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                // Fetch name for log
                db.collection(COLLECTION).document(phone).get()
                    .addOnSuccessListener(doc -> {
                        String name      = doc.getString("name") != null ? doc.getString("name") : "";
                        String staffType = doc.getString("staffType") != null ? doc.getString("staffType") : "";
                        logAction(phone, name, staffType, "check-in");
                        onSuccess.onSuccess();
                    });
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 3. Check Out ──────────────────────────────────────────────────────────
    /**
     * Marks staff as exited and records exit time.
     */
    public static void checkOut(
            String phone,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status",   "exited");
        updates.put("exitTime", System.currentTimeMillis());

        db.collection(COLLECTION)
            .document(phone)
            .update(updates)
            .addOnSuccessListener(aVoid -> {
                db.collection(COLLECTION).document(phone).get()
                    .addOnSuccessListener(doc -> {
                        String name      = doc.getString("name") != null ? doc.getString("name") : "";
                        String staffType = doc.getString("staffType") != null ? doc.getString("staffType") : "";
                        logAction(phone, name, staffType, "check-out");
                        onSuccess.onSuccess();
                    });
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 4. Look Up Staff by Phone ──────────────────────────────────────────────
    /**
     * Looks up a staff member by their phone number.
     * Calls onFound if exists, onNotFound if new staff.
     */
    public static void getStaffByPhone(
            String phone,
            OnStaffFound onFound,
            OnStaffNotFound onNotFound,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .document(phone)
            .get()
            .addOnSuccessListener(doc -> {
                if (doc.exists()) {
                    StaffModel staff = doc.toObject(StaffModel.class);
                    if (staff != null) {
                        staff.setId(doc.getId());
                        onFound.onFound(staff);
                    } else {
                        onNotFound.onNotFound();
                    }
                } else {
                    onNotFound.onNotFound();
                }
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 5. Get All Staff Currently Inside ─────────────────────────────────────
    public static void getStaffInside(
            OnStaffListLoaded onLoaded,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .whereEqualTo("status", "inside")
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<StaffModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    StaffModel s = doc.toObject(StaffModel.class);
                    s.setId(doc.getId());
                    list.add(s);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 6. Get All Staff (all statuses) ───────────────────────────────────────
    public static void getAllStaff(
            OnStaffListLoaded onLoaded,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .orderBy("entryTime", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(snapshot -> {
                List<StaffModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : snapshot) {
                    StaffModel s = doc.toObject(StaffModel.class);
                    s.setId(doc.getId());
                    list.add(s);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 7. Private: Log entry/exit action ─────────────────────────────────────
    private static void logAction(String phone, String name, String staffType, String action) {
        Map<String, Object> log = new HashMap<>();
        log.put("staffPhone", phone);
        log.put("staffName",  name);
        log.put("staffType",  staffType);
        log.put("action",     action);   // "check-in" | "check-out"
        log.put("timestamp",  System.currentTimeMillis());

        db.collection(LOG_COLLECTION).add(log); // fire and forget
    }
}
