package com.khalid.tajweedmeaning.adaptive;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.pdf.PdfRenderer;
import android.os.*;
import android.os.storage.StorageManager;
import android.system.ErrnoException;
import android.system.OsConstants;
import android.util.Base64;
import android.view.*;
import android.webkit.*;
import android.widget.FrameLayout;
import org.json.*;
import java.io.*;
import java.util.concurrent.*;

public class MainActivity extends Activity {
    private WebView web;
    private FrameLayout root;
    private final ExecutorService worker=Executors.newSingleThreadExecutor();
    private final HandlerThread proxyThread=new HandlerThread("BundledPdfRead");
    private PdfRenderer word,plain;
    private ParcelFileDescriptor wordDescriptor,plainDescriptor;
    private JSONArray crops,plainCrops;
    private volatile boolean volume,dead;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        proxyThread.start();
        if(Build.VERSION.SDK_INT>=30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE|View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN|View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        if(Build.VERSION.SDK_INT>=28) {
            WindowManager.LayoutParams attributes=getWindow().getAttributes();
            attributes.layoutInDisplayCutoutMode=WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
            getWindow().setAttributes(attributes);
        }
        root=new FrameLayout(this); root.setBackgroundColor(Color.rgb(255,253,247));
        web=new WebView(this);
        root.addView(web,new FrameLayout.LayoutParams(-1,-1)); setContentView(root);
        root.setOnApplyWindowInsetsListener((view,insets)->{
            int left,top,right,bottom;
            if(Build.VERSION.SDK_INT>=30) {
                android.graphics.Insets safe=insets.getInsets(WindowInsets.Type.systemBars()|WindowInsets.Type.displayCutout()|WindowInsets.Type.ime());
                left=safe.left;top=safe.top;right=safe.right;bottom=safe.bottom;
            } else {
                left=insets.getSystemWindowInsetLeft();top=insets.getSystemWindowInsetTop();right=insets.getSystemWindowInsetRight();bottom=insets.getSystemWindowInsetBottom();
                if(Build.VERSION.SDK_INT>=28 && insets.getDisplayCutout()!=null) {
                    DisplayCutout c=insets.getDisplayCutout();left=Math.max(left,c.getSafeInsetLeft());top=Math.max(top,c.getSafeInsetTop());right=Math.max(right,c.getSafeInsetRight());bottom=Math.max(bottom,c.getSafeInsetBottom());
                }
            }
            view.setPadding(left,top,right,bottom);
            return Build.VERSION.SDK_INT>=30 ? WindowInsets.CONSUMED : insets.consumeSystemWindowInsets();
        });
        WebSettings settings=web.getSettings();
        settings.setJavaScriptEnabled(true); settings.setDomStorageEnabled(true);
        settings.setBuiltInZoomControls(true);settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccess(true);settings.setAllowContentAccess(false);
        settings.setAllowFileAccessFromFileURLs(false);settings.setAllowUniversalAccessFromFileURLs(false);
        web.setWebViewClient(new WebViewClient(){
            @Override public boolean shouldOverrideUrlLoading(WebView w,WebResourceRequest r){return !"file".equals(r.getUrl().getScheme());}
        });
        web.addJavascriptInterface(new Bridge(),"Android");
        web.loadUrl("file:///android_asset/index.html");
        root.requestApplyInsets();
    }

    public class Bridge {
        @JavascriptInterface public void pdfPage(int n){render(false,n);}
        @JavascriptInterface public void plainPage(int n){render(true,n);}
        @JavascriptInterface public void awake(boolean on){runOnUiThread(()->{if(on)getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);else getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);});}
        @JavascriptInterface public void volumeKeys(boolean on){volume=on;}
        @JavascriptInterface public void fullscreen(boolean on){runOnUiThread(()->{
            if(Build.VERSION.SDK_INT>=30) {
                WindowInsetsController c=getWindow().getInsetsController();
                c.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
                if(on)c.hide(WindowInsets.Type.systemBars());else c.show(WindowInsets.Type.systemBars());
            } else {
                View decor=getWindow().getDecorView();int hide=View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY|View.SYSTEM_UI_FLAG_FULLSCREEN|View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                decor.setSystemUiVisibility((decor.getSystemUiVisibility()&~hide)|(on?hide:0));
            }
            root.requestApplyInsets();
        });}
        @JavascriptInterface public void appearance(boolean dark){runOnUiThread(()->{
            int color=dark?Color.rgb(21,30,27):Color.rgb(255,253,247);root.setBackgroundColor(color);
            getWindow().setStatusBarColor(color);getWindow().setNavigationBarColor(color);
            if(Build.VERSION.SDK_INT>=30) getWindow().getInsetsController().setSystemBarsAppearance(dark?0:WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS,WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS|WindowInsetsController.APPEARANCE_LIGHT_NAVIGATION_BARS);
            else {
                View decor=getWindow().getDecorView();int light=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR|View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
                decor.setSystemUiVisibility((decor.getSystemUiVisibility()&~light)|(dark?0:light));
            }
        });}
    }

    /** Seekable proxy reads the uncompressed asset's byte range directly from the APK. No 185 MB duplicate PDF cache. */
    private ParcelFileDescriptor bundledDescriptor(String name) throws IOException {
        final long start,length;
        try(AssetFileDescriptor asset=getAssets().openFd(name)){start=asset.getStartOffset();length=asset.getLength();}
        final RandomAccessFile apk=new RandomAccessFile(getApplicationInfo().sourceDir,"r");
        try {
            return getSystemService(StorageManager.class).openProxyFileDescriptor(ParcelFileDescriptor.MODE_READ_ONLY,new ProxyFileDescriptorCallback(){
                @Override public long onGetSize(){return length;}
                @Override public int onRead(long offset,int size,byte[] data) throws ErrnoException {
                    if(offset>=length)return 0;
                    try {apk.seek(start+offset);int amount=(int)Math.min(size,length-offset),done=0;while(done<amount){int n=apk.read(data,done,amount-done);if(n<0)break;done+=n;}return done;}
                    catch(IOException e){throw new ErrnoException("read",OsConstants.EIO);}
                }
                @Override public void onRelease(){try{apk.close();}catch(IOException ignored){}}
            },new Handler(proxyThread.getLooper()));
        } catch(IOException | RuntimeException e){apk.close();throw e;}
    }

    private void render(boolean isPlain,int number){
        if(number<1||number>(isPlain?850:960)||dead)return;
        final int pixelWidth=Math.min(2400,Math.max(1600,web.getWidth()*2));
        worker.execute(()->{
            Bitmap bitmap=null;
            try {
                if(isPlain && plain==null){plainDescriptor=bundledDescriptor("plain-13line.pdf");plain=new PdfRenderer(plainDescriptor);plainCrops=new JSONArray(readAsset("plain-crops.json"));}
                if(!isPlain && word==null){wordDescriptor=bundledDescriptor("word-by-word.pdf");word=new PdfRenderer(wordDescriptor);crops=new JSONArray(readAsset("word-crops.json"));}
                PdfRenderer renderer=isPlain?plain:word;
                try(PdfRenderer.Page page=renderer.openPage(number-1)) {
                    float x=0,y=0,w=page.getWidth(),h=page.getHeight();
                    {JSONArray c=(isPlain?plainCrops:crops).getJSONArray(number-1);x=(float)c.getDouble(0);y=(float)c.getDouble(1);w=(float)c.getDouble(2)-x;h=(float)c.getDouble(3)-y;}
                    float scale=pixelWidth/w;
                    bitmap=Bitmap.createBitmap(pixelWidth,(int)Math.ceil(h*scale),Bitmap.Config.ARGB_8888);
                    bitmap.eraseColor(Color.WHITE);Matrix transform=new Matrix();transform.setScale(scale,scale);transform.postTranslate(-x*scale,-y*scale);
                    page.render(bitmap,null,transform,PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);
                    ByteArrayOutputStream bytes=new ByteArrayOutputStream();bitmap.compress(Bitmap.CompressFormat.JPEG,94,bytes);
                    String data=Base64.encodeToString(bytes.toByteArray(),Base64.NO_WRAP);
                    reply(isPlain,number,data,null);
                }
            }catch(Exception error){reply(isPlain,number,null,"Unable to display this page. Please try again.");}
            finally {if(bitmap!=null)bitmap.recycle();}
        });
    }
    private String readAsset(String name) throws IOException {try(InputStream in=getAssets().open(name);ByteArrayOutputStream out=new ByteArrayOutputStream()){byte[] b=new byte[8192];int n;while((n=in.read(b))>=0)out.write(b,0,n);return out.toString("UTF-8");}}
    private void reply(boolean plain,int n,String data,String error){runOnUiThread(()->{if(!dead)web.evaluateJavascript("window."+(plain?"receivePlainPdf":"receivePdf")+"("+n+","+(data==null?"null":JSONObject.quote(data))+","+(error==null?"null":JSONObject.quote(error))+")",null);});}
    @Override public boolean onKeyDown(int code,KeyEvent event){if(volume && (code==KeyEvent.KEYCODE_VOLUME_DOWN||code==KeyEvent.KEYCODE_VOLUME_UP)){web.evaluateJavascript("window.turnPage("+(code==KeyEvent.KEYCODE_VOLUME_DOWN?1:-1)+")",null);return true;}return super.onKeyDown(code,event);}
    @Override public void onBackPressed(){web.evaluateJavascript("window.handleBack()",handled->{if(!"true".equals(handled))finish();});}
    @Override protected void onDestroy(){dead=true;web.removeJavascriptInterface("Android");web.destroy();worker.execute(()->{if(word!=null)word.close();if(plain!=null)plain.close();new Handler(proxyThread.getLooper()).post(()->proxyThread.quitSafely());});worker.shutdown();super.onDestroy();}
}
