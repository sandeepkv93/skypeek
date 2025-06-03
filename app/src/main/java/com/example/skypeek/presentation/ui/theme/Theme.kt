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
        Color(0xFF87CEEB), // Light blue
        Color(0xFF4A90E2)  // Deeper blue
    )
    
    val CloudyGradient = listOf(
        Color(0xFFDDA0DD), // Plum
        Color(0xFF9370DB), // Medium slate blue
        Color(0xFF663399)  // Rebecca purple
    )
    
    val RainyGradient = listOf(
        Color(0xFF4A4A4A), // Dark gray
        Color(0xFF2F2F2F), // Darker gray
        Color(0xFF1A1A1A)  // Almost black
    )
    
    val SnowGradient = listOf(
        Color(0xFFB0C4DE), // Light steel blue
        Color(0xFF778899), // Light slate gray
        Color(0xFF556B8D)  // Dark slate blue
    )
    
    val StormyGradient = listOf(
        Color(0xFF2F2F2F), // Very dark gray
        Color(0xFF1A1A1A), // Almost black
        Color(0xFF000000)  // Black
    )
    
    val FoggyGradient = listOf(
        Color(0xFF696969), // Dim gray
        Color(0xFF484848), // Dark gray
        Color(0xFF2F2F2F)  // Very dark gray
    )
    
    // Text colors for weather overlays
    val WeatherTextPrimary = Color.White
    val WeatherTextSecondary = Color.White.copy(alpha = 0.9f)
    val WeatherTextTertiary = Color.White.copy(alpha = 0.8f)
    val WeatherTextQuaternary = Color.White.copy(alpha = 0.6f)
} 