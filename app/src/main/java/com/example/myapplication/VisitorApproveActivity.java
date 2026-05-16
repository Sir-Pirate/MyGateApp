package com.example.myapplication;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

public class VisitorApproveActivity extends AppCompatActivity {

    private TextInputLayout tilVisitorName, tilVisitorPhone, tilVisitorNote;
    private TextInputEditText etVisitorName, etVisitorPhone, etVisitorNote;

    private MaterialButton btnApproveVisitor, btnBackToHome;

    private TextView tvApproveStatus;
    private ProgressBar progressBar;

    private String userRole = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_approve);

        userRole = getIntent().getStringExtra("role");

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

        tilVisitorName = findViewById(R.id.tilVisitorName);
        etVisitorName = findViewById(R.id.etVisitorName);

        tilVisitorPhone = findViewById(R.id.tilVisitorPhone);
        etVisitorPhone = findViewById(R.id.etVisitorPhone);

        tilVisitorNote = findViewById(R.id.tilVisitorNote);
        etVisitorNote = findViewById(R.id.etVisitorNote);

        btnApproveVisitor = findViewById(R.id.btnApproveVisitor);
        btnBackToHome = findViewById(R.id.btnBackToHome);

        tvApproveStatus = findViewById(R.id.tvApproveStatus);

        progressBar = findViewById(R.id.progressBarApprove);
    }

    private void setClickListeners() {

        btnApproveVisitor.setOnClickListener(v -> {

            if (userRole == null || !userRole.equals("resident")) {
                showStatus("Only residents can approve visitors", true);
                return;
            }

            if (validateInputs()) {
                submitApproval();
            }
        });

        btnBackToHome.setOnClickListener(
                v -> navigateToHome()
        );
    }

    private boolean validateInputs() {

        boolean isValid = true;

        String name = etVisitorName.getText() != null
                ? etVisitorName.getText().toString().trim()
                : "";

        String phone = etVisitorPhone.getText() != null
                ? etVisitorPhone.getText().toString().trim()
                : "";

        tilVisitorName.setError(null);
        tilVisitorPhone.setError(null);

        if (name.isEmpty()) {
            tilVisitorName.setError("Visitor name is required");
            isValid = false;
        }

        if (phone.isEmpty()) {
            tilVisitorPhone.setError("Phone number is required");
            isValid = false;
        }
        else if (phone.length() != 10 ||
                !phone.matches("[0-9]+")) {

            tilVisitorPhone.setError(
                    "Enter valid 10-digit phone"
            );

            isValid = false;
        }

        return isValid;
    }

    private void submitApproval() {

        String name =
                etVisitorName.getText().toString().trim();

        String phone =
                etVisitorPhone.getText().toString().trim();

        String note =
                etVisitorNote.getText() != null
                        ? etVisitorNote.getText().toString().trim()
                        : "";

        showLoading(true);

        VisitorManager.approveVisitor(
                name,
                phone,
                note,

                () -> {
                    showLoading(false);

                    showStatus(
                            "✓ Visitor pre-approved successfully!",
                            false
                    );

                    clearFields();
                },

                errorMsg -> {
                    showLoading(false);

                    showStatus(
                            "✗ Failed: " + errorMsg,
                            true
                    );
                }
        );
    }

    private void showLoading(boolean show) {

        progressBar.setVisibility(
                show ? View.VISIBLE : View.GONE
        );

        btnApproveVisitor.setEnabled(!show);
        btnBackToHome.setEnabled(!show);
    }

    private void showStatus(
            String message,
            boolean isError) {

        tvApproveStatus.setText(message);

        tvApproveStatus.setTextColor(
                isError
                        ? Color.parseColor("#B71C1C")
                        : Color.parseColor("#1B5E20")
        );

        tvApproveStatus.setBackgroundColor(
                isError
                        ? Color.parseColor("#FFEBEE")
                        : Color.parseColor("#E8F5E9")
        );

        tvApproveStatus.setVisibility(View.VISIBLE);
    }

    private void clearFields() {

        if (etVisitorName.getText()!=null)
            etVisitorName.getText().clear();

        if (etVisitorPhone.getText()!=null)
            etVisitorPhone.getText().clear();

        if (etVisitorNote.getText()!=null)
            etVisitorNote.getText().clear();
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