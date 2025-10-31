package com.example.monify_kotlin.data.cache

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pending_expenses")
data class PendingExpense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val currency: String,
    val categoryId: String,
    val description: String?,
    val note: String?,
    val date: String,
    val receiptImageLocalPath: String?, // Local file path
    val receiptImageUrl: String?, // Firebase URL (if already uploaded)
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null
)

@Entity(tableName = "pending_incomes")
data class PendingIncome(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val currency: String,
    val source: String,
    val description: String?,
    val date: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false,
    val syncAttempts: Int = 0,
    val lastSyncAttempt: Long? = null
)

@Entity(tableName = "synced_transactions")
data class SyncedTransaction(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val type: String, // "expense" or "income"
    val amount: Double,
    val description: String,
    val category: String,
    val date: String,
    val timestamp: Long,
    val isSynced: Boolean = true
)