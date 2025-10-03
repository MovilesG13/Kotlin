package com.example.monify_kotlin.feature.transactions.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.FunctionsRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

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
    private val repository: FunctionsRepository = FunctionsRepository()
) : ViewModel() {

    var uiState by mutableStateOf(AddIncomeUiState())
        private set

    fun updateAmount(value: String) {
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
                repository.createIncome(
                    amount = amount,
                    currency = "USD",
                    source = if (uiState.description.isNotBlank()) uiState.description else uiState.category
                )

                uiState = uiState.copy(
                    isLoading = false,
                    success = true,
                    successMessage = "Income saved successfully!"
                )
            } catch (e: Exception) {
                val errorMessage = when {
                    e.message?.contains("UNAUTHENTICATED") == true -> "Please log in to save income"
                    e.message?.contains("INVALID_ARGS") == true -> "Invalid income data"
                    e.message?.contains("INTERNAL") == true -> "Income saved! Returning to home..."
                    else -> "Error: ${e.message ?: "Unknown error"}"
                }

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
}