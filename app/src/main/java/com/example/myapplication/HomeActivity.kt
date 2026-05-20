package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import android.util.Log
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import com.google.firebase.firestore.DocumentChange

class HomeActivity : AppCompatActivity() {

    // Header
    private lateinit var tvWelcome: TextView
    private lateinit var tvDateTime: TextView

    // Main Buttons
    private lateinit var btnGoToVisitorApprove: LinearLayout
    private lateinit var btnGoToVisitorArrival: LinearLayout
    private lateinit var btnVisitorAuth: LinearLayout
    private lateinit var btnDelivery: LinearLayout
    private lateinit var btnStaff: LinearLayout
    private lateinit var btnAlerts: LinearLayout
    private lateinit var btnResidents: LinearLayout
    private lateinit var btnNotices: LinearLayout
    private lateinit var btnMyProfile: LinearLayout
    private lateinit var btnParking: LinearLayout
    private lateinit var btnSOS: LinearLayout
    private lateinit var btnStaffDashboard: LinearLayout

    private lateinit var btnLogout: MaterialButton

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Role
    private var userRole: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        userRole = intent.getStringExtra("role") ?: ""

        bindViews()
        setWelcomeHeader()
        applyRoleBasedUI()
        setupClickListeners()

        saveFCMToken()
        requestNotificationPermission()
        startResidentAlertListener()
    }

    // Bind Views
    private fun bindViews() {

        tvWelcome = findViewById(R.id.tvWelcome)
        tvDateTime = findViewById(R.id.tvDateTime)

        btnGoToVisitorApprove =
            findViewById(R.id.btnGoToVisitorApprove)

        btnGoToVisitorArrival =
            findViewById(R.id.btnGoToVisitorArrival)

        btnVisitorAuth =
            findViewById(R.id.btnVisitorAuth)

        btnDelivery =
            findViewById(R.id.btnDelivery)

        btnStaff =
            findViewById(R.id.btnStaff)

        btnAlerts =
            findViewById(R.id.btnAlerts)

        btnResidents =
            findViewById(R.id.btnResidents)

        btnNotices =
            findViewById(R.id.btnNotices)

        btnMyProfile =
            findViewById(R.id.btnMyProfile)

        btnParking =
            findViewById(R.id.btnParking)

        btnSOS =
            findViewById(R.id.btnSOS)

        btnStaffDashboard =
            findViewById(R.id.btnStaffDashboard)

        btnLogout =
            findViewById(R.id.btnLogout)
    }

    // Welcome Header
    private fun setWelcomeHeader() {

        val user = auth.currentUser

        if (user?.email != null) {

            val name = user.email!!
                .substringBefore("@")

            tvWelcome.text = "Welcome, $name!"
        }

        val currentDate = SimpleDateFormat(
            "EEE, dd MMM",
            Locale.getDefault()
        ).format(Date())

        tvDateTime.text = currentDate
    }

    // Role UI
    private fun applyRoleBasedUI() {

        when (userRole) {

            "resident" -> {

                btnGoToVisitorArrival.visibility = View.GONE

                btnDelivery.visibility = View.GONE
                btnDelivery.isEnabled = false

                btnVisitorAuth.visibility = View.VISIBLE
                btnStaffDashboard.visibility = View.VISIBLE
            }

            "guard" -> {

                btnGoToVisitorApprove.visibility = View.GONE

                btnNotices.visibility = View.GONE
                btnMyProfile.visibility = View.GONE
                btnParking.visibility = View.GONE
                btnSOS.visibility = View.GONE
                btnStaffDashboard.visibility = View.GONE
            }

            "admin" -> {
                // Full access
            }

            else -> {

                Toast.makeText(
                    this,
                    "Invalid role",
                    Toast.LENGTH_SHORT
                ).show()

                hideSensitiveFeatures()
            }
        }
    }

    // Hide Invalid Features
    private fun hideSensitiveFeatures() {

        btnDelivery.visibility = View.GONE
        btnVisitorAuth.visibility = View.GONE
        btnGoToVisitorApprove.visibility = View.GONE
    }

    // Save FCM Token
    private fun saveFCMToken() {

        val currentUser = auth.currentUser ?: return

        FirebaseMessaging.getInstance()
            .token
            .addOnSuccessListener { token ->

                Toast.makeText(
                    this,
                    token,
                    Toast.LENGTH_LONG
                ).show()

                Log.d("FCM_TOKEN", token)

                firestore.collection("users")
                    .document(currentUser.uid)
                    .update("fcmToken", token)
            }
    }

    // Resident Alert Listener
    private fun startResidentAlertListener() {

        val currentUser = auth.currentUser ?: return

        firestore.collection("alerts")
            .whereEqualTo("residentId", currentUser.uid)
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) {
                    return@addSnapshotListener
                }

                for (change in snapshots.documentChanges) {

                    if (change.type ==
                        DocumentChange.Type.ADDED
                    ) {

                        val title =
                            change.document.getString("title")
                                ?: "New Alert"

                        val message =
                            change.document.getString("message")
                                ?: ""

                        showLocalNotification(
                            title,
                            message
                        )
                    }
                }
            }
    }

    // Local Push Notification
    private fun showLocalNotification(
        title: String,
        message: String
    ) {

        val channelId = "resident_alerts"

        val manager =
            getSystemService(
                NotificationManager::class.java
            )

        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.O
        ) {

            val channel = NotificationChannel(
                channelId,
                "Resident Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val builder =
            NotificationCompat.Builder(this, channelId)
                .setSmallIcon(
                    android.R.drawable.ic_dialog_info
                )
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(
                    NotificationCompat.PRIORITY_HIGH
                )
                .setAutoCancel(true)

        manager.notify(
            System.currentTimeMillis().toInt(),
            builder.build()
        )
    }


    // Click Listeners
    private fun setupClickListeners() {

        btnGoToVisitorApprove.setOnClickListener {

            val intent = Intent(
                this,
                VisitorApproveActivity::class.java
            )

            intent.putExtra("role", userRole)

            startActivity(intent)
        }

        btnGoToVisitorArrival.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    VisitorArrivalActivity::class.java
                )
            )
        }

        btnVisitorAuth.setOnClickListener {

            val intent = Intent(
                this,
                VisitorAuthActivity::class.java
            )

            intent.putExtra("role", userRole)

            startActivity(intent)
        }

        btnDelivery.setOnClickListener {

            if (userRole == "resident") {

                Toast.makeText(
                    this,
                    "Access Denied",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            startActivity(
                Intent(
                    this,
                    DeliveryLogActivity::class.java
                )
            )
        }

        // Staff Module
        btnStaff.setOnClickListener {

            when (userRole) {

                "guard" -> {

                    startActivity(
                        Intent(
                            this,
                            StaffEntryActivity::class.java
                        )
                    )
                }

                "resident" -> {

                    startActivity(
                        Intent(
                            this,
                            StaffMenuActivity::class.java
                        )
                    )
                }

                else -> {
                    showComingSoon("Staff Feature")
                }
            }
        }

        // Alerts
        btnAlerts.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    AlertsActivity::class.java
                )
            )
        }

        btnResidents.setOnClickListener {
            showComingSoon("Residents Directory")
        }

        btnNotices.setOnClickListener {
            showComingSoon("Notice Board")
        }

        btnMyProfile.setOnClickListener {
            showComingSoon("My Profile")
        }

        btnParking.setOnClickListener {
            showComingSoon("Parking Management")
        }

        btnSOS.setOnClickListener {
            showSOSDialog()
        }

        btnStaffDashboard.setOnClickListener {

            if (userRole == "resident") {

                startActivity(
                    Intent(
                        this,
                        StaffDashboardActivity::class.java
                    )
                )

            } else {

                showComingSoon("Staff Dashboard")
            }
        }

        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    // Coming Soon Toast
    private fun showComingSoon(feature: String) {

        Toast.makeText(
            this,
            "$feature - Coming Soon!",
            Toast.LENGTH_SHORT
        ).show()
    }

    // Logout Dialog
    private fun showLogoutDialog() {

        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->

                auth.signOut()

                val intent = Intent(
                    this,
                    MainActivity::class.java
                )

                intent.flags =
                    Intent.FLAG_ACTIVITY_NEW_TASK or
                            Intent.FLAG_ACTIVITY_CLEAR_TASK

                startActivity(intent)

                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // SOS Dialog
    private fun showSOSDialog() {

        AlertDialog.Builder(this)
            .setTitle("🆘 Emergency SOS")
            .setMessage("Send emergency alert to security?")
            .setPositiveButton("Send") { _, _ ->

                Toast.makeText(
                    this,
                    "SOS Alert Sent!",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    private fun requestNotificationPermission() {

        if (android.os.Build.VERSION.SDK_INT >=
            android.os.Build.VERSION_CODES.TIRAMISU
        ) {

            if (
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                )
                != PackageManager.PERMISSION_GRANTED
            ) {

                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    1001
                )
            }
        }
    }
}
