'use strict';
let webWorkerReady;
if('serviceWorker' in navigator)webWorkerReady=navigator.serviceWorker.register('./sw.js').then(()=>navigator.serviceWorker.ready).catch(()=>null);
const webDrawer=drawer;
drawer=function(){webDrawer();const bottom=$('drawer').querySelector('.drawer-bottom');bottom.innerHTML='Complete Qur’an · Save pages for offline reading<br>No advertisements';bottom.insertAdjacentHTML('beforebegin','<button class="drawer-row" id="web-offline"><span class="icon">↓</span>Save for offline reading</button>');$('web-offline').onclick=offlineScreen};
$('menu').onclick=drawer;if($('focus-menu'))$('focus-menu').onclick=drawer;
function offlineScreen(){openSheet('Offline reading','<p class="helper">Pages you open are saved automatically when browser storage is available. Save all three modes below to read without internet. Keep this app’s Safari data to retain your downloads and bookmarks.</p><p id="offline-status">Checking saved pages…</p><button class="action" id="download-quran">Save complete Qur’an</button><button id="cancel-download" class="hidden">Stop download</button>');
 const status=$('offline-status');
 if(!('caches' in window)){status.textContent='Offline storage is unavailable in this browser.';$('download-quran').disabled=true;return}
 caches.open('quran-pages-v1').then(c=>c.keys()).then(keys=>{if(status.isConnected)status.textContent=`${keys.length} of 1810 scanned pages saved.`});
 $('download-quran').onclick=async()=>{let stop=false;$('download-quran').disabled=true;const cancel=$('cancel-download');cancel.classList.remove('hidden');cancel.onclick=()=>{stop=true};try{await webWorkerReady;const cache=await caches.open('quran-pages-v1');let done=0;for(const [m,total] of [['word',960],['plain',850]]){for(let n=1;n<=total&&!stop;n++){const url=`pages/${m}-${n}.${SCANS[m][n-1].ext}`;if(!await cache.match(url)){const response=await fetch(url);if(!response.ok)throw Error('Download interrupted.');await cache.put(url,response)}done++;status.textContent=`Saved ${done} of 1810 scanned pages. Keep this screen open.`}if(stop)break}status.textContent=stop?'Stopped. Saved pages are kept; tap Save to resume.':'All scanned pages saved. Tajweed text is saved with the app. Browser storage can still be cleared by your device.';await navigator.storage?.persist?.()}catch(e){status.textContent='Could not finish saving. Check your connection and free storage, then try again. Saved pages are kept.'}finally{cancel.classList.add('hidden');$('download-quran').disabled=false}};
}
if(!navigator.standalone&&!matchMedia('(display-mode: standalone)').matches&&!load('tm-install-tip',false)){
const tip=document.createElement('div');tip.id='install-tip';tip.innerHTML='<button aria-label="Dismiss installation tip">×</button><strong>Read from your Home Screen</strong><br>On iPad or iPhone: open in Safari, tap Share, then Add to Home Screen.';document.body.appendChild(tip);tip.querySelector('button').onclick=()=>{save('tm-install-tip',true);tip.remove()};}
document.addEventListener('visibilitychange',()=>{if(!document.hidden&&prefs.awake)Android.awake(true)});

const browserScreen=screen;screen=function(id){browserScreen(id);if(id==='settings'){const quality=$('word-quality');if(quality)quality.closest('.setting').classList.add('hidden');const volume=$('volume');if(volume)volume.closest('.setting').classList.add('hidden')}};
