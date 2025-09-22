package com.example.monify_kotlin.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavHostController
import com.example.monify_kotlin.feature.login.ui.LoginScreen
import com.example.monify_kotlin.feature.home.HomeScreen
import com.example.monify_kotlin.feature.savings.ui.SavingsScreen
import com.example.monify_kotlin.feature.transactions.ui.AddIncomeScreen
import com.example.monify_kotlin.feature.transactions.ui.AddExpenseScreen

object Routes {
    const val LOGIN = "login"
    const val HOME  = "home"
    const val SAVINGS = "savings"
    const val ADD_INCOME = "add_income"
    const val ADD_EXPENSE = "add_expense"
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
            HomeScreen(
                onGoSavings = { navController.navigate(Routes.SAVINGS) },
                onAddIncome = { navController.navigate(Routes.ADD_INCOME) },
                onAddExpense = { navController.navigate(Routes.ADD_EXPENSE) }
            )
        }
        composable(Routes.SAVINGS) { SavingsScreen(onBackHome = { navController.navigate(Routes.HOME) }) }
        composable(Routes.ADD_INCOME) { AddIncomeScreen(onBack = { navController.popBackStack() }) }
        composable(Routes.ADD_EXPENSE) { AddExpenseScreen(onBack = { navController.popBackStack() }) }
    }
}
