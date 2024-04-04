package nl.jovmit.loctrack.sdk.submit

import kotlinx.serialization.Serializable

@Serializable
internal data class RefreshTokenResponse(
    val accessToken: String,
    val expiresAt: String
)