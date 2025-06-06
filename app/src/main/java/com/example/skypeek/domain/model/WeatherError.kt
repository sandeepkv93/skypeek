package com.example.skypeek.domain.model

/**
 * Sealed class hierarchy for specific weather app errors
 * Provides better error handling and user feedback
 */
sealed class WeatherError : Exception() {
    
    // Network-related errors
    sealed class NetworkError : WeatherError() {
        object NoInternet : NetworkError() {
            override val message: String = "No internet connection available"
        }
        
        object RequestTimeout : NetworkError() {
            override val message: String = "Request timed out. Please try again"
        }
        
        data class ServerError(val code: Int) : NetworkError() {
            override val message: String = "Server error (Code: $code). Please try again later"
        }
        
        object UnknownNetworkError : NetworkError() {
            override val message: String = "Network error occurred. Check your connection"
        }
    }
    
    // API-specific errors
    sealed class ApiError : WeatherError() {
        object ApiKeyInvalid : ApiError() {
            override val message: String = "Invalid API key configuration"
        }
        
        object RateLimitExceeded : ApiError() {
            override val message: String = "Too many requests. Please wait a moment"
        }
        
        object LocationNotFound : ApiError() {
            override val message: String = "Weather data not available for this location"
        }
        
        data class InvalidResponse(val details: String) : ApiError() {
            override val message: String = "Invalid weather data received: $details"
        }
        
        object AllApisFailed : ApiError() {
            override val message: String = "Weather services are temporarily unavailable"
        }
    }
    
    // Location-related errors
    sealed class LocationError : WeatherError() {
        object PermissionDenied : LocationError() {
            override val message: String = "Location permission is required for current weather"
        }
        
        object LocationDisabled : LocationError() {
            override val message: String = "Location services are disabled. Please enable them"
        }
        
        object LocationUnavailable : LocationError() {
            override val message: String = "Unable to determine your location. Try again"
        }
        
        object GeocodingFailed : LocationError() {
            override val message: String = "Unable to find weather data for this location"
        }
    }
    
    // Database-related errors
    sealed class DatabaseError : WeatherError() {
        object DatabaseCorrupted : DatabaseError() {
            override val message: String = "Weather data is corrupted. Refreshing..."
        }
        
        object StorageError : DatabaseError() {
            override val message: String = "Unable to save weather data. Check device storage"
        }
        
        object DataMigrationError : DatabaseError() {
            override val message: String = "Updating app data. Please wait..."
        }
    }
    
    // Cache-related errors
    sealed class CacheError : WeatherError() {
        object CacheExpired : CacheError() {
            override val message: String = "Weather data is outdated. Refreshing..."
        }
        
        object CacheCorrupted : CacheError() {
            override val message: String = "Cached data is invalid. Loading fresh data..."
        }
    }
    
    // Widget-specific errors
    sealed class WidgetError : WeatherError() {
        object WidgetDataUnavailable : WidgetError() {
            override val message: String = "Widget data unavailable. Open app to refresh"
        }
        
        object WidgetUpdateFailed : WidgetError() {
            override val message: String = "Failed to update weather widget"
        }
    }
    
    // Generic fallback
    data class UnknownError(override val message: String) : WeatherError()
}

/**
 * Extension functions for error conversion and user-friendly messages
 */

/**
 * Convert generic exceptions to specific WeatherError types
 */
fun Throwable.toWeatherError(): WeatherError {
    return when (this) {
        is WeatherError -> this
        is java.net.UnknownHostException -> WeatherError.NetworkError.NoInternet
        is java.net.SocketTimeoutException -> WeatherError.NetworkError.RequestTimeout
        is java.net.ConnectException -> WeatherError.NetworkError.NoInternet
        is java.io.IOException -> WeatherError.NetworkError.UnknownNetworkError
        is SecurityException -> WeatherError.LocationError.PermissionDenied
        is IllegalStateException -> WeatherError.DatabaseError.StorageError
        else -> WeatherError.UnknownError(message ?: "An unexpected error occurred")
    }
}

/**
 * Get user-friendly error message for display
 */
fun WeatherError.getUserMessage(): String = this.message ?: "An error occurred"

/**
 * Determine if error should trigger a retry
 */
fun WeatherError.isRetryable(): Boolean {
    return when (this) {
        is WeatherError.NetworkError.RequestTimeout,
        is WeatherError.NetworkError.ServerError,
        is WeatherError.NetworkError.UnknownNetworkError,
        is WeatherError.ApiError.RateLimitExceeded,
        is WeatherError.CacheError -> true
        else -> false
    }
}

/**
 * Determine if error should show refresh button
 */
fun WeatherError.shouldShowRefresh(): Boolean {
    return when (this) {
        is WeatherError.NetworkError,
        is WeatherError.ApiError.AllApisFailed,
        is WeatherError.CacheError -> true
        else -> false
    }
}