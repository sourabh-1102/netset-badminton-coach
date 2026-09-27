package com.example.ui.theme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Current theme plus a setter, provided by App() so any screen can show the toggle. */
class ThemeToggle(val isDark: Boolean, val setDark: (Boolean) -> Unit)

val LocalThemeToggle = staticCompositionLocalOf { ThemeToggle(false) {} }

/** Round sun/moon button that switches between light and dark mode. */
@Composable
fun ThemeToggleButton(modifier: Modifier = Modifier) {
    val toggle = LocalThemeToggle.current
    Surface(
        onClick = { toggle.setDark(!toggle.isDark) },
        shape = CircleShape,
        color = TealContainer,
        modifier = modifier.size(44.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = if (toggle.isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = if (toggle.isDark) "Switch to light mode" else "Switch to dark mode",
                tint = CourtGreenDark,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
