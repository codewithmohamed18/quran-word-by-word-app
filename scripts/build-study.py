"""Build web-only Reading Mode 4 from the verified Darussalam three-volume scans.
Usage: python scripts/build-study.py SOURCE_DIRECTORY WEB_OUTPUT_DIRECTORY PDF_OUTPUT
SOURCE_DIRECTORY contains volume-1.pdf .. volume-3.pdf and juz-01.pdf .. juz-30.pdf.
Full-volume PDFs supply the merged download, covers, front matter and navigation.
Juz scans supply higher-resolution reading images. All printed content is retained.
"""
from pathlib import Path
from concurrent.futures import ProcessPoolExecutor
from io import BytesIO
import sys,json,re,hashlib
import fitz
from PIL import Image, ImageOps

PREFIXES=[12,11,13]
def export_job(job):
    source,output,vol,juz,start=job
    path=Path(source)/(f'juz-{juz:02}.pdf' if juz else f'volume-{vol}.pdf')
    d=fitz.open(path); rows=[]
    indices=range(2,len(d)) if juz else range(PREFIXES[vol-1])
    for count,i in enumerate(indices):
        p=d[i];images=p.get_images();assert len(images)==1,(path,i,len(images))
        data=d.extract_image(images[0][0]);im=Image.open(BytesIO(data['image'])).convert('RGB')
        # The threshold detects every coloured mark and printed marginal label.
        mask=ImageOps.invert(im.convert('L')).point(lambda x:255 if x>8 else 0)
        box=mask.getbbox() or (0,0,*im.size)
        box=(max(0,box[0]-12),max(0,box[1]-12),min(im.width,box[2]+12),min(im.height,box[3]+12))
        im=im.crop(box)
        # Larger than the full-volume scans, while keeping mobile downloads modest.
        if im.width>1500:im=im.resize((1500,round(im.height*1500/im.width)),Image.Resampling.LANCZOS)
        n=start+count;dest=Path(output)/'pages'/f'study-{n}.webp'
        im.save(dest,'WEBP',quality=90,method=4)
        rows.append((n,{'ext':'webp','crop':[0,0,im.width,im.height],'image':[0,0,im.width,im.height]}))
    print(f'Volume {vol}, Juz {juz or "intro"}: {len(rows)} pages',flush=True)
    return rows

def main():
    source,output,pdf=map(Path,sys.argv[1:]);(output/'pages').mkdir(parents=True,exist_ok=True);pdf.parent.mkdir(parents=True,exist_ok=True)
    merged=fitz.open();toc=[];headers=[None];surahs={};juznav=[];volumes=[];jobs=[];offset=0
    for v in range(1,4):
        d=fitz.open(source/f'volume-{v}.pdf');assert len(d)==[579,600,618][v-1]
        merged.insert_pdf(d);volumes.append({'number':v,'page':offset+1,'start':offset+PREFIXES[v-1]+1,'count':len(d)})
        toc.append([1,f'Volume {v} · Juz {v*10-9}–{v*10}',offset+1]);jobs.append((str(source),str(output),v,0,offset+1))
        cursor=offset+PREFIXES[v-1]+1
        for j in range(v*10-9,v*10+1):
            jd=fitz.open(source/f'juz-{j:02}.pdf');count=len(jd)-2;assert count>40
            juznav.append({'number':j,'page':cursor});toc.append([2,f'Juz {j}',cursor]);jobs.append((str(source),str(output),v,j,cursor));cursor+=count
        assert cursor==offset+len(d)+1
        current_j=v*10-9
        for i,p in enumerate(d):
            n=offset+i+1
            if i>=PREFIXES[v-1]:current_j=max(j['number'] for j in juznav if j['page']<=n)
            headers.append({'volume':v,'j':current_j,'printed':i if v<3 else i,'intro':i<PREFIXES[v-1]})
            if i<PREFIXES[v-1]:continue
            t=' '.join(p.get_text().split())
            for m in re.finditer(r'Surah\s+(.{1,180}?)\s+(\d{1,3})\s+In the Name',t,re.I):
                s=int(m[2]);
                if 1<=s<=114:surahs.setdefault(s,n)
        offset+=len(d)
    # Heading layouts without the basmalah in the extracted OCR.
    for s,local in {1:13,5:308,9:538}.items():surahs.setdefault(s,local)
    # Short surahs share a page; verified against the printed scans.
    for s,local in {104:610,110:615}.items():surahs.setdefault(s,579+600+local)
    assert sorted(surahs)==list(range(1,115)),sorted(set(range(1,115))-surahs.keys())
    assert all(surahs[n]<=surahs[n+1] for n in range(1,114))
    merged.set_toc(toc);merged.set_metadata({'title':'Study the Noble Quran - Word for Word - Complete Three Volumes','author':'Darussalam; Muhammad Taqi-ud-Din Al-Hilali and Muhammad Muhsin Khan','subject':'Three original volumes combined in order, with volume and juz bookmarks'})
    merged.save(pdf,garbage=3,deflate=True);merged.close()
    nav={'total':offset,'first':surahs[1],'volumes':volumes,'juz':juznav,'surahs':[{'number':s,'page':p} for s,p in sorted(surahs.items())],'headers':headers}
    (output/'study-navigation.js').write_text('const STUDY_NAV='+json.dumps(nav,separators=(',',':'))+';')
    meta=[None]*offset
    with ProcessPoolExecutor(max_workers=4) as pool:
        for rows in pool.map(export_job,jobs):
            for n,row in rows:assert meta[n-1] is None;meta[n-1]=row
    assert all(meta)
    (output/'study-meta.js').write_text('SCANS.study='+json.dumps(meta,separators=(',',':'))+';')
    chunkdir=output/'study-pdf';chunkdir.mkdir(exist_ok=True);chunks=[]
    with pdf.open('rb') as f:
        while b:=f.read(16*1024*1024):
            name=f'study-pdf/part-{len(chunks)+1:03}.bin';(output/name).write_bytes(b);chunks.append({'url':name,'bytes':len(b),'sha256':hashlib.sha256(b).hexdigest()})
    size=sum(p.stat().st_size for p in (output/'pages').glob('study-*'))
    (output/'study-download.js').write_text('const STUDY_PDF='+json.dumps({'bytes':pdf.stat().st_size,'pageBytes':size,'chunks':chunks,'sha256':hashlib.sha256(pdf.read_bytes()).hexdigest()},separators=(',',':'))+';')
    (source/'build-report.json').write_text(json.dumps({'total':offset,'volumes':volumes,'surahs':nav['surahs'],'juz':juznav,'pdfBytes':pdf.stat().st_size,'pageBytes':size},indent=2))
    print(f'Complete: {offset} pages; PDF {pdf.stat().st_size/1048576:.1f} MB; web pages {size/1048576:.1f} MB',flush=True)
if __name__=='__main__':main()
