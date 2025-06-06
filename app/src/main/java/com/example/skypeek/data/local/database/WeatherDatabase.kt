package com.example.skypeek.data.local.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.skypeek.data.local.entities.*
import com.example.skypeek.domain.model.DailyWeather
import com.example.skypeek.domain.model.HourlyWeather
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(
    entities = [
        // Legacy entities (keeping for migration)
        WeatherEntity::class, 
        SavedLocationEntity::class,
        // New normalized entities
        WeatherDataEntity::class,
        HourlyForecastEntity::class,
        DailyForecastEntity::class,
        WeatherAlertEntity::class,
        SavedLocationEntityV2::class,
        CacheMetadataEntity::class
    ],
    version = 3,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {
    // Legacy DAOs
    abstract fun weatherDao(): WeatherDao
    abstract fun locationDao(): LocationDao
    
    // New normalized DAOs
    abstract fun weatherDataDao(): WeatherDataDao
    abstract fun hourlyForecastDao(): HourlyForecastDao
    abstract fun dailyForecastDao(): DailyForecastDao
    abstract fun weatherAlertDao(): WeatherAlertDao
    abstract fun savedLocationDaoV2(): SavedLocationDaoV2
    abstract fun cacheMetadataDao(): CacheMetadataDao
    abstract fun weatherCombinedDao(): WeatherCombinedDao
    
    companion object {
        const val DATABASE_NAME = "weather_database"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add sunrise and sunset columns to weather_data table
                database.execSQL("ALTER TABLE weather_data ADD COLUMN sunrise TEXT")
                database.execSQL("ALTER TABLE weather_data ADD COLUMN sunset TEXT")
            }
        }
        
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new normalized tables
                
                // Weather data v2 table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS weather_data_v2 (
                        id TEXT NOT NULL PRIMARY KEY,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        cityName TEXT NOT NULL,
                        country TEXT NOT NULL,
                        currentTemp INTEGER NOT NULL,
                        condition TEXT NOT NULL,
                        weatherCode INTEGER NOT NULL,
                        highTemp INTEGER NOT NULL,
                        lowTemp INTEGER NOT NULL,
                        feelsLike INTEGER NOT NULL,
                        humidity INTEGER NOT NULL,
                        windSpeed REAL NOT NULL,
                        description TEXT NOT NULL,
                        backgroundType TEXT NOT NULL,
                        visibility REAL,
                        uvIndex INTEGER,
                        pressure REAL,
                        sunrise TEXT,
                        sunset TEXT,
                        lastUpdated INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Hourly forecast table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS hourly_forecast (
                        id TEXT NOT NULL PRIMARY KEY,
                        weatherDataId TEXT NOT NULL,
                        time TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        temperature INTEGER NOT NULL,
                        weatherCode INTEGER NOT NULL,
                        windSpeed REAL NOT NULL,
                        windDirection INTEGER NOT NULL,
                        humidity INTEGER NOT NULL,
                        pressure REAL,
                        precipitation REAL NOT NULL DEFAULT 0.0,
                        precipitationProbability INTEGER NOT NULL DEFAULT 0,
                        visibility REAL,
                        uvIndex INTEGER,
                        feelsLike INTEGER,
                        FOREIGN KEY (weatherDataId) REFERENCES weather_data_v2(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Daily forecast table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS daily_forecast (
                        id TEXT NOT NULL PRIMARY KEY,
                        weatherDataId TEXT NOT NULL,
                        date TEXT NOT NULL,
                        timestamp INTEGER NOT NULL,
                        highTemp INTEGER NOT NULL,
                        lowTemp INTEGER NOT NULL,
                        weatherCode INTEGER NOT NULL,
                        condition TEXT NOT NULL,
                        windSpeed REAL NOT NULL,
                        windDirection INTEGER NOT NULL,
                        humidity INTEGER NOT NULL,
                        precipitation REAL NOT NULL DEFAULT 0.0,
                        precipitationProbability INTEGER NOT NULL DEFAULT 0,
                        sunrise TEXT,
                        sunset TEXT,
                        uvIndex INTEGER,
                        pressure REAL,
                        FOREIGN KEY (weatherDataId) REFERENCES weather_data_v2(id) ON DELETE CASCADE
                    )
                """.trimIndent())
                
                // Weather alerts table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS weather_alerts (
                        id TEXT NOT NULL PRIMARY KEY,
                        locationId TEXT NOT NULL,
                        title TEXT NOT NULL,
                        description TEXT NOT NULL,
                        severity TEXT NOT NULL,
                        startTime INTEGER NOT NULL,
                        endTime INTEGER NOT NULL,
                        source TEXT NOT NULL,
                        event TEXT NOT NULL,
                        areas TEXT NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                
                // Enhanced saved locations table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS saved_locations_v2 (
                        id TEXT NOT NULL PRIMARY KEY,
                        latitude REAL NOT NULL,
                        longitude REAL NOT NULL,
                        cityName TEXT NOT NULL,
                        country TEXT NOT NULL,
                        state TEXT,
                        timezone TEXT,
                        isCurrentLocation INTEGER NOT NULL DEFAULT 0,
                        `order` INTEGER NOT NULL,
                        lastWeatherUpdate INTEGER,
                        addedAt INTEGER NOT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
                
                // Cache metadata table
                database.execSQL("""
                    CREATE TABLE IF NOT EXISTS cache_metadata (
                        key TEXT NOT NULL PRIMARY KEY,
                        lastUpdated INTEGER NOT NULL,
                        expiresAt INTEGER NOT NULL,
                        dataType TEXT NOT NULL,
                        source TEXT NOT NULL,
                        requestParams TEXT NOT NULL,
                        isValid INTEGER NOT NULL DEFAULT 1
                    )
                """.trimIndent())
                
                // Create indices for better performance
                database.execSQL("CREATE INDEX IF NOT EXISTS index_hourly_forecast_weatherDataId_time ON hourly_forecast(weatherDataId, time)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_daily_forecast_weatherDataId_date ON daily_forecast(weatherDataId, date)")
                database.execSQL("CREATE INDEX IF NOT EXISTS index_weather_alerts_locationId_startTime ON weather_alerts(locationId, startTime)")
                
                // Migrate existing data from old schema to new schema
                migrateWeatherData(database)
                migrateSavedLocations(database)
            }
            
            private fun migrateWeatherData(database: SupportSQLiteDatabase) {
                // Copy weather data from old table to new table (without forecast JSON)
                database.execSQL("""
                    INSERT INTO weather_data_v2 (
                        id, latitude, longitude, cityName, country, currentTemp, condition, 
                        weatherCode, highTemp, lowTemp, feelsLike, humidity, windSpeed, 
                        description, backgroundType, visibility, uvIndex, pressure, 
                        sunrise, sunset, lastUpdated
                    )
                    SELECT 
                        id, latitude, longitude, cityName, country, currentTemp, condition,
                        weatherCode, highTemp, lowTemp, feelsLike, humidity, windSpeed,
                        description, backgroundType, visibility, uvIndex, pressure,
                        sunrise, sunset, lastUpdated
                    FROM weather_data
                """.trimIndent())
            }
            
            private fun migrateSavedLocations(database: SupportSQLiteDatabase) {
                // Copy saved locations from old table to new table
                database.execSQL("""
                    INSERT INTO saved_locations_v2 (
                        id, latitude, longitude, cityName, country, isCurrentLocation, 
                        `order`, addedAt, isActive
                    )
                    SELECT 
                        id, latitude, longitude, cityName, country, isCurrentLocation,
                        `order`, addedAt, 1
                    FROM saved_locations
                """.trimIndent())
            }
        }
    }
}

@Dao
interface WeatherDao {
    @Query("SELECT * FROM weather_data WHERE latitude = :lat AND longitude = :lon")
    suspend fun getWeatherByLocation(lat: Double, lon: Double): WeatherEntity?
    
    @Query("SELECT * FROM weather_data WHERE id = :id")
    suspend fun getWeatherById(id: String): WeatherEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherEntity)
    
    @Update
    suspend fun updateWeather(weather: WeatherEntity)
    
    @Delete
    suspend fun deleteWeather(weather: WeatherEntity)
    
    @Query("DELETE FROM weather_data WHERE lastUpdated < :timestamp")
    suspend fun deleteOldWeatherData(timestamp: Long)
    
    @Query("SELECT * FROM weather_data ORDER BY lastUpdated DESC")
    suspend fun getAllWeatherData(): List<WeatherEntity>
    
    @Query("SELECT COUNT(*) FROM weather_data")
    suspend fun getWeatherDataCount(): Int
    
    @Query("DELETE FROM weather_data")
    suspend fun clearAllWeatherData()
}

@Dao
interface LocationDao {
    @Query("SELECT * FROM saved_locations ORDER BY `order` ASC")
    suspend fun getAllSavedLocations(): List<SavedLocationEntity>
    
    @Query("SELECT * FROM saved_locations WHERE id = :id")
    suspend fun getLocationById(id: String): SavedLocationEntity?
    
    @Query("SELECT * FROM saved_locations WHERE isCurrentLocation = 1 LIMIT 1")
    suspend fun getCurrentLocation(): SavedLocationEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<SavedLocationEntity>)
    
    @Update
    suspend fun updateLocation(location: SavedLocationEntity)
    
    @Delete
    suspend fun deleteLocation(location: SavedLocationEntity)
    
    @Query("DELETE FROM saved_locations WHERE id = :id")
    suspend fun deleteLocationById(id: String)
    
    @Query("UPDATE saved_locations SET `order` = :newOrder WHERE id = :locationId")
    suspend fun updateLocationOrder(locationId: String, newOrder: Int)
    
    @Query("SELECT COUNT(*) FROM saved_locations")
    suspend fun getLocationCount(): Int
    
    @Query("DELETE FROM saved_locations")
    suspend fun clearAllLocations()
    
    @Query("SELECT MAX(`order`) FROM saved_locations")
    suspend fun getMaxOrder(): Int?
}

class Converters {
    private val gson = Gson()
    
    @TypeConverter
    fun fromHourlyForecastList(value: List<HourlyWeather>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toHourlyForecastList(value: String): List<HourlyWeather> {
        val listType = object : TypeToken<List<HourlyWeather>>() {}.type
        return gson.fromJson(value, listType)
    }
    
    @TypeConverter
    fun fromDailyForecastList(value: List<DailyWeather>): String {
        return gson.toJson(value)
    }
    
    @TypeConverter
    fun toDailyForecastList(value: String): List<DailyWeather> {
        val listType = object : TypeToken<List<DailyWeather>>() {}.type
        return gson.fromJson(value, listType)
    }
} 