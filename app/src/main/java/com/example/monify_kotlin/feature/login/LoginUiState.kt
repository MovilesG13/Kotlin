package com.example.monify_kotlin.feature.login

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
) {
    val canSubmit: Boolean get() = email.isNotBlank() && password.isNotBlank()
}
