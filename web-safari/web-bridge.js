// Web equivalent of the native page handoff. Pages retain the source scans.
const scanGeneration={word:0,plain:0};
function browserScan(target,n){const generation=++scanGeneration[target];const info=SCANS[target][n-1];const receive=target==='word'?'receivePdf':'receivePlainPdf';const img=new Image();img.onload=()=>{if(generation!==scanGeneration[target])return;try{const [x,y,r,b]=info.crop,[ix,iy,ir,ib]=info.image;const sx=img.naturalWidth/(ir-ix),sy=img.naturalHeight/(ib-iy);const c=document.createElement('canvas');c.width=Math.ceil((r-x)*sx);c.height=Math.ceil((b-y)*sy);const ctx=c.getContext('2d');ctx.fillStyle='white';ctx.fillRect(0,0,c.width,c.height);ctx.drawImage(img,(ix-x)*sx,(iy-y)*sy,img.naturalWidth,img.naturalHeight);window[receive]?.(n,c.toDataURL('image/png'),null);if(n<SCANS[target].length&&navigator.onLine!==false)fetch(`pages/${target}-${n+1}.${SCANS[target][n].ext}`).catch(()=>{})}catch{window[receive]?.(n,null,'Unable to display page. Try again.')}};img.onerror=()=>window[receive]?.(n,null,'This page is not saved offline yet. Connect to the internet and reopen it.');img.src=`pages/${target}-${n}.${info.ext}`}
window.Android={
 pdfPage(n){setTimeout(()=>browserScan('word',n),0)},
 plainPage(n){setTimeout(()=>browserScan('plain',n),0)},
 fullscreen(){},volumeKeys(){},pdfQuality(){},
 async awake(enabled){try{if(enabled&&navigator.wakeLock){window.quranWakeLock=await navigator.wakeLock.request('screen')}else await window.quranWakeLock?.release()}catch{}}
};
