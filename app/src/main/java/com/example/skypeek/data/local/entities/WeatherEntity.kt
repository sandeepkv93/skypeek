package com.example.skypeek.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.skypeek.domain.model.DailyWeather
import com.example.skypeek.domain.model.HourlyWeather

@Entity(tableName = "weather_data")
data class WeatherEntity(
    @PrimaryKey 
    val id: String, // Generated from "${latitude}_${longitude}"
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val country: String,
    val currentTemp: Int,
    val condition: String,
    val weatherCode: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val feelsLike: Int,
    val humidity: Int,
    val windSpeed: Double,
    val description: String,
    val backgroundType: String, // WeatherType enum as string
    val visibility: Double? = null,
    val uvIndex: Int? = null,
    val pressure: Double? = null,
    val hourlyForecastJson: String, // JSON string of List<HourlyWeather>
    val dailyForecastJson: String, // JSON string of List<DailyWeather>
    val lastUpdated: Long
)

@Entity(tableName = "saved_locations")
data class SavedLocationEntity(
    @PrimaryKey 
    val id: String, // Generated from "${latitude}_${longitude}"
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val country: String,
    val isCurrentLocation: Boolean = false,
    val order: Int, // For ordering locations in the UI
    val addedAt: Long = System.currentTimeMillis()
) 