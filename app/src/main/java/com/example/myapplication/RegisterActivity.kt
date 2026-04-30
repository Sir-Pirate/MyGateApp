package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
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

    private lateinit var layoutFlatNo: LinearLayout
    private lateinit var layoutTower: LinearLayout

    private lateinit var buttonRegister: Button
    private lateinit var buttonGoToLogin: Button
    private lateinit var spinnerRole: Spinner

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        initializeViews()
        setupRoleSpinner()
        setupRegisterButton()
        setupLoginRedirect()
    }

    private fun initializeViews() {
        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextFlatNo = findViewById(R.id.editTextFlatNo)
        editTextTower = findViewById(R.id.editTextTower)

        layoutFlatNo = findViewById(R.id.layoutFlatNo)
        layoutTower = findViewById(R.id.layoutTower)

        buttonRegister = findViewById(R.id.buttonRegister)
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin)
        spinnerRole = findViewById(R.id.spinnerRole)
    }

    private fun setupRoleSpinner() {

        val roles = arrayOf("resident", "guard", "admin")

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
                        spinnerRole.selectedItem.toString().lowercase()

                    if (selectedRole == "resident") {
                        layoutFlatNo.visibility = View.VISIBLE
                        layoutTower.visibility = View.VISIBLE
                    } else {
                        layoutFlatNo.visibility = View.GONE
                        layoutTower.visibility = View.GONE
                    }
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {}
            }
    }

    private fun setupRegisterButton() {

        buttonRegister.setOnClickListener {

            val name = editTextName.text.toString().trim()
            val email = editTextEmail.text.toString().trim()
            val phone = editTextPhone.text.toString().trim()
            val password = editTextPassword.text.toString().trim()
            val flatNo = editTextFlatNo.text.toString().trim()
            val tower = editTextTower.text.toString().trim()
            val role = spinnerRole.selectedItem.toString().trim().lowercase()

            if (name.isEmpty()) {
                Toast.makeText(this, "Enter your full name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length != 10 || !phone.matches("[0-9]+".toRegex())) {
                Toast.makeText(
                    this,
                    "Enter valid 10-digit phone number",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(
                    this,
                    "Password must be at least 6 characters",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (role == "resident") {

                if (flatNo.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter your flat number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }

                if (tower.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter your tower number",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            val residentFlatNo =
                if (role == "resident") flatNo else ""

            val residentTower =
                if (role == "resident") tower else ""

            registerUser(
                email = email,
                password = password,
                name = name,
                phone = phone,
                role = role,
                flatNo = residentFlatNo,
                tower = residentTower,

                onSuccess = {

                    Toast.makeText(
                        this,
                        "Registration Successful!",
                        Toast.LENGTH_SHORT
                    ).show()

                    val destination = when (role) {
                        "admin" -> HomeActivity::class.java
                        "guard" -> GuardHomeActivity::class.java
                        else -> ResidentHomeActivity::class.java
                    }

                    val intent = Intent(this, destination)
                    intent.putExtra("role", role)

                    startActivity(intent)
                    finish()
                },

                onError = { errorMessage ->

                    Toast.makeText(
                        this,
                        errorMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            )
        }
    }

    private fun setupLoginRedirect() {

        buttonGoToLogin.setOnClickListener {

            val intent = Intent(
                this,
                MainActivity::class.java
            )

            startActivity(intent)
            finish()
        }
    }
}