'use strict';
// The original JPEG bytes retain every printed letter, margin and Tajweed colour.
const SIXTEEN_SOURCE='https://quranpdf.wordpress.com/wp-content/uploads/2014/12/quran-16-lines-tajwedi-hammad-company1.pdf';
backupKeys.push('tm-sixteen-page','tm-sixteen-bookmarks');
const sixteenSearchTerm=s=>s.toLowerCase().normalize('NFKD').replace(/[\u064B-\u065F\u0670\u06D6-\u06ED]/g,'').replace(/[أإآٱ]/g,'ا').replace(/[\s'-]/g,'');
function sixteenPageLabel(n){return n>=3&&n<=550?'Printed page '+(n-1)+' · PDF page '+n:'Source PDF page '+n}
function openSixteenSource(){
 openSheet('16-line colour-coded Tajweed',`<p class="helper">The complete original edition has 561 PDF pages. Reading opens at Al-Fatihah. Covers, printed contents and Tajweed reference pages are included, with the source’s colours unchanged.</p><div class="tools-actions"><a class="action" href="${SIXTEEN_SOURCE}" target="_blank" rel="noopener">Open / download original PDF · 120 MB</a><button data-sixteen-start="3">Read Al-Fatihah</button><button data-sixteen-start="553">Printed contents</button><button data-sixteen-start="555">Tajweed reference</button></div><p class="helper">On iPhone or iPad, open the PDF and use Share → Save to Files. To read offline inside this app, choose Downloads & offline reading and save Mode 7, a Juz, or your current page.</p>`);
 document.querySelectorAll('[data-sixteen-start]').forEach(b=>b.onclick=()=>{changeMode('sixteen');go(Number(b.dataset.sixteenStart))});
}
const modeFiveScreen=screen;
screen=function(id){
 if(id==='modes'){
  modeFiveScreen(id);const card=document.createElement('button');card.id='sixteen-mode';card.className='mode-card'+(mode==='sixteen'?' active':'');card.innerHTML='<strong>5. Colour-coded Tajweed · 16 lines</strong><small>Original printed Arabic Mushaf · 561 source pages<br>Full colours, page bookmarks and offline downloads</small>';card.onclick=()=>changeMode('sixteen');$('sheet').querySelector('.sheet-content').appendChild(card);return;
 }
 if(mode==='sixteen'){
  if(id==='source'){openSixteenSource();return}
  if(id==='surahs'){
   openSheet('16-line Tajweed · Surahs','<input id="sixteen-search" class="search" placeholder="Find a Surah by name or number" aria-label="Find a Surah"><div id="sixteen-surahs"></div><p class="helper">Some Surahs begin partway down a page. Every link opens the page containing that Surah’s heading and first ayah.</p>');
   const list=()=>{const query=sixteenSearchTerm($('sixteen-search').value.trim());$('sixteen-surahs').innerHTML=SIXTEEN_NAV.surahs.filter(s=>{const q=QURAN.surahs[s.number-1];return sixteenSearchTerm(s.number+' '+q.name+' '+q.ar).includes(query)}).map(s=>{const q=QURAN.surahs[s.number-1];return `<button class="list-row" data-page="${s.page}" data-surah="${s.number}"><span class="n">${s.number}</span><span class="names">${esc(q.name)}<small>${sixteenPageLabel(s.page)}</small></span><span class="ar-name" lang="ar">${esc(q.ar)}</span></button>`}).join('')||'<p class="helper">No matching Surah. Try its name or number.</p>';wireJumps()};$('sixteen-search').oninput=list;list();return;
  }
  if(id==='juz'){
   openSheet('16-line Tajweed · Juz',SIXTEEN_NAV.juz.map(j=>`<button class="list-row" data-page="${j.page}"><span class="n">${j.number}</span><span class="names">Juz ${j.number}<small>${j.number===1?'Al-Fatihah · ':''}${sixteenPageLabel(j.page)}</small></span><span>‹</span></button>`).join(''));wireJumps();return;
  }
  if(id==='jump'){
   openSheet('16-line Tajweed · Go to page',`<p class="helper">Enter a PDF page number from 1 to 561. For the Qur’an pages, PDF page = printed page + 1.</p><form id="sixteen-jump"><input id="sixteen-number" class="search" type="number" min="1" max="561" required value="${page}" aria-label="Source PDF page number"><button class="action">Open page</button><p id="sixteen-error" class="helper" role="status"></p></form><div class="tools-actions"><button data-page="1">Cover</button><button data-page="3">Al-Fatihah</button><button data-page="530">Juz 30</button><button data-page="553">Contents</button><button data-page="555">Tajweed reference</button></div>`);wireJumps();$('sixteen-jump').onsubmit=e=>{e.preventDefault();const n=Number($('sixteen-number').value);if(Number.isInteger(n)&&n>=1&&n<=561)go(n);else $('sixteen-error').textContent='Enter a whole number from 1 to 561.'};return;
  }
  if(id==='bookmarks'){
   openSheet('16-line Tajweed · Bookmarks',marks.length?marks.map(p=>`<div class="list-row"><button class="names" data-page="${p}">${sixteenPageLabel(p)}<small>Mode 7 · 16-line Tajweed</small></button><button data-sixteen-remove="${p}" aria-label="Remove bookmark">×</button></div>`).join(''):'<p class="helper">Tap the star while reading to save your page.</p>');wireJumps();document.querySelectorAll('[data-sixteen-remove]').forEach(b=>b.onclick=()=>{marks=marks.filter(p=>p!==Number(b.dataset.sixteenRemove));save(bookmarkKey(),marks);updateBookmark();screen('bookmarks')});return;
  }
  if(id==='rules'){
   openSheet('16-line edition · Tajweed reference','<p class="helper">Mode 7 preserves this edition’s own printed colour markings. Read its original reference pages below.</p>'+[555,556,557,558,559,560].map(p=>`<button class="list-row" data-page="${p}"><span class="names">Reference · PDF page ${p}<small>${p===555?'Urdu':p===556||p===557?'English letter characteristics':'Printed recitation reference'}</small></span></button>`).join(''));wireJumps();return;
  }
 }
 modeFiveScreen(id);
 if(id==='settings'&&mode==='sixteen'&&settingsTab!=='general'){const content=$('sheet').querySelector('.settings-section-body');content.insertAdjacentHTML('beforeend',checkSetting('sixteen-fullscreen','Mode 7 full-page on opening',prefs.sixteenFullscreen!==false,'Use the same full reading area as Mode 5.')+checkSetting('sixteen-minimal','Mode 7 clear reading controls',prefs.sixteenMinimal!==false,'Tap the page to show menu and bookmark without covering the Arabic.')+checkSetting('sixteen-footer','Show Mode 7 floating page controls',prefs.sixteenFooter===true,'Optional page number and turn buttons.'));for(const [id,key]of [['sixteen-fullscreen','sixteenFullscreen'],['sixteen-minimal','sixteenMinimal'],['sixteen-footer','sixteenFooter']])$(id).onchange=()=>{prefs[key]=$(id).checked;applyPrefs()};}
 if(id==='about')$('sheet').querySelector('.sheet-content').insertAdjacentHTML('afterbegin',`<h3>Mode 7 · 16-line colour-coded Tajweed</h3><p>All 561 pages of the requested edition, displayed using the original embedded JPEG scans without rewriting or recolouring. Choose an Arabic-frame layout for larger text, or Original complete page for every printed margin note.</p><p><a href="${SIXTEEN_SOURCE}" target="_blank" rel="noopener">Original PDF from QuranPDF</a></p>`);
};
const modeFiveDrawer=drawer;drawer=function(){modeFiveDrawer();const source=$('drawer').querySelector('[data-screen="source"]');if(source&&mode==='sixteen')source.innerHTML='<span class="icon">▤</span>16-line Tajweed · original PDF';$('drawer').querySelector('.drawer-bottom').insertAdjacentHTML('beforebegin','<button id="sixteen-book" class="drawer-row"><span class="icon">▤</span>16-line Tajweed · complete PDF</button>');$('sixteen-book').onclick=openSixteenSource};$('menu').onclick=drawer;if($('focus-menu'))$('focus-menu').onclick=drawer;
$('page').addEventListener('load',()=>{if(mode==='sixteen')requestAnimationFrame(alignMagnifiedPage)},true);
if(mode==='sixteen'){applyPrefs();render()}

// Reuse Mode 5's quiet full-page reading controls without sharing its preferences.
const sixteenAppearance=readerAppearance;readerAppearance=function(){sixteenAppearance();if(mode==='sixteen'){document.body.dataset.minimalMushaf=String(prefs.sixteenMinimal!==false);document.body.dataset.hideMushafFooter=String(prefs.sixteenFooter!==true)}};
const sixteenChangeMode=changeMode;changeMode=function(next){sixteenChangeMode(next);if(next==='sixteen'&&prefs.sixteenFullscreen!==false)fullscreen(true)};
const sixteenValidation=validateBackup;validateBackup=function(b){sixteenValidation(b);const p=b.data['tm-settings'];if(p)for(const k of ['sixteenFullscreen','sixteenMinimal','sixteenFooter'])if(p[k]!=null&&typeof p[k]!=='boolean')throw Error('Invalid Mode 7 display setting')};
applyPrefs();if(mode==='sixteen'&&prefs.sixteenFullscreen!==false)fullscreen(true);
// Mouse dragging mirrors touch panning on magnified pages; clicks after a drag are suppressed.
let sixteenDrag=null;
reader.addEventListener('pointerdown',e=>{const stage=$('pdf-stage');if(mode!=='sixteen'||e.pointerType!=='mouse'||e.button!==0||zoomForMode()<=100||!stage||!stage.contains(e.target))return;sixteenDrag={id:e.pointerId,x:e.clientX,y:e.clientY,left:stage.scrollLeft,top:stage.scrollTop,moved:false,stage};stage.setPointerCapture(e.pointerId);e.preventDefault()});
reader.addEventListener('pointermove',e=>{const g=sixteenDrag;if(!g||g.id!==e.pointerId)return;const dx=e.clientX-g.x,dy=e.clientY-g.y;if(Math.hypot(dx,dy)>5)g.moved=true;g.stage.scrollLeft=g.left-dx;g.stage.scrollTop=g.top-dy;e.preventDefault()});
function finishSixteenDrag(e){if(sixteenDrag?.id!==e.pointerId)return;if(sixteenDrag.moved)suppressClickUntil=Date.now()+600;sixteenDrag=null}
reader.addEventListener('pointerup',finishSixteenDrag);reader.addEventListener('pointercancel',finishSixteenDrag);
reader.addEventListener('dragstart',e=>{if(mode==='sixteen')e.preventDefault()});

// Frame-only views use the same original JPEG, positioned inside a clipping window.
// Covers and references keep their complete contents in every layout.
function prepareSixteenFrame(image){
 const frame=document.createElement('div');frame.className='sixteen-frame';image.replaceWith(frame);frame.appendChild(image);sizeSixteenFrame(frame);
}
function sizeSixteenFrame(frame){
 const image=frame.querySelector('img'),stage=$('pdf-stage');if(!image||!stage)return;
 const n=Number(image.dataset.page),layout=layoutForMode(),focused=layout.startsWith('sixteen-');
 const original=[0,0,image.naturalWidth,image.naturalHeight],crop=focused?(SIXTEEN_FRAMES[n]||original):original;
 const [x,y,w,h]=crop,sw=stage.clientWidth,sh=stage.clientHeight,z=zoomForMode()/100;
 let vw=sw,vh=sh;
 if(z>1||layout==='sixteen-width'||layout==='width'){vw=sw*z;vh=vw*h/w}
 else if(layout==='sixteen-fit'||layout==='proportions'||(focused&&!SIXTEEN_FRAMES[n])){const scale=Math.min(sw/w,sh/h);vw=w*scale;vh=h*scale}
 frame.style.cssText=`position:absolute;overflow:hidden;width:${vw}px;height:${vh}px;left:${vw<sw?(sw-vw)/2:0}px;top:${vh<sh?(sh-vh)/2:0}px;`;
 image.style.width=(image.naturalWidth*vw/w)+'px';image.style.height=(image.naturalHeight*vh/h)+'px';image.style.left=(-x*vw/w)+'px';image.style.top=(-y*vh/h)+'px';image.style.right='auto';image.style.bottom='auto';image.style.objectFit='fill';
 stage.style.overflow=z>1||layout.endsWith('width')?'auto':'hidden';
}
function updateSixteenFrames(){if(mode!=='sixteen')return;$('pdf-stage')?.querySelectorAll('.sixteen-frame').forEach(sizeSixteenFrame)}
const framedAppearance=readerAppearance;readerAppearance=function(){framedAppearance();updateSixteenFrames()};
// Adopt the photo-style layout once; later choices remain saved independently.
if(!prefs.sixteenFrameLayout){prefs.sixteenFrameLayout=true;prefs.layouts={...(prefs.layouts||{}),sixteen:'sixteen-full'};save('tm-settings',prefs)}
const framedScreen=screen;screen=function(id){framedScreen(id);if(id==='settings'&&mode==='sixteen'&&settingsTab!=='general')$('pdf-fit').previousElementSibling.innerHTML='Mode 7 page layout<small>16-line full screen fills the reading area. Fit Arabic area preserves letter proportions. Arabic width gives larger text with vertical scrolling. Original complete page includes all printed margin notes.</small>'};
applyPrefs();
