package com.example.skypeek.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.skypeek.domain.model.LocationData
import com.example.skypeek.domain.repository.WeatherRepository
import com.example.skypeek.domain.repository.LocationRepository
import com.example.skypeek.presentation.ui.WeatherScreenState
import com.example.skypeek.presentation.ui.WeatherUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class WeatherViewModel @Inject constructor(
    private val weatherRepository: WeatherRepository,
    private val locationRepository: LocationRepository
) : ViewModel() {

    private val _screenState = MutableStateFlow(WeatherScreenState())
    val screenState: StateFlow<WeatherScreenState> = _screenState.asStateFlow()

    private val _savedLocations = MutableStateFlow<List<LocationData>>(emptyList())
    val savedLocations: StateFlow<List<LocationData>> = _savedLocations.asStateFlow()

    private val _showMenu = MutableStateFlow(false)
    val showMenu: StateFlow<Boolean> = _showMenu.asStateFlow()

    init {
        // Load saved locations on app startup
        loadSavedLocations()
    }
    
    /**
     * Load saved locations from database on app startup
     */
    private fun loadSavedLocations() {
        viewModelScope.launch {
            try {
                val savedLocations = locationRepository.getSavedLocations()
                if (savedLocations.isNotEmpty()) {
                    loadWeatherForLocations(savedLocations)
                } else {
                    // No saved locations, try to initialize with current location
                    initializeWithLocationPermission()
                }
            } catch (e: Exception) {
                // If loading saved locations fails, fallback to current location
                initializeWithLocationPermission()
            }
        }
    }

    /**
     * Initialize app with current location if permission granted, otherwise show loading
     */
    fun initializeWithLocationPermission() {
        viewModelScope.launch {
            // Check if we have location permission
            if (locationRepository.hasLocationPermission()) {
                // Try to get current location
                val locationResult = locationRepository.getCurrentLocation()
                if (locationResult.isSuccess) {
                    val currentLocation = locationResult.getOrThrow()
                    loadWeatherForLocations(listOf(currentLocation))
                } else {
                    // Failed to get current location, use default
                    loadDefaultLocation()
                }
            } else {
                // No permission, show initial loading state and wait for permission result
                _screenState.value = _screenState.value.copy(
                    weatherStates = listOf(WeatherUiState.Loading),
                    isRefreshing = false
                )
            }
        }
    }

    /**
     * Load weather for the default location (San Jose) - only as fallback
     */
    private fun loadDefaultLocation() {
        val defaultLocation = LocationData(
            latitude = 37.3382,
            longitude = -121.8863,
            cityName = "San Jose",
            country = "United States",
            isCurrentLocation = false
        )
        
        loadWeatherForLocations(listOf(defaultLocation))
    }

    /**
     * Load default location as fallback when location permission is denied
     */
    fun loadDefaultLocationAsFallback() {
        viewModelScope.launch {
            _screenState.value = _screenState.value.copy(showLocationDialog = false)
            loadDefaultLocation()
        }
    }

    /**
     * Load weather data for multiple locations
     */
    fun loadWeatherForLocations(locations: List<LocationData>) {
        viewModelScope.launch {
            _savedLocations.value = locations
            
            // Initialize with loading states
            val loadingStates = locations.map { WeatherUiState.Loading }
            _screenState.value = _screenState.value.copy(
                weatherStates = loadingStates,
                isRefreshing = false
            )

            // Load weather for each location
            locations.forEachIndexed { index, location ->
                loadWeatherForLocation(location, index)
            }
        }
    }

    /**
     * Load weather for a specific location at given index
     */
    private suspend fun loadWeatherForLocation(location: LocationData, index: Int) {
        try {
            val result = weatherRepository.getWeatherData(
                latitude = location.latitude,
                longitude = location.longitude,
                forceRefresh = false
            )

            result.fold(
                onSuccess = { weatherData ->
                    // Update the city name from the weather data if available
                    val updatedWeatherData = if (weatherData.location.cityName != "Loading...") {
                        weatherData
                    } else {
                        weatherData.copy(
                            location = weatherData.location.copy(
                                cityName = location.cityName,
                                country = location.country
                            )
                        )
                    }
                    updateWeatherStateAtIndex(index, WeatherUiState.Success(updatedWeatherData))
                },
                onFailure = { error ->
                    updateWeatherStateAtIndex(index, WeatherUiState.Error(error.message ?: "Unknown error"))
                }
            )
        } catch (e: Exception) {
            updateWeatherStateAtIndex(index, WeatherUiState.Error(e.message ?: "Failed to load weather"))
        }
    }

    /**
     * Update weather state at specific index
     */
    private fun updateWeatherStateAtIndex(index: Int, newState: WeatherUiState) {
        val currentStates = _screenState.value.weatherStates.toMutableList()
        if (index < currentStates.size) {
            currentStates[index] = newState
            _screenState.value = _screenState.value.copy(weatherStates = currentStates)
        }
    }

    /**
     * Refresh weather data for all locations
     */
    fun refreshAllWeather() {
        viewModelScope.launch {
            _screenState.value = _screenState.value.copy(isRefreshing = true)
            
            val locations = _savedLocations.value
            locations.forEachIndexed { index, location ->
                try {
                    val result = weatherRepository.getWeatherData(
                        latitude = location.latitude,
                        longitude = location.longitude,
                        forceRefresh = true
                    )

                    result.fold(
                        onSuccess = { weatherData ->
                            val updatedWeatherData = weatherData.copy(
                                location = weatherData.location.copy(
                                    cityName = if (weatherData.location.cityName == "Loading...") location.cityName else weatherData.location.cityName,
                                    country = if (weatherData.location.country.isEmpty()) location.country else weatherData.location.country
                                )
                            )
                            updateWeatherStateAtIndex(index, WeatherUiState.Success(updatedWeatherData))
                        },
                        onFailure = { error ->
                            updateWeatherStateAtIndex(index, WeatherUiState.Error(error.message ?: "Unknown error"))
                        }
                    )
                } catch (e: Exception) {
                    updateWeatherStateAtIndex(index, WeatherUiState.Error(e.message ?: "Failed to refresh weather"))
                }
            }
            
            _screenState.value = _screenState.value.copy(isRefreshing = false)
        }
    }

    /**
     * Refresh weather for specific location index
     */
    fun refreshWeatherAtIndex(index: Int) {
        viewModelScope.launch {
            val locations = _savedLocations.value
            if (index < locations.size) {
                updateWeatherStateAtIndex(index, WeatherUiState.Loading)
                loadWeatherForLocation(locations[index], index)
            }
        }
    }

    /**
     * Set current location index for pager
     */
    fun setCurrentLocationIndex(index: Int) {
        _screenState.value = _screenState.value.copy(currentLocationIndex = index)
    }

    /**
     * Add a new location and save it to database
     */
    fun addLocation(location: LocationData) {
        viewModelScope.launch {
            val currentLocations = _savedLocations.value.toMutableList()
            if (!currentLocations.any { it.latitude == location.latitude && it.longitude == location.longitude }) {
                try {
                    // Save location to database first
                    locationRepository.saveLocation(location)
                    
                    // Then update UI state
                    currentLocations.add(location)
                    loadWeatherForLocations(currentLocations)
                } catch (e: Exception) {
                    // Handle save error - could show user feedback here
                    // For now, still update UI to prevent confusing behavior
                    currentLocations.add(location)
                    loadWeatherForLocations(currentLocations)
                }
            }
        }
    }

    /**
     * Remove location at index and delete from database
     */
    fun removeLocationAtIndex(index: Int) {
        viewModelScope.launch {
            val currentLocations = _savedLocations.value.toMutableList()
            if (index < currentLocations.size) {
                val locationToRemove = currentLocations[index]
                
                try {
                    // Remove from database first
                    locationRepository.removeLocation(locationToRemove)
                } catch (e: Exception) {
                    // Handle removal error - continue with UI update anyway
                }
                
                // Update UI state
                currentLocations.removeAt(index)
                
                if (currentLocations.isEmpty()) {
                    // Load default location if no locations left
                    loadDefaultLocation()
                } else {
                    loadWeatherForLocations(currentLocations)
                    
                    // Adjust current index if needed
                    val currentIndex = _screenState.value.currentLocationIndex
                    if (currentIndex >= currentLocations.size) {
                        _screenState.value = _screenState.value.copy(
                            currentLocationIndex = currentLocations.size - 1
                        )
                    }
                }
            }
        }
    }

    /**
     * Update current location with GPS coordinates
     */
    fun updateCurrentLocation(context: android.content.Context, latitude: Double, longitude: Double) {
        viewModelScope.launch {
            // Use LocationRepository to get actual city name via reverse geocoding
            val locationResult = locationRepository.reverseGeocode(latitude, longitude)
            
            val currentLocation = locationResult.fold(
                onSuccess = { geocodedLocation ->
                    geocodedLocation.copy(isCurrentLocation = true)
                },
                onFailure = {
                    LocationData(
                        latitude = latitude,
                        longitude = longitude,
                        cityName = "Current Location",
                        country = "",
                        isCurrentLocation = true
                    )
                }
            )
            
            val currentLocations = _savedLocations.value.toMutableList()
            
            // Remove existing current location and add new one at the beginning
            val oldCurrentLocations = currentLocations.filter { it.isCurrentLocation }
            currentLocations.removeAll { it.isCurrentLocation }
            currentLocations.add(0, currentLocation)
            
            try {
                // Remove old current locations from database
                oldCurrentLocations.forEach { oldLocation ->
                    locationRepository.removeLocation(oldLocation)
                }
                
                // Save new current location to database
                locationRepository.saveLocation(currentLocation)
            } catch (e: Exception) {
                // Handle database error - continue with UI update anyway
            }
            
            loadWeatherForLocations(currentLocations)
            _screenState.value = _screenState.value.copy(currentLocationIndex = 0)
            
            // Update widgets that use current location
            try {
                com.example.skypeek.widgets.WeatherWidgetService.updateWidgetLocation(
                    context,
                    currentLocation.latitude,
                    currentLocation.longitude,
                    currentLocation.cityName
                )
            } catch (e: Exception) {
                // Widget update failed, but don't crash the app
            }
        }
    }

    /**
     * Set permission granted state
     */
    fun setPermissionGranted(granted: Boolean) {
        _screenState.value = _screenState.value.copy(permissionGranted = granted)
    }

    /**
     * Show/hide location permission dialog
     */
    fun setShowLocationDialog(show: Boolean) {
        _screenState.value = _screenState.value.copy(showLocationDialog = show)
    }

    /**
     * Search for locations by name
     */
    suspend fun searchLocations(query: String): List<LocationData> {
        return try {
            locationRepository.searchLocations(query)
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Clear cache and refresh
     */
    fun clearCacheAndRefresh() {
        viewModelScope.launch {
            weatherRepository.clearCache()
            refreshAllWeather()
        }
    }

    /**
     * Show/hide menu
     */
    fun toggleMenu() {
        _showMenu.value = !_showMenu.value
    }
    
    fun hideMenu() {
        _showMenu.value = false
    }
    
    /**
     * Navigate to current location (home page)
     */
    fun navigateToCurrentLocation() {
        viewModelScope.launch {
            val locations = _savedLocations.value
            val currentLocationIndex = locations.indexOfFirst { it.isCurrentLocation }
            if (currentLocationIndex != -1) {
                // Set flag to indicate immediate navigation (snap) and update index
                _screenState.value = _screenState.value.copy(
                    currentLocationIndex = currentLocationIndex,
                    shouldSnapToPage = true
                )
            }
        }
    }
    
    /**
     * Clear the snap to page flag after navigation completes
     */
    fun clearSnapToPageFlag() {
        _screenState.value = _screenState.value.copy(shouldSnapToPage = false)
    }

    /**
     * Request location permission and update current location
     */
    fun requestLocationPermission() {
        _screenState.value = _screenState.value.copy(showLocationDialog = true)
    }
} 