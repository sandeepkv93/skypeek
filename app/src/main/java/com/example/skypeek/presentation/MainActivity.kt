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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Initialize location client
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        
        setContent {
            SkyPeekTheme {
                WeatherApp(
                    viewModel = weatherViewModel,
                    onLocationRequest = { requestCurrentLocation() }
                )
            }
        }
    }
    
    private fun requestCurrentLocation() {
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
                weatherViewModel.updateCurrentLocation(it.latitude, it.longitude)
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
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    
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
    LaunchedEffect(locationPermissionState.status.isGranted) {
        if (locationPermissionState.status.isGranted) {
            // Permission granted, get current location
            onLocationRequest()
        } else if (screenState.showLocationDialog && !locationPermissionState.status.shouldShowRationale) {
            // Initial request
            locationPermissionState.launchPermissionRequest()
        }
    }
    
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
                    // TODO: Implement add location functionality
                },
                onManageLocations = {
                    // TODO: Implement manage locations functionality  
                },
                onSettings = {
                    // TODO: Implement settings screen
                },
                onAbout = {
                    // TODO: Implement about dialog
                }
            )
        }
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