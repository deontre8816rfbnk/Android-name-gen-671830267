package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = BtnPrimaryBg,
    onPrimary = Color.White,
    secondary = BtnSecondaryBg,
    onSecondary = BtnSecondaryText,
    background = BgColor,
    onBackground = TextDark,
    surface = CardBg,
    onSurface = TextDark,
    outline = CardBorder
)

@Composable
fun MyApplicationTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}

