#!/bin/bash
set -euo pipefail
mkdir -p dist/v19-checks
adb install --no-incremental -r dist/Tajweed-and-Meaning-v1.9.apk
adb install --no-incremental -r android-v19/tests/build/reader-tests.apk
adb shell settings put system accelerometer_rotation 0
adb shell am instrument -w com.khalid.tajweedmeaning.webreader.tests/com.khalid.tajweedmeaning.webreader.tests.ReaderChecks | tee dist/v19-checks/results.txt
adb logcat -d > dist/v19-checks/logcat.txt
grep -q 'PASS:' dist/v19-checks/results.txt
! grep -q 'FAIL:' dist/v19-checks/results.txt
