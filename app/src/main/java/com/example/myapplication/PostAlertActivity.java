package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/**
 * PostAlertActivity.java
 *
 * Allows residents and guards to post a new alert.
 * Select type (SOS / Announcement / Maintenance), fill title + message, submit.
 * Saves to Firestore via AlertManager.postAlert().
 */
public class PostAlertActivity extends AppCompatActivity {

    private LinearLayout      btnTypeSOSSelect, btnTypeAnnouncementSelect, btnTypeMaintenanceSelect;
    private TextInputLayout   tilAlertTitle, tilAlertMessage, tilAlertFlat;
    private TextInputEditText etAlertTitle, etAlertMessage, etAlertFlat;
    private MaterialButton    btnSubmitAlert, btnCancelPost;
    private TextView          tvPostAlertStatus;
    private ProgressBar       progressBar;

    private String selectedType = "sos"; // default

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_alert);
        bindViews();
        setTypeSelection("sos"); // highlight SOS by default
        setClickListeners();
    }

    private void bindViews() {
        btnTypeSOSSelect          = findViewById(R.id.btnTypeSOSSelect);
        btnTypeAnnouncementSelect = findViewById(R.id.btnTypeAnnouncementSelect);
        btnTypeMaintenanceSelect  = findViewById(R.id.btnTypeMaintenanceSelect);
        tilAlertTitle             = findViewById(R.id.tilAlertTitle);
        etAlertTitle              = findViewById(R.id.etAlertTitle);
        tilAlertMessage           = findViewById(R.id.tilAlertMessage);
        etAlertMessage            = findViewById(R.id.etAlertMessage);
        tilAlertFlat              = findViewById(R.id.tilAlertFlat);
        etAlertFlat               = findViewById(R.id.etAlertFlat);
        btnSubmitAlert            = findViewById(R.id.btnSubmitAlert);
        btnCancelPost             = findViewById(R.id.btnCancelPost);
        tvPostAlertStatus         = findViewById(R.id.tvPostAlertStatus);
        progressBar               = findViewById(R.id.progressBarPost);
    }

    private void setClickListeners() {
        btnTypeSOSSelect.setOnClickListener(v          -> setTypeSelection("sos"));
        btnTypeAnnouncementSelect.setOnClickListener(v -> setTypeSelection("announcement"));
        btnTypeMaintenanceSelect.setOnClickListener(v  -> setTypeSelection("maintenance"));

        btnSubmitAlert.setOnClickListener(v -> { if (validateInputs()) submitAlert(); });
        btnCancelPost.setOnClickListener(v  -> finish());
    }

    // ── Type Selection ─────────────────────────────────────────────────────────
    private void setTypeSelection(String type) {
        selectedType = type;

        // Reset all
        btnTypeSOSSelect.setBackgroundResource(R.drawable.type_unselected_bg);
        btnTypeAnnouncementSelect.setBackgroundResource(R.drawable.type_unselected_bg);
        btnTypeMaintenanceSelect.setBackgroundResource(R.drawable.type_unselected_bg);

        // Set text color for labels (children index 1 = TextView)
        setTypeTextColor(btnTypeSOSSelect, "#78909C");
        setTypeTextColor(btnTypeAnnouncementSelect, "#78909C");
        setTypeTextColor(btnTypeMaintenanceSelect, "#78909C");

        // Highlight selected
        switch (type) {
            case "sos":
                btnTypeSOSSelect.setBackgroundResource(R.drawable.type_selected_bg);
                setTypeTextColor(btnTypeSOSSelect, "#B71C1C");
                break;
            case "announcement":
                btnTypeAnnouncementSelect.setBackgroundResource(R.drawable.type_selected_bg);
                setTypeTextColor(btnTypeAnnouncementSelect, "#B71C1C");
                break;
            case "maintenance":
                btnTypeMaintenanceSelect.setBackgroundResource(R.drawable.type_selected_bg);
                setTypeTextColor(btnTypeMaintenanceSelect, "#B71C1C");
                break;
        }
    }

    private void setTypeTextColor(LinearLayout layout, String colorHex) {
        if (layout.getChildCount() > 1 && layout.getChildAt(1) instanceof TextView) {
            ((TextView) layout.getChildAt(1)).setTextColor(Color.parseColor(colorHex));
        }
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private boolean validateInputs() {
        boolean isValid = true;
        tilAlertTitle.setError(null);
        tilAlertMessage.setError(null);

        String title   = getText(etAlertTitle);
        String message = getText(etAlertMessage);

        if (title.isEmpty())   { tilAlertTitle.setError("Title is required"); isValid = false; }
        if (message.isEmpty()) { tilAlertMessage.setError("Message is required"); isValid = false; }
        return isValid;
    }

    // ── Submit ─────────────────────────────────────────────────────────────────
    private void submitAlert() {
        String title   = getText(etAlertTitle);
        String message = getText(etAlertMessage);
        String flat    = getText(etAlertFlat);

        showLoading(true);

        AlertManager.postAlert(title, message, selectedType, flat,
            () -> {
                showLoading(false);
                showStatus("✓ Alert posted successfully!", false);
                clearFields();
            },
            err -> {
                showLoading(false);
                showStatus("✗ Failed to post: " + err, true);
            }
        );
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSubmitAlert.setEnabled(!show);
    }

    private void showStatus(String message, boolean isError) {
        tvPostAlertStatus.setText(message);
        tvPostAlertStatus.setTextColor(isError ? Color.parseColor("#B71C1C") : Color.parseColor("#1B5E20"));
        tvPostAlertStatus.setBackgroundColor(isError ? Color.parseColor("#FFEBEE") : Color.parseColor("#E8F5E9"));
        tvPostAlertStatus.setVisibility(View.VISIBLE);
    }

    private void clearFields() {
        if (etAlertTitle.getText()   != null) etAlertTitle.getText().clear();
        if (etAlertMessage.getText() != null) etAlertMessage.getText().clear();
        if (etAlertFlat.getText()    != null) etAlertFlat.getText().clear();
        setTypeSelection("sos");
    }

    @Override
    public void onBackPressed() { finish(); }
}
