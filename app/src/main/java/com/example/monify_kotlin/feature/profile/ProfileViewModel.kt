package com.example.monify_kotlin.feature.profile

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.core.util.ConnectivityObserver
import com.example.monify_kotlin.data.UserPreferencesRepository
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

data class ProfileUiState(
    val name: String = "",
    val currency: String = "USD",
    val isDarkMode: Boolean = false,
    val isOnline: Boolean = true
)

class ProfileViewModel(
    private val preferencesRepo: UserPreferencesRepository,
    private val connectivityObserver: ConnectivityObserver
) : ViewModel() {

    var uiState by mutableStateOf(ProfileUiState())
        private set

    init {
        observePreferences()
        observeConnectivity()
    }

    private fun observePreferences() {
        viewModelScope.launch {
            preferencesRepo.userName.collectLatest { name ->
                uiState = uiState.copy(name = name)
            }
        }
        viewModelScope.launch {
            preferencesRepo.currency.collectLatest { curr ->
                uiState = uiState.copy(currency = curr)
            }
        }
        viewModelScope.launch {
            preferencesRepo.isDarkMode.collectLatest { dark ->
                uiState = uiState.copy(isDarkMode = dark)
            }
        }
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            connectivityObserver.isConnected.collectLatest { connected ->
                uiState = uiState.copy(isOnline = connected)
            }
        }
    }

    // Acciones de Usuario
    fun onNameChanged(newName: String) {
        if (uiState.isOnline) {
            viewModelScope.launch { preferencesRepo.updateUserName(newName) }
        }
    }

    fun onCurrencyChanged(newCurrency: String) {
        if (uiState.isOnline) {
            viewModelScope.launch { preferencesRepo.updateCurrency(newCurrency) }
        }
    }

    fun onThemeChanged(isDark: Boolean) {
        // El tema se puede cambiar Offline sin problema, es preferencia local
        viewModelScope.launch { preferencesRepo.toggleDarkMode(isDark) }
    }
}

// Factory para inyectar dependencias manualmente
class ProfileViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(
                UserPreferencesRepository(context),
                ConnectivityObserver(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}