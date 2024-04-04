package nl.jovmit.loctrack.sdk

import android.content.Context
import kotlinx.coroutines.flow.emptyFlow
import nl.jovmit.loctrack.sdk.permissions.DeviceLocationPermissionChecker
import nl.jovmit.loctrack.sdk.permissions.LocationPermissionChecker
import nl.jovmit.loctrack.sdk.request.CurrentLocationResult
import nl.jovmit.loctrack.sdk.request.FusedLocationFetcher
import nl.jovmit.loctrack.sdk.request.Location
import nl.jovmit.loctrack.sdk.request.LocationFetcher
import nl.jovmit.loctrack.sdk.submit.LocationSubmitResult
import nl.jovmit.loctrack.sdk.submit.LocationSubmitter
import nl.jovmit.loctrack.sdk.submit.RestLocationSubmitter

/**
 * LocationTrackerConfiguration used to construct the [LocationTracker].
 *
 * This configuration holds instances of [LocationPermissionChecker], [LocationFetcher],
 * and [LocationSubmitter].
 *
 * Creation of new instances of this class is only possible by using the [LocationTrackerConfig.Builder],
 * which allows passing different implementations of the required interfaces, to better support
 * configurability and testability.
 *
 * @see [LocationTrackerConfig.Builder]
 */
class LocationTrackerConfig private constructor(
    val locationPermissionChecker: LocationPermissionChecker,
    val locationFetcher: LocationFetcher,
    val locationSubmitter: LocationSubmitter
) {

    /**
     * A builder for creating [LocationTrackerConfig]. By default, it uses an empty implementations
     * of the corresponding interfaces which are not functional. By using this builder, the user
     * can supply their own implementation of either [LocationPermissionChecker], [LocationFetcher],
     * and [LocationSubmitter], or they can choose to use the default SDK implementation which
     * internally is using Android based components, by using the [Builder.useDefaultAndroidComponents]
     * function.
     */
    class Builder {

        private var locationPermissionChecker: LocationPermissionChecker = EmptyPermissionChecker()
        private var locationFetcher: LocationFetcher = EmptyLocationFetcher()
        private var locationSubmitter: LocationSubmitter = EmptyLocationSubmitter()

        fun setLocationPermissionChecker(checker: LocationPermissionChecker) = apply {
            this.locationPermissionChecker = checker
        }

        fun setLocationFetcher(locationFetcher: LocationFetcher) = apply {
            this.locationFetcher = locationFetcher
        }

        fun setLocationSubmitter(locationSubmitter: LocationSubmitter) = apply {
            this.locationSubmitter = locationSubmitter
        }

        fun useDefaultAndroidComponents(context: Context) = apply {
            this.locationPermissionChecker = DeviceLocationPermissionChecker(context)
            this.locationFetcher = FusedLocationFetcher(context)
            this.locationSubmitter = RestLocationSubmitter(context)
        }

        fun build(): LocationTrackerConfig {
            return LocationTrackerConfig(
                locationPermissionChecker,
                locationFetcher,
                locationSubmitter
            )
        }

        private class EmptyPermissionChecker : LocationPermissionChecker {
            override fun hasLocationPermissions() = false
        }

        private class EmptyLocationFetcher : LocationFetcher {
            override fun requestLocationUpdates() = emptyFlow<Location>()
            override suspend fun requestCurrentLocation() = CurrentLocationResult.Failure
        }

        private class EmptyLocationSubmitter : LocationSubmitter {
            override suspend fun submitLocation(location: Location) = LocationSubmitResult.Failure
        }
    }
}