package com.example.monify_kotlin.feature.home

import java.time.LocalDateTime

data class TransactionUiModel(
    val id: Long,
    val type: TransactionType,
    val amount: Double,
    val description: String,
    val category: String,
    val timestamp: Long,
    val isSynced: Boolean,
    val dateTime: LocalDateTime
)

enum class TransactionType {
    INCOME, EXPENSE
}

data class WeeklyTransactions(
    val weekLabel: String,
    val startDate: LocalDateTime,
    val endDate: LocalDateTime,
    val transactions: List<TransactionUiModel>
)