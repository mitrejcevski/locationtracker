package nl.jovmit.loctrack.sdk.submit

import kotlinx.serialization.Serializable

@Serializable
internal data class LocationData(
    val latitude: Double,
    val longitude: Double
)