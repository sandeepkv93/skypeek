package com.example.skypeek.domain.repository

import com.example.skypeek.domain.model.WeatherData

interface WeatherRepository {
    
    /**
     * Get weather data for specific coordinates
     * Uses multi-API strategy with caching fallback
     */
    suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean = false
    ): Result<WeatherData>
    
    /**
     * Clear old cached weather data
     */
    suspend fun clearCache()
} 