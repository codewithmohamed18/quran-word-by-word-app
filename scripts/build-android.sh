#!/bin/bash
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
SDK=${ANDROID_HOME:-${ANDROID_SDK_ROOT:-}}
TOOLS="$SDK/build-tools/35.0.0"
JAR="$SDK/platforms/android-35/android.jar"
BUILD="$ROOT/android/build"
mkdir -p "$BUILD/classes" "$BUILD/dex" "$ROOT/dist"
"$TOOLS/aapt2" compile --dir "$ROOT/android/src/main/res" -o "$BUILD/resources.zip"
"$TOOLS/aapt2" link -o "$BUILD/base.apk" --manifest "$ROOT/android/src/main/AndroidManifest.xml" -I "$JAR" -A "$ROOT/shared/web" -0 pdf "$BUILD/resources.zip"
find "$ROOT/android/src/main/java" -name '*.java' > "$BUILD/sources.txt"
javac -source 8 -target 8 -classpath "$JAR" -d "$BUILD/classes" @"$BUILD/sources.txt"
jar cf "$BUILD/classes.jar" -C "$BUILD/classes" .
"$TOOLS/d8" --lib "$JAR" --min-api 26 --output "$BUILD/dex" "$BUILD/classes.jar"
export BUILD
python3 - <<'PY'
import os,zipfile,pathlib
p=pathlib.Path(os.environ['BUILD'])
with zipfile.ZipFile(p/'base.apk','a') as z:z.write(p/'dex/classes.dex','classes.dex',compress_type=zipfile.ZIP_DEFLATED)
PY
"$TOOLS/zipalign" -f -p 4 "$BUILD/base.apk" "$BUILD/aligned.apk"
"$TOOLS/apksigner" sign --ks "$ROOT/android/debug.keystore" --ks-pass pass:android --key-pass pass:android --ks-key-alias androiddebugkey --out "$ROOT/dist/Tajweed-and-Meaning-v1.8-Adaptive.apk" "$BUILD/aligned.apk"
"$TOOLS/apksigner" verify "$ROOT/dist/Tajweed-and-Meaning-v1.8-Adaptive.apk"
