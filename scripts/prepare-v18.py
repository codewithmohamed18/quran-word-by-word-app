"""Prepare v1.8 without modifying the archived v1.7 APK."""
import runpy
from pathlib import Path
import fitz
import hashlib,json
ROOT=Path(__file__).resolve().parents[1]
runpy.run_path(str(ROOT/'scripts/prepare.py'))
web=ROOT/'shared/web'; report=[]
for name in ['word-by-word.pdf','plain-13line.pdf']:
    source=web/name; before=source.stat().st_size; output=source.with_suffix('.optimized.pdf')
    doc=fitz.open(source); doc.save(output,garbage=4,deflate=True,use_objstms=1)
    optimized=fitz.open(output)
    raw_images=lambda d:sorted(hashlib.sha256(d.xref_stream_raw(n)).hexdigest() for n in range(1,d.xref_length()) if d.xref_is_image(n))
    assert raw_images(doc)==raw_images(optimized), 'An original image changed'
    assert [(p.rect,p.rotation) for p in doc]==[(p.rect,p.rotation) for p in optimized]
    for number in [0,len(doc)//2,len(doc)-1]:
        assert doc[number].get_pixmap(matrix=fitz.Matrix(.7,.7)).samples==optimized[number].get_pixmap(matrix=fitz.Matrix(.7,.7)).samples
    report.append({'file':name,'original_bytes':before,'optimized_bytes':output.stat().st_size,'pages':len(doc),'original_image_streams_preserved':True,'sample_pixels_identical':True})
    doc.close();optimized.close();output.replace(source)
# Verify every crop against the immutable bundled edition before shipping it.
import importlib.util
spec=importlib.util.spec_from_file_location('plain_crops',ROOT/'scripts/generate-plain-crops.py');module=importlib.util.module_from_spec(spec);spec.loader.exec_module(module)
configured=json.loads((ROOT/'config/plain-crops.json').read_text())
assert len(configured)==850 and configured==module.detect(web/'plain-13line.pdf')
(web/'plain-crops.json').write_text(json.dumps(configured,separators=(',',':')))
html=(web/'index.html').read_text().replace('</head>','<link rel="stylesheet" href="adaptive.css"></head>').replace('</body>','<script src="adaptive.js"></script></body>')
(web/'index.html').write_text(html)
for name in ['adaptive.css','adaptive.js']:(web/name).write_bytes((ROOT/'web-overrides'/name).read_bytes())
(web/'app.js').write_text((web/'app.js').read_text().replace('Tajweed & Meaning · 1.8','Tajweed & Meaning · 1.8.4').replace('Tajweed & Meaning · 1.7','Tajweed & Meaning · 1.8.4'))
(ROOT/'dist/quality-and-size.json').write_text(json.dumps(report,indent=2))
print(json.dumps(report))
