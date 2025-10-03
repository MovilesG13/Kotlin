package com.example.monify_kotlin.feature.reports.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.core.navigation.Routes
import com.example.monify_kotlin.core.ui.BottomBar
import com.example.monify_kotlin.feature.reports.CategoryData
import com.example.monify_kotlin.feature.reports.ReportsViewModel
import com.example.monify_kotlin.ui.theme.*

@Composable
fun ReportsScreen(
    onNavigate: (String) -> Unit,
    viewModel: ReportsViewModel = viewModel()
) {
    val uiState = viewModel.uiState

    Scaffold(
        containerColor = White,
        bottomBar = {
            BottomBar("reports") { route -> onNavigate(route) }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            ReportsHeader()

            // Summary Card
            SummaryCard(
                totalIncome = uiState.totalIncome,
                totalExpenses = uiState.totalExpenses
            )

            // Chart Card
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Blue)
                }
            } else if (uiState.error != null) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = uiState.error!!,
                            color = Red,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else if (uiState.categories.isEmpty()) {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No expenses registered this month",
                            color = Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                ExpenseDistributionCard(categories = uiState.categories)
            }

            // Category List
            if (uiState.categories.isNotEmpty()) {
                CategoryListCard(categories = uiState.categories)
            }
        }
    }
}

@Composable
private fun ReportsHeader() {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = LightBlue),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Blue.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Assessment,
                    contentDescription = null,
                    tint = Blue
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = "Expense Reports",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Blue
                )
                Text(
                    text = "View your spending distribution",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
            }
        }
    }
}

@Composable
private fun SummaryCard(totalIncome: Double, totalExpenses: Double) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Income",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$${totalIncome.formatMoney()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Green
                )
            }
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = Gray
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Expenses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "$${totalExpenses.formatMoney()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Red
                )
            }
            Divider(
                modifier = Modifier
                    .width(1.dp)
                    .height(40.dp),
                color = Gray
            )
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    "Balance",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
                )
                Spacer(Modifier.height(4.dp))
                val balance = totalIncome - totalExpenses
                Text(
                    "$${balance.formatMoney()}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = if (balance >= 0) Blue else Red
                )
            }
        }
    }
}

@Composable
private fun ExpenseDistributionCard(categories: List<CategoryData>) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Expense Distribution",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Black
            )
            Spacer(Modifier.height(16.dp))
            PieChart(
                categories = categories,
                modifier = Modifier.size(240.dp)
            )
        }
    }
}

@Composable
private fun PieChart(
    categories: List<CategoryData>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val strokeWidth = 60f
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
                topLeft = Offset(
                    center.x - radius,
                    center.y - radius
                ),
                size = Size(radius * 2, radius * 2),
                style = Stroke(
                    width = strokeWidth,
                    cap = StrokeCap.Butt
                )
            )

            startAngle += sweepAngle
        }
    }
}

@Composable
private fun CategoryListCard(categories: List<CategoryData>) {
    Card(shape = RoundedCornerShape(16.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "Categories",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Black
            )
            Spacer(Modifier.height(12.dp))
            categories.forEach { category ->
                CategoryRow(category)
                if (category != categories.last()) {
                    Spacer(Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun CategoryRow(category: CategoryData) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(category.color)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                category.name,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = Black
            )
            Text(
                "$${category.amount.formatMoney()}",
                style = MaterialTheme.typography.bodyMedium,
                color = Black
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(category.color.copy(alpha = 0.2f))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                "${(category.percentage * 100).toInt()}%",
                color = category.color,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

private fun Double.formatMoney(): String = "%,.2f".format(this)