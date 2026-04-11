package com.example.myapplication;

import android.content.Intent;

import android.os.Bundle;

import android.view.View;

import android.widget.ArrayAdapter;

import android.widget.Button;

import android.widget.EditText;

import android.widget.Spinner;

import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextEmail, editTextPhone, editTextPassword;

    private Button buttonRegister, buttonGoToLogin;

    private Spinner spinnerRole;

    @Override

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_register);

        editTextName = findViewById(R.id.editTextName);

        editTextEmail = findViewById(R.id.editTextEmail);

        editTextPhone = findViewById(R.id.editTextPhone);

        editTextPassword = findViewById(R.id.editTextPassword);

        buttonRegister = findViewById(R.id.buttonRegister);

        buttonGoToLogin = findViewById(R.id.buttonGoToLogin);

        spinnerRole = findViewById(R.id.spinnerRole);

        // Role dropdown options

        String[] roles = {"resident", "guard", "admin"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(

                this,

                android.R.layout.simple_spinner_item,

                roles

        );

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerRole.setAdapter(adapter);

        // Register button

        buttonRegister.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                String name = editTextName.getText().toString().trim();

                String email = editTextEmail.getText().toString().trim();

                String phone = editTextPhone.getText().toString().trim();

                String password = editTextPassword.getText().toString().trim();

                String role = spinnerRole.getSelectedItem().toString();

                if (name.isEmpty() || email.isEmpty() ||

                        phone.isEmpty() || password.isEmpty()) {

                    Toast.makeText(RegisterActivity.this,

                            "Please fill all fields",

                            Toast.LENGTH_SHORT).show();

                    return;

                }

                AuthManager.INSTANCE.registerUser(

                        email,

                        password,

                        name,

                        role,

                        () -> {

                            Toast.makeText(RegisterActivity.this,

                                    "Registration Successful!",

                                    Toast.LENGTH_SHORT).show();

                            Intent intent = new Intent(RegisterActivity.this, mainactivity.class);

                            startActivity(intent);

                            finish();

                            return null;

                        },

                        errorMessage -> {

                            Toast.makeText(RegisterActivity.this,

                                    errorMessage,

                                    Toast.LENGTH_SHORT).show();

                            return null;

                        }

                );

            }

        });

        // Go to login button

        buttonGoToLogin.setOnClickListener(new View.OnClickListener() {

            @Override

            public void onClick(View v) {

                Intent intent = new Intent(RegisterActivity.this, mainactivity.class);

                startActivity(intent);

                finish();

            }

        });

    }

}
