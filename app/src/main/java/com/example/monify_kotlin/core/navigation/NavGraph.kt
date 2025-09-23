package com.example.monify_kotlin.core.navigation

import com.example.monify_kotlin.feature.login.ui.SignUpScreen


import androidx.compose.material3.Text


import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.monify_kotlin.feature.login.ui.LoginScreen
import com.example.monify_kotlin.feature.login.ui.MainLoginScreen // Import the new MainLoginScreen

// Define all your routes here
object Routes {
    const val MAIN_LOGIN = "main_login" // New route for the main login options screen
    const val LOGIN = "login"           // Your existing login screen
    const val SIGN_UP = "sign_up"       // Placeholder for a future sign-in screen
    const val HOME = "home"
}

@Composable
fun AppNavGraph(navController: NavHostController) {
    // Start destination is now the MAIN_LOGIN screen
    NavHost(navController = navController, startDestination = Routes.MAIN_LOGIN) {

        composable(Routes.MAIN_LOGIN) {
            MainLoginScreen(
                onLoginClicked = {
                    navController.navigate(Routes.LOGIN)
                },
                onSignInClicked = {
                    navController.navigate(Routes.SIGN_UP)
                    // For now, you can leave it empty or navigate to a placeholder
                    // navController.navigate(Routes.SIGN_IN)
                    println("Sign In Clicked - Implement navigation to Sign In screen")
                }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        // Clear the back stack up to MAIN_LOGIN, including it,
                        // so the user doesn't go back to any login screens.
                        popUpTo(Routes.MAIN_LOGIN) { inclusive = true }
                        launchSingleTop = true // Avoid multiple copies of Home if already on top
                    }
                }
                // If your LoginScreen takes a ViewModel, it would be instantiated here or passed in
                // vm = viewModel()
            )
        }

        composable(Routes.HOME) {
            // Placeholder de Home (mock)
            Text(text = "Home Screen (Authenticated)") // Made the text a bit more descriptive
        }

        composable(Routes.SIGN_UP) {
            SignUpScreen(
                navController = navController,
                onSignUpSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.MAIN_LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

