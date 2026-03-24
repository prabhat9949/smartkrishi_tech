package com.smartkrishi.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Import your color file
import com.smartkrishi.presentation.theme.TealPrimary
import com.smartkrishi.presentation.theme.TealLight
import com.smartkrishi.presentation.theme.TextPrimary
import com.smartkrishi.presentation.theme.TextOnPrimary
import com.smartkrishi.presentation.theme.BackgroundLight
import com.smartkrishi.presentation.theme.BackgroundDark
import com.smartkrishi.presentation.theme.SurfaceLight
import com.smartkrishi.presentation.theme.SurfaceDark
import com.smartkrishi.presentation.theme.Info
import com.smartkrishi.presentation.theme.ErrorColor

// -----------------------------
// Light Color Palette
// -----------------------------
private val LightColorScheme = lightColorScheme(
    primary = TealPrimary,
    onPrimary = TextOnPrimary,

    secondary = TealLight,
    tertiary = Info,

    background = BackgroundLight,
    surface = SurfaceLight,
    onSurface = TextPrimary,

    error = ErrorColor,
)

// -----------------------------
// Dark Color Palette
// -----------------------------
private val DarkColorScheme = darkColorScheme(
    primary = TealLight,
    onPrimary = TextPrimary,

    secondary = TealPrimary,
    tertiary = Info,

    background = BackgroundDark,
    surface = SurfaceDark,
    onSurface = TextOnPrimary,

    error = ErrorColor,
)

// -------------------------------------------------------
// Main App Theme Wrapper
// -------------------------------------------------------
@Composable
fun SmartKrishiTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
