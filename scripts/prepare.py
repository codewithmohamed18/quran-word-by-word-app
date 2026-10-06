"""Reassemble the supplied, signed APK and extract its unmodified offline assets."""
import hashlib
import json
from pathlib import Path
import zipfile

ROOT = Path(__file__).resolve().parents[1]
manifest = json.loads((ROOT / 'payload-manifest.json').read_text())
destination = ROOT / 'dist'
destination.mkdir(exist_ok=True)
apk = destination / manifest['filename']
parts = sorted((ROOT / 'payload').glob('apk.part*'))
assert len(parts) == manifest['parts'], 'Missing APK payload parts'
with apk.open('wb') as output:
    for index, part in enumerate(parts):
        assert part.name == f'apk.part{index:04d}', 'Unexpected part order'
        output.write(part.read_bytes())
assert apk.stat().st_size == manifest['size'], 'Incorrect APK size'
assert hashlib.sha256(apk.read_bytes()).hexdigest() == manifest['sha256'], 'APK checksum mismatch'
web = ROOT / 'shared' / 'web'
web.mkdir(parents=True, exist_ok=True)
with zipfile.ZipFile(apk) as archive:
    for name, checksum in manifest['assets'].items():
        data = archive.read('assets/' + name)
        assert hashlib.sha256(data).hexdigest() == checksum, name
        (web / name).write_bytes(data)
(destination / 'SHA256SUMS.txt').write_text(f"{manifest['sha256']}  {apk.name}\n")
print('Verified supplied APK and all offline assets:', apk)
