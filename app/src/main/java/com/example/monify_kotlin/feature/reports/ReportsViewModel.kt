package com.example.monify_kotlin.feature.reports

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.monify_kotlin.data.ExpenseRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import com.example.monify_kotlin.ui.theme.*
import com.github.mikephil.charting.data.Entry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

class ReportsViewModel(
    private val repository: ExpenseRepository,
    private val database: AppDatabase
) : ViewModel() {

    var uiState by mutableStateOf(ReportsUiState())
        private set

    var trendsUiState by mutableStateOf(TrendsUiState())
        private set

    init {
        // Load everything on init
        refreshData()
    }

    fun refreshData() {
        loadMonthlyData()     // Tab 1: Categories (Current Month)
        loadSixMonthTrend()   // Tab 2: Trends (Six-Month History)
    }

    // --- TAB 1: CATEGORIES (Original logic) ---
    fun loadMonthlyData() {
        uiState = uiState.copy(isLoading = true, error = null)
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val selectedMonthStr = uiState.selectedMonth
                val totalData = fetchMonthTotal(selectedMonthStr) // Reuse helper logic

                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(
                        isLoading = false,
                        totalExpenses = totalData.totalExpenses,
                        totalIncome = totalData.totalIncome,
                        categories = totalData.categories
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    uiState = uiState.copy(isLoading = false, error = "Error: ${e.message}")
                }
            }
        }
    }

    // --- TAB 2: TRENDS (NEW LOGIC: Last 6 Months) ---
    fun loadSixMonthTrend() {
        trendsUiState = trendsUiState.copy(isLoading = true)

        viewModelScope.launch(Dispatchers.IO) { // IO because we will make many network requests
            try {
                val currentMonth = YearMonth.now()
                val last6Months = (5 downTo 0).map { i -> currentMonth.minusMonths(i.toLong()) }

                // MULTI-THREADING STRATEGY:
                // Launch 6 parallel requests (async) to avoid blocking or sequential waiting
                val deferredResults = last6Months.map { month ->
                    async {
                        // For each month, get total using hybrid logic
                        val monthStr = month.format(DateTimeFormatter.ofPattern("yyyy-MM"))
                        val data = fetchMonthTotal(monthStr)
                        // Return pair: (MonthName, TotalSpent)
                        // Using Locale.ENGLISH to ensure labels are "Jan", "Feb" etc.
                        val label = month.month.getDisplayName(TextStyle.SHORT, Locale.ENGLISH)
                        Pair(label, data.totalExpenses.toFloat())
                    }
                }

                // Wait for all to finish
                val results = deferredResults.awaitAll()

                // Prepare data for Chart
                val chartEntries = results.mapIndexed { index, pair ->
                    Entry(index.toFloat(), pair.second)
                }
                val xLabels = results.map { it.first }

                // Smart Insights (Business Logic)
                val currentMonthTotal = results.last().second
                val previousMonthTotal = results[results.size - 2].second
                val averageSpending = results.map { it.second }.average()

                // Insight 1: Previous month comparison
                val trendText = when {
                    currentMonthTotal < previousMonthTotal -> "Great! You spent less than last month."
                    currentMonthTotal > previousMonthTotal -> "Your expenses have increased compared to last month."
                    else -> "Your spending remains stable."
                }

                // Insight 2: Average
                val avgText = "Your six-month spending average is ${averageSpending.toDouble().formatMoney()}."

                // Insight 3: Recommendation
                val recText = if (currentMonthTotal > averageSpending * 1.2) {
                    "You are spending 20% more than your average. Check your categories."
                } else {
                    "You are maintaining good financial control."
                }

                withContext(Dispatchers.Main) {
                    trendsUiState = trendsUiState.copy(
                        isLoading = false,
                        chartData = chartEntries,
                        labels = xLabels,
                        totalSpentWeek = currentMonthTotal.toDouble(), // Reuse field for current total
                        savingsInsight = trendText,
                        dominantCategoryInsight = avgText,
                        recommendationInsight = recText
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) { trendsUiState = trendsUiState.copy(isLoading = false) }
            }
        }
    }

    // --- UNIFIED HELPER (Cloud + Local) ---
    // This function is the heart of your "Hybrid" strategy
    private suspend fun fetchMonthTotal(monthStr: String): MonthData {
        // 1. Cloud (Firebase)
        val cloudSummary = try {
            repository.getMonthlySummary(monthStr)
        } catch (e: Exception) {
            emptyMap()
        }

        // 2. Local (Room - Pending)
        val pendingExpenses = database.pendingExpenseDao().getUnsyncedExpenses().firstOrNull() ?: emptyList()
        val selectedMonthDate = YearMonth.parse(monthStr)

        val pendingInMonth = pendingExpenses.filter {
            val date = parseDateSafe(it.date)
            YearMonth.from(date) == selectedMonthDate
        }

        // 3. Merge
        val cloudTotal = (cloudSummary["totalExpenses"] as? Number)?.toDouble() ?: 0.0
        val pendingTotal = pendingInMonth.sumOf { it.amount }
        val finalTotal = cloudTotal + pendingTotal

        val cloudIncome = (cloudSummary["totalIncome"] as? Number)?.toDouble() ?: 0.0

        // Categories (Only needed for tab 1, but calculated here for consistency)
        @Suppress("UNCHECKED_CAST")
        val cloudCats = (cloudSummary["byCategory"] as? List<Map<String, Any>>) ?: emptyList()
        val mergedCats = mutableMapOf<String, Double>()

        cloudCats.forEach { mergedCats[it["categoryId"] as String] = (it["total"] as Number).toDouble() }
        pendingInMonth.forEach { mergedCats[it.categoryId] = (mergedCats[it.categoryId] ?: 0.0) + it.amount }

        val catList = mergedCats.map { (id, amount) ->
            CategoryData(
                name = mapCategoryName(id),
                amount = amount,
                color = mapCategoryColor(id),
                percentage = if (finalTotal > 0) (amount / finalTotal).toFloat() else 0f
            )
        }.sortedByDescending { it.amount }

        return MonthData(finalTotal, cloudIncome, catList)
    }

    // Internal class to pass data
    private data class MonthData(val totalExpenses: Double, val totalIncome: Double, val categories: List<CategoryData>)

    private fun parseDateSafe(dateStr: String): LocalDate {
        return try {
            val cleanDate = if (dateStr.length >= 10) dateStr.substring(0, 10) else dateStr
            LocalDate.parse(cleanDate)
        } catch (e: Exception) { LocalDate.now() }
    }

    private fun mapCategoryName(id: String): String = when (id.lowercase()) {
        "food" -> "Food"
        "transport" -> "Transport"
        "bills" -> "Bills"
        "shopping" -> "Shopping"
        "entertainment" -> "Entertainment"
        "health" -> "Health"
        else -> id.replaceFirstChar { it.uppercase() }
    }

    private fun mapCategoryColor(id: String): Color = when (id.lowercase()) {
        "food" -> Red
        "transport" -> Blue
        "bills" -> Green
        "shopping" -> LightBlue
        "health" -> Color(0xFFFF9800)
        else -> Gray
    }

    fun selectMonth(month: String) {
        uiState = uiState.copy(selectedMonth = month)
        loadMonthlyData()
    }
}

private fun Double.formatMoney(): String = "$%,.0f".format(this)