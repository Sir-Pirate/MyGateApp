package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * StaffActivity.java
 *
 * Shows all staff — filterable by Inside / All.
 * Tap a card to see details and option to force check-out (admin use).
 */
public class StaffActivity extends AppCompatActivity {

    private LinearLayout layoutStaffList, layoutStaffEmpty;
    private ProgressBar  progressBar;
    private TextView     tvStaffCount, tabInsideStaff, tabAllStaff;

    private List<StaffModel> allStaff = new ArrayList<>();
    private String currentFilter = "inside";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff);
        bindViews();
        setTabListeners();
        loadStaff(true); // load inside staff first
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadStaff(currentFilter.equals("inside"));
    }

    private void bindViews() {
        layoutStaffList  = findViewById(R.id.layoutStaffList);
        layoutStaffEmpty = findViewById(R.id.layoutStaffEmpty);
        progressBar      = findViewById(R.id.progressBarStaff);
        tvStaffCount     = findViewById(R.id.tvStaffCount);
        tabInsideStaff   = findViewById(R.id.tabInsideStaff);
        tabAllStaff      = findViewById(R.id.tabAllStaff);
    }

    private void setTabListeners() {
        tabInsideStaff.setOnClickListener(v -> {
            currentFilter = "inside";
            setActiveTab(tabInsideStaff);
            loadStaff(true);
        });

        tabAllStaff.setOnClickListener(v -> {
            currentFilter = "all";
            setActiveTab(tabAllStaff);
            loadStaff(false);
        });
    }

    private void setActiveTab(TextView active) {
        for (TextView tab : new TextView[]{tabInsideStaff, tabAllStaff}) {
            tab.setTextColor(Color.parseColor("#4A148C"));
            tab.setBackgroundResource(R.drawable.tab_staff_unselected);
        }
        active.setTextColor(Color.WHITE);
        active.setBackgroundResource(R.drawable.tab_staff_selected);
    }

    private void loadStaff(boolean insideOnly) {
        showLoading(true);

        if (insideOnly) {
            StaffManager.getStaffInside(
                staff -> { showLoading(false); allStaff = staff; renderList(staff); },
                err -> { showLoading(false); Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show(); }
            );
        } else {
            StaffManager.getAllStaff(
                staff -> { showLoading(false); allStaff = staff; renderList(staff); },
                err -> { showLoading(false); Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show(); }
            );
        }
    }

    @SuppressLint("SetTextI18n")
    private void renderList(List<StaffModel> staffList) {
        layoutStaffList.removeAllViews();

        tvStaffCount.setText(staffList.size() + " staff member" + (staffList.size() != 1 ? "s" : ""));

        if (staffList.isEmpty()) {
            layoutStaffEmpty.setVisibility(View.VISIBLE);
            layoutStaffList.setVisibility(View.GONE);
            return;
        }

        layoutStaffEmpty.setVisibility(View.GONE);
        layoutStaffList.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (StaffModel staff : staffList) {
            View card = inflater.inflate(R.layout.item_staff_card, layoutStaffList, false);

            TextView tvAvatar     = card.findViewById(R.id.tvStaffAvatar);
            TextView tvName       = card.findViewById(R.id.tvStaffItemName);
            TextView tvType       = card.findViewById(R.id.tvStaffItemType);
            TextView tvPhone      = card.findViewById(R.id.tvStaffItemPhone);
            TextView tvStatus     = card.findViewById(R.id.tvStaffItemStatus);
            TextView tvTime       = card.findViewById(R.id.tvStaffItemTime);

            tvAvatar.setText(staff.getName().isEmpty() ? "?"
                    : String.valueOf(staff.getName().charAt(0)).toUpperCase());
            tvName.setText(staff.getName());
            tvType.setText(staff.getStaffType());
            tvPhone.setText(staff.getPhone());

            if (staff.isInside()) {
                tvStatus.setText("● Inside");
                tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                ((CardView) tvStatus.getParent()).setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                tvTime.setText("Since " + formatTime(staff.getEntryTime()));
            } else {
                tvStatus.setText("Exited");
                tvStatus.setTextColor(Color.parseColor("#78909C"));
                ((CardView) tvStatus.getParent()).setCardBackgroundColor(Color.parseColor("#ECEFF1"));
                tvTime.setText("Left " + formatTime(staff.getExitTime()));
            }

            // Tap for detail + force checkout option
            card.setOnClickListener(v -> showStaffDetailDialog(staff));
            layoutStaffList.addView(card);
        }
    }

    private void showStaffDetailDialog(StaffModel staff) {
        String flat = (staff.getFlatNumber() != null && !staff.getFlatNumber().isEmpty())
                ? staff.getFlatNumber() : "—";

        String details =
                "Name: "      + staff.getName()      + "\n" +
                "Phone: "     + staff.getPhone()     + "\n" +
                "Role: "      + staff.getStaffType() + "\n" +
                "Flat: "      + flat                 + "\n" +
                "Status: "    + staff.getStatus()    + "\n" +
                "Entry: "     + formatTime(staff.getEntryTime());

        new android.app.AlertDialog.Builder(this)
            .setTitle("Staff Details")
            .setMessage(details)
            .setNegativeButton("Close", null)
            .setPositiveButton("Force Check-Out", (dialog, which) -> {
                if (staff.isInside()) {
                    StaffManager.checkOut(staff.getPhone(),
                        () -> {
                            Toast.makeText(this, "Staff checked out.", Toast.LENGTH_SHORT).show();
                            loadStaff(currentFilter.equals("inside"));
                        },
                        err -> Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show()
                    );
                } else {
                    Toast.makeText(this, "Staff is already outside.", Toast.LENGTH_SHORT).show();
                }
            })
            .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutStaffList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private String formatTime(long millis) {
        if (millis == 0) return "—";
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }
}
