package com.example.myapplication

import android.util.Patterns
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.AuthManager.registerUser

class RegisterActivity : AppCompatActivity() {

    private var editTextName: EditText? = null
    private var editTextEmail: EditText? = null
    private var editTextPhone: EditText? = null
    private var editTextPassword: EditText? = null
    private var editTextFlatNo: EditText? = null
    private var editTextTower: EditText? = null
    private var buttonRegister: Button? = null
    private var buttonGoToLogin: Button? = null
    private var spinnerRole: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextName = findViewById(R.id.editTextName)
        editTextEmail = findViewById(R.id.editTextEmail)
        editTextPhone = findViewById(R.id.editTextPhone)
        editTextPassword = findViewById(R.id.editTextPassword)
        editTextFlatNo = findViewById(R.id.editTextFlatNo)
        editTextTower = findViewById(R.id.editTextTower)
        buttonRegister = findViewById(R.id.buttonRegister)
        buttonGoToLogin = findViewById(R.id.buttonGoToLogin)
        spinnerRole = findViewById(R.id.spinnerRole)
        editTextFlatNo?.visibility = android.view.View.GONE
        editTextTower?.visibility = android.view.View.GONE

        // Role dropdown
        val roles = arrayOf("resident", "guard", "admin")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, roles)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole!!.adapter = adapter
        spinnerRole!!.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {

                    val selectedRole =
                        parent.getItemAtPosition(position).toString()

                    if (selectedRole == "resident") {

                        editTextFlatNo?.visibility = android.view.View.VISIBLE
                        editTextTower?.visibility = android.view.View.VISIBLE

                    } else {

                        editTextFlatNo?.visibility = android.view.View.GONE
                        editTextTower?.visibility = android.view.View.GONE
                    }
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {}
            }

        // ✅ Register button
        buttonRegister!!.setOnClickListener {

            val name = editTextName!!.text.toString().trim()
            val email = editTextEmail!!.text.toString().trim()
            val phone = editTextPhone!!.text.toString().trim()
            val password = editTextPassword!!.text.toString().trim()
            val role = spinnerRole!!.selectedItem.toString().trim().lowercase()
            val flatNo = editTextFlatNo!!.text.toString().trim()
            val tower = editTextTower!!.text.toString().trim()



            // ✅ Validations
            if (name.isEmpty()) {
                Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(this, "Enter valid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (phone.length != 10 || !phone.matches("[0-9]+".toRegex())) {
                Toast.makeText(this, "Enter valid 10-digit phone number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 6) {
                Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (role == "resident") {

                if (flatNo.isEmpty() || tower.isEmpty()) {
                    Toast.makeText(
                        this,
                        "Enter Flat and Tower",
                        Toast.LENGTH_SHORT
                    ).show()

                    return@setOnClickListener
                }
            }

            // ✅ Register user
            registerUser(
                email,
                password,
                name,
                phone,
                role,
                flatNo,
                tower,

                {
                    runOnUiThread {
                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()

                        val intent = Intent(this, HomeActivity::class.java)
                        intent.putExtra("role", role)
                        startActivity(intent)
                        finish()
                    }
                },
                { errorMessage ->
                    runOnUiThread {
                        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            )
        }

        // ✅ Login button
        buttonGoToLogin!!.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}