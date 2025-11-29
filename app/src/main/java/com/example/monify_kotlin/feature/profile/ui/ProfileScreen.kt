package com.example.monify_kotlin.feature.profile.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.monify_kotlin.core.ui.BottomBar
import com.example.monify_kotlin.feature.profile.ProfileViewModel
import com.example.monify_kotlin.feature.profile.ProfileViewModelFactory
import com.example.monify_kotlin.ui.theme.*

@Composable
fun ProfileScreen(
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val viewModel: ProfileViewModel = viewModel(
        factory = ProfileViewModelFactory(context)
    )
    val state = viewModel.uiState
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = if(state.isDarkMode) Color.DarkGray else White, // Ejemplo simple de cambio de tema
        bottomBar = { BottomBar("profile") { route -> onNavigate(route) } }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Header de Perfil (Con Edición)
            ProfileHeader(
                name = state.name,
                email = "usuario@monify.com", // Puedes traer esto de Firebase Auth si quieres
                isOnline = state.isOnline,
                onNameChange = viewModel::onNameChanged
            )

            // 2. Resumen de Actividad (Visual, basado en tu imagen)
            ActivitySummaryCard(isDark = state.isDarkMode)

            // 3. Configuración
            Text(
                "Configuración",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if(state.isDarkMode) White else Black
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = if(state.isDarkMode) Color.Gray else White),
                elevation = CardDefaults.cardElevation(2.dp)
            ) {
                Column {
                    // Notificaciones (Visual)
                    SettingsSwitchItem("Notificaciones", "Gestionar alertas", Icons.Outlined.Notifications, true, {}, state.isDarkMode)
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    // Modo Oscuro (Funcional)
                    SettingsSwitchItem(
                        "Modo Oscuro",
                        "Cambiar apariencia de la app",
                        Icons.Outlined.DarkMode,
                        state.isDarkMode,
                        viewModel::onThemeChanged,
                        state.isDarkMode
                    )
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    // Moneda (Edit Information)
                    // Usamos un Switch visual o un Row especial. Para simplificar, un Row clickeable o un dropdown.
                    // Aquí lo haré visual simulando navegación como la imagen
                    SettingsNavItem("Idioma", "Español", Icons.Default.Language, state.isDarkMode)
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))

                    // Edición de Moneda (Funcionalidad requerida)
                    CurrencySelectorRow(
                        currentCurrency = state.currency,
                        isOnline = state.isOnline,
                        onCurrencyChange = viewModel::onCurrencyChanged,
                        isDark = state.isDarkMode
                    )

                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingsNavItem("Privacidad y Seguridad", "Configurar PIN", Icons.Default.Security, state.isDarkMode)
                    Divider(color = Color.LightGray.copy(alpha = 0.3f))
                    SettingsNavItem("Ayuda y Soporte", "FAQ y contacto", Icons.Default.HelpOutline, state.isDarkMode)
                }
            }

            // Versión
            Column(modifier = Modifier.padding(top = 16.dp)) {
                Text("Acerca de", fontWeight = FontWeight.Bold, color = if(state.isDarkMode) White else Black)
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Versión", color = Color.Gray)
                    Text("1.0.0", color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ProfileHeader(name: String, email: String, isOnline: Boolean, onNameChange: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar Placeholder
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE0E0E0)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = name.take(2).uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                // Campo de Texto para Editar Nombre
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("Nombre") },
                    enabled = isOnline, // Eventual connectivity: Solo edita si hay red
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent
                    )
                )
                if (!isOnline) {
                    Text("Offline - Edición deshabilitada", style = MaterialTheme.typography.labelSmall, color = Red)
                } else {
                    Text(email, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                }
            }
        }
    }
}

@Composable
fun ActivitySummaryCard(isDark: Boolean) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = if(isDark) Color.Gray else White),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(16.dp)) {
            Text("Resumen de Actividad", fontWeight = FontWeight.Bold, color = if(isDark) White else Black)
            Spacer(Modifier.height(16.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StatItem("47", "Transacciones", Blue)
                StatItem("12", "Categorías", Green)
                StatItem("3", "Meses activo", Blue)
            }
        }
    }
}

@Composable
fun StatItem(count: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, style = MaterialTheme.typography.titleLarge, color = color)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
    }
}

@Composable
fun SettingsSwitchItem(title: String, subtitle: String, icon: ImageVector, checked: Boolean, onCheckedChange: (Boolean) -> Unit, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = if(isDark) White else Black)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsNavItem(title: String, subtitle: String, icon: ImageVector, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, null, tint = Color.Gray)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, color = if(isDark) White else Black)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
        Icon(Icons.Default.ArrowForward, null, tint = Color.Gray, modifier = Modifier.size(16.dp))
    }
}

@Composable
fun CurrencySelectorRow(currentCurrency: String, isOnline: Boolean, onCurrencyChange: (String) -> Unit, isDark: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.CreditCard, null, tint = Color.Gray)
        Spacer(Modifier.width(16.dp))
        Column(Modifier.weight(1f)) {
            Text("Moneda Principal", fontWeight = FontWeight.SemiBold, color = if(isDark) White else Black)
            Text("Afecta reportes y home", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }

        // Selector simple (Toggle USD/COP)
        Button(
            onClick = {
                val next = if (currentCurrency == "USD") "COP" else "USD"
                onCurrencyChange(next)
            },
            enabled = isOnline,
            colors = ButtonDefaults.buttonColors(containerColor = if(isOnline) Blue else Color.Gray)
        ) {
            Text(currentCurrency)
        }
    }
}