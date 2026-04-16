package com.example.myapplication;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * VisitorAuthActivity.java
 *
 * Shows all visitors (approved + arrived) in a searchable, filterable list.
 * Pulls data from Firestore via VisitorManager.
 * Tap any visitor card to see details and option to revoke.
 */
public class VisitorAuthActivity extends AppCompatActivity {

    // ── UI ─────────────────────────────────────────────────────────────────────
    private LinearLayout layoutVisitorList, layoutEmpty;
    private ProgressBar progressBar;
    private TextInputEditText etSearch;
    private TextView tabAll, tabPending, tabArrived;

    // ── Data ───────────────────────────────────────────────────────────────────
    private List<VisitorModel> allVisitors    = new ArrayList<>();
    private List<VisitorModel> filteredVisitors = new ArrayList<>();
    private String currentFilter = "all"; // "all" | "approved" | "arrived"

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_auth);

        bindViews();
        setTabListeners();
        setSearchListener();
        loadVisitors(null); // load all on start
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list when returning from approve/arrival screens
        loadVisitors(currentFilter.equals("all") ? null : currentFilter);
    }

    // ── View Binding ───────────────────────────────────────────────────────────
    private void bindViews() {
        layoutVisitorList = findViewById(R.id.layoutVisitorList);
        layoutEmpty       = findViewById(R.id.layoutEmpty);
        progressBar       = findViewById(R.id.progressBar);
        etSearch          = findViewById(R.id.etSearch);
        tabAll            = findViewById(R.id.tabAll);
        tabPending        = findViewById(R.id.tabPending);
        tabArrived        = findViewById(R.id.tabArrived);
    }

    // ── Tab Listeners ──────────────────────────────────────────────────────────
    private void setTabListeners() {
        tabAll.setOnClickListener(v -> {
            currentFilter = "all";
            setActiveTab(tabAll);
            loadVisitors(null);
        });

        tabPending.setOnClickListener(v -> {
            currentFilter = "approved";
            setActiveTab(tabPending);
            loadVisitors("approved");
        });

        tabArrived.setOnClickListener(v -> {
            currentFilter = "arrived";
            setActiveTab(tabArrived);
            loadVisitors("arrived");
        });
    }

    private void setActiveTab(TextView active) {
        // Reset all
        for (TextView tab : new TextView[]{tabAll, tabPending, tabArrived}) {
            tab.setTextColor(Color.parseColor("#1A237E"));
            tab.setBackgroundResource(R.drawable.tab_unselected_bg);
        }
        // Highlight active
        active.setTextColor(Color.WHITE);
        active.setBackgroundResource(R.drawable.tab_selected_bg);
    }

    // ── Search Listener ────────────────────────────────────────────────────────
    private void setSearchListener() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterBySearch(s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterBySearch(String query) {
        filteredVisitors.clear();
        if (query.isEmpty()) {
            filteredVisitors.addAll(allVisitors);
        } else {
            String lower = query.toLowerCase();
            for (VisitorModel v : allVisitors) {
                if (v.getName().toLowerCase().contains(lower)
                        || v.getPhone().contains(lower)) {
                    filteredVisitors.add(v);
                }
            }
        }
        renderList(filteredVisitors);
    }

    // ── Load from Firestore ────────────────────────────────────────────────────
    private void loadVisitors(String statusFilter) {
        showLoading(true);

        VisitorManager.getAllVisitors(
            statusFilter,
            visitors -> {
                allVisitors.clear();
                allVisitors.addAll(visitors);
                filteredVisitors.clear();
                filteredVisitors.addAll(visitors);
                showLoading(false);
                renderList(filteredVisitors);
            },
            errorMsg -> {
                showLoading(false);
                Toast.makeText(this, "Error: " + errorMsg, Toast.LENGTH_SHORT).show();
            }
        );
    }

    // ── Render List ────────────────────────────────────────────────────────────
    @SuppressLint("SetTextI18n")
    private void renderList(List<VisitorModel> visitors) {
        layoutVisitorList.removeAllViews();

        if (visitors.isEmpty()) {
            layoutEmpty.setVisibility(View.VISIBLE);
            layoutVisitorList.setVisibility(View.GONE);
            return;
        }

        layoutEmpty.setVisibility(View.GONE);
        layoutVisitorList.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (VisitorModel visitor : visitors) {
            View card = inflater.inflate(R.layout.item_visitor_card, layoutVisitorList, false);

            TextView tvAvatar      = card.findViewById(R.id.tvAvatar);
            TextView tvName        = card.findViewById(R.id.tvVisitorName);
            TextView tvPhone       = card.findViewById(R.id.tvVisitorPhone);
            TextView tvNote        = card.findViewById(R.id.tvVisitorNote);
            TextView tvStatus      = card.findViewById(R.id.tvStatus);
            TextView tvTime        = card.findViewById(R.id.tvTime);

            // Avatar — first letter of name
            tvAvatar.setText(visitor.getName().isEmpty() ? "?" :
                    String.valueOf(visitor.getName().charAt(0)).toUpperCase());

            tvName.setText(visitor.getName());
            tvPhone.setText(visitor.getPhone());
            tvNote.setText(visitor.getNote() != null && !visitor.getNote().isEmpty()
                    ? visitor.getNote() : "No note");

            // Status badge color
            if (visitor.isArrived()) {
                tvStatus.setText("Arrived");
                tvStatus.setTextColor(Color.parseColor("#1565C0"));
                ((CardView) tvStatus.getParent()).setCardBackgroundColor(Color.parseColor("#E3F2FD"));
            } else {
                tvStatus.setText("Approved");
                tvStatus.setTextColor(Color.parseColor("#2E7D32"));
                ((CardView) tvStatus.getParent()).setCardBackgroundColor(Color.parseColor("#E8F5E9"));
            }

            // Time
            long ts = visitor.isArrived() ? visitor.getArrivedAt() : visitor.getApprovedAt();
            tvTime.setText(ts > 0 ? formatTime(ts) : "—");

            // Click → detail / revoke dialog
            card.setOnClickListener(v -> showVisitorDetailDialog(visitor));

            layoutVisitorList.addView(card);
        }
    }

    // ── Visitor Detail Dialog ──────────────────────────────────────────────────
    private void showVisitorDetailDialog(VisitorModel visitor) {
        String details =
                "Name: "        + visitor.getName()         + "\n" +
                "Phone: "       + visitor.getPhone()        + "\n" +
                "Status: "      + visitor.getStatus()       + "\n" +
                "Approved by: " + visitor.getResidentName() + "\n" +
                "Note: "        + (visitor.getNote() != null ? visitor.getNote() : "—");

        new android.app.AlertDialog.Builder(this)
            .setTitle("Visitor Details")
            .setMessage(details)
            .setNegativeButton("Close", null)
            .setPositiveButton("Revoke Approval", (dialog, which) -> {
                VisitorManager.revokeVisitor(
                    visitor.getId(),
                    () -> {
                        Toast.makeText(this, "Visitor approval revoked.", Toast.LENGTH_SHORT).show();
                        loadVisitors(currentFilter.equals("all") ? null : currentFilter);
                    },
                    err -> Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show()
                );
            })
            .show();
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutVisitorList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, homeactivity.class));
        finish();
    }
}
