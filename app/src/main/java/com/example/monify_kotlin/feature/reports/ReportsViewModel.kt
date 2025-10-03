package com.example.monify_kotlin.feature.reports
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.ExpenseRepository
import com.example.monify_kotlin.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class CategoryData(
    val name: String,
    val amount: Double,
    val color: Color,
    val percentage: Float
)

data class ReportsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val categories: List<CategoryData> = emptyList(),
    val selectedMonth: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
)

class ReportsViewModel(
    private val repository: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    var uiState by mutableStateOf(ReportsUiState())
        private set

    init {
        loadMonthlyData()
    }

    fun loadMonthlyData() {
        uiState = uiState.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val summary = repository.getMonthlySummary(uiState.selectedMonth)
                val totalExpenses = (summary["totalExpenses"] as? Number)?.toDouble() ?: 0.0
                val totalIncome = (summary["totalIncome"] as? Number)?.toDouble() ?: 0.0

                @Suppress("UNCHECKED_CAST")
                val byCategory = summary["byCategory"] as? List<Map<String, Any>> ?: emptyList()

                val categoryColors = mapOf(
                    "food" to Red,
                    "transport" to Blue,
                    "bills" to Green,
                    "shopping" to LightBlue,
                    "other" to Gray
                )

                val categoryNames = mapOf(
                    "food" to "Food",
                    "transport" to "Transport",
                    "bills" to "Bills",
                    "shopping" to "Shopping",
                    "other" to "Other"
                )

                val categories = byCategory.map { item ->
                    val categoryId = item["categoryId"] as String
                    val total = (item["total"] as? Number)?.toDouble() ?: 0.0
                    val percentage = if (totalExpenses > 0) (total / totalExpenses).toFloat() else 0f

                    CategoryData(
                        name = categoryNames[categoryId] ?: categoryId.capitalize(),
                        amount = total,
                        color = categoryColors[categoryId] ?: Gray,
                        percentage = percentage
                    )
                }.sortedByDescending { it.amount }

                uiState = uiState.copy(
                    isLoading = false,
                    totalExpenses = totalExpenses,
                    totalIncome = totalIncome,
                    categories = categories
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load data"
                )
            }
        }
    }

    fun selectMonth(month: String) {
        uiState = uiState.copy(selectedMonth = month)
        loadMonthlyData()
    }
}

private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}