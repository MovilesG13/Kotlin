package com.example.monify_kotlin.feature.savings.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Savings
import androidx.compose.material.icons.outlined.TrendingUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.core.ui.BottomBar
import com.example.monify_kotlin.core.navigation.Routes
import com.example.monify_kotlin.feature.savings.*
import com.example.monify_kotlin.ui.theme.*

@Composable
fun SavingsScreen(onBackHome: () -> Unit, vm: SavingsViewModel = viewModel()) {
    val state = vm.uiState

    Scaffold(
        containerColor = White,
        bottomBar = {
            BottomBar(Routes.SAVINGS) { route ->
                if (route == Routes.HOME) onBackHome()
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ----------- HEADER FIJO (siempre visible) -----------
            SavingsHeader(
                totalSaved = state.totalSaved,
                totalTarget = state.totalTarget
            )

            // ----------- SEGMENTED TABS -----------
            SavingsTabs(
                selected = state.tab,
                onSelect = vm::selectTab
            )

            // ----------- CONTENIDO DINÁMICO -----------
            Crossfade(targetState = state.tab, label = "savings_tabs") { tab ->
                when (tab) {
                    SavingsTab.SUMMARY   -> SummaryTab(
                        activeGoals = state.activeGoals,
                        monthlySavings = state.monthlySavings,
                        saved = state.totalSaved,
                        target = state.totalTarget
                    )
                    SavingsTab.MY_GOALS  -> MyGoalsTab(goals = state.goals)
                    SavingsTab.PROGRESS  -> ProgressTab()
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

/* ============================================================
   Header
   ============================================================ */

@Composable
private fun SavingsHeader(totalSaved: Float, totalTarget: Float) {
    val pct = (totalSaved / totalTarget).coerceIn(0f, 1f)

    Surface(
        shape = RoundedCornerShape(22.dp),
        color = LightBlue,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            // Línea 1: icono + título + botón "+"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Savings,
                    contentDescription = null,
                    tint = Blue
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Savings Goals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Blue,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { /* mock add goal */ }) {
                    Icon(Icons.Outlined.Add, contentDescription = "Add", tint = Blue)
                }
            }

            // Subtítulo
            Text(
                "Reach your financial goals",
                style = MaterialTheme.typography.bodyMedium,
                color = Black
            )

            // Tarjeta “Total Progress”
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = SkyBlue.copy(alpha = 0.6f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Total Progress",
                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                            color = Black,
                            modifier = Modifier.weight(1f)
                        )
                        Text("$", color = Black)
                    }

                    Spacer(Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "$${totalSaved.formatMoney()}",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Blue
                        )
                        Spacer(Modifier.width(12.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(White)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("${(pct * 100).toInt()}%", color = Blue, style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    Spacer(Modifier.height(4.dp))
                    Text(
                        "of $${totalTarget.formatMoney()} total goal",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Black
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { pct },
                        modifier = Modifier.fillMaxWidth(),
                        trackColor = LightBlue.copy(alpha = 0.5f),
                        color = Blue
                    )
                }
            }
        }
    }
}

/* ============================================================
   Tabs (Summary / My Goals / Progress)
   ============================================================ */

@Composable
private fun SavingsTabs(selected: SavingsTab, onSelect: (SavingsTab) -> Unit) {
    // “Segmented” visual simple con pills
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        TabPill("Summary", selected == SavingsTab.SUMMARY) { onSelect(SavingsTab.SUMMARY) }
        TabPill("My Goals", selected == SavingsTab.MY_GOALS) { onSelect(SavingsTab.MY_GOALS) }
        TabPill("Progress", selected == SavingsTab.PROGRESS) { onSelect(SavingsTab.PROGRESS) }
    }
}

@Composable
private fun TabPill(text: String, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = if (selected) White else Gray.copy(alpha = 0.35f),
        shadowElevation = if (selected) 2.dp else 0.dp,
        onClick = onClick
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal),
            color = if (selected) Blue else Black
        )
    }
}

/* ============================================================
   SUMMARY TAB
   ============================================================ */

@Composable
private fun SummaryTab(activeGoals: Int, monthlySavings: Float, saved: Float, target: Float) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // 2 tarjetas pequeñas
        Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
            SummaryKpiCard(
                title = "Active Goals",
                valuePrimary = "$activeGoals",
                icon = Icons.Outlined.Savings,
                modifier = Modifier.weight(1f)
            )
            SummaryKpiCard(
                title = "Monthly Savings",
                valuePrimary = "$${monthlySavings.formatMoney()}",
                icon = Icons.Outlined.TrendingUp,
                accent = Green,
                modifier = Modifier.weight(1f)
            )
        }

        // Donut "Savings Distribution"
        Surface(shape = RoundedCornerShape(16.dp), tonalElevation = 0.dp, shadowElevation = 2.dp) {
            Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Savings Distribution",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Black
                )
                Spacer(Modifier.height(8.dp))
                DonutChart(
                    saved = saved,
                    target = target,
                    modifier = Modifier.size(220.dp)
                )
            }
        }
    }
}

@Composable
private fun SummaryKpiCard(
    title: String,
    valuePrimary: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    accent: androidx.compose.ui.graphics.Color = Blue,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = White,
        tonalElevation = 0.dp,
        shadowElevation = 4.dp,
        modifier = modifier
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(accent.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) { Icon(icon, contentDescription = null, tint = accent) }
                Spacer(Modifier.width(8.dp))
                Text(title, color = Black, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
            }
            Text(valuePrimary, color = Blue, style = MaterialTheme.typography.titleMedium)
        }
    }
}

/* Donut chart simulado */
@Composable
private fun DonutChart(saved: Float, target: Float, modifier: Modifier = Modifier) {
    val pct = (saved / target).coerceIn(0f, 1f)
    Box(modifier, contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val stroke = 24f
            // fondo gris
            drawArc(
                color = Gray.copy(alpha = 0.6f),
                startAngle = 0f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
            // porción verde (saved)
            drawArc(
                color = Green,
                startAngle = -90f,
                sweepAngle = 360f * pct,
                useCenter = false,
                style = Stroke(width = stroke, cap = StrokeCap.Round)
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$${saved.formatMoney()}", color = Green, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
            Text("Saved", color = Black, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/* ============================================================
   MY GOALS TAB
   ============================================================ */

@Composable
private fun MyGoalsTab(goals: List<SavingGoal>) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        goals.forEach { GoalCard(it) }
    }
}

@Composable
private fun GoalCard(goal: SavingGoal) {
    Surface(shape = RoundedCornerShape(16.dp), shadowElevation = 3.dp) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                // icono circular
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Blue.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) { Icon(goal.icon, contentDescription = null, tint = Blue) }

                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(goal.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Black)
                    Text("Goal: $${goal.target.formatMoney()}", style = MaterialTheme.typography.bodyMedium, color = Black)
                }
                // acciones (mock)
                IconButton(onClick = { /* edit */ }) { Icon(Icons.Outlined.Edit, contentDescription = null, tint = Blue) }
                IconButton(onClick = { /* delete */ }) { Icon(Icons.Outlined.Delete, contentDescription = null, tint = Red) }
            }

            // Progress row
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Progress: ${(goal.progress * 100).toInt()}%", color = Black, modifier = Modifier.weight(1f))
                Text("$${goal.saved.formatMoney()}", color = Black)
            }
            LinearProgressIndicator(
                progress = { goal.progress },
                modifier = Modifier.fillMaxWidth(),
                trackColor = LightBlue.copy(alpha = 0.5f),
                color = Blue
            )

            // metrics
            Row {
                Text("Missing $${goal.missing.formatMoney()}", modifier = Modifier.weight(1f), color = Black)
                Text("Monthly $${goal.monthly.formatMoney()}", modifier = Modifier.weight(1f), color = Black)
                Text("Est. Time ${goal.estMonths} months", modifier = Modifier.weight(1f), color = Black)
            }

            // Add money CTA
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { /* add money */ },
                    colors = ButtonDefaults.buttonColors(containerColor = SkyBlue),
                    modifier = Modifier.weight(1f)
                ) { Text("+  Add Money", color = Blue) }

                Spacer(Modifier.width(12.dp))
                FilledTonalIconButton(onClick = { /* open calendar */ }) {
                    Icon(Icons.Outlined.CalendarMonth, contentDescription = null)
                }
            }
        }
    }
}

/* ============================================================
   PROGRESS TAB
   ============================================================ */

@Composable
private fun ProgressTab() {
    // mock sencillo: tarjeta con texto y una barra de progreso “promedio”
    Surface(shape = RoundedCornerShape(16.dp), shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Progress", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Black)
            Text("Monthly trend (mock)", color = Black)
            LinearProgressIndicator(
                progress = { 0.26f },
                modifier = Modifier.fillMaxWidth(),
                trackColor = LightBlue.copy(alpha = 0.5f),
                color = Blue
            )
        }
    }
}

/* ============================================================
   utils
   ============================================================ */

private fun Float.formatMoney(): String = "%,.2f".format(this)

