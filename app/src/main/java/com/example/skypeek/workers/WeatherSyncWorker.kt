package com.example.skypeek.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.example.skypeek.domain.repository.WeatherRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

@HiltWorker
class WeatherSyncWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val weatherRepository: WeatherRepository
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        return@withContext try {
            // Get locations from input data or default location
            val latitude = inputData.getDouble(KEY_LATITUDE, DEFAULT_LATITUDE)
            val longitude = inputData.getDouble(KEY_LONGITUDE, DEFAULT_LONGITUDE)
            
            // Fetch fresh weather data
            val result = weatherRepository.getWeatherData(
                latitude = latitude,
                longitude = longitude,
                forceRefresh = true
            )
            
            if (result.isSuccess) {
                // Update widgets if necessary
                updateWeatherWidgets()
                Result.success()
            } else {
                Result.retry()
            }
        } catch (e: Exception) {
            Result.failure()
        }
    }
    
    private fun updateWeatherWidgets() {
        // This would trigger widget updates
        // Implementation would depend on widget setup
    }

    companion object {
        const val WORK_NAME = "weather_sync_work"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val DEFAULT_LATITUDE = 37.3382 // San Jose
        private const val DEFAULT_LONGITUDE = -121.8863
        
        fun createWorkRequest(
            latitude: Double = DEFAULT_LATITUDE,
            longitude: Double = DEFAULT_LONGITUDE
        ): PeriodicWorkRequest {
            val inputData = Data.Builder()
                .putDouble(KEY_LATITUDE, latitude)
                .putDouble(KEY_LONGITUDE, longitude)
                .build()
            
            return PeriodicWorkRequestBuilder<WeatherSyncWorker>(
                repeatInterval = 30, // 30 minutes
                repeatIntervalTimeUnit = TimeUnit.MINUTES,
                flexTimeInterval = 10, // 10 minutes flex
                flexTimeIntervalUnit = TimeUnit.MINUTES
            )
                .setInputData(inputData)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .setRequiresBatteryNotLow(true)
                        .build()
                )
                .setBackoffCriteria(
                    BackoffPolicy.EXPONENTIAL,
                    WorkRequest.MIN_BACKOFF_MILLIS,
                    TimeUnit.MILLISECONDS
                )
                .addTag("weather_sync")
                .build()
        }
        
        fun scheduleWork(context: Context, workManager: WorkManager) {
            val workRequest = createWorkRequest()
            
            workManager.enqueueUniquePeriodicWork(
                WORK_NAME,
                ExistingPeriodicWorkPolicy.KEEP,
                workRequest
            )
        }
    }
} 