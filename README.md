# Aura Home

An icon-first edition of aura-skin for Android 9 and later. Tap **All apps** or swipe up on the home screen to open the alphabetical drawer. Type to filter apps. Hold an icon to pin it to favorites, hide it, or open Android app info.

The application ID is `dev.aura.iconhome`, so the launcher can be installed alongside Minimal Launcher. Favorites and hidden apps are stored separately for each launcher.

## Build and install

With JDK 17, Android SDK 34 and a connected phone, run:

```sh
./gradlew :app:installDebug
```

Open Aura Home, tap **Settings → Set as default launcher**, and choose **Aura Home**. Use the same system setting to switch back.
