package com.example.myapplication

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.databinding.ActivityLockerBinding
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.random.Random

class LockerActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLockerBinding

    private lateinit var lockerId: String
    private lateinit var otp: String

    private val firestore = FirebaseFirestore.getInstance()

    // Resident details
    private var residentId = ""
    private var residentName = ""
    private var flatNumber = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLockerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Receive courier name
        var courierName =
            intent.getStringExtra("courierName") ?: ""

        binding.etCourierName.setText(courierName)

        // =====================================
        // GENERATE LOCKER + FIND RESIDENT
        // =====================================
        binding.btnGenerate.setOnClickListener {

            courierName =
                binding.etCourierName.text.toString().trim()

            val mobileNumber =
                binding.etMobileNumber.text.toString().trim()

            // Validation
            if (
                courierName.isEmpty() ||
                mobileNumber.isEmpty()
            ) {

                Toast.makeText(
                    this,
                    "Enter courier and mobile number",
                    Toast.LENGTH_SHORT
                ).show()

                return@setOnClickListener
            }

            // =====================================
            // FIND RESIDENT USING MOBILE NUMBER
            // =====================================
            firestore.collection("users")
                .whereEqualTo("phone", mobileNumber)
                .get()
                .addOnSuccessListener { result ->

                    if (!result.isEmpty) {

                        val userDoc =
                            result.documents[0]

                        residentId =
                            userDoc.id

                        residentName =
                            userDoc.getString("name") ?: ""

                        flatNumber =
                            userDoc.getString("flatNo") ?: ""

                        // Show resident details
                        binding.tvResidentInfo.visibility =
                            View.VISIBLE

                        binding.tvResidentInfo.text =
                            "Resident: $residentName\nFlat: $flatNumber"

                        // Generate locker values
                        lockerId = generateLockerId()
                        otp = generateOtp()

                        binding.etLockerId.setText(lockerId)
                        binding.etOtp.setText(otp)

                        // Enable Store button
                        binding.btnStore.isEnabled = true

                        Toast.makeText(
                            this,
                            "Locker & OTP Generated",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        Toast.makeText(
                            this,
                            "Resident not found",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

                .addOnFailureListener { e ->

                    Toast.makeText(
                        this,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // =====================================
        // STORE PACKAGE
        // =====================================
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

            val courierName =
                binding.etCourierName.text.toString().trim()

            // =====================================
            // LOCKER DATA
            // =====================================
            val lockerData = hashMapOf(

                "courierName" to courierName,

                "residentId" to residentId,

                "residentName" to residentName,

                "flatNumber" to flatNumber,

                "lockerId" to lockerId,

                "otp" to otp,

                "status" to "stored",

                "storedAt" to System.currentTimeMillis(),

                // 6 hour expiry
                "expiresAt" to (
                        System.currentTimeMillis() +
                                (6 * 60 * 60 * 1000)
                        )
            )

            // =====================================
            // SAVE LOCKER
            // =====================================
            firestore.collection("lockers")
                .add(lockerData)
                .addOnSuccessListener {

                    // =====================================
                    // CREATE ALERT
                    // =====================================
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

                    // =====================================
                    // SUCCESS UI
                    // =====================================
                    binding.tvStatus.text =
                        "✅ Package Stored\n\n" +
                                "Resident: $residentName\n" +
                                "Flat: $flatNumber\n" +
                                "Locker: $lockerId\n" +
                                "OTP: $otp"

                    Toast.makeText(
                        this@LockerActivity,
                        "Package Stored Successfully",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Disable after storing
                    binding.btnStore.isEnabled = false
                }

                .addOnFailureListener { e ->

                    Toast.makeText(
                        this@LockerActivity,
                        "Error: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }

        // =====================================
        // BACK BUTTON
        // =====================================
        binding.btnBack.setOnClickListener {
            finish()
        }
    }

    // =====================================
    // GENERATE LOCKER ID
    // =====================================
    private fun generateLockerId(): String {

        val number =
            Random.nextInt(100, 999)

        return "L-$number"
    }

    // =====================================
    // GENERATE OTP
    // =====================================
    private fun generateOtp(): String {

        return Random
            .nextInt(1000, 9999)
            .toString()
    }
}