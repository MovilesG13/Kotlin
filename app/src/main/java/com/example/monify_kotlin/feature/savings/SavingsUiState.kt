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
    val progress: Float get() = (saved / target).coerceIn(0f, 1f)
    val missing: Float get() = (target - saved).coerceAtLeast(0f)
}

data class SavingsUiState(
    val tab: SavingsTab = SavingsTab.SUMMARY,
    val totalSaved: Float = 13150f,
    val totalTarget: Float = 51200f,
    val activeGoals: Int = 2,
    val monthlySavings: Float = 900f,
    val goals: List<SavingGoal> = emptyList()
)

