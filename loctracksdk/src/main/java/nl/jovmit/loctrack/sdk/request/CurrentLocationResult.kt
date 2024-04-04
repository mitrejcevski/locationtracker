package nl.jovmit.loctrack.sdk.request

sealed class CurrentLocationResult {

    data class Success(val location: Location) : CurrentLocationResult()

    data object Failure : CurrentLocationResult()
}