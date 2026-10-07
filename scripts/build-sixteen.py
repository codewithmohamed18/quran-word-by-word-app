"""Extract original Mode 5 JPEGs, verified against the approved reader snapshot."""
from pathlib import Path
import hashlib, os, time, urllib.request
import fitz
ROOT=Path(__file__).resolve().parents[1]
URL='https://quranpdf.wordpress.com/wp-content/uploads/2014/12/quran-16-lines-tajwedi-hammad-company1.pdf'
SHA256='fd2e43a3ea89d96052ec0c1b3de4233ae0926a24a18712a471edbfc56ddc3cb3'
source=Path(os.environ.get('QURAN_SIXTEEN_SOURCE',str(ROOT/'dist/sources/sixteen-original.pdf')))
source.parent.mkdir(parents=True,exist_ok=True)
if not source.exists():
    for attempt in range(3):
        try:
            with urllib.request.urlopen(URL,timeout=240) as response, source.open('wb') as destination:
                import shutil
                shutil.copyfileobj(response,destination)
            break
        except Exception:
            source.unlink(missing_ok=True)
            if attempt==2:raise
            time.sleep(2)
assert hashlib.sha256(source.read_bytes()).hexdigest()==SHA256, 'Mode 5 source checksum mismatch'
output=ROOT/'dist/web/pages'
output.mkdir(parents=True,exist_ok=True)
with fitz.open(source) as document:
    assert len(document)==561, 'Mode 5 source page count changed'
    for index,page in enumerate(document):
        images=page.get_images()
        assert len(images)==1, f'Review Mode 5 page {index+1}: expected one original image'
        image=document.extract_image(images[0][0])
        assert image['ext']=='jpeg', 'Original Mode 5 JPEG expected'
        (output/f'sixteen-{index+1}.jpeg').write_bytes(image['image'])
print('Mode 5: all 561 original JPEG scans extracted without reencoding')
