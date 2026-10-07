package com.khalid.tajweedmeaning.webreader.tests;
import android.app.*;import android.os.*;import android.content.Intent;import android.view.*;import android.webkit.WebView;import java.util.concurrent.*;
public class ReaderChecks extends Instrumentation {
 Activity activity;WebView web;
 WebView find(View v){if(v instanceof WebView)return (WebView)v;if(v instanceof ViewGroup){ViewGroup g=(ViewGroup)v;for(int i=0;i<g.getChildCount();i++){WebView w=find(g.getChildAt(i));if(w!=null)return w;}}return null;}
 String js(String code)throws Exception{CountDownLatch latch=new CountDownLatch(1);String[] out={""};runOnMainSync(()->web.evaluateJavascript(code,r->{out[0]=r;latch.countDown();}));if(!latch.await(15,TimeUnit.SECONDS))throw new AssertionError("JS timeout");return out[0];}
 void await(String code,int seconds)throws Exception{long end=System.currentTimeMillis()+seconds*1000;while(System.currentTimeMillis()<end){if("true".equals(js(code)))return;Thread.sleep(200);}throw new AssertionError("Condition failed: "+code);}
 void image()throws Exception{await("$('pdf-image')?.dataset.page===String(page)&&$('pdf-image').naturalWidth>0&&$('pdf-loading').classList.contains('hidden')",60);}
 @Override public void onCreate(Bundle b){super.onCreate(b);start();}
 @Override public void onStart(){Bundle result=new Bundle();try{
 activity=startActivitySync(new Intent().setClassName(getTargetContext(),"com.khalid.tajweedmeaning.webreader.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));runOnMainSync(()->web=find(activity.getWindow().getDecorView()));
 await("typeof changeMode==='function'&&typeof SIXTEEN_FRAMES==='object'",30);image();
 js("changeMode('madina');go(16)");image();js("changeMode('fifteen');go(613)");image();js("changeMode('word');go(100)");image();await("$('pdf-image').naturalWidth>0",10);
 js("changeMode('tajweed');go(1)");await("mode==='tajweed'&&$('page').textContent.includes('In the Name of Allah')",10);
 js("changeMode('plain');go(50)");image();
 js("changeMode('sixteen');go(5)");image();await("layoutForMode()==='sixteen-full'&&!!$('pdf-image').closest('.sixteen-frame')",10);
 js("screen('surahs')");await("document.querySelectorAll('[data-surah]').length===114",10);js("closePanels();go(480)");image();
 js("screen('settings')");await("$('pdf-fit').options.length===6&&document.querySelectorAll('[data-settings-mode]').length===8",10);js("closePanels();prefs.pdfZooms.sixteen=200;applyPrefs()");await("$('pdf-stage').scrollWidth>$('pdf-stage').clientWidth&&$('pdf-stage').scrollHeight>$('pdf-stage').clientHeight",10);
 js("$('pdf-stage').scrollLeft=20;$('pdf-stage').scrollTop=200");await("$('pdf-stage').scrollTop===200",5);
 js("if(!marks.includes(page))bookmark();prefs.pdfZooms.sixteen=100;applyPrefs()");await("load('tm-sixteen-bookmarks',[]).includes(480)",5);
 js("screen('stats');$('custom-daily-goal').value=37;$('custom-goal-form').requestSubmit()");await("prefs.goal===37&&Array.from($('daily-goal').options).some(x=>x.value==='37')",10);js("closePanels();changeMode('study')");image();await("mode==='study'&&maxPages()===1797",5);
 js("window.cachedStudy=page;window.downloadReady=false;studyPageResponse(page).then(()=>window.downloadReady=true)");await("window.downloadReady===true",30);
 getUiAutomation().executeShellCommand("svc wifi disable").close();getUiAutomation().executeShellCommand("svc data disable").close();
 js("changeMode('madina');go(605)");image();js("changeMode('fifteen');go(641)");image();js("changeMode('sixteen');go(550)");image();js("changeMode('word');go(960)");image();js("changeMode('plain');go(850)");image();js("changeMode('study');go(window.cachedStudy)");image();
 js("changeMode('sixteen');go(480)");image();runOnMainSync(()->web.reload());await("mode==='sixteen'&&page===480",30);image();await("marks.includes(480)",5);
 result.putString("stream","PASS: all seven modes, local final scans offline, Mode 4 saved-page cache, zoom pan, settings, Surahs and persistent bookmarks\n");finish(Activity.RESULT_OK,result);
 }catch(Throwable e){result.putString("stream","FAIL: "+e.toString()+"\n");finish(Activity.RESULT_CANCELED,result);}}
}
