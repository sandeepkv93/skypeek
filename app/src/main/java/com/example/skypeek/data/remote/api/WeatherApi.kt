package com.example.skypeek.data.remote.api

import com.example.skypeek.data.remote.dto.OpenMeteoResponse
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Open-Meteo API - Primary weather service (Free, no API key required)
 * Documentation: https://open-meteo.com/en/docs
 */
interface OpenMeteoApi {
    @GET("v1/forecast")
    suspend fun getWeatherForecast(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("current") current: String = "temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m,apparent_temperature,surface_pressure,visibility",
        @Query("hourly") hourly: String = "temperature_2m,weather_code,wind_speed_10m,relative_humidity_2m,visibility",
        @Query("daily") daily: String = "temperature_2m_max,temperature_2m_min,weather_code,wind_speed_10m_max,sunrise,sunset",
        @Query("forecast_days") forecastDays: Int = 10,
        @Query("timezone") timezone: String = "auto",
        @Query("temperature_unit") temperatureUnit: String = "celsius",
        @Query("wind_speed_unit") windSpeedUnit: String = "kmh",
        @Query("precipitation_unit") precipitationUnit: String = "mm"
    ): OpenMeteoResponse
}

/**
 * WeatherAPI.com - Secondary service (1M calls/month free)
 * Documentation: https://www.weatherapi.com/docs/
 */
interface WeatherApiService {
    @GET("v1/current.json")
    suspend fun getCurrentWeather(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("aqi") aqi: String = "yes"
    ): WeatherApiCurrentResponse
    
    @GET("v1/forecast.json")
    suspend fun getForecast(
        @Query("key") apiKey: String,
        @Query("q") location: String,
        @Query("days") days: Int = 10,
        @Query("aqi") aqi: String = "yes",
        @Query("alerts") alerts: String = "yes"
    ): WeatherApiForecastResponse
}

/**
 * OpenWeatherMap API - Fallback service (1K calls/day free)
 * Documentation: https://openweathermap.org/api
 */
interface OpenWeatherMapApi {
    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherMapCurrentResponse
    
    @GET("data/2.5/forecast")
    suspend fun getFiveDayForecast(
        @Query("lat") latitude: Double,
        @Query("lon") longitude: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): OpenWeatherMapForecastResponse
}

// Data classes for WeatherAPI.com
data class WeatherApiCurrentResponse(
    val current: WeatherApiCurrent,
    val location: WeatherApiLocation
)

data class WeatherApiCurrent(
    val temp_c: Double,
    val condition: WeatherApiCondition,
    val wind_kph: Double,
    val humidity: Int,
    val feelslike_c: Double,
    val vis_km: Double,
    val uv: Double
)

data class WeatherApiCondition(
    val text: String,
    val icon: String,
    val code: Int
)

data class WeatherApiLocation(
    val name: String,
    val region: String,
    val country: String,
    val lat: Double,
    val lon: Double,
    val localtime: String
)

data class WeatherApiForecastResponse(
    val current: WeatherApiCurrent,
    val location: WeatherApiLocation,
    val forecast: WeatherApiForecast
)

data class WeatherApiForecast(
    val forecastday: List<WeatherApiForecastDay>
)

data class WeatherApiForecastDay(
    val date: String,
    val day: WeatherApiDay,
    val hour: List<WeatherApiHour>
)

data class WeatherApiDay(
    val maxtemp_c: Double,
    val mintemp_c: Double,
    val condition: WeatherApiCondition,
    val avghumidity: Int
)

data class WeatherApiHour(
    val time: String,
    val temp_c: Double,
    val condition: WeatherApiCondition,
    val wind_kph: Double,
    val humidity: Int
)

// Data classes for OpenWeatherMap
data class OpenWeatherMapCurrentResponse(
    val coord: OWMCoord,
    val weather: List<OWMWeather>,
    val main: OWMMain,
    val wind: OWMWind,
    val visibility: Int?,
    val name: String,
    val dt: Long
)

data class OWMCoord(
    val lat: Double,
    val lon: Double
)

data class OWMWeather(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
)

data class OWMMain(
    val temp: Double,
    val feels_like: Double,
    val temp_min: Double,
    val temp_max: Double,
    val pressure: Int,
    val humidity: Int
)

data class OWMWind(
    val speed: Double,
    val deg: Int?
)

data class OpenWeatherMapForecastResponse(
    val list: List<OWMForecastItem>,
    val city: OWMCity
)

data class OWMForecastItem(
    val dt: Long,
    val main: OWMMain,
    val weather: List<OWMWeather>,
    val wind: OWMWind,
    val visibility: Int?,
    val dt_txt: String
)

data class OWMCity(
    val name: String,
    val coord: OWMCoord,
    val country: String
) 