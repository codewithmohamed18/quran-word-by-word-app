"""Build the complete Safari/Home Screen reader from verified bundled assets."""
from pathlib import Path
import json
import runpy
import shutil
import sys

import fitz
from PIL import Image, ImageDraw

ROOT = Path(__file__).resolve().parents[1]
runpy.run_path(str(ROOT / 'scripts/prepare.py'))
source = ROOT / 'shared/web'
output = ROOT / 'dist/web'
output.mkdir(parents=True, exist_ok=True)
for item in source.iterdir():
    if item.suffix != '.pdf':
        shutil.copy2(item, output / item.name)
for directory in [ROOT / 'web-overrides', ROOT / 'web-safari']:
    for item in directory.iterdir():
        if item.is_file() and item.suffix != '.py' and item.name != 'README.md':
            shutil.copy2(item, output / item.name)

app = (output / 'app.js').read_text()
app = app.replace("incoming.src='data:image/jpeg;base64,'+data;",
                  "incoming.src=data.startsWith('pages/')||data.startsWith('data:')?data:'data:image/jpeg;base64,'+data;")
app = app.replace('pdfImages.size>5', 'pdfImages.size>3')
app = app.replace('fully offline', 'available offline after saving')
app = app.replace('Both modes work completely offline', 'Saved pages work offline')
(output / 'app.js').write_text(app)

html = (output / 'index.html').read_text()
html = html.replace('</head>', '<link rel="stylesheet" href="adaptive.css"><link rel="stylesheet" href="web.css"><link rel="manifest" href="manifest.json"><link rel="icon" href="icon.svg"><link rel="apple-touch-icon" href="icon.png"><meta name="apple-mobile-web-app-capable" content="yes"></head>')
html = html.replace('<script src="app.js">', '<script src="page-meta.js"></script><script src="web-bridge.js"></script><script src="app.js">')
html = html.replace('</body>', '<script src="adaptive.js"></script><script src="settings.js"></script><script src="web-install.js"></script><script src="web-reader.js"></script></body>')
(output / 'index.html').write_text(html)

pages = output / 'pages'
pages.mkdir(exist_ok=True)
metadata = {}
for mode, pdf, crop_file in [
    ('word', 'word-by-word.pdf', source / 'word-crops.json'),
    ('plain', 'plain-13line.pdf', ROOT / 'config/plain-crops.json'),
]:
    crops = json.loads(crop_file.read_text())
    metadata[mode] = []
    with fitz.open(source / pdf) as document:
        assert len(crops) == len(document)
        for index, page in enumerate(document):
            images = page.get_images()
            assert len(images) == 1, 'Review extraction for pages with multiple images'
            xref = images[0][0]
            image = document.extract_image(xref)
            ext = image['ext']
            (pages / f'{mode}-{index + 1}.{ext}').write_bytes(image['image'])
            metadata[mode].append({'ext': ext, 'crop': crops[index],
                                   'image': list(page.get_image_rects(xref)[0])})
    print(mode, len(metadata[mode]), 'original scans extracted')
(output / 'page-meta.js').write_text('const SCANS=' + json.dumps(metadata, separators=(',', ':')) + ';')

icon = Image.new('RGB', (512, 512), '#183f38')
draw = ImageDraw.Draw(icon)
draw.polygon([(80,136),(160,120),(256,160),(352,120),(432,136),(432,376),(352,360),(256,400),(160,360),(80,376)], fill='#fff5db', outline='#c6a45c')
draw.line([(256,160),(256,400)], fill='#183f38', width=12)
icon.save(output / 'icon.png')
print('Safari reader built:', output)

# Optional web-only Darussalam edition. Supply all verified source PDFs to include it.
import os
import subprocess
study_source = os.environ.get('QURAN_STUDY_SOURCE')
if study_source:
    subprocess.run([sys.executable, str(ROOT / 'scripts/build-study.py'), study_source, str(output), str(ROOT / 'dist/Study-the-Noble-Quran-Complete.pdf')], check=True)
    runpy.run_path(str(ROOT / 'web-safari/study-patch.py'))['patch'](output)
    if os.environ.get('QURAN_STUDY_ASSET_BASE'):
        for asset in pages.glob('study-*'):asset.unlink()
        shutil.rmtree(output / 'study-pdf')
else:
    print('Mode 4 source PDFs not supplied; building the existing three-mode reader.')
