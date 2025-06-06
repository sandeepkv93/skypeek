package com.example.skypeek.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Normalized database schema for weather data
 * Replaces JSON blob approach with proper relational structure
 */

@Entity(tableName = "weather_data_v2")
data class WeatherDataEntity(
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
    val sunrise: String? = null,
    val sunset: String? = null,
    val lastUpdated: Long
)

@Entity(
    tableName = "hourly_forecast",
    foreignKeys = [ForeignKey(
        entity = WeatherDataEntity::class,
        parentColumns = ["id"],
        childColumns = ["weatherDataId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["weatherDataId", "time"])]
)
data class HourlyForecastEntity(
    @PrimaryKey 
    val id: String, // Generated from "${weatherDataId}_${time}"
    val weatherDataId: String,
    val time: String, // ISO format: "2023-12-01T15:00"
    val timestamp: Long, // Unix timestamp for easy sorting
    val temperature: Int,
    val weatherCode: Int,
    val windSpeed: Double,
    val windDirection: Int,
    val humidity: Int,
    val pressure: Double? = null,
    val precipitation: Double = 0.0,
    val precipitationProbability: Int = 0,
    val visibility: Double? = null,
    val uvIndex: Int? = null,
    val feelsLike: Int? = null
)

@Entity(
    tableName = "daily_forecast",
    foreignKeys = [ForeignKey(
        entity = WeatherDataEntity::class,
        parentColumns = ["id"],
        childColumns = ["weatherDataId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index(value = ["weatherDataId", "date"])]
)
data class DailyForecastEntity(
    @PrimaryKey 
    val id: String, // Generated from "${weatherDataId}_${date}"
    val weatherDataId: String,
    val date: String, // ISO format: "2023-12-01"
    val timestamp: Long, // Unix timestamp for easy sorting
    val highTemp: Int,
    val lowTemp: Int,
    val weatherCode: Int,
    val condition: String,
    val windSpeed: Double,
    val windDirection: Int,
    val humidity: Int,
    val precipitation: Double = 0.0,
    val precipitationProbability: Int = 0,
    val sunrise: String? = null,
    val sunset: String? = null,
    val uvIndex: Int? = null,
    val pressure: Double? = null
)

/**
 * Entity for weather alerts and warnings
 */
@Entity(
    tableName = "weather_alerts",
    indices = [Index(value = ["locationId", "startTime"])]
)
data class WeatherAlertEntity(
    @PrimaryKey 
    val id: String,
    val locationId: String, // References weather_data_v2.id
    val title: String,
    val description: String,
    val severity: String, // "minor", "moderate", "severe", "extreme"
    val startTime: Long,
    val endTime: Long,
    val source: String, // "nws", "environment_canada", etc.
    val event: String, // "tornado", "flood", "heat", etc.
    val areas: String, // Affected areas as comma-separated string
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

/**
 * Enhanced location entity with additional metadata
 */
@Entity(tableName = "saved_locations_v2")
data class SavedLocationEntityV2(
    @PrimaryKey 
    val id: String, // Generated from "${latitude}_${longitude}"
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val country: String,
    val state: String? = null, // State/province
    val timezone: String? = null, // Timezone identifier
    val isCurrentLocation: Boolean = false,
    val order: Int, // For ordering locations in the UI
    val lastWeatherUpdate: Long? = null,
    val addedAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true // Soft delete support
)

/**
 * Cache metadata for intelligent cache management
 */
@Entity(tableName = "cache_metadata")
data class CacheMetadataEntity(
    @PrimaryKey 
    val key: String, // Cache key identifier
    val lastUpdated: Long,
    val expiresAt: Long,
    val dataType: String, // "weather", "forecast", "location"
    val source: String, // "open_meteo", "weather_api", etc.
    val requestParams: String, // JSON of request parameters for cache invalidation
    val isValid: Boolean = true
)