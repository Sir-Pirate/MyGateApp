package com.example.myapplication;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
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
 * Architecture:
 *
 * deliveries/ -> delivery history
 * lockers/    -> live locker state
 */

public class DeliveryManager {

    private static final FirebaseFirestore db =
            FirebaseFirestore.getInstance();

    private static final FirebaseAuth auth =
            FirebaseAuth.getInstance();

    // =========================================
    // COLLECTIONS
    // =========================================

    private static final String DELIVERY_COLLECTION =
            "deliveries";

    private static final String LOCKER_COLLECTION =
            "lockers";

    // =========================================
    // CALLBACKS
    // =========================================

    public interface OnSuccessCallback {
        void onSuccess();
    }

    public interface OnFailureCallback {
        void onFailure(String errorMsg);
    }

    public interface OnDeliveryListLoaded {
        void onLoaded(List<DeliveryModel> deliveries);
    }

    // =========================================
    // 1. NORMAL DELIVERY
    // =========================================

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

        long currentTime =
                System.currentTimeMillis();

        Map<String, Object> data =
                new HashMap<>();

        data.put("courierName", courierName);

        data.put("courierPhone", courierPhone);

        data.put("flatNumber", flatNumber);

        data.put("residentId", "");

        data.put("residentEmail", "");

        data.put("status", "pending");

        data.put("storedAt", currentTime);

        data.put("pickedUpAt", 0L);

        data.put("guardId", guard.getUid());

        data.put("lockerId", "");

        data.put("lockerOtp", "");

        data.put("lockerExpiresAt", 0L);

        db.collection(DELIVERY_COLLECTION)

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

    // =========================================
    // 2. STORE DELIVERY IN LOCKER
    // =========================================

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

        long currentTime =
                System.currentTimeMillis();

        // =====================================
        // 24 HOUR EXPIRY
        // =====================================

        long expiryTime =
                currentTime +
                        (24L * 60L * 60L * 1000L);

        // =====================================
        // CHECK LOCKER AVAILABILITY
        // =====================================

        db.collection(LOCKER_COLLECTION)

                .document(lockerId)

                .get()

                .addOnSuccessListener(lockerDoc -> {

                    if (lockerDoc.exists()) {

                        String status =
                                lockerDoc.getString(
                                        "status"
                                );

                        if (!"available".equals(status)) {

                            onFailure.onFailure(
                                    "Locker already occupied"
                            );

                            return;
                        }
                    }

                    // =================================
                    // CREATE DELIVERY HISTORY
                    // =================================

                    Map<String, Object> deliveryData =
                            new HashMap<>();

                    deliveryData.put(
                            "courierName",
                            courierName
                    );

                    deliveryData.put(
                            "courierPhone",
                            courierPhone
                    );

                    deliveryData.put(
                            "flatNumber",
                            flatNumber
                    );

                    deliveryData.put(
                            "residentId",
                            residentId
                    );

                    deliveryData.put(
                            "residentEmail",
                            residentEmail
                    );

                    deliveryData.put(
                            "status",
                            "locker"
                    );

                    deliveryData.put(
                            "storedAt",
                            currentTime
                    );

                    deliveryData.put(
                            "pickedUpAt",
                            0L
                    );

                    deliveryData.put(
                            "guardId",
                            guard.getUid()
                    );

                    deliveryData.put(
                            "lockerId",
                            lockerId
                    );

                    deliveryData.put(
                            "lockerOtp",
                            lockerOtp
                    );

                    deliveryData.put(
                            "lockerExpiresAt",
                            expiryTime
                    );

                    db.collection(DELIVERY_COLLECTION)

                            .add(deliveryData)

                            .addOnSuccessListener(
                                    deliveryDoc -> {

                                        // =========================
                                        // UPDATE LOCKER STATE
                                        // =========================

                                        Map<String, Object>
                                                lockerData =
                                                new HashMap<>();

                                        lockerData.put(
                                                "lockerId",
                                                lockerId
                                        );

                                        lockerData.put(
                                                "residentId",
                                                residentId
                                        );

                                        lockerData.put(
                                                "residentEmail",
                                                residentEmail
                                        );

                                        lockerData.put(
                                                "deliveryId",
                                                deliveryDoc.getId()
                                        );

                                        lockerData.put(
                                                "otp",
                                                lockerOtp
                                        );

                                        lockerData.put(
                                                "status",
                                                "active"
                                        );

                                        lockerData.put(
                                                "storedAt",
                                                currentTime
                                        );

                                        lockerData.put(
                                                "expiresAt",
                                                expiryTime
                                        );

                                        db.collection(
                                                        LOCKER_COLLECTION
                                                )

                                                .document(lockerId)

                                                .set(lockerData)

                                                .addOnSuccessListener(
                                                        unused ->
                                                                onSuccess.onSuccess()
                                                )

                                                .addOnFailureListener(
                                                        e ->
                                                                onFailure.onFailure(
                                                                        e.getMessage()
                                                                )
                                                );
                                    }
                            )

                            .addOnFailureListener(
                                    e ->
                                            onFailure.onFailure(
                                                    e.getMessage()
                                            )
                            );
                })

                .addOnFailureListener(
                        e ->
                                onFailure.onFailure(
                                        e.getMessage()
                                )
                );
    }

    // =========================================
    // 3. GET PENDING DELIVERIES
    // =========================================

    public static void getPendingDeliveries(

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        db.collection(DELIVERY_COLLECTION)

                .orderBy(
                        "storedAt",
                        Query.Direction.DESCENDING
                )

                .get()

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc :
                            snapshot) {

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
                        e ->
                                onFailure.onFailure(
                                        e.getMessage()
                                )
                );
    }

    // =========================================
    // 4. GET ALL DELIVERIES
    // =========================================

    public static void getAllDeliveries(

            String statusFilter,

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        Query query =
                db.collection(DELIVERY_COLLECTION)

                        .orderBy(
                                "storedAt",
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

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc :
                            snapshot) {

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
                        e ->
                                onFailure.onFailure(
                                        e.getMessage()
                                )
                );
    }

    // =========================================
    // 5. GET MY DELIVERIES
    // =========================================

    public static void getMyDeliveries(

            String flatNumber,

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        db.collection(DELIVERY_COLLECTION)

                .whereEqualTo(
                        "flatNumber",
                        flatNumber
                )

                .get()

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    for (QueryDocumentSnapshot doc :
                            snapshot) {

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
                        e ->
                                onFailure.onFailure(
                                        e.getMessage()
                                )
                );
    }

    // =========================================
    // 6. GET RESIDENT LOCKERS
    // =========================================

    public static void getResidentLockerDeliveries(

            String residentId,

            OnDeliveryListLoaded onLoaded,

            OnFailureCallback onFailure
    ) {

        db.collection(DELIVERY_COLLECTION)

                .whereEqualTo(
                        "residentId",
                        residentId
                )

                .whereEqualTo(
                        "status",
                        "locker"
                )

                .get()

                .addOnSuccessListener(snapshot -> {

                    List<DeliveryModel> list =
                            new ArrayList<>();

                    long currentTime =
                            System.currentTimeMillis();

                    for (DocumentSnapshot doc :
                            snapshot.getDocuments()) {

                        DeliveryModel d =
                                doc.toObject(
                                        DeliveryModel.class
                                );

                        if (d != null) {

                            d.setId(doc.getId());

                            // AUTO EXPIRE

                            if (
                                    d.getLockerExpiresAt() > 0 &&
                                            currentTime >
                                                    d.getLockerExpiresAt()
                            ) {

                                d.setStatus("expired");
                            }

                            // SHOW ONLY ACTIVE

                            if (!d.isExpired()) {

                                list.add(d);
                            }
                        }
                    }

                    onLoaded.onLoaded(list);
                })

                .addOnFailureListener(
                        e ->
                                onFailure.onFailure(
                                        e.getMessage()
                                )
                );
    }

    // =========================================
    // 7. CONFIRM PICKUP
    // =========================================

    public static void confirmPickup(

            String deliveryId,

            OnSuccessCallback onSuccess,

            OnFailureCallback onFailure
    ) {

        db.collection(DELIVERY_COLLECTION)

                .document(deliveryId)

                .get()

                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) {

                        onFailure.onFailure(
                                "Delivery not found"
                        );

                        return;
                    }

                    String lockerId =
                            documentSnapshot.getString(
                                    "lockerId"
                            );

                    Map<String, Object> updates =
                            new HashMap<>();

                    updates.put(
                            "status",
                            "pickedup"
                    );

                    updates.put(
                            "pickedUpAt",
                            System.currentTimeMillis()
                    );

                    // =================================
                    // UPDATE DELIVERY
                    // =================================

                    db.collection(DELIVERY_COLLECTION)

                            .document(deliveryId)

                            .update(updates)

                            .addOnSuccessListener(
                                    aVoid -> {

                                        // =====================
                                        // FREE LOCKER
                                        // =====================

                                        if (lockerId != null &&
                                                !lockerId.isEmpty()) {

                                            Map<String, Object>
                                                    lockerUpdate =
                                                    new HashMap<>();

                                            lockerUpdate.put(
                                                    "status",
                                                    "available"
                                            );

                                            lockerUpdate.put(
                                                    "residentId",
                                                    ""
                                            );

                                            lockerUpdate.put(
                                                    "residentEmail",
                                                    ""
                                            );

                                            lockerUpdate.put(
                                                    "deliveryId",
                                                    ""
                                            );

                                            lockerUpdate.put(
                                                    "otp",
                                                    ""
                                            );

                                            lockerUpdate.put(
                                                    "storedAt",
                                                    0L
                                            );

                                            lockerUpdate.put(
                                                    "expiresAt",
                                                    0L
                                            );

                                            db.collection(
                                                            LOCKER_COLLECTION
                                                    )

                                                    .document(lockerId)

                                                    .update(lockerUpdate)

                                                    .addOnSuccessListener(
                                                            unused ->
                                                                    onSuccess.onSuccess()
                                                    )

                                                    .addOnFailureListener(
                                                            e ->
                                                                    onFailure.onFailure(
                                                                            e.getMessage()
                                                                    )
                                                    );

                                        } else {

                                            onSuccess.onSuccess();
                                        }
                                    }
                            )

                            .addOnFailureListener(
                                    e ->
                                            onFailure.onFailure(
                                                    e.getMessage()
                                            )
                            );
                })

                .addOnFailureListener(
                        e ->
                                onFailure.onFailure(
                                        e.getMessage()
                                )
                );
    }

    // =========================================
    // 8. DELETE DELIVERY
    // =========================================

    public static void deleteDelivery(

            String deliveryId,

            OnSuccessCallback onSuccess,

            OnFailureCallback onFailure
    ) {

        db.collection(DELIVERY_COLLECTION)

                .document(deliveryId)

                .delete()

                .addOnSuccessListener(
                        aVoid -> onSuccess.onSuccess()
                )

                .addOnFailureListener(
                        e ->
                                onFailure.onFailure(
                                        e.getMessage()
                                )
                );
    }
}