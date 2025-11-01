package com.example.monify_kotlin.data.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.monify_kotlin.data.local.GoalDao
import com.example.monify_kotlin.data.local.GoalEntity

@Database(
    entities = [GoalEntity::class, PendingExpense::class, PendingIncome::class, SyncedTransaction::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun goalDao(): GoalDao
    abstract fun pendingExpenseDao(): PendingExpenseDao
    abstract fun pendingIncomeDao(): PendingIncomeDao
    abstract fun syncedTransactionDao(): SyncedTransactionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "monify_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
