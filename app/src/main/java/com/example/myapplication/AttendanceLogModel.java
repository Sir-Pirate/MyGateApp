package com.example.myapplication;

public class AttendanceLogModel {

    public long loginTime;
    public long logoutTime;
    public long durationMinutes;

    public AttendanceLogModel() {
    }

    public AttendanceLogModel(
            long loginTime,
            long logoutTime,
            long durationMinutes
    ) {
        this.loginTime = loginTime;
        this.logoutTime = logoutTime;
        this.durationMinutes = durationMinutes;
    }
}