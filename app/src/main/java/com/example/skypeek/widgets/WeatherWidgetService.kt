package com.example.skypeek.widgets

import android.app.IntentService
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.skypeek.R
import com.example.skypeek.domain.model.WeatherData
import com.example.skypeek.domain.model.WeatherType
import com.example.skypeek.domain.repository.WeatherRepository
import com.example.skypeek.utils.WeatherCodeMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WeatherWidgetService : IntentService("WeatherWidgetService") {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    override fun onHandleIntent(intent: Intent?) {
        when (intent?.action) {
            ACTION_UPDATE_WIDGETS -> updateAllWidgets()
            ACTION_UPDATE_SINGLE_WIDGET -> {
                val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                val widgetType = intent.getStringExtra(EXTRA_WIDGET_TYPE)
                if (appWidgetId != -1 && widgetType != null) {
                    updateSingleWidget(appWidgetId, widgetType)
                }
            }
        }
    }
    
    private fun updateAllWidgets() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        
        // Update 4x1 widgets
        val widget4x1Ids = appWidgetManager.getAppWidgetIds(
            ComponentName(this, WeatherWidget4x1Provider::class.java)
        )
        widget4x1Ids.forEach { widgetId ->
            updateSingleWidget(widgetId, WIDGET_TYPE_4X1)
        }
        
        // Update 4x2 widgets
        val widget4x2Ids = appWidgetManager.getAppWidgetIds(
            ComponentName(this, WeatherWidget4x2Provider::class.java)
        )
        widget4x2Ids.forEach { widgetId ->
            updateSingleWidget(widgetId, WIDGET_TYPE_4X2)
        }
    }
    
    private fun updateSingleWidget(appWidgetId: Int, widgetType: String) {
        runBlocking {
            try {
                val location = getWidgetLocation(appWidgetId)
                val weatherResult = weatherRepository.getWeatherData(
                    location.latitude,
                    location.longitude
                )
                
                weatherResult.fold(
                    onSuccess = { weather ->
                        when (widgetType) {
                            WIDGET_TYPE_4X1 -> updateWidget4x1(appWidgetId, weather)
                            WIDGET_TYPE_4X2 -> updateWidget4x2(appWidgetId, weather)
                        }
                    },
                    onFailure = { error ->
                        showWidgetError(appWidgetId, widgetType, error.message ?: "Error loading weather")
                    }
                )
            } catch (e: Exception) {
                showWidgetError(appWidgetId, widgetType, "Failed to update widget")
            }
        }
    }
    
    private fun updateWidget4x1(appWidgetId: Int, weather: WeatherData) {
        val views = RemoteViews(packageName, R.layout.weather_widget_4x1)
        
        views.apply {
            setTextViewText(R.id.widget_city_name, weather.location.cityName)
            setTextViewText(R.id.widget_temperature, "${weather.currentWeather.temperature}°")
            setTextViewText(R.id.widget_condition, weather.currentWeather.condition)
            setImageViewResource(R.id.widget_weather_icon, getWeatherIconResource(weather.currentWeather.weatherCode))
        }
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun updateWidget4x2(appWidgetId: Int, weather: WeatherData) {
        val views = RemoteViews(packageName, R.layout.weather_widget_4x2)
        
        views.apply {
            setTextViewText(R.id.widget_city_name, weather.location.cityName)
            setTextViewText(R.id.widget_temperature, "${weather.currentWeather.temperature}°")
            setTextViewText(R.id.widget_condition, weather.currentWeather.condition)
            setTextViewText(
                R.id.widget_high_low,
                "H:${weather.currentWeather.highTemp}° L:${weather.currentWeather.lowTemp}°"
            )
            setImageViewResource(R.id.widget_weather_icon, getWeatherIconResource(weather.currentWeather.weatherCode))
            
            // Update hourly forecast
            updateWidget4x2Hourly(this, weather.hourlyForecast.take(4))
        }
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun updateWidget4x2Hourly(views: RemoteViews, hourlyForecast: List<com.example.skypeek.domain.model.HourlyWeather>) {
        val hourlyViews = listOf(
            Triple(R.id.widget_hour1_time, R.id.widget_hour1_icon, R.id.widget_hour1_temp),
            Triple(R.id.widget_hour2_time, R.id.widget_hour2_icon, R.id.widget_hour2_temp),
            Triple(R.id.widget_hour3_time, R.id.widget_hour3_icon, R.id.widget_hour3_temp),
            Triple(R.id.widget_hour4_time, R.id.widget_hour4_icon, R.id.widget_hour4_temp)
        )
        
        hourlyForecast.forEachIndexed { index, hour ->
            if (index < hourlyViews.size) {
                val (timeId, iconId, tempId) = hourlyViews[index]
                views.setTextViewText(timeId, hour.time)
                views.setImageViewResource(iconId, getWeatherIconResource(hour.weatherCode))
                views.setTextViewText(tempId, "${hour.temperature}°")
            }
        }
    }
    
    private fun setWidgetBackground(views: RemoteViews, weatherType: WeatherType) {
        // Background is already set in the XML layout
        // We can implement dynamic background changes later if needed
    }
    
    private fun getWeatherIconResource(weatherCode: Int): Int {
        return when (weatherCode) {
            0 -> R.drawable.ic_sunny
            1, 2, 3 -> R.drawable.ic_partly_cloudy
            45, 48 -> R.drawable.ic_fog
            51, 53, 55, 61, 63, 65 -> R.drawable.ic_rain
            71, 73, 75, 77, 85, 86 -> R.drawable.ic_snow
            95, 96, 99 -> R.drawable.ic_thunderstorm
            else -> R.drawable.ic_cloudy
        }
    }
    
    private fun getCurrentTimeString(): String {
        val formatter = SimpleDateFormat("EEEE, h:mm a", Locale.getDefault())
        return formatter.format(Date())
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
    
    private fun getWidgetLocation(appWidgetId: Int): com.example.skypeek.domain.model.LocationData {
        // Get location from widget configuration or use default
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val latitude = prefs.getFloat("lat_$appWidgetId", 37.33f).toDouble()
        val longitude = prefs.getFloat("lon_$appWidgetId", -122.03f).toDouble()
        val cityName = prefs.getString("city_$appWidgetId", "Cupertino") ?: "Cupertino"
        
        return com.example.skypeek.domain.model.LocationData(
            latitude = latitude,
            longitude = longitude,
            cityName = cityName,
            country = "",
            isCurrentLocation = false
        )
    }
    
    private fun showWidgetError(appWidgetId: Int, widgetType: String, message: String) {
        val layoutId = when (widgetType) {
            WIDGET_TYPE_4X1 -> R.layout.weather_widget_4x1
            WIDGET_TYPE_4X2 -> R.layout.weather_widget_4x2
            else -> return
        }
        
        val views = RemoteViews(packageName, layoutId)
        views.setTextViewText(R.id.widget_city_name, "Error")
        views.setTextViewText(R.id.widget_temperature, "--°")
        views.setTextViewText(R.id.widget_condition, message)
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    companion object {
        const val ACTION_UPDATE_WIDGETS = "com.example.skypeek.UPDATE_WIDGETS"
        const val ACTION_UPDATE_SINGLE_WIDGET = "com.example.skypeek.UPDATE_SINGLE_WIDGET"
        const val EXTRA_WIDGET_TYPE = "widget_type"
        const val WIDGET_TYPE_4X1 = "4x1"
        const val WIDGET_TYPE_4X2 = "4x2"
    }
} 