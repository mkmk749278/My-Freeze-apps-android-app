#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
LOG_DIR="$ROOT_DIR/build-logs"
ARTIFACT_DIR="$ROOT_DIR/artifacts"
FINAL_APK="$ARTIFACT_DIR/MyApps-final.apk"
MAX_ATTEMPTS="${MAX_ATTEMPTS:-3}"
GRADLE_CMD="${GRADLE_CMD:-$ROOT_DIR/gradlew}"
mkdir -p "$LOG_DIR" "$ARTIFACT_DIR"

timestamp="$(date -u +%Y%m%dT%H%M%SZ)"
summary_log="$LOG_DIR/build-summary-$timestamp.log"
debug_apk="$ROOT_DIR/app/build/outputs/apk/debug/app-debug.apk"
release_apk="$ROOT_DIR/app/build/outputs/apk/release/app-release.apk"

echo "[build] root=$ROOT_DIR" | tee "$summary_log"
echo "[build] max_attempts=$MAX_ATTEMPTS" | tee -a "$summary_log"
echo "[build] gradle_cmd=$GRADLE_CMD" | tee -a "$summary_log"

python - <<'PY' | tee -a "$summary_log"
import socket
for host in ("services.gradle.org", "dl.google.com"):
    try:
        print(f"[build] dns {host} -> {socket.gethostbyname(host)}")
    except OSError as exc:
        print(f"[build] dns {host} unavailable: {exc}")
PY

for attempt in $(seq 1 "$MAX_ATTEMPTS"); do
  attempt_log="$LOG_DIR/build-attempt-${attempt}-$timestamp.log"
  echo "[build] attempt=$attempt tasks=clean,testDebugUnitTest,assembleDebug,assembleRelease" | tee -a "$summary_log"
  if "$GRADLE_CMD" --no-daemon --stacktrace clean testDebugUnitTest :app:assembleDebug :app:assembleRelease 2>&1 | tee "$attempt_log"; then
    if [[ -f "$release_apk" ]]; then
      cp "$release_apk" "$FINAL_APK"
      echo "[build] release_apk=$release_apk" | tee -a "$summary_log"
      echo "[build] final_apk=$FINAL_APK" | tee -a "$summary_log"
      "$ROOT_DIR/scripts/verify_apk.sh" "$FINAL_APK" | tee -a "$summary_log"
      exit 0
    fi
    if [[ -f "$debug_apk" ]]; then
      cp "$debug_apk" "$FINAL_APK"
      echo "[build] debug_apk=$debug_apk" | tee -a "$summary_log"
      echo "[build] final_apk=$FINAL_APK" | tee -a "$summary_log"
      "$ROOT_DIR/scripts/verify_apk.sh" "$FINAL_APK" | tee -a "$summary_log"
      exit 0
    fi
  fi
  echo "[build] attempt=$attempt failed" | tee -a "$summary_log"
done

echo "[build] exhausted attempts without a valid APK" | tee -a "$summary_log"
exit 1
