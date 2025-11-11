package com.example.monify_kotlin.feature.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.ui.theme.*
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onGoSavings: () -> Unit,          // Tu parámetro original
    onAddIncome: () -> Unit,          // Tu parámetro original
    onAddExpense: () -> Unit,
    onGoReports: () -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val state by viewModel.state.collectAsState()
    val dateFormatter = remember { DateTimeFormatter.ofPattern("MMM d, h:mm a") }

    Scaffold(
        containerColor = White,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = White
                ),
                title = {
                    Column {
                        Text(
                            "Hello, ${state.name}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            state.monthLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                },
                actions = {
                    // Badge de transacciones pendientes
                    AnimatedVisibility(
                        visible = state.hasPendingTransactions,
                        enter = fadeIn() + scaleIn(),
                        exit = fadeOut() + scaleOut()
                    ) {
                        BadgedBox(
                            badge = {
                                Badge(
                                    containerColor = Orange,
                                    contentColor = White
                                ) {
                                    Icon(
                                        Icons.Filled.CloudOff,
                                        contentDescription = "Pending sync",
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            },
                            modifier = Modifier.padding(end = 8.dp)
                        ) {
                            Box(modifier = Modifier.size(24.dp))
                        }
                    }

                    // Botón de sincronización manual
                    IconButton(
                        onClick = { viewModel.forceSyncNow() },
                        enabled = !state.isSyncing
                    ) {
                        if (state.isSyncing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = Purple
                            )
                        } else {
                            Icon(
                                Icons.Filled.Sync,
                                contentDescription = "Sync",
                                tint = if (state.hasPendingTransactions) Orange else Color.Gray
                            )
                        }
                    }

                    // Botón de Reports (nuevo)
                    IconButton(onClick = onGoReports) {
                        Icon(
                            Icons.Filled.Assessment,
                            contentDescription = "Reports",
                            tint = Color.Gray
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            // Botones flotantes para agregar transacciones
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FloatingActionButton(
                    onClick = onAddIncome,
                    containerColor = Green,
                    contentColor = White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.TrendingUp, "Add Income")
                }
                FloatingActionButton(
                    onClick = onAddExpense,
                    containerColor = Red,
                    contentColor = White,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(Icons.Filled.TrendingDown, "Add Expense")
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Indicador de sincronización en progreso
            AnimatedVisibility(
                visible = state.isSyncing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    color = Purple,
                    trackColor = Purple.copy(alpha = 0.2f)
                )
            }

            // Timestamp de última sincronización
            AnimatedVisibility(
                visible = state.lastSyncTime != null && !state.isSyncing,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                LastSyncIndicator(state.lastSyncTime)
            }

            // Banner de modo offline
            AnimatedVisibility(
                visible = state.hasPendingTransactions,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                OfflineBanner(
                    onClick = { viewModel.forceSyncNow() }
                )
            }

            // Contenido principal
            if (state.loading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Purple)
                }
            } else if (state.error != null) {
                ErrorView(
                    message = state.error!!,
                    onRetry = { viewModel.refreshBalanceFromBackend() }
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Balance Card
                    item {
                        BalanceCard(
                            income = state.income,
                            expenses = state.expenses,
                            balance = state.balance,
                            onGoReports = onGoReports  // Agregado para navegar a reports
                        )
                    }

                    // Goals Section (si hay goals)
                    if (state.goals.isNotEmpty()) {
                        item {
                            GoalsSection(
                                goals = state.goals,
                                onNavigateToGoals = onGoSavings  // Mapea a tu onGoSavings
                            )
                        }
                    }

                    // Weekly Transactions
                    if (state.weeklyTransactions.isNotEmpty()) {
                        item {
                            Text(
                                "Recent Transactions",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                        }

                        items(state.weeklyTransactions) { weeklyGroup ->
                            WeeklyTransactionGroup(
                                weeklyTransactions = weeklyGroup,
                                dateFormatter = dateFormatter
                            )
                        }
                    } else {
                        item {
                            EmptyTransactionsView(
                                onAddExpense = onAddExpense,
                                onAddIncome = onAddIncome
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LastSyncIndicator(lastSyncTime: Long?) {
    lastSyncTime?.let { timestamp ->
        val minutesAgo = TimeUnit.MILLISECONDS.toMinutes(
            System.currentTimeMillis() - timestamp
        )

        val timeText = when {
            minutesAgo < 1 -> "Just now"
            minutesAgo < 60 -> "$minutesAgo min ago"
            minutesAgo < 1440 -> "${minutesAgo / 60} hours ago"
            else -> "${minutesAgo / 1440} days ago"
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFFE8F5E9)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = Green,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Synced $timeText",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF2E7D32)
                )
            }
        }
    }
}

@Composable
private fun OfflineBanner(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFFFF3CD)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Filled.CloudOff,
                contentDescription = null,
                tint = Color(0xFFFF9800),
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "Pending Sync",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF856404)
                )
                Text(
                    "You have unsaved transactions. Tap to sync now.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF856404)
                )
            }
            Icon(
                Icons.Filled.Sync,
                contentDescription = "Sync",
                tint = Color(0xFFFF9800)
            )
        }
    }
}

@Composable
private fun BalanceCard(
    income: Double,
    expenses: Double,
    balance: Double,
    onGoReports: () -> Unit  // Agregado
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onGoReports),  // Click para ir a Reports
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Purple
        )
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Total Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = White.copy(alpha = 0.8f)
                )
                // Indicador de que es clickeable
                Icon(
                    Icons.Filled.Assessment,
                    contentDescription = "View Reports",
                    tint = White.copy(alpha = 0.6f),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                "$${String.format("%.2f", balance)}",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = White
            )
            Spacer(Modifier.height(24.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Income
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Green.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.TrendingUp,
                                contentDescription = null,
                                tint = Green,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Income",
                                style = MaterialTheme.typography.bodySmall,
                                color = White.copy(alpha = 0.7f)
                            )
                            Text(
                                "$${String.format("%.2f", income)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }

                // Expenses
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Red.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Filled.TrendingDown,
                                contentDescription = null,
                                tint = Red,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text(
                                "Expenses",
                                style = MaterialTheme.typography.bodySmall,
                                color = White.copy(alpha = 0.7f)
                            )
                            Text(
                                "$${String.format("%.2f", expenses)}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = White
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun GoalsSection(
    goals: List<com.example.monify_kotlin.data.Goal>,
    onNavigateToGoals: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Goals",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = onNavigateToGoals) {
                    Text("See All")
                }
            }

            Spacer(Modifier.height(8.dp))

            goals.take(2).forEach { goal ->
                GoalItem(goal)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
private fun GoalItem(goal: com.example.monify_kotlin.data.Goal) {
    val progress = (goal.currentAmount / goal.targetAmount).coerceIn(0.0, 1.0)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                goal.title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "$${String.format("%.0f", goal.currentAmount)} / $${String.format("%.0f", goal.targetAmount)}",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = progress.toFloat(),
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = Purple,
            trackColor = Purple.copy(alpha = 0.2f)
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun WeeklyTransactionGroup(
    weeklyTransactions: WeeklyTransactions,
    dateFormatter: DateTimeFormatter
) {
    Column {
        Text(
            weeklyTransactions.weekLabel,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.Gray,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                weeklyTransactions.transactions.forEach { transaction ->
                    TransactionItem(transaction, dateFormatter)
                    if (transaction != weeklyTransactions.transactions.last()) {
                        Divider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = Color.LightGray.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun TransactionItem(
    transaction: TransactionUiModel,
    dateFormatter: DateTimeFormatter
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            // Icono
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (transaction.type == TransactionType.INCOME)
                            Green.copy(alpha = 0.15f)
                        else
                            Red.copy(alpha = 0.15f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (transaction.type == TransactionType.INCOME)
                        Icons.Filled.TrendingUp
                    else
                        Icons.Filled.TrendingDown,
                    contentDescription = null,
                    tint = if (transaction.type == TransactionType.INCOME) Green else Red,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // Descripción y fecha
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        transaction.description,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Indicador de no sincronizado
                    if (!transaction.isSynced) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            Icons.Filled.CloudOff,
                            contentDescription = "Not synced",
                            tint = Orange,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
                Text(
                    transaction.dateTime.format(dateFormatter),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        // Monto
        Text(
            "${if (transaction.type == TransactionType.INCOME) "+" else "-"}$${String.format("%.2f", transaction.amount)}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = if (transaction.type == TransactionType.INCOME) Green else Red
        )
    }
}

@Composable
private fun EmptyTransactionsView(
    onAddExpense: () -> Unit,
    onAddIncome: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Assignment,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.Gray.copy(alpha = 0.5f)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No transactions yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Text(
                "Start tracking your finances",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray.copy(alpha = 0.7f)
            )
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onAddExpense,
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Red
                    )
                ) {
                    Icon(Icons.Filled.TrendingDown, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Expense")
                }
                Button(
                    onClick = onAddIncome,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Green
                    )
                ) {
                    Icon(Icons.Filled.TrendingUp, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Income")
                }
            }
        }
    }
}

@Composable
private fun ErrorView(
    message: String,
    onRetry: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = Red.copy(alpha = 0.1f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Error,
                contentDescription = null,
                tint = Red,
                modifier = Modifier.size(48.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "Oops!",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Red
            )
            Spacer(Modifier.height(8.dp))
            Text(
                message,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Red
                )
            ) {
                Icon(Icons.Filled.Refresh, null)
                Spacer(Modifier.width(8.dp))
                Text("Retry")
            }
        }
    }
}