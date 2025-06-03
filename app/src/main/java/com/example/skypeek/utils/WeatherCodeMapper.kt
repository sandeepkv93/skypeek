package com.example.skypeek.utils

import com.example.skypeek.domain.model.WeatherInfo
import com.example.skypeek.domain.model.WeatherType

object WeatherCodeMapper {
    
    /**
     * Maps Open-Meteo weather codes to WeatherInfo
     * Open-Meteo codes: https://open-meteo.com/en/docs
     */
    fun mapOpenMeteoCode(code: Int): WeatherInfo {
        return when (code) {
            0 -> WeatherInfo("Clear sky", "☀️", WeatherType.SUNNY)
            1 -> WeatherInfo("Mainly clear", "🌤️", WeatherType.SUNNY)
            2 -> WeatherInfo("Partly cloudy", "⛅", WeatherType.CLOUDY)
            3 -> WeatherInfo("Overcast", "☁️", WeatherType.CLOUDY)
            45, 48 -> WeatherInfo("Fog", "🌫️", WeatherType.FOGGY)
            51 -> WeatherInfo("Light drizzle", "🌦️", WeatherType.RAINY)
            53 -> WeatherInfo("Moderate drizzle", "🌦️", WeatherType.RAINY)
            55 -> WeatherInfo("Dense drizzle", "🌧️", WeatherType.RAINY)
            56, 57 -> WeatherInfo("Freezing drizzle", "🌨️", WeatherType.RAINY)
            61 -> WeatherInfo("Slight rain", "🌧️", WeatherType.RAINY)
            63 -> WeatherInfo("Moderate rain", "🌧️", WeatherType.RAINY)
            65 -> WeatherInfo("Heavy rain", "⛈️", WeatherType.RAINY)
            66, 67 -> WeatherInfo("Freezing rain", "🌨️", WeatherType.RAINY)
            71 -> WeatherInfo("Slight snow", "🌨️", WeatherType.SNOW)
            73 -> WeatherInfo("Moderate snow", "❄️", WeatherType.SNOW)
            75 -> WeatherInfo("Heavy snow", "❄️", WeatherType.SNOW)
            77 -> WeatherInfo("Snow grains", "❄️", WeatherType.SNOW)
            80 -> WeatherInfo("Slight rain showers", "🌦️", WeatherType.RAINY)
            81 -> WeatherInfo("Moderate rain showers", "🌧️", WeatherType.RAINY)
            82 -> WeatherInfo("Violent rain showers", "⛈️", WeatherType.RAINY)
            85 -> WeatherInfo("Slight snow showers", "🌨️", WeatherType.SNOW)
            86 -> WeatherInfo("Heavy snow showers", "❄️", WeatherType.SNOW)
            95 -> WeatherInfo("Thunderstorm", "⛈️", WeatherType.STORMY)
            96 -> WeatherInfo("Thunderstorm with hail", "⛈️", WeatherType.STORMY)
            99 -> WeatherInfo("Thunderstorm with heavy hail", "⛈️", WeatherType.STORMY)
            else -> WeatherInfo("Unknown", "❓", WeatherType.CLOUDY)
        }
    }
    
    /**
     * Maps WeatherAPI.com weather codes to WeatherInfo
     */
    fun mapWeatherAPICode(code: Int, isDay: Boolean = true): WeatherInfo {
        return when (code) {
            1000 -> if (isDay) WeatherInfo("Sunny", "☀️", WeatherType.SUNNY) 
                    else WeatherInfo("Clear", "🌙", WeatherType.SUNNY)
            1003 -> WeatherInfo("Partly cloudy", "⛅", WeatherType.CLOUDY)
            1006 -> WeatherInfo("Cloudy", "☁️", WeatherType.CLOUDY)
            1009 -> WeatherInfo("Overcast", "☁️", WeatherType.CLOUDY)
            1030, 1135, 1147 -> WeatherInfo("Fog", "🌫️", WeatherType.FOGGY)
            1063, 1180, 1183, 1186, 1189, 1192, 1195 -> WeatherInfo("Rain", "🌧️", WeatherType.RAINY)
            1066, 1210, 1213, 1216, 1219, 1222, 1225 -> WeatherInfo("Snow", "❄️", WeatherType.SNOW)
            1087, 1273, 1276, 1279, 1282 -> WeatherInfo("Thunderstorm", "⛈️", WeatherType.STORMY)
            else -> WeatherInfo("Unknown", "❓", WeatherType.CLOUDY)
        }
    }
    
    /**
     * Maps OpenWeatherMap weather codes to WeatherInfo
     */
    fun mapOpenWeatherMapCode(code: Int): WeatherInfo {
        return when (code) {
            in 200..299 -> WeatherInfo("Thunderstorm", "⛈️", WeatherType.STORMY)
            in 300..399 -> WeatherInfo("Drizzle", "🌦️", WeatherType.RAINY)
            in 500..599 -> WeatherInfo("Rain", "🌧️", WeatherType.RAINY)
            in 600..699 -> WeatherInfo("Snow", "❄️", WeatherType.SNOW)
            in 700..799 -> WeatherInfo("Fog", "🌫️", WeatherType.FOGGY)
            800 -> WeatherInfo("Clear sky", "☀️", WeatherType.SUNNY)
            in 801..804 -> WeatherInfo("Cloudy", "☁️", WeatherType.CLOUDY)
            else -> WeatherInfo("Unknown", "❓", WeatherType.CLOUDY)
        }
    }
    
    /**
     * Generates contextual weather descriptions based on weather info
     */
    fun generateWeatherDescription(
        weatherInfo: WeatherInfo, 
        currentTemp: Int, 
        windSpeed: Double,
        timeOfDay: String = "day"
    ): String {
        val windDesc = when {
            windSpeed < 5 -> "Light winds"
            windSpeed < 15 -> "Moderate winds"
            windSpeed < 25 -> "Strong winds"
            else -> "Very strong winds"
        }
        
        return when (weatherInfo.type) {
            WeatherType.SUNNY -> {
                if (timeOfDay == "day") {
                    "Sunny conditions will continue all day. $windDesc are up to ${windSpeed.toInt()} km/h."
                } else {
                    "Clear skies tonight with comfortable conditions. $windDesc expected."
                }
            }
            WeatherType.CLOUDY -> {
                "Cloudy conditions from afternoon, with partly cloudy evening expected. $windDesc with gusts up to ${windSpeed.toInt()} km/h."
            }
            WeatherType.RAINY -> {
                "Rainy conditions tonight, continuing through the morning. $windDesc with gusts and possible thunderstorms."
            }
            WeatherType.SNOW -> {
                "Windy conditions with heavy snow expected throughout the day. Travel may be affected by poor visibility."
            }
            WeatherType.STORMY -> {
                "Severe weather conditions with thunderstorms and strong winds. Stay indoors and avoid travel if possible."
            }
            WeatherType.FOGGY -> {
                "Foggy conditions reducing visibility, clearing by afternoon. Drive carefully and use headlights."
            }
        }
    }
    
    /**
     * Gets appropriate background gradient colors based on weather type
     */
    fun getBackgroundColors(weatherType: WeatherType): List<Long> {
        return when (weatherType) {
            WeatherType.SUNNY -> listOf(0xFF87CEEB, 0xFF4A90E2) // Light blue to deeper blue
            WeatherType.CLOUDY -> listOf(0xFFDDA0DD, 0xFF9370DB, 0xFF663399) // Plum to purple
            WeatherType.RAINY -> listOf(0xFF4A4A4A, 0xFF2F2F2F, 0xFF1A1A1A) // Dark grays
            WeatherType.SNOW -> listOf(0xFFB0C4DE, 0xFF778899, 0xFF556B8D) // Light steel blue
            WeatherType.STORMY -> listOf(0xFF2F2F2F, 0xFF1A1A1A, 0xFF000000) // Very dark
            WeatherType.FOGGY -> listOf(0xFF696969, 0xFF484848, 0xFF2F2F2F) // Gray tones
        }
    }
} 