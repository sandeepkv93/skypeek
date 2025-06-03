package com.example.skypeek.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.location.LocationManager
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
class WeatherWidget5x1Provider : AppWidgetProvider() {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget5x1(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_WIDGET_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, WeatherWidget5x1Provider::class.java)
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
        }
    }
    
    private fun updateWidget5x1(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_5x1)
        
        // Show loading state initially
        views.apply {
            setTextViewText(R.id.widget_city_name, "Loading...")
            setTextViewText(R.id.widget_temperature, "--°")
            setTextViewText(R.id.widget_condition, "Getting location...")
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
                            updateWidget5x1WithData(context, appWidgetManager, appWidgetId, weather)
                        },
                        onFailure = { error ->
                            showWidget5x1Error(context, appWidgetManager, appWidgetId, error.message ?: "Failed to load weather")
                        }
                    )
                } else {
                    showWidget5x1Error(context, appWidgetManager, appWidgetId, "Location not available")
                }
            } catch (e: Exception) {
                showWidget5x1Error(context, appWidgetManager, appWidgetId, "Network error")
            }
        }
        
        // Set up click handlers
        setupWidget5x1ClickHandlers(context, views)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun updateWidget5x1WithData(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        weather: WeatherData
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_5x1)
        
        views.apply {
            // Current weather information
            setTextViewText(R.id.widget_city_name, weather.location.cityName)
            setTextViewText(R.id.widget_temperature, "${weather.currentWeather.temperature}°")
            setTextViewText(R.id.widget_condition, weather.currentWeather.condition)
            setTextViewText(
                R.id.widget_high_low,
                "H:${weather.currentWeather.highTemp}° L:${weather.currentWeather.lowTemp}°"
            )
            setImageViewResource(R.id.widget_weather_icon, getWeatherIconResource(weather.currentWeather.weatherCode))
            
            // Next 3 hours forecast
            val hourlyForecast = weather.hourlyForecast.take(3)
            val hourlyViews = listOf(
                Triple(R.id.widget_hour1_time, R.id.widget_hour1_icon, R.id.widget_hour1_temp),
                Triple(R.id.widget_hour2_time, R.id.widget_hour2_icon, R.id.widget_hour2_temp),
                Triple(R.id.widget_hour3_time, R.id.widget_hour3_icon, R.id.widget_hour3_temp)
            )
            
            hourlyForecast.forEachIndexed { index, hour ->
                if (index < hourlyViews.size) {
                    val (timeId, iconId, tempId) = hourlyViews[index]
                    setTextViewText(timeId, hour.time)
                    setImageViewResource(iconId, getWeatherIconResource(hour.weatherCode))
                    setTextViewText(tempId, "${hour.temperature}°")
                }
            }
        }
        
        setupWidget5x1ClickHandlers(context, views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun showWidget5x1Error(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        message: String
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_5x1)
        views.apply {
            setTextViewText(R.id.widget_city_name, "Error")
            setTextViewText(R.id.widget_temperature, "--°")
            setTextViewText(R.id.widget_condition, message)
            setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_cloudy)
        }
        
        setupWidget5x1ClickHandlers(context, views)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun getCurrentLocation(context: Context): Pair<Double, Double>? {
        // For now, return a default location (San Jose, CA)
        // In a production app, you'd implement proper location services
        return Pair(37.3382, -121.8863)
    }
    
    private fun setupWidget5x1ClickHandlers(context: Context, views: RemoteViews) {
        // Main widget click - open app
        val mainIntent = Intent(context, WeatherWidget5x1Provider::class.java).apply {
            action = ACTION_WIDGET_CLICK
        }
        val mainPendingIntent = PendingIntent.getBroadcast(
            context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
        
        // Hourly forecast click - open app to hourly screen
        val hourlyIntent = Intent(context, WeatherWidget5x1Provider::class.java).apply {
            action = ACTION_HOURLY_CLICK
        }
        val hourlyPendingIntent = PendingIntent.getBroadcast(
            context, 1, hourlyIntent, PendingIntent.FLAG_IMMUTABLE
        )
        
        // Set click listeners for hourly forecast sections
        listOf(
            R.id.widget_hour1_time, R.id.widget_hour1_icon, R.id.widget_hour1_temp,
            R.id.widget_hour2_time, R.id.widget_hour2_icon, R.id.widget_hour2_temp,
            R.id.widget_hour3_time, R.id.widget_hour3_icon, R.id.widget_hour3_temp
        ).forEach { viewId ->
            views.setOnClickPendingIntent(viewId, hourlyPendingIntent)
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
        const val ACTION_WIDGET_REFRESH = "com.example.skypeek.WIDGET_5X1_REFRESH"
        const val ACTION_WIDGET_CLICK = "com.example.skypeek.WIDGET_5X1_CLICK"
        const val ACTION_HOURLY_CLICK = "com.example.skypeek.WIDGET_5X1_HOURLY_CLICK"
        
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WeatherWidget5x1Provider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
} 