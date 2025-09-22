package com.example.monify_kotlin.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.monify_kotlin.feature.login.ui.LoginScreen

object Routes {
    const val LOGIN = "login"
    const val HOME  = "home"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    NavHost(navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = { navController.navigate(Routes.HOME) { popUpTo(Routes.LOGIN) { inclusive = true } } }
            )
        }
        composable(Routes.HOME) {
            // Placeholder de Home (mock)
            androidx.compose.material3.Text(text = "Home (mock)")
        }
    }
}
