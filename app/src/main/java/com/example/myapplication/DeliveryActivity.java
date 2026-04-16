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

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * DeliveryActivity.java
 *
 * Role   : Resident (+ Guard can view all)
 * Purpose: Lists all deliveries. Resident enters flat number to see their
 *          deliveries and tap "Confirm Pickup" on pending ones.
 *
 * Flow   : homeactivity → DeliveryActivity
 *          DeliveryLogActivity → DeliveryActivity (via btnViewDeliveries)
 */
public class DeliveryActivity extends AppCompatActivity {

    private LinearLayout      layoutDeliveryList, layoutDeliveryEmpty;
    private ProgressBar       progressBar;
    private TextInputEditText etFlatFilter;
    private MaterialButton    btnFilterFlat;
    private TextView          tabAllDeliveries, tabPendingDeliveries, tabPickedUp;

    private List<DeliveryModel> allDeliveries      = new ArrayList<>();
    private List<DeliveryModel> filteredDeliveries  = new ArrayList<>();
    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);
        bindViews();
        setTabListeners();
        loadDeliveries(null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDeliveries(currentFilter.equals("all") ? null : currentFilter);
    }

    private void bindViews() {
        layoutDeliveryList   = findViewById(R.id.layoutDeliveryList);
        layoutDeliveryEmpty  = findViewById(R.id.layoutDeliveryEmpty);
        progressBar          = findViewById(R.id.progressBarDelivery);
        etFlatFilter         = findViewById(R.id.etFlatFilter);
        btnFilterFlat        = findViewById(R.id.btnFilterFlat);
        tabAllDeliveries     = findViewById(R.id.tabAllDeliveries);
        tabPendingDeliveries = findViewById(R.id.tabPendingDeliveries);
        tabPickedUp          = findViewById(R.id.tabPickedUp);

        btnFilterFlat.setOnClickListener(v -> {
            String flat = etFlatFilter.getText() != null
                    ? etFlatFilter.getText().toString().trim() : "";
            if (!flat.isEmpty()) {
                loadDeliveriesByFlat(flat);
            } else {
                Toast.makeText(this, "Enter your flat number", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ── Tabs ───────────────────────────────────────────────────────────────────
    private void setTabListeners() {
        tabAllDeliveries.setOnClickListener(v -> {
            currentFilter = "all";
            setActiveTab(tabAllDeliveries);
            loadDeliveries(null);
        });

        tabPendingDeliveries.setOnClickListener(v -> {
            currentFilter = "pending";
            setActiveTab(tabPendingDeliveries);
            loadDeliveries("pending");
        });

        tabPickedUp.setOnClickListener(v -> {
            currentFilter = "pickedup";
            setActiveTab(tabPickedUp);
            loadDeliveries("pickedup");
        });
    }

    private void setActiveTab(TextView active) {
        for (TextView tab : new TextView[]{tabAllDeliveries, tabPendingDeliveries, tabPickedUp}) {
            tab.setTextColor(Color.parseColor("#E65100"));
            tab.setBackgroundResource(R.drawable.tab_delivery_unselected);
        }
        active.setTextColor(Color.WHITE);
        active.setBackgroundResource(R.drawable.tab_delivery_selected);
    }

    // ── Load Data ──────────────────────────────────────────────────────────────
    private void loadDeliveries(String statusFilter) {
        showLoading(true);
        DeliveryManager.getAllDeliveries(statusFilter,
            deliveries -> {
                allDeliveries.clear();
                allDeliveries.addAll(deliveries);
                filteredDeliveries.clear();
                filteredDeliveries.addAll(deliveries);
                showLoading(false);
                renderList(filteredDeliveries);
            },
            err -> {
                showLoading(false);
                Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
            }
        );
    }

    private void loadDeliveriesByFlat(String flat) {
        showLoading(true);
        DeliveryManager.getMyDeliveries(flat,
            deliveries -> {
                allDeliveries.clear();
                allDeliveries.addAll(deliveries);
                filteredDeliveries.clear();
                filteredDeliveries.addAll(deliveries);
                showLoading(false);
                renderList(filteredDeliveries);
            },
            err -> {
                showLoading(false);
                Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
            }
        );
    }

    // ── Render ─────────────────────────────────────────────────────────────────
    @SuppressLint("SetTextI18n")
    private void renderList(List<DeliveryModel> deliveries) {
        layoutDeliveryList.removeAllViews();

        if (deliveries.isEmpty()) {
            layoutDeliveryEmpty.setVisibility(View.VISIBLE);
            layoutDeliveryList.setVisibility(View.GONE);
            return;
        }

        layoutDeliveryEmpty.setVisibility(View.GONE);
        layoutDeliveryList.setVisibility(View.VISIBLE);

        LayoutInflater inflater = LayoutInflater.from(this);

        for (DeliveryModel delivery : deliveries) {
            View card = inflater.inflate(R.layout.item_delivery_card, layoutDeliveryList, false);

            TextView       tvCourierName    = card.findViewById(R.id.tvCourierName);
            TextView       tvCourierPhone   = card.findViewById(R.id.tvCourierPhone);
            TextView       tvDeliveryStatus = card.findViewById(R.id.tvDeliveryStatus);
            TextView       tvDeliveryFlat   = card.findViewById(R.id.tvDeliveryFlat);
            TextView       tvDeliveryTime   = card.findViewById(R.id.tvDeliveryTime);
            MaterialButton btnConfirmPickup = card.findViewById(R.id.btnConfirmPickup);

            tvCourierName.setText(delivery.getCourierName());
            tvCourierPhone.setText(delivery.getCourierPhone());
            tvDeliveryFlat.setText("Flat " + delivery.getFlatNumber());
            tvDeliveryTime.setText("Logged at " + formatTime(delivery.getLoggedAt()));

            if (delivery.isPickedUp()) {
                tvDeliveryStatus.setText("Picked Up ✓");
                tvDeliveryStatus.setTextColor(Color.parseColor("#2E7D32"));
                ((CardView) tvDeliveryStatus.getParent())
                        .setCardBackgroundColor(Color.parseColor("#E8F5E9"));
                btnConfirmPickup.setVisibility(View.GONE);
            } else {
                tvDeliveryStatus.setText("Pending");
                tvDeliveryStatus.setTextColor(Color.parseColor("#E65100"));
                ((CardView) tvDeliveryStatus.getParent())
                        .setCardBackgroundColor(Color.parseColor("#FFF3E0"));
                btnConfirmPickup.setVisibility(View.VISIBLE);
                btnConfirmPickup.setOnClickListener(v -> confirmPickup(delivery, btnConfirmPickup));
            }

            layoutDeliveryList.addView(card);
        }
    }

    // ── Confirm Pickup ─────────────────────────────────────────────────────────
    private void confirmPickup(DeliveryModel delivery, MaterialButton btn) {
        btn.setEnabled(false);
        btn.setText("Confirming...");

        DeliveryManager.confirmPickup(delivery.getId(),
            () -> {
                Toast.makeText(this, "✓ Pickup confirmed for Flat " + delivery.getFlatNumber(), Toast.LENGTH_SHORT).show();
                loadDeliveries(currentFilter.equals("all") ? null : currentFilter);
            },
            err -> {
                btn.setEnabled(true);
                btn.setText("Confirm Pickup");
                Toast.makeText(this, "Error: " + err, Toast.LENGTH_SHORT).show();
            }
        );
    }

    // ── Helpers ────────────────────────────────────────────────────────────────
    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        layoutDeliveryList.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private String formatTime(long millis) {
        return new SimpleDateFormat("hh:mm a, dd MMM", Locale.getDefault()).format(new Date(millis));
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(this, homeactivity.class));
        finish();
    }
}
