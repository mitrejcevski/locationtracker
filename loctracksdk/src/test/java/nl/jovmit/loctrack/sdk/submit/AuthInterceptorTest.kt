package nl.jovmit.loctrack.sdk.submit

import com.google.common.truth.Truth.assertThat
import nl.jovmit.loctrack.sdk.InMemorySharedPreferences
import okhttp3.Call
import okhttp3.Connection
import okhttp3.Interceptor
import okhttp3.Protocol
import okhttp3.Request
import okhttp3.Response
import org.junit.jupiter.api.Test
import java.util.concurrent.TimeUnit

class AuthInterceptorTest {

    private val accessToken = "accessToken"
    private val refreshedAccessToken = "newAccessToken"
    private val refreshToken = "refreshToken"
    private val expiresAt = "2024-04-04T22:26:30.879Z"

    @Test
    fun noRefreshTokenAvailable() {
        val tokenStorage = TokenStorage(
            InMemorySharedPreferences(),
            TestableClock()
        )
        val authApi = FakeAuthApi(accessToken, refreshToken, expiresAt)
        val interceptor = AuthInterceptor(authApi, tokenStorage)

        interceptor.intercept(createDummyRequest())

        assertThat(tokenStorage.getAccessToken()).isEqualTo(accessToken)
        assertThat(tokenStorage.getRefreshToken()).isEqualTo(refreshToken)
        assertThat(tokenStorage.isTokenExpired()).isFalse()
    }

    @Test
    fun refreshExpiredAccessToken() {
        val timeAfterTokenExpiration = Long.MAX_VALUE
        val tokenStorage = TokenStorage(
            InMemorySharedPreferences(),
            TestableClock(timeAfterTokenExpiration)
        ).apply {
            storeAccessToken(AccessTokenResponse(accessToken, expiresAt, refreshToken))
        }
        val authApi = FakeAuthApi(accessToken, refreshToken, expiresAt, refreshedAccessToken)
        val interceptor = AuthInterceptor(authApi, tokenStorage)

        interceptor.intercept(createDummyRequest())

        assertThat(tokenStorage.getAccessToken()).isEqualTo(refreshedAccessToken)
    }

    @Test
    fun applyAuthHeader() {
        val tokenStorage = TokenStorage(
            InMemorySharedPreferences(),
            TestableClock()
        )
        val authApi = FakeAuthApi(accessToken, refreshToken, expiresAt)
        val interceptor = AuthInterceptor(authApi, tokenStorage)

        val result = interceptor.intercept(createDummyRequest())

        assertThat(result.request.header("Authorization")).isEqualTo("Bearer $accessToken")
    }

    private class TestableClock(
        private val desiredTimestamp: Long = 0L
    ) : Clock {

        override fun now(): Long {
            return desiredTimestamp
        }
    }

    private class FakeAuthApi(
        private val accessToken: String,
        private val refreshToken: String,
        private val expiresAt: String,
        private val refreshedAccessToken: String = ""
    ) : AuthApi {

        override suspend fun authorize(authToken: String): AccessTokenResponse {
            return AccessTokenResponse(
                accessToken = accessToken,
                expiresAt = expiresAt,
                refreshToken = refreshToken
            )
        }

        override suspend fun refreshToken(refreshToken: String): RefreshTokenResponse {
            return RefreshTokenResponse(
                accessToken = refreshedAccessToken,
                expiresAt = expiresAt
            )
        }
    }

    private fun createDummyRequest(): Interceptor.Chain {
        val request = Request.Builder()
            .url("https://dummy.url/")
            .build()
        return object : Interceptor.Chain {
            override fun call(): Call {
                TODO("Not yet implemented")
            }

            override fun connectTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

            override fun connection(): Connection? {
                TODO("Not yet implemented")
            }

            override fun proceed(request: Request): Response {
                return Response.Builder()
                    .request(request)
                    .code(200)
                    .protocol(Protocol.HTTP_2)
                    .message("")
                    .build()
            }

            override fun readTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }

            override fun request(): Request {
                return request
            }

            override fun withConnectTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun withReadTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun withWriteTimeout(timeout: Int, unit: TimeUnit): Interceptor.Chain {
                TODO("Not yet implemented")
            }

            override fun writeTimeoutMillis(): Int {
                TODO("Not yet implemented")
            }
        }
    }
}