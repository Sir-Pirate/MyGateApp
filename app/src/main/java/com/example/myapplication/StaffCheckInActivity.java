package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * StaffCheckInActivity.java
 *
 * Role   : Staff (Maid / Driver / Plumber etc.)
 * Purpose: Staff enters their phone number.
 *          - If registered → shows their profile + Check In / Check Out button
 *          - If new        → shows registration form, then checks them in
 *
 * Flow   : homeactivity → StaffCheckInActivity
 *          StaffCheckInActivity → StaffActivity (view all staff)
 */
public class StaffCheckInActivity extends AppCompatActivity {

    // ── Step 1 UI ──────────────────────────────────────────────────────────────
    private TextInputLayout   tilPhone;
    private TextInputEditText etPhone;
    private MaterialButton    btnLookup;

    // ── Step 2a: Returning Staff UI ────────────────────────────────────────────
    private CardView       cardReturningStaff;
    private TextView       tvStaffName, tvStaffType, tvCurrentStatus;
    private MaterialButton btnCheckIn, btnCheckOut;

    // ── Step 2b: New Staff Registration UI ────────────────────────────────────
    private CardView          cardNewStaff;
    private TextInputLayout   tilName, tilFlat;
    private TextInputEditText etName, etFlat;
    private Spinner           spinnerStaffType;
    private MaterialButton    btnRegisterCheckIn;

    // ── Common ─────────────────────────────────────────────────────────────────
    private ProgressBar    progressBar;
    private TextView       tvStatus;
    private MaterialButton btnViewStaff, btnBackToHome;

    private final String[] STAFF_TYPES = {
            "Select Staff Type", "Maid", "Cook", "Driver",
            "Plumber", "Electrician", "Security", "Gardener", "Other"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_checkin);
        bindViews();
        setupSpinner();
        setClickListeners();
    }

    // ── View Binding ───────────────────────────────────────────────────────────
    private void bindViews() {
        tilPhone           = findViewById(R.id.tilPhone);
        etPhone            = findViewById(R.id.etPhone);
        btnLookup          = findViewById(R.id.btnLookup);

        cardReturningStaff = findViewById(R.id.cardReturningStaff);
        tvStaffName        = findViewById(R.id.tvStaffName);
        tvStaffType        = findViewById(R.id.tvStaffType);
        tvCurrentStatus    = findViewById(R.id.tvCurrentStatus);
        btnCheckIn         = findViewById(R.id.btnCheckIn);
        btnCheckOut        = findViewById(R.id.btnCheckOut);

        cardNewStaff       = findViewById(R.id.cardNewStaff);
        tilName            = findViewById(R.id.tilName);
        etName             = findViewById(R.id.etName);
        spinnerStaffType   = findViewById(R.id.spinnerStaffType);
        tilFlat            = findViewById(R.id.tilFlat);
        etFlat             = findViewById(R.id.etFlat);
        btnRegisterCheckIn = findViewById(R.id.btnRegisterCheckIn);

        progressBar        = findViewById(R.id.progressBar);
        tvStatus           = findViewById(R.id.tvStatus);
        btnViewStaff       = findViewById(R.id.btnViewStaff);
        btnBackToHome      = findViewById(R.id.btnBackToHome);
    }

    private void setupSpinner() {
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, STAFF_TYPES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStaffType.setAdapter(adapter);
    }

    // ── Click Listeners ────────────────────────────────────────────────────────
    private void setClickListeners() {

        // Step 1: Look up by phone
        btnLookup.setOnClickListener(v -> {
            String phone = getText(etPhone);
            if (validatePhone(phone)) lookupStaff(phone);
        });

        // Step 2a: Check In (returning staff who is currently outside)
        btnCheckIn.setOnClickListener(v -> {
            String phone = getText(etPhone);
            showLoading(true);
            StaffManager.checkIn(phone,
                () -> {
                    showLoading(false);
                    showStatus("✓ Checked in successfully! Welcome.", false);
                    resetForm();
                },
                err -> { showLoading(false); showStatus("✗ " + err, true); }
            );
        });

        // Step 2a: Check Out (staff currently inside)
        btnCheckOut.setOnClickListener(v -> {
            String phone = getText(etPhone);
            showLoading(true);
            StaffManager.checkOut(phone,
                () -> {
                    showLoading(false);
                    showStatus("✓ Checked out. Have a good day!", false);
                    resetForm();
                },
                err -> { showLoading(false); showStatus("✗ " + err, true); }
            );
        });

        // Step 2b: Register + Check In (new staff)
        btnRegisterCheckIn.setOnClickListener(v -> {
            if (validateRegistration()) registerAndCheckIn();
        });

        btnViewStaff.setOnClickListener(v ->
            startActivity(new Intent(this, StaffActivity.class))
        );

        btnBackToHome.setOnClickListener(v -> navigateToHome());
    }

    // ── Lookup ─────────────────────────────────────────────────────────────────
    private void lookupStaff(String phone) {
        showLoading(true);
        hideCards();
        hideStatus();

        StaffManager.getStaffByPhone(phone,
            staff -> {
                // Returning staff
                showLoading(false);
                tvStaffName.setText(staff.getName());
                tvStaffType.setText(staff.getStaffType());

                if (staff.isInside()) {
                    tvCurrentStatus.setText("Currently Inside");
                    tvCurrentStatus.setTextColor(Color.parseColor("#2E7D32"));
                    btnCheckIn.setVisibility(View.GONE);
                    btnCheckOut.setVisibility(View.VISIBLE);
                } else {
                    tvCurrentStatus.setText("Currently Outside");
                    tvCurrentStatus.setTextColor(Color.parseColor("#C62828"));
                    btnCheckIn.setVisibility(View.VISIBLE);
                    btnCheckOut.setVisibility(View.GONE);
                }

                cardReturningStaff.setVisibility(View.VISIBLE);
            },
            () -> {
                // New staff — show registration form
                showLoading(false);
                cardNewStaff.setVisibility(View.VISIBLE);
            },
            err -> { showLoading(false); showStatus("✗ " + err, true); }
        );
    }

    // ── Register + Check In ────────────────────────────────────────────────────
    private void registerAndCheckIn() {
        String phone     = getText(etPhone);
        String name      = getText(etName);
        String staffType = spinnerStaffType.getSelectedItem().toString();
        String flat      = getText(etFlat);

        showLoading(true);

        StaffManager.registerAndCheckIn(name, phone, staffType, flat,
            () -> {
                showLoading(false);
                showStatus("✓ Registered and checked in! Welcome, " + name + ".", false);
                resetForm();
            },
            err -> { showLoading(false); showStatus("✗ " + err, true); }
        );
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private boolean validatePhone(String phone) {
        tilPhone.setError(null);
        if (phone.isEmpty()) { tilPhone.setError("Phone number required"); return false; }
        if (phone.length() != 10 || !phone.matches("[0-9]+")) {
            tilPhone.setError("Enter a valid 10-digit number"); return false;
        }
        return true;
    }

    private boolean validateRegistration() {
        boolean isValid = true;
        tilName.setError(null);

        String name      = getText(etName);
        String staffType = spinnerStaffType.getSelectedItem().toString();

        if (name.isEmpty()) { tilName.setError("Name is required"); isValid = false; }
        if (staffType.equals("Select Staff Type")) {
            showStatus("Please select your staff type", true); isValid = false;
        }
        return isValid;
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────────
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLookup.setEnabled(!show);
    }

    private void showStatus(String message, boolean isError) {
        tvStatus.setText(message);
        tvStatus.setTextColor(isError ? Color.parseColor("#B71C1C") : Color.parseColor("#1B5E20"));
        tvStatus.setBackgroundColor(isError ? Color.parseColor("#FFEBEE") : Color.parseColor("#E8F5E9"));
        tvStatus.setVisibility(View.VISIBLE);
    }

    private void hideStatus() { tvStatus.setVisibility(View.GONE); }
    private void hideCards() {
        cardReturningStaff.setVisibility(View.GONE);
        cardNewStaff.setVisibility(View.GONE);
    }

    private void resetForm() {
        if (etPhone.getText() != null) etPhone.getText().clear();
        if (etName.getText()  != null) etName.getText().clear();
        if (etFlat.getText()  != null) etFlat.getText().clear();
        spinnerStaffType.setSelection(0);
        hideCards();
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() { navigateToHome(); }
}
