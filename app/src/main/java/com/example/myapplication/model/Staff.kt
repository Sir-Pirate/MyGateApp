package com.example.myapplication.model

data class Staff(
    val name: String = "",
    val role: String = "",
    val assignedTo: String = "",
    val entryTime: com.google.firebase.Timestamp? = null,
    val exitTime: com.google.firebase.Timestamp? = null
)