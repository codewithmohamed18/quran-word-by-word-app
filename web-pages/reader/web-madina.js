'use strict';
// Lossless WebP pages preserve the pixels rendered from the source vector PDF.
const MADINA_SOURCE='https://pdf.quran.ws/pdfs/hafs/quran-hafs-mushaf.pdf';
backupKeys.push('tm-madina-page','tm-madina-bookmarks');
const madinaSearchTerm=s=>s.toLowerCase().normalize('NFKD').replace(/[\u064B-\u065F\u0670\u06D6-\u06ED]/g,'').replace(/[أإآٱ]/g,'ا').replace(/[\s'-]/g,'');
function madinaPageLabel(n){return n>=2&&n<=605?'Mushaf page '+(n-1)+' · PDF page '+n:'Source PDF page '+n}
function openMadinaSource(){
 openSheet('15-line Madina Mushaf',`<p class="helper">The complete original edition has 605 PDF pages. Reading opens at Al-Fatihah. It contains a cover and all 604 Madani Mushaf pages in the Hafs reading. The source Arabic and page breaks are preserved.</p><div class="tools-actions"><a class="action" href="${MADINA_SOURCE}" target="_blank" rel="noopener">Open / download original PDF · 210 MB</a><button data-madina-start="2">Read Al-Fatihah</button></div><p class="helper">On iPhone or iPad, open the PDF and use Share → Save to Files. To read offline inside this app, choose Downloads & offline reading and save Mode 1, a Juz, or your current page.</p>`);
 document.querySelectorAll('[data-madina-start]').forEach(b=>b.onclick=()=>{changeMode('madina');go(Number(b.dataset.madinaStart))});
}
const modeSevenScreen=screen;
screen=function(id){
 if(id==='modes'){
  modeSevenScreen(id);const card=document.createElement('button');card.id='madina-mode';card.className='mode-card'+(mode==='madina'?' active':'');card.innerHTML='<strong>7. Madina Mushaf · 15 lines</strong><small>604 Madina Mushaf pages · Hafs<br>Original Uthmani Arabic, page bookmarks and offline downloads</small>';card.onclick=()=>changeMode('madina');$('sheet').querySelector('.sheet-content').prepend(card);return;
 }
 if(mode==='madina'){
  if(id==='source'){openMadinaSource();return}
  if(id==='surahs'){
   openSheet('Madina Mushaf · Surahs','<input id="madina-search" class="search" placeholder="Find a Surah by name or number" aria-label="Find a Surah"><div id="madina-surahs"></div><p class="helper">Some Surahs begin partway down a page. Every link opens the page containing that Surah’s heading and first ayah.</p>');
   const list=()=>{const query=madinaSearchTerm($('madina-search').value.trim());$('madina-surahs').innerHTML=MADINA_NAV.surahs.filter(s=>{const q=QURAN.surahs[s.number-1];return madinaSearchTerm(s.number+' '+q.name+' '+q.ar).includes(query)}).map(s=>{const q=QURAN.surahs[s.number-1];return `<button class="list-row" data-page="${s.page}" data-surah="${s.number}"><span class="n">${s.number}</span><span class="names">${esc(q.name)}<small>${madinaPageLabel(s.page)}</small></span><span class="ar-name" lang="ar">${esc(q.ar)}</span></button>`}).join('')||'<p class="helper">No matching Surah. Try its name or number.</p>';wireJumps()};$('madina-search').oninput=list;list();return;
  }
  if(id==='juz'){
   openSheet('Madina Mushaf · Juz',MADINA_NAV.juz.map(j=>`<button class="list-row" data-page="${j.page}"><span class="n">${j.number}</span><span class="names">Juz ${j.number}<small>${j.number===1?'Al-Fatihah · ':''}${madinaPageLabel(j.page)}</small></span><span>‹</span></button>`).join(''));wireJumps();return;
  }
  if(id==='jump'){
   openSheet('Madina Mushaf · Go to page',`<p class="helper">Enter a PDF page number from 1 to 605. For the Qur’an pages, PDF page = Mushaf page + 1.</p><form id="madina-jump"><input id="madina-number" class="search" type="number" min="1" max="605" required value="${page}" aria-label="Source PDF page number"><button class="action">Open page</button><p id="madina-error" class="helper" role="status"></p></form><div class="tools-actions"><button data-page="1">Cover</button><button data-page="2">Al-Fatihah</button><button data-page="583">Juz 30</button></div>`);wireJumps();$('madina-jump').onsubmit=e=>{e.preventDefault();const n=Number($('madina-number').value);if(Number.isInteger(n)&&n>=1&&n<=605)go(n);else $('madina-error').textContent='Enter a whole number from 1 to 605.'};return;
  }
  if(id==='bookmarks'){
   openSheet('Madina Mushaf · Bookmarks',marks.length?marks.map(p=>`<div class="list-row"><button class="names" data-page="${p}">${madinaPageLabel(p)}<small>Mode 1 · Madina Mushaf</small></button><button data-madina-remove="${p}" aria-label="Remove bookmark">×</button></div>`).join(''):'<p class="helper">Tap the star while reading to save your page.</p>');wireJumps();document.querySelectorAll('[data-madina-remove]').forEach(b=>b.onclick=()=>{marks=marks.filter(p=>p!==Number(b.dataset.madinaRemove));save(bookmarkKey(),marks);updateBookmark();screen('bookmarks')});return;
  }
  if(id==='rules'){openSheet('Madina Mushaf · Edition',`<p class="helper">The standard 604-page, 15-line Madani Mushaf layout in the Hafs reading. Mode 1 uses the source PDF’s Uthmani Arabic and recitation marks without adding a colour layer. For colour-coded Tajweed, choose Mode 7 or Mode 6.</p><a class="action" href="${MADINA_SOURCE}" target="_blank" rel="noopener">Open original Hafs PDF</a>`);return}
 }
 modeSevenScreen(id);
 if(id==='settings'&&mode==='madina'&&settingsTab!=='general'){const content=$('sheet').querySelector('.settings-section-body');content.insertAdjacentHTML('beforeend',checkSetting('madina-fullscreen','Mode 1 full-page on opening',prefs.madinaFullscreen!==false,'Use the same full reading area as Mode 5.')+checkSetting('madina-minimal','Mode 1 clear reading controls',prefs.madinaMinimal!==false,'Tap the page to show menu and bookmark without covering the Arabic.')+checkSetting('madina-footer','Show Mode 1 floating page controls',prefs.madinaFooter===true,'Optional page number and turn buttons.'));for(const [id,key]of [['madina-fullscreen','madinaFullscreen'],['madina-minimal','madinaMinimal'],['madina-footer','madinaFooter']])$(id).onchange=()=>{prefs[key]=$(id).checked;applyPrefs()};}
 if(id==='about')$('sheet').querySelector('.sheet-content').insertAdjacentHTML('afterbegin',`<h3>Mode 1 · 15-line Madina Mushaf</h3><p>The cover and all 604 Madani Mushaf pages, displayed using the faithful high-resolution renders of the source PDF without rewriting the Arabic or changing the page breaks. Choose an Arabic-frame layout for larger text, or Original complete page for the complete source page.</p><p><a href="${MADINA_SOURCE}" target="_blank" rel="noopener">Original Hafs PDF from Quran.ws</a></p>`);
};
const modeSevenDrawer=drawer;drawer=function(){modeSevenDrawer();const source=$('drawer').querySelector('[data-screen="source"]');if(source&&mode==='madina')source.innerHTML='<span class="icon">▤</span>Madina Mushaf · original PDF';$('drawer').querySelector('.drawer-bottom').insertAdjacentHTML('beforebegin','<button id="madina-book" class="drawer-row"><span class="icon">▤</span>Madina Mushaf · complete PDF</button>');$('madina-book').onclick=openMadinaSource};$('menu').onclick=drawer;if($('focus-menu'))$('focus-menu').onclick=drawer;
$('page').addEventListener('load',()=>{if(mode==='madina')requestAnimationFrame(alignMagnifiedPage)},true);
if(mode==='madina'){applyPrefs();render()}

// Reuse Mode 5's quiet full-page reading controls without sharing its preferences.
const madinaAppearance=readerAppearance;readerAppearance=function(){madinaAppearance();if(mode==='madina'){document.body.dataset.minimalMushaf=String(prefs.madinaMinimal!==false);document.body.dataset.hideMushafFooter=String(prefs.madinaFooter!==true)}};
const madinaChangeMode=changeMode;changeMode=function(next){madinaChangeMode(next);if(next==='madina'&&prefs.madinaFullscreen!==false)fullscreen(true)};
const madinaValidation=validateBackup;validateBackup=function(b){madinaValidation(b);const p=b.data['tm-settings'];if(p)for(const k of ['madinaFullscreen','madinaMinimal','madinaFooter'])if(p[k]!=null&&typeof p[k]!=='boolean')throw Error('Invalid Mode 1 display setting')};
applyPrefs();if(mode==='madina'&&prefs.madinaFullscreen!==false)fullscreen(true);
// Mouse dragging mirrors touch panning on magnified pages; clicks after a drag are suppressed.
let madinaDrag=null;
reader.addEventListener('pointerdown',e=>{const stage=$('pdf-stage');if(mode!=='madina'||e.pointerType!=='mouse'||e.button!==0||zoomForMode()<=100||!stage||!stage.contains(e.target))return;madinaDrag={id:e.pointerId,x:e.clientX,y:e.clientY,left:stage.scrollLeft,top:stage.scrollTop,moved:false,stage};stage.setPointerCapture(e.pointerId);e.preventDefault()});
reader.addEventListener('pointermove',e=>{const g=madinaDrag;if(!g||g.id!==e.pointerId)return;const dx=e.clientX-g.x,dy=e.clientY-g.y;if(Math.hypot(dx,dy)>5)g.moved=true;g.stage.scrollLeft=g.left-dx;g.stage.scrollTop=g.top-dy;e.preventDefault()});
function finishMadinaDrag(e){if(madinaDrag?.id!==e.pointerId)return;if(madinaDrag.moved)suppressClickUntil=Date.now()+600;madinaDrag=null}
reader.addEventListener('pointerup',finishMadinaDrag);reader.addEventListener('pointercancel',finishMadinaDrag);
reader.addEventListener('dragstart',e=>{if(mode==='madina')e.preventDefault()});

// Arabic-area layouts clip only whitespace from the original page render.
// Covers and references keep their complete contents in every layout.
function prepareMadinaFrame(image){
 const frame=document.createElement('div');frame.className='madina-frame';image.replaceWith(frame);frame.appendChild(image);sizeMadinaFrame(frame);
}
function sizeMadinaFrame(frame){
 const image=frame.querySelector('img'),stage=$('pdf-stage');if(!image||!stage)return;
 const n=Number(image.dataset.page),layout=layoutForMode(),focused=layout.startsWith('madina-');
 const original=[0,0,image.naturalWidth,image.naturalHeight],crop=focused?(MADINA_FRAMES[n]||original):original;
 const [x,y,w,h]=crop,sw=stage.clientWidth,sh=stage.clientHeight,z=zoomForMode()/100;
 let vw=sw,vh=sh;
 if(z>1||layout==='madina-width'||layout==='width'){vw=sw*z;vh=vw*h/w}
 else if(layout==='madina-fit'||layout==='proportions'||(focused&&!MADINA_FRAMES[n])){const scale=Math.min(sw/w,sh/h);vw=w*scale;vh=h*scale}
 frame.style.cssText=`position:absolute;overflow:hidden;width:${vw}px;height:${vh}px;left:${vw<sw?(sw-vw)/2:0}px;top:${vh<sh?(sh-vh)/2:0}px;`;
 image.style.width=(image.naturalWidth*vw/w)+'px';image.style.height=(image.naturalHeight*vh/h)+'px';image.style.left=(-x*vw/w)+'px';image.style.top=(-y*vh/h)+'px';image.style.right='auto';image.style.bottom='auto';image.style.objectFit='fill';
 stage.style.overflow=z>1||layout.endsWith('width')?'auto':'hidden';
}
function updateMadinaFrames(){if(mode!=='madina')return;$('pdf-stage')?.querySelectorAll('.madina-frame').forEach(sizeMadinaFrame)}
const madinaFramedAppearance=readerAppearance;readerAppearance=function(){madinaFramedAppearance();updateMadinaFrames()};
// Adopt the photo-style layout once; later choices remain saved independently.
if(!prefs.madinaFrameLayout){prefs.madinaFrameLayout=true;prefs.layouts={...(prefs.layouts||{}),madina:'madina-full'};save('tm-settings',prefs)}
const madinaFramedScreen=screen;screen=function(id){madinaFramedScreen(id);if(id==='settings'&&mode==='madina'&&settingsTab!=='general')$('pdf-fit').previousElementSibling.innerHTML='Mode 1 page layout<small>Madina full screen fills the reading area. Fit Arabic area preserves letter proportions. Arabic width gives larger text with vertical scrolling. Original complete page keeps the complete source page.</small>'};
applyPrefs();

// Fullscreen reading is clear; a page tap restores the separate toolbar.
reader.onclick=e=>{if(immersive||prefs.tapControls!==false)originalReaderTap(e)};
reader.tabIndex=0;
reader.addEventListener('keydown',e=>{if(e.target===reader&&immersive&&(e.key==='Enter'||e.key===' '||e.key==='Escape')){e.preventDefault();fullscreen(false)}});

const clearControlsScreen=screen;screen=function(id){clearControlsScreen(id);if(id==='settings')for(const key of ['focus-tools','mushaf-minimal','mushaf-footer','sixteen-minimal','sixteen-footer','fifteen-minimal','fifteen-footer','madina-minimal','madina-footer'])$(key)?.closest('.setting')?.remove()};
reader.setAttribute('aria-label','Qur’an reading page. Tap to show or hide the toolbar.');

// Display numbers change; saved mode IDs, bookmarks and reading positions stay stable.
const namedModesScreen=screen;screen=function(id){
 namedModesScreen(id);
 if(id==='modes')for(const [i,[m]]of readerModes.entries()){const card=$(m+'-mode');card.querySelector('strong').textContent=(i+1)+'. '+modeLabel(m);$('sheet').querySelector('.sheet-content').appendChild(card)}
 if(id==='stats'||(id==='settings'&&settingsTab==='general')){
  const content=$('sheet').querySelector('.sheet-content');
  const box=document.createElement('div');box.className='tools-actions';box.innerHTML='<button id="reset-reading-goal">Reset reading goal</button><button id="clear-reading-statistics">Clear reading statistics</button>';content.appendChild(box);
  $('reset-reading-goal').onclick=()=>{prefs.goal=10;applyPrefs();screen(id);toast('Reading goal reset to 10 pages a day')};
  $('clear-reading-statistics').onclick=()=>confirmStatisticsReset(id);
 }
};
function confirmStatisticsReset(back){
 openSheet('Clear reading statistics?', '<p class="helper">Clear all recorded reading minutes, daily page counts and reading days on this device. Your reading goal, bookmarks, notes and saved pages will stay intact.</p><div class="tools-actions"><button class="action" id="confirm-statistics-reset">Clear statistics</button><button id="cancel-statistics-reset">Cancel</button></div>');
 $('cancel-statistics-reset').onclick=()=>screen(back);
 $('confirm-statistics-reset').onclick=()=>{readingStats={days:{}};lastTick=Date.now();persistStats();screen(back);toast('Reading statistics cleared')};
}

const customGoalScreen=screen;screen=function(id){customGoalScreen(id);if(id==='stats'||(id==='settings'&&settingsTab==='general')){
 const form=document.createElement('form');form.id='custom-goal-form';form.innerHTML='<div class="setting"><label for="custom-daily-goal">Type your daily page goal<small>Choose a whole number from 1 to 10,000. The preset choices above remain available.</small></label><input id="custom-daily-goal" type="number" min="1" max="10000" step="1" required inputmode="numeric" value="'+clamp(prefs.goal,1,10000,10)+'" style="width:7em;padding:10px;border:1px solid var(--line);border-radius:6px;background:var(--bg);color:var(--ink)"></div><button class="action" type="submit">Save page goal</button><p id="custom-goal-message" class="helper" role="status"></p>';
 $('daily-goal').closest('.setting').after(form);
 $('daily-goal').addEventListener('change',()=>{$('custom-daily-goal').value=prefs.goal});
 form.onsubmit=e=>{e.preventDefault();const n=Number($('custom-daily-goal').value);if(!Number.isInteger(n)||n<1||n>10000){$('custom-goal-message').textContent='Enter a whole number from 1 to 10,000.';return}prefs.goal=n;applyPrefs();screen(id);toast('Daily goal saved: '+n+' pages')};
}};
