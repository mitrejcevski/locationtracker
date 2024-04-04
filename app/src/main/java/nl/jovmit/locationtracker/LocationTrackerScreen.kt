package nl.jovmit.locationtracker

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import nl.jovmit.locationtracker.ui.theme.LocationTrackerTheme
import nl.jovmit.loctrack.sdk.LocationTracker
import nl.jovmit.loctrack.sdk.LocationTrackingState

@Composable
fun LocationTrackerScreen(
    locationTracker: LocationTracker
) {
    val state by locationTracker.locationTrackingState.collectAsStateWithLifecycle()

    val backgroundLocationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) {
                locationTracker.startTrackingLocation()
            }
        }
    )
    val basicLocationPermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissions ->
            val granted = permissions.values.reduce { acc, isGranted -> acc && isGranted }
            if (granted) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    backgroundLocationPermissionLauncher.launch(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    )
                } else {
                    locationTracker.startTrackingLocation()
                }
            }
        }
    )

    LaunchedEffect(state) {
        if (state == LocationTrackingState.MissingLocationPermissions) {
            basicLocationPermissionsLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    LocationTrackerScreenContent(
        screenState = state,
        onStartLocationTracking = locationTracker::startTrackingLocation,
        onRequestCurrentLocation = locationTracker::requestCurrentLocation
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationTrackerScreenContent(
    screenState: LocationTrackingState,
    onStartLocationTracking: () -> Unit,
    onRequestCurrentLocation: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text(text = "Location Tracker Demo") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(text = screenState.toString())
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(onClick = onStartLocationTracking) {
                    Text(text = "Start Location Tracking")
                }
                Button(onClick = onRequestCurrentLocation) {
                    Text(text = "Request Current Location")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewLocationTrackerScreen() {
    LocationTrackerTheme {
        LocationTrackerScreenContent(
            screenState = LocationTrackingState.Idle,
            onStartLocationTracking = {},
            onRequestCurrentLocation = {}
        )
    }
}