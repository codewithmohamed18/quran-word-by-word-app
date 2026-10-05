#!/usr/bin/env python3
"""Bundle verified source pages 2–961: thirty unchanged 32-page Juz blocks."""
from pathlib import Path
import argparse, hashlib, urllib.request, tempfile, shutil
from pypdf import PdfReader, PdfWriter

URL = 'https://archive.org/download/quran-arabic-english-word-by-word-translation/quran-arabic-english-word-by-word-translation.pdf'
SHA256 = '4f6c1a532dc9fe4bec3f09f72ffbb9be66753536b19e268850281d05874adc82'
ROOT = Path(__file__).resolve().parents[1]

def prepare(source):
    if hashlib.sha256(source.read_bytes()).hexdigest() != SHA256:
        raise ValueError('Source checksum mismatch; refusing to bundle an unverified document')
    reader = PdfReader(source)
    if len(reader.pages) != 962: raise ValueError('Unexpected source page count')
    positions = [reader.get_destination_page_number(d) for d in reader.outline
                 if not isinstance(d, list) and str(d.title).startswith('Juz ')]
    if positions != [1 + 32 * i for i in range(30)]: raise ValueError('Unexpected Juz boundaries')
    writer = PdfWriter()
    for page in reader.pages[1:961]: writer.add_page(page)
    for i in range(30): writer.add_outline_item('Juz ' + str(i+1), i*32)
    writer.add_metadata({'/Title': 'Qur’an Word by Word', '/Subject': 'Arabic–English, 30 Juz, 960 pages'})
    output = ROOT / 'app/src/main/assets/quran-960.pdf'
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary = output.with_suffix('.tmp')
    with temporary.open('wb') as f: writer.write(f)
    if len(PdfReader(temporary).pages) != 960: raise ValueError('Bundled PDF page count mismatch')
    temporary.replace(output)
    print('Prepared 960 pages and 30 verified Juz starts:', output.name)

if __name__ == '__main__':
    parser = argparse.ArgumentParser(); parser.add_argument('--source', type=Path); args = parser.parse_args()
    if args.source: prepare(args.source)
    else:
        with tempfile.TemporaryDirectory() as directory:
            source = Path(directory) / 'source.pdf'
            request = urllib.request.Request(URL, headers={'User-Agent': 'QuranWordByWord-Build/2.0'})
            with urllib.request.urlopen(request, timeout=180) as response, source.open('wb') as output:
                shutil.copyfileobj(response, output)
            prepare(source)
