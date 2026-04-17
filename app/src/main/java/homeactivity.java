import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;
import com.example.myapplication.VisitorApproveActivity;
import com.example.myapplication.VisitorArrivalActivity;
import com.example.myapplication.MainActivity;
import com.google.android.material.button.MaterialButton;

// Commented out until team members merge their branches
// import AlertsActivity.AlertsActivity;
// import DeliveryActivity.DeliveryActivity;
// import ResidentsActivity.ResidentsActivity;
// import StaffActivity.StaffActivity;
// import VisitorActivity.VisitorActivity;

public class homeactivity extends AppCompatActivity {

    Button btnVisitorAuth, btnDelivery, btnStaff, btnAlerts, btnResidents, btnLogout;
    MaterialButton btnGoApprove, btnGoArrival;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        btnVisitorAuth = findViewById(R.id.btnVisitorAuth);
        btnDelivery    = findViewById(R.id.btnDelivery);
        btnStaff       = findViewById(R.id.btnStaff);
        btnAlerts      = findViewById(R.id.btnAlerts);
        btnResidents   = findViewById(R.id.btnResidents);
        btnLogout      = findViewById(R.id.btnLogout);
        btnGoApprove   = findViewById(R.id.btnGoToVisitorApprove);
        btnGoArrival   = findViewById(R.id.btnGoToVisitorArrival);

        // ── Commented out until team merges their branches ────────────────────
        // btnVisitorAuth.setOnClickListener(v -> startActivity(new Intent(this, VisitorActivity.class)));
        // btnDelivery.setOnClickListener(v -> startActivity(new Intent(this, DeliveryActivity.class)));
        // btnStaff.setOnClickListener(v -> startActivity(new Intent(this, StaffActivity.class)));
        // btnAlerts.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        // btnResidents.setOnClickListener(v -> startActivity(new Intent(this, ResidentsActivity.class)));
        // ─────────────────────────────────────────────────────────────────────

        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, MainActivity.class)); // fixed: was LoginActivity
            finish();
        });

        // Navigate to Visitor Approve screen
        if (btnGoApprove != null) {
            btnGoApprove.setOnClickListener(v ->
                    startActivity(new Intent(this, VisitorApproveActivity.class))
            );
        }

        // Navigate to Visitor Arrival screen
        if (btnGoArrival != null) {
            btnGoArrival.setOnClickListener(v ->
                    startActivity(new Intent(this, VisitorArrivalActivity.class))
            );
        }
    }
}