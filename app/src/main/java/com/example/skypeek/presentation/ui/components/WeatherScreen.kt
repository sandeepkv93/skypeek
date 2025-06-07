package com.example.skypeek.presentation.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.skypeek.domain.model.DailyWeather
import com.example.skypeek.domain.model.HourlyWeather
import com.example.skypeek.domain.model.WeatherData
import com.example.skypeek.presentation.ui.theme.WeatherColors
import com.example.skypeek.presentation.ui.theme.WeatherTextStyles

@Composable
fun WeatherScreen(
    weatherData: WeatherData,
    onRefresh: () -> Unit,
    onMapClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                androidx.compose.ui.graphics.Brush.verticalGradient(
                    colors = WeatherColors.SunnyGradient
                )
            )
    ) {
        // Main content with scrollable weather information - positioned first (behind navigation)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 25.dp)
                .padding(top = 120.dp), // Account for top navigation
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            LocationHeader(
                cityName = weatherData.location.cityName,
                isCurrentLocation = weatherData.location.isCurrentLocation
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            MainTemperatureDisplay(
                temperature = weatherData.currentWeather.temperature,
                condition = weatherData.currentWeather.condition,
                highTemp = weatherData.currentWeather.highTemp,
                lowTemp = weatherData.currentWeather.lowTemp,
                weatherCode = weatherData.currentWeather.weatherCode,
                sunrise = weatherData.currentWeather.sunrise,
                sunset = weatherData.currentWeather.sunset
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            WeatherDetailsRow(
                feelsLike = weatherData.currentWeather.feelsLike.toFloat(),
                humidity = weatherData.currentWeather.humidity,
                windSpeed = weatherData.currentWeather.windSpeed.toFloat()
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Sunrise/Sunset section
            if (weatherData.currentWeather.sunrise != null && weatherData.currentWeather.sunset != null) {
                SunriseSunsetSection(
                    sunrise = weatherData.currentWeather.sunrise!!,
                    sunset = weatherData.currentWeather.sunset!!
                )
                
                Spacer(modifier = Modifier.height(30.dp))
            }
            
            Spacer(modifier = Modifier.height(40.dp))
            
            HourlyForecastSection(
                hourlyForecast = weatherData.hourlyForecast.take(24),
                sunriseTime = weatherData.currentWeather.sunrise,
                sunsetTime = weatherData.currentWeather.sunset
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            TenDayForecastSection(
                dailyForecast = weatherData.dailyForecast
            )
            
            Spacer(modifier = Modifier.height(40.dp)) // Bottom padding
        }
        
        // Top Navigation Bar - positioned last (on top) with elevated clickable area
        Row(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 50.dp) // Increased top padding for status bar
                .background(
                    androidx.compose.ui.graphics.Brush.verticalGradient(
                        colors = listOf(
                            WeatherColors.SunnyGradient[0].copy(alpha = 0.8f),
                            androidx.compose.ui.graphics.Color.Transparent
                        )
                    )
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu button (top left) - increased touch target
            IconButton(
                onClick = onMenuClick,
                modifier = Modifier
                    .size(48.dp) // Larger touch target
            ) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = WeatherColors.WeatherTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            // Map button (top right) - increased touch target
            IconButton(
                onClick = onMapClick,
                modifier = Modifier
                    .size(48.dp) // Larger touch target
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "Map",
                    tint = WeatherColors.WeatherTextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun LocationHeader(
    cityName: String,
    isCurrentLocation: Boolean
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isCurrentLocation) {
            Text(
                text = "MY LOCATION",
                style = WeatherTextStyles.LocationLabel,
                color = WeatherColors.WeatherTextTertiary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        Text(
            text = cityName,
            style = WeatherTextStyles.CityName,
            color = WeatherColors.WeatherTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun MainTemperatureDisplay(
    temperature: Int,
    condition: String,
    highTemp: Int,
    lowTemp: Int,
    weatherCode: Int,
    sunrise: String?,
    sunset: String?
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large weather icon - prominent display like iOS
        WeatherIcon(
            weatherCode = weatherCode,
            size = 120.dp,
            timestamp = System.currentTimeMillis(), // Use current time for proper night detection
            sunrise = sunrise,
            sunset = sunset,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        
        // Main temperature - must dominate the screen
        Text(
            text = "${temperature}°",
            style = WeatherTextStyles.MainTemperature,
            color = WeatherColors.WeatherTextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Weather condition
        Text(
            text = condition,
            style = WeatherTextStyles.WeatherCondition,
            color = WeatherColors.WeatherTextPrimary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // High/Low temperatures
        Text(
            text = "H:${highTemp}° L:${lowTemp}°",
            style = WeatherTextStyles.HighLowTemp,
            color = WeatherColors.WeatherTextSecondary
        )
    }
}

@Composable
private fun WeatherDetailsRow(
    feelsLike: Float,
    humidity: Int,
    windSpeed: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        WeatherDetailItem(
            title = "FEELS LIKE",
            value = "${feelsLike.toInt()}°"
        )
        
        WeatherDetailItem(
            title = "HUMIDITY",
            value = "${humidity}%"
        )
        
        WeatherDetailItem(
            title = "WIND",
            value = "${windSpeed.toInt()} km/h"
        )
    }
}

@Composable
private fun WeatherDetailItem(
    title: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = WeatherTextStyles.SectionHeader,
            color = WeatherColors.WeatherTextTertiary
        )
        
        Spacer(modifier = Modifier.height(4.dp))
        
        Text(
            text = value,
            style = WeatherTextStyles.WeatherCondition,
            color = WeatherColors.WeatherTextPrimary
        )
    }
}

@Composable
private fun HourlyForecastSection(
    hourlyForecast: List<HourlyWeather>,
    sunriseTime: String?,
    sunsetTime: String?
) {
    Column {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(hourlyForecast.take(24)) { hour ->
                HourlyForecastItem(hour = hour, sunriseTime = sunriseTime, sunsetTime = sunsetTime)
            }
        }
    }
}

@Composable
private fun HourlyForecastItem(
    hour: HourlyWeather,
    sunriseTime: String?,
    sunsetTime: String?,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(60.dp)
    ) {
        // Time
        Text(
            text = hour.time,
            style = WeatherTextStyles.HourlyTime,
            color = WeatherColors.WeatherTextTertiary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Check if this hour matches sunrise or sunset time
        val isSunriseHour = sunriseTime?.let { sunrise ->
            // Extract hour from sunrise time (e.g., "6:45 AM" -> "6AM" or "6:45AM")
            val sunriseHour = extractHourFromTime(sunrise)
            hour.time == sunriseHour || hour.time.startsWith(sunriseHour.take(1))
        } ?: false
        
        val isSunsetHour = sunsetTime?.let { sunset ->
            // Extract hour from sunset time (e.g., "7:23 PM" -> "7PM" or "7:23PM")
            val sunsetHour = extractHourFromTime(sunset)
            hour.time == sunsetHour || hour.time.startsWith(sunsetHour.take(1))
        } ?: false
        
        // Show sunrise/sunset indicator or weather icon
        when {
            isSunriseHour -> {
                Text(
                    text = "🌅",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.height(35.dp)
                )
            }
            isSunsetHour -> {
                Text(
                    text = "🌇",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.height(35.dp)
                )
            }
            else -> {
                // Weather icon (custom iOS-style icon)
                WeatherIcon(
                    weatherCode = hour.weatherCode,
                    size = 30.dp,
                    timestamp = hour.timestamp,
                    sunrise = sunriseTime,
                    sunset = sunsetTime,
                    modifier = Modifier.height(35.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Temperature
        Text(
            text = "${hour.temperature}°",
            style = WeatherTextStyles.HourlyTemp,
            color = WeatherColors.WeatherTextPrimary
        )
    }
}

// Helper function to extract hour from time string like "6:45 AM" -> "6AM"
private fun extractHourFromTime(timeString: String): String {
    return try {
        val parts = timeString.split(":")
        if (parts.size >= 2) {
            val hour = parts[0]
            val amPm = if (timeString.contains("AM")) "AM" else "PM"
            "${hour}${amPm}"
        } else {
            timeString
        }
    } catch (e: Exception) {
        timeString
    }
}

@Composable
private fun TenDayForecastSection(
    dailyForecast: List<DailyWeather>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Section header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 15.dp)
        ) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = null,
                tint = WeatherColors.WeatherTextTertiary,
                modifier = Modifier.size(16.dp)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Text(
                text = "10-DAY FORECAST",
                style = WeatherTextStyles.SectionHeader,
                color = WeatherColors.WeatherTextTertiary
            )
        }
        
        // Daily forecast items
        dailyForecast.take(10).forEach { day ->
            DailyForecastRow(day = day)
        }
    }
}

@Composable
private fun DailyForecastRow(
    day: DailyWeather
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp)
    ) {
        // Day name
        Text(
            text = day.dayName,
            style = WeatherTextStyles.DailyDay,
            color = WeatherColors.WeatherTextPrimary,
            modifier = Modifier.width(80.dp)
        )
        
        // Weather icon - use midday timestamp (12 PM) for daily weather
        Box(
            modifier = Modifier.width(40.dp),
            contentAlignment = Alignment.Center
        ) {
            WeatherIcon(
                weatherCode = day.weatherCode,
                size = 28.dp,
                timestamp = createMiddayTimestamp(), // Use midday for daily forecast icons
                sunrise = null, // Daily forecasts don't need sunset/sunrise logic
                sunset = null
            )
        }
        
        // Low temperature
        Text(
            text = "${day.lowTemp}°",
            style = WeatherTextStyles.DailyLowTemp,
            color = WeatherColors.WeatherTextQuaternary,
            modifier = Modifier.padding(start = 15.dp)
        )
        
        // Temperature range bar
        TemperatureRangeBar(
            lowTemp = day.lowTemp,
            highTemp = day.highTemp,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 10.dp)
        )
        
        // High temperature
        Text(
            text = "${day.highTemp}°",
            style = WeatherTextStyles.DailyHighTemp,
            color = WeatherColors.WeatherTextPrimary,
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.End
        )
    }
}

// Helper function to create midday timestamp for daily weather icons
private fun createMiddayTimestamp(): Long {
    val calendar = java.util.Calendar.getInstance().apply {
        set(java.util.Calendar.HOUR_OF_DAY, 12) // 12 PM midday
        set(java.util.Calendar.MINUTE, 0)
        set(java.util.Calendar.SECOND, 0)
        set(java.util.Calendar.MILLISECOND, 0)
    }
    return calendar.timeInMillis
}

@Composable
private fun TemperatureRangeBar(
    lowTemp: Int,
    highTemp: Int,
    modifier: Modifier = Modifier
) {
    // Calculate progress based on temperature range
    val tempRange = maxOf(highTemp - lowTemp, 1)
    val progress = tempRange / 40f // Normalize to reasonable range
    
    Box(
        modifier = modifier
            .height(4.dp)
            .fillMaxWidth()
    ) {
        // Background bar
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = WeatherColors.WeatherTextQuaternary,
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
        )
        
        // Temperature range indicator
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0.1f, 1f))
                .background(
                    color = when {
                        highTemp >= 30 -> Color(0xFFFF6B35) // Hot
                        highTemp >= 20 -> Color(0xFFFFA500) // Warm
                        highTemp >= 10 -> Color(0xFF4A90E2) // Cool
                        else -> Color(0xFF87CEEB) // Cold
                    },
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp)
                )
        )
    }
}

@Composable
private fun SunriseSunsetSection(
    sunrise: String,
    sunset: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        SunriseSunsetItem(
            title = "SUNRISE",
            time = sunrise,
            icon = "🌅"
        )
        
        SunriseSunsetItem(
            title = "SUNSET", 
            time = sunset,
            icon = "🌇"
        )
    }
}

@Composable
private fun SunriseSunsetItem(
    title: String,
    time: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        Text(
            text = title,
            style = WeatherTextStyles.SectionHeader,
            color = WeatherColors.WeatherTextTertiary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = icon,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        
        Text(
            text = time,
            style = WeatherTextStyles.WeatherCondition,
            color = WeatherColors.WeatherTextPrimary
        )
    }
} 