package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Map;

public class StaffEntryActivity extends AppCompatActivity {

    private TextInputEditText etStaffPhone;
    private MaterialButton btnSearchStaff, btnLoginStaff, btnLogoutStaff;

    private androidx.cardview.widget.CardView cardStaffResult;
    private TextView tvStaffName, tvStaffDetails, tvStaffStatus, tvStatus;

    private String foundStaffId = null;
    private boolean isLoggedIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_entry);

        bindViews();
        setClickListeners();
    }

    private void bindViews() {
        etStaffPhone = findViewById(R.id.etStaffPhone);
        btnSearchStaff = findViewById(R.id.btnSearchStaff);
        btnLoginStaff = findViewById(R.id.btnLoginStaff);
        btnLogoutStaff = findViewById(R.id.btnLogoutStaff);

        cardStaffResult = findViewById(R.id.cardStaffResult);
        tvStaffName = findViewById(R.id.tvStaffName);
        tvStaffDetails = findViewById(R.id.tvStaffDetails);
        tvStaffStatus = findViewById(R.id.tvStaffStatus);
        tvStatus = findViewById(R.id.tvStatus);
    }

    private void setClickListeners() {

        btnSearchStaff.setOnClickListener(v -> {
            String phone = etStaffPhone.getText() != null
                    ? etStaffPhone.getText().toString().trim()
                    : "";

            if (phone.length() != 10) {
                showStatus("Enter valid 10-digit phone", true);
                return;
            }

            searchStaff(phone);
        });

        btnLoginStaff.setOnClickListener(v -> {
            if (foundStaffId != null && !isLoggedIn) {
                markLogin(foundStaffId);
            }
        });

        btnLogoutStaff.setOnClickListener(v -> {
            if (foundStaffId != null && isLoggedIn) {
                markLogout(foundStaffId);
            }
        });
    }

    private void searchStaff(String phone) {

        StaffManager.getStaffByPhone(
                phone,

                (staff, docId) -> {

                    foundStaffId = docId;

                    tvStaffName.setText((String) staff.get("name"));

                    tvStaffDetails.setText(
                            staff.get("role") +
                                    "\nFlat: " + staff.get("flatNo") +
                                    " | Tower: " + staff.get("tower")
                    );

                    Boolean logged = (Boolean) staff.get("isLoggedIn");
                    isLoggedIn = logged != null && logged;

                    if (isLoggedIn) {
                        tvStaffStatus.setText("Currently Logged In");
                        btnLoginStaff.setEnabled(false);
                        btnLogoutStaff.setEnabled(true);
                    } else {
                        tvStaffStatus.setText("Not Logged In");
                        btnLoginStaff.setEnabled(true);
                        btnLogoutStaff.setEnabled(false);
                    }

                    cardStaffResult.setVisibility(View.VISIBLE);
                    showStatus("", false);
                },

                error -> showStatus(error, true)
        );
    }

    private void markLogin(String staffId) {

        StaffManager.markLogin(
                staffId,

                () -> {
                    showStatus("✓ Login marked", false);
                    btnLoginStaff.setEnabled(false);
                    btnLogoutStaff.setEnabled(true);
                    tvStaffStatus.setText("Currently Logged In");
                    isLoggedIn = true;
                },

                error -> showStatus(error, true)
        );
    }

    private void markLogout(String staffId) {

        StaffManager.markLogout(
                staffId,

                () -> {
                    showStatus("✓ Logout marked", false);
                    btnLoginStaff.setEnabled(true);
                    btnLogoutStaff.setEnabled(false);
                    tvStaffStatus.setText("Logged Out");
                    isLoggedIn = false;
                },

                error -> showStatus(error, true)
        );
    }

    private void showStatus(String msg, boolean isError) {
        tvStatus.setVisibility(View.VISIBLE);
        tvStatus.setText(msg);

        if (isError) {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        } else {
            tvStatus.setTextColor(getResources().getColor(android.R.color.holo_green_dark));
        }
    }
}