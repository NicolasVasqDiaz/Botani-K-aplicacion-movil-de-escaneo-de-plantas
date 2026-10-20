package com.botanik.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// TODO: agregar Nunito / Inter en app/src/main/res/font/ y cambiar a FontFamily(Font(R.font...))
// Por ahora se usa Default para permitir compilación sin recursos de fuentes.
val NunitoFamily = FontFamily.Default

val InterFamily = FontFamily.Default

val BotaniKTypography = Typography(
    displayLarge = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.ExtraBold, fontSize = 34.sp, lineHeight = 42.sp, letterSpacing = (-0.02).sp),
    headlineLarge = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold, fontSize = 26.sp, lineHeight = 34.sp),
    headlineMedium = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, lineHeight = 28.sp),
    headlineSmall = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    titleMedium = TextStyle(fontFamily = NunitoFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, lineHeight = 22.sp),
    bodyLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    bodySmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp, lineHeight = 16.sp),
    labelLarge = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.02.sp),
    labelMedium = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.SemiBold, fontSize = 11.sp, lineHeight = 16.sp, letterSpacing = 0.04.sp),
    labelSmall = TextStyle(fontFamily = InterFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, lineHeight = 12.sp, letterSpacing = 0.06.sp)
)
