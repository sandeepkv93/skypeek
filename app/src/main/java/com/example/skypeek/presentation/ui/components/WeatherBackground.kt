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
import com.example.skypeek.utils.AnimationPools
import com.example.skypeek.utils.PooledSnowflake
import com.example.skypeek.utils.PooledRaindrop
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
    
    // Use pooled snowflakes to reduce memory allocations
    val snowflakes = remember {
        List(10) { // Reduced from 15 to 10 for better performance
            AnimationPools.snowflakePool.acquire().apply {
                x = Random.nextFloat() * screenWidthPx
                y = Random.nextFloat() * screenHeightPx
                speed = Random.nextFloat() * 2f + 1f
                size = Random.nextFloat() * 3f + 2f
            }
        }
    }
    
    // Release pooled objects when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            snowflakes.forEach { AnimationPools.snowflakePool.release(it) }
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
    
    // Use pooled raindrops to reduce memory allocations
    val raindrops = remember {
        List(20) { // Reduced from 25 to 20 for better performance
            AnimationPools.raindropPool.acquire().apply {
                x = Random.nextFloat() * screenWidthPx
                y = Random.nextFloat() * screenHeightPx
                speed = Random.nextFloat() * 8f + 8f
                length = Random.nextFloat() * 20f + 10f
            }
        }
    }
    
    // Release pooled objects when composable is disposed
    DisposableEffect(Unit) {
        onDispose {
            raindrops.forEach { AnimationPools.raindropPool.release(it) }
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
        initialValue = 0.02f,
        targetValue = 0.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(8000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_alpha"
    )
    
    val glowRadius by infiniteTransition.animateFloat(
        initialValue = 60f,
        targetValue = 90f,
        animationSpec = infiniteRepeatable(
            animation = tween(10000, easing = EaseInOut),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_radius"
    )
    
    Canvas(modifier = modifier) {
        val center = Offset(size.width * 0.8f, size.height * 0.12f)
        
        // Outer glow - extremely subtle
        drawCircle(
            color = Color.White.copy(alpha = glowAlpha * 0.2f),
            radius = glowRadius * 1.3f,
            center = center
        )
        
        // Middle glow - very subtle
        drawCircle(
            color = Color.White.copy(alpha = glowAlpha * 0.4f),
            radius = glowRadius * 0.8f,
            center = center
        )
        
        // Inner subtle glow
        drawCircle(
            color = Color.White.copy(alpha = glowAlpha * 0.6f),
            radius = glowRadius * 0.5f,
            center = center
        )
    }
}


private fun getWeatherGradient(weatherType: WeatherType): List<ULong> {
    return when (weatherType) {
        WeatherType.SUNNY -> listOf(
            Color(0xFF5A9FD4).value,  // Softer blue at top
            Color(0xFF7BB3E0).value,  // Medium blue
            Color(0xFF9BC7EC).value,  // Light blue
            Color(0xFFB8D4F1).value   // Very light blue at bottom
        )
        WeatherType.CLOUDY -> listOf(
            Color(0xFF6B7A8A).value,    // Muted gray-blue
            Color(0xFF7D8C9C).value,    // Slightly lighter gray-blue  
            Color(0xFF9FAAB5).value,    // Light gray-blue
            Color(0xFFC1CCD7).value     // Very light gray-blue
        )
        WeatherType.RAINY -> listOf(
            Color(0xFF3A4A5C).value,    // Dark muted blue-gray
            Color(0xFF4A5A6C).value,    // Medium dark gray-blue
            Color(0xFF5A6A7C).value,    // Medium gray-blue
            Color(0xFF7A8A9C).value     // Lighter gray-blue
        )
        WeatherType.SNOW -> listOf(
            Color(0xFF9BB0C4).value,    // Soft blue-gray
            Color(0xFFB1C6DA).value,    // Light blue-gray
            Color(0xFFC7DCF0).value,    // Very light blue
            Color(0xFFE3F2FF).value     // Almost white blue
        )
        WeatherType.STORMY -> listOf(
            Color(0xFF2A2A2F).value,    // Dark but not black
            Color(0xFF3A3A40).value,    // Medium dark gray
            Color(0xFF4A4A50).value,    // Lighter dark gray
            Color(0xFF5A5A60).value     // Medium gray
        )
        WeatherType.FOGGY -> listOf(
            Color(0xFF5A6670).value,    // Muted gray
            Color(0xFF6A7680).value,    // Light muted gray
            Color(0xFF8A96A0).value,    // Lighter gray
            Color(0xFFAAB6C0).value     // Very light gray
        )
    }
} 