package com.example.myapplication;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
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

        loadAlerts();
    }

    private void loadAlerts() {

        layoutAlerts.removeAllViews();
        String uid =
                FirebaseAuth.getInstance()
                        .getCurrentUser()
                        .getUid();

        FirebaseFirestore.getInstance()
                .collection("alerts")
                .whereEqualTo("residentId", uid)
                .get()
                .addOnSuccessListener(querySnapshot -> {

                    if (querySnapshot.isEmpty()) {
                        tvEmpty.setVisibility(View.VISIBLE);
                        return;
                    }

                    for (QueryDocumentSnapshot doc : querySnapshot) {

                        String msg =
                                doc.getString("message");

                        TextView alertItem =
                                new TextView(this);

                        alertItem.setText("🔔 " + msg);
                        alertItem.setTextSize(18);
                        alertItem.setPadding(
                                30,30,30,30
                        );

                        layoutAlerts.addView(alertItem);
                    }
                });
    }
}