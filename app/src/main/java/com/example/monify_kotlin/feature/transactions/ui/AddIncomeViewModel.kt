package com.example.monify_kotlin.feature.transactions.ui

import android.app.Application
import android.os.Build
import android.util.LongSparseArray
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.core.util.ConnectivityObserver
import com.example.monify_kotlin.data.IncomeRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import com.example.monify_kotlin.data.cache.PendingIncome
import com.example.monify_kotlin.data.sync.TransactionSyncWorker
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class AddIncomeUiState(
    val amount: String = "",
    val description: String = "",
    val category: String = "Salary",
    val notes: String = "",
    val date: LocalDate? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val success: Boolean = false,
    val successMessage: String? = null,
    val isConnected: Boolean = true,
    val isOfflineMode: Boolean = false
)

class AddIncomeViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val repository = IncomeRepository()
    private val database = AppDatabase.getDatabase(application)
    private val connectivityObserver = ConnectivityObserver(application)

    // LongSparseArray cache for storing income metadata (more memory efficient than HashMap for long keys)
    private val incomeCache = LongSparseArray<Any>()
    private var cacheKeyCounter = 0L

    var uiState by mutableStateOf(AddIncomeUiState())
        private set

    init {
        observeConnectivity()
    }

    private fun observeConnectivity() {
        viewModelScope.launch {
            var wasOffline = false
            connectivityObserver.isConnected.collect { isConnected ->
                uiState = uiState.copy(
                    isConnected = isConnected,
                    isOfflineMode = !isConnected
                )

                // Trigger sync when coming back online
                if (isConnected && wasOffline) {
                    android.util.Log.d("AddIncomeViewModel", "Back online - triggering sync")
                    TransactionSyncWorker.schedule(getApplication())
                }
                wasOffline = !isConnected
            }
        }
    }

    fun updateAmount(value: String) {
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            uiState = uiState.copy(amount = value, error = null)
            incomeCache.put(1L, value)
        }
    }

    fun updateDescription(value: String) {
        uiState = uiState.copy(description = value)
        incomeCache.put(2L, value)
    }

    fun updateCategory(value: String) {
        uiState = uiState.copy(category = value)
        incomeCache.put(3L, value)
    }

    fun updateNotes(value: String) {
        uiState = uiState.copy(notes = value)
        incomeCache.put(4L, value)
    }

    fun updateDate(value: LocalDate?) {
        uiState = uiState.copy(date = value, error = null)
        value?.let { incomeCache.put(5L, it) }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveIncome() {
        val amount = uiState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            uiState = uiState.copy(error = "Please enter a valid amount")
            return
        }

        if (uiState.date == null) {
            uiState = uiState.copy(error = "Please select a date")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val dateString = uiState.date!!.atStartOfDay()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

                if (uiState.isConnected) {
                    // Online: Save directly to Firebase
                    repository.createIncome(
                        amount = amount,
                        currency = "USD",
                        source = uiState.category,
                        description = if (uiState.description.isNotBlank()) uiState.description else null,
                        date = dateString
                    )

                    uiState = uiState.copy(
                        isLoading = false,
                        success = true,
                        successMessage = "Income saved successfully!"
                    )
                } else {
                    // Offline: Save to local database
                    val pendingIncome = PendingIncome(
                        amount = amount,
                        currency = "USD",
                        source = uiState.category,
                        description = if (uiState.description.isNotBlank()) uiState.description else null,
                        date = dateString
                    )

                    database.pendingIncomeDao().insert(pendingIncome)

                    uiState = uiState.copy(
                        isLoading = false,
                        success = true,
                        successMessage = "Income saved offline. Will sync when online."
                    )
                }

                incomeCache.clear()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = "Error: ${e.message ?: "Unknown error"}"
                )
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }
}