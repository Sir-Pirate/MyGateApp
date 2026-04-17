package com.example.myapplication.repository

import com.example.myapplication.FirebaseHelper
import com.example.myapplication.model.Patrol

class PatrolRepository {

    private val db = FirebaseHelper.getDatabase()
    private val patrolsCollection = db.collection("patrols")

    // ── ADD a new patrol ───────────────────────────────────────────
    fun addPatrol(patrol: Patrol, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        patrolsCollection
            .add(patrol)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // ── GET all patrols ────────────────────────────────────────────
    fun getAllPatrols(onSuccess: (List<Patrol>) -> Unit, onFailure: (Exception) -> Unit) {
        patrolsCollection
            .get()
            .addOnSuccessListener { result ->
                val patrols = result.toObjects(Patrol::class.java)
                onSuccess(patrols)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // ── MARK patrol as completed ───────────────────────────────────
    fun completePatrol(patrolId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        patrolsCollection
            .document(patrolId)
            .update("status", "completed")
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}