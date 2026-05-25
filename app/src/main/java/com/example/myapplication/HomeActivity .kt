package com.example.myapplication

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class HomeActivity : AppCompatActivity() {

    // Header
    private lateinit var tvWelcome: TextView
    private lateinit var tvDateTime: TextView

    // Fix 7: Avatar menu replaces logout button
    private var btnAvatarMenu: LinearLayout? = null
    private var tvAvatarInitial: TextView? = null

    // Buttons
    private var btnGoToVisitorApprove: LinearLayout? = null
    private var btnGoToVisitorArrival: LinearLayout? = null
    private var btnVisitorAuth: LinearLayout? = null
    private var btnDelivery: LinearLayout? = null
    private var btnStaff: LinearLayout? = null
    private var btnAlerts: LinearLayout? = null
    private var btnMyProfile: LinearLayout? = null
    private var btnStaffDashboard: LinearLayout? = null
    private var btnLockerStatus: LinearLayout? = null

    // Fix 7: btnLogout removed — logout is now triggered via btnAvatarMenu

    // Fix 5: Stat TextViews for live Firestore data
    private var tvStatVisitors: TextView? = null
    private var tvStatDeliveries: TextView? = null
    private var tvStatAlerts: TextView? = null

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // Role
    private var userRole: String = ""

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        // Fix 6: Enable edge-to-edge before setContentView
        enableEdgeToEdge()

        // Read role from intent
        userRole = intent.getStringExtra("role") ?: ""

        inflateLayoutForRole(userRole)

        // Fix 6: Apply system bar insets dynamically
        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(android.R.id.content)
        ) { view, insets ->

            val systemBars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars()
            )

            view.setPadding(0, systemBars.top, 0, 0)

            insets
        }

        bindViews()
        setWelcomeHeader()
        setupClickListeners()

        saveFCMToken()
        requestNotificationPermission()
        startResidentAlertListener()
    }

    // Re-check role whenever activity resumes
    override fun onResume() {

        super.onResume()

        val currentUser = auth.currentUser

        // If user logged out
        if (currentUser == null) {
            goToLogin()
            return
        }

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { document ->

                if (!document.exists()) {
                    goToLogin()
                    return@addOnSuccessListener
                }

                val freshRole = document.getString("role") ?: "resident"

                // Reload correct layout if role changed
                if (freshRole != userRole) {

                    userRole = freshRole

                    inflateLayoutForRole(userRole)

                    // Re-apply insets after layout reload
                    ViewCompat.setOnApplyWindowInsetsListener(
                        findViewById(android.R.id.content)
                    ) { view, insets ->

                        val systemBars = insets.getInsets(
                            WindowInsetsCompat.Type.systemBars()
                        )

                        view.setPadding(0, systemBars.top, 0, 0)

                        insets
                    }

                    bindViews()
                    setWelcomeHeader()
                    setupClickListeners()
                }

                // Fix 5: Refresh live stats on every resume
                loadStats()
            }
    }

    // Inflate role-based layout
    private fun inflateLayoutForRole(role: String) {

        val layout = when (role) {
            "resident" -> R.layout.homepage_resident
            "guard"    -> R.layout.homepage_guard
            "admin"    -> R.layout.homepage_admin
            else       -> R.layout.homepage_resident
        }

        setContentView(layout)
    }

    // Bind all views
    private fun bindViews() {

        tvWelcome  = findViewById(R.id.tvWelcome)
        tvDateTime = findViewById(R.id.tvDateTime)

        // Fix 7: Bind avatar menu views
        btnAvatarMenu   = findViewById(R.id.btnAvatarMenu)
        tvAvatarInitial = findViewById(R.id.tvAvatarInitial)

        btnGoToVisitorApprove = findViewById(R.id.btnGoToVisitorApprove)
        btnGoToVisitorArrival = findViewById(R.id.btnGoToVisitorArrival)
        btnVisitorAuth        = findViewById(R.id.btnVisitorAuth)
        btnDelivery           = findViewById(R.id.btnDelivery)
        btnStaff              = findViewById(R.id.btnStaff)
        btnAlerts             = findViewById(R.id.btnAlerts)
        btnMyProfile          = findViewById(R.id.btnMyProfile)
        btnStaffDashboard     = findViewById(R.id.btnStaffDashboard)
        btnLockerStatus       = findViewById(R.id.btnLockerStatus)

        // Fix 5: Bind stat views
        tvStatVisitors   = findViewById(R.id.tvStatVisitors)
        tvStatDeliveries = findViewById(R.id.tvStatDeliveries)
        tvStatAlerts     = findViewById(R.id.tvStatAlerts)
    }

    // Welcome text, date, and avatar initial
    private fun setWelcomeHeader() {

        val user = auth.currentUser

        if (user?.email != null) {

            val name = user.email!!.substringBefore("@")
            tvWelcome.text = "Welcome, $name!"

            // Fix 7: Set avatar initial from email
            val initial = name
                .first()
                .uppercaseChar()
                .toString()

            tvAvatarInitial?.text = initial
        }

        val currentDate = SimpleDateFormat(
            "EEE, dd MMM",
            Locale.getDefault()
        ).format(Date())

        tvDateTime.text = currentDate
    }

    // Fix 5: Load live stats from Firestore
    private fun loadStats() {

        val currentUser = auth.currentUser ?: return

        // Today's start timestamp
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Visitors today — resident sees own flat only, guard/admin see all
        val visitorQuery = if (userRole == "resident") {
            firestore.collection("visitors")
                .whereEqualTo("residentId", currentUser.uid)
                .whereGreaterThan("timestamp", startOfDay)
        } else {
            firestore.collection("visitors")
                .whereGreaterThan("timestamp", startOfDay)
        }

        visitorQuery.get().addOnSuccessListener { snap ->
            tvStatVisitors?.text = snap.size().toString()
        }

        // Pending deliveries
        firestore.collection("deliveries")
            .whereEqualTo("status", "pending")
            .get()
            .addOnSuccessListener { snap ->
                tvStatDeliveries?.text = snap.size().toString()
            }

        // Unread alerts for this user
        firestore.collection("alerts")
            .whereEqualTo("residentId", currentUser.uid)
            .whereEqualTo("read", false)
            .get()
            .addOnSuccessListener { snap ->
                tvStatAlerts?.text = snap.size().toString()
            }
    }

    // Button click listeners
    private fun setupClickListeners() {

        // Fix 7: Avatar menu triggers logout dialog
        btnAvatarMenu?.setOnClickListener {
            showLogoutDialog()
        }

        btnGoToVisitorApprove?.setOnClickListener {

            val intent = Intent(this, VisitorApproveActivity::class.java)
            intent.putExtra("role", userRole)
            startActivity(intent)
        }

        btnGoToVisitorArrival?.setOnClickListener {

            startActivity(
                Intent(this, VisitorArrivalActivity::class.java)
            )
        }

        btnVisitorAuth?.setOnClickListener {

            val intent = Intent(this, VisitorAuthActivity::class.java)
            intent.putExtra("role", userRole)
            startActivity(intent)
        }

        btnDelivery?.setOnClickListener {

            startActivity(
                Intent(this, DeliveryLogActivity::class.java)
            )
        }

        // Staff routing based on role
        btnStaff?.setOnClickListener {

            when (userRole) {
                "guard"    -> startActivity(Intent(this, StaffEntryActivity::class.java))
                "resident" -> startActivity(Intent(this, StaffMenuActivity::class.java))
                "admin"    -> startActivity(Intent(this, StaffListActivity::class.java))
            }
        }

        btnAlerts?.setOnClickListener {

            startActivity(
                Intent(this, AlertsActivity::class.java)
            )
        }

        btnLockerStatus?.setOnClickListener {

            startActivity(
                Intent(this, ResidentLockerActivity::class.java)
            )
        }

        btnStaffDashboard?.setOnClickListener {

            startActivity(
                Intent(this, StaffDashboardActivity::class.java)
            )
        }

        btnMyProfile?.setOnClickListener {

            showComingSoon("My Profile")
        }
    }

    // Save FCM token to Firestore
    private fun saveFCMToken() {

        val currentUser = auth.currentUser ?: return

        FirebaseMessaging.getInstance()
            .token
            .addOnSuccessListener { token ->

                Log.d("FCM_TOKEN", token)

                firestore.collection("users")
                    .document(currentUser.uid)
                    .update("fcmToken", token)
            }
    }

    // Listen for new resident alerts and show local notification
    private fun startResidentAlertListener() {

        val currentUser = auth.currentUser ?: return

        firestore.collection("alerts")
            .whereEqualTo("residentId", currentUser.uid)
            .addSnapshotListener { snapshots, error ->

                if (error != null || snapshots == null) {
                    return@addSnapshotListener
                }

                for (change in snapshots.documentChanges) {

                    if (change.type == DocumentChange.Type.ADDED) {

                        val title   = change.document.getString("title")   ?: "New Alert"
                        val message = change.document.getString("message") ?: ""

                        showLocalNotification(title, message)
                    }
                }
            }
    }

    // Show local push notification
    private fun showLocalNotification(title: String, message: String) {

        val channelId = "resident_alerts"
        val manager   = getSystemService(NotificationManager::class.java)

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            val channel = NotificationChannel(
                channelId,
                "Resident Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )

            manager.createNotificationChannel(channel)
        }

        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)

        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    // Coming soon toast
    private fun showComingSoon(feature: String) {

        Toast.makeText(this, "$feature - Coming Soon!", Toast.LENGTH_SHORT).show()
    }

    // Logout confirmation dialog
    private fun showLogoutDialog() {

        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                auth.signOut()
                goToLogin()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // Redirect to log in screen
    private fun goToLogin() {

        val intent = Intent(this, MainActivity::class.java)

        intent.flags =
            Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TASK

        startActivity(intent)
        finish()
    }

    // Request notification permission (Android 13+)
    private fun requestNotificationPermission() {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {

            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
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