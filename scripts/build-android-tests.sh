#!/bin/bash
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
TOOLS="$ANDROID_HOME/build-tools/35.0.0"
JAR="$ANDROID_HOME/platforms/android-35/android.jar"
BUILD="$ROOT/android/tests/build"
mkdir -p "$BUILD/classes" "$BUILD/dex"
"$TOOLS/aapt2" link -o "$BUILD/test.apk" --manifest "$ROOT/android/tests/AndroidManifest.xml" -I "$JAR"
find "$ROOT/android/tests/src" -name '*.java' > "$BUILD/sources.txt"
javac -source 8 -target 8 -classpath "$JAR" -d "$BUILD/classes" @"$BUILD/sources.txt"
jar cf "$BUILD/classes.jar" -C "$BUILD/classes" .
"$TOOLS/d8" --lib "$JAR" --min-api 26 --output "$BUILD/dex" "$BUILD/classes.jar"
export BUILD
python3 - <<'PY'
import os,zipfile,pathlib
p=pathlib.Path(os.environ['BUILD'])
with zipfile.ZipFile(p/'test.apk','a') as z:z.write(p/'dex/classes.dex','classes.dex',compress_type=zipfile.ZIP_DEFLATED)
PY
"$TOOLS/zipalign" -f -p 4 "$BUILD/test.apk" "$BUILD/aligned.apk"
"$TOOLS/apksigner" sign --ks "$ROOT/android/debug.keystore" --ks-pass pass:android --key-pass pass:android --ks-key-alias androiddebugkey --out "$BUILD/reader-tests.apk" "$BUILD/aligned.apk"
