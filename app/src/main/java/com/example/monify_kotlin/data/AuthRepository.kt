package com.example.monify_kotlin.data

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).await()
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    fun currentUid(): String? = auth.currentUser?.uid
    fun isLoggedIn(): Boolean = auth.currentUser != null
    fun signOut() = auth.signOut()
}

