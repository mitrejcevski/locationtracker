package nl.jovmit.loctrack.sdk.submit

import retrofit2.http.Body
import retrofit2.http.POST

internal interface LocationSubmitApi {

    @POST("/location")
    suspend fun submitLocation(@Body data: LocationData): LocationSubmitResponse
}