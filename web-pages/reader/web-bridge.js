// Web equivalent of the native page handoff. Pages retain the source scans.
const scanGeneration={word:0,plain:0,study:0};
function releaseScanUrl(url){if(!url?.startsWith('blob:'))return;function release(){const used=[...document.querySelectorAll('.pdf-image')].some(img=>img.src===url);if(used)setTimeout(release,1000);else URL.revokeObjectURL(url)}setTimeout(release,1000)}
function browserScan(target,n){
 const generation=++scanGeneration[target],info=SCANS[target][n-1];
 const receive=target==='study'?'receiveStudyPdf':target==='word'?'receivePdf':'receivePlainPdf';
 const send=blob=>{if(generation!==scanGeneration[target])return;window[receive]?.(n,URL.createObjectURL(blob),null);if(n<SCANS[target].length&&navigator.onLine!==false)(target==='study'?studyPageResponse(n+1):fetch(`pages/${target}-${n+1}.${SCANS[target][n].ext}`)).catch(()=>{})};
 const fail=()=>{if(generation===scanGeneration[target])window[receive]?.(n,null,'This page is not saved offline yet. Connect to the internet and reopen it.')};
 if(target==='study'&&info.crop.every((v,i)=>v===info.image[i])){studyPageResponse(n).then(r=>r.blob()).then(send).catch(fail);return}
 const img=new Image();img.onload=()=>{if(generation!==scanGeneration[target])return;try{const [x,y,r,b]=info.crop,[ix,iy,ir,ib]=info.image;const sx=img.naturalWidth/(ir-ix),sy=img.naturalHeight/(ib-iy);const c=document.createElement('canvas');c.width=Math.ceil((r-x)*sx);c.height=Math.ceil((b-y)*sy);const ctx=c.getContext('2d');ctx.fillStyle='white';ctx.fillRect(0,0,c.width,c.height);ctx.drawImage(img,(ix-x)*sx,(iy-y)*sy,img.naturalWidth,img.naturalHeight);c.toBlob(blob=>blob?send(blob):fail(),'image/png')}catch{fail()}};img.onerror=fail;
 if(target==='study'){studyPageResponse(n).then(r=>r.blob()).then(blob=>{if(generation!==scanGeneration[target])return;const url=URL.createObjectURL(blob),loaded=img.onload,failed=img.onerror;img.onload=()=>{URL.revokeObjectURL(url);loaded()};img.onerror=()=>{URL.revokeObjectURL(url);failed()};img.src=url}).catch(fail)}else img.src=`pages/${target}-${n}.${info.ext}`
}
window.Android={
 pdfPage(n){setTimeout(()=>browserScan('word',n),0)},
 studyPage(n){setTimeout(()=>browserScan('study',n),0)},
 plainPage(n){setTimeout(()=>browserScan('plain',n),0)},
 fullscreen(){},volumeKeys(){},pdfQuality(){},
 async awake(enabled){try{if(enabled&&navigator.wakeLock){window.quranWakeLock=await navigator.wakeLock.request('screen')}else await window.quranWakeLock?.release()}catch{}}
};

function studyScanUrl(n){return (typeof STUDY_ASSET_BASE==='string'&&STUDY_ASSET_BASE?STUDY_ASSET_BASE.replace(/\/$/,'')+'/':'')+'pages/study-'+n+'.'+SCANS.study[n-1].ext}
async function studyPageResponse(n){const url=studyScanUrl(n),cache=await caches.open('quran-pages-v1'),saved=await cache.match(url);if(saved)return saved;const response=await fetch(url);if(!response.ok||!response.headers.get('content-type')?.startsWith('image/'))throw Error('Could not download this page.');try{await cache.put(url,response.clone())}catch{}return response}
