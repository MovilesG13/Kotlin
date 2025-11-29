package com.example.monify_kotlin.core.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.monify_kotlin.core.navigation.Routes

data class BottomItem(val route: String, val label: String, val icon: ImageVector, val enabled: Boolean)

@Composable
fun BottomBar(currentRoute: String?, onNavigate: (String) -> Unit) {
    val items = listOf(
        BottomItem(Routes.HOME,    "Inicio",   Icons.Filled.Home,      true),
        BottomItem(Routes.SAVINGS, "Ahorros",  Icons.Filled.AccountBalance, true),
        BottomItem(Routes.REPORTS, "Reportes", Icons.Filled.Assessment, true),
        BottomItem("profile",      "Perfil",   Icons.Filled.Person,    true),
    )
    NavigationBar {
        items.forEach { it ->
            NavigationBarItem(
                selected = currentRoute == it.route,
                onClick = { if (it.enabled) onNavigate(it.route) },
                icon = { Icon(it.icon, contentDescription = it.label) },
                label = { Text(it.label) },
                enabled = it.enabled
            )
        }
    }
}
