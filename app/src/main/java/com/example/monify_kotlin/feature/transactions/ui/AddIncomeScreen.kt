package com.example.monify_kotlin.feature.transactions.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.monify_kotlin.ui.theme.Green
import com.example.monify_kotlin.ui.theme.Red

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddIncomeScreen(onBack: () -> Unit) {
    Scaffold(topBar = { TopAppBar(title = { Text("Agregar Ingreso") }) }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Formulario de ingreso (mock)")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBack, colors = ButtonDefaults.buttonColors(containerColor = Green)) {
                Text("Guardar (mock)")
            }
        }
    }
}