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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ResidentLockerActivity extends AppCompatActivity {

    private LinearLayout layoutLockerList;

    private LinearLayout layoutEmpty;

    private ProgressBar progressBar;

    private FirebaseUser currentUser;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_resident_locker);

        currentUser =
                FirebaseAuth.getInstance().getCurrentUser();

        db = FirebaseFirestore.getInstance();

        bindViews();

        loadLockers();
    }

    // =========================================
    // BIND VIEWS
    // =========================================

    private void bindViews() {

        layoutLockerList =
                findViewById(R.id.layoutLockerList);

        layoutEmpty =
                findViewById(R.id.layoutLockerEmpty);

        progressBar =
                findViewById(R.id.progressBarLocker);
    }

    // =========================================
    // LOAD LOCKERS
    // =========================================

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

        db.collection("lockers")

                .whereEqualTo(
                        "residentId",
                        currentUser.getUid()
                )

                .get()

                .addOnSuccessListener(snapshot -> {

                    showLoading(false);

                    layoutLockerList.removeAllViews();

                    if (snapshot.isEmpty()) {

                        layoutEmpty.setVisibility(View.VISIBLE);

                        layoutLockerList.setVisibility(View.GONE);

                        return;
                    }

                    layoutEmpty.setVisibility(View.GONE);

                    layoutLockerList.setVisibility(View.VISIBLE);

                    LayoutInflater inflater =
                            LayoutInflater.from(this);

                    for (QueryDocumentSnapshot doc : snapshot) {

                        LockerModel locker =
                                doc.toObject(
                                        LockerModel.class
                                );

                        locker.setLockerId(doc.getId());

                        renderLockerCard(
                                inflater,
                                locker
                        );
                    }
                })

                .addOnFailureListener(e -> {

                    showLoading(false);

                    Toast.makeText(
                            this,
                            e.getMessage(),
                            Toast.LENGTH_LONG
                    ).show();
                });
    }

    // =========================================
    // RENDER CARD
    // =========================================

    @SuppressLint("SetTextI18n")
    private void renderLockerCard(

            LayoutInflater inflater,

            LockerModel locker
    ) {

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

        // =====================================
        // DATA
        // =====================================

        tvCourier.setText(
                locker.getCourierName() != null
                        ? locker.getCourierName()
                        : "Package Ready"
        );

        tvLockerId.setText(
                "Locker: " + locker.getLockerId()
        );

        tvOtp.setText(
                "OTP: " + locker.getOtp()
        );

        tvExpiry.setText(
                "Expires: " +
                        formatTime(
                                locker.getExpiresAt()
                        )
        );

        // =====================================
        // EXPIRED
        // =====================================

        if (locker.isExpired()) {

            tvStatus.setText("Expired");

            tvStatus.setTextColor(Color.RED);

            btnPickup.setVisibility(View.GONE);

            autoReleaseLocker(locker);

        }

        // =====================================
        // ACTIVE
        // =====================================

        else {

            tvStatus.setText(
                    "Waiting for Pickup"
            );

            tvStatus.setTextColor(
                    Color.parseColor("#2E7D32")
            );

            btnPickup.setVisibility(View.VISIBLE);

            btnPickup.setOnClickListener(v ->
                    pickupLocker(locker)
            );
        }

        layoutLockerList.addView(card);
    }

    // =========================================
    // PICKUP
    // =========================================

    private void pickupLocker(
            LockerModel locker
    ) {

        db.collection("lockers")

                .document(locker.getLockerId())

                .update(
                        "status", "available",
                        "residentId", "",
                        "residentEmail", "",
                        "otp", "",
                        "deliveryId", ""
                )

                .addOnSuccessListener(unused -> {

                    Toast.makeText(
                            this,
                            "Package Picked Up",
                            Toast.LENGTH_SHORT
                    ).show();

                    loadLockers();
                })

                .addOnFailureListener(e ->

                        Toast.makeText(
                                this,
                                e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show()
                );
    }

    // =========================================
    // AUTO RELEASE
    // =========================================

    private void autoReleaseLocker(
            LockerModel locker
    ) {

        db.collection("lockers")

                .document(locker.getLockerId())

                .update(
                        "status", "available",
                        "residentId", "",
                        "residentEmail", "",
                        "otp", "",
                        "deliveryId", ""
                );
    }

    // =========================================
    // HELPERS
    // =========================================

    private void showLoading(boolean show) {

        progressBar.setVisibility(
                show ? View.VISIBLE : View.GONE
        );
    }

    private String formatTime(long millis) {

        return new SimpleDateFormat(

                "dd MMM yyyy, hh:mm a",

                Locale.getDefault()

        ).format(new Date(millis));
    }
}