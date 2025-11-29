package com.example.monify_kotlin.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Tus colores definidos en Color.kt
// Asegúrate de que Color.kt tenga las variables:
// BrandBlue, BrandLightBlue, White, Black, DarkBackground, etc.
// (Usa la paleta que definimos en el paso anterior para que se vea bien)

private val LightColors = lightColorScheme(
    primary = Blue, // O BrandBlue
    onPrimary = White,
    secondary = LightBlue, // O BrandLightBlue
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    error = Red,
    onError = White
)

// Define una paleta oscura real para que no se vea feo
private val DarkColors = darkColorScheme(
    primary = LightBlue, // En oscuro el primario suele ser más claro
    onPrimary = Black,
    secondary = Blue,
    background = Color(0xFF121212), // Gris muy oscuro
    onBackground = Color(0xFFE0E0E0), // Texto claro
    surface = Color(0xFF1E1E1E),    // Tarjetas oscuras
    onSurface = Color(0xFFE0E0E0),
    error = Red,
    onError = Black
)

@Composable
fun FinanceTheme(
    // AQUI ESTA EL CAMBIO: Recibe el parámetro manual
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
