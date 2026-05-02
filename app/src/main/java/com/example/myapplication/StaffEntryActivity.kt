package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class StaffEntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_staff_entry)

        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, StaffEntryFragment())
                .commit()
        }
    }
}