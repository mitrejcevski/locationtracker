package nl.jovmit.loctrack.sdk.permissions

/**
 * An interface defining a way to check for location permissions.
 */
interface LocationPermissionChecker {

    fun hasLocationPermissions(): Boolean
}