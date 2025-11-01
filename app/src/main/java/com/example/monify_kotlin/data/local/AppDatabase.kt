package com.example.monify_kotlin.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.monify_kotlin.data.cache.PendingExpense
import com.example.monify_kotlin.data.cache.PendingExpenseDao
import com.example.monify_kotlin.data.cache.PendingIncome
import com.example.monify_kotlin.data.cache.PendingIncomeDao
import com.example.monify_kotlin.data.cache.SyncedTransaction
import com.example.monify_kotlin.data.cache.SyncedTransactionDao

@Database(
    entities = [GoalEntity::class, SyncedTransaction::class, PendingExpense::class, PendingIncome::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun goalDao(): GoalDao
    abstract fun syncedTransactionDao(): SyncedTransactionDao
    abstract fun pendingExpenseDao(): PendingExpenseDao
    abstract fun pendingIncomeDao(): PendingIncomeDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monify_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
