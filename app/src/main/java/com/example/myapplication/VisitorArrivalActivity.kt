package com.example.myapplication

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

/**
 * VisitorArrivalActivity — Week 2, Screen 2
 *
 * Role: Guard
 * Purpose: Guard types a visitor's phone number → looks up their pre-approval →
 *          sees visitor details → marks them as arrived.
 *          Calls VisitorManager from M1 for both lookup and marking arrival.
 *
 * How it fits:
 *   HomeActivity --> (button) --> VisitorArrivalActivity
 *
 * M1 Integration Points:
 *   1. VisitorManager.getVisitorByPhone(phone, onSuccess, onFailure)
 *   2. VisitorManager.markVisitorArrived(visitorId, onSuccess, onFailure)
 *   Un-comment the VisitorManager blocks once M1 pushes that file.
 */
class VisitorArrivalActivity : AppCompatActivity() {

    // ── UI references ──────────────────────────────────────────────────────────
    private lateinit var tilArrivalPhone: TextInputLayout
    private lateinit var etArrivalPhone: TextInputEditText

    private lateinit var btnCheckApproval: MaterialButton
    private lateinit var btnMarkArrival: MaterialButton
    private lateinit var btnArrivalBackToHome: MaterialButton

    private lateinit var cardVisitorResult: CardView
    private lateinit var tvFoundVisitorName: TextView
    private lateinit var tvApprovalStatus: TextView
    private lateinit var tvApprovedBy: TextView

    private lateinit var tvArrivalStatus: TextView
    private lateinit var progressBar: ProgressBar

    // Holds the Firestore document ID of the found visitor (set after lookup)
    private var foundVisitorId: String? = null

    // ── Lifecycle ──────────────────────────────────────────────────────────────
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_visitor_arrival)

        bindViews()
        setClickListeners()
    }

    // ── View Binding ───────────────────────────────────────────────────────────
    private fun bindViews() {
        tilArrivalPhone      = findViewById(R.id.tilArrivalPhone)
        etArrivalPhone       = findViewById(R.id.etArrivalPhone)

        btnCheckApproval     = findViewById(R.id.btnCheckApproval)
        btnMarkArrival       = findViewById(R.id.btnMarkArrival)
        btnArrivalBackToHome = findViewById(R.id.btnArrivalBackToHome)

        cardVisitorResult    = findViewById(R.id.cardVisitorResult)
        tvFoundVisitorName   = findViewById(R.id.tvFoundVisitorName)
        tvApprovalStatus     = findViewById(R.id.tvApprovalStatus)
        tvApprovedBy         = findViewById(R.id.tvApprovedBy)

        tvArrivalStatus      = findViewById(R.id.tvArrivalStatus)
        progressBar          = findViewById(R.id.progressBarArrival)
    }

    // ── Click Listeners ────────────────────────────────────────────────────────
    private fun setClickListeners() {

        btnCheckApproval.setOnClickListener {
            val phone = etArrivalPhone.text.toString().trim()
            if (validatePhone(phone)) {
                lookupVisitor(phone)
            }
        }

        btnMarkArrival.setOnClickListener {
            val visitorId = foundVisitorId
            if (visitorId != null) {
                markArrived(visitorId)
            } else {
                showStatus("No visitor selected. Please search first.", isError = true)
            }
        }

        btnArrivalBackToHome.setOnClickListener {
            navigateToHome()
        }
    }

    // ── Validation ─────────────────────────────────────────────────────────────
    private fun validatePhone(phone: String): Boolean {
        tilArrivalPhone.error = null
        return when {
            phone.isEmpty()                          -> { tilArrivalPhone.error = "Phone number required"; false }
            phone.length != 10 || !phone.all { it.isDigit() } -> { tilArrivalPhone.error = "Enter a valid 10-digit number"; false }
            else                                     -> true
        }
    }

    // ── Step 1: Look Up Visitor by Phone ───────────────────────────────────────
    private fun lookupVisitor(phone: String) {
        showLoading(true)
        hideResultCard()
        hideStatus()

        // ── M1 Integration ────────────────────────────────────────────────────
        // Un-comment once M1 pushes VisitorManager.kt.
        // The onSuccess lambda receives a VisitorData object — adjust field
        // names (visitor.name, visitor.id, visitor.approvedByName) to match
        // whatever data class M1 defines.
        //
        // VisitorManager.getVisitorByPhone(
        //     phone     = phone,
        //     onSuccess = { visitor ->
        //         showLoading(false)
        //         foundVisitorId = visitor.id                 // save for Step 2
        //         populateResultCard(
        //             name        = visitor.name,
        //             isApproved  = visitor.isApproved,
        //             approvedBy  = visitor.approvedByName ?: "Unknown"
        //         )
        //     },
        //     onFailure = { errorMsg ->
        //         showLoading(false)
        //         showStatus("✗ $errorMsg", isError = true)
        //     }
        // )
        //
        // ── TEMP STUB ─────────────────────────────────────────────────────────
        android.os.Handler(mainLooper).postDelayed({
            showLoading(false)
            // Simulate a found + approved visitor
            foundVisitorId = "STUB_VISITOR_ID_001"
            populateResultCard(
                name       = "Ravi Kumar (STUB)",
                isApproved = true,
                approvedBy = "Flat 4B - Meena"
            )
        }, 1000)
        // ── END STUB ──────────────────────────────────────────────────────────
    }

    // ── Step 2: Mark Visitor as Arrived ───────────────────────────────────────
    private fun markArrived(visitorId: String) {
        showLoading(true)

        // ── M1 Integration ────────────────────────────────────────────────────
        // VisitorManager.markVisitorArrived(
        //     visitorId = visitorId,
        //     onSuccess = {
        //         showLoading(false)
        //         showStatus("✓ Visitor marked as arrived. Entry logged!", isError = false)
        //         btnMarkArrival.isEnabled = false   // prevent double-tap
        //     },
        //     onFailure = { errorMsg ->
        //         showLoading(false)
        //         showStatus("✗ Could not log arrival: $errorMsg", isError = true)
        //     }
        // )
        //
        // ── TEMP STUB ─────────────────────────────────────────────────────────
        android.os.Handler(mainLooper).postDelayed({
            showLoading(false)
            showStatus("✓ [STUB] Visitor marked arrived! (Connect VisitorManager.kt from M1)", isError = false)
            btnMarkArrival.isEnabled = false
        }, 1000)
        // ── END STUB ──────────────────────────────────────────────────────────
    }

    // ── UI Helpers ─────────────────────────────────────────────────────────────
    private fun populateResultCard(name: String, isApproved: Boolean, approvedBy: String) {
        tvFoundVisitorName.text = name
        tvApprovedBy.text       = approvedBy

        if (isApproved) {
            tvApprovalStatus.text      = "✓ Pre-Approved"
            tvApprovalStatus.setTextColor(Color.parseColor("#1B5E20"))
            btnMarkArrival.isEnabled   = true
        } else {
            tvApprovalStatus.text      = "✗ Not Approved"
            tvApprovalStatus.setTextColor(Color.parseColor("#B71C1C"))
            btnMarkArrival.isEnabled   = false
            showStatus("This visitor has not been pre-approved. Contact the resident.", isError = true)
        }

        cardVisitorResult.visibility = View.VISIBLE
    }

    private fun hideResultCard() {
        cardVisitorResult.visibility = View.GONE
        foundVisitorId = null
    }

    private fun showLoading(show: Boolean) {
        progressBar.visibility       = if (show) View.VISIBLE else View.GONE
        btnCheckApproval.isEnabled   = !show
        btnArrivalBackToHome.isEnabled = !show
    }

    private fun showStatus(message: String, isError: Boolean) {
        tvArrivalStatus.text       = message
        tvArrivalStatus.setTextColor(if (isError) Color.parseColor("#B71C1C") else Color.parseColor("#1B5E20"))
        tvArrivalStatus.setBackgroundColor(if (isError) Color.parseColor("#FFEBEE") else Color.parseColor("#E8F5E9"))
        tvArrivalStatus.visibility = View.VISIBLE
    }

    private fun hideStatus() {
        tvArrivalStatus.visibility = View.GONE
    }

    // ── Navigation ─────────────────────────────────────────────────────────────
    private fun navigateToHome() {
        val intent = Intent(this, homeactivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        super.onBackPressed()
        navigateToHome()
    }
}
