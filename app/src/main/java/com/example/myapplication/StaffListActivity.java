package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class StaffListActivity extends AppCompatActivity {

    private LinearLayout layoutStaffList;
    private TextView tvEmpty;
    private MaterialButton btnAddStaff;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_list);

        layoutStaffList = findViewById(R.id.layoutStaffList);
        tvEmpty = findViewById(R.id.tvEmptyStaff);
        btnAddStaff = findViewById(R.id.btnAddNewStaff);

        btnAddStaff.setOnClickListener(v ->
                startActivity(new Intent(this, AddStaffActivity.class))
        );

        loadStaff();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStaff();
    }

    private void loadStaff() {

        layoutStaffList.removeAllViews(); // 🔥 IMPORTANT FIX

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        FirebaseFirestore.getInstance()
                .collection("staff")
                .whereEqualTo("residentId", uid)
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    layoutStaffList.removeAllViews(); // 🔥 DOUBLE SAFETY CLEAR

                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvEmpty.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        String staffId = doc.getId();

                        String name = doc.getString("name");
                        String phone = doc.getString("phone");
                        String role = doc.getString("role");
                        String shiftStart = doc.getString("shiftStart");
                        String shiftEnd = doc.getString("shiftEnd");

                        // CARD
                        LinearLayout card = new LinearLayout(this);
                        card.setOrientation(LinearLayout.VERTICAL);
                        card.setPadding(40, 40, 40, 40);
                        card.setBackgroundResource(android.R.drawable.dialog_holo_light_frame);

                        LinearLayout.LayoutParams params =
                                new LinearLayout.LayoutParams(
                                        LinearLayout.LayoutParams.MATCH_PARENT,
                                        LinearLayout.LayoutParams.WRAP_CONTENT
                                );
                        params.setMargins(0, 0, 0, 30);
                        card.setLayoutParams(params);

                        // TEXT
                        TextView tvInfo = new TextView(this);
                        tvInfo.setText(
                                "👤 " + name + "\n" +
                                        "📞 " + phone + "\n" +
                                        "Role: " + role + "\n" +
                                        "⏰ " + shiftStart + " - " + shiftEnd
                        );
                        tvInfo.setTextSize(16);

                        // REMOVE BUTTON
                        MaterialButton btnRemove = new MaterialButton(this);
                        btnRemove.setText("Remove Staff");

                        btnRemove.setOnClickListener(v -> {

                            StaffManager.removeStaff(
                                    staffId,
                                    () -> {
                                        Toast.makeText(this, "Staff removed", Toast.LENGTH_SHORT).show();
                                        loadStaff(); // refresh
                                    },
                                    errorMsg ->
                                            Toast.makeText(this, errorMsg, Toast.LENGTH_SHORT).show()
                            );
                        });

                        card.addView(tvInfo);
                        card.addView(btnRemove);

                        layoutStaffList.addView(card);
                    }
                });
    }
}