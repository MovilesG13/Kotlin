package com.example.monify_kotlin.data.cache

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PendingExpenseDao {
    @Insert
    suspend fun insert(expense: PendingExpense): Long

    @Query("SELECT * FROM pending_expenses WHERE isSynced = 0 ORDER BY timestamp DESC")
    fun getUnsyncedExpenses(): Flow<List<PendingExpense>>

    @Query("UPDATE pending_expenses SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE pending_expenses SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun incrementSyncAttempts(id: Long, timestamp: Long)

    @Delete
    suspend fun delete(expense: PendingExpense)
}

@Dao
interface PendingIncomeDao {
    @Insert
    suspend fun insert(income: PendingIncome): Long

    @Query("SELECT * FROM pending_incomes WHERE isSynced = 0 ORDER BY timestamp DESC")
    fun getUnsyncedIncomes(): Flow<List<PendingIncome>>

    @Query("UPDATE pending_incomes SET isSynced = 1 WHERE id = :id")
    suspend fun markAsSynced(id: Long)

    @Query("UPDATE pending_incomes SET syncAttempts = syncAttempts + 1, lastSyncAttempt = :timestamp WHERE id = :id")
    suspend fun incrementSyncAttempts(id: Long, timestamp: Long)

    @Delete
    suspend fun delete(income: PendingIncome)
}

@Dao
interface SyncedTransactionDao {
    @Insert
    suspend fun insert(transaction: SyncedTransaction)

    @Query("SELECT * FROM synced_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<SyncedTransaction>>

    @Query("SELECT * FROM synced_transactions WHERE timestamp >= :startOfWeek AND timestamp < :endOfWeek ORDER BY timestamp DESC")
    fun getTransactionsForWeek(startOfWeek: Long, endOfWeek: Long): Flow<List<SyncedTransaction>>
}