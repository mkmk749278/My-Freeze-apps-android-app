# My Apps

Production-ready Kotlin/Jetpack Compose Android utility for curating installed apps, freezing or unfreezing them through root or Shizuku access, and protecting the dashboard with switchable PIN and biometric authentication.

## Highlights
- Dark-only Compose UI with a dashboard-first workflow, modular tabs, and animated quick actions.
- Dashboard grid shows selected apps as icon-first launch targets with dashboard-only freeze, unfreeze, app info, and uninstall options.
- Root-first command execution with Shizuku fallback when permission is granted.
- Secure PIN storage with `EncryptedSharedPreferences`, plus DataStore-backed authentication preferences and selected apps.
- Automated debug and release APK build logging, retry, verification, and final artifact export.

## Project Structure
- `app/src/main/java/com/mkmk749278/myapps/MainActivity.kt` - app entry point and external intents.
- `app/src/main/java/com/mkmk749278/myapps/MainViewModel.kt` - MVVM state orchestration.
- `app/src/main/java/com/mkmk749278/myapps/data` - secure settings, DataStore preferences, root/Shizuku access, and app repository.
- `app/src/main/java/com/mkmk749278/myapps/model` - UI state and enum models.
- `app/src/main/java/com/mkmk749278/myapps/ui` - Compose root, screens, and theme.
- `scripts/build_apk.sh` - automated debug + release build, retry, verification, and artifact copy.
- `scripts/verify_apk.sh` - APK integrity and optional ADB install verification.

## Build Debug/Release APKs With Logging
```bash
./scripts/build_apk.sh
```

The script writes logs to `build-logs/`, verifies the generated APK, and copies the preferred final artifact to `artifacts/MyApps-final.apk`.
