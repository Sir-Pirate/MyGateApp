package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.List;
import java.util.ArrayList;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

public class StaffManager {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final String STAFF_COLLECTION = "staff";
    private static final String LOG_COLLECTION = "staff_logs";

    // ── Callbacks ─────────────────────────────

    public interface OnSuccessCallback {
        void onSuccess();
    }

    public interface OnFailureCallback {
        void onFailure(String errorMsg);
    }

    public interface OnStaffFound {
        void onFound(Map<String, Object> staff, String docId);
    }

    public interface OnLogsLoaded {
        void onLoaded(List<Map<String, Object>> logs);
    }

    // ── 1. Add Staff ─────────────────────────────

    public static void addStaff(
            String name,
            String phone,
            String role,
            String shiftStart,
            String shiftEnd,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            onFailure.onFailure("Not logged in");
            return;
        }

        String residentId = user.getUid();

        db.collection("users")
                .document(residentId)
                .get()
                .addOnSuccessListener(userDoc -> {

                    String residentName = user.getEmail() != null
                            ? user.getEmail().split("@")[0]
                            : "Resident";

                    String flatNo = userDoc.getString("flatNo");
                    String tower = userDoc.getString("tower");

                    Map<String, Object> data = new HashMap<>();

                    data.put("name", name);
                    data.put("phone", phone);
                    data.put("role", role);
                    data.put("shiftStart", shiftStart);
                    data.put("shiftEnd", shiftEnd);

                    data.put("residentId", residentId);
                    data.put("residentName", residentName);
                    data.put("flatNo", flatNo);
                    data.put("tower", tower);

                    data.put("isActive", true);
                    data.put("isLoggedIn", false);
                    data.put("createdAt", System.currentTimeMillis());


                    String docId = phone + "_" + residentId;

                    db.collection(STAFF_COLLECTION)
                            .document(docId)
                            .get()
                            .addOnSuccessListener(doc -> {

                                if (doc.exists()) {
                                    onFailure.onFailure("Staff already exists");
                                    return;
                                }

                                db.collection(STAFF_COLLECTION)
                                        .document(docId)
                                        .set(data)
                                        .addOnSuccessListener(unused -> onSuccess.onSuccess())
                                        .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));

                            });
                })
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 2. Get Staff ─────────────────────────────

    public static void getStaffByPhone(
            String phone,
            OnStaffFound onFound,
            OnFailureCallback onFailure) {

        db.collection(STAFF_COLLECTION)
                .whereEqualTo("phone", phone)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        onFailure.onFailure("No staff found");
                        return;
                    }

                    QueryDocumentSnapshot doc =
                            (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);

                    onFound.onFound(doc.getData(), doc.getId());
                })
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 3. Mark Login ─────────────────────────────

    public static void markLogin(
            String staffId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        long now = System.currentTimeMillis();

        db.collection(STAFF_COLLECTION)
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {

                    Boolean isLoggedIn = doc.getBoolean("isLoggedIn");

                    if (isLoggedIn != null && isLoggedIn) {
                        onFailure.onFailure("Already logged in");
                        return;
                    }

                    String residentId = doc.getString("residentId");

                    Map<String, Object> log = new HashMap<>();
                    log.put("staffId", staffId);
                    log.put("residentId", residentId);
                    log.put("loginTime", now);
                    log.put("logoutTime", 0L);
                    log.put("isActive", true);
                    log.put("createdAt", now);

                    db.collection(LOG_COLLECTION)
                            .add(log)
                            .addOnSuccessListener(ref -> {

                                db.collection(STAFF_COLLECTION)
                                        .document(staffId)
                                        .update("isLoggedIn", true)
                                        .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
                                        .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));

                            })
                            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));

                })
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 4. Mark Logout ─────────────────────────────

    public static void markLogout(
            String staffId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        long now = System.currentTimeMillis();

        db.collection(LOG_COLLECTION)
                .whereEqualTo("staffId", staffId)
                .whereEqualTo("isActive", true)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        onFailure.onFailure("No active login found");
                        return;
                    }

                    QueryDocumentSnapshot logDoc =
                            (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);

                    String logId = logDoc.getId();
                    Long loginTime = logDoc.getLong("loginTime");

                    if (loginTime == null) {
                        onFailure.onFailure("Invalid login data");
                        return;
                    }

                    long durationMinutes = (now - loginTime) / (1000 * 60);
                    String status = durationMinutes >= 60 ? "present" : "partial";

                    db.collection(LOG_COLLECTION)
                            .document(logId)
                            .update(
                                    "logoutTime", now,
                                    "durationMinutes", durationMinutes,
                                    "status", status,
                                    "isActive", false
                            )
                            .addOnSuccessListener(aVoid -> {

                                db.collection(STAFF_COLLECTION)
                                        .document(staffId)
                                        .update("isLoggedIn", false)
                                        .addOnSuccessListener(v -> onSuccess.onSuccess())
                                        .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));

                            })
                            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));

                })
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }
    // ── 5. Get Staff Logs ─────────────────────────────

    public static void getStaffLogs(
            String staffId,
            OnLogsLoaded onSuccess,
            OnFailureCallback onFailure) {

        db.collection(LOG_COLLECTION)
                .whereEqualTo("staffId", staffId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        onSuccess.onLoaded(new ArrayList<>());
                        return;
                    }

                    List<Map<String, Object>> logs = new ArrayList<>();

                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        logs.add(doc.getData());
                    }

                    onSuccess.onLoaded(logs);
                })
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    public static void removeStaff(
            String staffId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        db.collection("staff")
                .document(staffId)
                .update("isActive", false)
                .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
                .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }
}