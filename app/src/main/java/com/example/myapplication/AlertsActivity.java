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

    // ✅ FIXED: onResume must be OUTSIDE onCreate
    @Override
    protected void onResume() {
        super.onResume();
        loadAlerts(); // reload every time screen opens
    }

    private void loadAlerts() {

        layoutAlerts.removeAllViews();

        // Safety check
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
                .get(com.google.firebase.firestore.Source.SERVER)
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    tvEmpty.setVisibility(View.GONE);

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        String msg = doc.getString("message");

                        // Handle read/unread (supports old + new data)
                        Boolean isRead = doc.getBoolean("read");
                        if (isRead == null) {
                            isRead = doc.getBoolean("isRead");
                        }
                        if (isRead == null) isRead = false;

                        TextView alertItem = new TextView(this);

                        if (!isRead) {
                            // Show unread UI
                            alertItem.setTypeface(null, Typeface.BOLD);
                            alertItem.setText("🔴 🔔 " + msg);

                            // Mark as read (ONLY once here)
                            doc.getReference().update("read", true);

                        } else {
                            alertItem.setText("🔔 " + msg);
                        }

                        alertItem.setTextSize(18);
                        alertItem.setPadding(30, 30, 30, 30);

                        layoutAlerts.addView(alertItem);
                    }
                })
                .addOnFailureListener(e -> {
                    tvEmpty.setVisibility(View.VISIBLE);
                    tvEmpty.setText("Failed to load alerts: " + e.getMessage());
                });
    }
}