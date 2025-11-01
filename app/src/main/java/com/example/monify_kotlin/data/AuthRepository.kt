package com.example.monify_kotlin.data
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.coroutines.tasks.await

class AuthRepository(private val tokenManager: TokenManager? = null) {
    private val auth = FirebaseAuth.getInstance()

    val currentUser get() = auth.currentUser

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

    /**
     * Refactorizado: usa 'suspend' y 'await()' para esperar el resultado
     * de forma asíncrona sin bloquear el hilo.
     */
    suspend fun loginUser(email: String, pass: String): AuthResult {
        return auth.signInWithEmailAndPassword(email, pass).await()
    }

    /**
     * Refactorizado: igual que el login.
     */
    suspend fun signUpUser(email: String, pass: String): AuthResult {
        return auth.createUserWithEmailAndPassword(email, pass).await()
    }

    suspend fun updateProfileName(name: String) {
        val user = auth.currentUser ?: throw Exception("User not found after sign up")

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates).await()
    }
}
