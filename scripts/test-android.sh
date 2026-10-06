#!/bin/bash
set -euo pipefail
mkdir -p dist/android-checks
trap 'adb logcat -d > dist/android-checks/device-logcat.txt || true' EXIT
FAILURES=0
adb install -r dist/Tajweed-and-Meaning-v1.8-Adaptive.apk
adb install -r android/tests/build/reader-tests.apk
# The stock Quickstep launcher can ANR when wm density is changed repeatedly.
# This disposable emulator tests the reader directly, so remove the launcher from the test session.
adb shell am force-stop com.android.launcher3
adb shell pm disable-user --user 0 com.android.launcher3
adb shell svc wifi disable
adb shell svc data disable
adb shell cmd overlay enable com.android.internal.display.cutout.emulation.hole || true
adb shell settings put system accelerometer_rotation 0
run_case() {
  local profile=$1
  adb shell am force-stop com.khalid.tajweedmeaning.adaptive
  # Each profile starts from word-by-word mode so both PDF renderers are exercised.
  adb shell pm clear com.khalid.tajweedmeaning.adaptive >/dev/null
  if ! timeout 240s adb shell am instrument -w -e profile "$profile" com.khalid.tajweedmeaning.adaptive.tests/com.khalid.tajweedmeaning.adaptive.tests.ReaderChecks | tee "dist/android-checks/$profile.txt"; then
    FAILURES=$((FAILURES+1))
  fi
  adb pull /sdcard/Android/data/com.khalid.tajweedmeaning.adaptive/files/checks dist/android-checks/ >/dev/null || true
  if ! grep -q 'PASS:' "dist/android-checks/$profile.txt"; then
    adb logcat -d -s AndroidRuntime System.err chromium > "dist/android-checks/$profile-crash.txt"
    FAILURES=$((FAILURES+1))
  fi
}
adb shell wm size 1080x2400
adb shell wm density 420
run_case pixel-cutout
adb shell wm size 720x1280
adb shell wm density 360
run_case compact-phone
adb shell wm size 1080x2400
adb shell wm density 420
adb shell settings put system user_rotation 1
run_case landscape
adb shell settings put system user_rotation 0
adb shell wm size 1600x2560
adb shell wm density 240
run_case tablet

if [ "$FAILURES" -gt 0 ]; then exit 1; fi
