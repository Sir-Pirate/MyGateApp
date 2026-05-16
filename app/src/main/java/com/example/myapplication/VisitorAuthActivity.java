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

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class VisitorAuthActivity extends AppCompatActivity {

    private LinearLayout layoutVisitorList, layoutEmpty;
    private ProgressBar progressBar;
    private TextInputEditText etSearch;
    private TextView tabAll, tabPending, tabArrived, tabRevoked;

    private List<VisitorModel> allVisitors = new ArrayList<>();
    private List<VisitorModel> filteredVisitors = new ArrayList<>();

    private String currentFilter = "all";
    private String userRole = null;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_visitor_auth);

        bindViews();

        userRole = getIntent().getStringExtra("role");

        setTabListeners();
        setSearchListener();
        loadVisitors(null);

        getOnBackPressedDispatcher().addCallback(
                this,
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {

                        Intent intent =
                                new Intent(
                                        VisitorAuthActivity.this,
                                        HomeActivity.class
                                );

                        intent.setFlags(
                                Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP
                        );

                        startActivity(intent);
                        finish();
                    }
                }
        );
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadVisitors(
                currentFilter.equals("all")
                        ? null
                        : currentFilter
        );
    }

    private void bindViews() {

        layoutVisitorList = findViewById(R.id.layoutVisitorList);
        layoutEmpty = findViewById(R.id.layoutEmpty);

        progressBar = findViewById(R.id.progressBar);

        etSearch = findViewById(R.id.etSearch);

        tabAll = findViewById(R.id.tabAll);
        tabPending = findViewById(R.id.tabPending);
        tabArrived = findViewById(R.id.tabArrived);
        tabRevoked = findViewById(R.id.tabRevoked);

    }

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

        tabRevoked.setOnClickListener(v -> {
            currentFilter = "revoked";
            setActiveTab(tabRevoked);
            loadVisitors("revoked");
        });
    }

    private void setActiveTab(TextView active) {

        for (TextView tab : new TextView[]{
                tabAll, tabPending, tabArrived, tabRevoked
        }) {
            tab.setTextColor(Color.parseColor("#1A237E"));
            tab.setBackgroundResource(
                    R.drawable.tab_unselected_bg
            );
        }

        active.setTextColor(Color.WHITE);
        active.setBackgroundResource(
                R.drawable.tab_selected_bg
        );
    }

    private void setSearchListener() {

        etSearch.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence s,
                            int start,
                            int count,
                            int after
                    ) {}

                    @Override
                    public void onTextChanged(
                            CharSequence s,
                            int start,
                            int before,
                            int count
                    ) {
                        filterBySearch(
                                s.toString().trim()
                        );
                    }

                    @Override
                    public void afterTextChanged(
                            Editable s
                    ) {}
                }
        );
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

        LayoutInflater inflater =
                LayoutInflater.from(this);

        for (VisitorModel visitor : visitors) {

            View card =
                    inflater.inflate(
                            R.layout.item_visitor_card,
                            layoutVisitorList,
                            false
                    );

            TextView tvAvatar =
                    card.findViewById(R.id.tvAvatar);

            TextView tvName =
                    card.findViewById(R.id.tvVisitorName);

            TextView tvPhone =
                    card.findViewById(R.id.tvVisitorPhone);

            TextView tvNote =
                    card.findViewById(R.id.tvVisitorNote);

            TextView tvStatus =
                    card.findViewById(R.id.tvStatus);

            TextView tvTime =
                    card.findViewById(R.id.tvTime);

            tvAvatar.setText(
                    visitor.getName().isEmpty()
                            ? "?"
                            : String.valueOf(
                            visitor.getName().charAt(0)
                    ).toUpperCase()
            );

            tvName.setText(visitor.getName());
            tvPhone.setText(visitor.getPhone());

            tvNote.setText(
                    visitor.getNote() != null &&
                            !visitor.getNote().isEmpty()
                            ? visitor.getNote()
                            : "No note"
            );

            if (visitor.isArrived()) {

                tvStatus.setText("Arrived");
                tvStatus.setTextColor(
                        Color.parseColor("#1565C0")
                );

                ((CardView) tvStatus.getParent())
                        .setCardBackgroundColor(
                                Color.parseColor(
                                        "#E3F2FD"
                                )
                        );

            } else if ("revoked".equals(visitor.getStatus())) {

                tvStatus.setText("Revoked");
                tvStatus.setTextColor(Color.RED);

            }else {

                tvStatus.setText("Approved");
                tvStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                );

                ((CardView) tvStatus.getParent())
                        .setCardBackgroundColor(
                                Color.parseColor(
                                        "#E8F5E9"
                                )
                        );
            }

            long ts =
                    visitor.isArrived()
                            ? visitor.getArrivedAt()
                            : visitor.getApprovedAt();

            tvTime.setText(
                    ts > 0
                            ? formatTime(ts)
                            : "—"
            );

            card.setOnClickListener(
                    v -> showVisitorDetailDialog(visitor)
            );

            layoutVisitorList.addView(card);
        }
    }

    private void showVisitorDetailDialog(
            VisitorModel visitor
    ) {

        String details =
                "Name: " + visitor.getName() + "\n" +
                        "Phone: " + visitor.getPhone() + "\n" +
                        "Status: " + visitor.getStatus() + "\n" +
                        "Approved by: " + visitor.getResidentName() + "\n" +
                        "Flat: " + visitor.getFlatNo() + "\n" +
                        "Tower: " + visitor.getTower() + "\n" +
                        "Note: " +
                        (visitor.getNote() != null
                                ? visitor.getNote()
                                : "—");

        android.app.AlertDialog.Builder builder =
                new android.app.AlertDialog.Builder(this)
                        .setTitle("Visitor Details")
                        .setMessage(details)
                        .setNegativeButton(
                                "Close",
                                null
                        );

        // ONLY resident sees revoke
        if ("resident".equals(userRole)) {

            builder.setPositiveButton(
                    "Revoke Approval",
                    (dialog, which) -> {

                        VisitorManager.revokeVisitor(
                                visitor.getId(),

                                () -> {

                                    Toast.makeText(
                                            this,
                                            "Visitor approval revoked",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    loadVisitors(
                                            currentFilter.equals("all")
                                                    ? null
                                                    : currentFilter
                                    );
                                },

                                err -> Toast.makeText(
                                        this,
                                        "Error: " + err,
                                        Toast.LENGTH_SHORT
                                ).show()
                        );
                    }
            );
        }

        builder.show();
    }

    private void showLoading(boolean show) {

        progressBar.setVisibility(
                show ? View.VISIBLE : View.GONE
        );

        layoutVisitorList.setVisibility(
                show ? View.GONE : View.VISIBLE
        );
    }

    private String formatTime(long millis) {

        return new SimpleDateFormat(
                "hh:mm a",
                Locale.getDefault()
        ).format(
                new Date(millis)
        );
    }


    private void loadVisitors(String statusFilter) {

        showLoading(true);

        // RESIDENT → only their visitors
        if ("resident".equals(userRole)) {

            VisitorManager.getMyVisitors(

                    visitors -> {

                        allVisitors.clear();

                        // Apply status filter locally
                        for (VisitorModel v : visitors) {

                            if (statusFilter == null ||
                                    statusFilter.equals(v.getStatus())) {

                                allVisitors.add(v);
                            }
                        }

                        filteredVisitors.clear();
                        filteredVisitors.addAll(allVisitors);

                        showLoading(false);
                        renderList(filteredVisitors);
                    },

                    errorMsg -> {

                        showLoading(false);

                        Toast.makeText(
                                this,
                                "Error: " + errorMsg,
                                Toast.LENGTH_SHORT
                        ).show();
                    }
            );

            return;
        }


        // GUARD → all visitors
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

                    Toast.makeText(
                            this,
                            "Error: " + errorMsg,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

}