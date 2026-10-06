"""Download and verify Reading Mode 4 source PDFs for the optional web build."""
from pathlib import Path
import concurrent.futures,hashlib,json,subprocess,sys
import fitz
ROOT=Path(__file__).resolve().parents[1]
out=Path(sys.argv[1] if len(sys.argv)>1 else ROOT/'dist/study-source');out.mkdir(parents=True,exist_ok=True)
records=json.loads((ROOT/'config/study-sources.json').read_text())['files']
def fetch(r):
 dest=out/r['name']
 for attempt in range(3):
  if not dest.exists() or hashlib.sha256(dest.read_bytes()).hexdigest()!=r['sha256']:
   url=r['url']+('' if attempt==0 else f'?download={attempt}')
   subprocess.run(['curl','-sSL','--fail','--max-time','180',url,'-o',str(dest)],check=True)
  if hashlib.sha256(dest.read_bytes()).hexdigest()==r['sha256'] and len(fitz.open(dest))==r['pages']:
   print('Verified',r['name'],flush=True);return
 raise RuntimeError('Source file changed or was incomplete: '+r['name'])
with concurrent.futures.ThreadPoolExecutor(max_workers=3) as pool:list(pool.map(fetch,records))
print(out.resolve())
