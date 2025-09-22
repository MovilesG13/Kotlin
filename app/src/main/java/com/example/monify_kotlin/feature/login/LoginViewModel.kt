package com.example.monify_kotlin.feature.login
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class LoginViewModel : ViewModel() {
    var uiState by mutableStateOf(LoginUiState())
        private set

    fun onEmailChange(v: String)    { uiState = uiState.copy(email = v, error = null) }
    fun onPasswordChange(v: String) { uiState = uiState.copy(password = v, error = null) }
    fun togglePassword()            { uiState = uiState.copy(isPasswordVisible = !uiState.isPasswordVisible) }

    // Mock: solo navega si campos no vacíos
    fun submit(onSuccess: () -> Unit) {
        if (!uiState.canSubmit) {
            uiState = uiState.copy(error = "Completa email y contraseña")
            return
        }
        onSuccess()
    }
}
