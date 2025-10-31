package com.example.monify_kotlin.data
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

class AuthRepository(private val tokenManager: TokenManager? = null) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signIn(email: String, password: String) {
        val result = auth.signInWithEmailAndPassword(email, password).await()
        val token = result.user?.getIdToken(false)?.await()?.token
        token?.let {
            tokenManager?.saveToken(it, email)
        }
    }

    suspend fun signUp(email: String, password: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val token = result.user?.getIdToken(false)?.await()?.token
        token?.let {
            tokenManager?.saveToken(it, email)
        }
    }

    fun isUserLoggedIn(): Boolean = auth.currentUser != null

    suspend fun signOut() {
        auth.signOut()
        tokenManager?.clearToken()
    }
}
