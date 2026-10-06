"""Detect outer blank margins only; retain all scanned ink, headings and page borders."""
import fitz,json
from pathlib import Path
ROOT=Path(__file__).resolve().parents[1]
def detect(source):
    out=[]
    with fitz.open(source) as document:
        for page in document:
            scale=.4
            pix=page.get_pixmap(matrix=fitz.Matrix(scale,scale),colorspace=fitz.csGRAY)
            data=pix.samples;w,h=pix.width,pix.height;xs=[];ys=[]
            for y in range(h):
                row=data[y*w:(y+1)*w]
                ink=[x for x,value in enumerate(row) if value<225]
                if ink:xs.extend((min(ink),max(ink)));ys.append(y)
            if not xs:out.append(list(page.rect));continue
            # Padding protects antialiasing, punctuation and thin borders. Coordinates are PDF points.
            rect=fitz.Rect(max(0,(min(xs)-2)/scale),max(0,(min(ys)-2)/scale),min(page.rect.width,(max(xs)+3)/scale),min(page.rect.height,(max(ys)+3)/scale))
            assert rect.width>page.rect.width*.75 and rect.height>page.rect.height*.5
            out.append(list(rect))
    return out
if __name__=='__main__':
    output=ROOT/'config/plain-crops.json'
    output.write_text(json.dumps(detect(ROOT/'shared/web/plain-13line.pdf'),separators=(',',':'))+'\n')
