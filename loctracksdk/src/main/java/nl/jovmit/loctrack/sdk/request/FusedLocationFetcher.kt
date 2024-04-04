package nl.jovmit.loctrack.sdk.request

import android.annotation.SuppressLint
import android.content.Context
import android.os.Looper
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

@SuppressLint("MissingPermission")
internal class FusedLocationFetcher(context: Context) : LocationFetcher {

    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    override fun requestLocationUpdates(): Flow<Location> {
        return callbackFlow {

            val locationCallback = object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    result.locations.lastOrNull()?.let { geoPoint ->
                        trySend(Location(geoPoint.latitude, geoPoint.longitude))
                    }
                }
            }

            val request = LocationRequest.Builder(DEFAULT_INTERVAL_MILLIS)
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setMinUpdateDistanceMeters(DEFAULT_MIN_DISTANCE_METERS)
                .build()

            fusedLocationProviderClient.requestLocationUpdates(
                request, locationCallback, Looper.getMainLooper()
            ).addOnFailureListener {
                close(it)
            }

            awaitClose {
                fusedLocationProviderClient.removeLocationUpdates(locationCallback)
            }
        }
    }

    override suspend fun requestCurrentLocation(): CurrentLocationResult {
        return suspendCancellableCoroutine { continuation ->
            fusedLocationProviderClient.lastLocation
                .addOnSuccessListener { geoPoint ->
                    val location = Location(geoPoint.latitude, geoPoint.longitude)
                    continuation.resume(CurrentLocationResult.Success(location))
                }.addOnFailureListener {
                    continuation.resume(CurrentLocationResult.Failure)
                }
        }
    }

    companion object {
        const val DEFAULT_INTERVAL_MILLIS = 10_000L
        const val DEFAULT_MIN_DISTANCE_METERS = 10f
    }
}