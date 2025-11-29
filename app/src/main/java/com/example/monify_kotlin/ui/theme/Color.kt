package com.example.monify_kotlin.ui.theme

import androidx.compose.ui.graphics.Color

// --- PALETA BRANDING (Tu imagen) ---
val BrandBlue     = Color(0xFF0048C4) // #0048c4 (Azul Fuerte - Botones/Headers)
val BrandLightBlue= Color(0xFFA7C7E7) // #a7c7e7 (Azul Suave - Acentos)
val BrandSkyBlue  = Color(0xFFBDE3F6) // #bde3f6 (Cielo - Fondos claros)
val BrandGray     = Color(0xFFE8E8E8) // #e8e8e8 (Gris Claro - Cards/Dividers)

// --- SEMÁFORO (Estados) ---
val ExpenseRed    = Color(0xFFFA2E38) // #fa2e38 (Gastos/Error)
val IncomeGreen   = Color(0xFF06C951) // #06c951 (Ingresos/Éxito)
val WarningOrange = Color(0xFFFFCBA4) // #FFCBA4 (Alertas)

// --- COLORES NEUTROS (Light Mode) ---
val White         = Color(0xFFFFFFFF)
val Black         = Color(0xFF000000)
val TextGray      = Color(0xFF757575) // Para subtítulos

// --- COLORES NEUTROS (Dark Mode - Nuevos) ---
val DarkBackground = Color(0xFF121212) // Fondo general oscuro (Estándar Material)
val DarkSurface    = Color(0xFF1E1E1E) // Fondo de tarjetas oscuro
val DarkTextPrimary= Color(0xFFE0E0E0) // Blanco humo (no blanco puro)
val DarkTextSec    = Color(0xFFB0B0B0) // Gris claro para subtítulos
val DarkDivider    = Color(0xFF2C2C2C)

// Aliases para compatibilidad con tu código actual
val Blue = BrandBlue
val LightBlue = BrandLightBlue
val Red = ExpenseRed
val Green = IncomeGreen
val Purple = BrandBlue // Reemplazamos el morado por tu azul de marca para consistencia
val Orange = WarningOrange