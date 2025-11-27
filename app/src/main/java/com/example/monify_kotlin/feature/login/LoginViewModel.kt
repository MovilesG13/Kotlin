package com.example.monify_kotlin.feature.login

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.core.util.ConnectivityObserver
import com.example.monify_kotlin.data.AuthRepository
import com.example.monify_kotlin.data.TokenManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.lang.Exception

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val loggedIn: Boolean = false,
    val isConnected: Boolean = true,
    val hasValidToken: Boolean = false
)

class LoginViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val tokenManager = TokenManager(application)
    private val repo = AuthRepository(tokenManager)
    private val connectivityObserver = ConnectivityObserver(application)

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    init {
        observeConnectivity()
        checkExistingToken()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.isConnected.collect { isConnected ->
                _state.value = _state.value.copy(isConnected = isConnected)
            }
        }
    }

    private fun checkExistingToken() {
        viewModelScope.launch {
            val isValid = tokenManager.isTokenValid()
            if (isValid && repo.isUserLoggedIn()) {
                _state.value = _state.value.copy(
                    hasValidToken = true,
                    loggedIn = true
                )
            }
        }
    }

    fun signIn(email: String, pass: String) {
        if (!_state.value.isConnected) {
            _state.value = _state.value.copy(
                error = "No internet connection. Please connect to log in."
            )
            return
        }

        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                repo.signIn(email, pass)
                _state.value = AuthUiState(loggedIn = true, isConnected = _state.value.isConnected)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Login error"
                )
            }
        }
    }



    // ... dentro de LoginViewModel
    fun signUp(name: String, email: String, pass: String) { // <-- (1) Añadir name
        if (!_state.value.isConnected) {
            _state.value = _state.value.copy(
                error = "No internet connection. Please connect to sign up."
            )
            return
        }

        _state.value = _state.value.copy(loading = true, error = null)
        viewModelScope.launch {
            try {
                // (2) First it creates the user
                repo.signUp(email, pass)
                // (3) Then load the name of the user
                repo.updateProfileName(name)

                _state.value = AuthUiState(loggedIn = true, isConnected = _state.value.isConnected)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Signup error"
                )
            }
        }
    }
}


