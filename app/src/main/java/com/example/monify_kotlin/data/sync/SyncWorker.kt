package com.example.monify_kotlin.data.sync

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.net.toUri
import androidx.work.*
import com.example.monify_kotlin.data.ExpenseRepository
import com.example.monify_kotlin.data.IncomeRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import kotlinx.coroutines.flow.first
import java.io.File
import java.util.concurrent.TimeUnit

class TransactionSyncWorker(
    private val context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val expenseRepo = ExpenseRepository()
    private val incomeRepo = IncomeRepository()

    override suspend fun doWork(): Result {
        Log.d(TAG, "🔄 ========== SYNC STARTED ========== Attempt ${runAttemptCount + 1}")

        return try {
            val expensesSynced = syncPendingExpenses()
            val incomesSynced = syncPendingIncomes()

            Log.d(TAG, "✅ ========== SYNC COMPLETED ========== Expenses: $expensesSynced, Incomes: $incomesSynced")

            // Enviar broadcast para notificar que la sincronización terminó
            sendSyncCompletedBroadcast(expensesSynced + incomesSynced)

            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "❌ Sync failed: ${e.message}", e)
            e.printStackTrace()

            if (runAttemptCount < 3) {
                Log.d(TAG, "🔁 Retrying sync... (${runAttemptCount + 1}/3)")
                Result.retry()
            } else {
                Log.e(TAG, "💀 Max retry attempts reached")
                Result.failure()
            }
        }
    }

    private suspend fun syncPendingExpenses(): Int {
        val pendingExpenses = database.pendingExpenseDao().getUnsyncedExpenses().first()
        Log.d(TAG, "📤 Found ${pendingExpenses.size} pending expenses to sync")

        if (pendingExpenses.isEmpty()) {
            Log.d(TAG, "✨ No pending expenses to sync")
            return 0
        }

        var syncedCount = 0
        pendingExpenses.forEach { expense ->
            try {
                Log.d(TAG, "💳 Syncing expense ID ${expense.id}: ${expense.description} - $${expense.amount}")

                // Upload image if exists and not uploaded yet
                var imageUrl = expense.receiptImageUrl
                if (expense.receiptImageLocalPath != null && imageUrl == null) {
                    Log.d(TAG, "📸 Uploading receipt image for expense ${expense.id}")

                    // CORRECCIÓN: Convertir path a Uri de manera segura
                    val file = File(expense.receiptImageLocalPath)
                    if (file.exists()) {
                        val uri = Uri.fromFile(file)
                        imageUrl = expenseRepo.uploadReceiptImage(uri)
                        Log.d(TAG, "✅ Image uploaded successfully: $imageUrl")
                    } else {
                        Log.w(TAG, "⚠️ Receipt image file not found: ${expense.receiptImageLocalPath}")
                    }
                }

                // Create expense in Firebase
                Log.d(TAG, "☁️ Creating expense in Firebase...")
                expenseRepo.createExpense(
                    amount = expense.amount,
                    currency = expense.currency,
                    categoryId = expense.categoryId,
                    description = expense.description,
                    note = expense.note,
                    date = expense.date,
                    receiptImageUrl = imageUrl
                )
                Log.d(TAG, "✅ Expense created in Firebase")

                // Add to synced transactions
                database.syncedTransactionDao().insert(
                    com.example.monify_kotlin.data.cache.SyncedTransaction(
                        type = "expense",
                        amount = expense.amount,
                        description = expense.description ?: expense.categoryId,
                        category = expense.categoryId,
                        date = expense.date,
                        timestamp = expense.timestamp,
                        isSynced = true
                    )
                )
                Log.d(TAG, "✅ Expense added to synced_transactions table")

                // Delete from pending
                database.pendingExpenseDao().delete(expense)
                Log.d(TAG, "✅ Expense ${expense.id} removed from pending_expenses")

                syncedCount++

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to sync expense ${expense.id}: ${e.message}", e)
                e.printStackTrace()

                database.pendingExpenseDao().incrementSyncAttempts(
                    expense.id,
                    System.currentTimeMillis()
                )
            }
        }

        return syncedCount
    }

    private suspend fun syncPendingIncomes(): Int {
        val pendingIncomes = database.pendingIncomeDao().getUnsyncedIncomes().first()
        Log.d(TAG, "📤 Found ${pendingIncomes.size} pending incomes to sync")

        if (pendingIncomes.isEmpty()) {
            Log.d(TAG, "✨ No pending incomes to sync")
            return 0
        }

        var syncedCount = 0
        pendingIncomes.forEach { income ->
            try {
                Log.d(TAG, "💵 Syncing income ID ${income.id}: ${income.description} - $${income.amount}")

                // Create income in Firebase
                Log.d(TAG, "☁️ Creating income in Firebase...")
                incomeRepo.createIncome(
                    amount = income.amount,
                    currency = income.currency,
                    source = income.source,
                    description = income.description,
                    date = income.date
                )
                Log.d(TAG, "✅ Income created in Firebase")

                // Add to synced transactions
                database.syncedTransactionDao().insert(
                    com.example.monify_kotlin.data.cache.SyncedTransaction(
                        type = "income",
                        amount = income.amount,
                        description = income.description ?: income.source,
                        category = income.source,
                        date = income.date,
                        timestamp = income.timestamp,
                        isSynced = true
                    )
                )
                Log.d(TAG, "✅ Income added to synced_transactions table")

                // Delete from pending
                database.pendingIncomeDao().delete(income)
                Log.d(TAG, "✅ Income ${income.id} removed from pending_incomes")

                syncedCount++

            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to sync income ${income.id}: ${e.message}", e)
                e.printStackTrace()

                database.pendingIncomeDao().incrementSyncAttempts(
                    income.id,
                    System.currentTimeMillis()
                )
            }
        }

        return syncedCount
    }

    /**
     * Envía un broadcast para notificar que la sincronización terminó
     * Esto permite que el HomeViewModel recargue el balance
     */
    private fun sendSyncCompletedBroadcast(syncedCount: Int) {
        val intent = Intent(SYNC_COMPLETED_ACTION).apply {
            putExtra(EXTRA_SYNCED_COUNT, syncedCount)
        }
        context.sendBroadcast(intent)
        Log.d(TAG, "📢 Sync completed broadcast sent (count: $syncedCount)")
    }

    companion object {
        private const val TAG = "TransactionSyncWorker"
        const val WORK_NAME = "transaction_sync"
        const val SYNC_COMPLETED_ACTION = "com.example.monify_kotlin.SYNC_COMPLETED"
        const val EXTRA_SYNCED_COUNT = "synced_count"

        fun schedule(context: Context) {
            Log.d(TAG, "📅 Scheduling periodic sync work")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<TransactionSyncWorker>()
                .setConstraints(constraints)
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

            Log.d(TAG, "✅ Sync work scheduled successfully")
        }

        fun scheduleImmediate(context: Context) {
            Log.d(TAG, "⚡ Scheduling IMMEDIATE sync work")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<TransactionSyncWorker>()
                .setConstraints(constraints)
                .setInitialDelay(0, TimeUnit.SECONDS)
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

            Log.d(TAG, "✅ Immediate sync work scheduled")
        }

        /**
         * Cancela cualquier trabajo de sincronización pendiente
         */
        fun cancelSync(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
            Log.d(TAG, "🛑 Sync work cancelled")
        }

        /**
         * Verifica el estado del trabajo de sincronización
         */
        fun checkSyncStatus(context: Context) {
            val workInfo = WorkManager.getInstance(context)
                .getWorkInfosForUniqueWork(WORK_NAME)

            workInfo.get()?.forEach { info ->
                Log.d(TAG, "📊 Sync work status: ${info.state}")
            }
        }
    }
}