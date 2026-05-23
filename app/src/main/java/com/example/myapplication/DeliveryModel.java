package com.example.myapplication;

/**
 * DeliveryModel.java
 *
 * Firestore collection: "deliveries"
 */

public class DeliveryModel {

    private String id;

    private String courierName;

    private String courierPhone;

    private String flatNumber;

    private String residentId;

    private String residentEmail;

    /**
     * pending
     * locker
     * pickedup
     * expired
     */
    private String status;

    private long loggedAt;

    private long pickedUpAt;

    private String guardId;

    // =====================================
    // LOCKER FIELDS
    // =====================================

    private boolean storedInLocker;

    private String lockerId;

    private String lockerOtp;

    private long lockerStoredAt;

    private long lockerExpiresAt;

    // Required empty constructor
    public DeliveryModel() {}

    public DeliveryModel(
            String id,
            String courierName,
            String courierPhone,
            String flatNumber,
            String residentId,
            String residentEmail,
            String status,
            long loggedAt,
            long pickedUpAt,
            String guardId,
            boolean storedInLocker,
            String lockerId,
            String lockerOtp,
            long lockerStoredAt,
            long lockerExpiresAt
    ) {

        this.id = id;

        this.courierName = courierName;

        this.courierPhone = courierPhone;

        this.flatNumber = flatNumber;

        this.residentId = residentId;

        this.residentEmail = residentEmail;

        this.status = status;

        this.loggedAt = loggedAt;

        this.pickedUpAt = pickedUpAt;

        this.guardId = guardId;

        this.storedInLocker = storedInLocker;

        this.lockerId = lockerId;

        this.lockerOtp = lockerOtp;

        this.lockerStoredAt = lockerStoredAt;

        this.lockerExpiresAt = lockerExpiresAt;
    }

    // =====================================
    // GETTERS
    // =====================================

    public String getId() {
        return id;
    }

    public String getCourierName() {
        return courierName;
    }

    public String getCourierPhone() {
        return courierPhone;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public String getResidentId() {
        return residentId;
    }

    public String getResidentEmail() {
        return residentEmail;
    }

    public String getStatus() {
        return status;
    }

    public long getLoggedAt() {
        return loggedAt;
    }

    public long getPickedUpAt() {
        return pickedUpAt;
    }

    public String getGuardId() {
        return guardId;
    }

    public boolean isStoredInLocker() {
        return storedInLocker;
    }

    public String getLockerId() {
        return lockerId;
    }

    public String getLockerOtp() {
        return lockerOtp;
    }

    public long getLockerStoredAt() {
        return lockerStoredAt;
    }

    public long getLockerExpiresAt() {
        return lockerExpiresAt;
    }

    // =====================================
    // SETTERS
    // =====================================

    public void setId(String id) {
        this.id = id;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public void setCourierPhone(String courierPhone) {
        this.courierPhone = courierPhone;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public void setResidentId(String residentId) {
        this.residentId = residentId;
    }

    public void setResidentEmail(String residentEmail) {
        this.residentEmail = residentEmail;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setLoggedAt(long loggedAt) {
        this.loggedAt = loggedAt;
    }

    public void setPickedUpAt(long pickedUpAt) {
        this.pickedUpAt = pickedUpAt;
    }

    public void setGuardId(String guardId) {
        this.guardId = guardId;
    }

    public void setStoredInLocker(boolean storedInLocker) {
        this.storedInLocker = storedInLocker;
    }

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }

    public void setLockerOtp(String lockerOtp) {
        this.lockerOtp = lockerOtp;
    }

    public void setLockerStoredAt(long lockerStoredAt) {
        this.lockerStoredAt = lockerStoredAt;
    }

    public void setLockerExpiresAt(long lockerExpiresAt) {
        this.lockerExpiresAt = lockerExpiresAt;
    }

    // =====================================
    // HELPERS
    // =====================================

    public boolean isPending() {
        return "pending".equals(status);
    }

    public boolean isPickedUp() {
        return "pickedup".equals(status);
    }

    public boolean isLockerDelivery() {
        return "locker".equals(status);
    }

    public boolean isExpired() {
        return "expired".equals(status);
    }
}