package nl.jovmit.locationtracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import nl.jovmit.locationtracker.ui.theme.LocationTrackerTheme
import nl.jovmit.loctrack.sdk.LocationTracker
import nl.jovmit.loctrack.sdk.LocationTrackerConfig

class MainActivity : ComponentActivity() {

    private val locationTracker by viewModels<LocationTracker> {
        viewModelFactory {
            initializer {
                val configuration = LocationTrackerConfig.Builder()
                    .useDefaultAndroidComponents(applicationContext)
                    .build()
                LocationTracker(configuration)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LocationTrackerTheme {
                LocationTrackerScreen(locationTracker)
            }
        }
    }
}