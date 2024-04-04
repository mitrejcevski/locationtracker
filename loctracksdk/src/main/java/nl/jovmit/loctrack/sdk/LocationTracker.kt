package nl.jovmit.loctrack.sdk

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import nl.jovmit.loctrack.sdk.request.CurrentLocationResult
import nl.jovmit.loctrack.sdk.request.Location
import nl.jovmit.loctrack.sdk.submit.LocationSubmitResult

/**
 * A lifecycle-aware component that leverages the [LocationTrackerConfig] and allows
 * regular location tracking updates and on-demand location update.
 *
 * The tracker is first checking the location permissions by using
 * the [LocationTrackerConfig.locationPermissionChecker]. When granted, it could request
 * [LocationTracker.startTrackingLocation], or [LocationTracker.requestCurrentLocation] respectively,
 * by using the [LocationTrackerConfig.locationFetcher]. Once a new location is being loaded,
 * it gets submitted to the [LocationTrackerConfig.locationSubmitter].
 *
 * The tracker does the location loading and the location submission operations in the background
 * thread, so that it doesn't block the main (UI) thread of the app.
 *
 * Additionally, the tracker exposes the current [LocationTrackingState] so that the user can
 * observe it and display different UI based on it.
 *
 */
class LocationTracker(
    private val configuration: LocationTrackerConfig,
    private val backgroundDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _locationTrackingState = MutableStateFlow<LocationTrackingState>(
        LocationTrackingState.Idle
    )
    val locationTrackingState = _locationTrackingState.asStateFlow()

    fun startTrackingLocation() {
        requireLocationPermissions {
            viewModelScope.launch {
                _locationTrackingState.update { LocationTrackingState.Loading }
                configuration.locationFetcher.requestLocationUpdates().onEach { location ->
                    submitNewLocation(location)
                }.stateIn(viewModelScope)
            }
        }
    }

    fun requestCurrentLocation() {
        requireLocationPermissions {
            viewModelScope.launch {
                _locationTrackingState.update { LocationTrackingState.Loading }
                val currentLocationResult = withContext(backgroundDispatcher) {
                    configuration.locationFetcher.requestCurrentLocation()
                }
                onCurrentLocationResult(currentLocationResult)
            }
        }
    }

    private fun onCurrentLocationResult(result: CurrentLocationResult) {
        when (result) {
            is CurrentLocationResult.Success -> submitNewLocation(result.location)
            is CurrentLocationResult.Failure -> onErrorFetchingCurrentLocation()
        }
    }

    private fun submitNewLocation(location: Location) {
        _locationTrackingState.update { LocationTrackingState.LocationLoaded(location) }
        viewModelScope.launch {
            val submitResult = withContext(backgroundDispatcher) {
                configuration.locationSubmitter.submitLocation(location)
            }
            onLocationSubmitResult(submitResult)
        }
    }

    private fun onErrorFetchingCurrentLocation() {
        _locationTrackingState.update { LocationTrackingState.FetchLocationError }
    }

    private fun onLocationSubmitResult(submitResult: LocationSubmitResult) {
        when (submitResult) {
            is LocationSubmitResult.Success -> onLocationSubmitted()
            is LocationSubmitResult.Failure -> onLocationSubmitFailure()
        }
    }

    private fun onLocationSubmitted() {
        _locationTrackingState.update { LocationTrackingState.LocationSubmitted }
    }

    private fun onLocationSubmitFailure() {
        _locationTrackingState.update { LocationTrackingState.SubmitLocationError }
    }

    private fun requireLocationPermissions(block: () -> Unit) {
        if (configuration.locationPermissionChecker.hasLocationPermissions()) {
            block()
        } else {
            onMissingLocationPermissions()
        }
    }

    private fun onMissingLocationPermissions() {
        _locationTrackingState.update { LocationTrackingState.MissingLocationPermissions }
    }
}