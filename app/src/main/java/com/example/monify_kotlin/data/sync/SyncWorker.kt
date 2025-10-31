package com.example.monify_kotlin.data.sync

import android.content.Context
import android.util.Log
import androidx.work.*
import com.example.monify_kotlin.data.ExpenseRepository
import com.example.monify_kotlin.data.IncomeRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import kotlinx.coroutines.flow.first
import java.util.concurrent.TimeUnit
import androidx.core.net.toUri

class TransactionSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    private val database = AppDatabase.getDatabase(context)
    private val expenseRepo = ExpenseRepository()
    private val incomeRepo = IncomeRepository()

    override suspend fun doWork(): Result {
        Log.d(TAG, "Starting sync work - Attempt ${runAttemptCount + 1}")

        return try {
            val expensesSynced = syncPendingExpenses()
            val incomesSynced = syncPendingIncomes()

            Log.d(TAG, "Sync completed - Expenses: $expensesSynced, Incomes: $incomesSynced")
            Result.success()
        } catch (e: Exception) {
            Log.e(TAG, "Sync failed: ${e.message}", e)
            if (runAttemptCount < 3) {
                Log.d(TAG, "Retrying sync...")
                Result.retry()
            } else {
                Log.e(TAG, "Max retry attempts reached")
                Result.failure()
            }
        }
    }

    private suspend fun syncPendingExpenses(): Int {
        val pendingExpenses = database.pendingExpenseDao().getUnsyncedExpenses().first()
        Log.d(TAG, "Found ${pendingExpenses.size} pending expenses to sync")

        var syncedCount = 0
        pendingExpenses.forEach { expense ->
            try {
                Log.d(TAG, "Syncing expense ID ${expense.id}: ${expense.description}")

                // Upload image if exists and not uploaded yet
                var imageUrl = expense.receiptImageUrl
                if (expense.receiptImageLocalPath != null && imageUrl == null) {
                    Log.d(TAG, "Uploading receipt image for expense ${expense.id}")
                    imageUrl = expenseRepo.uploadReceiptImage(
                        expense.receiptImageLocalPath.toUri()
                    )
                    Log.d(TAG, "Image uploaded successfully: $imageUrl")
                }

                // Create expense in Firebase
                expenseRepo.createExpense(
                    amount = expense.amount,
                    currency = expense.currency,
                    categoryId = expense.categoryId,
                    description = expense.description,
                    note = expense.note,
                    date = expense.date,
                    receiptImageUrl = imageUrl
                )

                // Mark as synced
                database.pendingExpenseDao().markAsSynced(expense.id)
                Log.d(TAG, "Expense ${expense.id} synced successfully")

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

                syncedCount++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync expense ${expense.id}: ${e.message}", e)
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
        Log.d(TAG, "Found ${pendingIncomes.size} pending incomes to sync")

        var syncedCount = 0
        pendingIncomes.forEach { income ->
            try {
                Log.d(TAG, "Syncing income ID ${income.id}: ${income.description}")

                incomeRepo.createIncome(
                    amount = income.amount,
                    currency = income.currency,
                    source = income.source,
                    description = income.description,
                    date = income.date
                )

                database.pendingIncomeDao().markAsSynced(income.id)
                Log.d(TAG, "Income ${income.id} synced successfully")

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

                syncedCount++
            } catch (e: Exception) {
                Log.e(TAG, "Failed to sync income ${income.id}: ${e.message}", e)
                database.pendingIncomeDao().incrementSyncAttempts(
                    income.id,
                    System.currentTimeMillis()
                )
            }
        }

        return syncedCount
    }

    companion object {
        private const val TAG = "TransactionSyncWorker"
        const val WORK_NAME = "transaction_sync"

        fun schedule(context: Context) {
            Log.d(TAG, "Scheduling sync work")

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

            Log.d(TAG, "Sync work scheduled successfully")
        }

        fun scheduleImmediate(context: Context) {
            Log.d(TAG, "Scheduling immediate sync work")

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncRequest = OneTimeWorkRequestBuilder<TransactionSyncWorker>()
                .setConstraints(constraints)
                .setInitialDelay(0, TimeUnit.SECONDS) // Immediate execution
                .build()

            WorkManager.getInstance(context)
                .enqueueUniqueWork(
                    WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncRequest
                )

            Log.d(TAG, "Immediate sync work scheduled")
        }
    }
}