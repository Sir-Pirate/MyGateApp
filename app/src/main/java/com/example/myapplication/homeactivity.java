package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

// Commented out until team members merge their branches
// import AlertsActivity.AlertsActivity;
// import DeliveryActivity.DeliveryActivity;
// import ResidentsActivity.ResidentsActivity;
// import StaffActivity.StaffActivity;
// import VisitorActivity.VisitorActivity;

public class homeactivity extends AppCompatActivity {

    // ── Visitor Management (M3 — your screens) ─────────────────────────────────
    private LinearLayout btnGoToVisitorApprove, btnGoToVisitorArrival;

    // ── Quick Access Grid ──────────────────────────────────────────────────────
    private LinearLayout btnVisitorAuth, btnDelivery, btnStaff;
    private LinearLayout btnAlerts, btnResidents, btnNotices;
    private LinearLayout btnMyProfile, btnParking, btnSOS;

    // ── Header ─────────────────────────────────────────────────────────────────
    private TextView tvWelcome, tvDateTime;

    // ── Logout ─────────────────────────────────────────────────────────────────
    private MaterialButton btnLogout;

    // ── Firebase ───────────────────────────────────────────────────────────────
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        bindViews();
        setWelcomeHeader();
        setClickListeners();
    }

    // ── View Binding ───────────────────────────────────────────────────────────
    private void bindViews() {
        // Header
        tvWelcome    = findViewById(R.id.tvWelcome);
        tvDateTime   = findViewById(R.id.tvDateTime);

        // Visitor Management
        btnGoToVisitorApprove = findViewById(R.id.btnGoToVisitorApprove);
        btnGoToVisitorArrival = findViewById(R.id.btnGoToVisitorArrival);

        // Quick Access
        btnVisitorAuth = findViewById(R.id.btnVisitorAuth);
        btnDelivery    = findViewById(R.id.btnDelivery);
        btnStaff       = findViewById(R.id.btnStaff);
        btnAlerts      = findViewById(R.id.btnAlerts);
        btnResidents   = findViewById(R.id.btnResidents);
        btnNotices     = findViewById(R.id.btnNotices);
        btnMyProfile   = findViewById(R.id.btnMyProfile);
        btnParking     = findViewById(R.id.btnParking);
        btnSOS         = findViewById(R.id.btnSOS);

        // Logout
        btnLogout = findViewById(R.id.btnLogout);
    }

    // ── Welcome Header ─────────────────────────────────────────────────────────
    private void setWelcomeHeader() {
        // Show logged-in user's email in welcome text
        FirebaseUser user = auth.getCurrentUser();
        if (user != null && user.getEmail() != null) {
            String email = user.getEmail();
            String name  = email.substring(0, email.indexOf('@')); // use part before @
            tvWelcome.setText("Welcome, " + name + "!");
        }

        // Show current date
        String date = new SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(new Date());
        tvDateTime.setText(date);
    }

    // ── Click Listeners ────────────────────────────────────────────────────────
    private void setClickListeners() {

        // ── Visitor Management (your screens — fully working) ──────────────────
        btnGoToVisitorApprove.setOnClickListener(v ->
            startActivity(new Intent(this, VisitorApproveActivity.class))
        );

        btnGoToVisitorArrival.setOnClickListener(v ->
            startActivity(new Intent(this, VisitorArrivalActivity.class))
        );

        // ── Quick Access (commented out until team merges) ─────────────────────
        btnVisitorAuth.setOnClickListener(v ->
            showComingSoon("Visitor Auth")
            // startActivity(new Intent(this, VisitorActivity.class))
        );

        btnDelivery.setOnClickListener(v ->
            showComingSoon("Delivery Management")
            // startActivity(new Intent(this, DeliveryActivity.class))
        );

        btnStaff.setOnClickListener(v ->
            showComingSoon("Staff Entry")
            // startActivity(new Intent(this, StaffActivity.class))
        );

        btnAlerts.setOnClickListener(v ->
            showComingSoon("Alerts")
            // startActivity(new Intent(this, AlertsActivity.class))
        );

        btnResidents.setOnClickListener(v ->
            showComingSoon("Residents Directory")
            // startActivity(new Intent(this, ResidentsActivity.class))
        );

        // ── New Buttons (placeholders — wire up when ready) ────────────────────
        btnNotices.setOnClickListener(v ->
            showComingSoon("Notices Board")
        );

        btnMyProfile.setOnClickListener(v ->
            showComingSoon("My Profile")
        );

        btnParking.setOnClickListener(v ->
            showComingSoon("Parking Management")
        );

        btnSOS.setOnClickListener(v ->
            showSOSDialog()
        );

        // ── Logout ─────────────────────────────────────────────────────────────
        btnLogout.setOnClickListener(v -> showLogoutDialog());
    }

    // ── Helper: Coming Soon Toast ──────────────────────────────────────────────
    private void showComingSoon(String feature) {
        Toast.makeText(this, feature + " — Coming Soon!", Toast.LENGTH_SHORT).show();
    }

    // ── Helper: Logout Confirmation Dialog ────────────────────────────────────
    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes, Logout", (dialog, which) -> {
                auth.signOut();
                Intent intent = new Intent(this, mainactivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    // ── Helper: SOS Emergency Dialog ──────────────────────────────────────────
    private void showSOSDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🆘 Emergency SOS")
            .setMessage("This will alert the security guard immediately. Confirm?")
            .setPositiveButton("Send Alert", (dialog, which) -> {
                Toast.makeText(this, "🚨 SOS Alert Sent to Security!", Toast.LENGTH_LONG).show();
                // TODO: Connect to Firebase to send SOS notification
            })
            .setNegativeButton("Cancel", null)
            .show();
    }
}
