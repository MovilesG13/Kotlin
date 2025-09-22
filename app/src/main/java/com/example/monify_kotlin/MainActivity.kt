package com.example.monify_kotlin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import com.example.monify_kotlin.core.navigation.AppNavGraph
import com.example.monify_kotlin.ui.theme.FinanceTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FinanceTheme {
                val nav = rememberNavController()
                AppNavGraph(navController = nav)
            }
        }
    }
}
