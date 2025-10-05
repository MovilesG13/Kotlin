package com.example.monify_kotlin.feature.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import com.example.monify_kotlin.data.HomeRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val loading: Boolean = true,
    val name: String = "User",
    val monthLabel: String = "",
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val balance: Double = 0.0,
    val goals: List<Goal> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val repo: HomeRepository = HomeRepository(),
    private val goalsRepo: GoalsRepository = GoalsRepository()
) : ViewModel() {

    var state = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    @RequiresApi(Build.VERSION_CODES.O)
    fun load() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val ym = "%04d-%02d".format(today.year, today.monthValue)
                val name = repo.getProfileName()
                val (inc, exp, bal) = repo.getMonthlySummary(ym)
                val goals = goalsRepo.getGoals()
                state.value = HomeUiState(
                    loading = false,
                    name = name,
                    monthLabel = today.month.name.lowercase().replaceFirstChar { it.titlecase() },
                    income = inc,
                    expenses = exp,
                    balance = bal,
                    goals = goals
                )
            } catch (e: Exception) {
                state.value = state.value.copy(loading = false, error = e.message ?: "Error")
            }
        }
    }
}
