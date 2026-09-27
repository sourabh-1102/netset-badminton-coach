package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF2B8C8E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF1F4A4B),
    onPrimaryContainer = Color(0xFFD2ECEA),
    secondary = Color(0xFF9CCFCF),
    onSecondary = CourtGreenDeep,
    tertiary = Color(0xFFD9C9A8),
    background = SurfaceDark,
    surface = CardBgDark,
    surfaceVariant = Color(0xFF2A3434),
    onBackground = Color(0xFFE0E3E3),
    onSurface = Color(0xFFE0E3E3),
    onSurfaceVariant = Color(0xFFB9C6C5),
    outline = Color(0xFF4A5857)
)

private val LightColorScheme = lightColorScheme(
    primary = CourtGreenPrimaryLight,
    onPrimary = Color.White,
    primaryContainer = TealContainerLight,
    onPrimaryContainer = CourtGreenDeep,
    secondary = CourtGreenLight,
    onSecondary = Color.White,
    tertiary = Color(0xFFE8E1D6),
    background = SurfaceLight,
    surface = CardBgLight,
    onBackground = Color(0xFF191C1C),
    onSurface = Color(0xFF191C1C),
    onSurfaceVariant = Color(0xFF3F4948),
    outline = Color(0xFFC0C9C8)
)

@Composable
fun SmashAssessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalDarkTheme provides darkTheme) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
