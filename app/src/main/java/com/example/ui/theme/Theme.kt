package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AbhtrixDarkColorScheme = darkColorScheme(
    primary = BrandCrimson,
    onPrimary = Color.White,
    primaryContainer = BrandCrimsonDark,
    onPrimaryContainer = Color.White,
    secondary = BrandCyan,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF00363D),
    onSecondaryContainer = BrandCyan,
    tertiary = AccentAmber,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    outline = DarkSurfaceBorder
)

@Composable
fun AbhtrixTheme(
    darkTheme: Boolean = true, // Streaming platform always defaults to cinematic dark
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = AbhtrixDarkColorScheme,
        typography = Typography,
        content = content
    )
}

// Backwards compatibility alias for template references
@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    AbhtrixTheme(content = content)
}
