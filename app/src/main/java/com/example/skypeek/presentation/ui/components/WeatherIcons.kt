package com.example.skypeek.presentation.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun WeatherIcon(
    weatherCode: Int,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp,
    timestamp: Long = System.currentTimeMillis()
) {
    val isNightTime = isNightTime(timestamp)
    
    when (weatherCode) {
        0 -> {
            if (isNightTime) MoonIcon(modifier = modifier.size(size)) 
            else SunnyIcon(modifier = modifier.size(size))
        }
        1 -> {
            if (isNightTime) PartlyCloudyNightIcon(modifier = modifier.size(size))
            else PartlyCloudyIcon(modifier = modifier.size(size))
        }
        2, 3 -> {
            CloudyIcon(modifier = modifier.size(size))
        }
        in 45..48 -> {
            FoggyIcon(modifier = modifier.size(size))
        }
        // Mist/Haze codes (if any additional ones exist)
        in 700..799 -> {
            FoggyIcon(modifier = modifier.size(size))
        }
        // FIXED: Proper rain codes (including freezing rain but not snow)
        51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> {
            RainyIcon(modifier = modifier.size(size))
        }
        // FIXED: Only true snow codes
        71, 73, 75, 77, 85, 86 -> {
            SnowyIcon(modifier = modifier.size(size))
        }
        in 95..99 -> {
            StormyIcon(modifier = modifier.size(size))
        }
        else -> {
            if (isNightTime) MoonIcon(modifier = modifier.size(size))
            else SunnyIcon(modifier = modifier.size(size))
        }
    }
}

@Composable
fun SunnyIcon(
    modifier: Modifier = Modifier
) {
    // Optimized sun animations - reduced complexity
    val infiniteTransition = rememberInfiniteTransition(label = "sun_rotation")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 30000, easing = LinearEasing), // Slower rotation
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rays_rotation"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val sunRadius = size.minDimension * 0.22f
        val rayLength = size.minDimension * 0.18f
        val rayStartDistance = size.minDimension * 0.32f
        val rayThickness = size.minDimension * 0.045f
        
        // iOS sun colors with gradient
        val sunGradient = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFEB3B), // Bright yellow center
                Color(0xFFFFC107), // Orange-yellow middle
                Color(0xFFFF9800)  // Orange edge
            ),
            center = center,
            radius = sunRadius
        )
        
        // Simple outer glow - no animation for better performance
        drawCircle(
            color = Color(0x15FFEB3B),
            radius = sunRadius * 1.2f,
            center = center
        )
        
        // Reduced sun rays for better performance
        rotate(rotation, center) {
            for (i in 0 until 6) {
                val angle = (i * 60.0) * PI / 180.0
                val rayStart = Offset(
                    center.x + (rayStartDistance * cos(angle)).toFloat(),
                    center.y + (rayStartDistance * sin(angle)).toFloat()
                )
                val rayEnd = Offset(
                    center.x + ((rayStartDistance + rayLength) * cos(angle)).toFloat(),
                    center.y + ((rayStartDistance + rayLength) * sin(angle)).toFloat()
                )
                
                drawLine(
                    color = Color(0xFFFFB300), // iOS sun ray color
                    start = rayStart,
                    end = rayEnd,
                    strokeWidth = rayThickness,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
        
        // Main sun circle with gradient
        drawCircle(
            brush = sunGradient,
            radius = sunRadius,
            center = center
        )
        
        // Inner highlight for 3D iOS effect
        drawCircle(
            color = Color(0x40FFFFFF), // Semi-transparent white highlight
            radius = sunRadius * 0.6f,
            center = Offset(
                center.x - sunRadius * 0.15f,
                center.y - sunRadius * 0.15f
            )
        )
    }
}

@Composable
fun PartlyCloudyIcon(
    modifier: Modifier = Modifier
) {
    // Gentle cloud floating animation
    val infiniteTransition = rememberInfiniteTransition(label = "partly_cloudy_anim")
    val cloudOffset by infiniteTransition.animateFloat(
        initialValue = -2f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_float"
    )
    
    // Sun ray rotation (slower for partly cloudy)
    val sunRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 25000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sun_rotation"
    )

    Canvas(modifier = modifier) {
        // Sun positioned exactly like iOS
        val sunCenter = Offset(size.width * 0.3f, size.height * 0.3f)
        val sunRadius = size.minDimension * 0.15f
        
        val sunGradient = Brush.radialGradient(
            colors = listOf(Color(0xFFFFEB3B), Color(0xFFFFC107)),
            center = sunCenter,
            radius = sunRadius
        )
        
        // Draw animated partial sun rays
        rotate(sunRotation, sunCenter) {
            val visibleRays = listOf(0, 1, 7) // Top-right, right, and top rays
            visibleRays.forEach { i ->
                val angle = (i * 45.0) * PI / 180.0
                val rayStart = Offset(
                    sunCenter.x + (sunRadius * 1.5f * cos(angle)).toFloat(),
                    sunCenter.y + (sunRadius * 1.5f * sin(angle)).toFloat()
                )
                val rayEnd = Offset(
                    sunCenter.x + (sunRadius * 2.2f * cos(angle)).toFloat(),
                    sunCenter.y + (sunRadius * 2.2f * sin(angle)).toFloat()
                )
                
                drawLine(
                    color = Color(0xFFFFB300),
                    start = rayStart,
                    end = rayEnd,
                    strokeWidth = size.minDimension * 0.04f,
                    cap = androidx.compose.ui.graphics.StrokeCap.Round
                )
            }
        }
        
        // Sun circle with gradient
        drawCircle(
            brush = sunGradient,
            radius = sunRadius,
            center = sunCenter
        )
        
        // Animated cloud with floating movement
        val cloudCenter = Offset(
            size.width * 0.65f + cloudOffset,
            size.height * 0.65f
        )
        drawIOSCloudWithShadow(
            center = cloudCenter,
            scale = 0.85f
        )
    }
}

@Composable
fun CloudyIcon(
    modifier: Modifier = Modifier
) {
    // Multiple cloud layers with different animations
    val infiniteTransition = rememberInfiniteTransition(label = "cloudy_animation")
    
    // Main cloud breathing
    val mainCloudScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "main_cloud_scale"
    )
    
    // Background cloud drift
    val bgCloudOffset by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bg_cloud_drift"
    )
    
    // Foreground cloud movement
    val fgCloudOffset by infiniteTransition.animateFloat(
        initialValue = 2f,
        targetValue = -2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fg_cloud_drift"
    )

    Canvas(modifier = modifier) {
        // Background cloud layer (darker, larger, slower) - MADE WIDER
        drawEnhancedIOSCloud(
            center = Offset(size.width * 0.35f + bgCloudOffset, size.height * 0.6f),
            scale = 1.4f, // Increased from 1.2f
            cloudColor = Color(0xFFD3D3D3), // Light gray
            shadowIntensity = 0.15f
        )
        
        // Middle cloud layer (medium color, main focus) - MADE WIDER
        drawEnhancedIOSCloud(
            center = Offset(size.width / 2f, size.height * 0.45f),
            scale = mainCloudScale * 1.3f, // Increased from 1.0f
            cloudColor = Color.White,
            shadowIntensity = 0.2f
        )
        
        // Foreground cloud layer (brightest, smaller, faster) - MADE WIDER
        drawEnhancedIOSCloud(
            center = Offset(size.width * 0.7f + fgCloudOffset, size.height * 0.3f),
            scale = 1.0f, // Increased from 0.8f
            cloudColor = Color(0xFFFAFAFA), // Very light gray
            shadowIntensity = 0.1f
        )
    }
}

@Composable
fun RainyIcon(
    modifier: Modifier = Modifier
) {
    // Continuous rain drop animation
    val infiniteTransition = rememberInfiniteTransition(label = "rain_animation")
    val rainOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rain_drops"
    )
    
    // Cloud gentle movement
    val cloudSway by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_sway"
    )

    Canvas(modifier = modifier) {
        // Animated WIDER cloud with gentle swaying
        drawEnhancedIOSCloud(
            center = Offset(size.width / 2f + cloudSway, size.height * 0.2f),
            scale = 1.2f, // Much wider than before
            cloudColor = Color(0xFFE0E0E0), // Slightly darker for rain
            shadowIntensity = 0.25f
        )
        
        // Reduced rain drops for better performance
        val rainDrops = listOf(
            Triple(0.2f, 0.55f, 1.0f),   // x, y, size
            Triple(0.5f, 0.6f, 0.9f),
            Triple(0.8f, 0.55f, 1.0f)
        )
        
        rainDrops.forEachIndexed { index, (x, yStart, sizeScale) ->
            val dropHeight = size.height * 0.25f * sizeScale
            val strokeWidth = size.minDimension * 0.025f * sizeScale
            
            // Stagger the animation for each drop
            val staggeredOffset = (rainOffset + index * 0.2f) % 1f
            val animatedY = yStart + (staggeredOffset * 0.3f)
            
            // Create fading effect as drops fall
            val alpha = 1f - (staggeredOffset * 0.3f)
            
            drawLine(
                color = Color(0xFF2196F3).copy(alpha = alpha),
                start = Offset(size.width * x, size.height * animatedY),
                end = Offset(
                    size.width * (x + 0.02f),
                    size.height * animatedY + dropHeight * (1f - staggeredOffset * 0.3f)
                ),
                strokeWidth = strokeWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun SnowyIcon(
    modifier: Modifier = Modifier
) {
    // Gentle snowflake falling animation
    val infiniteTransition = rememberInfiniteTransition(label = "snow_animation")
    val snowFall by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowflakes_falling"
    )
    
    // Snowflake rotation
    val snowRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "snowflake_rotation"
    )

    Canvas(modifier = modifier) {
        // WIDER Cloud for snow
        drawEnhancedIOSCloud(
            center = Offset(size.width / 2f, size.height * 0.2f),
            scale = 1.2f, // Much wider than before
            cloudColor = Color(0xFFF5F5F5),
            shadowIntensity = 0.15f
        )
        
        // Reduced snowflakes for better performance
        val snowflakes = listOf(
            Triple(0.3f, 0.55f, 1.0f),
            Triple(0.7f, 0.65f, 0.9f)
        )
        
        snowflakes.forEachIndexed { index, (x, y, scale) ->
            // Staggered falling animation
            val staggeredFall = (snowFall + index * 0.3f) % 1f
            val animatedY = y + (staggeredFall * 0.2f)
            
            // Gentle side-to-side drift
            val drift = sin((snowFall + index) * 2 * PI).toFloat() * 0.02f
            
            val snowflakeCenter = Offset(
                size.width * (x + drift),
                size.height * animatedY
            )
            
            // Rotate each snowflake individually
            rotate(snowRotation + index * 60f, snowflakeCenter) {
                drawIOSSnowflake(
                    center = snowflakeCenter,
                    radius = size.minDimension * 0.04f * scale,
                    color = Color.White.copy(alpha = 1f - staggeredFall * 0.3f)
                )
            }
        }
    }
}

@Composable
fun StormyIcon(
    modifier: Modifier = Modifier
) {
    // Simplified lightning flash animation
    val infiniteTransition = rememberInfiniteTransition(label = "storm_animation")
    val lightningFlash by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 4000
                0f at 0
                0f at 3000
                1f at 3100
                0f at 3200
                1f at 3250
                0f at 3300
                0f at 4000
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "lightning_flash"
    )
    
    // Cloud rumbling
    val cloudRumble by infiniteTransition.animateFloat(
        initialValue = -0.5f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_rumble"
    )

    Canvas(modifier = modifier) {
        // Animated storm cloud with rumbling
        drawEnhancedIOSCloud(
            center = Offset(size.width / 2f + cloudRumble, size.height * 0.2f),
            scale = 1.3f,
            cloudColor = Color(0xFF666666),
            shadowIntensity = 0.3f
        )
        
        // Lightning bolt with flash effect
        val lightningPath = Path().apply {
            moveTo(size.width * 0.42f, size.height * 0.5f)
            lineTo(size.width * 0.35f, size.height * 0.68f)
            lineTo(size.width * 0.44f, size.height * 0.68f)
            lineTo(size.width * 0.38f, size.height * 0.85f)
            lineTo(size.width * 0.55f, size.height * 0.65f)
            lineTo(size.width * 0.46f, size.height * 0.65f)
            lineTo(size.width * 0.52f, size.height * 0.5f)
            close()
        }
        
        // Lightning glow effect (animated)
        if (lightningFlash > 0.3f) {
            drawPath(
                path = lightningPath,
                color = Color(0x60FFEB3B)
            )
        }
        
        // Main lightning bolt (with flash intensity)
        val lightningAlpha = if (lightningFlash > 0.3f) 1f else 0.7f + lightningFlash * 0.3f
        drawPath(
            path = lightningPath,
            color = Color(0xFFFFEB3B).copy(alpha = lightningAlpha)
        )
    }
}

@Composable
fun FoggyIcon(
    modifier: Modifier = Modifier
) {
    // Simplified flowing fog animation
    val infiniteTransition = rememberInfiniteTransition(label = "fog_animation")
    val fogFlow by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fog_flow"
    )

    Canvas(modifier = modifier) {
        val fogLines = listOf(
            Triple(0.05f, 0.2f, 0.9f),  // start x, y, length
            Triple(0.1f, 0.3f, 0.8f),
            Triple(0.0f, 0.4f, 1.0f),
            Triple(0.15f, 0.5f, 0.7f),
            Triple(0.05f, 0.6f, 0.85f),
            Triple(0.1f, 0.7f, 0.75f),
            Triple(0.0f, 0.8f, 0.9f)
        )
        
        fogLines.forEachIndexed { index, (startX, y, length) ->
            val alpha = 0.9f - (index * 0.1f)
            val thickness = size.minDimension * (0.025f - index * 0.002f)
            
            // Simplified flowing animation
            val flowOffset = (fogFlow + index * 0.2f) % 1f
            val animatedStartX = startX + (sin(flowOffset * 2 * PI).toFloat() * 0.05f)
            val animatedLength = length + (cos(flowOffset * 2 * PI).toFloat() * 0.1f)
            
            drawLine(
                color = Color(0xFFE0E0E0).copy(alpha = alpha * (0.7f + flowOffset * 0.3f)),
                start = Offset(size.width * animatedStartX, size.height * y),
                end = Offset(size.width * (animatedStartX + animatedLength), size.height * y),
                strokeWidth = thickness,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun MoonIcon(
    modifier: Modifier = Modifier
) {
    // Gentle moon glow pulsing
    val infiniteTransition = rememberInfiniteTransition(label = "moon_animation")
    val moonGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moon_glow"
    )

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val moonRadius = size.minDimension * 0.28f
        
        // iOS moon gradient
        val moonGradient = Brush.radialGradient(
            colors = listOf(
                Color(0xFFF5F5DC), // Beige center
                Color(0xFFE6E6FA), // Lavender edge
                Color(0xFFD3D3D3)  // Light gray outer
            ),
            center = center,
            radius = moonRadius
        )
        
        // Animated outer glow
        drawCircle(
            color = Color(0x30F5F5DC),
            radius = moonRadius * 1.4f * moonGlow,
            center = center
        )
        
        // Main moon circle with gradient
        drawCircle(
            brush = moonGradient,
            radius = moonRadius,
            center = center
        )
        
        // Crescent shadow effect
        val shadowCenter = Offset(
            center.x + moonRadius * 0.25f,
            center.y - moonRadius * 0.15f
        )
        
        drawCircle(
            color = Color(0x40696969),
            radius = moonRadius * 0.85f,
            center = shadowCenter
        )
        
        // Surface texture dots (subtle iOS detail)
        val craterPositions = listOf(
            Offset(center.x - moonRadius * 0.3f, center.y - moonRadius * 0.2f),
            Offset(center.x + moonRadius * 0.2f, center.y + moonRadius * 0.3f),
            Offset(center.x - moonRadius * 0.1f, center.y + moonRadius * 0.4f)
        )
        
        craterPositions.forEach { craterPos ->
            drawCircle(
                color = Color(0x20696969),
                radius = moonRadius * 0.08f,
                center = craterPos
            )
        }
    }
}

@Composable
fun PartlyCloudyNightIcon(
    modifier: Modifier = Modifier
) {
    // Moon glow and cloud drift
    val infiniteTransition = rememberInfiniteTransition(label = "partly_cloudy_night")
    val moonGlow by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 5000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "moon_glow"
    )
    
    val cloudDrift by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud_drift"
    )

    Canvas(modifier = modifier) {
        // Moon positioned like iOS
        val moonCenter = Offset(size.width * 0.3f, size.height * 0.3f)
        val moonRadius = size.minDimension * 0.14f
        
        val moonGradient = Brush.radialGradient(
            colors = listOf(Color(0xFFF5F5DC), Color(0xFFE6E6FA)),
            center = moonCenter,
            radius = moonRadius
        )
        
        // Animated moon glow
        drawCircle(
            color = Color(0x25F5F5DC),
            radius = moonRadius * 1.5f * moonGlow,
            center = moonCenter
        )
        
        // Moon circle with gradient
        drawCircle(
            brush = moonGradient,
            radius = moonRadius,
            center = moonCenter
        )
        
        // Cloud positioned to partially cover moon with drift animation
        drawIOSCloudWithShadow(
            center = Offset(size.width * 0.65f + cloudDrift, size.height * 0.65f),
            scale = 0.85f,
            cloudColor = Color(0xFFF8F8F8)
        )
    }
}

// Optimized iOS cloud with minimal memory footprint
private fun DrawScope.drawIOSCloudWithShadow(
    center: Offset,
    scale: Float,
    cloudColor: Color = Color.White
) {
    val baseRadius = size.minDimension * 0.08f * scale
    
    // Simplified cloud structure - reduced from 23 to 8 circles
    val optimizedCloudParts = listOf(
        Triple(0.0f, 0.4f, 1.4f),       // Center bottom
        Triple(-0.5f, 0.4f, 1.2f),      // Left bottom
        Triple(0.5f, 0.4f, 1.2f),       // Right bottom
        Triple(-0.8f, 0.15f, 1.0f),     // Far left
        Triple(0.8f, 0.15f, 1.0f),      // Far right
        Triple(-0.15f, -0.3f, 0.85f),   // Top left-center
        Triple(0.15f, -0.3f, 0.85f),    // Top right-center
        Triple(0.0f, -0.4f, 0.8f),      // Top center peak
    )
    
    // Draw shadow (single pass)
    val shadowCenter = Offset(center.x + baseRadius * 0.05f, center.y + baseRadius * 0.05f)
    optimizedCloudParts.forEach { (xOffset, yOffset, sizeMultiplier) ->
        drawCircle(
            color = Color(0x15000000),
            radius = baseRadius * sizeMultiplier,
            center = Offset(
                shadowCenter.x + baseRadius * xOffset,
                shadowCenter.y + baseRadius * yOffset
            )
        )
    }
    
    // Draw main cloud (single pass)
    optimizedCloudParts.forEach { (xOffset, yOffset, sizeMultiplier) ->
        drawCircle(
            color = cloudColor,
            radius = baseRadius * sizeMultiplier,
            center = Offset(
                center.x + baseRadius * xOffset,
                center.y + baseRadius * yOffset
            )
        )
    }
}



// Memory-optimized snowflake function
private fun DrawScope.drawIOSSnowflake(
    center: Offset,
    radius: Float,
    color: Color
) {
    // Simplified snowflake - just 4 main arms instead of 6 with branches
    drawCircle(
        color = color,
        radius = radius * 0.15f,
        center = center
    )
    
    // Draw 4 main arms only (reduced complexity)
    for (i in 0 until 4) {
        val angle = (i * 90.0) * PI / 180.0
        val endX = center.x + (radius * cos(angle)).toFloat()
        val endY = center.y + (radius * sin(angle)).toFloat()
        
        drawLine(
            color = color,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = radius * 0.1f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
}

// Helper function to determine if it's night time - RESTORED TO NORMAL
private fun isNightTime(timestamp: Long): Boolean {
    val calendar = java.util.Calendar.getInstance().apply {
        timeInMillis = timestamp
    }
    val hour = calendar.get(java.util.Calendar.HOUR_OF_DAY)
    
    // Night time: 8 PM (20:00) to 6 AM (06:00)
    return hour >= 20 || hour < 6
}

// Memory-optimized cloud drawing function
private fun DrawScope.drawEnhancedIOSCloud(
    center: Offset,
    scale: Float,
    cloudColor: Color = Color.White,
    shadowIntensity: Float = 0.2f
) {
    val baseRadius = size.minDimension * 0.08f * scale
    
    // Drastically simplified cloud structure - only 6 circles instead of 17
    val cloudParts = listOf(
        Triple(0.0f, 0.4f, 1.4f),       // Center bottom
        Triple(-0.6f, 0.2f, 1.0f),      // Left side
        Triple(0.6f, 0.2f, 1.0f),       // Right side
        Triple(-0.2f, -0.3f, 0.85f),    // Top left
        Triple(0.2f, -0.3f, 0.85f),     // Top right
        Triple(0.0f, -0.35f, 0.8f),     // Top center
    )
    
    // Single pass drawing - no shadow for better performance
    cloudParts.forEach { (xOffset, yOffset, sizeMultiplier) ->
        drawCircle(
            color = cloudColor,
            radius = baseRadius * sizeMultiplier,
            center = Offset(
                center.x + baseRadius * xOffset,
                center.y + baseRadius * yOffset
            )
        )
    }
} 