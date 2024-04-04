package nl.jovmit.loctrack.sdk.permissions

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi

internal class DeviceLocationPermissionChecker(
    private val context: Context
) : LocationPermissionChecker {

    override fun hasLocationPermissions(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            hasCoarseLocationPermission() &&
                    hasFineLocationPermission() &&
                    hasBackgroundLocationPermission()
        } else {
            hasCoarseLocationPermission() && hasFineLocationPermission()
        }
    }

    private fun hasCoarseLocationPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun hasFineLocationPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private fun hasBackgroundLocationPermission(): Boolean {
        return context.checkSelfPermission(Manifest.permission.ACCESS_BACKGROUND_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
    }
}