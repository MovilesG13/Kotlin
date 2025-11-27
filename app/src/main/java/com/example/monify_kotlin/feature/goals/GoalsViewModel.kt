package com.example.monify_kotlin.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import kotlinx.coroutines.launch

data class GoalsUiState(
    val loading: Boolean = true,
    val goals: List<Goal> = emptyList(),
    val error: String? = null
)

class GoalsViewModel(
    private val goalsRepo: GoalsRepository = GoalsRepository()
) : ViewModel() {

    var state = androidx.compose.runtime.mutableStateOf(GoalsUiState())
        private set

    fun loadGoals() {
        viewModelScope.launch {
            try {
                val goals = goalsRepo.getGoals()
                state.value = GoalsUiState(loading = false, goals = goals)
            } catch (e: Exception) {
                state.value = state.value.copy(loading = false, error = e.message ?: "Error")
            }
        }
    }
}
