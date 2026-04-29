package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.toColorInt
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ResidentHomeActivity.kt — Homescreen for RESIDENTS only.
 */
class ResidentHomeActivity : AppCompatActivity() {

    private var btnPreApprove: LinearLayout? = null
    private var btnAlerts:    LinearLayout? = null
    private var btnNotices:   LinearLayout? = null
    private var btnMyProfile: LinearLayout? = null
    private var btnParking:   LinearLayout? = null
    private var btnSOS:       LinearLayout? = null

    private var tvWelcome:  TextView? = null
    private var tvDateTime: TextView? = null

    private var tvStatMyVisitors: TextView? = null
    private var tvStatAlerts:     TextView? = null

    private var btnLogout: MaterialButton? = null

    private val auth = FirebaseAuth.getInstance()
    private val db   = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage_resident)

        // FCM token refresh — topic subscriptions are handled in MyFirebaseMessagingService
        NotificationHelper.refreshAndSaveToken()

        bindViews()
        setWelcomeHeader()
        registerBackHandler()
        setClickListeners()
    }

    override fun onResume() {
        super.onResume()
        loadResidentStats()
    }

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

    private fun setWelcomeHeader() {
        val user = auth.currentUser
        val name = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")
            ?: "Resident"
        tvWelcome?.text = getString(R.string.welcome_guard, name) // reuses same pattern

        val date = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        tvDateTime?.text = date
    }

    private fun loadResidentStats() {
        VisitorManager.getMyVisitors(
            { visitors -> tvStatMyVisitors?.text = visitors.size.toString() },
            { /* silently ignore */ }
        )

        AlertManager.getActiveAlerts(
            { alerts ->
                tvStatAlerts?.text = alerts.size.toString()
                val hasSOS = alerts.any { it.isSOS }
                val color = if (hasSOS) "#B71C1C" else "#37474F"
                tvStatAlerts?.setTextColor(color.toColorInt())
            },
            { /* silently ignore */ }
        )
    }

    private fun setClickListeners() {
        btnPreApprove?.setOnClickListener {
            val intent = Intent(this, VisitorApproveActivity::class.java)
            intent.putExtra("role", "resident")
            startActivity(intent)
        }

        btnAlerts?.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        btnNotices?.setOnClickListener   { showComingSoon("Notices Board") }
        btnMyProfile?.setOnClickListener { showComingSoon("My Profile") }
        btnParking?.setOnClickListener   { showComingSoon("Parking Management") }
        btnSOS?.setOnClickListener       { showSOSDialog() }
        btnLogout?.setOnClickListener    { showLogoutDialog() }
    }

    private fun registerBackHandler() {
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                AlertDialog.Builder(this@ResidentHomeActivity)
                    .setTitle(getString(R.string.exit_title))
                    .setMessage(getString(R.string.exit_message))
                    .setPositiveButton(getString(R.string.exit_confirm)) { _, _ -> finishAffinity() }
                    .setNegativeButton(getString(R.string.cancel), null)
                    .show()
            }
        })
    }

    private fun showSOSDialog() {
        AlertDialog.Builder(this)
            .setTitle("🆘 Emergency SOS")
            .setMessage("This will alert security and all residents. Send SOS?")
            .setPositiveButton("Send SOS") { _, _ ->
                val postedBy = auth.currentUser?.email?.substringBefore("@") ?: "Resident"
                AlertManager.postAlert(
                    "🆘 EMERGENCY SOS",
                    "Emergency alert triggered by $postedBy. Respond immediately!",
                    "sos", "",
                    {
                        Toast.makeText(this, "🆘 SOS sent! Check Alerts screen.", Toast.LENGTH_LONG).show()
                        loadResidentStats()
                    },
                    { err -> Toast.makeText(this, "Failed to send SOS: $err", Toast.LENGTH_SHORT).show() }
                )
            }
            .setNegativeButton(getString(R.string.cancel), null)
            .show()
    }

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

    private fun showComingSoon(feature: String) {
        Toast.makeText(this, "$feature — Coming Soon!", Toast.LENGTH_SHORT).show()
    }
}
