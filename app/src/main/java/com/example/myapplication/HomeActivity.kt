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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class HomeActivity : AppCompatActivity() {

    private var btnGoToVisitorApprove: LinearLayout? = null
    private var btnGoToVisitorArrival: LinearLayout? = null

    private var userRole: String? = null

    private var btnVisitorAuth: LinearLayout? = null
    private var btnDelivery: LinearLayout? = null
    private var btnStaff: LinearLayout? = null
    private var btnAlerts: LinearLayout? = null
    private var btnResidents: LinearLayout? = null
    private var btnNotices: LinearLayout? = null
    private var btnMyProfile: LinearLayout? = null
    private var btnParking: LinearLayout? = null
    private var btnSOS: LinearLayout? = null

    private var tvWelcome: TextView? = null
    private var tvDateTime: TextView? = null

    private var btnLogout: MaterialButton? = null

    private val auth = FirebaseAuth.getInstance()

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)

        userRole = intent.getStringExtra("role")

        bindViews()
        setWelcomeHeader()
        applyRoleBasedUI()
        setClickListeners()
    }

    private fun bindViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvDateTime = findViewById(R.id.tvDateTime)

        btnGoToVisitorApprove = findViewById(R.id.btnGoToVisitorApprove)
        btnGoToVisitorArrival = findViewById(R.id.btnGoToVisitorArrival)

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

    private fun setWelcomeHeader() {
        val user = auth.currentUser
        if (user?.email != null) {
            val name = user.email!!.substringBefore("@")
            tvWelcome?.text = "Welcome, $name!"
        }

        val date = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())
        tvDateTime?.text = date
    }

    private fun applyRoleBasedUI() {

        if (userRole == null) {
            Toast.makeText(this, "Role missing", Toast.LENGTH_SHORT).show()
            hideSensitiveFeatures()
            return
        }

        when (userRole) {

            "resident" -> {
                btnGoToVisitorArrival?.visibility = View.GONE
                btnVisitorAuth?.visibility = View.VISIBLE

                btnDelivery?.visibility = View.GONE
                btnDelivery?.isEnabled = false
                btnDelivery?.setOnClickListener(null)

                // Save FCM token
                val uid = auth.currentUser?.uid
                if (uid != null) {
                    FirebaseMessaging.getInstance()
                        .token
                        .addOnSuccessListener { token ->
                            FirebaseFirestore.getInstance()
                                .collection("users")
                                .document(uid)
                                .update("fcmToken", token)
                        }
                }
            }

            "guard" -> {
                btnGoToVisitorApprove?.visibility = View.GONE

                btnNotices?.visibility = View.GONE
                btnMyProfile?.visibility = View.GONE
                btnParking?.visibility = View.GONE
                btnSOS?.visibility = View.GONE
            }

            "admin" -> {
                // full access
            }

            else -> {
                Toast.makeText(this, "Invalid role", Toast.LENGTH_SHORT).show()
                hideSensitiveFeatures()
            }
        }
    }

    private fun hideSensitiveFeatures() {
        btnDelivery?.visibility = View.GONE
        btnGoToVisitorApprove?.visibility = View.GONE
        btnVisitorAuth?.visibility = View.GONE
    }

    private fun setClickListeners() {

        btnGoToVisitorApprove?.setOnClickListener {
            val intent = Intent(this, VisitorApproveActivity::class.java)
            intent.putExtra("role", userRole)
            startActivity(intent)
        }

        btnGoToVisitorArrival?.setOnClickListener {
            startActivity(Intent(this, VisitorArrivalActivity::class.java))
        }

        btnVisitorAuth?.setOnClickListener {
            val intent = Intent(this, VisitorAuthActivity::class.java)
            intent.putExtra("role", userRole)
            startActivity(intent)
        }

        btnDelivery?.setOnClickListener {
            if (userRole == "resident") {
                Toast.makeText(this, "Not allowed", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this, DeliveryLogActivity::class.java))
        }

        // ✅ FINAL STAFF FLOW
        btnStaff?.setOnClickListener {

            if (userRole == "guard") {

                startActivity(
                    Intent(this, StaffEntryActivity::class.java)
                )

            } else if (userRole == "resident") {

                // 🔥 NEW: open menu instead of directly adding
                startActivity(
                    Intent(this, StaffMenuActivity::class.java)
                )

            } else {

                showComingSoon("Staff Feature")
            }
        }

        btnAlerts?.setOnClickListener {
            startActivity(Intent(this, AlertsActivity::class.java))
        }

        btnResidents?.setOnClickListener {
            showComingSoon("Residents Directory")
        }

        btnNotices?.setOnClickListener {
            showComingSoon("Notices Board")
        }

        btnMyProfile?.setOnClickListener {
            showComingSoon("My Profile")
        }

        btnParking?.setOnClickListener {
            showComingSoon("Parking Management")
        }

        btnSOS?.setOnClickListener {
            showSOSDialog()
        }

        btnLogout?.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showComingSoon(feature: String) {
        Toast.makeText(this, "$feature — Coming Soon!", Toast.LENGTH_SHORT).show()
    }

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

    private fun showSOSDialog() {
        AlertDialog.Builder(this)
            .setTitle("🆘 Emergency SOS")
            .setMessage("Send alert to security?")
            .setPositiveButton("Send") { _, _ ->
                Toast.makeText(this, "SOS Sent!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}