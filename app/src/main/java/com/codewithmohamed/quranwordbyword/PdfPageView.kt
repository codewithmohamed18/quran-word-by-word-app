package com.codewithmohamed.quranwordbyword

import android.content.Context
import android.graphics.*
import android.view.*
import kotlinx.coroutines.*
import kotlin.math.*

/** Gesture-aware platform view hosted by Compose. Sharp visible-region rendering at zoom. */
class PdfPageView(context: Context) : View(context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
    private var preview: Bitmap? = null
    private var tile: Bitmap? = null
    private var engine: PdfEngine? = null
    private var page = 0
    private var generation = 0
    private var tileGeneration = 0
    private var dead = false
    private var zoom = 1f
    private var x = 0f; private var y = 0f
    private var lastX = 0f; private var lastY = 0f
    private var startX = 0f; private var startY = 0f
    private var multi = false
    var onDisplayed: (Int) -> Unit = {}
    var onFailure: (String) -> Unit = {}
    var onSwipe: (Int) -> Unit = {}
    var onTap: () -> Unit = {}
    private val pinch = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(d: ScaleGestureDetector): Boolean {
            clearTile()
            val old = zoom; zoom = (zoom*d.scaleFactor).coerceIn(1f, 6f)
            val factor = zoom/old
            x = d.focusX-(d.focusX-x)*factor; y = d.focusY-(d.focusY-y)*factor
            constrain(); invalidate(); return true
        }
        override fun onScaleEnd(d: ScaleGestureDetector) { requestTile() }
    })
    private val taps = GestureDetector(context, object : GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent) = true
        override fun onSingleTapConfirmed(e: MotionEvent): Boolean { performClick(); return true }
        override fun onDoubleTap(e: MotionEvent): Boolean {
            clearTile()
            if (zoom > 1.01f) fit() else {
                zoom=2.5f; x=e.x-(e.x-x)*zoom; y=e.y-(e.y-y)*zoom; constrain(); invalidate(); requestTile()
            }
            return true
        }
    })
    fun showPage(number: Int, pdf: PdfEngine, fitRequest: Int) {
        if (number != page || engine !== pdf) {
            page=number; engine=pdf; loadPreview()
        } else if (fitRequest != lastFitRequest) fit()
        lastFitRequest=fitRequest
    }
    private var lastFitRequest = 0
    private fun loadPreview() {
        if (width < 1 || height < 1 || page == 0 || dead) return
        val token=++generation; val current=page; val pdf=engine ?: return
        clearTile(); preview?.recycle(); preview=null; invalidate()
        scope.launch {
            try {
                val result=pdf.render(current, width*2)
                if (dead || token != generation) result.recycle()
                else { preview=result; fit(); onDisplayed(current) }
            } catch (e: Exception) {
                if (!dead && token==generation) onFailure("This page could not be rendered. Try another page.")
            }
        }
    }
    fun fit() {
        clearTile(); zoom=1f
        preview?.let { x=(width-it.width*base())/2; y=0f }
        invalidate()
    }
    private fun base(): Float = preview?.let { width.toFloat()/it.width } ?: 1f
    private fun constrain() {
        preview?.let {
            val w=it.width*base()*zoom; val h=it.height*base()*zoom
            x=if(w<=width) (width-w)/2 else x.coerceIn(width-w,0f)
            y=if(h<=height) 0f else y.coerceIn(height-h,0f)
        }
    }
    private fun clearTile() { tileGeneration++; tile?.recycle(); tile=null }
    private fun requestTile() {
        val bitmap=preview ?: return; val pdf=engine ?: return
        if (zoom<=1.01f || dead || width<1 || height<1) return
        val token=++tileGeneration; val p=page; val scale=base()*zoom; val tx=x; val ty=y; val previewWidth=bitmap.width
        scope.launch {
            try {
                val result=pdf.renderViewport(p, previewWidth,scale,tx,ty,width,height)
                if(dead || token!=tileGeneration) result.recycle()
                else { tile?.recycle(); tile=result; invalidate() }
            } catch (_: Exception) { /* Preview stays readable if a sharper viewport cannot render. */ }
        }
    }
    override fun onSizeChanged(w: Int,h: Int,oldw: Int,oldh: Int) {
        if(w!=oldw || preview==null) loadPreview()
        else { clearTile(); constrain(); invalidate(); requestTile() }
    }
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        preview?.let {
            canvas.save(); canvas.translate(x,y); canvas.scale(base()*zoom,base()*zoom)
            canvas.drawBitmap(it,0f,0f,paint); canvas.restore()
        }
        tile?.let { canvas.drawBitmap(it,null,Rect(0,0,width,height),paint) }
    }
    override fun onTouchEvent(e: MotionEvent): Boolean {
        pinch.onTouchEvent(e); taps.onTouchEvent(e)
        when(e.actionMasked) {
            MotionEvent.ACTION_DOWN -> { multi=false; startX=e.x; startY=e.y; lastX=e.x; lastY=e.y }
            MotionEvent.ACTION_POINTER_DOWN -> multi=true
            MotionEvent.ACTION_MOVE -> {
                if(!pinch.isInProgress && !multi && (zoom>1.01f || (preview?.let { it.height*base()>height } == true))) {
                    clearTile(); x+=e.x-lastX; y+=e.y-lastY; constrain(); invalidate()
                }
                lastX=e.x; lastY=e.y
            }
            MotionEvent.ACTION_UP -> {
                val dx=e.x-startX; val dy=e.y-startY
                if(!multi && zoom<=1.01f && abs(dx)>width*.18f && abs(dy)<abs(dx)*.6f) onSwipe(if(dx<0) 1 else -1)
                else requestTile()
                multi=false
            }
            MotionEvent.ACTION_CANCEL -> { multi=false; requestTile() }
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); onTap(); return true }
    fun dispose() { dead=true; generation++; clearTile(); preview?.recycle(); preview=null }
}
