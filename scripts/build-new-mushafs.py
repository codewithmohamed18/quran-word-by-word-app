from pathlib import Path
import hashlib,json,shutil,time,urllib.request
from concurrent.futures import ThreadPoolExecutor
import fitz
from PIL import Image
ROOT=Path(__file__).resolve().parents[1];OUTPUT=ROOT/'dist/web/pages';OUTPUT.mkdir(parents=True,exist_ok=True)
def download(c,name):
 p=ROOT/'dist/sources'/name;p.parent.mkdir(parents=True,exist_ok=True)
 if not p.exists():
  for attempt in range(4):
   try:
    with urllib.request.urlopen(urllib.request.Request(c['url'],headers={'User-Agent':'Mozilla/5.0'}),timeout=240) as response,p.open('wb') as out:shutil.copyfileobj(response,out)
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
def verify_madina(p,i):
 im=Image.open(p).convert('RGB');check=m['images'][str(i)]
 assert list(im.size)==check['size'] and hashlib.sha256(im.tobytes()).hexdigest()==check['pixelSha256'],f'Madina page {i} mismatch'
try:source=download(m,'hafs-madina.pdf')
except Exception as e:
 # Some source hosts reject GitHub runners. Recover the approved lossless
 # renders, checking every decoded pixel against the committed manifest.
 print('Original PDF unavailable; recovering verified approved renders:',type(e).__name__,flush=True)
 def recover(i):
  p=OUTPUT/f'madina-{i}.webp'
  for attempt in range(4):
   try:
    url='https://tajweed-meaning-ipad.lazify.chatgpt.site/pages/'+p.name
    with urllib.request.urlopen(url,timeout=90) as response,p.open('wb') as out:shutil.copyfileobj(response,out)
    verify_madina(p,i);return
   except Exception:
    p.unlink(missing_ok=True)
    if attempt==3:raise
    time.sleep(2)
 with ThreadPoolExecutor(max_workers=8) as pool:list(pool.map(recover,range(1,606)))
else:
 with fitz.open(source) as doc:
  assert len(doc)==m['pages']
  for i,page in enumerate(doc,1):
   pix=page.get_pixmap(matrix=fitz.Matrix(m['scale'],m['scale']),alpha=False);check=m['images'][str(i)]
   assert [pix.width,pix.height]==check['size'];assert hashlib.sha256(pix.samples).hexdigest()==check['pixelSha256'],f'Madina page {i} mismatch'
   im=Image.frombytes('RGB',(pix.width,pix.height),pix.samples);p=OUTPUT/f'madina-{i}.webp';im.save(p,lossless=True,method=6);verify_madina(p,i)
   if i%100==0:print('Verified Madina page',i,flush=True)
print('Verified cover and all 604 Madina pages',flush=True)
