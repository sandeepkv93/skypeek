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
        Color(0xFF5A9FD4), // Softer blue
        Color(0xFF7BB3E0)  // Medium blue
    )
    
    val CloudyGradient = listOf(
        Color(0xFF6B7A8A), // Muted gray-blue
        Color(0xFF7D8C9C), // Slightly lighter gray-blue
        Color(0xFF9FAAB5)  // Light gray-blue
    )
    
    val RainyGradient = listOf(
        Color(0xFF3A4A5C), // Dark muted blue-gray
        Color(0xFF4A5A6C), // Medium dark gray-blue
        Color(0xFF5A6A7C)  // Medium gray-blue
    )
    
    val SnowGradient = listOf(
        Color(0xFF9BB0C4), // Soft blue-gray
        Color(0xFFB1C6DA), // Light blue-gray
        Color(0xFFC7DCF0)  // Very light blue
    )
    
    val StormyGradient = listOf(
        Color(0xFF2A2A2F), // Dark but not black
        Color(0xFF3A3A40), // Medium dark gray
        Color(0xFF4A4A50)  // Lighter dark gray
    )
    
    val FoggyGradient = listOf(
        Color(0xFF5A6670), // Muted gray
        Color(0xFF6A7680), // Light muted gray
        Color(0xFF8A96A0)  // Lighter gray
    )
    
    // Text colors for weather overlays - slightly more transparent for elegance
    val WeatherTextPrimary = Color.White.copy(alpha = 0.95f)
    val WeatherTextSecondary = Color.White.copy(alpha = 0.85f)
    val WeatherTextTertiary = Color.White.copy(alpha = 0.75f)
    val WeatherTextQuaternary = Color.White.copy(alpha = 0.55f)
} 