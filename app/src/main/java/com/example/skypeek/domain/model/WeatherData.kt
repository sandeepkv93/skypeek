package com.example.skypeek.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WeatherData(
    val location: LocationData,
    val currentWeather: CurrentWeather,
    val hourlyForecast: List<HourlyWeather>,
    val dailyForecast: List<DailyWeather>,
    val lastUpdated: Long
) : Parcelable

@Parcelize
data class LocationData(
    val latitude: Double,
    val longitude: Double,
    val cityName: String,
    val country: String,
    val isCurrentLocation: Boolean = false
) : Parcelable

@Parcelize
data class CurrentWeather(
    val temperature: Int,
    val condition: String,
    val weatherCode: Int,
    val highTemp: Int,
    val lowTemp: Int,
    val feelsLike: Int,
    val humidity: Int,
    val windSpeed: Double,
    val icon: String,
    val backgroundType: WeatherType,
    val description: String,
    val visibility: Double? = null,
    val uvIndex: Int? = null,
    val pressure: Double? = null,
    val sunrise: String? = null,
    val sunset: String? = null
) : Parcelable

@Parcelize
data class HourlyWeather(
    val time: String,
    val temperature: Int,
    val weatherCode: Int,
    val icon: String,
    val humidity: Int,
    val windSpeed: Double,
    val timestamp: Long
) : Parcelable

@Parcelize
data class DailyWeather(
    val date: String,
    val dayName: String,
    val highTemp: Int,
    val lowTemp: Int,
    val weatherCode: Int,
    val icon: String,
    val condition: String,
    val humidity: Int? = null,
    val windSpeed: Double? = null
) : Parcelable

enum class WeatherType {
    SUNNY, CLOUDY, RAINY, SNOW, STORMY, FOGGY
}

data class WeatherInfo(
    val description: String,
    val icon: String,
    val type: WeatherType
) 