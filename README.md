# locationtracker
Location Tracking SDK &amp; Sample App.

## Description
The location tracker SDK is currently supporting only Android apps, and it is designed in a way to
be converted to Kotlin MultiPlatform and support iOS apps easily, if need be.

The SDK exposes a very simple API to track location updates and submitting them to a backend.
It contains default implementations to fetch location using native Android APIs, as well as a
default mechanism to submit the location updates to a predefined backend.

However, the SDK is highly configurable, so that the user can write their own implementations for
loading location and submitting it respectively.

## Usage
The usage of the library is rather simple.

## 1. Setup Dependency
Add the dependency in the build.gradle file

```kotlin
implementation("io.github.mitrejcevski:locationtracker:1.0.0-SNAPSHOT")
```

## 2. Instantiation
In your main activity, instantiate the tracker by passing the desired configuration.

#### 2.1 Without Dependency Injection Container:

```kotlin
class MainActivity : AppCompatActivity() {
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
    ...
}
```

#### 2.2 With Dependency Injection Container:

```kotlin
class MainActivity : AppCompatActivity() {
    private val locationTracker by viewModels<LocationTracker>()
    ...
}
```

## 3. Track location
The SDK can track regular location updates, and request a location update on-demand.

#### 3.1 Track regular location updates

```kotlin
locationTracker.startTrackingLocation()
```

#### 3.2 Request no-demand location update

```kotlin
locationTracker.requestCurrentLocation()
```

#### 3.3 Observe current state (Optional)
The SDK provides an observable property that you can use to check the current state
of the SDK (location fetching, submitssions, failures).

##### 3.3.1 Jetpack Compose

```kotlin
val state by locationTracker.locationTrackingState.collectAsStateWithLifecycle()
```

##### 3.3.1 Traditional Android UI

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        locationTracker.locationTrackingState.collectLatest { state ->
            //do something with the state
        }
    }
}
```

## 4. Additional customizations
You can supply your own implementations for checking permissions granted, location loading, and location submission.

```kotlin
val configuration = LocationTrackerConfig.Builder()
     .setLocationPermissionChecker(yourOwnImplementation)
     .setLocationFetcher(yourOwnImplementation)
     .setLocationSubmitter(yourOwnImplementation)
     .build()
```

That would allow to mix and match different components with the default built-in ones, and enable testability.


## Design Choices
The SDK is designed with a possibility to convert it into KMP in the future, and support iOS too. It keeps the testability in mind as well. Additionally, it should be highly configurable allowing the user to use the default implementation of the components, as well as providing custom implementations of them.

The LocationTracker is getting configured by taking in LocationTrackerConfig, instead of taking the corresponding component implementations in the constructor. It would make the future development of the SDK easier as it will allow deprecations and backwards compatibility support smoother.
