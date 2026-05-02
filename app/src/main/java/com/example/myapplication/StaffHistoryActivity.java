package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StaffHistoryActivity extends AppCompatActivity {

    private LinearLayout layoutLogs;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_staff_history);

        layoutLogs = findViewById(R.id.layoutLogs);
        tvEmpty = findViewById(R.id.tvEmptyLogs);

        String staffId = getIntent().getStringExtra("staffId");

        if (staffId == null) {
            finish();
            return;
        }

        loadLogs(staffId);
    }

    private void loadLogs(String staffId) {

        StaffManager.getStaffLogs(
                staffId,

                logs -> {

                    if (logs.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvEmpty.setVisibility(View.GONE);

                    for (Map<String, Object> log : logs) {

                        Long loginTime = (Long) log.get("loginTime");
                        Long logoutTime = (Long) log.get("logoutTime");
                        Long duration = (Long) log.get("durationMinutes");
                        String status = (String) log.get("status");

                        String loginText = formatTime(loginTime);
                        String logoutText = logoutTime != null && logoutTime > 0
                                ? formatTime(logoutTime)
                                : "Still inside";

                        TextView tv = new TextView(this);

                        tv.setText(
                                "Login: " + loginText +
                                        "\nLogout: " + logoutText +
                                        "\nDuration: " + (duration != null ? duration : 0) + " mins" +
                                        "\nStatus: " + (status != null ? status : "ongoing")
                        );

                        tv.setPadding(30, 30, 30, 30);

                        layoutLogs.addView(tv);
                    }
                },

                error -> {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText(error);
                }
        );
    }

    private String formatTime(Long time) {
        if (time == null) return "-";
        return new SimpleDateFormat("hh:mm a", Locale.getDefault())
                .format(new Date(time));
    }
}