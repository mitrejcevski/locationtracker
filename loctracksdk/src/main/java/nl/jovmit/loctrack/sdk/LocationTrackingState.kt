package nl.jovmit.loctrack.sdk

import nl.jovmit.loctrack.sdk.request.Location

sealed class LocationTrackingState {

    data object Idle : LocationTrackingState()

    data object MissingLocationPermissions : LocationTrackingState()

    data object Loading : LocationTrackingState()

    data class LocationLoaded(val location: Location) : LocationTrackingState()

    data object FetchLocationError : LocationTrackingState()

    data object SubmitLocationError : LocationTrackingState()

    data object LocationSubmitted : LocationTrackingState()
}