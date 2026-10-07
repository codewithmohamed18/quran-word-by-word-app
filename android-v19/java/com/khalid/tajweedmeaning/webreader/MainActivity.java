package com.khalid.tajweedmeaning.webreader;
import android.app.Activity;
import android.os.Bundle;
import android.net.Uri;
import android.content.Intent;
import android.graphics.Color;
import android.webkit.*;
import java.util.*;
import java.io.*;
public class MainActivity extends Activity {
 private WebView web;
 private static final String HOST="appassets.androidplatform.net";
 private WebResourceResponse asset(WebResourceRequest request){
  Uri u=request.getUrl();if(!HOST.equals(u.getHost()))return null;
  String path=u.getPath();if(path==null||path.equals("/"))path="/index.html";
  if(!"GET".equals(request.getMethod())||path.contains(".."))return missing();
  String ext=path.substring(path.lastIndexOf('.')+1).toLowerCase(Locale.ROOT);
  String mime=ext.equals("js")?"application/javascript":ext.equals("html")?"text/html":ext.equals("css")?"text/css":ext.equals("json")?"application/json":ext.equals("jpeg")||ext.equals("jpg")?"image/jpeg":ext.equals("png")?"image/png":ext.equals("webp")?"image/webp":ext.equals("svg")?"image/svg+xml":ext.equals("ttf")?"font/ttf":ext.equals("otf")?"font/otf":"application/octet-stream";
  try{Map<String,String> headers=new HashMap<>();headers.put("Service-Worker-Allowed","/");return new WebResourceResponse(mime,mime.startsWith("text/")||ext.equals("js")||ext.equals("json")?"UTF-8":null,200,"OK",headers,getAssets().open("site"+path));}catch(IOException e){return missing();}
 }
 private WebResourceResponse missing(){return new WebResourceResponse("text/plain","UTF-8",404,"Not Found",new HashMap<>(),new ByteArrayInputStream(new byte[0]));}
 private void external(Uri uri){if(!"https".equals(uri.getScheme())&&!"http".equals(uri.getScheme()))return;try{startActivity(new Intent(Intent.ACTION_VIEW,uri));}catch(Exception e){android.widget.Toast.makeText(this,"No browser is available to open this link",android.widget.Toast.LENGTH_SHORT).show();}}
 @Override public void onCreate(Bundle state){super.onCreate(state);web=new WebView(this);android.widget.FrameLayout root=new android.widget.FrameLayout(this);root.addView(web,new android.widget.FrameLayout.LayoutParams(-1,-1));setContentView(root);web.setBackgroundColor(Color.rgb(255,249,234));
  root.setOnApplyWindowInsetsListener((v,insets)->{v.setPadding(insets.getSystemWindowInsetLeft(),insets.getSystemWindowInsetTop(),insets.getSystemWindowInsetRight(),insets.getSystemWindowInsetBottom());return insets;});root.requestApplyInsets();
  WebSettings settings=web.getSettings();settings.setJavaScriptEnabled(true);settings.setDomStorageEnabled(true);settings.setDatabaseEnabled(true);settings.setAllowFileAccess(false);settings.setAllowContentAccess(false);settings.setMixedContentMode(WebSettings.MIXED_CONTENT_NEVER_ALLOW);settings.setSupportMultipleWindows(true);
  web.setWebViewClient(new WebViewClient(){@Override public WebResourceResponse shouldInterceptRequest(WebView view,WebResourceRequest request){return asset(request);}@Override public boolean shouldOverrideUrlLoading(WebView view,WebResourceRequest request){if(HOST.equals(request.getUrl().getHost()))return false;external(request.getUrl());return true;}});
  ServiceWorkerController.getInstance().setServiceWorkerClient(new ServiceWorkerClient(){@Override public WebResourceResponse shouldInterceptRequest(WebResourceRequest request){return asset(request);}});
  web.setWebChromeClient(new WebChromeClient(){@Override public boolean onCreateWindow(WebView view,boolean dialog,boolean gesture,android.os.Message result){WebView child=new WebView(MainActivity.this);child.setWebViewClient(new WebViewClient(){@Override public boolean shouldOverrideUrlLoading(WebView v,WebResourceRequest request){external(request.getUrl());v.destroy();return true;}});((WebView.WebViewTransport)result.obj).setWebView(child);result.sendToTarget();return true;}});
  web.addJavascriptInterface(new Object(){@JavascriptInterface public void shareLink(String link){Uri u=Uri.parse(link);if(!"https".equals(u.getScheme())||!"codewithmohamed18.github.io".equals(u.getHost()))return;runOnUiThread(()->{Intent i=new Intent(Intent.ACTION_SEND);i.setType("text/plain");i.putExtra(Intent.EXTRA_TEXT,link);startActivity(Intent.createChooser(i,"Share this reading"));});}},"NativeV19");
  web.setDownloadListener((url,agent,disposition,mime,length)->external(Uri.parse(url)));
  web.loadUrl("https://"+HOST+"/index.html");
 }
 @Override public void onBackPressed(){web.evaluateJavascript("(()=>{if(!$('sheet').classList.contains('hidden')||!$('drawer').classList.contains('hidden')){closePanels();return true}if(typeof immersive!=='undefined'&&immersive){fullscreen(false);return true}return false})()",handled->{if(!"true".equals(handled)){if(web.canGoBack())web.goBack();else finish();}});}
 @Override protected void onPause(){web.onPause();super.onPause();}
 @Override protected void onResume(){super.onResume();if(web!=null)web.onResume();}
 @Override protected void onDestroy(){if(web!=null)web.destroy();super.onDestroy();}
}
