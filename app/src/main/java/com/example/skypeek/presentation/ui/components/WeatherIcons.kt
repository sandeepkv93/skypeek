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
    color: Color = Color(0xFFFFD700) // Golden yellow like iOS
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension * 0.25f
        val rayLength = size.minDimension * 0.15f
        val rayWidth = size.minDimension * 0.03f
        
        // Draw sun rays (8 rays like iOS)
        for (i in 0 until 8) {
            val angle = (i * 45.0) * PI / 180.0
            val startRadius = radius + size.minDimension * 0.05f
            val endRadius = startRadius + rayLength
            
            val startX = center.x + (startRadius * cos(angle)).toFloat()
            val startY = center.y + (startRadius * sin(angle)).toFloat()
            val endX = center.x + (endRadius * cos(angle)).toFloat()
            val endY = center.y + (endRadius * sin(angle)).toFloat()
            
            drawLine(
                color = color,
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = rayWidth
            )
        }
        
        // Draw central sun circle
        drawCircle(
            color = color,
            radius = radius,
            center = center
        )
    }
}

@Composable
fun PartlyCloudyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        
        // Draw sun (smaller, positioned top-right)
        val sunCenter = Offset(size.width * 0.65f, size.height * 0.35f)
        val sunRadius = size.minDimension * 0.15f
        
        drawCircle(
            color = Color(0xFFFFD700),
            radius = sunRadius,
            center = sunCenter
        )
        
        // Draw a few sun rays
        for (i in 0 until 4) {
            val angle = (i * 90.0 + 45.0) * PI / 180.0
            val startRadius = sunRadius + size.minDimension * 0.02f
            val endRadius = startRadius + size.minDimension * 0.08f
            
            val startX = sunCenter.x + (startRadius * cos(angle)).toFloat()
            val startY = sunCenter.y + (startRadius * sin(angle)).toFloat()
            val endX = sunCenter.x + (endRadius * cos(angle)).toFloat()
            val endY = sunCenter.y + (endRadius * sin(angle)).toFloat()
            
            drawLine(
                color = Color(0xFFFFD700),
                start = Offset(startX, startY),
                end = Offset(endX, endY),
                strokeWidth = size.minDimension * 0.02f
            )
        }
        
        // Draw cloud
        drawCloud(
            center = Offset(size.width * 0.45f, size.height * 0.6f),
            scale = 0.8f
        )
    }
}

@Composable
fun CloudyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        drawCloud(center = center, scale = 1.0f)
    }
}

@Composable
fun RainyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * 0.4f)
        
        // Draw cloud
        drawCloud(center = center, scale = 0.9f)
        
        // Draw rain drops
        val dropColor = Color(0xFF4A90E2)
        for (i in 0 until 3) {
            val x = size.width * (0.3f + i * 0.2f)
            val startY = size.height * 0.65f
            val endY = size.height * 0.85f
            
            drawLine(
                color = dropColor,
                start = Offset(x, startY),
                end = Offset(x, endY),
                strokeWidth = size.minDimension * 0.02f
            )
        }
    }
}

@Composable
fun SnowyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * 0.4f)
        
        // Draw cloud
        drawCloud(center = center, scale = 0.9f, color = Color(0xFFE0E0E0))
        
        // Draw snowflakes
        val snowColor = Color.White
        for (i in 0 until 4) {
            val x = size.width * (0.25f + i * 0.17f)
            val y = size.height * (0.65f + (i % 2) * 0.1f)
            
            drawSnowflake(Offset(x, y), size.minDimension * 0.03f, snowColor)
        }
    }
}

@Composable
fun StormyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height * 0.4f)
        
        // Draw dark cloud
        drawCloud(center = center, scale = 0.9f, color = Color(0xFF4A4A4A))
        
        // Draw lightning bolt
        val lightningColor = Color(0xFFFFD700)
        val path = Path().apply {
            moveTo(size.width * 0.45f, size.height * 0.6f)
            lineTo(size.width * 0.55f, size.height * 0.7f)
            lineTo(size.width * 0.5f, size.height * 0.7f)
            lineTo(size.width * 0.6f, size.height * 0.85f)
            lineTo(size.width * 0.4f, size.height * 0.75f)
            lineTo(size.width * 0.45f, size.height * 0.75f)
            close()
        }
        
        drawPath(
            path = path,
            color = lightningColor
        )
    }
}

@Composable
fun FoggyIcon(
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val fogColor = Color(0xFFB0B0B0)
        
        // Draw horizontal fog lines
        for (i in 0 until 4) {
            val y = size.height * (0.3f + i * 0.15f)
            val startX = size.width * 0.2f
            val endX = size.width * 0.8f
            
            drawLine(
                color = fogColor,
                start = Offset(startX, y),
                end = Offset(endX, y),
                strokeWidth = size.minDimension * 0.04f
            )
        }
    }
}

// Helper function to draw a cloud shape
private fun DrawScope.drawCloud(
    center: Offset,
    scale: Float,
    color: Color = Color.White
) {
    val baseRadius = size.minDimension * 0.12f * scale
    
    // Main cloud body (larger circle)
    drawCircle(
        color = color,
        radius = baseRadius,
        center = center
    )
    
    // Left bump
    drawCircle(
        color = color,
        radius = baseRadius * 0.8f,
        center = Offset(center.x - baseRadius * 0.7f, center.y)
    )
    
    // Right bump
    drawCircle(
        color = color,
        radius = baseRadius * 0.8f,
        center = Offset(center.x + baseRadius * 0.7f, center.y)
    )
    
    // Top bump
    drawCircle(
        color = color,
        radius = baseRadius * 0.9f,
        center = Offset(center.x, center.y - baseRadius * 0.5f)
    )
}

// Helper function to draw a snowflake
private fun DrawScope.drawSnowflake(
    center: Offset,
    size: Float,
    color: Color
) {
    // Draw 6 lines radiating from center
    for (i in 0 until 6) {
        val angle = (i * 60.0) * PI / 180.0
        val endX = center.x + (size * cos(angle)).toFloat()
        val endY = center.y + (size * sin(angle)).toFloat()
        
        drawLine(
            color = color,
            start = center,
            end = Offset(endX, endY),
            strokeWidth = size * 0.1f
        )
    }
} 