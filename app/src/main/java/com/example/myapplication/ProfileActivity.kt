package com.example.myapplication

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.activity.enableEdgeToEdge
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    // Header
    private lateinit var btnBack: LinearLayout
    private lateinit var cardSave: CardView
    private lateinit var btnSaveProfile: TextView

    // Avatar + name
    private lateinit var tvProfileInitial: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileRoleBadge: TextView

    // Personal info fields
    private lateinit var etName: EditText
    private lateinit var etPhone: EditText
    private lateinit var tvEmail: TextView

    // Flat section (resident only)
    private lateinit var tvFlatSection: TextView
    private lateinit var cardFlatDetails: CardView
    private lateinit var tvFlatNo: TextView
    private lateinit var tvTower: TextView

    // Account rows
    private lateinit var rowEditProfile: LinearLayout
    private lateinit var rowChangePassword: LinearLayout
    private lateinit var rowLogout: LinearLayout

    // Firebase
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    // State
    private var isEditMode = false
    private var userRole = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_profile)

        ViewCompat.setOnApplyWindowInsetsListener(
            findViewById(android.R.id.content)
        ) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        bindViews()
        loadProfile()
        setupClickListeners()
    }

    // ─────────────────────────────────────
    // Bind Views
    // ─────────────────────────────────────
    private fun bindViews() {

        btnBack            = findViewById(R.id.btnBack)
        cardSave           = findViewById(R.id.cardSave)
        btnSaveProfile     = findViewById(R.id.btnSaveProfile)

        tvProfileInitial   = findViewById(R.id.tvProfileInitial)
        tvProfileName      = findViewById(R.id.tvProfileName)
        tvProfileRoleBadge = findViewById(R.id.tvProfileRoleBadge)

        etName             = findViewById(R.id.etName)
        etPhone            = findViewById(R.id.etPhone)
        tvEmail            = findViewById(R.id.tvEmail)

        tvFlatSection      = findViewById(R.id.tvFlatSection)
        cardFlatDetails    = findViewById(R.id.cardFlatDetails)
        tvFlatNo           = findViewById(R.id.tvFlatNo)
        tvTower            = findViewById(R.id.tvTower)

        rowEditProfile     = findViewById(R.id.rowEditProfile)
        rowChangePassword  = findViewById(R.id.rowChangePassword)
        rowLogout          = findViewById(R.id.rowLogout)
    }

    // ─────────────────────────────────────
    // Load profile from Firestore
    // ─────────────────────────────────────
    private fun loadProfile() {

        val currentUser = auth.currentUser ?: run {
            goToLogin()
            return
        }

        // Show email immediately from Auth (no Firestore needed)
        tvEmail.text = currentUser.email ?: ""

        firestore.collection("users")
            .document(currentUser.uid)
            .get()
            .addOnSuccessListener { doc ->

                if (!doc.exists()) {
                    showToast("Profile not found")
                    return@addOnSuccessListener
                }

                val name   = doc.getString("name")   ?: ""
                val phone  = doc.getString("phone")  ?: ""
                val role   = doc.getString("role")   ?: "resident"
                val flatNo = doc.getString("flatNo") ?: ""
                val tower  = doc.getString("tower")  ?: ""

                userRole = role

                // Avatar initial
                val initial = name.firstOrNull()
                    ?.uppercaseChar()?.toString() ?: "?"
                tvProfileInitial.text = initial

                // Name + role badge
                tvProfileName.text = name
                tvProfileRoleBadge.text = role.replaceFirstChar {
                    it.uppercaseChar()
                }

                // Editable fields
                etName.setText(name)
                etPhone.setText(phone)

                // Show flat section for residents only
                if (role == "resident") {
                    tvFlatSection.visibility = View.VISIBLE
                    cardFlatDetails.visibility = View.VISIBLE
                    tvFlatNo.text = flatNo.ifEmpty { "—" }
                    tvTower.text  = tower.ifEmpty { "—" }
                }
            }
            .addOnFailureListener {
                showToast("Failed to load profile")
            }
    }

    // ─────────────────────────────────────
    // Click Listeners
    // ─────────────────────────────────────
    private fun setupClickListeners() {

        btnBack.setOnClickListener {
            if (isEditMode) {
                // Cancel edit — restore view mode
                exitEditMode()
            } else {
                finish()
            }
        }

        // Edit Profile — toggles edit mode
        rowEditProfile.setOnClickListener {
            enterEditMode()
        }

        // Save button (top right, visible in edit mode)
        btnSaveProfile.setOnClickListener {
            saveProfile()
        }

        // Change Password
        rowChangePassword.setOnClickListener {
            showChangePasswordDialog()
        }

        // Logout
        rowLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    // ─────────────────────────────────────
    // Enter Edit Mode
    // ─────────────────────────────────────
    private fun enterEditMode() {

        isEditMode = true

        // Enable name + phone fields
        etName.isEnabled  = true
        etPhone.isEnabled = true

        // Give them a subtle visual cue — underline
        etName.setBackgroundResource(
            android.R.drawable.edit_text
        )
        etPhone.setBackgroundResource(
            android.R.drawable.edit_text
        )

        // Show save button, hide edit row
        cardSave.visibility       = View.VISIBLE
        rowEditProfile.visibility = View.GONE

        etName.requestFocus()
    }

    // ─────────────────────────────────────
    // Exit Edit Mode (cancel)
    // ─────────────────────────────────────
    private fun exitEditMode() {

        isEditMode = false

        etName.isEnabled  = false
        etPhone.isEnabled = false

        etName.setBackgroundResource(android.R.color.transparent)
        etPhone.setBackgroundResource(android.R.color.transparent)

        cardSave.visibility       = View.GONE
        rowEditProfile.visibility = View.VISIBLE

        // Reload original values from Firestore
        loadProfile()
    }

    // ─────────────────────────────────────
    // Save Profile to Firestore
    // ─────────────────────────────────────
    private fun saveProfile() {

        val newName  = etName.text.toString().trim()
        val newPhone = etPhone.text.toString().trim()

        // Validate
        if (newName.isEmpty()) {
            showToast("Name cannot be empty")
            return
        }

        if (newPhone.length != 10 || !newPhone.matches("[0-9]+".toRegex())) {
            showToast("Enter a valid 10-digit phone number")
            return
        }

        val currentUser = auth.currentUser ?: return

        // Check phone isn't taken by someone else
        firestore.collection("users")
            .whereEqualTo("phone", newPhone)
            .get()
            .addOnSuccessListener { docs ->

                val conflict = docs.documents.any {
                    it.id != currentUser.uid
                }

                if (conflict) {
                    showToast("Phone number already in use")
                    return@addOnSuccessListener
                }

                // Update Firestore
                firestore.collection("users")
                    .document(currentUser.uid)
                    .update(
                        mapOf(
                            "name"  to newName,
                            "phone" to newPhone
                        )
                    )
                    .addOnSuccessListener {

                        showToast("Profile updated successfully")

                        // Refresh display
                        tvProfileName.text = newName
                        val initial = newName.firstOrNull()
                            ?.uppercaseChar()?.toString() ?: "?"
                        tvProfileInitial.text = initial

                        exitEditMode()
                    }
                    .addOnFailureListener {
                        showToast("Update failed. Try again.")
                    }
            }
            .addOnFailureListener {
                showToast("Could not verify phone. Try again.")
            }
    }

    // ─────────────────────────────────────
    // Change Password Dialog
    // ─────────────────────────────────────
    private fun showChangePasswordDialog() {

        val dialogView = layoutInflater.inflate(
            R.layout.dialog_change_password,
            null
        )

        val etCurrentPassword = dialogView
            .findViewById<EditText>(R.id.etCurrentPassword)

        val etNewPassword = dialogView
            .findViewById<EditText>(R.id.etNewPassword)

        val etConfirmPassword = dialogView
            .findViewById<EditText>(R.id.etConfirmPassword)

        AlertDialog.Builder(this)
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->

                val current = etCurrentPassword.text.toString().trim()
                val newPass = etNewPassword.text.toString().trim()
                val confirm = etConfirmPassword.text.toString().trim()

                when {
                    current.isEmpty() ->
                        showToast("Enter current password")

                    newPass.length < 6 ->
                        showToast("New password must be at least 6 characters")

                    newPass != confirm ->
                        showToast("Passwords do not match")

                    else ->
                        reauthAndChangePassword(current, newPass)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    // ─────────────────────────────────────
    // Re-authenticate then change password
    // ─────────────────────────────────────
    private fun reauthAndChangePassword(
        currentPassword: String,
        newPassword: String
    ) {

        val user  = auth.currentUser ?: return
        val email = user.email ?: return

        val credential = EmailAuthProvider
            .getCredential(email, currentPassword)

        // Step 1: Re-authenticate
        user.reauthenticate(credential)
            .addOnSuccessListener {

                // Step 2: Update password
                user.updatePassword(newPassword)
                    .addOnSuccessListener {
                        showToast("Password changed successfully")
                    }
                    .addOnFailureListener {
                        showToast("Failed to update password")
                    }
            }
            .addOnFailureListener {
                showToast("Current password is incorrect")
            }
    }

    // ─────────────────────────────────────
    // Logout Dialog
    // ─────────────────────────────────────
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

    // ─────────────────────────────────────
    // Navigate to Login
    // ─────────────────────────────────────
    private fun goToLogin() {

        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                       Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    // ─────────────────────────────────────
    // Toast helper
    // ─────────────────────────────────────
    private fun showToast(msg: String) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
    }
}
