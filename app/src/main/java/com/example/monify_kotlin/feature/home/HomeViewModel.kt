package com.example.monify_kotlin.feature.home

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import com.example.monify_kotlin.data.HomeRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import com.example.monify_kotlin.data.cache.PendingExpense
import com.example.monify_kotlin.data.cache.PendingIncome
import com.example.monify_kotlin.data.cache.SyncedTransaction
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.temporal.WeekFields
import java.util.*

data class HomeUiState(
    val loading: Boolean = true,
    val name: String = "User",
    val monthLabel: String = "",
    val income: Double = 0.0,
    val expenses: Double = 0.0,
    val balance: Double = 0.0,
    val goals: List<Goal> = emptyList(),
    val weeklyTransactions: List<WeeklyTransactions> = emptyList(),
    val error: String? = null
)

class HomeViewModel(
    private val repo: HomeRepository = HomeRepository(),
    private val goalsRepo: GoalsRepository = GoalsRepository(),
    private var database: AppDatabase? = null
) : ViewModel() {

    var state = androidx.compose.runtime.mutableStateOf(HomeUiState())
        private set

    fun setDatabase(db: AppDatabase) {
        database = db
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun load() {
        viewModelScope.launch {
            try {
                val today = LocalDate.now()
                val ym = "%04d-%02d".format(today.year, today.monthValue)
                val name = repo.getProfileName()
                val (inc, exp, bal) = repo.getMonthlySummary(ym)
                val goals = goalsRepo.getGoals()

                // Load transactions
                val weeklyTxns = loadWeeklyTransactions()

                state.value = HomeUiState(
                    loading = false,
                    name = name,
                    monthLabel = today.month.name.lowercase().replaceFirstChar { it.titlecase() },
                    income = inc,
                    expenses = exp,
                    balance = bal,
                    goals = goals,
                    weeklyTransactions = weeklyTxns
                )
            } catch (e: Exception) {
                state.value = state.value.copy(loading = false, error = e.message ?: "Error")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun loadWeeklyTransactions(): List<WeeklyTransactions> {
        val db = database ?: return emptyList()

        val allTransactions = mutableListOf<TransactionUiModel>()

        // Load synced transactions
        val syncedTxns = db.syncedTransactionDao().getAllTransactions().first()
        allTransactions.addAll(syncedTxns.map { it.toUiModel() })

        // Load pending expenses (unsynced)
        val pendingExpenses = db.pendingExpenseDao().getUnsyncedExpenses().first()
        allTransactions.addAll(pendingExpenses.map { it.toUiModel() })

        // Load pending incomes (unsynced)
        val pendingIncomes = db.pendingIncomeDao().getUnsyncedIncomes().first()
        allTransactions.addAll(pendingIncomes.map { it.toUiModel() })

        // Group by week
        return groupTransactionsByWeek(allTransactions)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun groupTransactionsByWeek(transactions: List<TransactionUiModel>): List<WeeklyTransactions> {
        val weekField = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
        val grouped = transactions
            .sortedByDescending { it.timestamp }
            .groupBy { txn ->
                val date = txn.dateTime.toLocalDate()
                Pair(date.year, date.get(weekField))
            }

        return grouped.map { (weekKey, txns) ->
            val firstTxn = txns.first().dateTime
            val startOfWeek = firstTxn.toLocalDate().with(
                WeekFields.of(Locale.getDefault()).dayOfWeek(), 1
            ).atStartOfDay()
            val endOfWeek = startOfWeek.plusDays(6).withHour(23).withMinute(59)

            val weekLabel = if (isCurrentWeek(startOfWeek.toLocalDate())) {
                "This Week"
            } else if (isLastWeek(startOfWeek.toLocalDate())) {
                "Last Week"
            } else {
                "${startOfWeek.toLocalDate()} - ${endOfWeek.toLocalDate()}"
            }

            WeeklyTransactions(
                weekLabel = weekLabel,
                startDate = startOfWeek,
                endDate = endOfWeek,
                transactions = txns
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isCurrentWeek(date: LocalDate): Boolean {
        val today = LocalDate.now()
        val weekField = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
        return date.year == today.year && date.get(weekField) == today.get(weekField)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun isLastWeek(date: LocalDate): Boolean {
        val lastWeek = LocalDate.now().minusWeeks(1)
        val weekField = WeekFields.of(Locale.getDefault()).weekOfWeekBasedYear()
        return date.year == lastWeek.year && date.get(weekField) == lastWeek.get(weekField)
    }
}

// Extension functions to convert to UI models
@RequiresApi(Build.VERSION_CODES.O)
private fun SyncedTransaction.toUiModel() = TransactionUiModel(
    id = id,
    type = if (type == "expense") TransactionType.EXPENSE else TransactionType.INCOME,
    amount = amount,
    description = description,
    category = category,
    timestamp = timestamp,
    isSynced = true,
    dateTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    )
)

@RequiresApi(Build.VERSION_CODES.O)
private fun PendingExpense.toUiModel() = TransactionUiModel(
    id = id,
    type = TransactionType.EXPENSE,
    amount = amount,
    description = description ?: categoryId,
    category = categoryId,
    timestamp = timestamp,
    isSynced = false,
    dateTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    )
)

@RequiresApi(Build.VERSION_CODES.O)
private fun PendingIncome.toUiModel() = TransactionUiModel(
    id = id,
    type = TransactionType.INCOME,
    amount = amount,
    description = description ?: source,
    category = source,
    timestamp = timestamp,
    isSynced = false,
    dateTime = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(timestamp),
        ZoneId.systemDefault()
    )
)
