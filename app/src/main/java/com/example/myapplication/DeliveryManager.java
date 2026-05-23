package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeliveryManager.java
 */

public class DeliveryManager {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    // CHANGED HERE
    private static final String COLLECTION =
            "lockers";

    // ─────────────────────────────────────────
    // CALLBACKS
    // ─────────────────────────────────────────

    public interface OnSuccessCallback {
        void onSuccess();
    }

    public interface OnFailureCallback {
        void onFailure(String errorMsg);
    }

    public interface OnDeliveryListLoaded {
        void onLoaded(List<DeliveryModel> deliveries);
    }

    // ─────────────────────────────────────────
    // 1. NORMAL DELIVERY
    // ─────────────────────────────────────────

    public static void logDelivery(

            String courierName,

            String courierPhone,

            String flatNumber,

            OnSuccessCallback onSuccess,

            OnFailureCallback onFailure
    ) {

        FirebaseUser guard =
                auth.getCurrentUser();

        if (guard == null) {

            onFailure.onFailure("Not logged in");

            return;
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put("courierName", courierName);

        data.put("courierPhone", courierPhone);

        data.put("flatNumber", flatNumber);

        data.put("residentId", "");

        data.put("residentEmail", "");

        data.put("status", "pending");

        data.put(
                "storedAt",
                System.currentTimeMillis()
        );

        data.put("pickedUpAt", 0L);

        data.put("guardId", guard.getUid());

        data.put("storedInLocker", false);

        data.put("lockerId", "");

        data.put("lockerOtp", "");

        data.put("lockerStoredAt", 0L);

        data.put("lockerExpiresAt", 0L);

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
    }

    // ─────────────────────────────────────────
    // 2. STORE IN LOCKER
    // ─────────────────────────────────────────

    public static void storeDeliveryInLocker(

            String courierName,

            String courierPhone,

            String flatNumber,

            String residentId,

            String residentEmail,

            String lockerId,

            String lockerOtp,

            long lockerExpiresAt,

            OnSuccessCallback onSuccess,

            OnFailureCallback onFailure
    ) {

        FirebaseUser guard =
                auth.getCurrentUser();

        if (guard == null) {

            onFailure.onFailure("Not logged in");

            return;
        }

        Map<String, Object> data =
                new HashMap<>();

        data.put("courierName", courierName);

        data.put("courierPhone", courierPhone);

        data.put("flatNumber", flatNumber);

        data.put("residentId", residentId);

        data.put("residentEmail", residentEmail);

        data.put("status", "locker");

        data.put(
                "storedAt",
                System.currentTimeMillis()
        );

        data.put("pickedUpAt", 0L);

        data.put("guardId", guard.getUid());

        data.put("storedInLocker", true);

        data.put("lockerId", lockerId);

        data.put("lockerOtp", lockerOtp);

        data.put(
                "lockerStoredAt",
                System.currentTimeMillis()
        );

        data.put(
                "lockerExpiresAt",
                lockerExpiresAt
        );

        db.collection(COLLECTION)

                .add(data)

                .addOnSuccessListener(
                        doc -> onSuccess.onSuccess()
                )

                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ─────────────────────────────────────────
    // 3. GET PENDING
    // ─────────────────────────────────────────

    public static void getPendingDeliveries(

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        db.collection(COLLECTION)

                .orderBy(
                        "storedAt",
                        Query.Direction.DESCENDING
                )

                .get()

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        DeliveryModel d =
                                doc.toObject(
                                        DeliveryModel.class
                                );

                        d.setId(doc.getId());

                        list.add(d);
                    }

                    onLoaded.onLoaded(list);
                })

                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ─────────────────────────────────────────
    // 4. GET ALL
    // ─────────────────────────────────────────

    public static void getAllDeliveries(

            String statusFilter,

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        Query query =
                db.collection(COLLECTION)
                        .orderBy(
                                "storedAt",
                                Query.Direction.DESCENDING
                        );

        query.get()

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        DeliveryModel d =
                                doc.toObject(
                                        DeliveryModel.class
                                );

                        d.setId(doc.getId());

                        list.add(d);
                    }

                    onLoaded.onLoaded(list);
                })

                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ─────────────────────────────────────────
    // 5. GET MY DELIVERIES
    // ─────────────────────────────────────────

    public static void getMyDeliveries(

            String flatNumber,

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        db.collection(COLLECTION)

                .whereEqualTo(
                        "flatNumber",
                        flatNumber
                )

                .get()

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc : snapshot) {

                        DeliveryModel d =
                                doc.toObject(
                                        DeliveryModel.class
                                );

                        d.setId(doc.getId());

                        list.add(d);
                    }

                    onLoaded.onLoaded(list);
                })

                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ─────────────────────────────────────────
    // 6. RESIDENT LOCKERS
    // ─────────────────────────────────────────

    public static void getResidentLockerDeliveries(

            String residentId,

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        db.collection(COLLECTION)

                .whereEqualTo(
                        "residentId",
                        residentId
                )

                .get()

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        DeliveryModel d =
                                doc.toObject(
                                        DeliveryModel.class
                                );

                        if (d != null) {

                            d.setId(doc.getId());

                            long currentTime =
                                    System.currentTimeMillis();

                            if (
                                    d.getLockerExpiresAt() > 0 &&
                                            currentTime >
                                                    d.getLockerExpiresAt() &&
                                            !d.isPickedUp()
                            ) {

                                d.setStatus("expired");
                            }

                            list.add(d);
                        }
                    }

                    onLoaded.onLoaded(list);
                })

                .addOnFailureListener(
                        e -> onFailure.onFailure(
                                e.getMessage()
                        )
                );
    }

    // ─────────────────────────────────────────
    // 7. CONFIRM PICKUP
    // ─────────────────────────────────────────

    public static void confirmPickup(

            String deliveryId,

            OnSuccessCallback onSuccess,

            OnFailureCallback onFailure
    ) {

        Map<String, Object> updates =
                new HashMap<>();

        updates.put("status", "pickedup");

        updates.put(
                "pickedUpAt",
                System.currentTimeMillis()
        );

        db.collection(COLLECTION)

                .document(deliveryId)

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

    // ─────────────────────────────────────────
    // 8. MARK EXPIRED
    // ─────────────────────────────────────────

    public static void markLockerExpired(

            String deliveryId,

            OnSuccessCallback onSuccess,

            OnFailureCallback onFailure
    ) {

        Map<String, Object> updates =
                new HashMap<>();

        updates.put("status", "expired");

        db.collection(COLLECTION)

                .document(deliveryId)

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

    // ─────────────────────────────────────────
    // 9. DELETE
    // ─────────────────────────────────────────

    public static void deleteDelivery(

            String deliveryId,

            OnSuccessCallback onSuccess,

            OnFailureCallback onFailure
    ) {

        db.collection(COLLECTION)

                .document(deliveryId)

                .delete()

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