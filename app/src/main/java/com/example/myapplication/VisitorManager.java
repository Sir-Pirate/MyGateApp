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
 * VisitorManager.java
 *
 * Central Firebase backend for ALL visitor operations.
 * Used by: VisitorApproveActivity, VisitorArrivalActivity, VisitorAuthActivity
 *
 * Firestore structure:
 *   visitors/
 *     {docId}/
 *       name, phone, note, status, residentId,
 *       residentName, approvedAt, arrivedAt
 */
public class VisitorManager {

    private static final FirebaseFirestore db   = FirebaseFirestore.getInstance();
    private static final FirebaseAuth      auth = FirebaseAuth.getInstance();
    private static final String COLLECTION = "visitors";

    // ── Callback Interfaces ────────────────────────────────────────────────────

    public interface OnSuccessCallback       { void onSuccess(); }
    public interface OnFailureCallback       { void onFailure(String errorMsg); }
    public interface OnVisitorFound          { void onFound(VisitorModel visitor); }
    public interface OnVisitorListLoaded     { void onLoaded(List<VisitorModel> visitors); }

    // ── 1. Approve a Visitor (Resident) ───────────────────────────────────────
    /**
     * Creates a new visitor document in Firestore with status = "approved".
     * Called from VisitorApproveActivity.
     */
    public static void approveVisitor(
            String visitorName,
            String visitorPhone,
            String note,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { onFailure.onFailure("Not logged in"); return; }

        String residentId = user.getUid();
        String residentName = user.getEmail() != null
                ? user.getEmail().split("@")[0] : "Resident";

        Map<String, Object> data = new HashMap<>();
        data.put("name",         visitorName);
        data.put("phone",        visitorPhone);
        data.put("note",         note);
        data.put("status",       "approved");
        data.put("residentId",   residentId);
        data.put("residentName", residentName);
        data.put("approvedAt",   System.currentTimeMillis());
        data.put("arrivedAt",    0L);

        db.collection(COLLECTION)
            .add(data)
            .addOnSuccessListener(docRef -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 2. Get Visitor by Phone (Guard) ────────────────────────────────────────
    /**
     * Looks up a visitor by phone number.
     * Returns the first approved visitor with that phone.
     * Called from VisitorArrivalActivity.
     */
    public static void getVisitorByPhone(
            String phone,
            OnVisitorFound onFound,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .whereEqualTo("phone", phone)
            .whereEqualTo("status", "approved")
            .limit(1)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                if (querySnapshot.isEmpty()) {
                    onFailure.onFailure("No approved visitor found with this phone number");
                    return;
                }
                QueryDocumentSnapshot doc =
                        (QueryDocumentSnapshot) querySnapshot.getDocuments().get(0);
                VisitorModel visitor = doc.toObject(VisitorModel.class);
                visitor.setId(doc.getId());
                onFound.onFound(visitor);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 3. Mark Visitor as Arrived (Guard) ────────────────────────────────────
    /**
     * Updates visitor status to "arrived" and records the arrival timestamp.
     * Called from VisitorArrivalActivity.
     */
    public static void markVisitorArrived(
            String visitorId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        Map<String, Object> updates = new HashMap<>();
        updates.put("status",    "arrived");
        updates.put("arrivedAt", System.currentTimeMillis());

        db.collection(COLLECTION)
            .document(visitorId)
            .update(updates)
            .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 4. Get All Visitors (for VisitorAuthActivity list) ────────────────────
    /**
     * Fetches all visitors ordered by approvedAt descending.
     * Optional status filter: pass null to get all, or "approved"/"arrived".
     */
    public static void getAllVisitors(
            String statusFilter,
            OnVisitorListLoaded onLoaded,
            OnFailureCallback onFailure) {

        Query query = db.collection(COLLECTION)
                .orderBy("approvedAt", Query.Direction.DESCENDING);

        if (statusFilter != null) {
            query = query.whereEqualTo("status", statusFilter);
        }

        query.get()
            .addOnSuccessListener(querySnapshot -> {
                List<VisitorModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    VisitorModel visitor = doc.toObject(VisitorModel.class);
                    visitor.setId(doc.getId());
                    list.add(visitor);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 5. Get Visitors for Current Resident Only ─────────────────────────────
    /**
     * Fetches only the visitors approved by the currently logged-in resident.
     */
    public static void getMyVisitors(
            OnVisitorListLoaded onLoaded,
            OnFailureCallback onFailure) {

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) { onFailure.onFailure("Not logged in"); return; }

        db.collection(COLLECTION)
            .whereEqualTo("residentId", user.getUid())
            .orderBy("approvedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener(querySnapshot -> {
                List<VisitorModel> list = new ArrayList<>();
                for (QueryDocumentSnapshot doc : querySnapshot) {
                    VisitorModel visitor = doc.toObject(VisitorModel.class);
                    visitor.setId(doc.getId());
                    list.add(visitor);
                }
                onLoaded.onLoaded(list);
            })
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }

    // ── 6. Delete / Revoke a Visitor ──────────────────────────────────────────
    /**
     * Deletes a visitor approval. Called when resident wants to cancel.
     */
    public static void revokeVisitor(
            String visitorId,
            OnSuccessCallback onSuccess,
            OnFailureCallback onFailure) {

        db.collection(COLLECTION)
            .document(visitorId)
            .delete()
            .addOnSuccessListener(aVoid -> onSuccess.onSuccess())
            .addOnFailureListener(e -> onFailure.onFailure(e.getMessage()));
    }
}
