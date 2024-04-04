package nl.jovmit.loctrack.sdk.submit

import android.content.SharedPreferences
import androidx.core.content.edit
import java.time.Instant

internal class TokenStorage(
    private val preferences: SharedPreferences,
    private val clock: Clock = SystemClock()
) {
    fun hasRefreshToken(): Boolean {
        return preferences.contains(REFRESH_TOKEN)
    }

    fun isTokenExpired(): Boolean {
        val rawValue = preferences.getString(EXPIRATION_DATE_TIME, "") ?: ""
        if (rawValue.isBlank()) return true
        val timestamp = convertToTimestamp(rawValue)
        return timestamp < clock.now()
    }

    fun getRefreshToken(): String {
        return preferences.getString(REFRESH_TOKEN, "") ?: ""
    }

    fun getAccessToken(): String {
        return preferences.getString(ACCESS_TOKEN, "") ?: ""
    }

    fun storeAccessToken(response: AccessTokenResponse) {
        preferences.edit {
            putString(ACCESS_TOKEN, response.accessToken)
            putString(REFRESH_TOKEN, response.refreshToken)
            putString(EXPIRATION_DATE_TIME, response.expiresAt)
        }
    }

    fun updateAccessToken(response: RefreshTokenResponse) {
        preferences.edit {
            putString(ACCESS_TOKEN, response.accessToken)
            putString(EXPIRATION_DATE_TIME, response.expiresAt)
        }
    }

    private fun convertToTimestamp(rawValue: String): Long {
        val instant = Instant.parse(rawValue)
        return instant.toEpochMilli()
    }

    companion object {
        const val ACCESS_TOKEN = "accessToken"
        const val REFRESH_TOKEN = "refreshToken"
        const val EXPIRATION_DATE_TIME = "expirationDateTime"
    }
}
