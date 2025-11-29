package com.example.monify_kotlin

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.monify_kotlin.core.navigation.AppNavGraph
import com.example.monify_kotlin.data.UserPreferencesRepository
import com.example.monify_kotlin.ui.theme.FinanceTheme
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Inicializamos el Repo de Preferencias
        val userPrefs = UserPreferencesRepository(applicationContext)

        setContent {
            // 2. Instanciamos el ViewModel "Lite" aquí mismo
            val mainViewModel: MainViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return MainViewModel(userPrefs) as T
                    }
                }
            )


            val isDarkThemePref by mainViewModel.isDarkMode.collectAsState(initial = isSystemInDarkTheme())

            // 4. Pasamos el valor al Theme
            FinanceTheme(darkTheme = isDarkThemePref) {
                val nav = rememberNavController()
                AppNavGraph(navController = nav)
            }
        }
    }
}

// --- ViewModel "Lite" (Para no crear otro archivo) ---
class MainViewModel(repository: UserPreferencesRepository) : ViewModel() {
    // Solo necesitamos saber si es dark mode o no
    val isDarkMode: Flow<Boolean> = repository.isDarkMode
}
