package com.example.myapplication

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.DialogInterface
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

// Commented out until team members merge their branches
// import AlertsActivity.AlertsActivity;
// import DeliveryActivity.DeliveryActivity;
// import ResidentsActivity.ResidentsActivity;
// import StaffActivity.StaffActivity;
// import VisitorActivity.VisitorActivity;
class HomeActivity : AppCompatActivity() {
    // ── Visitor Management (M3 — your screens) ─────────────────────────────────
    private var btnGoToVisitorApprove: LinearLayout? = null
    private var btnGoToVisitorArrival: LinearLayout? = null

    //to display features based on role
    private var userRole: String? = null

    // ── Quick Access Grid ──────────────────────────────────────────────────────
    private var btnVisitorAuth: LinearLayout? = null
    private var btnDelivery: LinearLayout? = null
    private var btnStaff: LinearLayout? = null
    private var btnAlerts: LinearLayout? = null
    private var btnResidents: LinearLayout? = null
    private var btnNotices: LinearLayout? = null
    private var btnMyProfile: LinearLayout? = null
    private var btnParking: LinearLayout? = null
    private var btnSOS: LinearLayout? = null

    // ── Header ─────────────────────────────────────────────────────────────────
    private var tvWelcome: TextView? = null
    private var tvDateTime: TextView? = null

    // ── Logout ─────────────────────────────────────────────────────────────────
    private var btnLogout: MaterialButton? = null

    // ── Firebase ───────────────────────────────────────────────────────────────
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

    // ── View Binding ───────────────────────────────────────────────────────────
    private fun bindViews() {
        // Header
        tvWelcome = findViewById<TextView>(R.id.tvWelcome)
        tvDateTime = findViewById<TextView>(R.id.tvDateTime)

        // Visitor Management
        btnGoToVisitorApprove = findViewById<LinearLayout>(R.id.btnGoToVisitorApprove)
        btnGoToVisitorArrival = findViewById<LinearLayout>(R.id.btnGoToVisitorArrival)

        // Quick Access
        btnVisitorAuth = findViewById<LinearLayout>(R.id.btnVisitorAuth)
        btnDelivery = findViewById<LinearLayout>(R.id.btnDelivery)
        btnStaff = findViewById<LinearLayout>(R.id.btnStaff)
        btnAlerts = findViewById<LinearLayout>(R.id.btnAlerts)
        btnResidents = findViewById<LinearLayout>(R.id.btnResidents)
        btnNotices = findViewById<LinearLayout>(R.id.btnNotices)
        btnMyProfile = findViewById<LinearLayout>(R.id.btnMyProfile)
        btnParking = findViewById<LinearLayout>(R.id.btnParking)
        btnSOS = findViewById<LinearLayout>(R.id.btnSOS)

        // Logout
        btnLogout = findViewById<MaterialButton>(R.id.btnLogout)
    }

    // ── Welcome Header ─────────────────────────────────────────────────────────
    private fun setWelcomeHeader() {
        // Show logged-in user's email in welcome text
        val user = auth.getCurrentUser()
        if (user != null && user.getEmail() != null) {
            val email = user.getEmail()
            val name = email!!.substring(0, email.indexOf('@')) // use part before @
            tvWelcome!!.setText("Welcome, " + name + "!")
        }

        // Show current date
        val date = SimpleDateFormat("EEE, dd MMM", Locale.getDefault()).format(Date())
        tvDateTime!!.setText(date)
    }

    // role based features
    private fun applyRoleBasedUI() {
        when (userRole) {

            "resident" -> {
                // Residents should NOT see guard features
                btnGoToVisitorArrival?.visibility = View.GONE
                btnVisitorAuth?.visibility = View.GONE
            }

            "guard" -> {
                // Guards should NOT approve visitors
                btnGoToVisitorApprove?.visibility = View.GONE
                btnNotices?.visibility = View.GONE
                btnMyProfile?.visibility = View.GONE
                btnParking?.visibility = View.GONE
                btnSOS?.visibility = View.GONE
            }

            "admin" -> {
                // Admin sees everything (for now do nothing)
            }

            else -> {
                // Unknown role → safest option
                Toast.makeText(this, "Unknown role", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── Click Listeners ────────────────────────────────────────────────────────
    private fun setClickListeners() {
        // ── Visitor Management (your screens — fully working) ──────────────────

        btnGoToVisitorApprove!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(this, VisitorApproveActivity::class.java)
            )
        }
        )

        btnGoToVisitorArrival!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(this, VisitorArrivalActivity::class.java)
            )
        }
        )

        // ── Quick Access (commented out until team merges) ─────────────────────
        btnVisitorAuth!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    VisitorAuthActivity::class.java
                )
            )
        } // startActivity(new Intent(this, VisitorActivity.class))
        )

        btnDelivery!!.setOnClickListener(View.OnClickListener { v: View? ->
            startActivity(
                Intent(
                    this,
                    DeliveryLogActivity::class.java
                )
            )
        } // startActivity(new Intent(this, DeliveryActivity.class))
        )

        btnStaff!!.setOnClickListener(View.OnClickListener { v: View? -> showComingSoon("Staff Entry") } // startActivity(new Intent(this, StaffActivity.class))
        )

        btnAlerts!!.setOnClickListener(View.OnClickListener { v: View? -> showComingSoon("Alerts") } // startActivity(new Intent(this, AlertsActivity.class))
        )

        btnResidents!!.setOnClickListener(View.OnClickListener { v: View? -> showComingSoon("Residents Directory") } // startActivity(new Intent(this, ResidentsActivity.class))
        )

        // ── New Buttons (placeholders — wire up when ready) ────────────────────
        btnNotices!!.setOnClickListener(View.OnClickListener { v: View? -> showComingSoon("Notices Board") }
        )

        btnMyProfile!!.setOnClickListener(View.OnClickListener { v: View? -> showComingSoon("My Profile") }
        )

        btnParking!!.setOnClickListener(View.OnClickListener { v: View? -> showComingSoon("Parking Management") }
        )

        btnSOS!!.setOnClickListener(View.OnClickListener { v: View? -> showSOSDialog() }
        )

        // ── Logout ─────────────────────────────────────────────────────────────
        btnLogout!!.setOnClickListener(View.OnClickListener { v: View? -> showLogoutDialog() })
    }

    // ── Helper: Coming Soon Toast ──────────────────────────────────────────────
    private fun showComingSoon(feature: String?) {
        Toast.makeText(this, feature + " — Coming Soon!", Toast.LENGTH_SHORT).show()
    }

    // ── Helper: Logout Confirmation Dialog ────────────────────────────────────
    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton(
                "Yes, Logout",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    auth.signOut()
                    val intent = Intent(this, VisitorApproveActivity::class.java)
                    intent.putExtra("role", userRole)
                    startActivity(intent)
                })
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ── Helper: SOS Emergency Dialog ──────────────────────────────────────────
    private fun showSOSDialog() {
        AlertDialog.Builder(this)
            .setTitle("🆘 Emergency SOS")
            .setMessage("This will alert the security guard immediately. Confirm?")
            .setPositiveButton(
                "Send Alert",
                DialogInterface.OnClickListener { dialog: DialogInterface?, which: Int ->
                    Toast.makeText(this, "🚨 SOS Alert Sent to Security!", Toast.LENGTH_LONG).show()
                })
            .setNegativeButton("Cancel", null)
            .show()
    }
}
