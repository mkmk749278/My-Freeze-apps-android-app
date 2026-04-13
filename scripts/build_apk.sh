#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/build-logs"
ARTIFACT_DIR="$ROOT_DIR/artifacts"
FINAL_APK="$ARTIFACT_DIR/MyApps-final.apk"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-3}"
mkdir -p "$LOG_DIR" "$ARTIFACT_DIR"

timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
summary_log="$LOG_DIR/build-summary-$timestamp.log"
echo "[build] root=$ROOT_DIR" | tee "$summary_log"
echo "[build] max_attempts=$MAX_ATTEMPTS" | tee -a "$summary_log"

build_target="assembleRelease"
final_candidate="$ROOT_DIR/app/build/outputs/apk/release/app-release.apk"

for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
  attempt_log="$LOG_DIR/build-attempt-${attempt}-$timestamp.log"
  echo "[build] attempt=$attempt target=$build_target" | tee -a "$summary_log"
  if "$ROOT_DIR/gradlew" --no-daemon clean testDebugUnitTest ":app:$build_target" | tee "$attempt_log"; then
    if [[ -f "$final_candidate" ]]; then
      cp "$final_candidate" "$FINAL_APK"
      echo "[build] final_apk=$FINAL_APK" | tee -a "$summary_log"
      "$ROOT_DIR/scripts/verify_apk.sh" "$FINAL_APK" | tee -a "$summary_log"
      exit 0
    fi
  fi
  echo "[build] attempt=$attempt failed" | tee -a "$summary_log"
  build_target="assembleDebug"
  final_candidate="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
done

echo "[build] exhausted attempts without a valid APK" | tee -a "$summary_log"
exit 1
