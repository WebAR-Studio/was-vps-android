# WASVPS SDK (Android)

This is **WASVPS** SDK for native Android apps. Main features are:
- High-precision global user position localization for your AR apps
- Easy to use public API and premade Fragments
- Integration in [SceneForm Maintained](https://github.com/SceneView/sceneform-android)

## Requirements
- Android SDK 24+
- [ARCore](https://developers.google.com/ar/develop) supported device

## Installation

1. Open your project's `build.gradle`. Add `mavenCentral` repository if it doesn't exist:
    ```gradle
    allprojects {
        repositories {
            mavenCentral()
            ...
        }
    }
    ```

2. In your module's `build.gradle` add dependency:
    ```gradle
    dependencies {
        ...
        implementation "com.arstudio:wasvps-sdk:1.0.0"
    }
    ```

3. Sync Project with Gradle Files


## Sample

There is an sample project in this [repository](/sample). 

Just clone the repository and build it as a regular Android app. Make sure that your device support ARCore.

## Usage

### Android Manifest

Add this in `AndroidManifest.xml`, if android min sdk less than 24: 

```xml
<uses-sdk tools:overrideLibrary="com.arstudio.wasvps_sdk, com.google.ar.sceneform.sceneform, com.google.ar.sceneform.ux" />
```

By default `WASVPS SDK` has limited visibility in the Google Play Store to ARCore supported devices

```xml
<uses-feature
    android:name="android.hardware.camera.ar"
    android:required="true" />
```

To override visibility add this in your app's `AndroidManifest.xml`

```xml
<uses-feature
    android:name="android.hardware.camera.ar"
    android:required="false"
    tools:replace="android:required" />
```

### Using WASVPSArFragment

You can use build-in `WASVPSArFragment`. You can add into xml or by code:

*res/layout/main_activity.xml*
```xml
<androidx.fragment.app.FragmentContainerView
    android:id="@+id/vFragmentContainer"
    android:name="com.arstudio.wasvps_sdk.ui.WASVPSArFragment"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```
or

*src/main/java/…/MainActivity.kt*
```kotlin
supportFragmentManager.beginTransaction()
            .replace(R.id.vFragmentContainer, WASVPSArFragment())
            .commit()
```

### Setup WASVPSService

Create a config for `WASVPSService`:

```kotlin
val vpsConfig = WASVPSConfig.getIndoorConfig(
                    <locationIds>
                )
```
or
```kotlin
val vpsConfig = WASVPSConfig.getOutdoorConfig(
                    <locationIds>
                )
```

Setup a `WASVPSService`:

```kotlin
val vpsService = vpsArFragment.vpsService

vpsService.setVpsConfig(vpsConfig)

//optional
vpsService.setVpsCallback(object : VpsCallback {
                override fun onSuccess() {
                }

                override fun onFail() {
                }

                override fun onStateChange(state: State) {
                }

                override fun onError(error: Throwable) {
                }
            })
```

Start `VpsService`:

```kotlin
vpsService.startVpsService()
```

Stop `VpsService`:

```kotlin
vpsService.stopVpsService()
```

### WASVPSService in a custom ArFragment

For initialize WASVPS SDK in your app class add next code:
```kotlin
WASVPSSdk.init(this)
```

Create a new instance of `WASVPSService`:
```kotlin
WASVPSService.newInstance(): WASVPSService
```

You will also need to sync lifecycle of `WASVPSService` with lifecycle your `ArFragment`:
```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    vpsService.bindArSceneView(arSceneView)
}

override fun onResume() {
    super.onResume()
    vpsService.resume()
}

override fun onPause() {
    super.onPause()
    vpsService.pause()
}

override fun onDestroy() {
    super.onDestroy()
    vpsService.destroy()
}
```

After that you can use `WASVPSService` as mentioned above.


### Place 3D model

You can add a custom 3D model using `worldNode` in `WASVPSService`

```kotlin
vpsService.worldNode
```
All object placed under `worldNode` will have correct global position after localization.

## License


Google SceneForm library is licensed under [Apache License 2.0](https://github.com/google-ar/sceneform-android-sdk/blob/master/LICENSE).
