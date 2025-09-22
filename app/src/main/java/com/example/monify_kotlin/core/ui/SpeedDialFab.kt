package com.example.monify_kotlin.core.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.monify_kotlin.ui.theme.Green
import com.example.monify_kotlin.ui.theme.Red
import com.example.monify_kotlin.ui.theme.LightBlue

@Composable
fun SpeedDialFab(
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.BottomEnd) {
        Column(horizontalAlignment = Alignment.End) {
            AnimatedVisibility(visible = expanded) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SmallFloatingActionButton(onClick = onAddExpense, containerColor = Red) {
                        Icon(Icons.Filled.TrendingDown, contentDescription = "Gasto")
                    }
                    SmallFloatingActionButton(onClick = onAddIncome, containerColor = Green) {
                        Icon(Icons.Filled.TrendingUp, contentDescription = "Ingreso")
                    }
                }
            }
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = LightBlue,
                modifier = Modifier.padding(8.dp)
            ) { Icon(Icons.Filled.Add, contentDescription = "Agregar") }
        }
    }
}

