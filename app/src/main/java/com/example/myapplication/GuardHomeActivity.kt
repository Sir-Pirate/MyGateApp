package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * GuardHomeActivity.kt
 *
 * Homescreen for GUARDS only.
 *
 * ── What guards CAN do ────────────────────────────────────────────────────
 *   ✅ Visitor Arrival Check-In  (scan / log visitor at gate)
 *   ✅ Visitor Auth / lookup     (verify pre-approved visitors)
 *   ✅ Log a Delivery            (log parcels at gate)
 *   ✅ Staff Entry               (check in domestic/maintenance staff)
 *   ✅ View Alerts               (see community alerts)
 *
 * ── What guards CANNOT do (hidden) ───────────────────────────────────────
 *   ❌ Pre-Approve a Visitor     (resident privilege)
 *   ❌ SOS button                (resident privilege)
 *   ❌ Notices Board             (resident privilege)
 *   ❌ My Profile                (resident privilege)
 *   ❌ Parking Management        (resident privilege)
 *
 * ── Stats shown to guard ──────────────────────────────────────────────────
 *   • Visitors Today      — all visitors logged today (arrived status)
 *   • Pending Deliveries  — deliveries awaiting pickup
 *   • Active SOS Alerts   — unresolved SOS alerts
 */
class GuardHomeActivity : AppCompatActivity() {

    // ── Visitor Management ─────────────────────────────────────────────────────
    private var btnVisitorArrival: LinearLayout? = null
    private var btnVisitorAuth:    LinearLayout? = null

    // ── Quick Access ───────────────────────────────────────────────────────────
    private var btnDelivery: LinearLayout? = null
    private var btnStaff:    LinearLayout? = null
    private var btnAlerts:   LinearLayout? = null

    // ── Header ─────────────────────────────────────────────────────────────────
    private var tvWelcome:  TextView? = null
    private var tvDateTime: TextView? = null

    // ── Stats ──────────────────────────────────────────────────────────────────
    private var tvStatVisitors:   TextView? = null
    private var tvStatDeliveries: TextView? = null
    private var tvStatAlerts:     TextView? = null

    // ── Logout ─────────────────────────────────────────────────────────────────
    private var btnLogout: MaterialButton? = null

    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage_guard)

        // ── FCM setup ─────────────────────────────────────────────────────────
        // Topic subscriptions should be handled in your FirebaseMessagingService
        // (onNewToken callback) to avoid unresolved FirebaseMessaging references.
        NotificationHelper.refreshAndSaveToken()

        bindViews()
        setWelcomeHeader()
        registerBackHandler()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadGuardStats()
    }

    // ── View Binding ───────────────────────────────────────────────────────────
    private fun bindViews() {
        tvWelcome  = findViewById(R.id.tvWelcome)
        tvDateTime = findViewById(R.id.tvDateTime)

        btnVisitorArrival = findViewById(R.id.btnGoToVisitorArrival)
        btnVisitorAuth    = findViewById(R.id.btnVisitorAuth)

        btnDelivery = findViewById(R.id.btnDelivery)
        btnStaff    = findViewById(R.id.btnStaff)
        btnAlerts   = findViewById(R.id.btnAlerts)

        btnLogout = findViewById(R.id.btnLogout)

        tvStatVisitors   = findViewById(R.id.tvStatVisitors)
        tvStatDeliveries = findViewById(R.id.tvStatDeliveries)
        tvStatAlerts     = findViewById(R.id.tvStatAlerts)
    }

    // ── Welcome Header ─────────────────────────────────────────────────────────
    private fun setWelcomeHeader() {
        val user = auth.currentUser
        val name = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: getString(R.string.guard_default_name)

        // Use a string resource: <string name="welcome_guard">Welcome, %1$s!</string>
        tvWelcome?.text = getString(R.string.welcome_guard, name)

        val date = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        tvDateTime?.text = date
    }

    // ── Guard-Scoped Stats ─────────────────────────────────────────────────────
    private fun loadGuardStats() {

        // All visitors today (arrived)
        VisitorManager.getAllVisitors(
            "arrived",
            { visitors ->
                tvStatVisitors?.text = visitors.size.toString()
            },
            { /* silently ignore */ }
        )

        // Pending deliveries at gate
        DeliveryManager.getPendingDeliveries(
            { deliveries ->
                tvStatDeliveries?.text = deliveries.size.toString()
            },
            { /* silently ignore */ }
        )

        // Active SOS alerts — guard must respond
        AlertManager.getActiveAlerts(
            { alerts ->
                val sosCount = alerts.count { it.isSOS }
                tvStatAlerts?.text = sosCount.toString()
                // Red if active SOS, neutral green if clear
                val colorHex = if (sosCount > 0) "#B71C1C" else "#2E7D32"
                tvStatAlerts?.setTextColor(colorHex.toColorInt())
            },
            { /* silently ignore */ }
        )
    }

    // ── Click Listeners ────────────────────────────────────────────────────────
    private fun setClickListeners() {

        // Visitor arrival check-in at gate
        btnVisitorArrival?.setOnClickListener {
            startActivity(Intent(this, VisitorArrivalActivity::class.java))
        }

        // Visitor auth / lookup for pre-approved visitors
        btnVisitorAuth?.setOnClickListener {
            startActivity(Intent(this, VisitorAuthActivity::class.java))
        }

        // Delivery log
        btnDelivery?.setOnClickListener {
            startActivity(Intent(this, DeliveryLogActivity::class.java))
        }

        // Staff check-in
        btnStaff?.setOnClickListener {
            startActivity(Intent(this, StaffCheckInActivity::class.java))
        }

        // Alerts — guard can view all alerts
        btnAlerts?.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        // Logout
        btnLogout?.setOnClickListener { showLogoutDialog() }
    }

    // ── Back Press (AndroidX OnBackPressedDispatcher) ──────────────────────────
    private fun registerBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@GuardHomeActivity)
                    .setTitle(getString(R.string.exit_title))
                    .setMessage(getString(R.string.exit_message))
                    .setPositiveButton(getString(R.string.exit_confirm)) { _, _ ->
                        finishAffinity()
                    }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        })
    }

    // ── Logout ─────────────────────────────────────────────────────────────────
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.logout_title))
            .setMessage(getString(R.string.logout_message))
            .setPositiveButton(getString(R.string.logout_confirm)) { _, _ ->
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }
}