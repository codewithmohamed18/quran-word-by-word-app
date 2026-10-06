'use strict';
// Reading Mode 4 uses its own pages, navigation, settings and bookmarks.
backupKeys.push('tm-study-page','tm-study-bookmarks');
const modeFourScreen=screen;
screen=function(id){
 if(id==='modes'){
  modeFourScreen(id);
  const card=document.createElement('button');card.id='study-mode';card.className='mode-card'+(mode==='study'?' active':'');
  card.innerHTML='<strong>4. Study the Noble Qur’an · Word for word</strong><small>Darussalam’s three coloured volumes combined<br>Arabic verses, English meanings and word-by-word tables · '+STUDY_NAV.total+' pages</small>';
  card.onclick=()=>changeMode('study');$('sheet').querySelector('.sheet-content').appendChild(card);return;
 }
 if(mode==='study'){
  if(id==='surahs'){
   openSheet('Study Qur’an · Surahs','<input id="study-search" class="search" placeholder="Find a Surah" aria-label="Find a Surah"><div id="study-surahs"></div>');
   const list=()=>{const query=$('study-search').value.toLowerCase();$('study-surahs').innerHTML=STUDY_NAV.surahs.filter(s=>{const q=QURAN.surahs[s.number-1];return (s.number+' '+q.name+' '+q.ar).toLowerCase().includes(query)}).map(s=>{const q=QURAN.surahs[s.number-1];return `<button class="list-row" data-page="${s.page}"><span class="n">${s.number}</span><span class="names">${esc(q.name)}<small>Combined page ${s.page}</small></span><span class="ar-name" lang="ar">${esc(q.ar)}</span></button>`}).join('');wireJumps()};$('study-search').oninput=list;list();return;
  }
  if(id==='juz'){openSheet('Study Qur’an · Juz',STUDY_NAV.juz.map(j=>`<button class="list-row" data-page="${j.page}"><span class="n">${j.number}</span><span class="names">Juz ${j.number}<small>Volume ${Math.ceil(j.number/10)} · Combined page ${j.page}</small></span></button>`).join(''));wireJumps();return}
  if(id==='jump'){
   openSheet('Study Qur’an · Go to page',`<p class="helper">Combined pages 1–${STUDY_NAV.total}. Each volume retains its own printed numbering. The covers and introductions are included.</p><form id="study-jump"><input id="study-page" class="search" type="number" min="1" max="${STUDY_NAV.total}" required value="${page}" aria-label="Combined page number"><button class="action">Open page</button></form><h3>Volumes</h3>`+STUDY_NAV.volumes.map(v=>`<div class="list-row"><button class="names" data-page="${v.start}">Volume ${v.number}<small>Juz ${v.number*10-9}–${v.number*10}</small></button><button data-page="${v.page}">Introduction</button></div>`).join(''));
   wireJumps();$('study-jump').onsubmit=e=>{e.preventDefault();const n=Number($('study-page').value);if(Number.isInteger(n)&&n>=1&&n<=STUDY_NAV.total)go(n)};return;
  }
  if(id==='bookmarks'){
   openSheet('Study Qur’an · Bookmarks',marks.length?marks.map(p=>`<div class="list-row"><button class="names" data-page="${p}">Combined page ${p}<small>Volume ${STUDY_NAV.headers[p].volume}</small></button><button data-study-remove="${p}" aria-label="Remove bookmark">×</button></div>`).join(''):'<p class="helper">Tap the star to save your reading page.</p>');wireJumps();document.querySelectorAll('[data-study-remove]').forEach(b=>b.onclick=()=>{marks=marks.filter(p=>p!==Number(b.dataset.studyRemove));save(bookmarkKey(),marks);updateBookmark();screen('bookmarks')});return;
  }
  if(id==='source'||id==='rules'){openStudyBook();return}
 }
 modeFourScreen(id);
 if(id==='about')$('sheet').querySelector('.sheet-content').insertAdjacentHTML('afterbegin','<h3>Reading Mode 4 · Study the Noble Qur’an</h3><p>Darussalam’s complete coloured word-for-word edition, volumes 1–3, translated by Muhammad Taqi-ud-Din Al-Hilali and Muhammad Muhsin Khan. All 1,797 PDF pages, including covers and introductions, are combined in order. The web reading pages use the sharper matching juz scans. Only blank outer margins are trimmed for display; the printed text, headings, colours and marginal labels are retained.</p><p>Requested source: <a href="https://www.kalamullah.com/study-the-noble-quran.html" target="_blank" rel="noopener">Kalamullah</a>. Complete volume mirror: <a href="https://www.islamicauthenticlibrary.org/" target="_blank" rel="noopener">Islamic Authentic Library</a>. High-resolution juz scans: <a href="https://www.emaanlibrary.com/book/the-noble-quran-word-for-word-arabic-english-color/" target="_blank" rel="noopener">Emaan Library</a>.</p>');
};
function openStudyBook(){
 openSheet('Study the Noble Qur’an',`<p class="helper">Three volumes in one book: full Arabic verses, English verse meanings, and coloured word-by-word tables. The printed colours describe grammatical terms; the edition’s introductions explain the colour key.</p><div class="tools-actions"><button id="study-download-open">Download the combined PDF</button><button id="study-read-open">Read Mode 4</button><button id="study-intro-open">Read the introduction</button></div><p class="helper">The complete PDF is about ${Math.ceil(STUDY_PDF.bytes/1048576)} MB. To save less, use “Save for offline reading” and choose a Surah or Juz.</p>`);
 $('study-download-open').onclick=downloadStudyPdf;$('study-read-open').onclick=()=>{changeMode('study');go(STUDY_NAV.first)};$('study-intro-open').onclick=()=>{changeMode('study');go(1)};
}
let studyDownload=null,studyPdfUrl=null;
function downloadStudyPdf(){
 openSheet('Download the complete PDF',`<p class="helper">All three original volumes combined, with bookmarks for each volume and Juz. About ${Math.ceil(STUDY_PDF.bytes/1048576)} MB. Keep this screen open while downloading.</p><p id="study-pdf-status" role="status" aria-live="polite">Ready to download.</p><progress id="study-pdf-progress" style="width:100%;height:18px" max="${STUDY_PDF.bytes}" value="0" aria-label="PDF download progress"></progress><div class="tools-actions"><button id="study-pdf-start" class="action">${studyPdfUrl?'Download again':'Start download'}</button><button id="study-pdf-cancel" class="hidden">Cancel</button><a id="study-pdf-save" class="action ${studyPdfUrl?'':'hidden'}" download="Study-the-Noble-Quran-Complete.pdf" target="_blank" rel="noopener">Open / save PDF</a></div><p class="helper">On iPhone or iPad, open the finished PDF and use Share → Save to Files.</p>`);
 if(studyPdfUrl)$('study-pdf-save').href=studyPdfUrl;
 const start=$('study-pdf-start'),status=$('study-pdf-status'),bar=$('study-pdf-progress'),cancel=$('study-pdf-cancel'),link=$('study-pdf-save');
 start.disabled=!!studyDownload;
 cancel.onclick=()=>studyDownload?.abort();
 start.onclick=async()=>{
  if(studyDownload)return;const controller=studyDownload=new AbortController();start.disabled=true;cancel.classList.remove('hidden');link.classList.add('hidden');const parts=[];let done=0;
  try{
   for(const part of STUDY_PDF.chunks){
    if(!status.isConnected)throw Error('Download screen was closed. Reopen it to download.');
    const r=await fetch(part.url,{signal:controller.signal});if(!r.ok)throw Error('Download failed. Check your connection and try again.');const bytes=await r.arrayBuffer();if(bytes.byteLength!==part.bytes)throw Error('An incomplete PDF part was received. Please try again.');
    const digest=[...new Uint8Array(await crypto.subtle.digest('SHA-256',bytes))].map(v=>v.toString(16).padStart(2,'0')).join('');if(digest!==part.sha256)throw Error('PDF verification failed. Please reload and try again.');
    parts.push(new Blob([bytes]));done+=bytes.byteLength;bar.value=done;status.textContent=`Downloaded ${Math.round(done/1048576)} of ${Math.ceil(STUDY_PDF.bytes/1048576)} MB.`;
   }
   if(studyPdfUrl)URL.revokeObjectURL(studyPdfUrl);studyPdfUrl=URL.createObjectURL(new Blob(parts,{type:'application/pdf'}));link.href=studyPdfUrl;link.classList.remove('hidden');status.textContent='Complete PDF verified. Tap Open / save PDF.';
  }catch(e){status.textContent=e.name==='AbortError'?'Download cancelled.':e.message}
  finally{studyDownload=null;start.disabled=false;cancel.classList.add('hidden')}
 };
}
const studyDrawer=drawer;drawer=function(){studyDrawer();$('drawer').querySelector('.drawer-bottom').insertAdjacentHTML('beforebegin','<button id="study-book" class="drawer-row"><span class="icon">▤</span>Study Qur’an · complete PDF</button>');$('study-book').onclick=openStudyBook};$('menu').onclick=drawer;if($('focus-menu'))$('focus-menu').onclick=drawer;
const modeFourChangeMode=changeMode;changeMode=function(next){modeFourChangeMode(next);if(next==='study')toast('Mode 4 · Scroll or pan to read; Reading tools adjusts zoom')};
if(mode==='study'){applyPrefs();render()}

$('page').addEventListener('load',()=>{if(mode==='study')requestAnimationFrame(alignMagnifiedPage)},true);
