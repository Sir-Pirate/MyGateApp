package com.example.myapplication

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

object FirebaseHelper {

    // Connects to Firebase Authentication
    fun getAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }

    // Connects to Firestore Database
    fun getDatabase(): FirebaseFirestore {
        return FirebaseFirestore.getInstance()
    }

    // Check if a user is currently logged in
    fun isLoggedIn(): Boolean {
        return getAuth().currentUser != null
    }

    // Get the current logged in user's ID
    fun getCurrentUserId(): String? {
        return getAuth().currentUser?.uid
    }
}