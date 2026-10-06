const SHELL='quran-shell-v2',PAGES='quran-pages-v1';
const ASSETS=['./','index.html','app.js','data.js','word-navigation.js','plain-navigation.js','adaptive.js','adaptive.css','settings.js','web.css','page-meta.js','web-bridge.js','web-install.js','web-reader.js','Uthmani.ttf','plain-rules.jpg','manifest.json','icon.svg','icon.png'];
self.addEventListener('install',e=>e.waitUntil(caches.open(SHELL).then(c=>c.addAll(ASSETS)).then(()=>self.skipWaiting())));
self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k.startsWith('quran-shell-')&&k!==SHELL).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',e=>{const u=new URL(e.request.url);if(e.request.method!=='GET'||u.origin!==self.location.origin)return;
 if(u.pathname.includes('/pages/')){e.respondWith(caches.open(PAGES).then(async c=>{const saved=await c.match(e.request);if(saved)return saved;const r=await fetch(e.request);if(r.ok&&r.headers.get('content-type')?.includes('image/')){try{await c.put(e.request,r.clone())}catch{}}return r}));return}
 if(e.request.mode==='navigate'){e.respondWith(fetch(e.request).catch(()=>caches.open(SHELL).then(c=>c.match('index.html'))));return}
 e.respondWith(caches.open(SHELL).then(async c=>(await c.match(e.request))||fetch(e.request)));
});
