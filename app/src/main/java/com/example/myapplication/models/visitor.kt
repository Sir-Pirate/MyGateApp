package com.example.myapplication.models
 
data class Visitor(
    val name: String = "",
    val phone: String = "",
    val approvedBy: String = "",
    val status: String = "pending",
    val arrivalTime: String = ""
)