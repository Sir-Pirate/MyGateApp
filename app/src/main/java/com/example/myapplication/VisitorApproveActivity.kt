package com.example.myapplication

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import kotlin.jvm.java

/**
 * VisitorApproveActivity — Week 2, Screen 1
 *
 * Role: Resident
 * Purpose: Resident pre-approves a visitor by entering their name + phone.
 *          Calls VisitorManager.approveVisitor() built by M1.
 *
 * How it fits:
 *   HomeActivity --> (button) --> VisitorApproveActivity
 *
 * M1 Integration Point:
 *   VisitorManager.approveVisitor(visitorName, visitorPhone, residentId, note, onSuccess, onFailure)
 *   Un-comment the VisitorManager block once M1 pushes that file.
 */
class VisitorApproveActivity : AppCompatActivity() {

    // ── UI references ──────────────────────────────────────────────────────────
    private lateinit var tilVisitorName: TextInputLayout
    private lateinit var etVisitorName: TextInputEditText
    private lateinit var tilVisitorPhone: TextInputLayout
    private lateinit var etVisitorPhone: TextInputEditText
    private lateinit var tilVisitorNote: TextInputLayout
    private lateinit var etVisitorNote: TextInputEditText

    private lateinit var btnApproveVisitor: MaterialButton
    private lateinit var btnBackToHome: MaterialButton

    private lateinit var tvApproveStatus: TextView
    private lateinit var progressBar: ProgressBar

    // ── Firebase ───────────────────────────────────────────────────────────────
    private val auth = FirebaseAuth.getInstance()

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor_approve)

        bindViews()
        setClickListeners()
    }

    // ── View Binding ───────────────────────────────────────────────────────────
    private fun bindViews() {
        tilVisitorName    = findViewById(R.id.tilVisitorName)
        etVisitorName     = findViewById(R.id.etVisitorName)
        tilVisitorPhone   = findViewById(R.id.tilVisitorPhone)
        etVisitorPhone    = findViewById(R.id.etVisitorPhone)
        tilVisitorNote    = findViewById(R.id.tilVisitorNote)
        etVisitorNote     = findViewById(R.id.etVisitorNote)

        btnApproveVisitor = findViewById(R.id.btnApproveVisitor)
        btnBackToHome     = findViewById(R.id.btnBackToHome)

        tvApproveStatus   = findViewById(R.id.tvApproveStatus)
        progressBar       = findViewById(R.id.progressBarApprove)
    }

    // ── Click Listeners ────────────────────────────────────────────────────────
    private fun setClickListeners() {

        btnApproveVisitor.setOnClickListener {
            if (validateInputs()) {
                submitApproval()
            }
        }

        btnBackToHome.setOnClickListener {
            navigateToHome()
        }
    }

    // ── Input Validation ───────────────────────────────────────────────────────
    private fun validateInputs(): Boolean {
        var isValid = true

        val name  = etVisitorName.text.toString().trim()
        val phone = etVisitorPhone.text.toString().trim()

        // Clear previous errors
        tilVisitorName.error  = null
        tilVisitorPhone.error = null

        if (name.isEmpty()) {
            tilVisitorName.error = "Visitor name is required"
            isValid = false
        }

        if (phone.isEmpty()) {
            tilVisitorPhone.error = "Phone number is required"
            isValid = false
        } else if (phone.length != 10 || !phone.all { it.isDigit() }) {
            tilVisitorPhone.error = "Enter a valid 10-digit phone number"
            isValid = false
        }

        return isValid
    }

    // ── Core Logic: Submit Approval ────────────────────────────────────────────
    private fun submitApproval() {
        val visitorName  = etVisitorName.text.toString().trim()
        val visitorPhone = etVisitorPhone.text.toString().trim()
        val note         = etVisitorNote.text.toString().trim()

        // The currently logged-in resident's UID
        val residentId = auth.currentUser?.uid ?: run {
            showStatus("Error: Not logged in. Please log in again.", isError = true)
            return
        }

        showLoading(true)

        // ── M1 Integration ────────────────────────────────────────────────────
        // Un-comment this block once M1 pushes VisitorManager.kt
        //
        // VisitorManager.approveVisitor(
        //     visitorName  = visitorName,
        //     visitorPhone = visitorPhone,
        //     residentId   = residentId,
        //     note         = note,
        //     onSuccess    = {
        //         showLoading(false)
        //         showStatus("✓ Visitor pre-approved successfully!", isError = false)
        //         clearFields()
        //     },
        //     onFailure    = { errorMsg ->
        //         showLoading(false)
        //         showStatus("✗ Failed: $errorMsg", isError = true)
        //     }
        // )
        //
        // ── TEMP STUB (remove after M1 integration) ───────────────────────────
        // Simulates a successful approval so you can test UI flow now.
        android.os.Handler(mainLooper).postDelayed({
            showLoading(false)
            showStatus("✓ [STUB] Visitor pre-approved! (Connect VisitorManager.kt from M1)", isError = false)
            clearFields()
        }, 1200)
        // ── END STUB ──────────────────────────────────────────────────────────
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────────
    private fun showLoading(show: Boolean) {
        progressBar.visibility      = if (show) View.VISIBLE else View.GONE
        btnApproveVisitor.isEnabled = !show
        btnBackToHome.isEnabled     = !show
    }

    private fun showStatus(message: String, isError: Boolean) {
        tvApproveStatus.text       = message
        tvApproveStatus.setTextColor(if (isError) Color.parseColor("#B71C1C") else Color.parseColor("#1B5E20"))
        tvApproveStatus.setBackgroundColor(if (isError) Color.parseColor("#FFEBEE") else Color.parseColor("#E8F5E9"))
        tvApproveStatus.visibility = View.VISIBLE
    }

    private fun clearFields() {
        etVisitorName.text?.clear()
        etVisitorPhone.text?.clear()
        etVisitorNote.text?.clear()
    }

    // ── Navigation ─────────────────────────────────────────────────────────────
    private fun navigateToHome() {
        val intent = Intent(this, homeactivity::class.java)
        // Clear activity stack so Back button doesn't return here
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    // Prevent accidental back-press losing form data silently
    @SuppressLint("GestureBackNavigation")
    override fun onBackPressed() {
        super.onBackPressed()
        super.onBackPressed()
        navigateToHome()
    }
}
