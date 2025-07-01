package com.win11launcher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0078D4),
    onPrimary = Color.White,
    primaryContainer = Color(0xFF106EBE),
    onPrimaryContainer = Color.White,
    secondary = Color(0xFF005A9E),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF3C3C3C),
    onSecondaryContainer = Color.White,
    tertiary = Color(0xFF0078D4),
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF3C3C3C),
    onTertiaryContainer = Color.White,
    background = Color(0xFF1C1C1C),
    onBackground = Color.White,
    surface = Color(0xFF2D2D30),
    onSurface = Color.White,
    surfaceVariant = Color(0xFF3C3C3C),
    onSurfaceVariant = Color.White,
    outline = Color(0xFF404040),
    outlineVariant = Color(0xFF404040)
)

@Composable
fun Win11LauncherTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = Typography,
        content = content
    )
}