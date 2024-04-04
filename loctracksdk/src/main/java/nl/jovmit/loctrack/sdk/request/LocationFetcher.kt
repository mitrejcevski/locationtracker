package nl.jovmit.loctrack.sdk.request

import kotlinx.coroutines.flow.Flow

/**
 * Fetcher interface defining ways to request location. It supports
 * regular location updates, and on demand location requesting.
 */
interface LocationFetcher {

    fun requestLocationUpdates(): Flow<Location>

    suspend fun requestCurrentLocation(): CurrentLocationResult
}