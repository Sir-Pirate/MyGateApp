package com.example.myapplication

// Example: add EditTexts for flatNo and tower
private var editTextFlatNo: EditText? = null
private var editTextTower: EditText? = null

// Inside onCreate
editTextFlatNo = findViewById(R.id.editTextFlatNo)
editTextTower = findViewById(R.id.editTextTower)

// Inside buttonRegister.setOnClickListener
val flatNo = editTextFlatNo!!.text.toString().trim()
val tower = editTextTower!!.text.toString().trim()

registerUser(
email,
password,
name,
phone,
role,
flatNo,
tower,
onSuccess = {
    runOnUiThread {
        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show()
        val intent = Intent(this, HomeActivity::class.java)
        intent.putExtra("role", role)
        startActivity(intent)
        finish()
    }
},
onError = { errorMessage ->
    runOnUiThread {
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
    }
}
)
