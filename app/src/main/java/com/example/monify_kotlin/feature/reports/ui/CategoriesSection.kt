package com.example.monify_kotlin.feature.reports.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.monify_kotlin.feature.reports.CategoryData
import com.example.monify_kotlin.feature.reports.ReportsUiState
import com.example.monify_kotlin.ui.theme.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke



@Composable
fun CategoriesSection(
    uiState: ReportsUiState
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary Card
        SummaryCard(
            totalIncome = uiState.totalIncome,
            totalExpenses = uiState.totalExpenses
        )

        // Chart Card & Loading Logic
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth().height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue)
                }
            }
            uiState.error != null -> {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Box(modifier = Modifier.padding(32.dp), contentAlignment = Alignment.Center) {
                        Text(text = uiState.error!!, color = Red)
                    }
                }
            }
            uiState.categories.isEmpty() -> {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = White)
                ) {
                    Box(
                        modifier = Modifier.padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("📊", fontSize = 48.sp)
                            Spacer(Modifier.height(8.dp))
                            Text(text = "No hay gastos registrados este mes", color = Gray)
                        }
                    }
                }
            }
            else -> {
                ExpenseDistributionCard(categories = uiState.categories)
            }
        }

        // Category List
        if (uiState.categories.isNotEmpty()) {
            CategoryListCard(categories = uiState.categories)
        }
    }
}

// ---> PEGA AQUÍ AL FINAL LAS FUNCIONES PRIVADAS QUE TENÍAS EN ReportsScreen.kt <---
// SummaryCard, ExpenseDistributionCard, LegendItem, PieChart, CategoryListCard, CategoryRow, formatMoney
// Si no las pegas aquí, CategoriesSection no compilará.
@Composable
private fun SummaryCard(totalIncome: Double, totalExpenses: Double) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text("Income", style = MaterialTheme.typography.bodySmall, color = LightBlue)
                Spacer(Modifier.height(4.dp))
                Text(
                    totalIncome.formatMoney(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Green
                )
            }

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp),
                color = Gray.copy(alpha = 0.3f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text("Expenses", style = MaterialTheme.typography.bodySmall, color = LightBlue)
                Spacer(Modifier.height(4.dp))
                Text(
                    totalExpenses.formatMoney(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Red
                )
            }

            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(50.dp),
                color = Gray.copy(alpha = 0.3f)
            )

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text("Balance", style = MaterialTheme.typography.bodySmall, color = LightBlue)
                Spacer(Modifier.height(4.dp))
                val balance = totalIncome - totalExpenses
                Text(
                    balance.formatMoney(),
                    style = MaterialTheme.typography.titleLarge,
                    color = if (balance >= 0) Blue else Red
                )
            }
        }
    }
}

@Composable
private fun ExpenseDistributionCard(categories: List<CategoryData>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Expense Distribution",
                style = MaterialTheme.typography.titleLarge,
                color = Black
            )
            Spacer(Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                PieChart(
                    categories = categories,
                    modifier = Modifier.size(260.dp)
                )
            }

            Spacer(Modifier.height(24.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                categories.take(3).forEach { category ->
                    LegendItem(category)
                }
            }
        }
    }
}

@Composable
private fun LegendItem(category: CategoryData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(category.color)
            )
            Spacer(Modifier.width(12.dp))
            Text(
                category.name,
                style = MaterialTheme.typography.bodyLarge,
                color = Black
            )
        }

        Text(
            "${(category.percentage * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium,
            color = category.color
        )
    }
}

@Composable
private fun PieChart(
    categories: List<CategoryData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 55f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2f, size.height / 2f)

        var startAngle = -90f

        categories.forEach { category ->
            val sweepAngle = 360f * category.percentage

            drawArc(
                color = category.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun CategoryListCard(categories: List<CategoryData>) {
    Card(
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(
                "Categories",
                style = MaterialTheme.typography.titleLarge,
                color = Black
            )
            Spacer(Modifier.height(16.dp))
            categories.forEach { category ->
                CategoryRow(category)
                if (category != categories.last()) {
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(category: CategoryData) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(category.color)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    category.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Black
                )
                Text(
                    category.amount.formatMoney(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = LightBlue
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(category.color.copy(alpha = 0.15f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    "${(category.percentage * 100).toInt()}%",
                    color = category.color,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }

        // Barra de progreso (Material3: determinista con lambda)
        LinearProgressIndicator(
            progress = { category.percentage }, // 0f..1f
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = category.color,
            trackColor = category.color.copy(alpha = 0.2f)
        )
    }
}

private fun Double.formatMoney(): String = "%,.0f".format(this)