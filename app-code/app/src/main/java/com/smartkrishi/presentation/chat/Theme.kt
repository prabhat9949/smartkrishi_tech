package com.smartkrishi.presentation.chat

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Light Theme Colors
object LightChatColors {
    val background = Color(0xFFF0F4F1)
    val surface = Color(0xFFFFFFFF)
    val primary = Color(0xFF2D6A4F)
    val primaryContainer = Color(0xFFB7E4C7)
    val secondary = Color(0xFF52B788)
    val onPrimary = Color(0xFFFFFFFF)
    val onBackground = Color(0xFF1B1C1E)
    val onSurface = Color(0xFF1B1C1E)

    // Chat specific
    val userBubble = Color(0xFF40916C)
    val botBubble = Color(0xFFE8F5E9)
    val userText = Color(0xFFFFFFFF)
    val botText = Color(0xFF1B5E20)
    val inputBackground = Color(0xFFFFFFFF)
    val inputBorder = Color(0xFFD0D0D0)
    val quickReplyBg = Color(0xFFFFFFFF)
    val quickReplyBorder = Color(0xFF52B788)
}

// Dark Theme Colors
object DarkChatColors {
    val background = Color(0xFF0A1F1A)
    val surface = Color(0xFF1B2E28)
    val primary = Color(0xFF52B788)
    val primaryContainer = Color(0xFF2D6A4F)
    val secondary = Color(0xFF74C69D)
    val onPrimary = Color(0xFF003822)
    val onBackground = Color(0xFFE1E3E1)
    val onSurface = Color(0xFFE1E3E1)

    // Chat specific
    val userBubble = Color(0xFF2D6A4F)
    val botBubble = Color(0xFF1E3A32)
    val userText = Color(0xFFFFFFFF)
    val botText = Color(0xFFB7E4C7)
    val inputBackground = Color(0xFF1B2E28)
    val inputBorder = Color(0xFF40916C)
    val quickReplyBg = Color(0xFF1B2E28)
    val quickReplyBorder = Color(0xFF52B788)
}

data class ChatColorScheme(
    val background: Color,
    val surface: Color,
    val primary: Color,
    val primaryContainer: Color,
    val userBubble: Color,
    val botBubble: Color,
    val userText: Color,
    val botText: Color,
    val inputBackground: Color,
    val inputBorder: Color,
    val quickReplyBg: Color,
    val quickReplyBorder: Color
)

// Typography
private val AppTypography = androidx.compose.material3.Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)

private val LightColorScheme = lightColorScheme(
    primary = LightChatColors.primary,
    onPrimary = LightChatColors.onPrimary,
    primaryContainer = LightChatColors.primaryContainer,
    secondary = LightChatColors.secondary,
    background = LightChatColors.background,
    surface = LightChatColors.surface,
    onBackground = LightChatColors.onBackground,
    onSurface = LightChatColors.onSurface
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkChatColors.primary,
    onPrimary = DarkChatColors.onPrimary,
    primaryContainer = DarkChatColors.primaryContainer,
    secondary = DarkChatColors.secondary,
    background = DarkChatColors.background,
    surface = DarkChatColors.surface,
    onBackground = DarkChatColors.onBackground,
    onSurface = DarkChatColors.onSurface
)

