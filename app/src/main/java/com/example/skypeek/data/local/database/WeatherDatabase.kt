package com.example.skypeek.data.local.database

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.skypeek.data.local.entities.SavedLocationEntity
import com.example.skypeek.data.local.entities.WeatherEntity
import com.example.skypeek.domain.model.DailyWeather
import com.example.skypeek.domain.model.HourlyWeather
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Database(
    entities = [WeatherEntity::class, SavedLocationEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class WeatherDatabase : RoomDatabase() {
    abstract fun weatherDao(): WeatherDao
    abstract fun locationDao(): LocationDao
    
    companion object {
        const val DATABASE_NAME = "weather_database"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic here if needed in future versions
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