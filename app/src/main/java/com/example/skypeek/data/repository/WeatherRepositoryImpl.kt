package com.example.skypeek.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.skypeek.BuildConfig
import com.example.skypeek.data.local.database.LocationDao
import com.example.skypeek.data.local.database.WeatherDao
import com.example.skypeek.data.local.entities.WeatherEntity
import com.example.skypeek.data.remote.api.OpenMeteoApi
import com.example.skypeek.data.remote.api.OpenWeatherMapApi
import com.example.skypeek.data.remote.api.WeatherApiService
import com.example.skypeek.data.remote.dto.OpenMeteoResponse
import com.example.skypeek.domain.model.*
import com.example.skypeek.domain.repository.WeatherRepository
import com.example.skypeek.utils.WeatherCodeMapper
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

// DataStore extension
private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "weather_prefs")

@Singleton
class WeatherRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val openMeteoApi: OpenMeteoApi,
    private val weatherApiService: WeatherApiService,
    private val openWeatherMapApi: OpenWeatherMapApi,
    private val weatherDao: WeatherDao,
    private val locationDao: LocationDao,
    private val gson: Gson
) : WeatherRepository {

    private val dataStore = context.dataStore
    
    companion object {
        private val WEATHER_API_KEY = stringPreferencesKey("weather_api_key")
        private val OPENWEATHER_API_KEY = stringPreferencesKey("openweather_api_key")
        private const val CACHE_DURATION = 30 * 60 * 1000L // 30 minutes
    }

    override suspend fun getWeatherData(
        latitude: Double,
        longitude: Double,
        forceRefresh: Boolean
    ): Result<WeatherData> {
        return try {
            // Check cache first unless forcing refresh
            if (!forceRefresh) {
                try {
                    val cachedData = weatherDao.getWeatherByLocation(latitude, longitude)
                    if (cachedData != null && !isDataStale(cachedData.lastUpdated)) {
                        return Result.success(mapEntityToWeatherData(cachedData))
                    }
                } catch (e: Exception) {
                    // Cache read failed, probably due to schema mismatch - clear it
                    println("🔥 Cache read failed, clearing database: ${e.message}")
                    try {
                        weatherDao.clearAllWeatherData()
                    } catch (clearException: Exception) {
                        println("🔥 Failed to clear database: ${clearException.message}")
                    }
                }
            }

            // Try Open-Meteo first (free, no API key required)
            println("🌐 Trying Open-Meteo API...")
            val openMeteoResult = tryOpenMeteo(latitude, longitude)
            if (openMeteoResult.isSuccess) {
                val weatherData = openMeteoResult.getOrThrow()
                cacheWeatherData(weatherData)
                return Result.success(weatherData)
            } else {
                println("❌ Open-Meteo failed: ${openMeteoResult.exceptionOrNull()?.message}")
            }

            // Fallback to WeatherAPI.com
            println("🌐 Trying WeatherAPI.com...")
            val weatherApiResult = tryWeatherApi(latitude, longitude)
            if (weatherApiResult.isSuccess) {
                val weatherData = weatherApiResult.getOrThrow()
                cacheWeatherData(weatherData)
                return Result.success(weatherData)
            } else {
                println("❌ WeatherAPI failed: ${weatherApiResult.exceptionOrNull()?.message}")
            }

            // Final fallback to OpenWeatherMap
            println("🌐 Trying OpenWeatherMap...")
            val openWeatherResult = tryOpenWeatherMap(latitude, longitude)
            if (openWeatherResult.isSuccess) {
                val weatherData = openWeatherResult.getOrThrow()
                cacheWeatherData(weatherData)
                return Result.success(weatherData)
            } else {
                println("❌ OpenWeatherMap failed: ${openWeatherResult.exceptionOrNull()?.message}")
            }

            // If all APIs fail, try to return cached data
            try {
                val cachedData = weatherDao.getWeatherByLocation(latitude, longitude)
                if (cachedData != null) {
                    return Result.success(mapEntityToWeatherData(cachedData))
                }
            } catch (e: Exception) {
                println("🔥 Final cache read failed: ${e.message}")
            }

            Result.failure(Exception("Unable to fetch weather data from any source"))

        } catch (e: Exception) {
            println("🔥 General error in getWeatherData: ${e.message}")
            // Return cached data if available
            try {
                val cachedData = weatherDao.getWeatherByLocation(latitude, longitude)
                if (cachedData != null) {
                    return Result.success(mapEntityToWeatherData(cachedData))
                }
            } catch (cacheException: Exception) {
                println("🔥 Cache exception: ${cacheException.message}")
            }
            Result.failure(e)
        }
    }

    private suspend fun tryOpenMeteo(latitude: Double, longitude: Double): Result<WeatherData> {
        return try {
            val response = openMeteoApi.getWeatherForecast(latitude, longitude)
            val weatherData = mapOpenMeteoToWeatherData(response, latitude, longitude)
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun tryWeatherApi(latitude: Double, longitude: Double): Result<WeatherData> {
        return try {
            val apiKey = getWeatherApiKey()
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("WeatherAPI key not configured"))
            }
            
            val response = weatherApiService.getForecast(
                apiKey = apiKey,
                location = "$latitude,$longitude",
                days = 10
            )
            val weatherData = mapWeatherApiToWeatherData(response)
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun tryOpenWeatherMap(latitude: Double, longitude: Double): Result<WeatherData> {
        return try {
            val apiKey = getOpenWeatherApiKey()
            if (apiKey.isEmpty()) {
                return Result.failure(Exception("OpenWeatherMap key not configured"))
            }
            
            val currentResponse = openWeatherMapApi.getCurrentWeather(latitude, longitude, apiKey)
            val forecastResponse = openWeatherMapApi.getFiveDayForecast(latitude, longitude, apiKey)
            val weatherData = mapOpenWeatherMapToWeatherData(currentResponse, forecastResponse)
            Result.success(weatherData)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapOpenMeteoToWeatherData(
        response: OpenMeteoResponse,
        latitude: Double,
        longitude: Double
    ): WeatherData {
        val current = response.current
        val weatherInfo = WeatherCodeMapper.mapOpenMeteoCode(current.weatherCode)
        
        // Extract today's sunrise and sunset times
        val todaySunrise = response.daily.sunrise?.firstOrNull()?.let { formatSunTime(it) }
        val todaySunset = response.daily.sunset?.firstOrNull()?.let { formatSunTime(it) }
        
        // 🔍 DEBUG: Add extensive logging for Open-Meteo response
        println("🌤️ OPEN-METEO API RESPONSE DEBUG:")
        println("   Current Weather Code: ${current.weatherCode}")
        println("   Current Temperature: ${current.temperature2m}")
        println("   Current Condition: ${weatherInfo.description}")
        println("   Sunrise: $todaySunrise")
        println("   Sunset: $todaySunset")
        println("   Hourly Weather Codes (first 6): ${response.hourly.weatherCode.take(6)}")
        println("   Daily Weather Codes: ${response.daily.weatherCode}")
        println("   Raw API Response: $response")
        
        return WeatherData(
            location = LocationData(
                latitude = latitude,
                longitude = longitude,
                cityName = "Loading...", // Will be reverse geocoded
                country = ""
            ),
            currentWeather = CurrentWeather(
                temperature = current.temperature2m.toInt(),
                condition = weatherInfo.description,
                weatherCode = current.weatherCode,
                highTemp = response.daily.temperature2mMax.first().toInt(),
                lowTemp = response.daily.temperature2mMin.first().toInt(),
                feelsLike = current.apparentTemperature.toInt(),
                humidity = current.relativeHumidity2m,
                windSpeed = current.windSpeed10m,
                icon = weatherInfo.icon,
                backgroundType = weatherInfo.type,
                description = WeatherCodeMapper.generateWeatherDescription(
                    weatherInfo, 
                    current.temperature2m.toInt(), 
                    current.windSpeed10m
                ),
                visibility = current.visibility,
                pressure = current.surfacePressure,
                sunrise = todaySunrise,
                sunset = todaySunset
            ),
            hourlyForecast = mapOpenMeteoHourlyForecast(response.hourly),
            dailyForecast = mapOpenMeteoDailyForecast(response.daily),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun mapOpenMeteoHourlyForecast(hourly: com.example.skypeek.data.remote.dto.OpenMeteoHourlyForecast): List<HourlyWeather> {
        // 🔍 DEBUG: Check what hourly weather codes we're getting
        println("🔍 HOURLY FORECAST DEBUG:")
        println("   First 10 hourly codes: ${hourly.weatherCode.take(10)}")
        
        return hourly.time.take(24).mapIndexed { index, time ->
            val weatherInfo = WeatherCodeMapper.mapOpenMeteoCode(hourly.weatherCode[index])
            HourlyWeather(
                time = formatHourlyTime(time, index),
                temperature = hourly.temperature2m[index].toInt(),
                weatherCode = hourly.weatherCode[index],
                icon = weatherInfo.icon,
                humidity = hourly.relativeHumidity2m[index],
                windSpeed = hourly.windSpeed10m[index],
                timestamp = parseISODateString(time)
            )
        }
    }

    private fun mapOpenMeteoDailyForecast(daily: com.example.skypeek.data.remote.dto.OpenMeteoDailyForecast): List<DailyWeather> {
        // 🔍 DEBUG: Check what daily weather codes we're getting
        println("🔍 DAILY FORECAST DEBUG:")
        println("   All daily codes: ${daily.weatherCode}")
        
        return daily.time.mapIndexed { index, date ->
            val weatherInfo = WeatherCodeMapper.mapOpenMeteoCode(daily.weatherCode[index])
            DailyWeather(
                date = date,
                dayName = formatDayName(date, index),
                highTemp = daily.temperature2mMax[index].toInt(),
                lowTemp = daily.temperature2mMin[index].toInt(),
                weatherCode = daily.weatherCode[index],
                icon = weatherInfo.icon,
                condition = weatherInfo.description
            )
        }
    }

    private fun mapWeatherApiToWeatherData(response: com.example.skypeek.data.remote.api.WeatherApiForecastResponse): WeatherData {
        val current = response.current
        val location = response.location
        val weatherInfo = WeatherCodeMapper.mapWeatherAPICode(current.condition.code)
        
        return WeatherData(
            location = LocationData(
                latitude = location.lat,
                longitude = location.lon,
                cityName = location.name,
                country = location.country
            ),
            currentWeather = CurrentWeather(
                temperature = current.temp_c.toInt(),
                condition = current.condition.text,
                weatherCode = current.condition.code,
                highTemp = response.forecast.forecastday.first().day.maxtemp_c.toInt(),
                lowTemp = response.forecast.forecastday.first().day.mintemp_c.toInt(),
                feelsLike = current.feelslike_c.toInt(),
                humidity = current.humidity,
                windSpeed = current.wind_kph,
                icon = weatherInfo.icon,
                backgroundType = weatherInfo.type,
                description = WeatherCodeMapper.generateWeatherDescription(
                    weatherInfo, 
                    current.temp_c.toInt(), 
                    current.wind_kph
                ),
                visibility = current.vis_km,
                uvIndex = current.uv.toInt(),
                sunrise = null,
                sunset = null
            ),
            hourlyForecast = mapWeatherApiHourlyForecast(response.forecast.forecastday),
            dailyForecast = mapWeatherApiDailyForecast(response.forecast.forecastday),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun mapWeatherApiHourlyForecast(forecastDays: List<com.example.skypeek.data.remote.api.WeatherApiForecastDay>): List<HourlyWeather> {
        val hourlyList = mutableListOf<HourlyWeather>()
        val currentTime = System.currentTimeMillis()
        
        forecastDays.take(2).forEach { day ->
            day.hour.forEach { hour ->
                val hourTime = parseWeatherApiTime(hour.time)
                if (hourTime >= currentTime && hourlyList.size < 24) {
                    val weatherInfo = WeatherCodeMapper.mapWeatherAPICode(hour.condition.code)
                    hourlyList.add(
                        HourlyWeather(
                            time = formatHourlyTimeFromTimestamp(hourTime, hourlyList.isEmpty()),
                            temperature = hour.temp_c.toInt(),
                            weatherCode = hour.condition.code,
                            icon = weatherInfo.icon,
                            humidity = hour.humidity,
                            windSpeed = hour.wind_kph,
                            timestamp = hourTime
                        )
                    )
                }
            }
        }
        
        return hourlyList.take(24)
    }

    private fun mapWeatherApiDailyForecast(forecastDays: List<com.example.skypeek.data.remote.api.WeatherApiForecastDay>): List<DailyWeather> {
        return forecastDays.mapIndexed { index, day ->
            val weatherInfo = WeatherCodeMapper.mapWeatherAPICode(day.day.condition.code)
            DailyWeather(
                date = day.date,
                dayName = formatDayName(day.date, index),
                highTemp = day.day.maxtemp_c.toInt(),
                lowTemp = day.day.mintemp_c.toInt(),
                weatherCode = day.day.condition.code,
                icon = weatherInfo.icon,
                condition = day.day.condition.text,
                humidity = day.day.avghumidity
            )
        }
    }

    private fun mapOpenWeatherMapToWeatherData(
        current: com.example.skypeek.data.remote.api.OpenWeatherMapCurrentResponse,
        forecast: com.example.skypeek.data.remote.api.OpenWeatherMapForecastResponse
    ): WeatherData {
        val weather = current.weather.first()
        val weatherInfo = WeatherCodeMapper.mapOpenWeatherMapCode(weather.id)
        
        return WeatherData(
            location = LocationData(
                latitude = current.coord.lat,
                longitude = current.coord.lon,
                cityName = current.name,
                country = forecast.city.country
            ),
            currentWeather = CurrentWeather(
                temperature = current.main.temp.toInt(),
                condition = weather.description.replaceFirstChar { it.uppercase() },
                weatherCode = weather.id,
                highTemp = current.main.temp_max.toInt(),
                lowTemp = current.main.temp_min.toInt(),
                feelsLike = current.main.feels_like.toInt(),
                humidity = current.main.humidity,
                windSpeed = current.wind.speed * 3.6, // Convert m/s to km/h
                icon = weatherInfo.icon,
                backgroundType = weatherInfo.type,
                description = WeatherCodeMapper.generateWeatherDescription(
                    weatherInfo, 
                    current.main.temp.toInt(), 
                    current.wind.speed * 3.6
                ),
                visibility = current.visibility?.div(1000.0),
                pressure = current.main.pressure.toDouble(),
                sunrise = null,
                sunset = null
            ),
            hourlyForecast = mapOpenWeatherMapHourlyForecast(forecast.list),
            dailyForecast = mapOpenWeatherMapDailyForecast(forecast.list),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun mapOpenWeatherMapHourlyForecast(forecastList: List<com.example.skypeek.data.remote.api.OWMForecastItem>): List<HourlyWeather> {
        return forecastList.take(24).mapIndexed { index, item ->
            val weather = item.weather.first()
            val weatherInfo = WeatherCodeMapper.mapOpenWeatherMapCode(weather.id)
            HourlyWeather(
                time = formatHourlyTimeFromTimestamp(item.dt * 1000, index == 0),
                temperature = item.main.temp.toInt(),
                weatherCode = weather.id,
                icon = weatherInfo.icon,
                humidity = item.main.humidity,
                windSpeed = item.wind.speed * 3.6, // Convert m/s to km/h
                timestamp = item.dt * 1000
            )
        }
    }

    private fun mapOpenWeatherMapDailyForecast(forecastList: List<com.example.skypeek.data.remote.api.OWMForecastItem>): List<DailyWeather> {
        // Group by date and take the first forecast for each day
        val dailyMap = forecastList.groupBy { item ->
            val date = Date(item.dt * 1000)
            android.text.format.DateFormat.format("yyyy-MM-dd", date).toString()
        }
        
        return dailyMap.entries.take(10).mapIndexed { index, (date, items) ->
            val item = items.first()
            val weather = item.weather.first()
            val weatherInfo = WeatherCodeMapper.mapOpenWeatherMapCode(weather.id)
            
            // Calculate high/low from all items for this day
            val highTemp = items.maxOfOrNull { it.main.temp_max }?.toInt() ?: item.main.temp.toInt()
            val lowTemp = items.minOfOrNull { it.main.temp_min }?.toInt() ?: item.main.temp.toInt()
            
            DailyWeather(
                date = date,
                dayName = formatDayName(date, index),
                highTemp = highTemp,
                lowTemp = lowTemp,
                weatherCode = weather.id,
                icon = weatherInfo.icon,
                condition = weather.description.replaceFirstChar { it.uppercase() },
                humidity = item.main.humidity
            )
        }
    }

    // Utility functions
    private fun isDataStale(lastUpdated: Long): Boolean {
        return (System.currentTimeMillis() - lastUpdated) > CACHE_DURATION
    }

    private suspend fun cacheWeatherData(weatherData: WeatherData) {
        try {
            val entity = mapWeatherDataToEntity(weatherData)
            weatherDao.insertWeather(entity)
            println("✅ Successfully cached weather data")
        } catch (e: Exception) {
            println("⚠️ Failed to cache weather data: ${e.message}")
            // Don't fail the entire operation if cache write fails
        }
    }

    private fun mapWeatherDataToEntity(weatherData: WeatherData): WeatherEntity {
        val location = weatherData.location
        return WeatherEntity(
            id = "${location.latitude}_${location.longitude}",
            latitude = location.latitude,
            longitude = location.longitude,
            cityName = location.cityName,
            country = location.country,
            currentTemp = weatherData.currentWeather.temperature,
            condition = weatherData.currentWeather.condition,
            weatherCode = weatherData.currentWeather.weatherCode,
            highTemp = weatherData.currentWeather.highTemp,
            lowTemp = weatherData.currentWeather.lowTemp,
            feelsLike = weatherData.currentWeather.feelsLike,
            humidity = weatherData.currentWeather.humidity,
            windSpeed = weatherData.currentWeather.windSpeed,
            description = weatherData.currentWeather.description,
            backgroundType = weatherData.currentWeather.backgroundType.name,
            visibility = weatherData.currentWeather.visibility,
            uvIndex = weatherData.currentWeather.uvIndex,
            pressure = weatherData.currentWeather.pressure,
            sunrise = weatherData.currentWeather.sunrise,
            sunset = weatherData.currentWeather.sunset,
            hourlyForecastJson = gson.toJson(weatherData.hourlyForecast),
            dailyForecastJson = gson.toJson(weatherData.dailyForecast),
            lastUpdated = weatherData.lastUpdated
        )
    }

    private fun mapEntityToWeatherData(entity: WeatherEntity): WeatherData {
        val hourlyForecast = gson.fromJson(entity.hourlyForecastJson, Array<HourlyWeather>::class.java).toList()
        val dailyForecast = gson.fromJson(entity.dailyForecastJson, Array<DailyWeather>::class.java).toList()
        
        return WeatherData(
            location = LocationData(
                latitude = entity.latitude,
                longitude = entity.longitude,
                cityName = entity.cityName,
                country = entity.country
            ),
            currentWeather = CurrentWeather(
                temperature = entity.currentTemp,
                condition = entity.condition,
                weatherCode = entity.weatherCode,
                highTemp = entity.highTemp,
                lowTemp = entity.lowTemp,
                feelsLike = entity.feelsLike,
                humidity = entity.humidity,
                windSpeed = entity.windSpeed,
                icon = WeatherCodeMapper.mapOpenMeteoCode(entity.weatherCode).icon,
                backgroundType = WeatherType.valueOf(entity.backgroundType),
                description = entity.description,
                visibility = entity.visibility,
                uvIndex = entity.uvIndex,
                pressure = entity.pressure,
                sunrise = entity.sunrise,
                sunset = entity.sunset
            ),
            hourlyForecast = hourlyForecast,
            dailyForecast = dailyForecast,
            lastUpdated = entity.lastUpdated
        )
    }

    private suspend fun getWeatherApiKey(): String {
        return dataStore.data.first()[WEATHER_API_KEY] ?: BuildConfig.WEATHER_API_KEY
    }

    private suspend fun getOpenWeatherApiKey(): String {
        return dataStore.data.first()[OPENWEATHER_API_KEY] ?: BuildConfig.OPENWEATHER_API_KEY
    }

    // Time formatting utilities
    private fun formatHourlyTime(time: String, index: Int): String {
        return try {
            val instant = Instant.parse(time)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            val hour = localDateTime.hour
            val minute = localDateTime.minute
            
            // Always show proper time format
            when {
                hour == 0 -> if (minute == 0) "12AM" else String.format(Locale.US, "12:%02dAM", minute)
                hour < 12 -> if (minute == 0) "${hour}AM" else String.format(Locale.US, "%d:%02dAM", hour, minute)
                hour == 12 -> if (minute == 0) "12PM" else String.format(Locale.US, "12:%02dPM", minute)
                else -> if (minute == 0) "${hour - 12}PM" else String.format(Locale.US, "%d:%02dPM", hour - 12, minute)
            }
        } catch (e: Exception) {
            // Fallback to current time if parsing fails
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.HOUR_OF_DAY, index)
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            when {
                hour == 0 -> "12AM"
                hour < 12 -> "${hour}AM"
                hour == 12 -> "12PM"
                else -> "${hour - 12}PM"
            }
        }
    }

    private fun formatHourlyTimeFromTimestamp(timestamp: Long, isFirst: Boolean): String {
        return try {
            val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)
            
            // Always show proper time format for all hours
            when {
                hour == 0 -> if (minute == 0) "12AM" else String.format(Locale.US, "12:%02dAM", minute)
                hour < 12 -> if (minute == 0) "${hour}AM" else String.format(Locale.US, "%d:%02dAM", hour, minute)
                hour == 12 -> if (minute == 0) "12PM" else String.format(Locale.US, "12:%02dPM", minute)
                else -> if (minute == 0) "${hour - 12}PM" else String.format(Locale.US, "%d:%02dPM", hour - 12, minute)
            }
        } catch (e: Exception) {
            // Fallback to current time format
            val calendar = Calendar.getInstance()
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            when {
                hour == 0 -> "12AM"
                hour < 12 -> "${hour}AM"  
                hour == 12 -> "12PM"
                else -> "${hour - 12}PM"
            }
        }
    }

    private fun formatDayName(date: String, index: Int): String {
        return if (index == 0) {
            "Today"
        } else {
            try {
                val localDate = LocalDate.parse(date)
                val dayOfWeek = localDate.dayOfWeek
                when (dayOfWeek.value) {
                    1 -> "Mon"
                    2 -> "Tue"
                    3 -> "Wed"
                    4 -> "Thu"
                    5 -> "Fri"
                    6 -> "Sat"
                    7 -> "Sun"
                    else -> "Day"
                }
            } catch (e: Exception) {
                "Day $index"
            }
        }
    }

    private fun parseISODateString(dateString: String): Long {
        return try {
            val instant = Instant.parse(dateString)
            instant.toEpochMilliseconds()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    private fun parseWeatherApiTime(timeString: String): Long {
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            val localDateTime = java.time.LocalDateTime.parse(timeString, formatter)
            localDateTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        } catch (e: Exception) {
            System.currentTimeMillis()
        }
    }

    override suspend fun clearCache() {
        try {
            val oldTimestamp = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000L) // 7 days ago
            weatherDao.deleteOldWeatherData(oldTimestamp)
        } catch (e: Exception) {
            // Log error but don't fail
        }
    }

    // Helper function to format sunrise/sunset times from ISO format to display format
    private fun formatSunTime(isoTimeString: String): String {
        return try {
            val instant = Instant.parse(isoTimeString)
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            
            // Format as "6:45 AM" or "7:23 PM"
            val hour = localDateTime.hour
            val minute = localDateTime.minute
            val amPm = if (hour < 12) "AM" else "PM"
            val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
            
            "${displayHour}:${minute.toString().padStart(2, '0')} $amPm"
        } catch (e: Exception) {
            // Fallback parsing if ISO format fails
            try {
                // Sometimes the format might be "2024-01-15T06:45"
                val parts = isoTimeString.split("T")
                if (parts.size == 2) {
                    val timePart = parts[1]
                    val timeParts = timePart.split(":")
                    if (timeParts.size >= 2) {
                        val hour = timeParts[0].toInt()
                        val minute = timeParts[1].toInt()
                        val amPm = if (hour < 12) "AM" else "PM"
                        val displayHour = if (hour == 0) 12 else if (hour > 12) hour - 12 else hour
                        
                        return "${displayHour}:${minute.toString().padStart(2, '0')} $amPm"
                    }
                }
            } catch (e2: Exception) {
                // Ignore and return original
            }
            isoTimeString // Return original if parsing fails
        }
    }
} 