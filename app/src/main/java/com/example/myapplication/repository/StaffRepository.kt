package com.example.myapplication.repository

import com.example.myapplication.FirebaseHelper
import com.example.myapplication.model.Staff

class StaffRepository {

    private val db = FirebaseHelper.getDatabase()
    private val staffCollection = db.collection("staff")

    // ── ADD new staff member ───────────────────────────────────────
    fun addStaff(staff: Staff, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        staffCollection
            .add(staff)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // ── GET all staff ──────────────────────────────────────────────
    fun getAllStaff(onSuccess: (List<Staff>) -> Unit, onFailure: (Exception) -> Unit) {
        staffCollection
            .get()
            .addOnSuccessListener { result ->
                val staffList = result.toObjects(Staff::class.java)
                onSuccess(staffList)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }
}