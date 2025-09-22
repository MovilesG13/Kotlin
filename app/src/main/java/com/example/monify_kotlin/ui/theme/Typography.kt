package com.example.monify_kotlin.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.monify_kotlin.R



val Nunito = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_medium,  FontWeight.Medium),
    Font(R.font.nunito_bold,    FontWeight.Bold),
)

val AppTypography = Typography(
    titleLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,   fontSize = 24.sp),
    titleMedium= TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,   fontSize = 20.sp),
    bodyLarge  = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    labelLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold,   fontSize = 14.sp)
)
