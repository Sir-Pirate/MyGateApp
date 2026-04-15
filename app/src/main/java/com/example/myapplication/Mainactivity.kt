package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.myapplication.AuthManager.loginUser

class mainactivity : AppCompatActivity() {
    private var editTextUsername: EditText? = null
    private var editTextPassword: EditText? = null
    private var buttonLogin: Button? = null
    private var buttonGoToRegister: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.loginpage)

        editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        buttonLogin = findViewById<Button>(R.id.buttonLogin)
        buttonGoToRegister = findViewById<Button>(R.id.buttonGoToRegister)

        // Login button
        buttonLogin!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val email = editTextUsername!!.getText().toString().trim { it <= ' ' }
                val password = editTextPassword!!.getText().toString().trim { it <= ' ' }

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(
                        getApplicationContext(),
                        "Please enter both fields",
                        Toast.LENGTH_SHORT
                    ).show()
                    return
                }

                loginUser(
                    email,
                    password,
                    {
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                getApplicationContext(),
                                "Login Successful!",
                                Toast.LENGTH_SHORT
                            ).show()
                            // ← Navigate to Home Screen after login
                            val intent = Intent(this@mainactivity, homeactivity::class.java)
                            startActivity(intent)
                            finish()
                        })
                        null
                    },
                    { errorMessage: String? ->
                        runOnUiThread(Runnable {
                            Toast.makeText(
                                getApplicationContext(),
                                errorMessage,
                                Toast.LENGTH_SHORT
                            ).show()
                        })
                        null
                    }
                )
            }
        })

        // Go to Register button
        buttonGoToRegister!!.setOnClickListener(object : View.OnClickListener {
            override fun onClick(v: View?) {
                val intent = Intent(this@mainactivity, RegisterActivity::class.java)
                startActivity(intent)
            }
        })
    }
}