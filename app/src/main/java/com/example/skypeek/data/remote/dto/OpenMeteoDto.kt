package com.example.skypeek.data.remote.dto

import com.google.gson.annotations.SerializedName

// Main response from Open-Meteo API
data class OpenMeteoResponse(
    val latitude: Double,
    val longitude: Double,
    @SerializedName("elevation")
    val elevation: Double,
    @SerializedName("timezone")
    val timezone: String,
    @SerializedName("timezone_abbreviation")
    val timezoneAbbreviation: String,
    @SerializedName("current")
    val current: OpenMeteoCurrentWeather,
    @SerializedName("hourly")
    val hourly: OpenMeteoHourlyForecast,
    @SerializedName("daily")
    val daily: OpenMeteoDailyForecast
)

data class OpenMeteoCurrentWeather(
    @SerializedName("time")
    val time: String,
    @SerializedName("temperature_2m")
    val temperature2m: Double,
    @SerializedName("weather_code")
    val weatherCode: Int,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: Double,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: Int,
    @SerializedName("apparent_temperature")
    val apparentTemperature: Double,
    @SerializedName("surface_pressure")
    val surfacePressure: Double? = null,
    @SerializedName("visibility")
    val visibility: Double? = null
)

data class OpenMeteoHourlyForecast(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m")
    val temperature2m: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("wind_speed_10m")
    val windSpeed10m: List<Double>,
    @SerializedName("relative_humidity_2m")
    val relativeHumidity2m: List<Int>,
    @SerializedName("visibility")
    val visibility: List<Double>? = null
)

data class OpenMeteoDailyForecast(
    @SerializedName("time")
    val time: List<String>,
    @SerializedName("temperature_2m_max")
    val temperature2mMax: List<Double>,
    @SerializedName("temperature_2m_min")
    val temperature2mMin: List<Double>,
    @SerializedName("weather_code")
    val weatherCode: List<Int>,
    @SerializedName("wind_speed_10m_max")
    val windSpeed10mMax: List<Double>,
    @SerializedName("sunrise")
    val sunrise: List<String>? = null,
    @SerializedName("sunset")
    val sunset: List<String>? = null
) 