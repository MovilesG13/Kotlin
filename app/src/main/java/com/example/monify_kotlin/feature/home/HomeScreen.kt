package com.example.monify_kotlin.feature.home.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.monify_kotlin.core.navigation.Routes
import com.example.monify_kotlin.core.ui.BottomBar
import com.example.monify_kotlin.core.ui.SpeedDialFab
import com.example.monify_kotlin.ui.theme.*

@Composable
fun HomeScreen(
    onGoSavings: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onGoReports: () -> Unit = {},
) {
    Scaffold(
        containerColor = White,
        bottomBar = {
            BottomBar(Routes.HOME) { route ->
                when (route) {
                    Routes.SAVINGS -> onGoSavings()
                    Routes.REPORTS -> onGoReports()
                }
            }
        },
        floatingActionButton = { SpeedDialFab(onAddIncome, onAddExpense) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            BalanceHeader(
                name = "Sofia",
                month = "September",
                balance = "$ 643.76"
            )

            FinancialOverviewCard(
                income = 2000f,
                expenses = 1350f,
                balance = 650f
            )

            SavingGoalsCard(
                goals = listOf(
                    GoalUi(iconTint = Blue, title = "New Phone", current = 650f, target = 1200f),
                    GoalUi(iconTint = Blue, title = "New House", current = 12500f, target = 50000f)
                )
            )
        }
    }
}

/* -------------------- Header de Balance -------------------- */

@Composable
private fun BalanceHeader(name: String, month: String, balance: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlue),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Blue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MonetizationOn,
                    contentDescription = null,
                    tint = Blue
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Welcome, $name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Blue
                )
                Text(
                    text = "Your balance for $month is",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = balance,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Blue
                )
            }
        }
    }
}

/* -------------------- Resumen Financiero (gráfico simulado) -------------------- */

@Composable
private fun FinancialOverviewCard(income: Float, expenses: Float, balance: Float) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Financial Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Black
            )
            Spacer(Modifier.height(8.dp))
            BarChart(
                bars = listOf(
                    Bar("Income", income, Green),
                    Bar("Expenses", expenses, Red),
                    Bar("Balance", balance, Blue)
                ),
                maxY = maxOf(income, expenses, balance).coerceAtLeast(1f)
            )
        }
    }
}

data class Bar(val label: String, val value: Float, val color: Color)

@Composable
private fun BarChart(bars: List<Bar>, maxY: Float, gridLines: Int = 4) {
    val chartHeight = 180.dp
    Column(Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(chartHeight)
                .background(Color.Transparent)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stepY = size.height / gridLines
                repeat(gridLines + 1) { i ->
                    drawLine(
                        color = Gray.copy(alpha = 0.6f),
                        start = androidx.compose.ui.geometry.Offset(0f, size.height - i * stepY),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height - i * stepY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(14f, 14f), 0f)
                    )
                }
            }
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                bars.forEach { bar ->
                    val heightRatio = (bar.value / maxY).coerceIn(0f, 1f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .fillMaxHeight(heightRatio)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bar.color)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(bar.label, style = MaterialTheme.typography.bodyMedium, color = Black)
                    }
                }
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(
            "Income  •  Expenses  •  Balance",
            color = Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* -------------------- Saving Goals -------------------- */

data class GoalUi(val iconTint: Color, val title: String, val current: Float, val target: Float)

@Composable
private fun SavingGoalsCard(goals: List<GoalUi>) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.MonetizationOn,
                    contentDescription = null,
                    tint = Blue
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Saving Goals",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Black
                )
            }
            goals.forEach { GoalRow(it) }
        }
    }
}

@Composable
private fun GoalRow(goal: GoalUi) {
    val pct = (goal.current / goal.target).coerceIn(0f, 1f)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Blue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                val icon = if (goal.title.contains("Phone", true)) Icons.Outlined.PhoneIphone else Icons.Outlined.Home
                Icon(icon, contentDescription = null, tint = goal.iconTint)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold), color = Black)
                Text(
                    "$${goal.current.formatMoney()} of $${goal.target.formatMoney()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(Green)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("${(pct * 100).toInt()}%", color = White, style = MaterialTheme.typography.labelLarge)
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "$${(goal.target - goal.current).coerceAtLeast(0f).formatMoney()} left",
                style = MaterialTheme.typography.bodyMedium,
                color = Black
            )
        }
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth(),
            trackColor = SkyBlue,
            color = LightBlue
        )
    }
}

/* -------------------- utils -------------------- */

private fun Float.formatMoney(): String = "%,.2f".format(this)

