#!/bin/bash
set -euo pipefail
ROOT=$(cd "$(dirname "$0")/.." && pwd)
SIGN_DIR=$(mktemp -d "${RUNNER_TEMP:-/tmp}/quran-sign.XXXXXX")
KEYCHAIN="$SIGN_DIR/build.keychain-db"
KEYCHAIN_PASSWORD=$(openssl rand -hex 32)
cleanup() { security delete-keychain "$KEYCHAIN" || true; rm -rf "$SIGN_DIR"; }
trap cleanup EXIT
export SIGN_DIR
python3 - <<'PY'
import os,base64,pathlib
p=pathlib.Path(os.environ['SIGN_DIR'])
for variable,name in [('APPLE_CERTIFICATE_P12_BASE64','certificate.p12'),('APPLE_PROVISIONING_PROFILE_BASE64','profile.mobileprovision')]:
    (p/name).write_bytes(base64.b64decode(os.environ[variable],validate=True))
PY
security create-keychain -p "$KEYCHAIN_PASSWORD" "$KEYCHAIN"
security set-keychain-settings -lut 21600 "$KEYCHAIN"
security unlock-keychain -p "$KEYCHAIN_PASSWORD" "$KEYCHAIN"
security import "$SIGN_DIR/certificate.p12" -k "$KEYCHAIN" -P "$APPLE_CERTIFICATE_PASSWORD" -T /usr/bin/codesign
security set-key-partition-list -S apple-tool:,apple: -k "$KEYCHAIN_PASSWORD" "$KEYCHAIN"
security list-keychains -d user -s "$KEYCHAIN" login.keychain-db
security cms -D -i "$SIGN_DIR/profile.mobileprovision" > "$SIGN_DIR/profile.plist"
python3 - <<'PY'
import os,plistlib,pathlib,shutil
p=pathlib.Path(os.environ['SIGN_DIR']); profile=plistlib.loads((p/'profile.plist').read_bytes())
assert profile['Entitlements']['application-identifier'].endswith('.com.codewithmohamed18.tajweedmeaning'), 'Provisioning profile must match the iOS bundle identifier'
assert profile.get('ProvisionedDevices'), 'Use an Ad Hoc profile for GitHub device installation'
target=pathlib.Path.home()/'Library/MobileDevice/Provisioning Profiles'; target.mkdir(parents=True,exist_ok=True)
shutil.copyfile(p/'profile.mobileprovision',target/(profile['UUID']+'.mobileprovision'))
(p/'team').write_text(profile['TeamIdentifier'][0]); (p/'uuid').write_text(profile['UUID'])
settings={'method':'release-testing','teamID':profile['TeamIdentifier'][0],'signingStyle':'manual','provisioningProfiles':{'com.codewithmohamed18.tajweedmeaning':profile['UUID']}}
(p/'ExportOptions.plist').write_bytes(plistlib.dumps(settings))
PY
TEAM=$(cat "$SIGN_DIR/team")
PROFILE=$(cat "$SIGN_DIR/uuid")
cd "$ROOT/ios"
xcodebuild -project TajweedMeaning.xcodeproj -scheme TajweedMeaning -configuration Release -destination 'generic/platform=iOS' -archivePath build/Signed.xcarchive CODE_SIGN_STYLE=Manual DEVELOPMENT_TEAM="$TEAM" PROVISIONING_PROFILE_SPECIFIER="$PROFILE" CODE_SIGN_IDENTITY='Apple Distribution' archive
xcodebuild -exportArchive -archivePath build/Signed.xcarchive -exportOptionsPlist "$SIGN_DIR/ExportOptions.plist" -exportPath build/export
cp build/export/*.ipa "$ROOT/dist/Tajweed-and-Meaning-iOS-SIGNED.ipa"
