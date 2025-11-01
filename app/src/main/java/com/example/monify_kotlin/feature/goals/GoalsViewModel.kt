package com.example.monify_kotlin.feature.goals

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


data class GoalsUiState(
    val goals: List<Goal> = emptyList(),
    val error: String? = null,
    val loading: Boolean = false
)

class GoalsViewModel(
    private val goalsRepo: GoalsRepository
) : ViewModel() {

    val uiState: StateFlow<GoalsUiState> = goalsRepo.goals
        .map { goals -> GoalsUiState(goals = goals) }
        .catch { e -> emit(GoalsUiState(error = e.message)) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = GoalsUiState(loading = true)
        )

    init {
        refreshData()
    }

    private fun refreshData() {
        viewModelScope.launch {
            goalsRepo.refreshGoals()
        }
    }
}

class GoalsViewModelFactory(private val repository: GoalsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GoalsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GoalsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
