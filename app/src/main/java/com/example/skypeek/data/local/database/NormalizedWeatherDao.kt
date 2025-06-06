package com.example.skypeek.data.local.database

import androidx.room.*
import com.example.skypeek.data.local.entities.*
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Objects for normalized weather database schema
 */

@Dao
interface WeatherDataDao {
    @Query("SELECT * FROM weather_data_v2 WHERE latitude = :lat AND longitude = :lon")
    suspend fun getWeatherByLocation(lat: Double, lon: Double): WeatherDataEntity?
    
    @Query("SELECT * FROM weather_data_v2 WHERE id = :id")
    suspend fun getWeatherById(id: String): WeatherDataEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWeather(weather: WeatherDataEntity)
    
    @Update
    suspend fun updateWeather(weather: WeatherDataEntity)
    
    @Delete
    suspend fun deleteWeather(weather: WeatherDataEntity)
    
    @Query("DELETE FROM weather_data_v2 WHERE lastUpdated < :timestamp")
    suspend fun deleteOldWeatherData(timestamp: Long)
    
    @Query("SELECT * FROM weather_data_v2 ORDER BY lastUpdated DESC")
    suspend fun getAllWeatherData(): List<WeatherDataEntity>
    
    @Query("SELECT COUNT(*) FROM weather_data_v2")
    suspend fun getWeatherDataCount(): Int
    
    @Query("DELETE FROM weather_data_v2")
    suspend fun clearAllWeatherData()
    
    @Query("SELECT * FROM weather_data_v2 WHERE id IN (:locationIds)")
    suspend fun getWeatherForLocations(locationIds: List<String>): List<WeatherDataEntity>
}

@Dao
interface HourlyForecastDao {
    @Query("SELECT * FROM hourly_forecast WHERE weatherDataId = :weatherDataId ORDER BY timestamp ASC")
    suspend fun getHourlyForecast(weatherDataId: String): List<HourlyForecastEntity>
    
    @Query("SELECT * FROM hourly_forecast WHERE weatherDataId = :weatherDataId AND timestamp >= :fromTime ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getHourlyForecastFromTime(weatherDataId: String, fromTime: Long, limit: Int = 24): List<HourlyForecastEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHourlyForecast(forecasts: List<HourlyForecastEntity>)
    
    @Query("DELETE FROM hourly_forecast WHERE weatherDataId = :weatherDataId")
    suspend fun deleteHourlyForecast(weatherDataId: String)
    
    @Query("DELETE FROM hourly_forecast WHERE timestamp < :timestamp")
    suspend fun deleteOldHourlyData(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM hourly_forecast WHERE weatherDataId = :weatherDataId")
    suspend fun getHourlyForecastCount(weatherDataId: String): Int
}

@Dao
interface DailyForecastDao {
    @Query("SELECT * FROM daily_forecast WHERE weatherDataId = :weatherDataId ORDER BY timestamp ASC")
    suspend fun getDailyForecast(weatherDataId: String): List<DailyForecastEntity>
    
    @Query("SELECT * FROM daily_forecast WHERE weatherDataId = :weatherDataId AND timestamp >= :fromTime ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getDailyForecastFromDate(weatherDataId: String, fromTime: Long, limit: Int = 10): List<DailyForecastEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDailyForecast(forecasts: List<DailyForecastEntity>)
    
    @Query("DELETE FROM daily_forecast WHERE weatherDataId = :weatherDataId")
    suspend fun deleteDailyForecast(weatherDataId: String)
    
    @Query("DELETE FROM daily_forecast WHERE timestamp < :timestamp")
    suspend fun deleteOldDailyData(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM daily_forecast WHERE weatherDataId = :weatherDataId")
    suspend fun getDailyForecastCount(weatherDataId: String): Int
}

@Dao
interface WeatherAlertDao {
    @Query("SELECT * FROM weather_alerts WHERE locationId = :locationId AND isActive = 1 ORDER BY startTime DESC")
    suspend fun getActiveAlertsForLocation(locationId: String): List<WeatherAlertEntity>
    
    @Query("SELECT * FROM weather_alerts WHERE isActive = 1 AND startTime <= :currentTime AND endTime > :currentTime")
    suspend fun getCurrentActiveAlerts(currentTime: Long): List<WeatherAlertEntity>
    
    @Query("SELECT * FROM weather_alerts WHERE locationId IN (:locationIds) AND isActive = 1")
    suspend fun getActiveAlertsForLocations(locationIds: List<String>): List<WeatherAlertEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlert(alert: WeatherAlertEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlerts(alerts: List<WeatherAlertEntity>)
    
    @Query("UPDATE weather_alerts SET isActive = 0 WHERE id = :alertId")
    suspend fun deactivateAlert(alertId: String)
    
    @Query("UPDATE weather_alerts SET isActive = 0 WHERE endTime < :currentTime")
    suspend fun deactivateExpiredAlerts(currentTime: Long)
    
    @Query("DELETE FROM weather_alerts WHERE endTime < :timestamp AND isActive = 0")
    suspend fun deleteOldAlerts(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM weather_alerts WHERE locationId = :locationId AND isActive = 1")
    suspend fun getActiveAlertCount(locationId: String): Int
}

@Dao
interface SavedLocationDaoV2 {
    @Query("SELECT * FROM saved_locations_v2 WHERE isActive = 1 ORDER BY `order` ASC")
    suspend fun getAllSavedLocations(): List<SavedLocationEntityV2>
    
    @Query("SELECT * FROM saved_locations_v2 WHERE isActive = 1 ORDER BY `order` ASC")
    fun getAllSavedLocationsFlow(): Flow<List<SavedLocationEntityV2>>
    
    @Query("SELECT * FROM saved_locations_v2 WHERE id = :id AND isActive = 1")
    suspend fun getLocationById(id: String): SavedLocationEntityV2?
    
    @Query("SELECT * FROM saved_locations_v2 WHERE isCurrentLocation = 1 AND isActive = 1 LIMIT 1")
    suspend fun getCurrentLocation(): SavedLocationEntityV2?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: SavedLocationEntityV2)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<SavedLocationEntityV2>)
    
    @Update
    suspend fun updateLocation(location: SavedLocationEntityV2)
    
    @Query("UPDATE saved_locations_v2 SET isActive = 0 WHERE id = :id")
    suspend fun softDeleteLocation(id: String)
    
    @Delete
    suspend fun hardDeleteLocation(location: SavedLocationEntityV2)
    
    @Query("UPDATE saved_locations_v2 SET `order` = :newOrder WHERE id = :locationId")
    suspend fun updateLocationOrder(locationId: String, newOrder: Int)
    
    @Query("UPDATE saved_locations_v2 SET lastWeatherUpdate = :timestamp WHERE id = :locationId")
    suspend fun updateLastWeatherUpdate(locationId: String, timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM saved_locations_v2 WHERE isActive = 1")
    suspend fun getActiveLocationCount(): Int
    
    @Query("SELECT MAX(`order`) FROM saved_locations_v2 WHERE isActive = 1")
    suspend fun getMaxOrder(): Int?
    
    @Query("DELETE FROM saved_locations_v2 WHERE isActive = 0 AND addedAt < :timestamp")
    suspend fun permanentlyDeleteOldLocations(timestamp: Long)
    
    @Query("UPDATE saved_locations_v2 SET isCurrentLocation = 0 WHERE isCurrentLocation = 1 AND isActive = 1")
    suspend fun clearCurrentLocationFlags()
    
    /**
     * Atomically update current location
     */
    @Transaction
    suspend fun updateCurrentLocation(newCurrentLocation: SavedLocationEntityV2) {
        // Clear all current location flags
        clearCurrentLocationFlags()
        // Insert new current location
        insertLocation(newCurrentLocation.copy(isCurrentLocation = true))
    }
}

@Dao
interface CacheMetadataDao {
    @Query("SELECT * FROM cache_metadata WHERE key = :key AND isValid = 1")
    suspend fun getCacheMetadata(key: String): CacheMetadataEntity?
    
    @Query("SELECT * FROM cache_metadata WHERE dataType = :dataType AND isValid = 1")
    suspend fun getCacheMetadataByType(dataType: String): List<CacheMetadataEntity>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCacheMetadata(metadata: CacheMetadataEntity)
    
    @Query("UPDATE cache_metadata SET isValid = 0 WHERE key = :key")
    suspend fun invalidateCache(key: String)
    
    @Query("UPDATE cache_metadata SET isValid = 0 WHERE dataType = :dataType")
    suspend fun invalidateCacheByType(dataType: String)
    
    @Query("UPDATE cache_metadata SET isValid = 0 WHERE expiresAt < :currentTime")
    suspend fun invalidateExpiredCache(currentTime: Long)
    
    @Query("DELETE FROM cache_metadata WHERE isValid = 0 AND lastUpdated < :timestamp")
    suspend fun deleteOldCacheMetadata(timestamp: Long)
    
    @Query("SELECT COUNT(*) FROM cache_metadata WHERE isValid = 1")
    suspend fun getValidCacheCount(): Int
}

/**
 * Combined DAO for complex queries across multiple tables
 */
@Dao
interface WeatherCombinedDao {
    /**
     * Get complete weather data with forecasts
     */
    @Transaction
    suspend fun getCompleteWeatherData(weatherDataId: String): CompleteWeatherData? {
        val weather = getWeatherById(weatherDataId) ?: return null
        val hourlyForecast = getHourlyForecast(weatherDataId)
        val dailyForecast = getDailyForecast(weatherDataId)
        val alerts = getActiveAlertsForLocation(weatherDataId)
        
        return CompleteWeatherData(
            weather = weather,
            hourlyForecast = hourlyForecast,
            dailyForecast = dailyForecast,
            alerts = alerts
        )
    }
    
    @Query("SELECT * FROM weather_data_v2 WHERE id = :id")
    suspend fun getWeatherById(id: String): WeatherDataEntity?
    
    @Query("SELECT * FROM hourly_forecast WHERE weatherDataId = :weatherDataId ORDER BY timestamp ASC")
    suspend fun getHourlyForecast(weatherDataId: String): List<HourlyForecastEntity>
    
    @Query("SELECT * FROM daily_forecast WHERE weatherDataId = :weatherDataId ORDER BY timestamp ASC")
    suspend fun getDailyForecast(weatherDataId: String): List<DailyForecastEntity>
    
    @Query("SELECT * FROM weather_alerts WHERE locationId = :locationId AND isActive = 1")
    suspend fun getActiveAlertsForLocation(locationId: String): List<WeatherAlertEntity>
    
    /**
     * Clean up old data across all tables
     */
    @Transaction
    suspend fun cleanupOldData(retentionTimestamp: Long) {
        // Delete old weather data
        deleteOldWeatherData(retentionTimestamp)
        
        // Delete old forecasts
        deleteOldHourlyData(retentionTimestamp)
        deleteOldDailyData(retentionTimestamp)
        
        // Delete old alerts
        deleteOldAlerts(retentionTimestamp)
        
        // Delete old cache metadata
        deleteOldCacheMetadata(retentionTimestamp)
    }
    
    @Query("DELETE FROM weather_data_v2 WHERE lastUpdated < :timestamp")
    suspend fun deleteOldWeatherData(timestamp: Long)
    
    @Query("DELETE FROM hourly_forecast WHERE timestamp < :timestamp")
    suspend fun deleteOldHourlyData(timestamp: Long)
    
    @Query("DELETE FROM daily_forecast WHERE timestamp < :timestamp")
    suspend fun deleteOldDailyData(timestamp: Long)
    
    @Query("DELETE FROM weather_alerts WHERE endTime < :timestamp AND isActive = 0")
    suspend fun deleteOldAlerts(timestamp: Long)
    
    @Query("DELETE FROM cache_metadata WHERE isValid = 0 AND lastUpdated < :timestamp")
    suspend fun deleteOldCacheMetadata(timestamp: Long)
}

/**
 * Data class for complete weather information
 */
data class CompleteWeatherData(
    val weather: WeatherDataEntity,
    val hourlyForecast: List<HourlyForecastEntity>,
    val dailyForecast: List<DailyForecastEntity>,
    val alerts: List<WeatherAlertEntity>
)