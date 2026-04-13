# My Apps

Production-ready Kotlin/Jetpack Compose Android utility for curating installed apps, freezing or unfreezing them through root access, and protecting the shell with a custom PIN plus optional biometric unlock.

## Highlights
- Dark-only Compose UI with Dashboard, App Library, Settings, Favorites, and floating Quick Actions.
- Root-driven freeze/unfreeze commands for installed user and system apps.
- One-tap unfreeze + launch flow from the curated dashboard.
- Custom PIN secured with PBKDF2 hashing and encrypted preferences-backed settings storage.
- Optional biometric or device-credential unlock via `BiometricPrompt`.
- Automated APK build, logging, retry, and optional ADB install verification scripts.

## Project Structure
- `app/src/main/java/com/mkmk749278/myapps/MainActivity.kt` - app entry point.
- `app/src/main/java/com/mkmk749278/myapps/MainViewModel.kt` - state orchestration and actions.
- `app/src/main/java/com/mkmk749278/myapps/data` - root shell, app repository, selection persistence, PIN security.
- `app/src/main/java/com/mkmk749278/myapps/model` - UI-facing app and screen models.
- `app/src/main/java/com/mkmk749278/myapps/ui` - Compose root, screens, components, and dark theme.
- `scripts/build_apk.sh` - automated build + retry + artifact copy.
- `scripts/verify_apk.sh` - APK validation and optional ADB install smoke test.

## Important Production Notes
- The app requests `QUERY_ALL_PACKAGES` because it is designed as a root-capable installed-app management utility.
- Release builds are signed with the local debug keystore by default so the generated APK installs immediately in development environments. Replace that signing setup before publishing.
- Root commands use `su -c` with safe shell escaping and fall back across multiple package-freeze commands for broader compatibility.
- Icon assets are adaptive vector placeholders representing frozen app management and can be replaced with branded production artwork.

## Build Debug/Release APKs With Logging
```bash
./scripts/build_apk.sh
```

The script writes logs to `build-logs/` and copies the selected final artifact to `artifacts/MyApps-final.apk`.
