package nl.jovmit.loctrack.sdk.submit

sealed class LocationSubmitResult {

    data class Success(val message: String) : LocationSubmitResult()

    data object Failure : LocationSubmitResult()
}
