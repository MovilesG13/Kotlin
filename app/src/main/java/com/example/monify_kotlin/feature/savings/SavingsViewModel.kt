package com.example.monify_kotlin.feature.savings

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class SavingsViewModel(
    private val repo: GoalsRepository
) : ViewModel() {

    var uiState by mutableStateOf(SavingsUiState())
        private set

    var showAddGoalDialog by mutableStateOf(false)
        private set

    init {
        // Observe the local database for changes
        observeGoals()
        // Refresh the local cache from the network in the background
        refreshData()
    }

    private fun observeGoals() {
        viewModelScope.launch {
            repo.goals.collect { goals ->
                val savingGoals = goals.map { it.toSavingGoal() }
                val totalSaved = savingGoals.sumOf { it.saved.toDouble() }.toFloat()
                val totalTarget = savingGoals.sumOf { it.target.toDouble() }.toFloat()

                uiState = uiState.copy(
                    goals = savingGoals,
                    totalSaved = totalSaved,
                    totalTarget = totalTarget,
                    activeGoals = savingGoals.size
                )
            }
        }
    }

    private fun refreshData() {
        viewModelScope.launch {
            try {
                repo.refreshGoals()
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Error refreshing goals", e)
            }
        }
    }

    fun addGoal(title: String, targetAmount: Double, icon: String) {
        viewModelScope.launch {
            try {
                repo.addGoal(title, targetAmount, icon)
            } catch (e: Exception) {
                Log.e("SavingsViewModel", "Failed to add goal", e)
            } finally {
                // Always dismiss the dialog, regardless of success or failure
                dismissAddGoalDialog()
            }
        }
    }

    fun onAddGoalClicked() {
        showAddGoalDialog = true
    }

    fun dismissAddGoalDialog() {
        showAddGoalDialog = false
    }

    fun selectTab(tab: SavingsTab) {
        uiState = uiState.copy(tab = tab)
    }

    private fun Goal.toSavingGoal(): SavingGoal {
        return SavingGoal(
            title = this.title,
            target = this.targetAmount.toFloat(),
            saved = this.currentAmount.toFloat(),
            monthly = 100f, // Mocked for now
            estMonths = 6, // Mocked for now
            icon = mapIcon(this.icon)
        )
    }

    private fun mapIcon(iconName: String): ImageVector {
        return when (iconName.lowercase()) {
            "phone" -> Icons.Outlined.PhoneIphone
            "house" -> Icons.Outlined.Home
            else -> Icons.Outlined.Savings // Default icon
        }
    }
}

// Factory to create SavingsViewModel with its dependencies
class SavingsViewModelFactory(private val repository: GoalsRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SavingsViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return SavingsViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
