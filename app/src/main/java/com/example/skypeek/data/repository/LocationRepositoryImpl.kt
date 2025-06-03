package com.example.skypeek.data.repository

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.app.ActivityCompat
import com.example.skypeek.data.local.database.LocationDao
import com.example.skypeek.data.local.entities.SavedLocationEntity
import com.example.skypeek.domain.model.LocationData
import com.example.skypeek.domain.repository.LocationRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume

@Singleton
class LocationRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationDao: LocationDao,
    private val fusedLocationClient: FusedLocationProviderClient,
    private val geocoder: Geocoder
) : LocationRepository {

    override suspend fun getCurrentLocation(): Result<LocationData> {
        return withContext(Dispatchers.IO) {
            try {
                if (!hasLocationPermission()) {
                    return@withContext Result.failure(Exception("Location permission not granted"))
                }

                val location = suspendCancellableCoroutine<android.location.Location?> { continuation ->
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                            context,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        continuation.resume(null)
                        return@suspendCancellableCoroutine
                    }

                    fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                        .addOnSuccessListener { location ->
                            continuation.resume(location)
                        }
                        .addOnFailureListener { exception ->
                            continuation.resume(null)
                        }
                }

                if (location != null) {
                    val locationData = reverseGeocode(location.latitude, location.longitude)
                    locationData.fold(
                        onSuccess = { data ->
                            Result.success(data.copy(isCurrentLocation = true))
                        },
                        onFailure = {
                            Result.success(
                                LocationData(
                                    latitude = location.latitude,
                                    longitude = location.longitude,
                                    cityName = "Current Location",
                                    country = "",
                                    isCurrentLocation = true
                                )
                            )
                        }
                    )
                } else {
                    Result.failure(Exception("Unable to get current location"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override suspend fun getSavedLocations(): List<LocationData> {
        return withContext(Dispatchers.IO) {
            try {
                locationDao.getAllSavedLocations().map { entity ->
                    LocationData(
                        latitude = entity.latitude,
                        longitude = entity.longitude,
                        cityName = entity.cityName,
                        country = entity.country,
                        isCurrentLocation = entity.isCurrentLocation
                    )
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun saveLocation(location: LocationData) {
        withContext(Dispatchers.IO) {
            try {
                val entity = SavedLocationEntity(
                    id = "${location.latitude}_${location.longitude}",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = location.cityName,
                    country = location.country,
                    isCurrentLocation = location.isCurrentLocation,
                    order = locationDao.getAllSavedLocations().size
                )
                locationDao.insertLocation(entity)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override suspend fun removeLocation(location: LocationData) {
        withContext(Dispatchers.IO) {
            try {
                val entity = SavedLocationEntity(
                    id = "${location.latitude}_${location.longitude}",
                    latitude = location.latitude,
                    longitude = location.longitude,
                    cityName = location.cityName,
                    country = location.country,
                    isCurrentLocation = location.isCurrentLocation,
                    order = 0
                )
                locationDao.deleteLocation(entity)
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override suspend fun updateLocationOrder(locations: List<LocationData>) {
        withContext(Dispatchers.IO) {
            try {
                locations.forEachIndexed { index, location ->
                    locationDao.updateLocationOrder(
                        "${location.latitude}_${location.longitude}",
                        index
                    )
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    override suspend fun searchLocations(query: String): List<LocationData> {
        return withContext(Dispatchers.IO) {
            try {
                if (query.length < 3) return@withContext emptyList()

                val addresses = geocoder.getFromLocationName(query, 10)
                addresses?.mapNotNull { address ->
                    if (address.hasLatitude() && address.hasLongitude()) {
                        LocationData(
                            latitude = address.latitude,
                            longitude = address.longitude,
                            cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown",
                            country = address.countryName ?: "",
                            isCurrentLocation = false
                        )
                    } else null
                } ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<LocationData> {
        return withContext(Dispatchers.IO) {
            try {
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                if (addresses?.isNotEmpty() == true) {
                    val address = addresses[0]
                    Result.success(
                        LocationData(
                            latitude = latitude,
                            longitude = longitude,
                            cityName = address.locality ?: address.subAdminArea ?: address.adminArea ?: "Unknown",
                            country = address.countryName ?: "",
                            isCurrentLocation = false
                        )
                    )
                } else {
                    Result.failure(Exception("No address found for coordinates"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    override fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }
} 