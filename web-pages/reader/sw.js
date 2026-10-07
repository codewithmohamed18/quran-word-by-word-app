const SHELL='quran-shell-v7',PAGES='quran-pages-v1';
const ASSETS=['./','index.html','app.js','data.js','word-navigation.js','plain-navigation.js','adaptive.js','adaptive.css','settings.js','web.css','page-meta.js','web-bridge.js','web-install.js','web-reader.js','web-upgrade.js','web-upgrade.css','web-offline.js','fonts/IndoPak.otf','web-study.js','study-navigation.js','study-meta.js','study-download.js','Uthmani.ttf','plain-rules.jpg','manifest.json','icon.svg','icon.png'].map(url=>url.endsWith('.js')?url+'?v=7':url);
self.addEventListener('install',e=>e.waitUntil((async()=>{const cache=await caches.open(SHELL);await Promise.all(ASSETS.map(async asset=>{if(await cache.match(asset))return;let failure;for(let attempt=0;attempt<3;attempt++){try{const r=await fetch(new Request(asset,{cache:'reload'}));if(!r.ok||r.type==='opaque')throw Error('Resource unavailable: '+asset);await cache.put(asset,r);failure=null;break}catch(error){failure=error}}if(failure)throw failure}));await self.skipWaiting()})()));
self.addEventListener('activate',e=>e.waitUntil(caches.keys().then(keys=>Promise.all(keys.filter(k=>k.startsWith('quran-shell-')&&k!==SHELL).map(k=>caches.delete(k)))).then(()=>self.clients.claim())));
self.addEventListener('fetch',e=>{const u=new URL(e.request.url);if(e.request.method!=='GET'||u.origin!==self.location.origin)return;
 if(u.pathname.includes('/study-pdf/')){e.respondWith(fetch(e.request));return}
 if(u.pathname.includes('/pages/')){e.respondWith(caches.open(PAGES).then(async c=>{const saved=await c.match(e.request);if(saved)return saved;const r=await fetch(e.request);if(r.ok&&r.headers.get('content-type')?.includes('image/')){try{await c.put(e.request,r.clone())}catch{}}return r}));return}
 if(e.request.mode==='navigate'){// Installed reader opens its versioned saved shell immediately, even on slow networks.
 e.respondWith(caches.open(SHELL).then(async c=>(await c.match('index.html'))||fetch(e.request)));return}
 e.respondWith(caches.open(SHELL).then(async c=>(await c.match(e.request))||fetch(e.request)));
});
