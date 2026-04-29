package com.example.myapplication;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
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

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * AlertsActivity.java
 *
 * Displays all active community alerts.
 * Filter by: All | SOS | Announcement | Maintenance
 * Quick SOS button at top for emergencies.
 * Tap "Post Alert" to go to PostAlertActivity.
 * Tap "Mark Resolved" on any card to close it.
 */
public class AlertsActivity extends AppCompatActivity {

    private LinearLayout layoutAlertList, layoutAlertsEmpty;
    private ProgressBar  progressBar;
    private TextView     tabAllAlerts, tabSOS, tabAnnouncement, tabMaintenance;
    private MaterialButton btnPostAlert, btnAlertsBackToHome;
    private LinearLayout btnSOSQuick;

    private List<AlertModel> allAlerts = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alerts);
        bindViews();
        setTabListeners();
        setClickListeners();
        loadAlerts(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlerts(currentFilter.equals("all") ? null : currentFilter);
    }

    private void bindViews() {
        layoutAlertList    = findViewById(R.id.layoutAlertList);
        layoutAlertsEmpty  = findViewById(R.id.layoutAlertsEmpty);
        progressBar        = findViewById(R.id.progressBarAlerts);
        tabAllAlerts       = findViewById(R.id.tabAllAlerts);
        tabSOS             = findViewById(R.id.tabSOS);
        tabAnnouncement    = findViewById(R.id.tabAnnouncement);
        tabMaintenance     = findViewById(R.id.tabMaintenance);
        btnPostAlert       = findViewById(R.id.btnPostAlert);
        btnAlertsBackToHome = findViewById(R.id.btnAlertsBackToHome);
        btnSOSQuick        = findViewById(R.id.btnSOSQuick);
    }

    private void setTabListeners() {
        tabAllAlerts.setOnClickListener(v -> {
            currentFilter = "all"; setActiveTab(tabAllAlerts); loadAlerts(null);
        });
        tabSOS.setOnClickListener(v -> {
            currentFilter = "sos"; setActiveTab(tabSOS); loadAlerts("sos");
        });
        tabAnnouncement.setOnClickListener(v -> {
            currentFilter = "announcement"; setActiveTab(tabAnnouncement); loadAlerts("announcement");
        });
        tabMaintenance.setOnClickListener(v -> {
            currentFilter = "maintenance"; setActiveTab(tabMaintenance); loadAlerts("maintenance");
        });
    }

    private void setActiveTab(TextView active) {
        for (TextView tab : new TextView[]{tabAllAlerts, tabSOS, tabAnnouncement, tabMaintenance}) {
            tab.setTextColor(Color.parseColor("#B71C1C"));
            tab.setBackgroundResource(R.drawable.tab_alert_unselected);
        }
        active.setTextColor(Color.WHITE);
        active.setBackgroundResource(R.drawable.tab_alert_selected);
    }

    private void setClickListeners() {
        btnPostAlert.setOnClickListener(v ->
            startActivity(new Intent(this, PostAlertActivity.class))
        );

        btnAlertsBackToHome.setOnClickListener(v -> navigateToHome());

        // Quick SOS — directly posts SOS alert with one tap
        btnSOSQuick.setOnClickListener(v -> showSOSConfirmDialog());
    }

    // ── Quick SOS Dialog ───────────────────────────────────────────────────────
    private void showSOSConfirmDialog() {
        new AlertDialog.Builder(this)
            .setTitle("🆘 Send Emergency SOS?")
            .setMessage("This will immediately notify security and all residents. Only use in a real emergency.")
            .setPositiveButton("Yes, Send SOS", (dialog, which) -> sendQuickSOS())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void sendQuickSOS() {
        AlertManager.postAlert(
            "EMERGENCY SOS",
            "Emergency alert triggered. Immediate assistance required.",
            "sos",
            "",
            () -> {
                Toast.makeText(this, "🆘 SOS sent! Security has been notified.", Toast.LENGTH_LONG).show();
                loadAlerts(null);
            },
            err -> Toast.makeText(this, "Failed to send SOS: " + err, Toast.LENGTH_SHORT).show()
        );
    }

    // ── Load Alerts ────────────────────────────────────────────────────────────
    private void loadAlerts(String typeFilter) {
        showLoading(true);

        if (typeFilter == null) {
            AlertManager.getActiveAlerts(
                alerts -> { showLoading(false); allAlerts = alerts; renderList(alerts); },
                err -> { showLoading(false); Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show(); }
            );
        } else {
            AlertManager.getAlertsByType(typeFilter,
                alerts -> { showLoading(false); allAlerts = alerts; renderList(alerts); },
                err -> { showLoading(false); Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show(); }
            );
        }
    }

    // ── Render ─────────────────────────────────────────────────────────────────
    @SuppressLint("SetTextI18n")
    private void renderList(List<AlertModel> alerts) {
        layoutAlertList.removeAllViews();

        if (alerts.isEmpty()) {
            layoutAlertsEmpty.setVisibility(View.VISIBLE);
            layoutAlertList.setVisibility(View.GONE);
            return;
        }

        layoutAlertsEmpty.setVisibility(View.GONE);
        layoutAlertList.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (AlertModel alert : alerts) {
            View card = inflater.inflate(R.layout.item_alert_card, layoutAlertList, false);

            TextView       tvIcon      = card.findViewById(R.id.tvAlertIcon);
            TextView       tvTitle     = card.findViewById(R.id.tvAlertTitle);
            TextView       tvPostedBy  = card.findViewById(R.id.tvAlertPostedBy);
            TextView       tvTime      = card.findViewById(R.id.tvAlertTime);
            TextView       tvMessage   = card.findViewById(R.id.tvAlertMessage);
            TextView       tvType      = card.findViewById(R.id.tvAlertType);
            MaterialButton btnResolve  = card.findViewById(R.id.btnResolveAlert);

            tvTitle.setText(alert.getTitle());
            tvMessage.setText(alert.getMessage());
            tvTime.setText(formatTime(alert.getTimestamp()));

            String postedBy = alert.getFlatNumber() != null && !alert.getFlatNumber().isEmpty()
                    ? alert.getPostedBy() + " · Flat " + alert.getFlatNumber()
                    : alert.getPostedBy();
            tvPostedBy.setText("Posted by " + postedBy);

            // Style by type
            switch (alert.getType()) {
                case "sos":
                    tvIcon.setText("🆘");
                    tvType.setText("SOS");
                    tvType.setTextColor(Color.parseColor("#B71C1C"));
                    ((CardView) tvType.getParent()).setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                    ((CardView) tvIcon.getParent().getParent()).setCardBackgroundColor(Color.parseColor("#FFEBEE"));
                    break;
                case "announcement":
                    tvIcon.setText("📢");
                    tvType.setText("Announcement");
                    tvType.setTextColor(Color.parseColor("#1565C0"));
                    ((CardView) tvType.getParent()).setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                    ((CardView) tvIcon.getParent().getParent()).setCardBackgroundColor(Color.parseColor("#E3F2FD"));
                    break;
                case "maintenance":
                    tvIcon.setText("🔧");
                    tvType.setText("Maintenance");
                    tvType.setTextColor(Color.parseColor("#E65100"));
                    ((CardView) tvType.getParent()).setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                    ((CardView) tvIcon.getParent().getParent()).setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                    break;
            }

            btnResolve.setOnClickListener(v -> resolveAlert(alert, card));
            layoutAlertList.addView(card);
        }
    }

    // ── Resolve Alert ──────────────────────────────────────────────────────────
    private void resolveAlert(AlertModel alert, View card) {
        new AlertDialog.Builder(this)
            .setTitle("Mark as Resolved?")
            .setMessage("This will close the alert: \"" + alert.getTitle() + "\"")
            .setPositiveButton("Yes, Resolve", (dialog, which) -> {
                AlertManager.resolveAlert(alert.getId(),
                    () -> {
                        Toast.makeText(this, "Alert resolved.", Toast.LENGTH_SHORT).show();
                        loadAlerts(currentFilter.equals("all") ? null : currentFilter);
                    },
                    err -> Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show()
                );
            })
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutAlertList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(new Date(millis));
    }

    private void navigateToHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() { navigateToHome(); }
}
