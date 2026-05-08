package com.example.myapplication;

public class StaffModel {

    public String id;
    public String name;
    public String role;

    public boolean isLoggedIn;

    // Attendance fields
    public long loginTime;
    public long logoutTime;
    public long durationMinutes;

    // Empty constructor required for Firestore
    public StaffModel() {
    }

    public StaffModel(
            String id,
            String name,
            String role,
            boolean isLoggedIn,
            long loginTime,
            long logoutTime,
            long durationMinutes
    ) {

        this.id = id;
        this.name = name;
        this.role = role;

        this.isLoggedIn = isLoggedIn;

        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.durationMinutes = durationMinutes;
    }
}