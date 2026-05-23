package com.example.myapplication;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ResidentLockerActivity extends AppCompatActivity {

    private LinearLayout layoutLockerList;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;

    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_resident_locker);

        currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        bindViews();

        loadLockers();
    }

    // ====================================================
    // BIND VIEWS
    // ====================================================

    private void bindViews() {

        layoutLockerList =
                findViewById(R.id.layoutLockerList);

        layoutEmpty =
                findViewById(R.id.layoutLockerEmpty);

        progressBar =
                findViewById(R.id.progressBarLocker);
    }

    // ====================================================
    // LOAD LOCKERS
    // ====================================================

    private void loadLockers() {

        if (currentUser == null) {

            Toast.makeText(
                    this,
                    "User not logged in",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        showLoading(true);

        DeliveryManager.getResidentLockerDeliveries(

                currentUser.getUid(),

                deliveries -> {

                    showLoading(false);

                    renderLockers(deliveries);
                },

                err -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            err,
                            Toast.LENGTH_SHORT
                    ).show();
                }
        );
    }

    // ====================================================
    // RENDER LOCKERS
    // ====================================================

    @SuppressLint("SetTextI18n")
    private void renderLockers(
            List<DeliveryModel> deliveries
    ) {

        layoutLockerList.removeAllViews();

        if (deliveries == null || deliveries.isEmpty()) {

            layoutEmpty.setVisibility(View.VISIBLE);

            layoutLockerList.setVisibility(View.GONE);

            return;
        }

        layoutEmpty.setVisibility(View.GONE);

        layoutLockerList.setVisibility(View.VISIBLE);

        LayoutInflater inflater =
                LayoutInflater.from(this);

        for (DeliveryModel delivery : deliveries) {

            View card = inflater.inflate(
                    R.layout.item_locker_card,
                    layoutLockerList,
                    false
            );

            TextView tvCourier =
                    card.findViewById(R.id.tvLockerCourier);

            TextView tvLockerId =
                    card.findViewById(R.id.tvLockerId);

            TextView tvOtp =
                    card.findViewById(R.id.tvLockerOtp);

            TextView tvExpiry =
                    card.findViewById(R.id.tvLockerExpiry);

            TextView tvStatus =
                    card.findViewById(R.id.tvLockerStatus);

            MaterialButton btnPickup =
                    card.findViewById(R.id.btnPickupLocker);

            tvCourier.setText(
                    delivery.getCourierName()
            );

            tvLockerId.setText(
                    "Locker: " + delivery.getLockerId()
            );

            tvOtp.setText(
                    "OTP: " + delivery.getLockerOtp()
            );

            tvExpiry.setText(
                    "Expires: " +
                            formatTime(
                                    delivery.getLockerExpiresAt()
                            )
            );

            // ====================================
            // EXPIRED
            // ====================================

            if (delivery.isExpired()) {

                tvStatus.setText("Expired");

                tvStatus.setTextColor(Color.RED);

                btnPickup.setVisibility(View.GONE);
            }

            // ====================================
            // ACTIVE
            // ====================================

            else {

                tvStatus.setText("Active");

                tvStatus.setTextColor(
                        Color.parseColor("#2E7D32")
                );

                btnPickup.setVisibility(View.VISIBLE);

                btnPickup.setOnClickListener(v ->
                        markPickedUp(delivery)
                );
            }

            layoutLockerList.addView(card);
        }
    }

    // ====================================================
    // MARK PICKED UP
    // ====================================================

    private void markPickedUp(
            DeliveryModel delivery
    ) {

        DeliveryManager.confirmPickup(

                delivery.getId(),

                () -> {

                    Toast.makeText(
                            this,
                            "Package Picked Up",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadLockers();
                },

                err -> Toast.makeText(
                        this,
                        err,
                        Toast.LENGTH_SHORT
                ).show()
        );
    }

    // ====================================================
    // HELPERS
    // ====================================================

    private void showLoading(boolean show) {

        progressBar.setVisibility(
                show ? View.VISIBLE : View.GONE
        );
    }

    private String formatTime(long millis) {

        return new SimpleDateFormat(

                "dd MMM, hh:mm a",

                Locale.getDefault()

        ).format(new Date(millis));
    }
}