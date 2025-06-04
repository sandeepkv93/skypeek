package com.example.skypeek.widgets

import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.widget.RemoteViews
import com.example.skypeek.R
import com.example.skypeek.domain.model.WeatherData
import com.example.skypeek.domain.model.WeatherType
import com.example.skypeek.domain.repository.WeatherRepository
import com.example.skypeek.domain.repository.LocationRepository
import com.example.skypeek.utils.WeatherCodeMapper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class WeatherWidgetService : Service() {
    
    @Inject
    lateinit var weatherRepository: WeatherRepository
    
    @Inject
    lateinit var locationRepository: LocationRepository
    
    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_NOT_STICKY
    }
    
    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }
    
    private fun handleIntent(intent: Intent) {
        serviceScope.launch {
            try {
                when (intent.action) {
                    ACTION_UPDATE_WIDGETS -> updateAllWidgets()
                    ACTION_UPDATE_SINGLE_WIDGET -> {
                        val appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        val widgetType = intent.getStringExtra(EXTRA_WIDGET_TYPE)
                        if (appWidgetId != -1 && widgetType != null) {
                            updateSingleWidget(appWidgetId, widgetType)
                        }
                    }
                    ACTION_UPDATE_LOCATION -> {
                        val latitude = intent.getDoubleExtra(EXTRA_LATITUDE, 0.0)
                        val longitude = intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                        val cityName = intent.getStringExtra(EXTRA_CITY_NAME) ?: "Current Location"
                        
                        if (latitude != 0.0 && longitude != 0.0) {
                            val newLocation = com.example.skypeek.domain.model.LocationData(
                                latitude = latitude,
                                longitude = longitude,
                                cityName = cityName,
                                country = "",
                                isCurrentLocation = true
                            )
                            updateAllWidgetsLocation(newLocation)
                        }
                    }
                    ACTION_RESET_TO_CURRENT_LOCATION -> {
                        resetAllWidgetsToCurrentLocation()
                        updateAllWidgets() // Refresh widgets with new location settings
                    }
                }
            } catch (e: Exception) {
                // Log error and stop service
                stopSelf()
            } finally {
                stopSelf()
            }
        }
    }
    
    private suspend fun updateAllWidgets() {
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
        
        // Update 5x1 widgets
        val widget5x1Ids = appWidgetManager.getAppWidgetIds(
            ComponentName(this, WeatherWidget5x1Provider::class.java)
        )
        widget5x1Ids.forEach { widgetId ->
            updateSingleWidget(widgetId, WIDGET_TYPE_5X1)
        }
        
        // Update 5x2 widgets
        val widget5x2Ids = appWidgetManager.getAppWidgetIds(
            ComponentName(this, WeatherWidget5x2Provider::class.java)
        )
        widget5x2Ids.forEach { widgetId ->
            updateSingleWidget(widgetId, WIDGET_TYPE_5X2)
        }
    }
    
    private suspend fun updateSingleWidget(appWidgetId: Int, widgetType: String) {
        try {
            val location = getWidgetLocation(appWidgetId)
            val weatherResult = weatherRepository.getWeatherData(
                location.latitude,
                location.longitude
            )
            
            weatherResult.fold(
                onSuccess = { weather ->
                    // Handle "Loading..." city name by reverse geocoding
                    val updatedWeather = if (weather.location.cityName == "Loading...") {
                        val reverseGeocodeResult = locationRepository.reverseGeocode(
                            weather.location.latitude, 
                            weather.location.longitude
                        )
                        reverseGeocodeResult.fold(
                            onSuccess = { geocodedLocation ->
                                weather.copy(
                                    location = weather.location.copy(
                                        cityName = geocodedLocation.cityName,
                                        country = geocodedLocation.country
                                    )
                                )
                            },
                            onFailure = {
                                // Fallback to stored location name or default
                                weather.copy(
                                    location = weather.location.copy(
                                        cityName = location.cityName.takeIf { it.isNotBlank() } ?: "San Jose"
                                    )
                                )
                            }
                        )
                    } else {
                        weather
                    }
                    
                    when (widgetType) {
                        WIDGET_TYPE_4X1 -> updateWidget4x1(appWidgetId, updatedWeather)
                        WIDGET_TYPE_4X2 -> updateWidget4x2(appWidgetId, updatedWeather)
                        WIDGET_TYPE_5X1 -> updateWidget5x1(appWidgetId, updatedWeather)
                        WIDGET_TYPE_5X2 -> updateWidget5x2(appWidgetId, updatedWeather)
                    }
                },
                onFailure = { error ->
                    showWidgetError(appWidgetId, widgetType, error.message ?: "Error loading weather")
                }
            )
        } catch (e: Exception) {
            showWidgetError(appWidgetId, widgetType, "Failed to update widget: ${e.message}")
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
    
    private fun updateWidget5x1(appWidgetId: Int, weather: WeatherData) {
        val views = RemoteViews(packageName, R.layout.weather_widget_5x1)
        
        // Ensure we have a valid city name - handle both blank and "Loading..." cases
        val cityName = when {
            weather.location.cityName.isNotBlank() && weather.location.cityName != "Loading..." -> weather.location.cityName
            weather.location.country.isNotBlank() -> weather.location.country
            else -> "San Jose" // Fallback
        }
        
        views.apply {
            setTextViewText(R.id.widget_city_name, cityName)
            setTextViewText(R.id.widget_temperature, "${weather.currentWeather.temperature}°")
            setTextViewText(R.id.widget_condition, weather.currentWeather.condition)
            setTextViewText(
                R.id.widget_high_low,
                "H:${weather.currentWeather.highTemp}° L:${weather.currentWeather.lowTemp}°"
            )
            setImageViewResource(R.id.widget_weather_icon, getWeatherIconResource(weather.currentWeather.weatherCode))
            
            // Update 3-hour forecast
            updateWidget5x1Hourly(this, weather.hourlyForecast.take(3))
        }
        
        // Set up click handlers
        setupWidget5x1ClickHandlers(views)
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun updateWidget5x1Hourly(views: RemoteViews, hourlyForecast: List<com.example.skypeek.domain.model.HourlyWeather>) {
        val hourlyViews = listOf(
            Triple(R.id.widget_hour1_time, R.id.widget_hour1_icon, R.id.widget_hour1_temp),
            Triple(R.id.widget_hour2_time, R.id.widget_hour2_icon, R.id.widget_hour2_temp),
            Triple(R.id.widget_hour3_time, R.id.widget_hour3_icon, R.id.widget_hour3_temp)
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
    
    private fun updateWidget5x2(appWidgetId: Int, weather: WeatherData) {
        try {
            val views = RemoteViews(packageName, R.layout.weather_widget_5x2)
            
            // Ensure we have a valid city name - handle both blank and "Loading..." cases
            val cityName = when {
                weather.location.cityName.isNotBlank() && weather.location.cityName != "Loading..." -> weather.location.cityName
                weather.location.country.isNotBlank() -> weather.location.country
                else -> "San Jose" // Fallback
            }
            
            views.apply {
                setTextViewText(R.id.widget_city_name, cityName)
                setTextViewText(R.id.widget_temperature, "${weather.currentWeather.temperature}°")
                setTextViewText(R.id.widget_condition, weather.currentWeather.condition)
                setTextViewText(
                    R.id.widget_high_low,
                    "H:${weather.currentWeather.highTemp}° L:${weather.currentWeather.lowTemp}°"
                )
                setTextViewText(R.id.widget_feels_like, "Feels like ${weather.currentWeather.feelsLike}°")
                setTextViewText(R.id.widget_last_updated, "Updated ${getTimeAgo(weather.lastUpdated)}")
                setImageViewResource(R.id.widget_weather_icon, getWeatherIconResource(weather.currentWeather.weatherCode))
                
                // Update 6-hour forecast
                updateWidget5x2Hourly(this, weather.hourlyForecast.take(6))
                
                // Update tomorrow's forecast
                if (weather.dailyForecast.isNotEmpty()) {
                    val tomorrow = weather.dailyForecast.first()
                    setImageViewResource(R.id.widget_tomorrow_icon, getWeatherIconResource(tomorrow.weatherCode))
                    setTextViewText(R.id.widget_tomorrow_high, "${tomorrow.highTemp}°")
                    setTextViewText(R.id.widget_tomorrow_low, "${tomorrow.lowTemp}°")
                }
            }
            
            // Set up click handlers
            setupWidget5x2ClickHandlers(views)
            
            val appWidgetManager = AppWidgetManager.getInstance(this)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            // Log error and show error state
            android.util.Log.e("WeatherWidget5x2", "Error updating widget $appWidgetId", e)
            showWidgetError(appWidgetId, WIDGET_TYPE_5X2, "Widget update failed: ${e.message}")
        }
    }
    
    private fun updateWidget5x2Hourly(views: RemoteViews, hourlyForecast: List<com.example.skypeek.domain.model.HourlyWeather>) {
        try {
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
                    views.setTextViewText(timeId, hour.time)
                    views.setImageViewResource(iconId, getWeatherIconResource(hour.weatherCode))
                    views.setTextViewText(tempId, "${hour.temperature}°")
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("WeatherWidget5x2", "Error updating hourly forecast", e)
            // Continue without crashing the widget
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
    
    private suspend fun getWidgetLocation(appWidgetId: Int): com.example.skypeek.domain.model.LocationData {
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        
        // Check if this widget is set to use current location
        val isCurrentLocation = prefs.getBoolean("is_current_$appWidgetId", true) // Default to true for new widgets
        
        if (isCurrentLocation) {
            // Always try to get current location for widgets marked as "current location"
            try {
                val currentLocationResult = locationRepository.getCurrentLocation()
                currentLocationResult.fold(
                    onSuccess = { currentLocation ->
                        // Save/update current location for this widget
                        prefs.edit().apply {
                            putFloat("lat_$appWidgetId", currentLocation.latitude.toFloat())
                            putFloat("lon_$appWidgetId", currentLocation.longitude.toFloat())
                            putString("city_$appWidgetId", currentLocation.cityName)
                            putBoolean("is_current_$appWidgetId", true)
                            apply()
                        }
                        return currentLocation.copy(isCurrentLocation = true)
                    },
                    onFailure = {
                        // Failed to get current location, use saved coordinates if available
                        val savedLatitude = prefs.getFloat("lat_$appWidgetId", Float.NaN)
                        val savedLongitude = prefs.getFloat("lon_$appWidgetId", Float.NaN)
                        
                        if (!savedLatitude.isNaN() && !savedLongitude.isNaN()) {
                            val cityName = prefs.getString("city_$appWidgetId", "Current Location") ?: "Current Location"
                            return com.example.skypeek.domain.model.LocationData(
                                latitude = savedLatitude.toDouble(),
                                longitude = savedLongitude.toDouble(),
                                cityName = cityName,
                                country = "",
                                isCurrentLocation = true
                            )
                        } else {
                            // No saved coordinates either, fall back to default
                            val defaultLocation = com.example.skypeek.domain.model.LocationData(
                                latitude = 37.3382,
                                longitude = -121.8863,
                                cityName = "San Jose",
                                country = "US",
                                isCurrentLocation = false
                            )
                            prefs.edit().apply {
                                putFloat("lat_$appWidgetId", defaultLocation.latitude.toFloat())
                                putFloat("lon_$appWidgetId", defaultLocation.longitude.toFloat())
                                putString("city_$appWidgetId", defaultLocation.cityName)
                                putBoolean("is_current_$appWidgetId", false)
                                apply()
                            }
                            return defaultLocation
                        }
                    }
                )
            } catch (e: Exception) {
                // Error getting current location, use saved or default
                val savedLatitude = prefs.getFloat("lat_$appWidgetId", 37.3382f)
                val savedLongitude = prefs.getFloat("lon_$appWidgetId", -121.8863f)
                val cityName = prefs.getString("city_$appWidgetId", "San Jose") ?: "San Jose"
                
                return com.example.skypeek.domain.model.LocationData(
                    latitude = savedLatitude.toDouble(),
                    longitude = savedLongitude.toDouble(),
                    cityName = cityName,
                    country = "",
                    isCurrentLocation = false
                )
            }
        } else {
            // Widget is set to use a specific saved location
            val savedLatitude = prefs.getFloat("lat_$appWidgetId", 37.3382f)
            val savedLongitude = prefs.getFloat("lon_$appWidgetId", -121.8863f)
            val cityName = prefs.getString("city_$appWidgetId", "San Jose") ?: "San Jose"
            
            return com.example.skypeek.domain.model.LocationData(
                latitude = savedLatitude.toDouble(),
                longitude = savedLongitude.toDouble(),
                cityName = cityName,
                country = "",
                isCurrentLocation = false
            )
        }
    }
    
    private fun showWidgetError(appWidgetId: Int, widgetType: String, message: String) {
        val layoutId = when (widgetType) {
            WIDGET_TYPE_4X1 -> R.layout.weather_widget_4x1
            WIDGET_TYPE_4X2 -> R.layout.weather_widget_4x2
            WIDGET_TYPE_5X1 -> R.layout.weather_widget_5x1
            WIDGET_TYPE_5X2 -> R.layout.weather_widget_5x2
            else -> return
        }
        
        val views = RemoteViews(packageName, layoutId)
        views.setTextViewText(R.id.widget_city_name, "Error")
        views.setTextViewText(R.id.widget_temperature, "--°")
        views.setTextViewText(R.id.widget_condition, message)
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun setupWidget5x1ClickHandlers(views: RemoteViews) {
        // Main widget click - open app
        val mainIntent = Intent(this, com.example.skypeek.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = android.app.PendingIntent.getActivity(
            this, 
            0, 
            mainIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
        
        // Hourly forecast click - open app to hourly screen
        val hourlyIntent = Intent(this, com.example.skypeek.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "hourly")
        }
        val hourlyPendingIntent = android.app.PendingIntent.getActivity(
            this, 
            1, 
            hourlyIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
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
    
    private fun setupWidget5x2ClickHandlers(views: RemoteViews) {
        // Main widget click - open app
        val mainIntent = Intent(this, com.example.skypeek.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val mainPendingIntent = android.app.PendingIntent.getActivity(
            this, 
            0, 
            mainIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
        
        // Hourly forecast section click
        val hourlyIntent = Intent(this, com.example.skypeek.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "hourly")
        }
        val hourlyPendingIntent = android.app.PendingIntent.getActivity(
            this, 
            1, 
            hourlyIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
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
        val forecastIntent = Intent(this, com.example.skypeek.presentation.MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("open_screen", "forecast")
        }
        val forecastPendingIntent = android.app.PendingIntent.getActivity(
            this, 
            2, 
            forecastIntent, 
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        listOf(R.id.widget_tomorrow_icon, R.id.widget_tomorrow_high, R.id.widget_tomorrow_low).forEach { viewId ->
            views.setOnClickPendingIntent(viewId, forecastPendingIntent)
        }
    }
    
    private suspend fun updateAllWidgetsLocation(newLocation: com.example.skypeek.domain.model.LocationData) {
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        
        // Update all widget types that are using current location
        val allWidgetProviders = listOf(
            WeatherWidget4x1Provider::class.java,
            WeatherWidget4x2Provider::class.java,
            WeatherWidget5x1Provider::class.java,
            WeatherWidget5x2Provider::class.java
        )
        
        allWidgetProviders.forEach { providerClass ->
            val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(this, providerClass))
            
            widgetIds.forEach { widgetId ->
                val isCurrentLocation = prefs.getBoolean("is_current_$widgetId", false)
                
                if (isCurrentLocation) {
                    // Update this widget to use the new current location
                    editor.apply {
                        putFloat("lat_$widgetId", newLocation.latitude.toFloat())
                        putFloat("lon_$widgetId", newLocation.longitude.toFloat())
                        putString("city_$widgetId", newLocation.cityName)
                    }
                    
                    // Determine widget type and update
                    val widgetType = when (providerClass.simpleName) {
                        "WeatherWidget4x1Provider" -> WIDGET_TYPE_4X1
                        "WeatherWidget4x2Provider" -> WIDGET_TYPE_4X2
                        "WeatherWidget5x1Provider" -> WIDGET_TYPE_5X1
                        "WeatherWidget5x2Provider" -> WIDGET_TYPE_5X2
                        else -> null
                    }
                    
                    if (widgetType != null) {
                        updateSingleWidget(widgetId, widgetType)
                    }
                }
            }
        }
        
        editor.apply()
    }
    
    /**
     * Reset all widgets to use current location
     */
    private fun resetAllWidgetsToCurrentLocation() {
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        
        val appWidgetManager = AppWidgetManager.getInstance(this)
        val allWidgetProviders = listOf(
            WeatherWidget4x1Provider::class.java,
            WeatherWidget4x2Provider::class.java,
            WeatherWidget5x1Provider::class.java,
            WeatherWidget5x2Provider::class.java
        )
        
        allWidgetProviders.forEach { providerClass ->
            val widgetIds = appWidgetManager.getAppWidgetIds(ComponentName(this, providerClass))
            widgetIds.forEach { widgetId ->
                // Mark widget to use current location
                editor.putBoolean("is_current_$widgetId", true)
                // Clear saved coordinates so it will fetch fresh
                editor.remove("lat_$widgetId")
                editor.remove("lon_$widgetId")
                editor.remove("city_$widgetId")
            }
        }
        
        editor.apply()
    }
    
    companion object {
        const val ACTION_UPDATE_WIDGETS = "com.example.skypeek.UPDATE_WIDGETS"
        const val ACTION_UPDATE_SINGLE_WIDGET = "com.example.skypeek.UPDATE_SINGLE_WIDGET"
        const val ACTION_UPDATE_LOCATION = "com.example.skypeek.UPDATE_LOCATION"
        const val ACTION_RESET_TO_CURRENT_LOCATION = "com.example.skypeek.RESET_TO_CURRENT_LOCATION"
        const val EXTRA_WIDGET_TYPE = "widget_type"
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
        const val EXTRA_CITY_NAME = "city_name"
        const val WIDGET_TYPE_4X1 = "4x1"
        const val WIDGET_TYPE_4X2 = "4x2"
        const val WIDGET_TYPE_5X1 = "5x1"
        const val WIDGET_TYPE_5X2 = "5x2"
        
        /**
         * Update all widgets with fresh data
         */
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WeatherWidgetService::class.java).apply {
                action = ACTION_UPDATE_WIDGETS
            }
            context.startService(intent)
        }
        
        /**
         * Update all widgets that use current location when the location changes
         */
        fun updateWidgetLocation(context: Context, latitude: Double, longitude: Double, cityName: String) {
            val intent = Intent(context, WeatherWidgetService::class.java).apply {
                action = ACTION_UPDATE_LOCATION
                putExtra(EXTRA_LATITUDE, latitude)
                putExtra(EXTRA_LONGITUDE, longitude)
                putExtra(EXTRA_CITY_NAME, cityName)
            }
            context.startService(intent)
        }
        
        /**
         * Reset all widgets to use current location instead of saved coordinates
         */
        fun resetWidgetsToCurrentLocation(context: Context) {
            val intent = Intent(context, WeatherWidgetService::class.java).apply {
                action = ACTION_RESET_TO_CURRENT_LOCATION
            }
            context.startService(intent)
        }
    }
} 