// File: presentation/chat/ChatTheme.kt
package com.smartkrishi.presentation.chat

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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

object LightChatColors {
    val background = Color(0xFFF8F9FA)  // Slightly warmer gray
    val surface = Color(0xFFFFFFFF)
    val primary = Color(0xFF4CAF50)
    val primaryContainer = Color(0xFFE8F5E9)
    val userBubble = Color(0xFF4CAF50)
    val botBubble = Color(0xFFFFFFFF)
    val userText = Color(0xFFFFFFFF)
    val botText = Color(0xFF212121)
    val inputBackground = Color(0xFFFAFAFA)  // Subtle gray for input
    val inputBorder = Color(0xFFE0E0E0)
    val quickReplyBg = Color(0xFFFFFFFF)
    val quickReplyBorder = Color(0xFF4CAF50)
}

object DarkChatColors {
    val background = Color(0xFF0F0F0F)  // True dark background
    val surface = Color(0xFF1C1C1C)
    val primary = Color(0xFF66BB6A)
    val primaryContainer = Color(0xFF2E7D32)
    val userBubble = Color(0xFF66BB6A)
    val botBubble = Color(0xFF262626)  // Better contrast
    val userText = Color(0xFF000000)
    val botText = Color(0xFFE8E8E8)  // Better readability
    val inputBackground = Color(0xFF262626)
    val inputBorder = Color(0xFF3F3F3F)  // More visible border
    val quickReplyBg = Color(0xFF262626)
    val quickReplyBorder = Color(0xFF66BB6A)
}

@Composable
fun ChatTheme(
    darkTheme: Boolean = false,  // Light mode by default
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFF66BB6A),
            onPrimary = Color(0xFF000000),
            primaryContainer = Color(0xFF2E7D32),
            onPrimaryContainer = Color(0xFFE8F5E9),
            background = Color(0xFF0F0F0F),
            onBackground = Color(0xFFE8E8E8),
            surface = Color(0xFF1C1C1C),
            onSurface = Color(0xFFE8E8E8),
            surfaceVariant = Color(0xFF262626),
            onSurfaceVariant = Color(0xFFB0B0B0),
            outline = Color(0xFF3F3F3F),
            error = Color(0xFFCF6679)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF4CAF50),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFE8F5E9),
            onPrimaryContainer = Color(0xFF1B5E20),
            background = Color(0xFFF8F9FA),
            onBackground = Color(0xFF212121),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF212121),
            surfaceVariant = Color(0xFFFAFAFA),
            onSurfaceVariant = Color(0xFF616161),
            outline = Color(0xFFE0E0E0),
            error = Color(0xFFB00020)
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
