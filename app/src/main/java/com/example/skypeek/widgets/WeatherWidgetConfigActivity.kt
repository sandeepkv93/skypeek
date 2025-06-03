package com.example.skypeek.widgets

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.skypeek.presentation.ui.theme.SkyPeekTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class WeatherWidgetConfigActivity : ComponentActivity() {
    
    private var appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Set the result to CANCELED. This will cause the widget host to cancel
        // out of the widget placement if the user presses the back button.
        setResult(Activity.RESULT_CANCELED)
        
        // Find the widget id from the intent
        appWidgetId = intent?.extras?.getInt(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            AppWidgetManager.INVALID_APPWIDGET_ID
        ) ?: AppWidgetManager.INVALID_APPWIDGET_ID
        
        // If this activity was started with an intent without an app widget ID, finish with an error.
        if (appWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish()
            return
        }
        
        setContent {
            SkyPeekTheme {
                WidgetConfigScreen(
                    onConfigComplete = { latitude, longitude, cityName ->
                        saveWidgetConfiguration(latitude, longitude, cityName)
                        updateWidget()
                        finishWithResult()
                    },
                    onCancel = {
                        finish()
                    }
                )
            }
        }
    }
    
    private fun saveWidgetConfiguration(latitude: Double, longitude: Double, cityName: String) {
        val prefs = getSharedPreferences("widget_prefs", Context.MODE_PRIVATE)
        prefs.edit().apply {
            putFloat("lat_$appWidgetId", latitude.toFloat())
            putFloat("lon_$appWidgetId", longitude.toFloat())
            putString("city_$appWidgetId", cityName)
            apply()
        }
    }
    
    private fun updateWidget() {
        val appWidgetManager = AppWidgetManager.getInstance(this)
        
        // Trigger widget update
        val updateIntent = Intent(this, WeatherWidgetService::class.java).apply {
            action = WeatherWidgetService.ACTION_UPDATE_SINGLE_WIDGET
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            putExtra(WeatherWidgetService.EXTRA_WIDGET_TYPE, WeatherWidgetService.WIDGET_TYPE_4X1)
        }
        startService(updateIntent)
    }
    
    private fun finishWithResult() {
        val resultValue = Intent().apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
        }
        setResult(Activity.RESULT_OK, resultValue)
        finish()
    }
}

@Composable
private fun WidgetConfigScreen(
    onConfigComplete: (Double, Double, String) -> Unit,
    onCancel: () -> Unit
) {
    val context = LocalContext.current
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Configure Weather Widget",
                    style = MaterialTheme.typography.headlineSmall
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "This widget will show weather for your current location.",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedButton(
                        onClick = onCancel,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    
                    Button(
                        onClick = {
                            // For now, use default location (Cupertino)
                            onConfigComplete(37.33, -122.03, "Cupertino")
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Add Widget")
                    }
                }
            }
        }
    }
} 