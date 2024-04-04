package nl.jovmit.loctrack.sdk.submit

import kotlinx.serialization.Serializable

@Serializable
internal data class LocationSubmitResponse(
    val message: String
)