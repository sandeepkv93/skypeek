package com.example.skypeek.presentation.ui

import com.example.skypeek.domain.model.WeatherData

/**
 * UI state for weather screens
 */
sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val weather: WeatherData) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

/**
 * UI state for location search
 */
sealed class LocationSearchState {
    object Idle : LocationSearchState()
    object Loading : LocationSearchState()
    data class Success(val locations: List<com.example.skypeek.domain.model.LocationData>) : LocationSearchState()
    data class Error(val message: String) : LocationSearchState()
}

/**
 * UI state for multiple weather locations
 */
data class WeatherScreenState(
    val weatherStates: List<WeatherUiState> = emptyList(),
    val currentLocationIndex: Int = 0,
    val isRefreshing: Boolean = false,
    val permissionGranted: Boolean = false,
    val showLocationDialog: Boolean = false,
    val shouldSnapToPage: Boolean = false
) 