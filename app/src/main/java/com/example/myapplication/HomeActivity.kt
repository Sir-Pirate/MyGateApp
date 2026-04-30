package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var tvWelcome: TextView
    private lateinit var tvDateTime: TextView
    private lateinit var tvStatVisitors: TextView
    private lateinit var tvStatDeliveries: TextView
    private lateinit var tvStatAlerts: TextView

    private lateinit var btnVisitorApprove: View
    private lateinit var btnVisitorArrival: View
    private lateinit var btnVisitorAuth: View
    private lateinit var btnDelivery: View
    private lateinit var btnStaff: View
    private lateinit var btnAlerts: View
    private lateinit var btnResidents: View
    private lateinit var btnNotices: View
    private lateinit var btnMyProfile: View
    private lateinit var btnParking: View
    private lateinit var btnSOS: View
    private lateinit var btnLogout: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        initializeViews()
        setupWelcomeMessage()
        setupDateTime()
        setupButtonListeners()
        loadDashboardStats()
        setupBackPressHandler()
    }

    private fun initializeViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvDateTime = findViewById(R.id.tvDateTime)
        tvStatVisitors = findViewById(R.id.tvStatVisitors)
        tvStatDeliveries = findViewById(R.id.tvStatDeliveries)
        tvStatAlerts = findViewById(R.id.tvStatAlerts)

        btnVisitorApprove = findViewById(R.id.btnGoToVisitorApprove)
        btnVisitorArrival = findViewById(R.id.btnGoToVisitorArrival)
        btnVisitorAuth = findViewById(R.id.btnVisitorAuth)
        btnDelivery = findViewById(R.id.btnDelivery)
        btnStaff = findViewById(R.id.btnStaff)
        btnAlerts = findViewById(R.id.btnAlerts)
        btnResidents = findViewById(R.id.btnResidents)
        btnNotices = findViewById(R.id.btnNotices)
        btnMyProfile = findViewById(R.id.btnMyProfile)
        btnParking = findViewById(R.id.btnParking)
        btnSOS = findViewById(R.id.btnSOS)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun setupWelcomeMessage() {
        val user = auth.currentUser

        if (user != null) {
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { document ->

                    val name = document.getString("name") ?: "Admin"

                    tvWelcome.text = "Welcome, $name!"
                }
                .addOnFailureListener {
                    tvWelcome.text = "Welcome, Admin!"
                }
        }
    }

    private fun setupDateTime() {
        val currentDate = SimpleDateFormat(
            "EEE, dd MMM",
            Locale.getDefault()
        ).format(Date())

        tvDateTime.text = currentDate
    }

    private fun setupButtonListeners() {

        btnVisitorApprove.setOnClickListener {
            startActivity(Intent(this, VisitorApproveActivity::class.java))
        }

        btnVisitorArrival.setOnClickListener {
            startActivity(Intent(this, VisitorArrivalActivity::class.java))
        }

        btnVisitorAuth.setOnClickListener {
            startActivity(Intent(this, VisitorAuthActivity::class.java))
        }

        btnDelivery.setOnClickListener {
            startActivity(Intent(this, DeliveryLogActivity::class.java))
        }

        btnStaff.setOnClickListener {
            startActivity(Intent(this, StaffCheckInActivity::class.java))
        }

        btnAlerts.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        btnResidents.setOnClickListener {
            Toast.makeText(
                this,
                "Residents Directory Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnNotices.setOnClickListener {
            Toast.makeText(
                this,
                "Notices Module Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnMyProfile.setOnClickListener {
            Toast.makeText(
                this,
                "Profile Module Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnParking.setOnClickListener {
            Toast.makeText(
                this,
                "Parking Module Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnSOS.setOnClickListener {
            Toast.makeText(
                this,
                "SOS Module Coming Soon",
                Toast.LENGTH_SHORT
            ).show()
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun loadDashboardStats() {

        db.collection("visitor_requests")
            .get()
            .addOnSuccessListener { result ->
                tvStatVisitors.text = result.size().toString()
            }
            .addOnFailureListener {
                tvStatVisitors.text = "0"
            }

        db.collection("deliveries")
            .get()
            .addOnSuccessListener { result ->
                tvStatDeliveries.text = result.size().toString()
            }
            .addOnFailureListener {
                tvStatDeliveries.text = "0"
            }

        db.collection("alerts")
            .get()
            .addOnSuccessListener { result ->
                tvStatAlerts.text = result.size().toString()
            }
            .addOnFailureListener {
                tvStatAlerts.text = "0"
            }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->

                auth.signOut()

                val intent = Intent(this, MainActivity::class.java)

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupBackPressHandler() {
        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {

                override fun handleOnBackPressed() {

                    AlertDialog.Builder(this@HomeActivity)
                        .setTitle("Exit App")
                        .setMessage("Do you want to close MyGate?")
                        .setPositiveButton("Exit") { _, _ ->
                            finishAffinity()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        )
    }
}