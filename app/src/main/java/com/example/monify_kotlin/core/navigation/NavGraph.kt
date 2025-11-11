package com.example.monify_kotlin.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.monify_kotlin.feature.login.ui.LoginScreen
import com.example.monify_kotlin.feature.login.ui.MainLoginScreen
import com.example.monify_kotlin.feature.login.ui.SignUpScreen
import com.example.monify_kotlin.feature.savings.ui.SavingsScreen
import com.example.monify_kotlin.feature.transactions.ui.AddExpenseScreen
import com.example.monify_kotlin.feature.transactions.ui.AddIncomeScreen
import com.example.monify_kotlin.feature.reports.ui.ReportsScreen
import android.os.Build
import androidx.compose.material3.Text
import androidx.annotation.RequiresApi
import com.example.monify_kotlin.feature.home.HomeScreen

object Routes {
    const val MAIN_LOGIN = "main_login"
    const val LOGIN = "login"
    const val SIGN_UP = "sign_up"
    const val HOME = "home"
    const val SAVINGS = "savings"
    const val ADD_INCOME = "add_income"
    const val ADD_EXPENSE = "add_expense"
    const val REPORTS = "reports"
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavGraph(
    navController: NavHostController,
    startDestination: String = Routes.MAIN_LOGIN
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.MAIN_LOGIN) {
            MainLoginScreen(
                onLoginClicked = { navController.navigate(Routes.LOGIN) },
                onSignInClicked = { navController.navigate(Routes.SIGN_UP) }
            )
        }

        composable(Routes.LOGIN) {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.MAIN_LOGIN) { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
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

        composable(Routes.HOME) {
            HomeScreen(
                onGoSavings = { navController.navigate(Routes.SAVINGS) },
                onAddIncome = { navController.navigate(Routes.ADD_INCOME) },
                onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) },
                onGoReports = { navController.navigate(Routes.REPORTS) }
            )
        }

        composable(Routes.SAVINGS) {
            SavingsScreen(onBackHome = { navController.navigate(Routes.HOME) })
        }

        composable(Routes.REPORTS) {
            ReportsScreen(
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(Routes.HOME) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable(Routes.ADD_INCOME) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AddIncomeScreen(onBack = { navController.popBackStack() })
            } else {
                Text("Esta pantalla requiere Android O (API 26) o superior")
            }
        }

        composable(Routes.ADD_EXPENSE) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                AddExpenseScreen(onBack = { navController.popBackStack() })
            } else {
                Text("Esta pantalla requiere Android O (API 26) o superior")
            }
        }
    }
}
