package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class StaffHistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerViewHistory;

    private TextView tvEmptyLogs;
    private TextView tvStaffName;
    private TextView tvStaffRole;

    private StaffHistoryAdapter adapter;

    private List<AttendanceLogModel> historyList =
            new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_staff_history);

        // Bind Views

        recyclerViewHistory =
                findViewById(R.id.recyclerViewHistory);

        tvEmptyLogs =
                findViewById(R.id.tvEmptyLogs);

        tvStaffName =
                findViewById(R.id.tvStaffName);

        tvStaffRole =
                findViewById(R.id.tvStaffRole);

        // RecyclerView Setup

        recyclerViewHistory.setLayoutManager(
                new LinearLayoutManager(this)
        );

        adapter = new StaffHistoryAdapter(historyList);

        recyclerViewHistory.setAdapter(adapter);

        // Get Staff ID

        String staffId =
                getIntent().getStringExtra("staffId");

        if (staffId == null) {
            finish();
            return;
        }

        // Load Staff Info

        loadStaffInfo(staffId);

        // Load Attendance Logs

        loadLogs(staffId);
    }

    private void loadStaffInfo(String staffId) {

        FirebaseFirestore.getInstance()
                .collection("staff")
                .document(staffId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {

                    if (!documentSnapshot.exists()) return;

                    tvStaffName.setText(
                            documentSnapshot.getString("name")
                    );

                    tvStaffRole.setText(
                            documentSnapshot.getString("role")
                    );
                });
    }

    private void loadLogs(String staffId) {

        StaffManager.getStaffLogs(

                staffId,

                logs -> {

                    historyList.clear();

                    if (logs.isEmpty()) {

                        tvEmptyLogs.setVisibility(View.VISIBLE);

                        adapter.notifyDataSetChanged();

                        return;
                    }

                    tvEmptyLogs.setVisibility(View.GONE);

                    for (Map<String, Object> log : logs) {

                        Long loginTime =
                                (Long) log.get("loginTime");

                        Long logoutTime =
                                (Long) log.get("logoutTime");

                        Long duration =
                                (Long) log.get("durationMinutes");

                        AttendanceLogModel model =
                                new AttendanceLogModel(
                                        loginTime != null ? loginTime : 0,
                                        logoutTime != null ? logoutTime : 0,
                                        duration != null ? duration : 0
                                );

                        historyList.add(model);
                    }

                    adapter.notifyDataSetChanged();
                },

                error -> {

                    tvEmptyLogs.setVisibility(View.VISIBLE);

                    tvEmptyLogs.setText(error);
                }
        );
    }
}