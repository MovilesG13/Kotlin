package com.example.monify_kotlin.feature.home

import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.Goal
import com.example.monify_kotlin.data.GoalsRepository
import com.example.monify_kotlin.data.HomeRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import com.example.monify_kotlin.data.cache.PendingExpense
import com.example.monify_kotlin.data.cache.PendingIncome
import com.example.monify_kotlin.data.cache.SyncedTransaction
import com.example.monify_kotlin.core.util.ConnectivityObserver
import com.example.monify_kotlin.data.sync.TransactionSyncWorker

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
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
    val error: String? = null,
    val isSyncing: Boolean = false,
    val hasPendingTransactions: Boolean = false,
    val lastSyncTime: Long? = null
)

class HomeViewModel(
    application: Application,
) : AndroidViewModel(application) {

    private val repo: HomeRepository = HomeRepository()
    private val goalsRepo: GoalsRepository = GoalsRepository()

    private val database = AppDatabase.getDatabase(application)
    private val connectivityObserver = ConnectivityObserver(application)

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    // BroadcastReceiver para escuchar cuando termina la sincronización
    private val syncCompletedReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == TransactionSyncWorker.SYNC_COMPLETED_ACTION) {
                val syncedCount = intent.getIntExtra(TransactionSyncWorker.EXTRA_SYNCED_COUNT, 0)
                android.util.Log.d("HomeViewModel", "📢 Received sync completed broadcast - Count: $syncedCount")

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Recargar el balance desde el backend
                    refreshBalanceFromBackend()
                }
            }
        }
    }

    init {
        // Registrar el BroadcastReceiver
        val filter = IntentFilter(TransactionSyncWorker.SYNC_COMPLETED_ACTION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            application.registerReceiver(syncCompletedReceiver, filter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            ContextCompat.registerReceiver(
                application,
                syncCompletedReceiver,
                filter,
                ContextCompat.RECEIVER_NOT_EXPORTED
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            loadReactiveData()
            observeConnectivity()
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Desregistrar el BroadcastReceiver cuando el ViewModel se destruye
        try {
            getApplication<Application>().unregisterReceiver(syncCompletedReceiver)
        } catch (e: Exception) {
            android.util.Log.w("HomeViewModel", "Failed to unregister receiver", e)
        }
    }

    /**
     * Observa el estado de conectividad y recarga balance cuando vuelve online
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun observeConnectivity() {
        viewModelScope.launch {
            var wasOffline = false
            connectivityObserver.isConnected.collect { isConnected ->
                // Cuando vuelve online después de estar offline, recargar balance
                if (isConnected && wasOffline) {
                    android.util.Log.d("HomeViewModel", "📡 Back online - reloading balance in 2 seconds")
                    // Dar tiempo al Worker para sincronizar primero
                    kotlinx.coroutines.delay(2000)
                    refreshBalanceFromBackend()
                }
                wasOffline = !isConnected
            }
        }
    }

    /**
     * Carga datos reactivos combinando las 3 tablas de Room
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private fun loadReactiveData() {
        android.util.Log.d("HomeViewModel", "🚀 Starting reactive data loading")

        val syncedFlow = database.syncedTransactionDao().getAllTransactions()
        val pendingExpensesFlow = database.pendingExpenseDao().getUnsyncedExpenses()
        val pendingIncomesFlow = database.pendingIncomeDao().getUnsyncedIncomes()

        viewModelScope.launch {
            try {
                // Cargar datos remotos iniciales
                val today = LocalDate.now()
                val ym = "%04d-%02d".format(today.year, today.monthValue)
                val name = repo.getProfileName()
                val (inc, exp, bal) = repo.getMonthlySummary(ym)
                val goals = goalsRepo.getGoals()

                android.util.Log.d("HomeViewModel", "💰 Initial balance: $bal (Income: $inc, Expenses: $exp)")


                combine(syncedFlow, pendingExpensesFlow, pendingIncomesFlow) { synced, pendingExp, pendingInc ->
                    val allTransactions = mutableListOf<TransactionUiModel>()
                    allTransactions.addAll(synced.map { it.toUiModel() })
                    allTransactions.addAll(pendingExp.map { it.toUiModel() })
                    allTransactions.addAll(pendingInc.map { it.toUiModel() })

                    android.util.Log.d("HomeViewModel", "📊 Transactions updated - Synced: ${synced.size}, Pending Expenses: ${pendingExp.size}, Pending Incomes: ${pendingInc.size}")

                    val weeklyTxns = groupTransactionsByWeek(allTransactions)
                    val hasPending = pendingExp.isNotEmpty() || pendingInc.isNotEmpty()

                    _state.value.copy(
                        loading = false,
                        name = name,
                        monthLabel = today.month.name.lowercase().replaceFirstChar { it.titlecase() },
                        income = inc,
                        expenses = exp,
                        balance = bal,
                        goals = goals,
                        weeklyTransactions = weeklyTxns,
                        hasPendingTransactions = hasPending
                    )
                }
                    .catch { e ->
                        android.util.Log.e("HomeViewModel", "❌ Error in data flow", e)
                        _state.update { it.copy(
                            loading = false,
                            error = e.message ?: "Error loading transactions"
                        ) }
                    }
                    .collect { updatedState ->
                        _state.value = updatedState
                    }

            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ Error loading initial data", e)
                _state.update { it.copy(
                    loading = false,
                    error = e.message ?: "Error loading summary"
                ) }
            }
        }
    }

    /**
     * CRÍTICO: Recarga el balance desde el backend después de sincronizar
     */
    @RequiresApi(Build.VERSION_CODES.O)
    fun refreshBalanceFromBackend() {
        viewModelScope.launch {
            try {
                android.util.Log.d("HomeViewModel", "🔄 Refreshing balance from backend...")
                _state.update { it.copy(isSyncing = true) }

                val today = LocalDate.now()
                val ym = "%04d-%02d".format(today.year, today.monthValue)
                val (inc, exp, bal) = repo.getMonthlySummary(ym)
                val goals = goalsRepo.getGoals()

                android.util.Log.d("HomeViewModel", "✅ Balance refreshed: $bal (Income: $inc, Expenses: $exp)")

                _state.update {
                    it.copy(
                        income = inc,
                        expenses = exp,
                        balance = bal,
                        goals = goals,
                        isSyncing = false,
                        lastSyncTime = System.currentTimeMillis()
                    )
                }
            } catch (e: Exception) {
                android.util.Log.e("HomeViewModel", "❌ Error refreshing balance", e)
                _state.update { it.copy(isSyncing = false) }
            }
        }
    }

    /**
     * Fuerza una sincronización inmediata (para botón de refresh manual)
     */
    fun forceSyncNow() {
        android.util.Log.d("HomeViewModel", "⚡ Force sync requested")
        TransactionSyncWorker.scheduleImmediate(getApplication())
        _state.update { it.copy(isSyncing = true) }
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

// Extension functions
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
        ZoneId.systemDefault())
)
