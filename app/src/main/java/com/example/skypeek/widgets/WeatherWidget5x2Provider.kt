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

class WeatherWidget5x2Provider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        try {
            appWidgetIds.forEach { appWidgetId ->
                updateWidget5x2(context, appWidgetManager, appWidgetId)
            }
        } catch (e: Exception) {
            android.util.Log.e("WeatherWidget5x2Provider", "Error in onUpdate", e)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        try {
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
        } catch (e: Exception) {
            android.util.Log.e("WeatherWidget5x2Provider", "Error in onReceive", e)
        }
    }
    
    private fun updateWidget5x2(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        try {
            // Immediately show fallback data so widget is never stuck in loading state
            showFallbackWidget5x2(context, appWidgetManager, appWidgetId)
            
            // Then try to get real data via service
            try {
                val serviceIntent = Intent(context, WeatherWidgetService::class.java).apply {
                    action = WeatherWidgetService.ACTION_UPDATE_SINGLE_WIDGET
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                    putExtra(WeatherWidgetService.EXTRA_WIDGET_TYPE, WeatherWidgetService.WIDGET_TYPE_5X2)
                }
                context.startService(serviceIntent)
                // Note: If service succeeds, it will update the widget with real data
            } catch (e: Exception) {
                android.util.Log.e("WeatherWidget5x2Provider", "Error starting service for widget $appWidgetId", e)
                // Fallback data is already shown, so nothing more to do
            }
        } catch (e: Exception) {
            android.util.Log.e("WeatherWidget5x2Provider", "Critical error in updateWidget5x2 for widget $appWidgetId", e)
            // Show basic error state
            try {
                val views = RemoteViews(context.packageName, R.layout.weather_widget_5x2)
                views.setTextViewText(R.id.widget_city_name, "Error")
                views.setTextViewText(R.id.widget_temperature, "--°")
                views.setTextViewText(R.id.widget_condition, "Widget failed")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (fallbackError: Exception) {
                android.util.Log.e("WeatherWidget5x2Provider", "Even fallback failed", fallbackError)
            }
        }
    }
    
    private fun showFallbackWidget5x2(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        try {
            val views = RemoteViews(context.packageName, R.layout.weather_widget_5x2)
            
            views.apply {
                setTextViewText(R.id.widget_city_name, "San Jose")
                setTextViewText(R.id.widget_temperature, "72°")
                setTextViewText(R.id.widget_condition, "Partly Cloudy")
                setTextViewText(R.id.widget_high_low, "H:78° L:65°")
                setTextViewText(R.id.widget_feels_like, "Feels like 74°")
                setTextViewText(R.id.widget_last_updated, "Offline mode")
                setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_partly_cloudy)
                
                // Show fallback hourly data
                setTextViewText(R.id.widget_hour1_time, "Now")
                setTextViewText(R.id.widget_hour1_temp, "72°")
                setImageViewResource(R.id.widget_hour1_icon, R.drawable.ic_partly_cloudy)
                
                setTextViewText(R.id.widget_hour2_time, "1 PM")
                setTextViewText(R.id.widget_hour2_temp, "74°")
                setImageViewResource(R.id.widget_hour2_icon, R.drawable.ic_sunny)
                
                setTextViewText(R.id.widget_hour3_time, "2 PM")
                setTextViewText(R.id.widget_hour3_temp, "76°")
                setImageViewResource(R.id.widget_hour3_icon, R.drawable.ic_sunny)
                
                setTextViewText(R.id.widget_hour4_time, "3 PM")
                setTextViewText(R.id.widget_hour4_temp, "77°")
                setImageViewResource(R.id.widget_hour4_icon, R.drawable.ic_sunny)
                
                setTextViewText(R.id.widget_hour5_time, "4 PM")
                setTextViewText(R.id.widget_hour5_temp, "76°")
                setImageViewResource(R.id.widget_hour5_icon, R.drawable.ic_partly_cloudy)
                
                setTextViewText(R.id.widget_hour6_time, "5 PM")
                setTextViewText(R.id.widget_hour6_temp, "74°")
                setImageViewResource(R.id.widget_hour6_icon, R.drawable.ic_partly_cloudy)
                
                // Tomorrow's forecast
                setImageViewResource(R.id.widget_tomorrow_icon, R.drawable.ic_sunny)
                setTextViewText(R.id.widget_tomorrow_high, "80°")
                setTextViewText(R.id.widget_tomorrow_low, "62°")
            }
            
            setupWidget5x2ClickHandlers(context, views)
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            android.util.Log.e("WeatherWidget5x2Provider", "Error in showFallbackWidget5x2 for widget $appWidgetId", e)
            // Try ultra-minimal fallback
            try {
                val minimalViews = RemoteViews(context.packageName, R.layout.weather_widget_5x2)
                minimalViews.setTextViewText(R.id.widget_city_name, "Error")
                minimalViews.setTextViewText(R.id.widget_temperature, "--°")
                appWidgetManager.updateAppWidget(appWidgetId, minimalViews)
            } catch (minimalError: Exception) {
                android.util.Log.e("WeatherWidget5x2Provider", "Even minimal fallback failed", minimalError)
            }
        }
    }
    
    private fun setupWidget5x2ClickHandlers(context: Context, views: RemoteViews) {
        try {
            // Main widget click - open app
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val mainPendingIntent = PendingIntent.getActivity(
                context, 
                0, 
                mainIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
            
            // Hourly forecast section click
            val hourlyIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_screen", "hourly")
            }
            val hourlyPendingIntent = PendingIntent.getActivity(
                context, 
                1, 
                hourlyIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
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
            val forecastIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("open_screen", "forecast")
            }
            val forecastPendingIntent = PendingIntent.getActivity(
                context, 
                2, 
                forecastIntent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            
            listOf(R.id.widget_tomorrow_icon, R.id.widget_tomorrow_high, R.id.widget_tomorrow_low).forEach { viewId ->
                views.setOnClickPendingIntent(viewId, forecastPendingIntent)
            }
        } catch (e: Exception) {
            android.util.Log.e("WeatherWidget5x2Provider", "Error setting up click handlers", e)
            // Continue without click handlers rather than failing the widget
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