package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;

public class StaffMenuActivity extends AppCompatActivity {

    private MaterialButton btnAddStaff, btnViewStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_menu);

        btnAddStaff = findViewById(R.id.btnAddStaff);
        btnViewStaff = findViewById(R.id.btnViewStaff);

        btnAddStaff.setOnClickListener(v ->
                startActivity(new Intent(this, AddStaffActivity.class))
        );

        btnViewStaff.setOnClickListener(v ->
                startActivity(new Intent(this, StaffListActivity.class))
        );
    }
}