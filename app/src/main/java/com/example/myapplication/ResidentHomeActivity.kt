package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
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

/**
 * ResidentHomeActivity.kt
 *
 * Homescreen for RESIDENTS only.
 *
 * ── What residents CAN do ──────────────────────────────────────────────────
 *   ✅ Pre-Approve a Visitor (their own guests)
 *   ✅ View Alerts / SOS
 *   ✅ Trigger SOS
 *   ✅ Notices Board  (coming soon)
 *   ✅ My Profile     (coming soon)
 *   ✅ Parking        (coming soon)
 *
 * ── What residents CANNOT do (hidden) ────────────────────────────────────
 *   ❌ Visitor Arrival Check-In  (guard duty)
 *   ❌ Visitor Auth / lookup     (guard duty)
 *   ❌ Log / manage Deliveries   (guard duty)
 *   ❌ Staff Entry management    (guard duty)
 *
 * ── Stats shown to resident ───────────────────────────────────────────────
 *   • My Visitors   — visitors the resident personally approved
 *   • Active Alerts — community-wide active (non-resolved) alerts
 */
class ResidentHomeActivity : AppCompatActivity() {

    // ── Visitor Management ─────────────────────────────────────────────────────
    private var btnPreApprove: LinearLayout? = null

    // ── Quick Access ───────────────────────────────────────────────────────────
    private var btnAlerts:    LinearLayout? = null
    private var btnNotices:   LinearLayout? = null
    private var btnMyProfile: LinearLayout? = null
    private var btnParking:   LinearLayout? = null
    private var btnSOS:       LinearLayout? = null

    // ── Header ─────────────────────────────────────────────────────────────────
    private var tvWelcome:  TextView? = null
    private var tvDateTime: TextView? = null

    // ── Stats ──────────────────────────────────────────────────────────────────
    private var tvStatMyVisitors: TextView? = null
    private var tvStatAlerts:     TextView? = null

    // ── Logout ─────────────────────────────────────────────────────────────────
    private var btnLogout: MaterialButton? = null

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage_resident)

        // ── Save FCM token so this device can receive push notifications ──────
        NotificationHelper.refreshAndSaveToken()
        FirebaseMessaging.getInstance().subscribeToTopic("all")
        FirebaseMessaging.getInstance().subscribeToTopic("residents")

        bindViews()
        setWelcomeHeader()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadResidentStats()
    }

    // ── View Binding ───────────────────────────────────────────────────────────
    private fun bindViews() {
        tvWelcome  = findViewById(R.id.tvWelcome)
        tvDateTime = findViewById(R.id.tvDateTime)

        btnPreApprove = findViewById(R.id.btnGoToVisitorApprove)

        btnAlerts    = findViewById(R.id.btnAlerts)
        btnNotices   = findViewById(R.id.btnNotices)
        btnMyProfile = findViewById(R.id.btnMyProfile)
        btnParking   = findViewById(R.id.btnParking)
        btnSOS       = findViewById(R.id.btnSOS)

        btnLogout = findViewById(R.id.btnLogout)

        tvStatMyVisitors = findViewById(R.id.tvStatMyVisitors)
        tvStatAlerts     = findViewById(R.id.tvStatAlerts)
    }

    // ── Welcome Header ─────────────────────────────────────────────────────────
    private fun setWelcomeHeader() {
        val user = auth.currentUser
        // Prefer displayName set at registration; fall back to email prefix
        val name = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "Resident"
        tvWelcome?.text = "Welcome, $name!"

        val date = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        tvDateTime?.text = date
    }

    // ── Resident-Scoped Stats ──────────────────────────────────────────────────
    private fun loadResidentStats() {

        // My visitors — only those I approved
        VisitorManager.getMyVisitors(
            { visitors ->
                tvStatMyVisitors?.text = visitors.size.toString()
            },
            { /* silently ignore */ }
        )

        // Active alerts — community-wide (relevant for all residents)
        AlertManager.getActiveAlerts(
            { alerts ->
                tvStatAlerts?.text = alerts.size.toString()
                val hasSOS = alerts.any { it.isSOS }
                val color = if (hasSOS) "#B71C1C" else "#37474F"
                tvStatAlerts?.setTextColor(android.graphics.Color.parseColor(color))
            },
            { /* silently ignore */ }
        )
    }

    // ── Click Listeners ────────────────────────────────────────────────────────
    private fun setClickListeners() {

        // Pre-approve a visitor (resident core feature)
        btnPreApprove?.setOnClickListener {
            val intent = Intent(this, VisitorApproveActivity::class.java)
            intent.putExtra("role", "resident")
            startActivity(intent)
        }

        // Alerts
        btnAlerts?.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        // Coming-soon stubs
        btnNotices?.setOnClickListener   { showComingSoon("Notices Board") }
        btnMyProfile?.setOnClickListener { showComingSoon("My Profile") }
        btnParking?.setOnClickListener   { showComingSoon("Parking Management") }

        // SOS
        btnSOS?.setOnClickListener { showSOSDialog() }

        // Logout
        btnLogout?.setOnClickListener { showLogoutDialog() }
    }

    // ── SOS ────────────────────────────────────────────────────────────────────
    private fun showSOSDialog() {
        AlertDialog.Builder(this)
            .setTitle("🆘 Emergency SOS")
            .setMessage("This will alert security and all residents. Send SOS?")
            .setPositiveButton("Send SOS") { _, _ ->
                val postedBy = auth.currentUser?.email?.substringBefore("@") ?: "Resident"
                AlertManager.postAlert(
                    "🆘 EMERGENCY SOS",
                    "Emergency alert triggered by $postedBy. Respond immediately!",
                    "sos",
                    "",
                    {
                        Toast.makeText(this, "🆘 SOS sent! Check Alerts screen.", Toast.LENGTH_LONG).show()
                        loadResidentStats()
                    },
                    { err ->
                        Toast.makeText(this, "Failed to send SOS: $err", Toast.LENGTH_SHORT).show()
                    }
                )
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Logout ─────────────────────────────────────────────────────────────────
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes, Logout") { _, _ ->
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Back press → confirm exit ──────────────────────────────────────────────
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        AlertDialog.Builder(this)
            .setTitle("Exit")
            .setMessage("Do you want to exit the app?")
            .setPositiveButton("Exit") { _, _ ->
                finishAffinity()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showComingSoon(feature: String) {
        Toast.makeText(this, "$feature — Coming Soon!", Toast.LENGTH_SHORT).show()
    }
}
