from pathlib import Path
import hashlib,json,shutil,time,urllib.request
import fitz
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];OUTPUT=ROOT/'dist/web/pages';OUTPUT.mkdir(parents=True,exist_ok=True)
def download(c,name):
 p=ROOT/'dist/sources'/name;p.parent.mkdir(parents=True,exist_ok=True)
 if not p.exists():
  for attempt in range(4):
   try:
    with urllib.request.urlopen(c['url'],timeout=240) as response,p.open('wb') as out:shutil.copyfileobj(response,out)
    break
   except Exception:
    p.unlink(missing_ok=True)
    if attempt==3:raise
    time.sleep(3)
 assert hashlib.sha256(p.read_bytes()).hexdigest()==c['sha256'],'Source PDF changed: '+name
 return p
f=json.loads((ROOT/'config/fifteen-source.json').read_text())
with fitz.open(download(f,'fifteen-original.pdf')) as doc:
 assert len(doc)==f['pages']
 for i,page in enumerate(doc,1):
  images=page.get_images();assert len(images)==1
  im=doc.extract_image(images[0][0]);assert im['ext']=='jpeg'
  name=f'fifteen-{i}.jpeg';assert hashlib.sha256(im['image']).hexdigest()==f['images'][name];(OUTPUT/name).write_bytes(im['image'])
print('Verified all 641 original 15-line Tajweed scans',flush=True)
m=json.loads((ROOT/'config/madina-source.json').read_text())
with fitz.open(download(m,'hafs-madina.pdf')) as doc:
 assert len(doc)==m['pages']
 for i,page in enumerate(doc,1):
  pix=page.get_pixmap(matrix=fitz.Matrix(m['scale'],m['scale']),alpha=False);check=m['images'][str(i)]
  assert [pix.width,pix.height]==check['size'];assert hashlib.sha256(pix.samples).hexdigest()==check['pixelSha256'],f'Madina page {i} mismatch'
  im=Image.frombytes('RGB',(pix.width,pix.height),pix.samples);p=OUTPUT/f'madina-{i}.webp';im.save(p,lossless=True,method=6)
  assert hashlib.sha256(Image.open(p).convert('RGB').tobytes()).hexdigest()==check['pixelSha256']
  if i%100==0:print('Verified Madina page',i,flush=True)
print('Verified cover and all 604 Madina pages',flush=True)
