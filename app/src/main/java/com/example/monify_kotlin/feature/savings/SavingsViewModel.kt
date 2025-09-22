package com.example.monify_kotlin.feature.savings

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.PhoneIphone

class SavingsViewModel : ViewModel() {

    var uiState by mutableStateOf(
        SavingsUiState(
            goals = listOf(
                SavingGoal(
                    title = "Celular Nuevo",
                    target = 1200f,
                    saved = 650f,
                    monthly = 100f,
                    estMonths = 6,
                    icon = Icons.Outlined.PhoneIphone
                ),
                SavingGoal(
                    title = "Casa Sola",
                    target = 50000f,
                    saved = 6000f,
                    monthly = 1200f,
                    estMonths = 37,
                    icon = Icons.Outlined.Home
                )
            )
        )
    )
        private set

    fun selectTab(tab: SavingsTab) {
        uiState = uiState.copy(tab = tab)
    }
}
