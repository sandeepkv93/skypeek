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

class WeatherWidget5x1Provider : AppWidgetProvider() {
    
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
            setTextViewText(R.id.widget_condition, "Updating...")
            setTextViewText(R.id.widget_high_low, "H:--° L:--°")
            setImageViewResource(R.id.widget_weather_icon, R.drawable.ic_cloudy)
        }
        
        // Set up click handlers
        setupWidget5x1ClickHandlers(context, views)
        
        // Update widget with loading state first
        appWidgetManager.updateAppWidget(appWidgetId, views)
        
        // Start service to fetch real weather data
        val serviceIntent = Intent(context, WeatherWidgetService::class.java).apply {
            action = WeatherWidgetService.ACTION_UPDATE_SINGLE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(WeatherWidgetService.EXTRA_WIDGET_TYPE, WeatherWidgetService.WIDGET_TYPE_5X1)
        }
        context.startService(serviceIntent)
    }
    
    private fun setupWidget5x1ClickHandlers(context: Context, views: RemoteViews) {
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
        
        // Hourly forecast click - open app to hourly screen
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
            R.id.widget_hour3_time, R.id.widget_hour3_icon, R.id.widget_hour3_temp
        ).forEach { viewId ->
            views.setOnClickPendingIntent(viewId, hourlyPendingIntent)
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