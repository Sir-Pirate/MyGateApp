package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * DeliveryLogActivity.java
 *
 * Role   : Guard
 * Purpose: Guard enters courier name, phone, and flat number to log a delivery.
 *          Saves to Firestore via DeliveryManager.logDelivery().
 *
 * Flow   : homeactivity → DeliveryLogActivity → DeliveryActivity (view all)
 */
public class DeliveryLogActivity extends AppCompatActivity {

    private TextInputLayout   tilCourierName, tilCourierPhone, tilFlatNumber;
    private TextInputEditText etCourierName, etCourierPhone, etFlatNumber;
    private MaterialButton    btnLogDelivery, btnViewDeliveries, btnLogBackToHome;
    private TextView          tvLogStatus;
    private ProgressBar       progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_log);
        bindViews();
        setClickListeners();
    }

    private void bindViews() {
        tilCourierName    = findViewById(R.id.tilCourierName);
        etCourierName     = findViewById(R.id.etCourierName);
        tilCourierPhone   = findViewById(R.id.tilCourierPhone);
        etCourierPhone    = findViewById(R.id.etCourierPhone);
        tilFlatNumber     = findViewById(R.id.tilFlatNumber);
        etFlatNumber      = findViewById(R.id.etFlatNumber);
        btnLogDelivery    = findViewById(R.id.btnLogDelivery);
        btnViewDeliveries = findViewById(R.id.btnViewDeliveries);
        btnLogBackToHome  = findViewById(R.id.btnLogBackToHome);
        tvLogStatus       = findViewById(R.id.tvLogStatus);
        progressBar       = findViewById(R.id.progressBarLog);
    }

    private void setClickListeners() {
        btnLogDelivery.setOnClickListener(v -> {
            if (validateInputs()) submitDelivery();
        });

        btnViewDeliveries.setOnClickListener(v ->
            startActivity(new Intent(this, DeliveryActivity.class))
        );

        btnLogBackToHome.setOnClickListener(v -> navigateToHome());
    }

    private boolean validateInputs() {
        boolean isValid = true;

        String name  = getText(etCourierName);
        String phone = getText(etCourierPhone);
        String flat  = getText(etFlatNumber);

        tilCourierName.setError(null);
        tilCourierPhone.setError(null);
        tilFlatNumber.setError(null);

        if (name.isEmpty())  { tilCourierName.setError("Courier name is required"); isValid = false; }
        if (phone.isEmpty()) { tilCourierPhone.setError("Phone number is required"); isValid = false; }
        else if (phone.length() != 10 || !phone.matches("[0-9]+")) {
            tilCourierPhone.setError("Enter a valid 10-digit phone number"); isValid = false;
        }
        if (flat.isEmpty())  { tilFlatNumber.setError("Flat number is required"); isValid = false; }

        return isValid;
    }

    private void submitDelivery() {
        String name  = getText(etCourierName);
        String phone = getText(etCourierPhone);
        String flat  = getText(etFlatNumber);

        showLoading(true);

        DeliveryManager.logDelivery(name, phone, flat,
            () -> {
                showLoading(false);
                showStatus("✓ Delivery logged! Resident for Flat " + flat + " has been notified.", false);
                clearFields();
            },
            errorMsg -> {
                showLoading(false);
                showStatus("✗ Failed to log: " + errorMsg, true);
            }
        );
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnLogDelivery.setEnabled(!show);
    }

    private void showStatus(String message, boolean isError) {
        tvLogStatus.setText(message);
        tvLogStatus.setTextColor(isError ? Color.parseColor("#B71C1C") : Color.parseColor("#1B5E20"));
        tvLogStatus.setBackgroundColor(isError ? Color.parseColor("#FFEBEE") : Color.parseColor("#E8F5E9"));
        tvLogStatus.setVisibility(View.VISIBLE);
    }

    private void clearFields() {
        if (etCourierName.getText()  != null) etCourierName.getText().clear();
        if (etCourierPhone.getText() != null) etCourierPhone.getText().clear();
        if (etFlatNumber.getText()   != null) etFlatNumber.getText().clear();
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, homeactivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() { navigateToHome(); }
}
