package com.example.myapplication;

/**
 * DeliveryModel.java
 *
 * Firestore collection: "deliveries"
 *
 * Document fields:
 *   id             — String  (Firestore doc ID)
 *   courierName    — String  (name of courier/delivery person)
 *   courierPhone   — String  (10-digit phone)
 *   flatNumber     — String  (destination flat, e.g. "4B")
 *   residentId     — String  (UID of resident for that flat)
 *   residentEmail  — String  (email of resident)
 *   status         — String  ("pending" | "pickedup")
 *   loggedAt       — long    (timestamp when guard logged it)
 *   pickedUpAt     — long    (timestamp when resident confirmed pickup, 0 if pending)
 *   guardId        — String  (UID of guard who logged it)
 */
public class DeliveryModel {

    private String id;
    private String courierName;
    private String courierPhone;
    private String flatNumber;
    private String residentId;
    private String residentEmail;
    private String status;       // "pending" | "pickedup"
    private long   loggedAt;
    private long   pickedUpAt;
    private String guardId;

    // Required empty constructor for Firestore
    public DeliveryModel() {}

    public DeliveryModel(String id, String courierName, String courierPhone,
                         String flatNumber, String residentId, String residentEmail,
                         String status, long loggedAt, long pickedUpAt, String guardId) {
        this.id            = id;
        this.courierName   = courierName;
        this.courierPhone  = courierPhone;
        this.flatNumber    = flatNumber;
        this.residentId    = residentId;
        this.residentEmail = residentEmail;
        this.status        = status;
        this.loggedAt      = loggedAt;
        this.pickedUpAt    = pickedUpAt;
        this.guardId       = guardId;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String getId()            { return id; }
    public String getCourierName()   { return courierName; }
    public String getCourierPhone()  { return courierPhone; }
    public String getFlatNumber()    { return flatNumber; }
    public String getResidentId()    { return residentId; }
    public String getResidentEmail() { return residentEmail; }
    public String getStatus()        { return status; }
    public long   getLoggedAt()      { return loggedAt; }
    public long   getPickedUpAt()    { return pickedUpAt; }
    public String getGuardId()       { return guardId; }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setId(String id)                       { this.id = id; }
    public void setCourierName(String courierName)     { this.courierName = courierName; }
    public void setCourierPhone(String courierPhone)   { this.courierPhone = courierPhone; }
    public void setFlatNumber(String flatNumber)       { this.flatNumber = flatNumber; }
    public void setResidentId(String residentId)       { this.residentId = residentId; }
    public void setResidentEmail(String residentEmail) { this.residentEmail = residentEmail; }
    public void setStatus(String status)               { this.status = status; }
    public void setLoggedAt(long loggedAt)             { this.loggedAt = loggedAt; }
    public void setPickedUpAt(long pickedUpAt)         { this.pickedUpAt = pickedUpAt; }
    public void setGuardId(String guardId)             { this.guardId = guardId; }

    // ── Helpers ────────────────────────────────────────────────────────────────
    public boolean isPending()   { return "pending".equals(status); }
    public boolean isPickedUp()  { return "pickedup".equals(status); }
}
