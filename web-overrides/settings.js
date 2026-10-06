// Ordered settings extend the supplied reader while preserving its saved state and event handlers.
const clarityChoices=[['fast','Fast · lower memory'],['clear','Clear (default)'],['maximum','Maximum · more memory']];
const clarityLevel=()=>{const index=clarityChoices.findIndex(([id])=>id===(prefs.wordQuality||'clear'));return index<0?1:index};
let sentClarity=null;
function refreshWordPage(){
 for(const key of pdfImages.keys())if(key.startsWith('word:'))pdfImages.delete(key);
 if(mode!=='word')return;
 clearTimeout(pdfAnimation);pdfVisiblePage=0;lastZoomPosition='';
 $('page').innerHTML='<div id="pdf-stage" data-mode="word"><div class="pdf-loading" id="pdf-loading">Opening clearer Qur’an page…</div></div>';
 render();
}
const clarityPrefs=applyPrefs;applyPrefs=function(){
 const level=clarityLevel(),changed=sentClarity===null?level!==1:sentClarity!==level;sentClarity=level;
 if(window.Android&&Android.pdfQuality)Android.pdfQuality(level);
 clarityPrefs();if(changed)refreshWordPage();
};
const edgeFilters=document.createElementNS('http://www.w3.org/2000/svg','svg');edgeFilters.setAttribute('aria-hidden','true');edgeFilters.setAttribute('width','0');edgeFilters.setAttribute('height','0');edgeFilters.style.position='absolute';
edgeFilters.innerHTML='<defs><filter id="word-edges-gentle" color-interpolation-filters="sRGB"><feConvolveMatrix order="3" kernelMatrix="0 -0.08 0 -0.08 1.32 -0.08 0 -0.08 0" preserveAlpha="true"/></filter><filter id="word-edges-strong" color-interpolation-filters="sRGB"><feConvolveMatrix order="3" kernelMatrix="0 -0.15 0 -0.15 1.6 -0.15 0 -0.15 0" preserveAlpha="true"/></filter></defs>';document.body.appendChild(edgeFilters);
const edgesAppearance=readerAppearance;readerAppearance=function(){
 edgesAppearance();const theme=prefs.wordTheme||'cream';const paper={cream:'grayscale(1) sepia(.18) contrast(1.04)',neutral:'grayscale(1) contrast(1.04)',warm:'grayscale(1) sepia(.32) contrast(1.08)','high-contrast':'grayscale(1) contrast(1.18)',original:''}[theme]||'';
 const selected=prefs.wordEdges||'gentle';const edge=['gentle','strong'].includes(selected)?'url(#word-edges-'+selected+')':'';
 document.documentElement.style.setProperty('--word-filter',[paper,edge].filter(Boolean).join(' ')||'none');
};
backupKeys.push('tm-reading-profiles');
function profiles(){return load('tm-reading-profiles',[])}
function showProfiles(){
 const saved=profiles();const savedList='<div id="profile-list">'+(saved.length?saved.map((p,i)=>'<div class="profile-row"><button class="profile-load" data-profile="'+i+'"><strong>'+esc(p.name)+'</strong><small>'+esc(modeLabel(p.mode))+' · '+(p.focus?'Full-page focus':'Toolbar view')+'</small></button><button class="profile-remove" data-profile-remove="'+i+'" aria-label="Delete '+esc(p.name)+'">✕</button></div>').join(''):'<p class="helper">Your saved setups will appear here.</p>')+'</div>';
 openSheet('Saved reading setups','<p class="helper">Save your current mode, colours, layout, text sizes and controls. Loading a setup keeps your bookmarks and reading history. Up to 8 setups stay on this device and are included in backups.</p>'+savedList+'<form id="save-profile-form" class="profile-form"><label for="profile-name">Name this setup</label><input class="search" id="profile-name" maxlength="40" required placeholder="For example: Evening reading"><button class="action" id="save-profile">Save current setup</button></form>');
 $('save-profile-form').onsubmit=e=>{e.preventDefault();const name=$('profile-name').value.trim();if(!name)return;const saved=profiles(),index=saved.findIndex(p=>p.name.toLowerCase()===name.toLowerCase());if(index<0&&saved.length>=8){toast('Delete a setup before adding another');return}const value={name,mode,focus:immersive,prefs:JSON.parse(JSON.stringify(prefs))};if(index<0)saved.push(value);else saved[index]=value;save('tm-reading-profiles',saved);showProfiles();toast('Reading setup saved')};
 $('sheet').querySelectorAll('[data-profile]').forEach(b=>b.onclick=()=>{const p=profiles()[Number(b.dataset.profile)];if(!p)return;prefs=JSON.parse(JSON.stringify(p.prefs));applyPrefs();if(mode!==p.mode)changeMode(p.mode);else render();closePanels();fullscreen(p.focus);toast(p.name+' applied')});
 $('sheet').querySelectorAll('[data-profile-remove]').forEach(b=>b.onclick=()=>{const saved=profiles();saved.splice(Number(b.dataset.profileRemove),1);save('tm-reading-profiles',saved);showProfiles();toast('Setup deleted')});
}
function resetModeLayout(){
 if(isScan()){prefs.layouts={...(prefs.layouts||{}),[mode]:'fill'};prefs.pdfZooms={...(prefs.pdfZooms||{}),[mode]:100}}
 if(mode==='word'){prefs.wordTheme='cream';prefs.wordQuality='clear';prefs.wordEdges='gentle';prefs.wordStudy=false;prefs.wordFullscreen=false}
 else if(mode==='plain'){prefs.plainFullscreen=true;prefs.plainMinimal=true;prefs.plainFooter=false}
 else{prefs.size=36;prefs.englishSize=20;prefs.meaning=true;prefs.readingLine=1.9;prefs.meaningLine=1.65}
 lastZoomPosition='';applyPrefs();render();screen('settings');toast('This mode’s layout reset');
}
function orderSettings(){
 const content=$('sheet').querySelector('.sheet-content');
 content.insertAdjacentHTML('beforeend',selectSetting('word-quality','Mode 1 page clarity',clarityChoices,prefs.wordQuality||'clear','Clear and Maximum render more pixels within your device’s memory limit. Maximum may take longer. The original scan sets the available detail.')+selectSetting('word-edges','Mode 1 text edge clarity',[['none','Original edges'],['gentle','Gentle (default)'],['strong','Stronger']],prefs.wordEdges||'gentle','Optional edge emphasis for reading. The bundled PDF is unchanged; no letters or meanings are reconstructed.')+'<button class="action" id="reading-profiles">Saved reading setups</button><button id="reset-mode-layout">Reset this mode’s layout</button>');
 $('word-quality').onchange=()=>{prefs.wordQuality=$('word-quality').value;applyPrefs()};$('word-edges').onchange=()=>{prefs.wordEdges=$('word-edges').value;applyPrefs()};$('reading-profiles').onclick=()=>screen('profiles');$('reset-mode-layout').onclick=resetModeLayout;
 const groups=[
  ['page','Page layout & clarity',['pdf-fit','pdf-magnification','word-quality','word-edges'],true],
  ['comfort','Colours & reading comfort',['word-theme','dark','page-brightness','reading-contrast','reading-warmth','reading-guide-toggle','reading-guide-position'],false],
  ['text','Arabic & English · Mode 2',['meaning','size','english-size','reading-line','meaning-line'],mode==='tajweed'],
  ['navigation','Page turns & full-screen',['swipe-pages','swipe-action','motion','tap-controls','start-focus','focus-tools','show-progress'],false],
  ['word','Word-by-word study · Mode 1',['word-study','study-step','word-fullscreen'],mode==='word'],
  ['mushaf','Mushaf view · Mode 3',['mushaf-fullscreen','mushaf-minimal','mushaf-footer'],mode==='plain'],
  ['access','Accessibility & screen',['large-controls','awake','volume'],false],
  ['data','Goals, saved setups & backup',['daily-goal','reading-profiles','reading-stats','reading-backup','reset-mode-layout','reset-reading'],false]
 ];
 // Move the original nodes, rather than recreate them: their handlers and selections survive.
 const nodes=new Map();groups.forEach(([, ,ids])=>ids.forEach(id=>{const input=$(id);if(input)nodes.set(id,input.closest('.setting')||input)}));
 const quick=$('comfort-open');content.replaceChildren();content.classList.add('ordered-settings');
 const introduction=document.createElement('p');introduction.className='helper';introduction.textContent='Settings save automatically on this device. '+modeLabel(mode)+' is the current mode. Mode-specific sections are labelled below.';content.appendChild(introduction);
 const search=document.createElement('div');search.className='settings-search';search.innerHTML='<label class="visually-hidden" for="settings-search">Search settings</label><input id="settings-search" aria-label="Search settings" type="search" class="search" placeholder="Search settings…" autocomplete="off"><button id="clear-settings-search" aria-label="Clear settings search">✕</button>';content.appendChild(search);
 if(quick)content.appendChild(quick);
 groups.forEach(([key,title,ids,open])=>{
  const section=document.createElement('details');section.className='settings-section';section.id='settings-'+key;section.open=open;
  const heading=document.createElement('summary');heading.id=section.id+'-summary';heading.textContent=title;section.appendChild(heading);
  const rows=document.createElement('div');rows.className='settings-section-body';
  ids.forEach(id=>{const node=nodes.get(id);if(!node)return;
   if((id==='word-quality'||id==='word-edges'||id==='word-theme')&&mode!=='word')node.classList.add('hidden');
   if(node.classList.contains('hidden'))return;
   node.dataset.settingId=id;node.classList.add('searchable-setting');rows.appendChild(node);
  });section.appendChild(rows);if(rows.children.length)content.appendChild(section);
 });
 const empty=document.createElement('p');empty.id='settings-search-empty';empty.className='helper';empty.hidden=true;empty.textContent='No settings match. Try “zoom”, “page”, “colour” or “bookmark”.';content.appendChild(empty);
 // Useful search words stay associated with the visible labels.
 const keywords={ 'pdf-magnification':'zoom enlarge magnify', 'word-quality':'blur sharp resolution image', 'word-edges':'blur sharp letters', 'reading-backup':'bookmarks notes restore export', 'dark':'night theme', 'word-study':'sections larger translation' };
 let initialOpen=null;const filter=()=>{const query=$('settings-search').value.trim().toLowerCase();if(query&&!initialOpen)initialOpen=new Map([...content.querySelectorAll('details')].map(s=>[s.id,s.open]));let count=0;content.querySelectorAll('details').forEach(section=>{let matches=0;section.querySelectorAll('.searchable-setting').forEach(row=>{const text=(section.querySelector('summary').textContent+' '+row.textContent+' '+(keywords[row.dataset.settingId]||'')).toLowerCase();row.hidden=!!query&&!text.includes(query);if(!row.hidden)matches++});section.hidden=matches===0;if(matches){count+=matches;if(query)section.open=true;else if(initialOpen)section.open=initialOpen.get(section.id)}});empty.hidden=count!==0;if(!query)initialOpen=null;if(quick)quick.hidden=!!query;};
 $('settings-search').oninput=filter;$('clear-settings-search').onclick=()=>{$('settings-search').value='';filter()};
}
const organizedScreen=screen;screen=function(id){if(id==='profiles'){showProfiles();return}organizedScreen(id);if(id==='settings')orderSettings()};
const profilesDrawer=drawer;drawer=function(){profilesDrawer();const b=document.createElement('button');b.className='drawer-row';b.innerHTML='<span class="icon">▤</span>Saved reading setups';b.onclick=()=>screen('profiles');$('drawer').querySelector('.drawer-bottom').before(b)};$('menu').onclick=drawer;$('focus-menu').onclick=drawer;
const organizedBackup=validateBackup;validateBackup=function(b){
 organizedBackup(b);const p=b.data['tm-settings'];if(p){if(p.wordQuality!=null&&!clarityChoices.some(([id])=>id===p.wordQuality))throw Error('Invalid page clarity');if(p.wordEdges!=null&&!['none','gentle','strong'].includes(p.wordEdges))throw Error('Invalid edge clarity')}
 const saved=b.data['tm-reading-profiles'];if(saved!=null){if(!Array.isArray(saved)||saved.length>8)throw Error('Invalid reading setups');for(const item of saved){if(!item||typeof item.name!=='string'||!item.name.trim()||item.name.length>40||!['word','plain','tajweed'].includes(item.mode)||typeof item.focus!=='boolean'||!item.prefs||typeof item.prefs!=='object'||Array.isArray(item.prefs))throw Error('Invalid saved setup');validateBackup({format:'tajweed-meaning-backup',version:1,data:{'tm-settings':item.prefs}})}}
};
applyPrefs();readerAppearance();
