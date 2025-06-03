package com.example.skypeek.domain.repository

import com.example.skypeek.domain.model.LocationData

interface LocationRepository {
    
    /**
     * Get the current device location
     */
    suspend fun getCurrentLocation(): Result<LocationData>
    
    /**
     * Get all saved locations
     */
    suspend fun getSavedLocations(): List<LocationData>
    
    /**
     * Save a new location
     */
    suspend fun saveLocation(location: LocationData)
    
    /**
     * Remove a saved location
     */
    suspend fun removeLocation(location: LocationData)
    
    /**
     * Update the order of saved locations
     */
    suspend fun updateLocationOrder(locations: List<LocationData>)
    
    /**
     * Search for locations by name
     */
    suspend fun searchLocations(query: String): List<LocationData>
    
    /**
     * Reverse geocode coordinates to get location name
     */
    suspend fun reverseGeocode(latitude: Double, longitude: Double): Result<LocationData>
    
    /**
     * Check if location permissions are granted
     */
    fun hasLocationPermission(): Boolean
} 