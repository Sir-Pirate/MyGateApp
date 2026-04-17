package com.example.myapplication.model

data class Patrol(
    val guardId: String = "",
    val startTime: com.google.firebase.Timestamp? = null,
    val endTime: com.google.firebase.Timestamp? = null,
    val status: String = "",
    val checkpoints: List<String> = emptyList()
)