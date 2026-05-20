package com.example.myapplication

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLockerBinding
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

class LockerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockerBinding

    private lateinit var lockerId: String
    private lateinit var otp: String

    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLockerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive data from DeliveryActivity
        var courierName =
            intent.getStringExtra("courierName") ?: ""

        var flatNumber =
            intent.getStringExtra("flatNumber") ?: ""

        // Autofill fields
        binding.etCourierName.setText(courierName)
        binding.etFlatNumber.setText(flatNumber)

        // =========================
        // GENERATE LOCKER + OTP
        // =========================
        binding.btnGenerate.setOnClickListener {

            courierName =
                binding.etCourierName.text.toString().trim()

            flatNumber =
                binding.etFlatNumber.text.toString().trim()

            // Validation
            if (
                courierName.isEmpty() ||
                flatNumber.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Enter courier and flat number",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Generate values
            lockerId = generateLockerId()
            otp = generateOtp()

            // Show values
            binding.etLockerId.setText(lockerId)
            binding.etOtp.setText(otp)

            // Enable Store button
            binding.btnStore.isEnabled = true

            Toast.makeText(
                this,
                "Locker & OTP Generated",
                Toast.LENGTH_SHORT
            ).show()
        }

        // =========================
        // STORE PACKAGE
        // =========================
        binding.btnStore.setOnClickListener {

            // Safety check
            if (
                !::lockerId.isInitialized ||
                !::otp.isInitialized
            ) {

                Toast.makeText(
                    this,
                    "Generate Locker & OTP first",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // Locker Data
            val lockerData = hashMapOf(
                "courierName" to courierName,
                "flatNumber" to flatNumber,
                "lockerId" to lockerId,
                "otp" to otp,
                "status" to "stored",
                "storedAt" to System.currentTimeMillis()
            )

            // Save Locker
            firestore.collection("lockers")
                .add(lockerData)
                .addOnSuccessListener {

                    // =========================
                    // FIND RESIDENT BY FLAT
                    // =========================
                    firestore.collection("users")
                        .whereEqualTo("flatNo", flatNumber)
                        .get()
                        .addOnSuccessListener { result ->

                            if (!result.isEmpty) {

                                // Resident UID
                                val residentId =
                                    result.documents[0].id

                                // =========================
                                // CREATE ALERT
                                // =========================
                                val alertData = hashMapOf(
                                    "title" to "📦 Delivery Stored",
                                    "message" to
                                            "Your package from $courierName " +
                                            "is stored in Locker $lockerId. " +
                                            "OTP: $otp",

                                    "flatNo" to flatNumber,
                                    "residentId" to residentId,
                                    "type" to "locker",
                                    "read" to false,
                                    "createdAt" to System.currentTimeMillis()
                                )

                                firestore.collection("alerts")
                                    .add(alertData)
                            }

                            // Success UI
                            binding.tvStatus.text =
                                "✅ Package Stored\n\n" +
                                        "Locker: $lockerId\n" +
                                        "OTP: $otp"

                            Toast.makeText(
                                this@LockerActivity,
                                "Package Stored Successfully",
                                Toast.LENGTH_SHORT
                            ).show()

                            // Disable button after storing
                            binding.btnStore.isEnabled = false
                        }
                }

                .addOnFailureListener { e ->

                    Toast.makeText(
                        this@LockerActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // =========================
        // BACK BUTTON
        // =========================
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    // =========================
    // GENERATE LOCKER ID
    // =========================
    private fun generateLockerId(): String {

        val number =
            Random.nextInt(100, 999)

        return "L-$number"
    }

    // =========================
    // GENERATE OTP
    // =========================
    private fun generateOtp(): String {

        return Random
            .nextInt(1000, 9999)
            .toString()
    }

    // =========================
    // FORMAT TIME
    // =========================
    private fun getCurrentTime(): String {

        return SimpleDateFormat(
            "dd MMM yyyy, hh:mm a",
            Locale.getDefault()
        ).format(Date())
    }
}