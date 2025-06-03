package com.example.skypeek.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.skypeek.R
import com.example.skypeek.presentation.MainActivity
import com.example.skypeek.domain.repository.WeatherRepository
import com.example.skypeek.domain.model.WeatherData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WeatherWidget5x2Provider : AppWidgetProvider() {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget5x2(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_WIDGET_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, WeatherWidget5x2Provider::class.java)
                val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
                onUpdate(context, appWidgetManager, appWidgetIds)
            }
            ACTION_WIDGET_CLICK -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                }
                context.startActivity(launchIntent)
            }
            ACTION_HOURLY_CLICK -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("open_screen", "hourly")
                }
                context.startActivity(launchIntent)
            }
            ACTION_FORECAST_CLICK -> {
                val launchIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("open_screen", "forecast")
                }
                context.startActivity(launchIntent)
            }
        }
    }
    
    private fun updateWidget5x2(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_5x2)
        
        // Show loading state initially
        views.apply {
            setTextViewText(R.id.widget_city_name, "Loading...")
            setTextViewText(R.id.widget_temperature, "--°")
            setTextViewText(R.id.widget_condition, "Getting location...")
            setTextViewText(R.id.widget_last_updated, "Updating...")
            setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_cloudy)
        }
        
        // Update widget immediately with loading state
        appWidgetManager.updateAppWidget(appWidgetId, views)
        
        // Fetch weather data asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val location = getCurrentLocation(context)
                if (location != null) {
                    val weatherResult = weatherRepository.getWeatherData(
                        location.first,
                        location.second
                    )
                    
                    weatherResult.fold(
                        onSuccess = { weather ->
                            updateWidget5x2WithData(context, appWidgetManager, appWidgetId, weather)
                        },
                        onFailure = { error ->
                            showWidget5x2Error(context, appWidgetManager, appWidgetId, error.message ?: "Failed to load weather")
                        }
                    )
                } else {
                    showWidget5x2Error(context, appWidgetManager, appWidgetId, "Location not available")
                }
            } catch (e: Exception) {
                showWidget5x2Error(context, appWidgetManager, appWidgetId, "Network error")
            }
        }
        
        // Set up click handlers
        setupWidget5x2ClickHandlers(context, views)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun updateWidget5x2WithData(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weather: WeatherData
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_5x2)
        
        views.apply {
            // Current weather information
            setTextViewText(R.id.widget_city_name, weather.location.cityName)
            setTextViewText(R.id.widget_temperature, "${weather.currentWeather.temperature}°")
            setTextViewText(R.id.widget_condition, weather.currentWeather.condition)
            setTextViewText(
                R.id.widget_high_low,
                "H:${weather.currentWeather.highTemp}° L:${weather.currentWeather.lowTemp}°"
            )
            setTextViewText(R.id.widget_feels_like, "Feels like ${weather.currentWeather.temperature + 2}°")
            setTextViewText(R.id.widget_last_updated, "Updated ${getTimeAgo(System.currentTimeMillis())}")
            setImageViewResource(R.id.widget_weather_icon, getWeatherIconResource(weather.currentWeather.weatherCode))
            
            // 6-hour forecast
            val hourlyForecast = weather.hourlyForecast.take(6)
            val hourlyViews = listOf(
                Triple(R.id.widget_hour1_time, R.id.widget_hour1_icon, R.id.widget_hour1_temp),
                Triple(R.id.widget_hour2_time, R.id.widget_hour2_icon, R.id.widget_hour2_temp),
                Triple(R.id.widget_hour3_time, R.id.widget_hour3_icon, R.id.widget_hour3_temp),
                Triple(R.id.widget_hour4_time, R.id.widget_hour4_icon, R.id.widget_hour4_temp),
                Triple(R.id.widget_hour5_time, R.id.widget_hour5_icon, R.id.widget_hour5_temp),
                Triple(R.id.widget_hour6_time, R.id.widget_hour6_icon, R.id.widget_hour6_temp)
            )
            
            hourlyForecast.forEachIndexed { index, hour ->
                if (index < hourlyViews.size) {
                    val (timeId, iconId, tempId) = hourlyViews[index]
                    setTextViewText(timeId, if (index == 0) "Now" else hour.time)
                    setImageViewResource(iconId, getWeatherIconResource(hour.weatherCode))
                    setTextViewText(tempId, "${hour.temperature}°")
                }
            }
            
            // Tomorrow's forecast (use daily forecast data if available)
            if (weather.dailyForecast.isNotEmpty()) {
                val tomorrow = weather.dailyForecast.first()
                setImageViewResource(R.id.widget_tomorrow_icon, getWeatherIconResource(tomorrow.weatherCode))
                setTextViewText(R.id.widget_tomorrow_high, "${tomorrow.highTemp}°")
                setTextViewText(R.id.widget_tomorrow_low, "${tomorrow.lowTemp}°")
            } else {
                // Fallback to estimated values
                setImageViewResource(R.id.widget_tomorrow_icon, R.drawable.ic_sunny)
                setTextViewText(R.id.widget_tomorrow_high, "${weather.currentWeather.temperature + 3}°")
                setTextViewText(R.id.widget_tomorrow_low, "${weather.currentWeather.temperature - 5}°")
            }
        }
        
        setupWidget5x2ClickHandlers(context, views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun showWidget5x2Error(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        message: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_5x2)
        views.apply {
            setTextViewText(R.id.widget_city_name, "Error")
            setTextViewText(R.id.widget_temperature, "--°")
            setTextViewText(R.id.widget_condition, message)
            setTextViewText(R.id.widget_last_updated, "Tap to retry")
            setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_cloudy)
        }
        
        setupWidget5x2ClickHandlers(context, views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        // For now, return a default location (San Jose, CA)
        // In a production app, you'd implement proper location services
        return Pair(37.3382, -121.8863)
    }
    
    private fun getTimeAgo(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diff = now - timestamp
        val minutes = diff / (1000 * 60)
        
        return when {
            minutes < 1 -> "just now"
            minutes < 60 -> "${minutes}m ago"
            else -> "${minutes / 60}h ago"
        }
    }
    
    private fun setupWidget5x2ClickHandlers(context: Context, views: RemoteViews) {
        // Main widget click - open app
        val mainIntent = Intent(context, WeatherWidget5x2Provider::class.java).apply {
            action = ACTION_WIDGET_CLICK
        }
        val mainPendingIntent = PendingIntent.getBroadcast(
            context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
        
        // Hourly forecast section click
        val hourlyIntent = Intent(context, WeatherWidget5x2Provider::class.java).apply {
            action = ACTION_HOURLY_CLICK
        }
        val hourlyPendingIntent = PendingIntent.getBroadcast(
            context, 1, hourlyIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Set click listeners for hourly forecast sections
        listOf(
            R.id.widget_hour1_time, R.id.widget_hour1_icon, R.id.widget_hour1_temp,
            R.id.widget_hour2_time, R.id.widget_hour2_icon, R.id.widget_hour2_temp,
            R.id.widget_hour3_time, R.id.widget_hour3_icon, R.id.widget_hour3_temp,
            R.id.widget_hour4_time, R.id.widget_hour4_icon, R.id.widget_hour4_temp,
            R.id.widget_hour5_time, R.id.widget_hour5_icon, R.id.widget_hour5_temp,
            R.id.widget_hour6_time, R.id.widget_hour6_icon, R.id.widget_hour6_temp
        ).forEach { viewId ->
            views.setOnClickPendingIntent(viewId, hourlyPendingIntent)
        }
        
        // Tomorrow forecast section click
        val forecastIntent = Intent(context, WeatherWidget5x2Provider::class.java).apply {
            action = ACTION_FORECAST_CLICK
        }
        val forecastPendingIntent = PendingIntent.getBroadcast(
            context, 2, forecastIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        listOf(R.id.widget_tomorrow_icon, R.id.widget_tomorrow_high, R.id.widget_tomorrow_low).forEach { viewId ->
            views.setOnClickPendingIntent(viewId, forecastPendingIntent)
        }
    }
    
    private fun getWeatherIconResource(weatherCode: Int): Int {
        return when (weatherCode) {
            0 -> R.drawable.ic_sunny
            1, 2, 3 -> R.drawable.ic_partly_cloudy
            45, 48 -> R.drawable.ic_fog
            51, 53, 55, 56, 57, 61, 63, 65, 66, 67, 80, 81, 82 -> R.drawable.ic_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.ic_snow
            95, 96, 99 -> R.drawable.ic_thunderstorm
            else -> R.drawable.ic_cloudy
        }
    }
    
    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.skypeek.WIDGET_5X2_REFRESH"
        const val ACTION_WIDGET_CLICK = "com.example.skypeek.WIDGET_5X2_CLICK"
        const val ACTION_HOURLY_CLICK = "com.example.skypeek.WIDGET_5X2_HOURLY_CLICK"
        const val ACTION_FORECAST_CLICK = "com.example.skypeek.WIDGET_5X2_FORECAST_CLICK"
        
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WeatherWidget5x2Provider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
} 