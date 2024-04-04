package nl.jovmit.loctrack.sdk.submit

import kotlinx.serialization.Serializable

@Serializable
internal data class AccessTokenResponse(
    val accessToken: String,
    val expiresAt: String,
    val refreshToken: String
)