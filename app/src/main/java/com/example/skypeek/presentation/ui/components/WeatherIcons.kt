package com.example.skypeek.presentation.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.PI

@Composable
fun WeatherIcon(
    weatherCode: Int,
    modifier: Modifier = Modifier,
    size: Dp = 32.dp
) {
    when (weatherCode) {
        0 -> SunnyIcon(modifier = modifier.size(size))
        1 -> PartlyCloudyIcon(modifier = modifier.size(size))
        2, 3 -> CloudyIcon(modifier = modifier.size(size))
        in 45..48 -> FoggyIcon(modifier = modifier.size(size))
        in 51..67 -> RainyIcon(modifier = modifier.size(size))
        in 71..86 -> SnowyIcon(modifier = modifier.size(size))
        in 95..99 -> StormyIcon(modifier = modifier.size(size))
        else -> SunnyIcon(modifier = modifier.size(size)) // Default
    }
}

@Composable
fun SunnyIcon(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFFFFCC33) // iOS-style golden yellow
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val centerRadius = size.minDimension * 0.22f
        val rayLength = size.minDimension * 0.18f
        val rayWidth = size.minDimension * 0.025f
        
        // Draw 8 sun rays exactly like iOS - rounded ends
        for (i in 0 until 8) {
            val angle = (i * 45.0) * PI / 180.0
            val innerRadius = centerRadius + size.minDimension * 0.08f
            val outerRadius = innerRadius + rayLength
            
            val startX = center.x + (innerRadius * cos(angle)).toFloat()
            val startY = center.y + (innerRadius * sin(angle)).toFloat()
            val endX = center.x + (outerRadius * cos(angle)).toFloat()
            val endY = center.y + (outerRadius * sin(angle)).toFloat()
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = rayWidth,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        
        // Draw central sun circle with subtle gradient effect
        drawCircle(
            color = color,
            radius = centerRadius,
            center = center
        )
        
        // Add inner highlight for depth (iOS effect)
        drawCircle(
            color = Color(0xFFFFE55C),
            radius = centerRadius * 0.6f,
            center = Offset(center.x - centerRadius * 0.1f, center.y - centerRadius * 0.1f)
        )
    }
}

@Composable
fun PartlyCloudyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        // Draw sun behind cloud (iOS positioning)
        val sunCenter = Offset(size.width * 0.35f, size.height * 0.35f)
        val sunRadius = size.minDimension * 0.16f
        
        // Sun rays (partial, like iOS)
        val sunColor = Color(0xFFFFCC33)
        for (i in listOf(0, 1, 7)) { // Only show visible rays
            val angle = (i * 45.0) * PI / 180.0
            val innerRadius = sunRadius + size.minDimension * 0.04f
            val outerRadius = innerRadius + size.minDimension * 0.12f
            
            val startX = sunCenter.x + (innerRadius * cos(angle)).toFloat()
            val startY = sunCenter.y + (innerRadius * sin(angle)).toFloat()
            val endX = sunCenter.x + (outerRadius * cos(angle)).toFloat()
            val endY = sunCenter.y + (outerRadius * sin(angle)).toFloat()
            
            drawLine(
                color = sunColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = size.minDimension * 0.02f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
        
        // Sun circle
        drawCircle(
            color = sunColor,
            radius = sunRadius,
            center = sunCenter
        )
        
        // Cloud overlapping sun (iOS style)
        drawIOSCloud(
            center = Offset(size.width * 0.58f, size.height * 0.58f),
            scale = 0.85f
        )
    }
}

@Composable
fun CloudyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawIOSCloud(center = center, scale = 1.0f)
    }
}

@Composable
fun RainyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cloudCenter = Offset(size.width / 2f, size.height * 0.4f)
        
        // Draw cloud
        drawIOSCloud(center = cloudCenter, scale = 0.9f, color = Color(0xFFDDDDDD))
        
        // Draw rain drops (iOS style - angled and varied)
        val rainColor = Color(0xFF4A9FFF)
        val rainDrops = listOf(
            Pair(0.3f, 0.65f),
            Pair(0.45f, 0.7f),
            Pair(0.6f, 0.65f),
            Pair(0.75f, 0.7f)
        )
        
        rainDrops.forEach { (xRatio, yRatio) ->
            val startX = size.width * xRatio
            val startY = size.height * yRatio
            val endX = startX + size.width * 0.02f
            val endY = startY + size.height * 0.18f
            
            drawLine(
                color = rainColor,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = size.minDimension * 0.015f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
fun SnowyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cloudCenter = Offset(size.width / 2f, size.height * 0.4f)
        
        // Draw cloud
        drawIOSCloud(center = cloudCenter, scale = 0.9f, color = Color(0xFFF0F0F0))
        
        // Draw snowflakes (iOS style - 6-pointed stars)
        val snowColor = Color.White
        val snowflakes = listOf(
            Triple(0.25f, 0.65f, 0.8f),
            Triple(0.45f, 0.7f, 1.0f),
            Triple(0.65f, 0.65f, 0.9f),
            Triple(0.8f, 0.75f, 0.7f)
        )
        
        snowflakes.forEach { (xRatio, yRatio, sizeRatio) ->
            val center = Offset(size.width * xRatio, size.height * yRatio)
            val flakeSize = size.minDimension * 0.05f * sizeRatio
            drawIOSSnowflake(center, flakeSize, snowColor)
        }
    }
}

@Composable
fun StormyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val cloudCenter = Offset(size.width / 2f, size.height * 0.4f)
        
        // Draw dark storm cloud
        drawIOSCloud(center = cloudCenter, scale = 0.95f, color = Color(0xFF666666))
        
        // Draw lightning bolt (iOS style - more realistic zigzag)
        val lightningColor = Color(0xFFFFE55C)
        val path = Path().apply {
            // Main lightning bolt path
            moveTo(size.width * 0.48f, size.height * 0.6f)
            lineTo(size.width * 0.44f, size.height * 0.7f)
            lineTo(size.width * 0.5f, size.height * 0.72f)
            lineTo(size.width * 0.46f, size.height * 0.82f)
            lineTo(size.width * 0.54f, size.height * 0.75f)
            lineTo(size.width * 0.5f, size.height * 0.73f)
            lineTo(size.width * 0.52f, size.height * 0.62f)
            close()
        }
        
        drawPath(
            path = path,
            color = lightningColor
        )
        
        // Add glow effect
        drawPath(
            path = path,
            color = lightningColor.copy(alpha = 0.3f),
            style = Stroke(width = size.minDimension * 0.02f)
        )
    }
}

@Composable
fun FoggyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val fogColor = Color(0xFFCCCCCC)
        
        // Draw horizontal fog lines (iOS style - varying lengths and opacity)
        val fogLines = listOf(
            Triple(0.2f, 0.35f, 0.6f), // x start ratio, y ratio, length ratio
            Triple(0.15f, 0.45f, 0.7f),
            Triple(0.25f, 0.55f, 0.5f),
            Triple(0.2f, 0.65f, 0.65f)
        )
        
        fogLines.forEachIndexed { index, (startRatio, yRatio, lengthRatio) ->
            val alpha = 1.0f - (index * 0.15f)
            val y = size.height * yRatio
            val startX = size.width * startRatio
            val endX = startX + (size.width * lengthRatio)
            
            drawLine(
                color = fogColor.copy(alpha = alpha),
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = size.minDimension * 0.035f,
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

// iOS-style cloud helper function
private fun DrawScope.drawIOSCloud(
    center: Offset,
    scale: Float,
    color: Color = Color.White
) {
    val baseRadius = size.minDimension * 0.12f * scale
    
    // iOS clouds have a more organic, puffy appearance
    // Main cloud segments
    val cloudSegments = listOf(
        Triple(-0.8f, 0.1f, 0.8f), // left bump
        Triple(-0.3f, -0.4f, 0.9f), // top left
        Triple(0.2f, -0.5f, 0.85f), // top right
        Triple(0.7f, 0.0f, 0.8f), // right bump
        Triple(0.0f, 0.2f, 1.0f) // main body
    )
    
    cloudSegments.forEach { (xOffset, yOffset, radiusScale) ->
        drawCircle(
            color = color,
            radius = baseRadius * radiusScale,
            center = Offset(
                center.x + baseRadius * xOffset,
                center.y + baseRadius * yOffset
            )
        )
    }
    
    // Add subtle shadow/depth
    drawCircle(
        color = color.copy(alpha = 0.3f),
        radius = baseRadius * 0.7f,
        center = Offset(center.x + baseRadius * 0.1f, center.y + baseRadius * 0.3f)
    )
}

// iOS-style snowflake helper function
private fun DrawScope.drawIOSSnowflake(
    center: Offset,
    radius: Float,
    color: Color
) {
    // Draw 6 main arms
    for (i in 0 until 6) {
        val angle = (i * 60.0) * PI / 180.0
        val endX = center.x + (radius * cos(angle)).toFloat()
        val endY = center.y + (radius * sin(angle)).toFloat()
        
        drawLine(
            color = color,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = radius * 0.15f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        // Add small branches (iOS detail)
        val branchLength = radius * 0.3f
        val branchAngle1 = angle + PI / 6
        val branchAngle2 = angle - PI / 6
        
        val branchStart = Offset(
            center.x + (radius * 0.7f * cos(angle)).toFloat(),
            center.y + (radius * 0.7f * sin(angle)).toFloat()
        )
        
        drawLine(
            color = color,
            start = branchStart,
            end = Offset(
                branchStart.x + (branchLength * cos(branchAngle1)).toFloat(),
                branchStart.y + (branchLength * sin(branchAngle1)).toFloat()
            ),
            strokeWidth = radius * 0.1f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
        
        drawLine(
            color = color,
            start = branchStart,
            end = Offset(
                branchStart.x + (branchLength * cos(branchAngle2)).toFloat(),
                branchStart.y + (branchLength * sin(branchAngle2)).toFloat()
            ),
            strokeWidth = radius * 0.1f,
            cap = androidx.compose.ui.graphics.StrokeCap.Round
        )
    }
} 