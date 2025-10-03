package com.example.monify_kotlin.feature.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.AuthRepository
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false
)

class LoginViewModel(
    private val repo: AuthRepository = AuthRepository()
) : ViewModel() {

    var state = androidx.compose.runtime.mutableStateOf(AuthUiState())
        private set

    fun signIn(email: String, pass: String) {
        state.value = state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                repo.signIn(email, pass)
                state.value = AuthUiState(loggedIn = true)
            } catch (e: Exception) {
                state.value = AuthUiState(error = e.message ?: "Login error")
            }
        }
    }

    fun signUp(email: String, pass: String) {
        state.value = state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                repo.signUp(email, pass)
                state.value = AuthUiState(loggedIn = true)
            } catch (e: Exception) {
                state.value = AuthUiState(error = e.message ?: "Signup error")
            }
        }
    }
}


