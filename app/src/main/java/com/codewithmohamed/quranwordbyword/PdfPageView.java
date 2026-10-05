package com.codewithmohamed.quranwordbyword;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

/** One page at a time: bounded memory even for a 960-page PDF. */
public final class PdfPageView extends View {
    private Bitmap bitmap;
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.FILTER_BITMAP_FLAG);
    private float zoom = 1f, x, y, lastX, lastY;
    private boolean multiplePointers;
    private final ScaleGestureDetector pinch;
    private final GestureDetector taps;

    public PdfPageView(Context context) {
        super(context);
        setBackgroundColor(Color.rgb(225, 226, 219));
        setContentDescription("Qur’an PDF page. Pinch to zoom, drag to pan, double tap to zoom or fit.");
        pinch = new ScaleGestureDetector(context, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override public boolean onScale(ScaleGestureDetector detector) {
                float old = zoom;
                zoom = Math.max(1f, Math.min(5f, zoom * detector.getScaleFactor()));
                float factor = zoom / old;
                x = detector.getFocusX() - (detector.getFocusX() - x) * factor;
                y = detector.getFocusY() - (detector.getFocusY() - y) * factor;
                constrain(); invalidate(); return true;
            }
        });
        taps = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            @Override public boolean onDown(MotionEvent e) { return true; }
            @Override public boolean onSingleTapUp(MotionEvent e) { performClick(); return true; }
            @Override public boolean onDoubleTap(MotionEvent e) {
                if (zoom > 1.1f) fit();
                else {
                    zoom = 2.5f;
                    float fittedX = x, fittedY = y;
                    x = e.getX() - (e.getX() - fittedX) * zoom;
                    y = e.getY() - (e.getY() - fittedY) * zoom;
                    constrain(); invalidate();
                }
                return true;
            }
        });
    }
    public void setPage(Bitmap next) {
        Bitmap old = bitmap; bitmap = next; fit();
        if (old != null && old != next) old.recycle();
    }
    public void fit() {
        zoom = 1f;
        if (bitmap != null) {
            float base = baseScale();
            x = (getWidth() - bitmap.getWidth() * base) / 2f;
            y = (getHeight() - bitmap.getHeight() * base) / 2f;
        }
        invalidate();
    }
    private float baseScale() {
        return Math.min((float) getWidth() / bitmap.getWidth(), (float) getHeight() / bitmap.getHeight());
    }
    private void constrain() {
        if (bitmap == null) return;
        float w = bitmap.getWidth() * baseScale() * zoom;
        float h = bitmap.getHeight() * baseScale() * zoom;
        x = w <= getWidth() ? (getWidth() - w) / 2f : Math.max(getWidth() - w, Math.min(0, x));
        y = h <= getHeight() ? (getHeight() - h) / 2f : Math.max(getHeight() - h, Math.min(0, y));
    }
    @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) { fit(); }
    @Override protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (bitmap == null) return;
        canvas.save(); canvas.translate(x, y); canvas.scale(baseScale() * zoom, baseScale() * zoom);
        canvas.drawBitmap(bitmap, 0, 0, paint); canvas.restore();
    }
    @Override public boolean onTouchEvent(MotionEvent e) {
        pinch.onTouchEvent(e); taps.onTouchEvent(e);
        switch (e.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                multiplePointers = false; lastX = e.getX(); lastY = e.getY(); break;
            case MotionEvent.ACTION_POINTER_DOWN:
                multiplePointers = true; break;
            case MotionEvent.ACTION_MOVE:
                if (!pinch.isInProgress() && !multiplePointers && e.getPointerCount() == 1) {
                    x += e.getX() - lastX; y += e.getY() - lastY; constrain(); invalidate();
                }
                lastX = e.getX(); lastY = e.getY(); break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                multiplePointers = false; break;
            default: break;
        }
        return true;
    }
    @Override public boolean performClick() { super.performClick(); return true; }
}
