package nl.jovmit.loctrack.sdk.submit

import android.content.Context
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import nl.jovmit.loctrack.sdk.request.Location
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import java.io.IOException

internal class RestLocationSubmitter(context: Context) : LocationSubmitter {

    private val retrofit = Retrofit.Builder()
        .baseUrl(API_BASE_URL)
        .addConverterFactory(Json.asConverterFactory("application/json".toMediaType()))
        .build()
    private val authApi = retrofit.create(AuthApi::class.java)

    private val preferences = context.getSharedPreferences(TOKEN_STORAGE, Context.MODE_PRIVATE)
    private val tokenStorage = TokenStorage(preferences)
    private val authInterceptor = AuthInterceptor(authApi, tokenStorage)
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .build()
    private val authorisedRetrofit = retrofit.newBuilder()
        .client(okHttpClient)
        .build()
    private val locationSubmitApi = authorisedRetrofit.create(LocationSubmitApi::class.java)

    override suspend fun submitLocation(location: Location): LocationSubmitResult {
        return try {
            val data = with(location) { LocationData(latitude, longitude) }
            val response = locationSubmitApi.submitLocation(data)
            LocationSubmitResult.Success(response.message)
        } catch (httpException: Exception) {
            LocationSubmitResult.Failure
        } catch (connectionException: IOException) {
            LocationSubmitResult.Failure
        }
    }

    companion object {
        private const val API_BASE_URL = "https://dummy-api-mobile.api.sandbox.bird.one/"
        private const val TOKEN_STORAGE = "tokenStorage"
    }
}