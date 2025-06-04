package com.example.skypeek.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.skypeek.presentation.ui.WeatherUiState
import com.example.skypeek.presentation.ui.components.WeatherScreen
import com.example.skypeek.presentation.ui.theme.SkyPeekTheme
import com.example.skypeek.presentation.viewmodel.WeatherViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.accompanist.permissions.shouldShowRationale
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import androidx.compose.ui.unit.dp
import com.example.skypeek.presentation.ui.components.SettingsBottomSheet
import com.example.skypeek.presentation.ui.components.LocationSearchScreen
import com.example.skypeek.presentation.ui.components.ManageLocationsScreen
import com.example.skypeek.presentation.ui.components.SettingsScreen
import com.example.skypeek.presentation.ui.components.AboutDialog
import com.example.skypeek.domain.model.LocationData

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        // Reset widgets to use current location and trigger refresh when app starts
        try {
            com.example.skypeek.widgets.WeatherWidgetService.resetWidgetsToCurrentLocation(this)
        } catch (e: Exception) {
            // Widget reset failed, but don't crash the app - try regular update
            try {
                com.example.skypeek.widgets.WeatherWidgetService.updateAllWidgets(this)
            } catch (e2: Exception) {
                // Widget update also failed, but don't crash the app
            }
        }
        
        setContent {
            SkyPeekTheme {
                WeatherApp(
                    viewModel = weatherViewModel,
                    onLocationRequest = { getCurrentLocation() }
                )
            }
        }
    }
    
    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permission not granted, should be handled by the UI
            return
        }
        
        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            null
        ).addOnSuccessListener { location: Location? ->
            location?.let {
                weatherViewModel.updateCurrentLocation(this@MainActivity, it.latitude, it.longitude)
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class, ExperimentalPermissionsApi::class)
@Composable
fun WeatherApp(
    viewModel: WeatherViewModel,
    onLocationRequest: () -> Unit
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val showMenu by viewModel.showMenu.collectAsStateWithLifecycle()
    val savedLocations by viewModel.savedLocations.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
    // Navigation state
    var currentScreen by remember { mutableStateOf("weather") }
    var showAboutDialog by remember { mutableStateOf(false) }
    var searchResults by remember { mutableStateOf<List<LocationData>>(emptyList()) }
    var isSearching by remember { mutableStateOf(false) }
    
    // Location permission handling
    val locationPermissionState = rememberPermissionState(
        Manifest.permission.ACCESS_FINE_LOCATION
    ) { isGranted ->
        viewModel.setPermissionGranted(isGranted)
        if (isGranted) {
            onLocationRequest()
        } else {
            // Permission denied, fallback to default location
            viewModel.loadDefaultLocationAsFallback()
        }
    }
    
    // Pager state for multiple locations
    val pagerState = rememberPagerState(
        initialPage = screenState.currentLocationIndex,
        pageCount = { maxOf(screenState.weatherStates.size, 1) }
    )
    
    // Sync pager with view model
    LaunchedEffect(pagerState.currentPage) {
        viewModel.setCurrentLocationIndex(pagerState.currentPage)
    }
    
    LaunchedEffect(screenState.currentLocationIndex) {
        if (pagerState.currentPage != screenState.currentLocationIndex) {
            pagerState.animateScrollToPage(screenState.currentLocationIndex)
        }
    }
    
    // Request location permission on first launch or handle denied permission
    LaunchedEffect(Unit) {
        if (locationPermissionState.status.isGranted) {
            // Permission already granted, get current location
            onLocationRequest()
        } else if (screenState.showLocationDialog) {
            // Only show permission dialog if explicitly requested
            locationPermissionState.launchPermissionRequest()
        } else {
            // Permission not granted and dialog not requested, use default location
            viewModel.loadDefaultLocationAsFallback()
        }
    }
    
    when (currentScreen) {
        "weather" -> {
            // Existing weather screen code
            Box(modifier = Modifier.fillMaxSize()) {
                if (screenState.weatherStates.isEmpty()) {
                    // Loading initial state
                    LoadingScreen()
                } else {
                    // Weather pager
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        when (val weatherState = screenState.weatherStates.getOrNull(page)) {
                            is WeatherUiState.Loading -> LoadingScreen()
                            is WeatherUiState.Success -> {
                                WeatherScreen(
                                    weatherData = weatherState.weather,
                                    onRefresh = { viewModel.refreshWeatherAtIndex(page) },
                                    onMapClick = { /* Handle map click */ },
                                    onMenuClick = { viewModel.toggleMenu() }
                                )
                            }
                            is WeatherUiState.Error -> {
                                ErrorScreen(
                                    message = weatherState.message,
                                    onRetry = { viewModel.refreshWeatherAtIndex(page) }
                                )
                            }
                            null -> LoadingScreen()
                        }
                    }
                }
                
                // Location permission dialog
                if (screenState.showLocationDialog) {
                    LocationPermissionDialog(
                        onGrantPermission = {
                            viewModel.setShowLocationDialog(false)
                            locationPermissionState.launchPermissionRequest()
                        },
                        onDismiss = {
                            viewModel.setShowLocationDialog(false)
                        }
                    )
                }
                
                // Pull to refresh overlay
                if (screenState.isRefreshing) {
                    RefreshIndicator(
                        modifier = Modifier.align(Alignment.TopCenter)
                    )
                }
                
                // Settings bottom sheet
                if (showMenu) {
                    SettingsBottomSheet(
                        onDismiss = { viewModel.hideMenu() },
                        onAddLocation = {
                            currentScreen = "add_location"
                            viewModel.hideMenu()
                        },
                        onManageLocations = {
                            currentScreen = "manage_locations"
                            viewModel.hideMenu()
                        },
                        onSettings = {
                            currentScreen = "settings"
                            viewModel.hideMenu()
                        },
                        onAbout = {
                            showAboutDialog = true
                            viewModel.hideMenu()
                        }
                    )
                }
            }
        }
        
        "add_location" -> {
            LocationSearchScreen(
                onLocationSelected = { location ->
                    viewModel.addLocation(location)
                    currentScreen = "weather"
                },
                onNavigateBack = { currentScreen = "weather" },
                onSearchLocations = { query ->
                    isSearching = true
                    coroutineScope.launch {
                        try {
                            val results = viewModel.searchLocations(query)
                            searchResults = results
                        } catch (e: Exception) {
                            searchResults = emptyList()
                        } finally {
                            isSearching = false
                        }
                    }
                },
                searchResults = searchResults,
                isSearching = isSearching
            )
        }
        
        "manage_locations" -> {
            ManageLocationsScreen(
                locations = savedLocations,
                onNavigateBack = { currentScreen = "weather" },
                onRemoveLocation = { index ->
                    viewModel.removeLocationAtIndex(index)
                }
            )
        }
        
        "settings" -> {
            SettingsScreen(
                onNavigateBack = { currentScreen = "weather" }
            )
        }
    }
    
    // About dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Loading weather data...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "⚠️",
                style = MaterialTheme.typography.displayMedium
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Unable to load weather",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun LocationPermissionDialog(
    onGrantPermission: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Location Permission")
        },
        text = {
            Text("SkyPeek needs location access to provide accurate weather information for your current location.")
        },
        confirmButton = {
            TextButton(onClick = onGrantPermission) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RefreshIndicator(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 60.dp), // Status bar space
        contentAlignment = Alignment.Center
    ) {
        Card(
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
            )
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(16.dp),
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Updating...",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
} 