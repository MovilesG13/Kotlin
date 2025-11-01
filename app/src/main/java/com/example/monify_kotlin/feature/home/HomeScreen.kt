package com.example.monify_kotlin.feature.home.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.MonetizationOn
import androidx.compose.material.icons.outlined.PhoneIphone
import androidx.compose.material.icons.outlined.Receipt
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.core.navigation.Routes
import com.example.monify_kotlin.core.ui.BottomBar
import com.example.monify_kotlin.core.ui.SpeedDialFab
import com.example.monify_kotlin.core.ui.formatMoney
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import com.example.monify_kotlin.data.HomeRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import com.example.monify_kotlin.feature.home.HomeViewModel
import com.example.monify_kotlin.feature.home.HomeViewModelFactory
import com.example.monify_kotlin.feature.home.TransactionType
import com.example.monify_kotlin.feature.home.TransactionUiModel
import com.example.monify_kotlin.feature.home.WeeklyTransactions
import com.example.monify_kotlin.ui.theme.Blue
import com.example.monify_kotlin.ui.theme.Green
import com.example.monify_kotlin.ui.theme.LightBlue
import com.example.monify_kotlin.ui.theme.Red
import com.example.monify_kotlin.ui.theme.SkyBlue
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun HomeScreen(
    onGoSavings: () -> Unit,
    onAddIncome: () -> Unit,
    onAddExpense: () -> Unit,
    onGoReports: () -> Unit = {},
) {
    // --- Dependency Injection Setup ---
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val homeRepo = HomeRepository()
    val goalsRepo = GoalsRepository(db.goalDao())
    val factory = HomeViewModelFactory(homeRepo, goalsRepo, db)
    val vm: HomeViewModel = viewModel(factory = factory)
    // --- End of DI Setup ---

    val ui = vm.state.value

    LaunchedEffect(Unit) {
        vm.load()
    }

    Scaffold(
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
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Spacer(Modifier.height(0.dp)) }

            item {
                BalanceHeader(
                    name = ui.name,
                    month = ui.monthLabel,
                    balance = "$ ${ui.balance.formatMoney()}"
                )
            }

            item {
                FinancialOverviewCard(
                    income = ui.income.toFloat(),
                    expenses = ui.expenses.toFloat(),
                    balance = ui.balance.toFloat(),
                    isLoading = ui.loading,
                    error = ui.error
                )
            }

            item {
                SavingGoalsCard(goals = ui.goals)
            }

            // Weekly Transactions Section
            items(ui.weeklyTransactions) { weeklyTxn ->
                WeeklyTransactionsCard(weeklyTransactions = weeklyTxn)
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

/* -------------------- Balance Header -------------------- */
@Composable
private fun BalanceHeader(name: String, month: String, balance: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
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
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Outlined.MonetizationOn,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Welcome, $name",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Your balance for $month is",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = balance,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/* -------------------- Financial Overview -------------------- */
@Composable
private fun FinancialOverviewCard(income: Float, expenses: Float, balance: Float, isLoading: Boolean, error: String?) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Financial Overview",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(Modifier.height(8.dp))
            if (isLoading) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (error != null) {
                Box(modifier = Modifier.fillMaxWidth().height(180.dp), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: Could not load summary. $error",
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            } else {
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
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val stepY = size.height / gridLines
                repeat(gridLines + 1) { i ->
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.6f),
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
                    val height = (bar.value / maxY).coerceIn(0f, 1f) * chartHeight.value
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .width(38.dp)
                                .height(height.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(bar.color)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(bar.label, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}

/* -------------------- Saving Goals -------------------- */
@Composable
private fun SavingGoalsCard(goals: List<Goal>) {
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
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }
            goals.forEach { GoalRow(it) }
        }
    }
}

@Composable
private fun GoalRow(goal: Goal) {
    val pct = (goal.currentAmount / goal.targetAmount).toFloat().coerceIn(0f, 1f)
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
                Icon(icon, contentDescription = null, tint = Blue)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(goal.title, style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                Text(
                    "$${goal.currentAmount.formatMoney()} of $${goal.targetAmount.formatMoney()}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            Text("${(pct * 100).toInt()}%", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelLarge)
        }
        LinearProgressIndicator(
            progress = { pct },
            modifier = Modifier.fillMaxWidth(),
            trackColor = SkyBlue,
            color = LightBlue
        )
    }
}

/* -------------------- Weekly Transactions -------------------- */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeeklyTransactionsCard(weeklyTransactions: WeeklyTransactions) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Outlined.Receipt,
                    contentDescription = null,
                    tint = Blue
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    weeklyTransactions.weekLabel,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            // Transactions
            if (weeklyTransactions.transactions.isEmpty()) {
                Text(
                    "No transactions this week",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
            } else {
                weeklyTransactions.transactions.forEach { transaction ->
                    TransactionRow(transaction)
                }
            }
        }
    }
}

@Composable
private fun TransactionRow(transaction: TransactionUiModel) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        val icon = if (transaction.type == TransactionType.INCOME) Icons.Default.CheckCircle else Icons.Default.Close
        val color = if (transaction.type == TransactionType.INCOME) Green else Red
        Icon(imageVector = icon, contentDescription = null, tint = color)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                transaction.description,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.SemiBold)
            )
            Text(
                transaction.category,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
        if (!transaction.isSynced) {
            Icon(Icons.Default.Sync, contentDescription = "Pending Sync", tint = Color.Gray, modifier = Modifier.size(16.dp))
            Spacer(Modifier.width(8.dp))
        }
        Text(
            text = "$${transaction.amount.formatMoney()}",
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, color = color)
        )
    }
}
