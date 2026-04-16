package com.example.myapplication;

/**
 * VisitorModel.java
 *
 * Firestore document structure — collection: "visitors"
 *
 * Document fields:
 *   id           — String  (Firestore doc ID)
 *   name         — String  (visitor's full name)
 *   phone        — String  (10-digit phone)
 *   note         — String  (purpose, optional)
 *   status       — String  ("approved" | "arrived" | "rejected")
 *   residentId   — String  (UID of resident who approved)
 *   residentName — String  (display name of resident)
 *   approvedAt   — long    (timestamp millis)
 *   arrivedAt    — long    (timestamp millis, 0 if not arrived yet)
 */
public class VisitorModel {

    private String id;
    private String name;
    private String phone;
    private String note;
    private String status;        // "approved" | "arrived" | "rejected"
    private String residentId;
    private String residentName;
    private long   approvedAt;
    private long   arrivedAt;

    // Required empty constructor for Firestore deserialization
    public VisitorModel() {}

    public VisitorModel(String id, String name, String phone, String note,
                        String status, String residentId, String residentName,
                        long approvedAt, long arrivedAt) {
        this.id           = id;
        this.name         = name;
        this.phone        = phone;
        this.note         = note;
        this.status       = status;
        this.residentId   = residentId;
        this.residentName = residentName;
        this.approvedAt   = approvedAt;
        this.arrivedAt    = arrivedAt;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String getId()           { return id; }
    public String getName()         { return name; }
    public String getPhone()        { return phone; }
    public String getNote()         { return note; }
    public String getStatus()       { return status; }
    public String getResidentId()   { return residentId; }
    public String getResidentName() { return residentName; }
    public long   getApprovedAt()   { return approvedAt; }
    public long   getArrivedAt()    { return arrivedAt; }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setId(String id)                   { this.id = id; }
    public void setName(String name)               { this.name = name; }
    public void setPhone(String phone)             { this.phone = phone; }
    public void setNote(String note)               { this.note = note; }
    public void setStatus(String status)           { this.status = status; }
    public void setResidentId(String residentId)   { this.residentId = residentId; }
    public void setResidentName(String residentName) { this.residentName = residentName; }
    public void setApprovedAt(long approvedAt)     { this.approvedAt = approvedAt; }
    public void setArrivedAt(long arrivedAt)       { this.arrivedAt = arrivedAt; }

    // ── Helpers ────────────────────────────────────────────────────────────────
    public boolean isApproved() { return "approved".equals(status); }
    public boolean isArrived()  { return "arrived".equals(status); }
}
