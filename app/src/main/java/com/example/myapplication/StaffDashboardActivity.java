package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class StaffDashboardActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private StaffDashboardAdapter adapter;

    private TextView tvTotalStaff;
    private TextView tvInsideNow;
    private TextView tvWorkedToday;
    private TextView tvPresentToday;

    private List<StaffModel> staffList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_staff_dashboard);

        // Bind Views
        recyclerView = findViewById(R.id.recyclerViewStaff);

        tvTotalStaff = findViewById(R.id.tvTotalStaff);
        tvInsideNow = findViewById(R.id.tvInsideNow);
        tvWorkedToday = findViewById(R.id.tvWorkedToday);
        tvPresentToday = findViewById(R.id.tvPresentToday);

        // RecyclerView Setup
        recyclerView.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new StaffDashboardAdapter(staffList);

        recyclerView.setAdapter(adapter);

        // Load Data
        loadStaff();
    }

    private void loadStaff() {

        FirebaseFirestore.getInstance()
                .collection("staff")
                .whereEqualTo(
                        "residentId",
                        FirebaseAuth.getInstance().getUid()
                )
                .whereEqualTo("isActive", true)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    staffList.clear();

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {

                        StaffModel staff = new StaffModel(
                                doc.getId(),
                                doc.getString("name"),
                                doc.getString("role"),
                                Boolean.TRUE.equals(
                                        doc.getBoolean("isLoggedIn")
                                ),
                                0,
                                0,
                                0
                        );

                        // Load Latest Attendance
                        FirebaseFirestore.getInstance()
                                .collection("staff_logs")
                                .whereEqualTo("staffId", staff.id)
                                .orderBy(
                                        "loginTime",
                                        Query.Direction.DESCENDING
                                )
                                .limit(1)
                                .get()
                                .addOnSuccessListener(logSnapshot -> {

                                    if (!logSnapshot.isEmpty()) {

                                        var logDoc =
                                                logSnapshot.getDocuments().get(0);

                                        Long loginTime =
                                                logDoc.getLong("loginTime");

                                        Long logoutTime =
                                                logDoc.getLong("logoutTime");

                                        Long duration =
                                                logDoc.getLong("durationMinutes");

                                        staff.loginTime =
                                                loginTime != null ? loginTime : 0;

                                        staff.logoutTime =
                                                logoutTime != null ? logoutTime : 0;

                                        staff.durationMinutes =
                                                duration != null ? duration : 0;
                                    }

                                    staffList.add(staff);

                                    adapter.notifyDataSetChanged();

                                    updateAnalytics();
                                });
                    }
                });
    }

    private void updateAnalytics() {

        int insideCount = 0;
        int presentToday = 0;

        long totalWorked = 0;

        for (StaffModel staff : staffList) {

            // Inside count
            if (staff.isLoggedIn) {
                insideCount++;
            }

            // Present today
            if (staff.loginTime > 0) {
                presentToday++;
            }

            // Worked duration
            totalWorked += staff.durationMinutes;
        }

        tvTotalStaff.setText(
                String.valueOf(staffList.size())
        );

        tvInsideNow.setText(
                String.valueOf(insideCount)
        );

        tvPresentToday.setText(
                String.valueOf(presentToday)
        );

        tvWorkedToday.setText(
                totalWorked + " mins"
        );
    }
}