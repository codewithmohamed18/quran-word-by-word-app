"""Package a verified web snapshot without modifying either published website."""
from pathlib import Path
import hashlib,json,shutil,subprocess,urllib.request
ROOT=Path(__file__).resolve().parents[1]
WEB_SHA='175a4558da4de22aba248359dc7f46f741530462'
web=ROOT/'dist/web-source'
if not web.exists():subprocess.run(['git','clone','--depth','1','--branch','github-pages-web','https://github.com/codewithmohamed18/quran-word-by-word-app.git',str(web)],check=True)
assert subprocess.check_output(['git','rev-parse','HEAD'],cwd=web,text=True).strip()==WEB_SHA,'Web snapshot changed; review before updating v1.9'
subprocess.run(['python',str(web/'scripts/build-pages.py')],check=True)
site=ROOT/'dist/v19-assets/site'
shutil.copytree(web/'dist/web',site,dirs_exist_ok=True)
# Only the APK copy points every bundled Mode 1 scan at its local HTTPS asset host.
p=site/'web-bridge.js';s=p.read_text().replace("https://codewithmohamed18.github.io/quran-word-by-word-app","https://appassets.androidplatform.net");p.write_text(s)
p=site/'web-reader.js';s=p.read_text().replace('async function shareReading(s,a){','async function shareReading(s,a){if(window.NativeV19){NativeV19.shareLink(pageLink(mode,page,s,a).replace(location.origin,"https://codewithmohamed18.github.io").replace(location.pathname,"/quran-word-by-word-app/"));return;}');p.write_text(s)
assert len(list((site/'pages').glob('sixteen-*.jpeg')))==561
assert len(list((site/'pages').glob('word-*')))==960
assert len(list((site/'pages').glob('plain-*')))==850
manifest=json.loads((web/'web-pages/snapshot.json').read_text())
for name,sha in manifest['files'].items():
 if name not in ['web-bridge.js','web-reader.js']:assert hashlib.sha256((site/name).read_bytes()).hexdigest()==sha,name
(ROOT/'dist/v19-source.json').write_text(json.dumps({'web_commit':WEB_SHA,'approved_site_commit':manifest['source_commit'],'bundled_modes':[1,2,3,5,6,7],'downloadable_mode':4},indent=2)+'\n')
assert len(list((site/'pages').glob('fifteen-*.jpeg')))==641
assert len(list((site/'pages').glob('madina-*.webp')))==605
print('Verified seven-mode reader and all 3617 bundled scan pages')
