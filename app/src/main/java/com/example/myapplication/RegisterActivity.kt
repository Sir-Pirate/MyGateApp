package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
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
    private var buttonRegister: Button? = null
    private var buttonGoToLogin: Button? = null
    private var spinnerRole: Spinner? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        editTextName = findViewById<EditText>(R.id.editTextName)
        editTextEmail = findViewById<EditText>(R.id.editTextEmail)
        editTextPhone = findViewById<EditText>(R.id.editTextPhone)
        editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        buttonRegister = findViewById<Button>(R.id.buttonRegister)
        buttonGoToLogin = findViewById<Button>(R.id.buttonGoToLogin)
        spinnerRole = findViewById<Spinner>(R.id.spinnerRole)

        // Role dropdown
        val roles = arrayOf<String?>("resident", "guard", "admin")
        val adapter = ArrayAdapter<String?>(
            this,
            android.R.layout.simple_spinner_item,
            roles
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRole!!.setAdapter(adapter)

        // Register button
        buttonRegister!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val name = editTextName!!.getText().toString().trim { it <= ' ' }
                val email = editTextEmail!!.getText().toString().trim { it <= ' ' }
                val phone = editTextPhone!!.getText().toString().trim { it <= ' ' }
                val password = editTextPassword!!.getText().toString().trim { it <= ' ' }
                val role = spinnerRole!!.getSelectedItem().toString()

                if (name.isEmpty() || email.isEmpty() ||
                    phone.isEmpty() || password.isEmpty()
                ) {
                    Toast.makeText(
                        this@RegisterActivity,
                        "Please fill all fields",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                registerUser(
                    email,
                    password,
                    name,
                    role,
                    phone,
                    {
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                this@RegisterActivity,
                                "Registration Successful!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // ← Navigate to Home Screen after registration
                            val intent = Intent(this@RegisterActivity, HomeActivity::class.java)
                            startActivity(intent)
                            finish()
                        })
                        null
                    },
                    { errorMessage: String? ->
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                this@RegisterActivity,
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                        null
                    }
                )
            }
        })

        // Go to login button
        buttonGoToLogin!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(this@RegisterActivity, mainactivity::class.java)
                startActivity(intent)
                finish()
            }
        })
    }
}