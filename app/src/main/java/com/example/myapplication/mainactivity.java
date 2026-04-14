package com.example.myapplication;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class mainactivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin, buttonGoToRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginpage);

        // Initialize views
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        buttonGoToRegister = findViewById(R.id.buttonGoToRegister);

        // Login button click
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editTextUsername.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(mainactivity.this, "Please enter both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Connect to Firebase via AuthManager
                AuthManager.INSTANCE.loginUser(
                        email,
                        password,
                        () -> {
                            Toast.makeText(mainactivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            // Navigate to home screen
                            Intent intent = new Intent(mainactivity.this, homeactivity.class);
                            startActivity(intent);
                            finish();
                            return null;
                        },
                        errorMessage -> {
                            Toast.makeText(mainactivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                            return null;
                        }
                );
            }
        });

        // Register button click
        buttonGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mainactivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}