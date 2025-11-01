package com.example.monify_kotlin.feature.goals

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.CarRental
import androidx.compose.material.icons.outlined.House
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.School
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class SavingsTab {
    SUMMARY, MY_GOALS, PROGRESS
}

data class SavingGoalUi(
    val id: String,
    val title: String,
    val icon: ImageVector,
    val saved: Float,
    val target: Float,
    val progress: Float,
    val missing: Float,
    val monthly: Float, // This is likely a mock or needs more logic
    val estMonths: Int  // This is likely a mock or needs more logic
)

data class GoalsUiState(
    val goals: List<SavingGoalUi> = emptyList(),
    val tab: SavingsTab = SavingsTab.SUMMARY,
    val totalSaved: Float = 0f,
    val totalTarget: Float = 0f,
    val activeGoals: Int = 0,
    val monthlySavings: Float = 0f, // Mock value
    val loading: Boolean = false
)

class GoalsViewModel(
    private val goalsRepo: GoalsRepository
) : ViewModel() {

    var showAddGoalDialog by mutableStateOf(false)
        private set

    private val _tab = MutableStateFlow(SavingsTab.SUMMARY)

    val uiState: StateFlow<GoalsUiState> = combine(
        goalsRepo.goals, _tab
    ) { goals, tab ->
        val uiGoals = goals.map { it.toPresentationModel() }
        val totalSaved = uiGoals.sumOf { it.saved.toDouble() }.toFloat()
        val totalTarget = uiGoals.sumOf { it.target.toDouble() }.toFloat()

        GoalsUiState(
            goals = uiGoals,
            tab = tab,
            totalSaved = totalSaved,
            totalTarget = totalTarget,
            activeGoals = uiGoals.size,
            monthlySavings = 150.55f // Mock data as logic is not available
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = GoalsUiState(loading = true)
    )

    fun onAddGoalClicked() {
        showAddGoalDialog = true
    }

    fun dismissAddGoalDialog() {
        showAddGoalDialog = false
    }

    fun selectTab(tab: SavingsTab) {
        _tab.value = tab
    }

    fun addGoal(title: String, targetAmount: Double, icon: String) {
        viewModelScope.launch {
            goalsRepo.addGoal(title, targetAmount, icon)
            dismissAddGoalDialog()
        }
    }

    init {
        viewModelScope.launch {
            goalsRepo.refreshGoals()
        }
    }
}

private fun Goal.toPresentationModel(): SavingGoalUi {
    val saved = this.currentAmount.toFloat()
    val target = this.targetAmount.toFloat()
    val progress = if (target > 0) (saved / target).coerceIn(0f, 1f) else 0f
    val missing = (target - saved).coerceAtLeast(0f)
    val iconImage = when (this.icon.lowercase()) {
        "house" -> Icons.Outlined.House
        "car" -> Icons.Outlined.CarRental
        "apartment" -> Icons.Outlined.Apartment
        "school" -> Icons.Outlined.School
        else -> Icons.Outlined.Phone
    }

    return SavingGoalUi(
        id = this.id,
        title = this.title,
        icon = iconImage,
        saved = saved,
        target = target,
        progress = progress,
        missing = missing,
        monthly = 50.0f, // Mock data
        estMonths = if (missing > 0 && 50.0f > 0) (missing / 50.0f).toInt() else 0 // Mock data
    )
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
