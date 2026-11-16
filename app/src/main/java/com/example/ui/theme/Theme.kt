package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = GoldBright,
    onPrimary = CharcoalBlack,
    primaryContainer = MaroonPrimary,
    onPrimaryContainer = Color.White,
    secondary = GoldSecondary,
    onSecondary = CharcoalBlack,
    secondaryContainer = CharcoalElevated,
    onSecondaryContainer = GoldBright,
    tertiary = MaroonPrimary,
    onTertiary = Color.White,
    background = CharcoalBlack,
    onBackground = Color(0xFFF6EFE7),
    surface = CharcoalSurface,
    onSurface = Color(0xFFF6EFE7),
    surfaceVariant = CharcoalCard,
    onSurfaceVariant = Color(0xFFD6C7BA),
    outline = Color(0xFF53453E),
    error = Color(0xFFFF6B6B),
    onError = CharcoalBlack
)

private val LightColorScheme = lightColorScheme(
    primary = MaroonPrimary,
    onPrimary = Color.White,
    primaryContainer = MaroonContainer,
    onPrimaryContainer = OnMaroonContainer,
    secondary = GoldSecondary,
    onSecondary = CharcoalBlack,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = CharcoalBlack,
    onTertiary = Color.White,
    background = CreamBackground,
    onBackground = CharcoalBlack,
    surface = CreamSurface,
    onSurface = CharcoalBlack,
    surfaceVariant = WarmSurfaceVariant,
    onSurfaceVariant = Color(0xFF5A4B42),
    outline = OutlineWarm,
    error = Color(0xFFB3261E),
    onError = Color.White
)

@Composable
fun LahoriCafeTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
