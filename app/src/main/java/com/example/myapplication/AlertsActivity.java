package com.example.myapplication;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

public class AlertsActivity extends AppCompatActivity {

    private LinearLayout layoutAlerts;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visitor_alert);

        layoutAlerts = findViewById(R.id.layoutAlerts);
        tvEmpty = findViewById(R.id.tvEmptyAlerts);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAlerts();
    }

    private void loadAlerts() {

        layoutAlerts.removeAllViews();

        // Check login
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            return;
        }

        String uid = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        FirebaseFirestore.getInstance()
                .collection("alerts")
                .whereEqualTo("residentId", uid)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .addSnapshotListener((querySnapshot, error) -> {

                    // Error
                    if (error != null) {

                        tvEmpty.setVisibility(View.VISIBLE);

                        tvEmpty.setText(
                                "Failed to load alerts"
                        );

                        return;
                    }

                    // Empty
                    if (querySnapshot == null ||
                            querySnapshot.isEmpty()) {

                        tvEmpty.setVisibility(View.VISIBLE);

                        tvEmpty.setText(
                                "No alerts available"
                        );

                        return;
                    }

                    // Data available
                    tvEmpty.setVisibility(View.GONE);

                    layoutAlerts.removeAllViews();

                    for (QueryDocumentSnapshot document : querySnapshot) {

                        String msg =
                                document.getString("message");

                        Boolean isRead =
                                document.getBoolean("read");

                        if (isRead == null) {
                            isRead = false;
                        }

                        TextView alertItem =
                                new TextView(AlertsActivity.this);

                        // Unread alert
                        if (!isRead) {

                            alertItem.setTypeface(
                                    null,
                                    Typeface.BOLD
                            );

                            alertItem.setText(
                                    "🔴 🔔 " + msg
                            );

                            // Mark read
                            document.getReference()
                                    .update("read", true);

                        } else {

                            alertItem.setText(
                                    "🔔 " + msg
                            );
                        }

                        alertItem.setTextSize(18);

                        alertItem.setPadding(
                                30,
                                30,
                                30,
                                30
                        );

                        layoutAlerts.addView(alertItem);
                    }
                });
    }
}