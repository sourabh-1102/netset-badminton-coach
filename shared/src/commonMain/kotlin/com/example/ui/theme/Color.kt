package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** True while the app is rendered in dark mode; drives the adaptive tokens below. */
val LocalDarkTheme = staticCompositionLocalOf { false }

private val isDark: Boolean
    @Composable @ReadOnlyComposable get() = LocalDarkTheme.current

// Fixed brand colors (same in light and dark)
val CourtGreenPrimaryLight = Color(0xFF00696B)
val CourtGreenLight = Color(0xFF2B8587)
val CourtGreenDeep = Color(0xFF002020)
val TealContainerLight = Color(0xFFCCE8E8)

val SurfaceLight = Color(0xFFF8F9FA)
val SurfaceDark = Color(0xFF121717)
val CardBgLight = Color(0xFFFFFFFF)
val CardBgDark = Color(0xFF1E2626)

val ScoreRed = Color(0xFFBA1A1A)
val ScoreOrange = Color(0xFFE07A00)
val ScoreGreen = Color(0xFF00696B)
val ScoreDarkGreen = Color(0xFF002020)

// Adaptive tokens: screens use these for text, cards and borders so both themes stay readable
val CourtGreenDark: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFFD2ECEA) else CourtGreenDeep

val TextSlate: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFFB9C6C5) else Color(0xFF3F4948)

val TealContainer: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF1F4A4B) else TealContainerLight

val AvatarTeal: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF2A4645) else Color(0xFFD6E6E5)

val AvatarGold: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF4A4232) else Color(0xFFE8E1D6)

val AvatarBlue: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF2E3A4D) else Color(0xFFD6DCE6)

val CardBorderLight: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF2F3B3B) else Color(0xFFEFF1F1)

val SlateOutline: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF4A5857) else Color(0xFFC0C9C8)

val CardSurface: Color
    @Composable @ReadOnlyComposable get() = if (isDark) CardBgDark else CardBgLight

val PillTrack: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF2A3434) else Color(0xFFF1F3F3)

val ScoreRedContainer: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF5C1A17) else Color(0xFFFFDAD6)

val ScoreRedOnContainer: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFFFFDAD6) else Color(0xFF410002)

val CourtGreenPrimary: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF2B8C8E) else CourtGreenPrimaryLight

/** Score text colors tuned for contrast on both light and dark surfaces. */
val ScoreGoodText: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF5CC9A0) else ScoreGreen

val ScoreBadText: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFFFF8A80) else ScoreRed

val ScoreMidText: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFFFFB74D) else ScoreOrange

val BatchBarColor: Color
    @Composable @ReadOnlyComposable get() = if (isDark) Color(0xFF55696A) else Color(0xFFA9CDD4)
