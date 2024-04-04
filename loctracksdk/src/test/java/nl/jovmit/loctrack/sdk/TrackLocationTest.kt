package nl.jovmit.loctrack.sdk

import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import nl.jovmit.loctrack.sdk.permissions.LocationPermissionChecker
import nl.jovmit.loctrack.sdk.request.CurrentLocationResult
import nl.jovmit.loctrack.sdk.request.Location
import nl.jovmit.loctrack.sdk.request.LocationFetcher
import nl.jovmit.loctrack.sdk.submit.LocationSubmitResult
import nl.jovmit.loctrack.sdk.submit.LocationSubmitter
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(CoroutineTestExtension::class)
class TrackLocationTest {

    private val backgroundDispatcher = Dispatchers.Unconfined
    private val grantedPermissionsChecker = StubPermissionChecker(granted = true)
    private val deniedPermissionsChecker = StubPermissionChecker(granted = false)
    private val unavailableLocationFetcher = PredefinedLocationFetcher(
        emptyList(),
        CurrentLocationResult.Failure
    )
    private val locationUpdates = listOf(
        Location(latitude = 0.1, longitude = 0.2),
        Location(latitude = 0.2, longitude = 0.3)
    )
    private val currentLocation = Location(latitude = 1.0, longitude = 2.0)
    private val locationFetcher = PredefinedLocationFetcher(
        expectedLocations = locationUpdates,
        currentLocation = CurrentLocationResult.Success(
            currentLocation
        )
    )
    private val locationSubmitter = InMemoryLocationSubmitter()
    private val serverUnavailableSubmitter = InMemoryLocationSubmitter(isNotAvailable = true)
    private val defaultConfig = LocationTrackerConfig.Builder()
        .setLocationPermissionChecker(grantedPermissionsChecker)
        .setLocationFetcher(locationFetcher)
        .setLocationSubmitter(locationSubmitter)
        .build()

    @Test
    fun defaultTrackerState() {
        val tracker = LocationTracker(defaultConfig, backgroundDispatcher)

        assertThat(tracker.locationTrackingState.value)
            .isEqualTo(LocationTrackingState.Idle)
    }

    @Test
    fun startTrackingWithoutPermissions() {
        val configWithoutLocationPermissions = LocationTrackerConfig.Builder()
            .setLocationPermissionChecker(deniedPermissionsChecker)
            .build()
        val tracker = LocationTracker(configWithoutLocationPermissions, backgroundDispatcher)

        tracker.startTrackingLocation()

        assertThat(tracker.locationTrackingState.value)
            .isEqualTo(LocationTrackingState.MissingLocationPermissions)
    }

    @Test
    fun submitTrackedLocations() {
        val tracker = LocationTracker(defaultConfig, backgroundDispatcher)

        tracker.startTrackingLocation()

        assertThat(locationSubmitter.submittedLocations)
            .isEqualTo(locationUpdates)
    }

    @Test
    fun errorSubmittingLocation() {
        val configWithBrokenSubmitter = LocationTrackerConfig.Builder()
            .setLocationPermissionChecker(grantedPermissionsChecker)
            .setLocationFetcher(locationFetcher)
            .setLocationSubmitter(serverUnavailableSubmitter)
            .build()
        val tracker = LocationTracker(configWithBrokenSubmitter, backgroundDispatcher)

        tracker.startTrackingLocation()

        assertThat(tracker.locationTrackingState.value)
            .isEqualTo(LocationTrackingState.SubmitLocationError)
    }

    @Test
    fun fetchOnDemandLocationWithoutPermissions() {
        val configWithoutLocationPermissions = LocationTrackerConfig.Builder()
            .setLocationPermissionChecker(deniedPermissionsChecker)
            .build()
        val tracker = LocationTracker(configWithoutLocationPermissions, backgroundDispatcher)

        tracker.requestCurrentLocation()

        assertThat(tracker.locationTrackingState.value)
            .isEqualTo(LocationTrackingState.MissingLocationPermissions)
    }

    @Test
    fun locationFetchingFailure() {
        val configWithUnavailableLocationServices = LocationTrackerConfig.Builder()
            .setLocationPermissionChecker(grantedPermissionsChecker)
            .setLocationFetcher(unavailableLocationFetcher)
            .build()
        val tracker = LocationTracker(configWithUnavailableLocationServices, backgroundDispatcher)

        tracker.requestCurrentLocation()

        assertThat(tracker.locationTrackingState.value)
            .isEqualTo(LocationTrackingState.FetchLocationError)
    }

    @Test
    fun submitOnDemandLocation() {
        val tracker = LocationTracker(defaultConfig, backgroundDispatcher)

        tracker.requestCurrentLocation()

        assertThat(locationSubmitter.submittedLocations)
            .isEqualTo(listOf(currentLocation))
    }

    @Test
    fun errorSubmittingOnDemandLocation() {
        val configWithBrokenSubmitter = LocationTrackerConfig.Builder()
            .setLocationPermissionChecker(grantedPermissionsChecker)
            .setLocationFetcher(locationFetcher)
            .setLocationSubmitter(serverUnavailableSubmitter)
            .build()
        val tracker = LocationTracker(configWithBrokenSubmitter, backgroundDispatcher)

        tracker.requestCurrentLocation()

        assertThat(tracker.locationTrackingState.value)
            .isEqualTo(LocationTrackingState.SubmitLocationError)
    }

    class StubPermissionChecker(
        private val granted: Boolean
    ) : LocationPermissionChecker {
        override fun hasLocationPermissions() = granted
    }

    class PredefinedLocationFetcher(
        private val expectedLocations: List<Location>,
        private val currentLocation: CurrentLocationResult
    ) : LocationFetcher {

        override fun requestLocationUpdates(): Flow<Location> {
            return expectedLocations.asFlow()
        }

        override suspend fun requestCurrentLocation(): CurrentLocationResult {
            return currentLocation
        }
    }

    class InMemoryLocationSubmitter(
        private val isNotAvailable: Boolean = false
    ) : LocationSubmitter {

        private val _submittedLocations = mutableListOf<Location>()
        val submittedLocations get() = _submittedLocations.toList()

        override suspend fun submitLocation(location: Location): LocationSubmitResult {
            if (isNotAvailable) return LocationSubmitResult.Failure
            _submittedLocations.add(location)
            return LocationSubmitResult.Success("")
        }
    }
}