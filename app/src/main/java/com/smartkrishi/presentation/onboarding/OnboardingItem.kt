package com.smartkrishi.presentation.onboarding

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color

data class OnboardingItem(
    @DrawableRes val imageRes: Int,
    val title: String,
    val description: String,
    val gradient: OnboardingGradient = OnboardingGradient.GREEN_FOREST,
    val tag: String = ""
)

enum class OnboardingGradient(
    val primary: Color,
    val secondary: Color,
    val accent: Color,
    val background: List<Color>
) {
    GREEN_FOREST(
        primary = Color(0xFF2E7D32),
        secondary = Color(0xFF66BB6A),
        accent = Color(0xFF4CAF50),
        background = listOf(
            Color(0xFF1B5E20),
            Color(0xFF2E7D32),
            Color(0xFF388E3C)
        )
    ),
    EMERALD_GLOW(
        primary = Color(0xFF00695C),
        secondary = Color(0xFF26A69A),
        accent = Color(0xFF4DB6AC),
        background = listOf(
            Color(0xFF004D40),
            Color(0xFF00695C),
            Color(0xFF00897B)
        )
    ),
    NATURE_BLEND(
        primary = Color(0xFF558B2F),
        secondary = Color(0xFF9CCC65),
        accent = Color(0xFF7CB342),
        background = listOf(
            Color(0xFF33691E),
            Color(0xFF558B2F),
            Color(0xFF689F38)
        )
    )
}
