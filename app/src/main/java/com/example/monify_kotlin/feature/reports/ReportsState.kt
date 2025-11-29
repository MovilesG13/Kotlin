package com.example.monify_kotlin.feature.reports

import androidx.compose.ui.graphics.Color
import com.github.mikephil.charting.data.Entry
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Datos de una categoría individual (para el gráfico de pastel)
data class CategoryData(
    val name: String,
    val amount: Double,
    val color: Color,
    val percentage: Float
)

// Estado para la pestaña "Categorías" (Resumen Mensual)
data class ReportsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val totalExpenses: Double = 0.0,
    val totalIncome: Double = 0.0,
    val categories: List<CategoryData> = emptyList(),
    val selectedMonth: String = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM"))
)

// Estado para la pestaña "Tendencias" (Gráfico Semanal e Insights)
data class TrendsUiState(
    val isLoading: Boolean = false,
    val chartData: List<Entry> = emptyList(), // Puntos para MPAndroidChart
    val labels: List<String> = emptyList(),   // Fechas eje X
    val totalSpentWeek: Double = 0.0,
    val savingsInsight: String = "",
    val dominantCategoryInsight: String = "",
    val recommendationInsight: String = ""
)