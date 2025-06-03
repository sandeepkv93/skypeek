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

class WeatherWidget4x2Provider : AppWidgetProvider() {
    
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        appWidgetIds.forEach { appWidgetId ->
            updateWidget4x2(context, appWidgetManager, appWidgetId)
        }
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_WIDGET_REFRESH -> {
                val appWidgetManager = AppWidgetManager.getInstance(context)
                val componentName = ComponentName(context, WeatherWidget4x2Provider::class.java)
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
    
    private fun updateWidget4x2(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.weather_widget_4x2)
        
        // Start the widget service to update data
        val serviceIntent = Intent(context, WeatherWidgetService::class.java).apply {
            action = WeatherWidgetService.ACTION_UPDATE_SINGLE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(WeatherWidgetService.EXTRA_WIDGET_TYPE, WeatherWidgetService.WIDGET_TYPE_4X2)
        }
        context.startService(serviceIntent)
        
        // Set up click handlers
        setupWidget4x2ClickHandlers(context, views)
        
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
    
    private fun setupWidget4x2ClickHandlers(context: Context, views: RemoteViews) {
        // Main widget click - open app
        val mainIntent = Intent(context, WeatherWidget4x2Provider::class.java).apply {
            action = ACTION_WIDGET_CLICK
        }
        val mainPendingIntent = PendingIntent.getBroadcast(
            context, 0, mainIntent, PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_root, mainPendingIntent)
    }
    
    companion object {
        const val ACTION_WIDGET_REFRESH = "com.example.skypeek.WIDGET_4X2_REFRESH"
        const val ACTION_WIDGET_CLICK = "com.example.skypeek.WIDGET_4X2_CLICK"
        const val ACTION_HOURLY_CLICK = "com.example.skypeek.WIDGET_4X2_HOURLY_CLICK"
        
        fun updateAllWidgets(context: Context) {
            val intent = Intent(context, WeatherWidget4x2Provider::class.java).apply {
                action = ACTION_WIDGET_REFRESH
            }
            context.sendBroadcast(intent)
        }
    }
} 