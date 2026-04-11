package com.example.myapplication.models
 
data class Patrol(
    val guardId: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val status: String = "ongoing",
    val checkpoints: List<String> = emptyList()
)