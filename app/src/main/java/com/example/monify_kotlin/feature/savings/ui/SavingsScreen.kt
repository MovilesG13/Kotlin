package com.example.monify_kotlin.feature.savings.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavingsScreen(onBackHome: () -> Unit) {
    Scaffold(topBar = {
        TopAppBar(title = { Text("Ahorros") })
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Text("Pantalla de Ahorros (mock)")
            Spacer(Modifier.height(8.dp))
            Text("Aquí irán metas, progreso y distribución…")
            Spacer(Modifier.height(16.dp))
            Button(onClick = onBackHome) { Text("Volver a Inicio") }
        }
    }
}
