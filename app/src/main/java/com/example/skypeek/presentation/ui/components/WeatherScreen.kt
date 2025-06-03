package com.example.skypeek.presentation.ui.components

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
        modifier = modifier.fillMaxSize()
    ) {
        // Dynamic weather background
        WeatherBackground(
            weatherType = weatherData.currentWeather.backgroundType,
            modifier = Modifier.fillMaxSize()
        )
        
        // Main content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 25.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(60.dp)) // Status bar space
            
            // Location Header
            LocationHeader(
                cityName = weatherData.location.cityName,
                isCurrentLocation = weatherData.location.isCurrentLocation
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Main Temperature Display
            MainTemperatureDisplay(
                temperature = weatherData.currentWeather.temperature,
                condition = weatherData.currentWeather.condition,
                highTemp = weatherData.currentWeather.highTemp,
                lowTemp = weatherData.currentWeather.lowTemp
            )
            
            Spacer(modifier = Modifier.height(30.dp))
            
            // Weather Description
            WeatherDescription(
                description = weatherData.currentWeather.description
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // Hourly Forecast
            HourlyForecastSection(
                hourlyForecast = weatherData.hourlyForecast
            )
            
            Spacer(modifier = Modifier.height(40.dp))
            
            // 10-Day Forecast
            TenDayForecastSection(
                dailyForecast = weatherData.dailyForecast
            )
            
            Spacer(modifier = Modifier.height(120.dp)) // Bottom navigation space
        }
        
        // Bottom Navigation
        WeatherBottomNavigation(
            onMapClick = onMapClick,
            onMenuClick = onMenuClick,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
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
    lowTemp: Int
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
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
private fun WeatherDescription(
    description: String
) {
    Text(
        text = description,
        style = WeatherTextStyles.WeatherDescription,
        color = WeatherColors.WeatherTextSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.padding(horizontal = 20.dp)
    )
}

@Composable
private fun HourlyForecastSection(
    hourlyForecast: List<HourlyWeather>
) {
    Column {
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(20.dp),
            contentPadding = PaddingValues(horizontal = 0.dp)
        ) {
            items(hourlyForecast.take(24)) { hour ->
                HourlyForecastItem(hour = hour)
            }
        }
    }
}

@Composable
private fun HourlyForecastItem(
    hour: HourlyWeather
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
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Weather icon (emoji)
        Text(
            text = hour.icon,
            style = WeatherTextStyles.HourlyTemp.copy(fontSize = WeatherTextStyles.HourlyTemp.fontSize * 1.5f),
            modifier = Modifier.height(35.dp)
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Temperature
        Text(
            text = "${hour.temperature}°",
            style = WeatherTextStyles.HourlyTemp,
            color = WeatherColors.WeatherTextPrimary
        )
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
        
        // Weather icon
        Text(
            text = day.icon,
            style = WeatherTextStyles.DailyDay.copy(fontSize = WeatherTextStyles.DailyDay.fontSize * 1.4f),
            modifier = Modifier.width(40.dp),
            textAlign = TextAlign.Center
        )
        
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
private fun WeatherBottomNavigation(
    onMapClick: () -> Unit,
    onMenuClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 30.dp, vertical = 30.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Map button
        IconButton(onClick = onMapClick) {
            Icon(
                imageVector = Icons.Default.LocationOn,
                contentDescription = "Map",
                tint = WeatherColors.WeatherTextTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
        
        // Location dots (placeholder - will be handled by pager)
        LocationDots(currentIndex = 0, total = 1)
        
        // Menu button
        IconButton(onClick = onMenuClick) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Menu",
                tint = WeatherColors.WeatherTextTertiary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun LocationDots(
    currentIndex: Int,
    total: Int
) {
    if (total > 1) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(total) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(
                            color = if (index == currentIndex)
                                WeatherColors.WeatherTextPrimary
                            else
                                WeatherColors.WeatherTextQuaternary,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )
            }
        }
    }
} 