package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.AuthManager.registerUser

class RegisterActivity : AppCompatActivity() {

    private lateinit var editTextName: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var editTextPhone: EditText
    private lateinit var editTextPassword: EditText

    private lateinit var editTextFlatNo: EditText
    private lateinit var editTextTower: EditText

    private lateinit var buttonRegister: Button
    private lateinit var buttonGoToLogin: Button

    private lateinit var spinnerRole: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)

        bindViews()
        setupRoleSpinner()
        setupButtons()
    }

    // =====================================
    // Bind Views
    // =====================================
    private fun bindViews() {

        editTextName =
            findViewById(R.id.editTextName)

        editTextEmail =
            findViewById(R.id.editTextEmail)

        editTextPhone =
            findViewById(R.id.editTextPhone)

        editTextPassword =
            findViewById(R.id.editTextPassword)

        editTextFlatNo =
            findViewById(R.id.editTextFlatNo)

        editTextTower =
            findViewById(R.id.editTextTower)

        buttonRegister =
            findViewById(R.id.buttonRegister)

        buttonGoToLogin =
            findViewById(R.id.buttonGoToLogin)

        spinnerRole =
            findViewById(R.id.spinnerRole)

        // Hide resident fields initially
        editTextFlatNo.visibility = View.GONE
        editTextTower.visibility = View.GONE
    }

    // =====================================
    // Role Spinner
    // =====================================
    private fun setupRoleSpinner() {

        val roles = arrayOf(
            "resident",
            "guard",
            "admin"
        )

        val adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            roles
        )

        adapter.setDropDownViewResource(
            android.R.layout.simple_spinner_dropdown_item
        )

        spinnerRole.adapter = adapter

        spinnerRole.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {

                    val selectedRole =
                        roles[position]

                    if (selectedRole == "resident") {

                        editTextFlatNo.visibility =
                            View.VISIBLE

                        editTextTower.visibility =
                            View.VISIBLE

                    } else {

                        editTextFlatNo.visibility =
                            View.GONE

                        editTextTower.visibility =
                            View.GONE
                    }
                }

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {
                }
            }
    }

    // =====================================
    // Buttons
    // =====================================
    private fun setupButtons() {

        // Register
        buttonRegister.setOnClickListener {

            registerNewUser()
        }

        // Login
        buttonGoToLogin.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    MainActivity::class.java
                )
            )

            finish()
        }
    }

    // =====================================
    // Register Logic
    // =====================================
    private fun registerNewUser() {

        val name =
            editTextName.text.toString().trim()

        val email =
            editTextEmail.text.toString().trim()

        val phone =
            editTextPhone.text.toString().trim()

        val password =
            editTextPassword.text.toString().trim()

        val role =
            spinnerRole.selectedItem
                .toString()
                .trim()
                .lowercase()

        val flatNo =
            editTextFlatNo.text.toString().trim()

        val tower =
            editTextTower.text.toString().trim()

        // =====================================
        // Validations
        // =====================================

        if (name.isEmpty()) {

            showToast("Enter your name")
            return
        }

        if (
            !Patterns.EMAIL_ADDRESS
                .matcher(email)
                .matches()
        ) {

            showToast("Enter valid email")
            return
        }

        if (
            phone.length != 10 ||
            !phone.matches("[0-9]+".toRegex())
        ) {

            showToast("Enter valid phone number")
            return
        }

        if (password.length < 6) {

            showToast(
                "Password must be at least 6 characters"
            )

            return
        }

        // =====================================
        // Resident Validation
        // =====================================

        var fullFlatNo = ""

        if (role == "resident") {

            if (flatNo.isEmpty()) {

                showToast("Enter flat number")
                return
            }

            if (tower.isEmpty()) {

                showToast("Enter tower")
                return
            }

            // FINAL FLAT FORMAT
            // Example: A-101
            fullFlatNo =
                tower.uppercase() +
                        "-" +
                        flatNo
        }

        // =====================================
        // Register User
        // =====================================

        registerUser(
            email,
            password,
            name,
            phone,
            role,

            // Save combined flat number
            fullFlatNo,

            tower,

            // Success
            {

                runOnUiThread {

                    showToast(
                        "Registration Successful!"
                    )

                    val intent = Intent(
                        this,
                        HomeActivity::class.java
                    )

                    intent.putExtra(
                        "role",
                        role
                    )

                    startActivity(intent)

                    finish()
                }
            },

            // Error
            { errorMessage ->

                runOnUiThread {

                    showToast(errorMessage)
                }
            }
        )
    }

    // =====================================
    // Toast Helper
    // =====================================
    private fun showToast(message: String) {

        Toast.makeText(
            this,
            message,
            Toast.LENGTH_SHORT
        ).show()
    }
}