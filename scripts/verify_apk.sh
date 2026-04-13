#!/usr/bin/env bash
set -euo pipefail

apk_path="${1:?APK path required}"
echo "[verify] apk=$apk_path"
if [[ ! -f "$apk_path" ]]; then
  echo "[verify] missing apk"
  exit 1
fi

unzip -t "$apk_path" >/dev/null
echo "[verify] zip integrity ok"

if command -v adb >/dev/null 2>&1 && adb get-state >/dev/null 2>&1; then
  package_name="com.mkmk749278.myapps"
  echo "[verify] adb device detected"
  adb uninstall "$package_name" >/dev/null 2>&1 || true
  adb install -r "$apk_path"
  adb shell monkey -p "$package_name" -c android.intent.category.LAUNCHER 1
  echo "[verify] install and launch attempted via adb"
else
  echo "[verify] adb device unavailable; install/launch smoke test skipped"
fi
