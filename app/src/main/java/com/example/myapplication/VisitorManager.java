package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class VisitorManager {

    private static final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static final FirebaseAuth auth = FirebaseAuth.getInstance();

    private static final String COLLECTION = "visitors";

    // ── Callbacks ─────────────────────────────────────────────

    public interface OnSuccessCallback {
        void onSuccess();
    }

    public interface OnFailureCallback {
        void onFailure(String errorMsg);
    }

    public interface OnVisitorFound {
        void onFound(VisitorModel visitor);
    }

    public interface OnVisitorListLoaded {
        void onLoaded(List<VisitorModel> visitors);
    }

    // ── 1. Approve Visitor ───────────────────────────────────

    public static void approveVisitor(
            String visitorName,
            String visitorPhone,
            String note,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            onFailure.onFailure("Not logged in");
            return;
        }

        String residentId = user.getUid();

        String residentName =
                user.getEmail() != null
                        ? user.getEmail().split("@")[0]
                        : "Resident";

        db.collection("users")
                .document(residentId)
                .get()
                .addOnSuccessListener(userDoc -> {

                    String flatNo = userDoc.getString("flatNo");
                    String tower = userDoc.getString("tower");

                    Map<String,Object> data = new HashMap<>();

                    data.put("name", visitorName);
                    data.put("phone", visitorPhone);
                    data.put("note", note);

                    data.put("status", "approved");

                    data.put("residentId", residentId);
                    data.put("residentName", residentName);

                    data.put("flatNo", flatNo);
                    data.put("tower", tower);

                    data.put("approvedAt", System.currentTimeMillis());
                    data.put("arrivedAt", 0L);

                    db.collection(COLLECTION)
                            .add(data)
                            .addOnSuccessListener(
                                    docRef -> onSuccess.onSuccess()
                            )
                            .addOnFailureListener(
                                    e -> onFailure.onFailure(
                                            e.getMessage()
                                    )
                            );
                })
                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ── 2. Lookup Visitor by Phone ───────────────────────────

    public static void getVisitorByPhone(
            String phone,
            OnVisitorFound onFound,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
                .whereEqualTo("phone", phone)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {

                        onFailure.onFailure(
                                "No approved visitor found"
                        );
                        return;
                    }

                    QueryDocumentSnapshot doc =
                            (QueryDocumentSnapshot)
                                    querySnapshot
                                            .getDocuments()
                                            .get(0);

                    VisitorModel visitor =
                            doc.toObject(
                                    VisitorModel.class
                            );

                    visitor.setId(doc.getId());

                    onFound.onFound(visitor);

                })
                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ── 3. Mark Visitor Arrived + Create Alert ───────────────

    public static void markVisitorArrived(
            String visitorId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        // First read visitor
        db.collection(COLLECTION)
                .document(visitorId)
                .get()
                .addOnSuccessListener(visitorDoc -> {

                    if (!visitorDoc.exists()) {
                        onFailure.onFailure(
                                "Visitor not found"
                        );
                        return;
                    }

                    String currentStatus = visitorDoc.getString("status");

// Prevent duplicate arrival alerts STRICTLY
                    if ("arrived".equals(currentStatus)) {
                        onFailure.onFailure("Visitor already marked as arrived");
                        return;
                    }

                    long arrivalTime =
                            System.currentTimeMillis();

                    Map<String,Object> updates =
                            new HashMap<>();

                    updates.put(
                            "status",
                            "arrived"
                    );

                    updates.put(
                            "arrivedAt",
                            arrivalTime
                    );

                    db.collection(COLLECTION)
                            .document(visitorId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {

                                String residentId =
                                        visitorDoc.getString(
                                                "residentId"
                                        );

                                String visitorName =
                                        visitorDoc.getString(
                                                "name"
                                        );

                                String phone =
                                        visitorDoc.getString(
                                                "phone"
                                        );

                                String flatNo =
                                        visitorDoc.getString(
                                                "flatNo"
                                        );

                                String tower =
                                        visitorDoc.getString(
                                                "tower"
                                        );

                                String timeText =
                                        new SimpleDateFormat(
                                                "hh:mm a",
                                                Locale.getDefault()
                                        ).format(
                                                new Date(
                                                        arrivalTime
                                                )
                                        );

                                String alertMessage =
                                        visitorName +
                                                " (" + phone + ")" +
                                                " arrived at " +
                                                timeText +
                                                " to Flat " +
                                                flatNo +
                                                ", Tower " +
                                                tower;

                                Map<String,Object> alert =
                                        new HashMap<>();

                                alert.put(
                                        "residentId",
                                        residentId
                                );

                                alert.put(
                                        "visitorId",
                                        visitorId
                                );

                                alert.put(
                                        "visitorName",
                                        visitorName
                                );

                                alert.put(
                                        "phone",
                                        phone
                                );

                                alert.put(
                                        "flatNo",
                                        flatNo
                                );

                                alert.put(
                                        "tower",
                                        tower
                                );

                                alert.put(
                                        "message",
                                        alertMessage
                                );

                                alert.put(
                                        "createdAt",
                                        arrivalTime
                                );

                                alert.put(
                                        "read",
                                        false
                                );

                                db.collection("alerts")
                                        .add(alert);

                                onSuccess.onSuccess();

                            })
                            .addOnFailureListener(
                                    e -> onFailure.onFailure(
                                            e.getMessage()
                                    )
                            );

                })
                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ── 4. All Visitors ──────────────────────────────────────

    public static void getAllVisitors(
            String statusFilter,
            OnVisitorListLoaded onLoaded,
            OnFailureCallback onFailure) {

        Query query =
                db.collection(COLLECTION)
                        .orderBy(
                                "approvedAt",
                                Query.Direction.DESCENDING
                        );

        if (statusFilter != null) {
            query =
                    query.whereEqualTo(
                            "status",
                            statusFilter
                    );
        }

        query.get()
                .addOnSuccessListener(querySnapshot -> {

                    List<VisitorModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc
                            : querySnapshot) {

                        VisitorModel visitor =
                                doc.toObject(
                                        VisitorModel.class
                                );

                        visitor.setId(
                                doc.getId()
                        );

                        list.add(visitor);
                    }

                    onLoaded.onLoaded(list);

                })
                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ── 5. Current Resident Visitors ─────────────────────────

    public static void getMyVisitors(
            OnVisitorListLoaded onLoaded,
            OnFailureCallback onFailure) {

        FirebaseUser user =
                auth.getCurrentUser();

        if (user == null) {
            onFailure.onFailure(
                    "Not logged in"
            );
            return;
        }

        db.collection(COLLECTION)
                .whereEqualTo(
                        "residentId",
                        user.getUid()
                )
                .orderBy(
                        "approvedAt",
                        Query.Direction.DESCENDING
                )
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    List<VisitorModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc
                            : querySnapshot) {

                        VisitorModel visitor =
                                doc.toObject(
                                        VisitorModel.class
                                );

                        visitor.setId(
                                doc.getId()
                        );

                        list.add(visitor);
                    }

                    onLoaded.onLoaded(list);

                })
                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ── 6. Revoke Visitor ────────────────────────────────────

    public static void revokeVisitor(
            String visitorId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        Map<String,Object> updates =
                new HashMap<>();

        updates.put(
                "status",
                "revoked"
        );

        updates.put(
                "revokedAt",
                System.currentTimeMillis()
        );

        db.collection(COLLECTION)
                .document(visitorId)
                .update(updates)
                .addOnSuccessListener(
                        aVoid -> onSuccess.onSuccess()
                )
                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }
}