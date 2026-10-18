package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    onPrimary = Color.Black,
    primaryContainer = Color(0xFF004D5A),
    onPrimaryContainer = NeonCyan,
    secondary = NeonPurple,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4A0072),
    onSecondaryContainer = Color(0xFFE1BEE7),
    tertiary = NeonPink,
    onTertiary = Color.White,
    background = GamingDarkBg,
    onBackground = KeyTextPrimary,
    surface = GamingCardBg,
    onSurface = KeyTextPrimary,
    surfaceVariant = GamingSurface,
    onSurfaceVariant = KeyTextSecondary
)

private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF0091EA),
    onPrimary = Color.White,
    secondary = Color(0xFF7B1FA2),
    onSecondary = Color.White,
    tertiary = Color(0xFFC2185B),
    background = Color(0xFFF4F6F9),
    surface = Color.White,
    onBackground = Color(0xFF1E293B),
    onSurface = Color(0xFF1E293B)
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to gamer dark theme for Rimu Keyboard
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
