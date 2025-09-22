package com.example.monify_kotlin.feature.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.monify_kotlin.R
import com.example.monify_kotlin.core.ui.BottomBar
import com.example.monify_kotlin.core.ui.SpeedDialFab
import com.example.monify_kotlin.core.navigation.Routes
import com.example.monify_kotlin.ui.theme.*
import com.example.monify_kotlin.R.drawable

@Composable
fun HomeScreen(
    onGoSavings: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
) {
    val navController = rememberNavController() // sólo para obtener route actual del BottomBar en este mock
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route ?: Routes.HOME

    Scaffold(
        containerColor = SkyBlue,                              // header/fondo
        bottomBar = { BottomBar(currentRoute) { route ->
            if (route == Routes.SAVINGS) onGoSavings()
        }},
        floatingActionButton = {
            SpeedDialFab(onAddIncome = onAddIncome, onAddExpense = onAddExpense)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Encabezado con logo + balance
            Image(
                painter = painterResource(id = R.drawable.monify_logo),
                contentDescription = null,
                modifier = Modifier.size(72.dp)
            )
            Text(
                "Monify",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = Black,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(12.dp))

            // Balance card
            Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("¡Hola, Sofía!", style = MaterialTheme.typography.titleMedium, color = Black)
                    Text("Balance de Septiembre", style = MaterialTheme.typography.bodyMedium, color = Gray)
                    Spacer(Modifier.height(6.dp))
                    Text("$1.994,50", style = MaterialTheme.typography.titleLarge, color = Blue)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Resumen financiero (dummy)
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Resumen Financiero", style = MaterialTheme.typography.titleMedium, color = Black)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .background(White),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Gráfico (dummy)", color = Gray)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text("Ingresos (verde) · Gastos (rojo) · Balance (azul)", color = Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // Metas de ahorro (dummy)
            Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text("Metas de Ahorro", style = MaterialTheme.typography.titleMedium, color = Black)
                    Spacer(Modifier.height(8.dp))
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .height(90.dp)
                            .background(White),
                        contentAlignment = Alignment.Center
                    ) { Text("Progreso (dummy)", color = Gray) }
                }
            }
        }
    }
}
