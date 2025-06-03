package com.example.skypeek.presentation.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// iOS-style color palette
private val LightColorScheme = lightColorScheme(
    primary = Color(0xFF007AFF), // iOS Blue
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE3F2FD),
    onPrimaryContainer = Color(0xFF001D36),
    secondary = Color(0xFF34C759), // iOS Green
    onSecondary = Color.White,
    tertiary = Color(0xFFFF9500), // iOS Orange
    onTertiary = Color.White,
    background = Color(0xFFF2F2F7), // iOS Light Gray
    onBackground = Color(0xFF1D1D1F),
    surface = Color.White,
    onSurface = Color(0xFF1D1D1F),
    surfaceVariant = Color(0xFFF2F2F7),
    onSurfaceVariant = Color(0xFF48484A),
    outline = Color(0xFFC7C7CC),
    error = Color(0xFFFF3B30), // iOS Red
    onError = Color.White
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF0A84FF), // iOS Blue Dark
    onPrimary = Color.White,
    primaryContainer = Color(0xFF004C8C),
    onPrimaryContainer = Color(0xFFB8E6FF),
    secondary = Color(0xFF30D158), // iOS Green Dark
    onSecondary = Color.Black,
    tertiary = Color(0xFFFF9F0A), // iOS Orange Dark
    onTertiary = Color.Black,
    background = Color(0xFF000000), // iOS Dark
    onBackground = Color(0xFFFFFFFF),
    surface = Color(0xFF1C1C1E), // iOS Dark Gray
    onSurface = Color(0xFFFFFFFF),
    surfaceVariant = Color(0xFF2C2C2E),
    onSurfaceVariant = Color(0xFFAEAEB2),
    outline = Color(0xFF48484A),
    error = Color(0xFFFF453A), // iOS Red Dark
    onError = Color.White
)

@Composable
fun SkyPeekTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

// Additional colors for weather states
object WeatherColors {
    val SunnyGradient = listOf(
        Color(0xFF4A8BC2), // Softer, more muted blue
        Color(0xFF6BA3D0), // Gentler blue
        Color(0xFF87BFDD)  // Very soft blue
    )
    
    val CloudyGradient = listOf(
        Color(0xFF5A6B7A), // More muted gray-blue
        Color(0xFF6B7C8B), // Slightly lighter muted gray-blue
        Color(0xFF8A9BA5)  // Softer light gray-blue
    )
    
    val RainyGradient = listOf(
        Color(0xFF3A4751), // Darker muted blue-gray
        Color(0xFF4A5762), // Medium dark muted gray-blue
        Color(0xFF5A6B76)  // Softer medium gray-blue
    )
    
    val SnowGradient = listOf(
        Color(0xFF8AA5B9), // Very soft blue-gray
        Color(0xFFA1BBCF), // Gentle light blue-gray
        Color(0xFFB8D1E5)  // Subtle very light blue
    )
    
    val StormyGradient = listOf(
        Color(0xFF1A1A1F), // Very dark but not harsh
        Color(0xFF2A2A30), // Medium dark gray with warmth
        Color(0xFF3A3A40)  // Lighter dark gray
    )
    
    val FoggyGradient = listOf(
        Color(0xFF4A5660), // Softer muted gray
        Color(0xFF5A6670), // Gentler light muted gray
        Color(0xFF7A8690)  // Subtle lighter gray
    )
    
    // Text colors for weather overlays - more subtle and iOS-like
    val WeatherTextPrimary = Color.White.copy(alpha = 0.92f)
    val WeatherTextSecondary = Color.White.copy(alpha = 0.78f)
    val WeatherTextTertiary = Color.White.copy(alpha = 0.65f)
    val WeatherTextQuaternary = Color.White.copy(alpha = 0.45f)
} 