package com.example.skypeek.presentation.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// iOS-style Typography matching the Weather app exactly
val Typography = Typography(
    // Main temperature display - 96sp, Ultra Light
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W100, // Ultra Light
        fontSize = 96.sp,
        lineHeight = 86.sp // 0.9 line height
    ),
    
    // City name - 36sp, Light
    displayMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 36.sp,
        lineHeight = 40.sp
    ),
    
    // Weather condition - 24sp, Light
    displaySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 24.sp,
        lineHeight = 28.sp
    ),
    
    // High/Low temp - 20sp, Light
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    
    // Daily forecast day names - 20sp, Normal
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    
    // Weather description - 18sp, Regular
    headlineSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 18.sp,
        lineHeight = 25.sp // 1.4 line height
    ),
    
    // Hourly forecast temperature - 20sp, Normal
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 20.sp,
        lineHeight = 24.sp
    ),
    
    // Section headers - 15sp, Medium
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500, // Medium
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    
    // Hourly time labels - 15sp, Medium
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500, // Medium
        fontSize = 15.sp,
        lineHeight = 18.sp
    ),
    
    // MY LOCATION label - 15sp, Medium, All Caps
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500, // Medium
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    ),
    
    // Secondary labels - 12sp, Regular
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 12.sp,
        lineHeight = 16.sp
    ),
    
    // Small labels - 11sp, Regular
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 11.sp,
        lineHeight = 14.sp
    ),
    
    // Body text - 16sp, Regular
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    
    // Medium body text - 14sp, Regular
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    
    // Small body text - 12sp, Regular
    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 12.sp,
        lineHeight = 16.sp
    )
)

// Custom text styles for specific weather app elements
object WeatherTextStyles {
    
    // Main temperature - exactly like iOS
    val MainTemperature = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W100, // Ultra Light
        fontSize = 96.sp,
        lineHeight = 86.sp
    )
    
    // City name - exactly like iOS
    val CityName = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 36.sp,
        lineHeight = 40.sp
    )
    
    // Current location label - exactly like iOS
    val LocationLabel = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500, // Medium
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
    
    // Weather condition - exactly like iOS
    val WeatherCondition = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 24.sp,
        lineHeight = 28.sp
    )
    
    // High/Low temperatures - exactly like iOS
    val HighLowTemp = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
    
    // Weather description paragraph - exactly like iOS
    val WeatherDescription = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 18.sp,
        lineHeight = 25.sp
    )
    
    // Section headers (10-DAY FORECAST) - exactly like iOS
    val SectionHeader = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500, // Medium
        fontSize = 15.sp,
        lineHeight = 18.sp,
        letterSpacing = 0.5.sp
    )
    
    // Hourly forecast time - exactly like iOS
    val HourlyTime = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500, // Medium
        fontSize = 15.sp,
        lineHeight = 18.sp
    )
    
    // Hourly forecast temperature - exactly like iOS
    val HourlyTemp = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
    
    // Daily forecast day name - exactly like iOS
    val DailyDay = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
    
    // Daily forecast low temperature - exactly like iOS
    val DailyLowTemp = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300, // Light
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
    
    // Daily forecast high temperature - exactly like iOS
    val DailyHighTemp = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400, // Regular
        fontSize = 20.sp,
        lineHeight = 24.sp
    )
} 