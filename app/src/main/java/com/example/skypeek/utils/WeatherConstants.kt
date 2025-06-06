package com.example.skypeek.utils

/**
 * Central place for all app constants to eliminate magic numbers
 */
object WeatherConstants {
    
    // Cache durations (in milliseconds)
    object Cache {
        const val DEFAULT_DURATION = 30 * 60 * 1000L // 30 minutes
        const val SEVERE_WEATHER_DURATION = 10 * 60 * 1000L // 10 minutes for storms
        const val LOCATION_CACHE_DURATION = 60 * 60 * 1000L // 1 hour for location data
        const val WIDGET_UPDATE_INTERVAL = 15 * 60 * 1000L // 15 minutes for widgets
    }
    
    // Forecast limits
    object Forecast {
        const val MAX_HOURLY_FORECAST = 24 // 24 hours
        const val MAX_DAILY_FORECAST = 10 // 10 days
        const val HOURLY_DISPLAY_COUNT = 12 // Show 12 hours in UI
    }
    
    // UI dimensions (in dp)
    object UI {
        const val NAVIGATION_TOP_PADDING = 120
        const val CARD_PADDING = 16
        const val ICON_SIZE_SMALL = 24
        const val ICON_SIZE_MEDIUM = 32
        const val ICON_SIZE_LARGE = 48
        const val ANIMATION_DURATION = 300
        const val LOADING_DELAY = 500 // Minimum loading time for smooth UX
    }
    
    // Network configuration
    object Network {
        const val CONNECT_TIMEOUT = 10L // seconds
        const val READ_TIMEOUT = 30L // seconds
        const val WRITE_TIMEOUT = 15L // seconds
        const val MAX_RETRIES = 3
        const val RETRY_DELAY = 1000L // 1 second
    }
    
    // Location settings
    object Location {
        const val LOCATION_TIMEOUT = 10000L // 10 seconds
        const val MIN_LOCATION_ACCURACY = 100f // meters
        const val SEARCH_MIN_CHARS = 3 // Minimum characters for location search
        const val MAX_SEARCH_RESULTS = 10
    }
    
    // Weather codes and conditions
    object Weather {
        // OpenMeteo weather codes
        const val CLEAR_SKY = 0
        const val MAINLY_CLEAR = 1
        const val PARTLY_CLOUDY = 2
        const val OVERCAST = 3
        const val FOG_START = 45
        const val FOG_END = 48
        const val DRIZZLE_START = 51
        const val DRIZZLE_END = 57
        const val RAIN_START = 61
        const val RAIN_END = 67
        const val SNOW_START = 71
        const val SNOW_END = 86
        const val THUNDERSTORM_START = 95
        const val THUNDERSTORM_END = 99
        
        // Severe weather threshold for notifications
        const val SEVERE_WEATHER_THRESHOLD = 80 // km/h wind speed
        const val HEAVY_RAIN_THRESHOLD = 10.0 // mm/hour
    }
    
    // Widget configuration
    object Widget {
        const val WIDGET_UPDATE_WORK_NAME = "weather_widget_update"
        const val MIN_UPDATE_INTERVAL = 15 * 60 * 1000L // 15 minutes minimum
        const val WIDGET_CLICK_ACTION = "com.example.skypeek.WIDGET_CLICK"
        const val WIDGET_REFRESH_ACTION = "com.example.skypeek.WIDGET_REFRESH"
    }
    
    // Database configuration
    object Database {
        const val DATABASE_NAME = "weather_database"
        const val DATABASE_VERSION = 3 // Updated for new schema
        const val MAX_CACHED_LOCATIONS = 50
        const val CLEANUP_THRESHOLD_DAYS = 7 // Clean up data older than 7 days
    }
    
    // Error handling
    object Errors {
        const val MAX_ERROR_RETRIES = 3
        const val ERROR_RETRY_DELAY = 2000L // 2 seconds
        const val CIRCUIT_BREAKER_THRESHOLD = 5
        const val CIRCUIT_BREAKER_TIMEOUT = 60000L // 1 minute
    }
    
    // Animation constants
    object Animation {
        const val SUN_ROTATION_DURATION = 20000 // 20 seconds
        const val CLOUD_FLOAT_DURATION = 4000 // 4 seconds
        const val RAIN_DROP_DURATION = 1200 // 1.2 seconds
        const val LIGHTNING_FLASH_DURATION = 4000 // 4 seconds
        const val FOG_FLOW_DURATION = 6000 // 6 seconds
        const val SNOW_FALL_DURATION = 3000 // 3 seconds
    }
    
    // API endpoints and keys
    object Api {
        const val OPEN_METEO_BASE_URL = "https://api.open-meteo.com/v1/"
        const val WEATHER_API_BASE_URL = "https://api.weatherapi.com/v1/"
        const val OPENWEATHER_BASE_URL = "https://api.openweathermap.org/data/2.5/"
        
        // Request parameters
        const val TEMPERATURE_UNIT = "celsius"
        const val WIND_SPEED_UNIT = "kmh"
        const val PRECIPITATION_UNIT = "mm"
        const val TIMEZONE = "auto"
    }
}