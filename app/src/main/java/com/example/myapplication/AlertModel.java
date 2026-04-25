package com.example.myapplication;

/**
 * AlertModel.java
 *
 * Firestore collection: "alerts"
 *
 * Document fields:
 *   id          — String  (Firestore doc ID)
 *   title       — String  (short alert title)
 *   message     — String  (full alert description)
 *   type        — String  ("sos" | "announcement" | "maintenance")
 *   postedBy    — String  (display name / email of poster)
 *   postedById  — String  (Firebase UID of poster)
 *   flatNumber  — String  (optional — flat number of poster)
 *   timestamp   — long    (when alert was posted)
 *   isResolved  — boolean (true if alert has been resolved/closed)
 */
public class AlertModel {

    private String  id;
    private String  title;
    private String  message;
    private String  type;         // "sos" | "announcement" | "maintenance"
    private String  postedBy;
    private String  postedById;
    private String  flatNumber;
    private long    timestamp;
    private boolean isResolved;

    // Required empty constructor for Firestore
    public AlertModel() {}

    public AlertModel(String id, String title, String message, String type,
                      String postedBy, String postedById, String flatNumber,
                      long timestamp, boolean isResolved) {
        this.id          = id;
        this.title       = title;
        this.message     = message;
        this.type        = type;
        this.postedBy    = postedBy;
        this.postedById  = postedById;
        this.flatNumber  = flatNumber;
        this.timestamp   = timestamp;
        this.isResolved  = isResolved;
    }

    // ── Getters ────────────────────────────────────────────────────────────────
    public String  getId()          { return id; }
    public String  getTitle()       { return title; }
    public String  getMessage()     { return message; }
    public String  getType()        { return type; }
    public String  getPostedBy()    { return postedBy; }
    public String  getPostedById()  { return postedById; }
    public String  getFlatNumber()  { return flatNumber; }
    public long    getTimestamp()   { return timestamp; }
    public boolean isResolved()     { return isResolved; }

    // ── Setters ────────────────────────────────────────────────────────────────
    public void setId(String id)               { this.id = id; }
    public void setTitle(String title)         { this.title = title; }
    public void setMessage(String message)     { this.message = message; }
    public void setType(String type)           { this.type = type; }
    public void setPostedBy(String postedBy)   { this.postedBy = postedBy; }
    public void setPostedById(String id)       { this.postedById = id; }
    public void setFlatNumber(String flat)     { this.flatNumber = flat; }
    public void setTimestamp(long timestamp)   { this.timestamp = timestamp; }
    public void setResolved(boolean resolved)  { this.isResolved = resolved; }

    // ── Helpers ────────────────────────────────────────────────────────────────
    public boolean isSOS()          { return "sos".equals(type); }
    public boolean isAnnouncement() { return "announcement".equals(type); }
    public boolean isMaintenance()  { return "maintenance".equals(type); }
}
