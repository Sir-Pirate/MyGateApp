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
 * Resident + Guard Delivery Screen
 */

public class DeliveryActivity extends AppCompatActivity {

    // Layouts
    private LinearLayout layoutDeliveryList, layoutDeliveryEmpty;

    // Views
    private ProgressBar progressBar;
    private TextInputEditText etFlatFilter;

    // Buttons
    private MaterialButton btnFilterFlat;
    private MaterialButton btnStoreLocker;
    private MaterialButton btnBackHome;

    // Tabs
    private TextView tabAllDeliveries;
    private TextView tabPendingDeliveries;
    private TextView tabPickedUp;

    // Data
    private final List<DeliveryModel> allDeliveries = new ArrayList<>();
    private final List<DeliveryModel> filteredDeliveries = new ArrayList<>();

    private String currentFilter = "all";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery);

        bindViews();
        setTabListeners();
        setButtonListeners();

        loadDeliveries(null);
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadDeliveries(
                currentFilter.equals("all")
                        ? null
                        : currentFilter
        );
    }

    // ─────────────────────────────────────────────────────────
    // Bind Views
    // ─────────────────────────────────────────────────────────

    private void bindViews() {

        layoutDeliveryList = findViewById(R.id.layoutDeliveryList);
        layoutDeliveryEmpty = findViewById(R.id.layoutDeliveryEmpty);

        progressBar = findViewById(R.id.progressBarDelivery);

        etFlatFilter = findViewById(R.id.etFlatFilter);

        btnFilterFlat = findViewById(R.id.btnFilterFlat);

        // NEW BUTTONS
        btnStoreLocker = findViewById(R.id.btnStoreLocker);
        btnBackHome = findViewById(R.id.btnBackHome);

        tabAllDeliveries = findViewById(R.id.tabAllDeliveries);
        tabPendingDeliveries = findViewById(R.id.tabPendingDeliveries);
        tabPickedUp = findViewById(R.id.tabPickedUp);
    }

    // ─────────────────────────────────────────────────────────
    // Button Listeners
    // ─────────────────────────────────────────────────────────

    private void setButtonListeners() {

        // Filter by Flat
        btnFilterFlat.setOnClickListener(v -> {

            String flat = etFlatFilter.getText() != null
                    ? etFlatFilter.getText().toString().trim()
                    : "";

            if (!flat.isEmpty()) {

                loadDeliveriesByFlat(flat);

            } else {

                Toast.makeText(
                        this,
                        "Enter your flat number",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        // Store in Locker
        btnStoreLocker.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DeliveryActivity.this,
                    LockerActivity.class
            );

            startActivity(intent);
        });

        // Back Home
        btnBackHome.setOnClickListener(v -> {

            Intent intent = new Intent(
                    DeliveryActivity.this,
                    HomeActivity.class
            );

            startActivity(intent);

            finish();
        });
    }

    // ─────────────────────────────────────────────────────────
    // Tabs
    // ─────────────────────────────────────────────────────────

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

        TextView[] tabs = {
                tabAllDeliveries,
                tabPendingDeliveries,
                tabPickedUp
        };

        for (TextView tab : tabs) {

            tab.setTextColor(Color.parseColor("#E65100"));
            tab.setBackgroundResource(R.drawable.tab_delivery_unselected);
        }

        active.setTextColor(Color.WHITE);
        active.setBackgroundResource(R.drawable.tab_delivery_selected);
    }

    // ─────────────────────────────────────────────────────────
    // Load Deliveries
    // ─────────────────────────────────────────────────────────

    private void loadDeliveries(String statusFilter) {

        showLoading(true);

        DeliveryManager.getAllDeliveries(

                statusFilter,

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

                    Toast.makeText(
                            this,
                            "Error: " + err,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    private void loadDeliveriesByFlat(String flat) {

        showLoading(true);

        DeliveryManager.getMyDeliveries(

                flat,

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

                    Toast.makeText(
                            this,
                            "Error: " + err,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    // ─────────────────────────────────────────────────────────
    // Render List
    // ─────────────────────────────────────────────────────────

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

            View card = inflater.inflate(
                    R.layout.item_delivery_card,
                    layoutDeliveryList,
                    false
            );

            TextView tvCourierName =
                    card.findViewById(R.id.tvCourierName);

            TextView tvCourierPhone =
                    card.findViewById(R.id.tvCourierPhone);

            TextView tvDeliveryStatus =
                    card.findViewById(R.id.tvDeliveryStatus);

            TextView tvDeliveryFlat =
                    card.findViewById(R.id.tvDeliveryFlat);

            TextView tvDeliveryTime =
                    card.findViewById(R.id.tvDeliveryTime);

            MaterialButton btnConfirmPickup =
                    card.findViewById(R.id.btnConfirmPickup);

            tvCourierName.setText(delivery.getCourierName());

            tvCourierPhone.setText(delivery.getCourierPhone());

            tvDeliveryFlat.setText(
                    "Flat " + delivery.getFlatNumber()
            );

            tvDeliveryTime.setText(
                    "Logged at " + formatTime(delivery.getLoggedAt())
            );

            // PICKED UP
            if (delivery.isPickedUp()) {

                tvDeliveryStatus.setText("Picked Up ✓");

                tvDeliveryStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                );

                ((CardView) tvDeliveryStatus.getParent())
                        .setCardBackgroundColor(
                                Color.parseColor("#E8F5E9")
                        );

                btnConfirmPickup.setVisibility(View.GONE);

            }

            // PENDING
            else {

                tvDeliveryStatus.setText("Pending");

                tvDeliveryStatus.setTextColor(
                        Color.parseColor("#E65100")
                );

                ((CardView) tvDeliveryStatus.getParent())
                        .setCardBackgroundColor(
                                Color.parseColor("#FFF3E0")
                        );

                btnConfirmPickup.setVisibility(View.VISIBLE);

                btnConfirmPickup.setOnClickListener(v ->
                        confirmPickup(delivery, btnConfirmPickup)
                );
            }

            layoutDeliveryList.addView(card);
        }
    }

    // ─────────────────────────────────────────────────────────
    // Confirm Pickup
    // ─────────────────────────────────────────────────────────

    private void confirmPickup(
            DeliveryModel delivery,
            MaterialButton btn
    ) {

        btn.setEnabled(false);

        btn.setText("Confirming...");

        DeliveryManager.confirmPickup(

                delivery.getId(),

                () -> {

                    Toast.makeText(
                            this,
                            "✓ Pickup confirmed for Flat "
                                    + delivery.getFlatNumber(),
                            Toast.LENGTH_SHORT
                    ).show();

                    loadDeliveries(
                            currentFilter.equals("all")
                                    ? null
                                    : currentFilter
                    );
                },

                err -> {

                    btn.setEnabled(true);

                    btn.setText("Confirm Pickup");

                    Toast.makeText(
                            this,
                            "Error: " + err,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    // ─────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────

    private void showLoading(boolean show) {

        progressBar.setVisibility(
                show ? View.VISIBLE : View.GONE
        );

        layoutDeliveryList.setVisibility(
                show ? View.GONE : View.VISIBLE
        );
    }

    private String formatTime(long millis) {

        return new SimpleDateFormat(
                "hh:mm a, dd MMM",
                Locale.getDefault()
        ).format(new Date(millis));
    }
}