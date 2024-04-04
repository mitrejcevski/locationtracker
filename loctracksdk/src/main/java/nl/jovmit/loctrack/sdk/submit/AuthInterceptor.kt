package nl.jovmit.loctrack.sdk.submit

import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

internal class AuthInterceptor(
    private val authApi: AuthApi,
    private val tokenStorage: TokenStorage
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        if (!tokenStorage.hasRefreshToken()) {
            runBlocking {
                val response = authApi.authorize("Bearer $DUMMY_API_KEY")
                tokenStorage.storeAccessToken(response)
            }
        }
        if (tokenStorage.isTokenExpired()) {
            val refreshToken = tokenStorage.getRefreshToken()
            runBlocking {
                val response = authApi.refreshToken("Bearer $refreshToken")
                tokenStorage.updateAccessToken(response)
            }
        }
        val accessToken = tokenStorage.getAccessToken()
        val authorizedRequest = originalRequest.newBuilder()
            .header("Authorization", "Bearer $accessToken")
            .build()
        return chain.proceed(authorizedRequest)
    }

    companion object {
        const val DUMMY_API_KEY = "xdk8ih3kvw2c66isndihzke5"
    }
}