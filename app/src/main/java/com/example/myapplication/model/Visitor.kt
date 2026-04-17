package com.example.myapplication.model

data class Visitor(
    val name: String = "",
    val phone: String = "",
    val approvedBy: String = "",
    val status: String = "",
    val arrivalTime: com.google.firebase.Timestamp? = null
)