'use strict';
// The original JPEG bytes retain every printed letter, margin and Tajweed colour.
const FIFTEEN_SOURCE='https://quranpdf.wordpress.com/wp-content/uploads/2014/12/quran-color-coded-in-15-lines-with-rules-of-tajweed.pdf';
backupKeys.push('tm-fifteen-page','tm-fifteen-bookmarks');
const fifteenSearchTerm=s=>s.toLowerCase().normalize('NFKD').replace(/[\u064B-\u065F\u0670\u06D6-\u06ED]/g,'').replace(/[أإآٱ]/g,'ا').replace(/[\s'-]/g,'');
function fifteenPageLabel(n){return n>=30&&n<=639?'Printed page '+(n-28)+' · PDF page '+n:'Source PDF page '+n}
function openFifteenSource(){
 openSheet('15-line colour-coded Tajweed',`<p class="helper">The complete original edition has 641 PDF pages. Reading opens at Al-Fatihah. The introduction and Tajweed reference pages are included, with the source’s colours unchanged.</p><div class="tools-actions"><a class="action" href="${FIFTEEN_SOURCE}" target="_blank" rel="noopener">Open / download original PDF · 133 MB</a><button data-fifteen-start="30">Read Al-Fatihah</button><button data-fifteen-start="1">Edition introduction</button><button data-fifteen-start="29">Tajweed reference</button></div><p class="helper">On iPhone or iPad, open the PDF and use Share → Save to Files. To read offline inside this app, choose Downloads & offline reading and save Mode 6, a Juz, or your current page.</p>`);
 document.querySelectorAll('[data-fifteen-start]').forEach(b=>b.onclick=()=>{changeMode('fifteen');go(Number(b.dataset.fifteenStart))});
}
const modeSixScreen=screen;
screen=function(id){
 if(id==='modes'){
  modeSixScreen(id);const card=document.createElement('button');card.id='fifteen-mode';card.className='mode-card'+(mode==='fifteen'?' active':'');card.innerHTML='<strong>6. Colour-coded Tajweed · 15 lines</strong><small>Original printed Arabic Mushaf · 641 source pages<br>Full colours, page bookmarks and offline downloads</small>';card.onclick=()=>changeMode('fifteen');$('sheet').querySelector('.sheet-content').appendChild(card);return;
 }
 if(mode==='fifteen'){
  if(id==='source'){openFifteenSource();return}
  if(id==='surahs'){
   openSheet('15-line Tajweed · Surahs','<input id="fifteen-search" class="search" placeholder="Find a Surah by name or number" aria-label="Find a Surah"><div id="fifteen-surahs"></div><p class="helper">Some Surahs begin partway down a page. Every link opens the page containing that Surah’s heading and first ayah.</p>');
   const list=()=>{const query=fifteenSearchTerm($('fifteen-search').value.trim());$('fifteen-surahs').innerHTML=FIFTEEN_NAV.surahs.filter(s=>{const q=QURAN.surahs[s.number-1];return fifteenSearchTerm(s.number+' '+q.name+' '+q.ar).includes(query)}).map(s=>{const q=QURAN.surahs[s.number-1];return `<button class="list-row" data-page="${s.page}" data-surah="${s.number}"><span class="n">${s.number}</span><span class="names">${esc(q.name)}<small>${fifteenPageLabel(s.page)}</small></span><span class="ar-name" lang="ar">${esc(q.ar)}</span></button>`}).join('')||'<p class="helper">No matching Surah. Try its name or number.</p>';wireJumps()};$('fifteen-search').oninput=list;list();return;
  }
  if(id==='juz'){
   openSheet('15-line Tajweed · Juz',FIFTEEN_NAV.juz.map(j=>`<button class="list-row" data-page="${j.page}"><span class="n">${j.number}</span><span class="names">Juz ${j.number}<small>${j.number===1?'Al-Fatihah · ':''}${fifteenPageLabel(j.page)}</small></span><span>‹</span></button>`).join(''));wireJumps();return;
  }
  if(id==='jump'){
   openSheet('15-line Tajweed · Go to page',`<p class="helper">Enter a PDF page number from 1 to 641. For the Qur’an pages, PDF page = printed page + 28.</p><form id="fifteen-jump"><input id="fifteen-number" class="search" type="number" min="1" max="641" required value="${page}" aria-label="Source PDF page number"><button class="action">Open page</button><p id="fifteen-error" class="helper" role="status"></p></form><div class="tools-actions"><button data-page="1">Cover</button><button data-page="30">Al-Fatihah</button><button data-page="615">Juz 30</button><button data-page="1">Introduction</button><button data-page="29">Tajweed reference</button></div>`);wireJumps();$('fifteen-jump').onsubmit=e=>{e.preventDefault();const n=Number($('fifteen-number').value);if(Number.isInteger(n)&&n>=1&&n<=641)go(n);else $('fifteen-error').textContent='Enter a whole number from 1 to 641.'};return;
  }
  if(id==='bookmarks'){
   openSheet('15-line Tajweed · Bookmarks',marks.length?marks.map(p=>`<div class="list-row"><button class="names" data-page="${p}">${fifteenPageLabel(p)}<small>Mode 6 · 15-line Tajweed</small></button><button data-fifteen-remove="${p}" aria-label="Remove bookmark">×</button></div>`).join(''):'<p class="helper">Tap the star while reading to save your page.</p>');wireJumps();document.querySelectorAll('[data-fifteen-remove]').forEach(b=>b.onclick=()=>{marks=marks.filter(p=>p!==Number(b.dataset.fifteenRemove));save(bookmarkKey(),marks);updateBookmark();screen('bookmarks')});return;
  }
  if(id==='rules'){
   openSheet('15-line edition · Tajweed reference','<p class="helper">Mode 6 preserves this edition’s own printed colour markings. Read its original reference pages below.</p>'+Array.from({length:28},(_,i)=>i+2).map(p=>`<button class="list-row" data-page="${p}"><span class="names">Reference · PDF page ${p}<small>${p===29?'Printed colour-coded Tajweed key':'Original English recitation rules'}</small></span></button>`).join(''));wireJumps();return;
  }
 }
 modeSixScreen(id);
 if(id==='settings'&&mode==='fifteen'&&settingsTab!=='general'){const content=$('sheet').querySelector('.settings-section-body');content.insertAdjacentHTML('beforeend',checkSetting('fifteen-fullscreen','Mode 6 full-page on opening',prefs.fifteenFullscreen!==false,'Use the same full reading area as Mode 5.')+checkSetting('fifteen-minimal','Mode 6 clear reading controls',prefs.fifteenMinimal!==false,'Tap the page to show menu and bookmark without covering the Arabic.')+checkSetting('fifteen-footer','Show Mode 6 floating page controls',prefs.fifteenFooter===true,'Optional page number and turn buttons.'));for(const [id,key]of [['fifteen-fullscreen','fifteenFullscreen'],['fifteen-minimal','fifteenMinimal'],['fifteen-footer','fifteenFooter']])$(id).onchange=()=>{prefs[key]=$(id).checked;applyPrefs()};}
 if(id==='about')$('sheet').querySelector('.sheet-content').insertAdjacentHTML('afterbegin',`<h3>Mode 6 · 15-line colour-coded Tajweed</h3><p>All 641 pages of the requested edition, displayed using the original embedded JPEG scans without rewriting or recolouring. Choose an Arabic-frame layout for larger text, or Original complete page for every printed margin note.</p><p><a href="${FIFTEEN_SOURCE}" target="_blank" rel="noopener">Original PDF from QuranPDF</a></p>`);
};
const modeSixDrawer=drawer;drawer=function(){modeSixDrawer();const source=$('drawer').querySelector('[data-screen="source"]');if(source&&mode==='fifteen')source.innerHTML='<span class="icon">▤</span>15-line Tajweed · original PDF';$('drawer').querySelector('.drawer-bottom').insertAdjacentHTML('beforebegin','<button id="fifteen-book" class="drawer-row"><span class="icon">▤</span>15-line Tajweed · complete PDF</button>');$('fifteen-book').onclick=openFifteenSource};$('menu').onclick=drawer;if($('focus-menu'))$('focus-menu').onclick=drawer;
$('page').addEventListener('load',()=>{if(mode==='fifteen')requestAnimationFrame(alignMagnifiedPage)},true);
if(mode==='fifteen'){applyPrefs();render()}

// Reuse Mode 5's quiet full-page reading controls without sharing its preferences.
const fifteenAppearance=readerAppearance;readerAppearance=function(){fifteenAppearance();if(mode==='fifteen'){document.body.dataset.minimalMushaf=String(prefs.fifteenMinimal!==false);document.body.dataset.hideMushafFooter=String(prefs.fifteenFooter!==true)}};
const fifteenChangeMode=changeMode;changeMode=function(next){fifteenChangeMode(next);if(next==='fifteen'&&prefs.fifteenFullscreen!==false)fullscreen(true)};
const fifteenValidation=validateBackup;validateBackup=function(b){fifteenValidation(b);const p=b.data['tm-settings'];if(p)for(const k of ['fifteenFullscreen','fifteenMinimal','fifteenFooter'])if(p[k]!=null&&typeof p[k]!=='boolean')throw Error('Invalid Mode 6 display setting')};
applyPrefs();if(mode==='fifteen'&&prefs.fifteenFullscreen!==false)fullscreen(true);
// Mouse dragging mirrors touch panning on magnified pages; clicks after a drag are suppressed.
let fifteenDrag=null;
reader.addEventListener('pointerdown',e=>{const stage=$('pdf-stage');if(mode!=='fifteen'||e.pointerType!=='mouse'||e.button!==0||zoomForMode()<=100||!stage||!stage.contains(e.target))return;fifteenDrag={id:e.pointerId,x:e.clientX,y:e.clientY,left:stage.scrollLeft,top:stage.scrollTop,moved:false,stage};stage.setPointerCapture(e.pointerId);e.preventDefault()});
reader.addEventListener('pointermove',e=>{const g=fifteenDrag;if(!g||g.id!==e.pointerId)return;const dx=e.clientX-g.x,dy=e.clientY-g.y;if(Math.hypot(dx,dy)>5)g.moved=true;g.stage.scrollLeft=g.left-dx;g.stage.scrollTop=g.top-dy;e.preventDefault()});
function finishFifteenDrag(e){if(fifteenDrag?.id!==e.pointerId)return;if(fifteenDrag.moved)suppressClickUntil=Date.now()+600;fifteenDrag=null}
reader.addEventListener('pointerup',finishFifteenDrag);reader.addEventListener('pointercancel',finishFifteenDrag);
reader.addEventListener('dragstart',e=>{if(mode==='fifteen')e.preventDefault()});

// Frame-only views use the same original JPEG, positioned inside a clipping window.
// Covers and references keep their complete contents in every layout.
function prepareFifteenFrame(image){
 const frame=document.createElement('div');frame.className='fifteen-frame';image.replaceWith(frame);frame.appendChild(image);sizeFifteenFrame(frame);
}
function sizeFifteenFrame(frame){
 const image=frame.querySelector('img'),stage=$('pdf-stage');if(!image||!stage)return;
 const n=Number(image.dataset.page),layout=layoutForMode(),focused=layout.startsWith('fifteen-');
 const original=[0,0,image.naturalWidth,image.naturalHeight],crop=focused?(FIFTEEN_FRAMES[n]||original):original;
 const [x,y,w,h]=crop,sw=stage.clientWidth,sh=stage.clientHeight,z=zoomForMode()/100;
 let vw=sw,vh=sh;
 if(z>1||layout==='fifteen-width'||layout==='width'){vw=sw*z;vh=vw*h/w}
 else if(layout==='fifteen-fit'||layout==='proportions'||(focused&&!FIFTEEN_FRAMES[n])){const scale=Math.min(sw/w,sh/h);vw=w*scale;vh=h*scale}
 frame.style.cssText=`position:absolute;overflow:hidden;width:${vw}px;height:${vh}px;left:${vw<sw?(sw-vw)/2:0}px;top:${vh<sh?(sh-vh)/2:0}px;`;
 image.style.width=(image.naturalWidth*vw/w)+'px';image.style.height=(image.naturalHeight*vh/h)+'px';image.style.left=(-x*vw/w)+'px';image.style.top=(-y*vh/h)+'px';image.style.right='auto';image.style.bottom='auto';image.style.objectFit='fill';
 stage.style.overflow=z>1||layout.endsWith('width')?'auto':'hidden';
}
function updateFifteenFrames(){if(mode!=='fifteen')return;$('pdf-stage')?.querySelectorAll('.fifteen-frame').forEach(sizeFifteenFrame)}
const fifteenFramedAppearance=readerAppearance;readerAppearance=function(){fifteenFramedAppearance();updateFifteenFrames()};
// Adopt the photo-style layout once; later choices remain saved independently.
if(!prefs.fifteenFrameLayout){prefs.fifteenFrameLayout=true;prefs.layouts={...(prefs.layouts||{}),fifteen:'fifteen-full'};save('tm-settings',prefs)}
const fifteenFramedScreen=screen;screen=function(id){fifteenFramedScreen(id);if(id==='settings'&&mode==='fifteen'&&settingsTab!=='general')$('pdf-fit').previousElementSibling.innerHTML='Mode 6 page layout<small>15-line full screen fills the reading area. Fit Arabic area preserves letter proportions. Arabic width gives larger text with vertical scrolling. Original complete page includes all printed margin notes.</small>'};
applyPrefs();
