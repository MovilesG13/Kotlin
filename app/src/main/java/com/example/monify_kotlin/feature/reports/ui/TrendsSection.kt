package com.example.monify_kotlin.feature.reports.ui

import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.unit.dp
import com.example.monify_kotlin.feature.reports.TrendsUiState
import com.example.monify_kotlin.ui.theme.*
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

@Composable
fun TrendsSection(
    state: TrendsUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- Tarjeta del Gráfico ---
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = White),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier.fillMaxWidth().height(320.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text(
                    "Semestral Expenses History",
                    style = MaterialTheme.typography.titleMedium,
                    color = Black
                )
                Spacer(Modifier.height(16.dp))

                if (state.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = Blue)
                    }
                } else if (state.chartData.isEmpty() || state.totalSpentWeek == 0.0) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No expenses registered last 6 months", color = Gray)
                    }
                } else {
                    // INTEGRACIÓN MPANDROIDCHART
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            LineChart(context).apply {
                                layoutParams = LinearLayout.LayoutParams(
                                    ViewGroup.LayoutParams.MATCH_PARENT,
                                    ViewGroup.LayoutParams.MATCH_PARENT
                                )
                                description.isEnabled = false
                                legend.isEnabled = false
                                xAxis.position = XAxis.XAxisPosition.BOTTOM
                                xAxis.setDrawGridLines(false)
                                xAxis.textColor = android.graphics.Color.BLACK
                                axisLeft.textColor = android.graphics.Color.BLACK
                                axisRight.isEnabled = false

                                setTouchEnabled(false) // Desactivar interacción para este MVP
                                setDrawGridBackground(false)
                            }
                        },
                        update = { chart ->
                            if (chart.data?.dataSetCount ?: 0 > 0) {
                                chart.clear() // Limpiar datos viejos al recargar
                            }

                            val dataSet = LineDataSet(state.chartData, "Gastos").apply {
                                color = Blue.toArgb()
                                setCircleColor(Blue.toArgb())
                                lineWidth = 2.5f
                                circleRadius = 4f
                                setDrawValues(false) // No mostrar valores en cada punto
                                mode = LineDataSet.Mode.CUBIC_BEZIER // Línea curva suave
                                setDrawFilled(true)
                                fillColor = Blue.toArgb()
                                fillAlpha = 50
                            }

                            chart.xAxis.valueFormatter = IndexAxisValueFormatter(state.labels)
                            chart.data = LineData(dataSet)
                            chart.invalidate() // Refrescar
                            chart.animateX(800) // Animación simple
                        }
                    )
                }
            }
        }

        // --- Sección de Insights (Estilo visual corregido) ---
        Text(
            "Financial Insights",
            style = MaterialTheme.typography.titleMedium,
            color = Black,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        if (state.isLoading) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Blue, modifier = Modifier.size(24.dp))
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                InsightBanner(
                    title = "Weekly Savings",
                    body = state.savingsInsight,
                    accentColor = Green
                )


                InsightBanner(
                    title = "Dominant Category",
                    body = state.dominantCategoryInsight,
                    accentColor = Blue
                )


                InsightBanner(
                    title = "Recommendation",
                    body = state.recommendationInsight,
                    accentColor = Red
                )
            }
        }
        Spacer(Modifier.height(16.dp))
    }
}