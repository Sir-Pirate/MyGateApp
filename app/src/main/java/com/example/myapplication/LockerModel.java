package com.example.myapplication;

public class LockerModel {

    private String lockerId;

    private String residentId;

    private String residentEmail;

    private String residentName;

    private String deliveryId;

    private String courierName;

    private String flatNumber;

    private String otp;

    /**
     * available
     * active
     * expired
     * pickedup
     */
    private String status;

    private long storedAt;

    private long expiresAt;

    // =========================================
    // EMPTY CONSTRUCTOR
    // =========================================

    public LockerModel() {
    }

    // =========================================
    // GETTERS
    // =========================================

    public String getLockerId() {
        return lockerId;
    }

    public String getResidentId() {
        return residentId;
    }

    public String getResidentEmail() {
        return residentEmail;
    }

    public String getResidentName() {
        return residentName;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public String getCourierName() {
        return courierName;
    }

    public String getFlatNumber() {
        return flatNumber;
    }

    public String getOtp() {
        return otp;
    }

    public String getStatus() {
        return status;
    }

    public long getStoredAt() {
        return storedAt;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    // =========================================
    // SETTERS
    // =========================================

    public void setLockerId(String lockerId) {
        this.lockerId = lockerId;
    }

    public void setResidentId(String residentId) {
        this.residentId = residentId;
    }

    public void setResidentEmail(String residentEmail) {
        this.residentEmail = residentEmail;
    }

    public void setResidentName(String residentName) {
        this.residentName = residentName;
    }

    public void setDeliveryId(String deliveryId) {
        this.deliveryId = deliveryId;
    }

    public void setCourierName(String courierName) {
        this.courierName = courierName;
    }

    public void setFlatNumber(String flatNumber) {
        this.flatNumber = flatNumber;
    }

    public void setOtp(String otp) {
        this.otp = otp;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStoredAt(long storedAt) {
        this.storedAt = storedAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    // =========================================
    // HELPERS
    // =========================================

    public boolean isAvailable() {
        return "available".equalsIgnoreCase(status);
    }

    public boolean isActive() {

        if (expiresAt > 0 &&
                System.currentTimeMillis() > expiresAt) {

            return false;
        }

        return "active".equalsIgnoreCase(status)
                || "stored".equalsIgnoreCase(status);
    }

    public boolean isExpired() {

        if (expiresAt <= 0) {
            return false;
        }

        return System.currentTimeMillis() > expiresAt;
    }

    public boolean isPickedUp() {
        return "pickedup".equalsIgnoreCase(status);
    }

    // =========================================
    // AUTO STATUS
    // =========================================

    public String getDisplayStatus() {

        if (isPickedUp()) {
            return "Picked Up";
        }

        if (isExpired()) {
            return "Expired";
        }

        if (isAvailable()) {
            return "Available";
        }

        if (isActive()) {
            return "Waiting for Pickup";
        }

        return status;
    }

    // =========================================
    // TIME HELPERS
    // =========================================

    public long getRemainingTimeMillis() {

        if (expiresAt <= 0) {
            return 0;
        }

        return expiresAt - System.currentTimeMillis();
    }

    public long getHoursRemaining() {

        long remaining =
                getRemainingTimeMillis();

        return remaining / (1000 * 60 * 60);
    }
}