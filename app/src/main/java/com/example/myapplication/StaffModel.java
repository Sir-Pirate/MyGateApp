package com.example.myapplication;

/**
 * StaffModel.java
 *
 * Firestore collection: "staff"
 *
 * Document fields:
 *   id          — String  (Firestore doc ID)
 *   name        — String  (staff full name)
 *   phone       — String  (10-digit phone — used as unique identifier)
 *   staffType   — String  ("Maid" | "Driver" | "Plumber" | "Cook" | "Security" | "Other")
 *   flatNumber  — String  (flat they work in, optional)
 *   status      — String  ("inside" | "exited")
 *   entryTime   — long    (timestamp of check-in)
 *   exitTime    — long    (timestamp of exit, 0 if still inside)
 *   registeredAt— long    (first time they registered)
 */
public class StaffModel {

    private String id;
    private String name;
    private String phone;
    private String staffType;
    private String flatNumber;
    private String status;       // "inside" | "exited"
    private long   entryTime;
    private long   exitTime;
    private long   registeredAt;

    // Required empty constructor for Firestore
    public StaffModel() {}

    public StaffModel(String id, String name, String phone, String staffType,
                      String flatNumber, String status,
                      long entryTime, long exitTime, long registeredAt) {
        this.id           = id;
        this.name         = name;
        this.phone        = phone;
        this.staffType    = staffType;
        this.flatNumber   = flatNumber;
        this.status       = status;
        this.entryTime    = entryTime;
        this.exitTime     = exitTime;
        this.registeredAt = registeredAt;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String getId()           { return id; }
    public String getName()         { return name; }
    public String getPhone()        { return phone; }
    public String getStaffType()    { return staffType; }
    public String getFlatNumber()   { return flatNumber; }
    public String getStatus()       { return status; }
    public long   getEntryTime()    { return entryTime; }
    public long   getExitTime()     { return exitTime; }
    public long   getRegisteredAt() { return registeredAt; }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setId(String id)                 { this.id = id; }
    public void setName(String name)             { this.name = name; }
    public void setPhone(String phone)           { this.phone = phone; }
    public void setStaffType(String staffType)   { this.staffType = staffType; }
    public void setFlatNumber(String flatNumber) { this.flatNumber = flatNumber; }
    public void setStatus(String status)         { this.status = status; }
    public void setEntryTime(long entryTime)     { this.entryTime = entryTime; }
    public void setExitTime(long exitTime)       { this.exitTime = exitTime; }
    public void setRegisteredAt(long r)          { this.registeredAt = r; }

    // ── Helpers ────────────────────────────────────────────────────────────────
    public boolean isInside()  { return "inside".equals(status); }
    public boolean isExited()  { return "exited".equals(status); }
}
