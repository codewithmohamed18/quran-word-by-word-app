"""Reproduce the independent Pages reader from verified repository assets."""
from pathlib import Path
import hashlib,json,runpy,shutil
ROOT=Path(__file__).resolve().parents[1]
runpy.run_path(str(ROOT/'scripts/build-web.py'),run_name='__main__')
output=ROOT/'dist/web'
for source in (ROOT/'web-pages/reader').rglob('*'):
    if source.is_file():
        destination=output/source.relative_to(ROOT/'web-pages/reader')
        destination.parent.mkdir(parents=True,exist_ok=True)
        shutil.copy2(source,destination)
manifest=json.loads((ROOT/'web-pages/snapshot.json').read_text())
for name,checksum in manifest['files'].items():
    actual=hashlib.sha256((output/name).read_bytes()).hexdigest()
    assert actual==checksum, f'Reader snapshot mismatch: {name}'
(output/'.nojekyll').touch()
print('Verified complete web reader snapshot:',manifest['source_commit'],len(manifest['files']),'files')
