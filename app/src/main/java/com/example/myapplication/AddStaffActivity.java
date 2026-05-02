package com.example.myapplication;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AddStaffActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etRole, etShiftStart, etShiftEnd;
    private MaterialButton btnAddStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_staff);

        bindViews();
        setClickListener();
    }

    private void bindViews() {
        etName = findViewById(R.id.etStaffName);
        etPhone = findViewById(R.id.etStaffPhone);
        etRole = findViewById(R.id.etStaffRole);
        etShiftStart = findViewById(R.id.etShiftStart);
        etShiftEnd = findViewById(R.id.etShiftEnd);

        btnAddStaff = findViewById(R.id.btnAddStaff);
    }

    private void setClickListener() {
        btnAddStaff.setOnClickListener(v -> {

            String name = getText(etName);
            String phone = getText(etPhone);
            String role = getText(etRole);
            String shiftStart = getText(etShiftStart);
            String shiftEnd = getText(etShiftEnd);

            if (!validate(name, phone, role, shiftStart, shiftEnd)) {
                return;
            }

            StaffManager.addStaff(
                    name,
                    phone,
                    role,
                    shiftStart,
                    shiftEnd,
                    () -> {
                        Toast.makeText(this, "Staff added successfully", Toast.LENGTH_SHORT).show();
                        clearFields();
                        finish();
                    },
                    error -> Toast.makeText(this, error, Toast.LENGTH_SHORT).show()
            );
        });
    }

    private String getText(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }

    private boolean validate(String name, String phone, String role, String start, String end) {

        if (TextUtils.isEmpty(name)) {
            etName.setError("Required");
            return false;
        }

        if (phone.length() != 10) {
            etPhone.setError("Enter valid 10-digit number");
            return false;
        }

        if (TextUtils.isEmpty(role)) {
            etRole.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(start)) {
            etShiftStart.setError("Required");
            return false;
        }

        if (TextUtils.isEmpty(end)) {
            etShiftEnd.setError("Required");
            return false;
        }

        return true;
    }

    private void clearFields() {
        etName.setText("");
        etPhone.setText("");
        etRole.setText("");
        etShiftStart.setText("");
        etShiftEnd.setText("");
    }
}