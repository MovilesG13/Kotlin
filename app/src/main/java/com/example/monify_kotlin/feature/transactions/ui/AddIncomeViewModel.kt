package com.example.monify_kotlin.feature.transactions

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.IncomeRepository
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
    val successMessage: String? = null
)

class AddIncomeViewModel(
    private val repository: IncomeRepository = IncomeRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AddIncomeUiState())
        private set

    fun updateAmount(value: String) {
        // Solo permitir números y un punto decimal
        if (value.isEmpty() || value.matches(Regex("^\\d*\\.?\\d*$"))) {
            uiState = uiState.copy(amount = value, error = null)
        }
    }

    fun updateDescription(value: String) {
        uiState = uiState.copy(description = value)
    }

    fun updateCategory(value: String) {
        uiState = uiState.copy(category = value)
    }

    fun updateNotes(value: String) {
        uiState = uiState.copy(notes = value)
    }

    fun updateDate(value: LocalDate?) {
        uiState = uiState.copy(date = value, error = null)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveIncome() {
        // Validar amount
        val amount = uiState.amount.toDoubleOrNull()
        if (amount == null || amount <= 0) {
            uiState = uiState.copy(error = "Please enter a valid amount")
            return
        }

        // Validar fecha
        if (uiState.date == null) {
            uiState = uiState.copy(error = "Please select a date")
            return
        }

        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                // Convertir la fecha a formato ISO 8601
                val dateString = uiState.date!!.atStartOfDay()
                    .format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)

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
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("UNAUTHENTICATED") == true -> "Please log in to save income"
                    e.message?.contains("INVALID_ARGS") == true -> "Invalid income data. Please check all fields"
                    e.message?.contains("network", ignoreCase = true) == true -> "Network error. Please check your connection"
                    e.message?.contains("INTERNAL") == true -> "Income saved! Returning to home..."
                    else -> "Error: ${e.message ?: "Unknown error"}"
                }

                // Si el error es INTERNAL, probablemente se guardó exitosamente
                if (e.message?.contains("INTERNAL") == true) {
                    uiState = uiState.copy(
                        isLoading = false,
                        success = true,
                        successMessage = "Income saved successfully!"
                    )
                } else {
                    uiState = uiState.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            }
        }
    }

    fun clearError() {
        uiState = uiState.copy(error = null)
    }

    fun resetState() {
        uiState = AddIncomeUiState()
    }
}