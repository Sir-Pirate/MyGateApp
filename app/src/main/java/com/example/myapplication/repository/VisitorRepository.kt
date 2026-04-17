package com.example.myapplication.repository

import com.example.myapplication.FirebaseHelper
import com.example.myapplication.model.Visitor

class VisitorRepository {

    // Using FirebaseHelper instead of getInstance() directly
    // This keeps the code consistent with the rest of your team's code
    private val db = FirebaseHelper.getDatabase()
    private val visitorsCollection = db.collection("visitors")

    // ── ADD a new visitor ──────────────────────────────────────────
    fun addVisitor(visitor: Visitor, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        visitorsCollection
            .add(visitor)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // ── GET all visitors ───────────────────────────────────────────
    fun getAllVisitors(onSuccess: (List<Visitor>) -> Unit, onFailure: (Exception) -> Unit) {
        visitorsCollection
            .get()
            .addOnSuccessListener { result ->
                val visitors = result.documents.mapNotNull { it.toObject(Visitor::class.java) }
                onSuccess(visitors)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    // ── UPDATE visitor status ──────────────────────────────────────
    fun updateVisitorStatus(visitorId: String, newStatus: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        visitorsCollection
            .document(visitorId)
            .update("status", newStatus)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}