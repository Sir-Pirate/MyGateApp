package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class StaffDetailActivity extends AppCompatActivity {

    private TextView tvName, tvPhone, tvRole;
    private TextView tvStatus, tvLastLogin, tvLastLogout, tvDuration;

    private String staffId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_detail);

        bindViews();

        staffId = getIntent().getStringExtra("staffId");

        if (staffId == null || staffId.isEmpty()) {
            Toast.makeText(this, "Staff not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadStaffInfo();
        loadAttendance();
    }

    private void bindViews() {
        tvName = findViewById(R.id.tvName);
        tvPhone = findViewById(R.id.tvPhone);
        tvRole = findViewById(R.id.tvRole);

        tvStatus = findViewById(R.id.tvStatus);
        tvLastLogin = findViewById(R.id.tvLastLogin);
        tvLastLogout = findViewById(R.id.tvLastLogout);
        tvDuration = findViewById(R.id.tvDuration);
    }

    // ---------------- STAFF INFO ----------------
    private void loadStaffInfo() {

        FirebaseFirestore.getInstance()
                .collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(doc -> {

                    if (!doc.exists()) {
                        Toast.makeText(this, "Staff not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    tvName.setText(doc.getString("name"));
                    tvPhone.setText(doc.getString("phone"));
                    tvRole.setText(doc.getString("role"));

                    Boolean isLoggedIn = doc.getBoolean("isLoggedIn");

                    if (Boolean.TRUE.equals(isLoggedIn)) {
                        tvStatus.setText("🟢 Currently Inside");
                    } else {
                        tvStatus.setText("🔴 Not Inside");
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ---------------- ATTENDANCE ----------------
    private void loadAttendance() {

        FirebaseFirestore.getInstance()
                .collection("staff_logs")
                .whereEqualTo("staffId", staffId)
                .orderBy("loginTime", Query.Direction.DESCENDING)
                .limit(5) // safer than 1 (avoids missing/partial logs)
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.isEmpty()) {
                        tvLastLogin.setText("No logs yet");
                        tvLastLogout.setText("No logs yet");
                        tvDuration.setText("0 mins");
                        return;
                    }

                    // Get latest log
                    var doc = snapshot.getDocuments().get(0);

                    Long loginTime = doc.getLong("loginTime");
                    Long logoutTime = doc.getLong("logoutTime");
                    Long duration = doc.getLong("durationMinutes");

                    // ---------------- LOGIN ----------------
                    if (loginTime != null && loginTime > 0) {
                        tvLastLogin.setText("Login: " + formatTime(loginTime));
                    } else {
                        tvLastLogin.setText("Login: —");
                    }

                    // ---------------- LOGOUT ----------------
                    if (logoutTime != null && logoutTime > 0) {
                        tvLastLogout.setText("Logout: " + formatTime(logoutTime));
                    } else {
                        tvLastLogout.setText("Logout: Still active");
                    }

                    // ---------------- DURATION ----------------
                    tvDuration.setText(
                            (duration != null ? duration : 0) + " mins worked"
                    );

                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // ---------------- TIME FORMAT ----------------
    private String formatTime(long millis) {
        if (millis <= 0) return "—";

        return new SimpleDateFormat(
                "dd MMM, hh:mm a",
                Locale.getDefault()
        ).format(new Date(millis));
    }
}