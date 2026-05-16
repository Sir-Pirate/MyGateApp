package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class VisitorArrivalActivity extends AppCompatActivity {

    private TextInputLayout tilArrivalPhone;
    private TextInputEditText etArrivalPhone;

    private MaterialButton btnCheckApproval;
    private MaterialButton btnMarkArrival;
    private MaterialButton btnArrivalBackToHome;

    private CardView cardVisitorResult;

    private TextView tvFoundVisitorName;
    private TextView tvApprovalStatus;
    private TextView tvApprovedBy;
    private TextView tvArrivalStatus;

    private ProgressBar progressBar;

    private String foundVisitorId = null;

    // ✅ NEW: Prevent multiple clicks
    private boolean isMarkingArrival = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_visitor_arrival);

        bindViews();
        setClickListeners();

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        navigateToHome();
                    }
                }
        );
    }

    private void bindViews() {

        tilArrivalPhone = findViewById(R.id.tilArrivalPhone);
        etArrivalPhone = findViewById(R.id.etArrivalPhone);

        btnCheckApproval = findViewById(R.id.btnCheckApproval);
        btnMarkArrival = findViewById(R.id.btnMarkArrival);
        btnArrivalBackToHome = findViewById(R.id.btnArrivalBackToHome);

        cardVisitorResult = findViewById(R.id.cardVisitorResult);

        tvFoundVisitorName = findViewById(R.id.tvFoundVisitorName);
        tvApprovalStatus = findViewById(R.id.tvApprovalStatus);
        tvApprovedBy = findViewById(R.id.tvApprovedBy);

        tvArrivalStatus = findViewById(R.id.tvArrivalStatus);

        progressBar = findViewById(R.id.progressBarArrival);
    }

    private void setClickListeners() {

        btnCheckApproval.setOnClickListener(v -> {

            String phone =
                    etArrivalPhone.getText() != null
                            ? etArrivalPhone.getText().toString().trim()
                            : "";

            if (validatePhone(phone)) {
                lookupVisitor(phone);
            }
        });

        btnMarkArrival.setOnClickListener(v -> {

            // ✅ FIX: Prevent double clicks
            if (isMarkingArrival) return;

            if (foundVisitorId != null) {
                markArrived(foundVisitorId);
            } else {
                showStatus(
                        "No visitor selected. Search first.",
                        true
                );
            }
        });

        btnArrivalBackToHome.setOnClickListener(
                v -> navigateToHome()
        );
    }

    private boolean validatePhone(String phone) {

        tilArrivalPhone.setError(null);

        if (phone.isEmpty()) {

            tilArrivalPhone.setError(
                    "Phone number required"
            );
            return false;
        }

        if (phone.length() != 10 ||
                !phone.matches("[0-9]+")) {

            tilArrivalPhone.setError(
                    "Enter valid 10-digit number"
            );
            return false;
        }

        return true;
    }

    private void lookupVisitor(String phone) {

        showLoading(true);

        hideResultCard();
        hideStatus();

        VisitorManager.getVisitorByPhone(
                phone,

                visitor -> {

                    showLoading(false);

                    if ("revoked".equals(visitor.getStatus())) {

                        foundVisitorId = null;

                        showStatus(
                                "ALERT: Visitor approval revoked. Entry denied.",
                                true
                        );

                        hideResultCard();
                        return;
                    }

                    foundVisitorId = visitor.getId();

                    populateResultCard(
                            visitor.getName(),
                            visitor.getStatus(),

                            visitor.getResidentName() != null
                                    ? visitor.getResidentName()
                                    : "Unknown",

                            visitor.getFlatNo() != null
                                    ? visitor.getFlatNo()
                                    : "N/A",

                            visitor.getTower() != null
                                    ? visitor.getTower()
                                    : "N/A"
                    );
                },

                errorMsg -> {

                    showLoading(false);

                    showStatus(
                            "✗ " + errorMsg,
                            true
                    );
                }
        );
    }

    private void markArrived(String visitorId) {

        // ✅ LOCK
        isMarkingArrival = true;
        btnMarkArrival.setEnabled(false);

        showLoading(true);

        VisitorManager.markVisitorArrived(

                visitorId,

                () -> {

                    showLoading(false);

                    showStatus(
                            "✓ Visitor marked as arrived. Entry logged!",
                            false
                    );

                    // keep button disabled
                },

                errorMsg -> {

                    showLoading(false);

                    showStatus(
                            "✗ Could not log arrival: " + errorMsg,
                            true
                    );

                    // ✅ UNLOCK if failed
                    isMarkingArrival = false;
                    btnMarkArrival.setEnabled(true);
                }
        );
    }

    private void populateResultCard(
            String name,
            String status,
            String approvedBy,
            String flatNo,
            String tower) {

        tvFoundVisitorName.setText(name);

        tvApprovedBy.setText(
                approvedBy +
                        "\nFlat: " + flatNo +
                        " | Tower: " + tower
        );

        if ("arrived".equals(status)) {

            tvApprovalStatus.setText("✓ Already Arrived");
            tvApprovalStatus.setTextColor(Color.parseColor("#1565C0"));
            btnMarkArrival.setEnabled(false);

        }
        else if ("approved".equals(status)) {

            tvApprovalStatus.setText("✓ Pre-Approved");
            tvApprovalStatus.setTextColor(Color.parseColor("#1B5E20"));
            btnMarkArrival.setEnabled(true);

        }
        else if ("revoked".equals(status)) {

            tvApprovalStatus.setText("✗ Revoked");
            tvApprovalStatus.setTextColor(Color.parseColor("#B71C1C"));
            btnMarkArrival.setEnabled(false);

        }
        else {

            tvApprovalStatus.setText("✗ Not Approved");
            tvApprovalStatus.setTextColor(Color.parseColor("#B71C1C"));
            btnMarkArrival.setEnabled(false);
        }

        cardVisitorResult.setVisibility(View.VISIBLE);
    }

    private void hideResultCard() {
        cardVisitorResult.setVisibility(View.GONE);
        foundVisitorId = null;
    }

    private void hideStatus() {
        tvArrivalStatus.setVisibility(View.GONE);
    }

    private void showLoading(boolean show) {

        progressBar.setVisibility(
                show ? View.VISIBLE : View.GONE
        );

        btnCheckApproval.setEnabled(!show);
        btnArrivalBackToHome.setEnabled(!show);
    }

    private void showStatus(String message, boolean isError) {

        tvArrivalStatus.setText(message);

        tvArrivalStatus.setTextColor(
                isError
                        ? Color.parseColor("#B71C1C")
                        : Color.parseColor("#1B5E20")
        );

        tvArrivalStatus.setBackgroundColor(
                isError
                        ? Color.parseColor("#FFEBEE")
                        : Color.parseColor("#E8F5E9")
        );

        tvArrivalStatus.setVisibility(View.VISIBLE);
    }

    private void navigateToHome() {

        Intent intent =
                new Intent(this, HomeActivity.class);

        intent.setFlags(
                Intent.FLAG_ACTIVITY_CLEAR_TOP
                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
        );

        startActivity(intent);
        finish();
    }
}