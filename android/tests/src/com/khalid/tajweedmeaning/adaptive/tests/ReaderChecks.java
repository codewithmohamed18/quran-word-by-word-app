package com.khalid.tajweedmeaning.adaptive.tests;
import android.app.*;
import android.os.*;
import android.content.Intent;
import android.view.*;
import android.webkit.WebView;
import android.graphics.Bitmap;
import org.json.*;
import java.io.*;
import java.util.concurrent.*;

/** Device checks use real pointer taps, not JavaScript click(), to catch inaccessible controls. */
public class ReaderChecks extends Instrumentation {
    private Activity activity; private WebView web; private String profile;
    @Override public void onCreate(Bundle args){super.onCreate(args);profile=args.getString("profile","phone");start();}
    @Override public void onStart(){
        Bundle result=new Bundle();
        try {
            activity=startActivitySync(new Intent().setClassName(getTargetContext(),"com.khalid.tajweedmeaning.adaptive.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            runOnMainSync(()->web=findWeb(activity.getWindow().getDecorView()));
            await("!!document.getElementById('modes-button')",30);
            await("document.querySelector('#pdf-stage img') && document.querySelector('#pdf-stage img').complete && document.querySelector('#pdf-stage img').naturalWidth>0",60);
            runOnMainSync(()->{
                WindowInsets insets=activity.getWindow().getDecorView().getRootWindowInsets();
                android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout());
                int[] location=new int[2];web.getLocationOnScreen(location);
                if(location[0]<safe.left||location[1]<safe.top)throw new AssertionError("WebView overlaps system bars/cutout");
                if(web.getHeight()>activity.getWindow().getDecorView().getHeight()-safe.top-safe.bottom)throw new AssertionError("WebView overlaps bottom navigation");
            });
            for(String id:new String[]{"menu","modes-button","bookmark","full","next","previous"})checkButton(id);
            screenshot("word-reader");
            tap("modes-button");await("!document.getElementById('sheet').classList.contains('hidden') && !!document.getElementById('plain-mode')",10);
            screenshot("modes");
            // Pick the large text mode first. Tests its responsive layout before requesting the second bundled PDF.
            tap("tajweed-mode");await("document.body.classList.contains('fill-reading')",15);
            tap("modes-button");tap("plain-mode");
            await("document.querySelector('#pdf-stage img') && document.querySelector('#pdf-stage img').complete && document.querySelector('#pdf-stage img').naturalWidth>0",60);
            tap("bookmark");await("document.getElementById('bookmark').getAttribute('aria-label')==='Remove page bookmark'",10);
            runOnMainSync(()->activity.finish());waitForIdleSync();
            activity=startActivitySync(new Intent().setClassName(getTargetContext(),"com.khalid.tajweedmeaning.adaptive.MainActivity").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
            runOnMainSync(()->web=findWeb(activity.getWindow().getDecorView()));
            await("document.getElementById('bookmark') && document.getElementById('bookmark').getAttribute('aria-label')==='Remove page bookmark'",30);
            await("document.querySelector('#pdf-stage img') && document.querySelector('#pdf-stage img').complete && document.querySelector('#pdf-stage img').naturalWidth>0",60);
            screenshot("reader");
            if(hasPdf(getTargetContext().getFilesDir())||hasPdf(getTargetContext().getCacheDir()))throw new AssertionError("Unexpected PDF copy in private storage");
            if(size(getTargetContext().getFilesDir())>2_000_000)throw new AssertionError("Unexpected full PDF copy in private files");
            tap("bookmark"); // Leave each display scenario with an unbookmarked page.
            result.putString("stream","PASS: "+profile+" — safe areas, real toolbar taps, three modes, both PDF renderers, persistent bookmark, no PDF copies\n");
            finish(Activity.RESULT_OK,result);
        }catch(Throwable error){result.putString("stream","FAIL: "+profile+" "+error.toString()+"\n");error.printStackTrace();try{screenshot("failure");}catch(Exception ignored){}finish(Activity.RESULT_CANCELED,result);}
    }
    private WebView findWeb(View view){if(view instanceof WebView)return(WebView)view;if(view instanceof ViewGroup){ViewGroup g=(ViewGroup)view;for(int i=0;i<g.getChildCount();i++){WebView found=findWeb(g.getChildAt(i));if(found!=null)return found;}}return null;}
    private String js(String source)throws Exception{CountDownLatch done=new CountDownLatch(1);String[] answer=new String[1];runOnMainSync(()->web.evaluateJavascript(source,value->{answer[0]=value;done.countDown();}));if(!done.await(10,TimeUnit.SECONDS))throw new AssertionError("JavaScript timed out");return answer[0];}
    private void await(String source,int seconds)throws Exception{long end=SystemClock.uptimeMillis()+seconds*1000;while(SystemClock.uptimeMillis()<end){if("true".equals(js("Boolean("+source+")")))return;Thread.sleep(150);}throw new AssertionError("Timed out: "+source+"; "+js("document.body.innerText.slice(0,800)"));}
    private void checkButton(String id)throws Exception{if(!"true".equals(js("(()=>{let r=document.getElementById('"+id+"').getBoundingClientRect();return r.width>=44 && r.height>=44 && r.left>=0 && r.right<=innerWidth+1 && r.top>=0 && r.bottom<=innerHeight+1})()")))throw new AssertionError("Inaccessible toolbar control: "+id);}
    private void tap(String id)throws Exception{
        js("document.getElementById('"+id+"').scrollIntoView({block:'nearest'})");waitForIdleSync();
        JSONArray p=new JSONArray(js("(()=>{let r=document.getElementById('"+id+"').getBoundingClientRect();return [r.left+r.width/2,r.top+r.height/2,innerWidth]})()"));
        int[] location=new int[2];int[] width=new int[1];runOnMainSync(()->{web.getLocationOnScreen(location);width[0]=web.getWidth();});
        float scale=(float)(width[0]/p.getDouble(2));float x=location[0]+(float)p.getDouble(0)*scale,y=location[1]+(float)p.getDouble(1)*scale;
        long now=SystemClock.uptimeMillis();MotionEvent down=MotionEvent.obtain(now,now,MotionEvent.ACTION_DOWN,x,y,0),up=MotionEvent.obtain(now,now+80,MotionEvent.ACTION_UP,x,y,0);
        sendPointerSync(down);sendPointerSync(up);down.recycle();up.recycle();waitForIdleSync();Thread.sleep(200);
    }
    private boolean hasPdf(File directory){File[] files=directory.listFiles();if(files!=null)for(File f:files){if(f.isDirectory()?hasPdf(f):f.getName().endsWith(".pdf"))return true;}return false;}
    private long size(File directory){long total=0;File[] files=directory.listFiles();if(files!=null)for(File f:files)total+=f.isDirectory()?size(f):f.length();return total;}
    private void screenshot(String name)throws Exception{File out=new File(getTargetContext().getExternalFilesDir(null),"checks");out.mkdirs();Bitmap b=getUiAutomation().takeScreenshot();try(FileOutputStream stream=new FileOutputStream(new File(out,profile+"-"+name+".png"))){b.compress(Bitmap.CompressFormat.PNG,100,stream);}b.recycle();}
}
