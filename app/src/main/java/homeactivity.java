import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.myapplication.R;

import AlertsActivity.AlertsActivity;
import DeliveryActivity.DeliveryActivity;
import ResidentsActivity.ResidentsActivity;
import StaffActivity.StaffActivity;
import VisitorActivity.VisitorActivity;

public class homeactivity extends AppCompatActivity {
    Button btnVisitorAuth, btnDelivery, btnStaff, btnAlerts, btnResidents, btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.homepage);

        btnVisitorAuth = findViewById(R.id.btnVisitorAuth);
        btnDelivery = findViewById(R.id.btnDelivery);
        btnStaff = findViewById(R.id.btnStaff);
        btnAlerts = findViewById(R.id.btnAlerts);
        btnResidents = findViewById(R.id.btnResidents);
        btnLogout = findViewById(R.id.btnLogout);

        btnVisitorAuth.setOnClickListener(v -> startActivity(new Intent(this, VisitorActivity.class)));
        btnDelivery.setOnClickListener(v -> startActivity(new Intent(this, DeliveryActivity.class)));
        btnStaff.setOnClickListener(v -> startActivity(new Intent(this, StaffActivity.class)));
        btnAlerts.setOnClickListener(v -> startActivity(new Intent(this, AlertsActivity.class)));
        btnResidents.setOnClickListener(v -> startActivity(new Intent(this, ResidentsActivity.class)));
        btnLogout.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });
    }
}