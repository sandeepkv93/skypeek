package com.example.skypeek.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.example.skypeek.domain.model.WeatherType
import com.example.skypeek.presentation.ui.theme.WeatherColors
import kotlinx.coroutines.delay
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun WeatherBackground(
    weatherType: WeatherType,
    modifier: Modifier = Modifier
) {
    val gradient = getWeatherGradient(weatherType)
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = gradient.map { Color(it) }
                )
            )
    ) {
        // Add weather-specific effects
        when (weatherType) {
            WeatherType.SNOW -> SnowfallAnimation(modifier = Modifier.fillMaxSize())
            WeatherType.RAINY -> RainAnimation(modifier = Modifier.fillMaxSize())
            WeatherType.SUNNY -> SunAnimation(modifier = Modifier.fillMaxSize())
            else -> Unit
        }
    }
}

@Composable
private fun SnowfallAnimation(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) { 400.dp.toPx() } // Approximate screen width
    val screenHeightPx = with(density) { 800.dp.toPx() } // Approximate screen height
    
    // Create snowflakes with random positions and speeds
    val snowflakes = remember {
        List(15) {
            SnowflakeState(
                x = Random.nextFloat() * screenWidthPx,
                y = Random.nextFloat() * screenHeightPx,
                speed = Random.nextFloat() * 2f + 1f,
                size = Random.nextFloat() * 3f + 2f
            )
        }
    }
    
    // Animation for falling snowflakes
    LaunchedEffect(Unit) {
        while (true) {
            delay(50)
            snowflakes.forEach { snowflake ->
                snowflake.y += snowflake.speed * 5f
                snowflake.x += sin(snowflake.y * 0.01f) * 0.5f // Slight horizontal drift
                
                if (snowflake.y > screenHeightPx + 100f) {
                    snowflake.y = -50f
                    snowflake.x = Random.nextFloat() * screenWidthPx
                }
            }
        }
    }
    
    Canvas(modifier = modifier) {
        snowflakes.forEach { snowflake ->
            drawCircle(
                color = Color.White.copy(alpha = 0.8f),
                radius = snowflake.size,
                center = Offset(snowflake.x, snowflake.y)
            )
        }
    }
}

@Composable
private fun RainAnimation(modifier: Modifier = Modifier) {
    val density = LocalDensity.current
    val screenWidthPx = with(density) { 400.dp.toPx() }
    val screenHeightPx = with(density) { 800.dp.toPx() }
    
    val raindrops = remember {
        List(25) {
            RaindropState(
                x = Random.nextFloat() * screenWidthPx,
                y = Random.nextFloat() * screenHeightPx,
                speed = Random.nextFloat() * 8f + 8f,
                length = Random.nextFloat() * 20f + 10f
            )
        }
    }
    
    LaunchedEffect(Unit) {
        while (true) {
            delay(16) // ~60fps
            raindrops.forEach { raindrop ->
                raindrop.y += raindrop.speed
                raindrop.x += raindrop.speed * 0.2f // Slight diagonal movement
                
                if (raindrop.y > screenHeightPx + 50f) {
                    raindrop.y = -raindrop.length
                    raindrop.x = Random.nextFloat() * screenWidthPx
                }
            }
        }
    }
    
    Canvas(modifier = modifier) {
        raindrops.forEach { raindrop ->
            // Draw rain streak
            drawLine(
                color = Color.White.copy(alpha = 0.6f),
                start = Offset(raindrop.x, raindrop.y),
                end = Offset(raindrop.x - raindrop.length * 0.2f, raindrop.y - raindrop.length),
                strokeWidth = 2f
            )
        }
    }
}

@Composable
private fun SunAnimation(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "sun_glow")
    
    // Very subtle glow animation - more like iOS
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 80f,
        targetValue = 120f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_radius"
    )
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width * 0.75f, size.height * 0.15f)
        
        // Outer glow - very subtle
        drawCircle(
            color = Color.White.copy(alpha = glowAlpha * 0.3f),
            radius = glowRadius * 1.5f,
            center = center
        )
        
        // Middle glow
        drawCircle(
            color = Color.White.copy(alpha = glowAlpha * 0.5f),
            radius = glowRadius,
            center = center
        )
        
        // Inner subtle glow
        drawCircle(
            color = Color.White.copy(alpha = glowAlpha),
            radius = glowRadius * 0.6f,
            center = center
        )
    }
}

// Helper data classes for animations
private class SnowflakeState(
    var x: Float,
    var y: Float,
    val speed: Float,
    val size: Float
)

private class RaindropState(
    var x: Float,
    var y: Float,
    val speed: Float,
    val length: Float
)

private fun getWeatherGradient(weatherType: WeatherType): List<ULong> {
    return when (weatherType) {
        WeatherType.SUNNY -> listOf(
            Color(0xFF4A90E2).value,  // Bright blue at top
            Color(0xFF87CEEB).value,  // Sky blue
            Color(0xFF98D8E8).value,  // Light blue
            Color(0xFFFFF8F0).value   // Very light cream at bottom
        )
        WeatherType.CLOUDY -> listOf(
            Color(0xFF8E9AAF).value,    // Cool gray-blue
            Color(0xFFA8B4C7).value,    // Light gray-blue  
            Color(0xFFCBD2E1).value,    // Very light gray-blue
            Color(0xFFF1F3F6).value     // Off-white
        )
        WeatherType.RAINY -> listOf(
            Color(0xFF2F4858).value,    // Dark blue-gray
            Color(0xFF4A6741).value,    // Deeper gray-blue
            Color(0xFF5D7A89).value,    // Medium gray-blue
            Color(0xFF8FA7B7).value     // Light gray-blue
        )
        WeatherType.SNOW -> listOf(
            Color(0xFFB8C6DB).value,    // Light blue-gray
            Color(0xFFD1DCE8).value,    // Very light blue-gray
            Color(0xFFE8EDF4).value,    // Almost white blue
            Color(0xFFFAFBFC).value     // Pure white
        )
        WeatherType.STORMY -> listOf(
            Color(0xFF1C1C1C).value,    // Very dark
            Color(0xFF2F2F35).value,    // Dark gray
            Color(0xFF4A4A50).value,    // Medium dark gray
            Color(0xFF6A6A70).value     // Lighter dark gray
        )
        WeatherType.FOGGY -> listOf(
            Color(0xFF6B7280).value,    // Gray
            Color(0xFF8B92A5).value,    // Light gray
            Color(0xFFB1B8CA).value,    // Lighter gray
            Color(0xFFD5D9E4).value     // Very light gray
        )
    }
} 