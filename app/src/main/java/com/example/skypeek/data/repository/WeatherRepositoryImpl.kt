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
                val cachedData = weatherDao.getWeatherByLocation(latitude, longitude)
                if (cachedData != null && !isDataStale(cachedData.lastUpdated)) {
                    return Result.success(mapEntityToWeatherData(cachedData))
                }
            }

            // Try Open-Meteo first (free, no API key required)
            val openMeteoResult = tryOpenMeteo(latitude, longitude)
            if (openMeteoResult.isSuccess) {
                val weatherData = openMeteoResult.getOrThrow()
                cacheWeatherData(weatherData)
                return Result.success(weatherData)
            }

            // Fallback to WeatherAPI.com
            val weatherApiResult = tryWeatherApi(latitude, longitude)
            if (weatherApiResult.isSuccess) {
                val weatherData = weatherApiResult.getOrThrow()
                cacheWeatherData(weatherData)
                return Result.success(weatherData)
            }

            // Final fallback to OpenWeatherMap
            val openWeatherResult = tryOpenWeatherMap(latitude, longitude)
            if (openWeatherResult.isSuccess) {
                val weatherData = openWeatherResult.getOrThrow()
                cacheWeatherData(weatherData)
                return Result.success(weatherData)
            }

            // If all APIs fail, try to return cached data
            val cachedData = weatherDao.getWeatherByLocation(latitude, longitude)
            if (cachedData != null) {
                return Result.success(mapEntityToWeatherData(cachedData))
            }

            Result.failure(Exception("Unable to fetch weather data from any source"))

        } catch (e: Exception) {
            // Return cached data if available
            val cachedData = weatherDao.getWeatherByLocation(latitude, longitude)
            if (cachedData != null) {
                return Result.success(mapEntityToWeatherData(cachedData))
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
                pressure = current.surfacePressure
            ),
            hourlyForecast = mapOpenMeteoHourlyForecast(response.hourly),
            dailyForecast = mapOpenMeteoDailyForecast(response.daily),
            lastUpdated = System.currentTimeMillis()
        )
    }

    private fun mapOpenMeteoHourlyForecast(hourly: com.example.skypeek.data.remote.dto.OpenMeteoHourlyForecast): List<HourlyWeather> {
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
                uvIndex = current.uv.toInt()
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
                pressure = current.main.pressure.toDouble()
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
        val entity = mapWeatherDataToEntity(weatherData)
        weatherDao.insertWeather(entity)
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
                pressure = entity.pressure
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
        return if (index == 0) {
            "Now"
        } else {
            try {
                val instant = Instant.parse(time)
                val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
                val hour = localDateTime.hour
                when {
                    hour == 0 -> "12AM"
                    hour < 12 -> "${hour}AM"
                    hour == 12 -> "12PM"
                    else -> "${hour - 12}PM"
                }
            } catch (e: Exception) {
                "${index}H"
            }
        }
    }

    private fun formatHourlyTimeFromTimestamp(timestamp: Long, isFirst: Boolean): String {
        return if (isFirst) {
            "Now"
        } else {
            try {
                val calendar = Calendar.getInstance().apply { timeInMillis = timestamp }
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                when {
                    hour == 0 -> "12AM"
                    hour < 12 -> "${hour}AM"
                    hour == 12 -> "12PM"
                    else -> "${hour - 12}PM"
                }
            } catch (e: Exception) {
                "Hour"
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
} 