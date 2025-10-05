package com.example.monify_kotlin.feature.savings

import androidx.compose.ui.graphics.vector.ImageVector

enum class SavingsTab { SUMMARY, MY_GOALS, PROGRESS }

data class SavingGoal(
    val title: String,
    val target: Float,
    val saved: Float,
    val monthly: Float,
    val estMonths: Int,
    val icon: ImageVector
) {
    val progress: Float get() = if (target > 0) (saved / target).coerceIn(0f, 1f) else 0f
    val missing: Float get() = (target - saved).coerceAtLeast(0f)
}

data class SavingsUiState(
    val tab: SavingsTab = SavingsTab.SUMMARY,
    val totalSaved: Float = 0f,
    val totalTarget: Float = 0f,
    val activeGoals: Int = 0,
    val monthlySavings: Float = 0f,
    val goals: List<SavingGoal> = emptyList()
)
