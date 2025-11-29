package com.example.monify_kotlin.feature.reports.ui

import android.os.Bundle // <--- IMPORTANTE
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.core.ui.BottomBar
import com.example.monify_kotlin.core.util.ConnectivityObserver
import com.example.monify_kotlin.data.ExpenseRepository
import com.example.monify_kotlin.data.cache.AppDatabase
import com.example.monify_kotlin.feature.reports.CategoryData
import com.example.monify_kotlin.feature.reports.ReportsViewModel
import com.example.monify_kotlin.ui.theme.*
import com.google.firebase.analytics.FirebaseAnalytics
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke


@Composable
fun ReportsScreen(
    onNavigate: (String) -> Unit,
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // 1. Instancia de Analytics (Usamos la estándar de Firebase)
    val firebaseAnalytics = remember { FirebaseAnalytics.getInstance(context) }

    val viewModel: ReportsViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ReportsViewModel(
                    repository = ExpenseRepository(),
                    database = AppDatabase.getDatabase(context)
                ) as T
            }
        }
    )

    val uiState = viewModel.uiState
    val trendsState = viewModel.trendsUiState
    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = true)

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Categories", "Trends")

    // Efecto para recargar datos al volver online
    LaunchedEffect(isConnected) {
        if (isConnected) viewModel.refreshData()
    }

    // Efecto para registrar la vista inicial (Por defecto entra a Categories)
    LaunchedEffect(Unit) {
        logTabSelection(firebaseAnalytics, "Categories")
    }

    Scaffold(
        containerColor = White,
        bottomBar = { BottomBar("reports") { route -> onNavigate(route) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp)
        ) {
            ReportsHeader()

            if (!isConnected) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .background(Red.copy(alpha = 0.9f), RoundedCornerShape(4.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Offline Mode: Showing cached data", color = White, fontSize = 12.sp)
                }
            }

            Spacer(Modifier.height(16.dp))

            // TAB ROW CON ANALYTICS
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = White,
                contentColor = Blue,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = Blue
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            if (selectedTab != index) { // Solo si cambia de pestaña
                                selectedTab = index
                                // 2. LOGUEAR EL EVENTO AQUI
                                logTabSelection(firebaseAnalytics, title)
                            }
                        },
                        text = { Text(title, style = MaterialTheme.typography.labelLarge) }
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            when (selectedTab) {
                0 -> CategoriesSection(uiState = uiState)
                1 -> TrendsSection(state = trendsState)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

// --- HELPER FUNCTION PARA ANALYTICS ---
// Usamos el evento estándar SELECT_CONTENT que Firebase ama
private fun logTabSelection(analytics: FirebaseAnalytics, tabName: String) {
    val bundle = Bundle().apply {
        putString(FirebaseAnalytics.Param.ITEM_ID, "tab_${tabName.lowercase()}")
        putString(FirebaseAnalytics.Param.ITEM_NAME, tabName)
        putString(FirebaseAnalytics.Param.CONTENT_TYPE, "report_section")
    }
    analytics.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, bundle)
}

// ... (El resto de tus componentes ReportsHeader, etc. siguen igual abajo)
@Composable
private fun ReportsHeader() {
    // ... (Tu código existente del header) ...
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
                    text = "Financial Reports",
                    style = MaterialTheme.typography.titleMedium,
                    color = Blue
                )
                Text(
                    text = "Overview of your income and expenses",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Black
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

        LinearProgressIndicator(
            progress = { category.percentage },
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
