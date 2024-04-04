package nl.jovmit.loctrack.sdk.submit

import retrofit2.http.Header
import retrofit2.http.POST

internal interface AuthApi {

    @POST("/auth")
    suspend fun authorize(@Header("Authorization") authToken: String): AccessTokenResponse

    @POST("/auth/refresh")
    suspend fun refreshToken(@Header("Authorization") refreshToken: String): RefreshTokenResponse
}