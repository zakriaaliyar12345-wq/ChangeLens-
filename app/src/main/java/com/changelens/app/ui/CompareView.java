package com.changelens.app.ui;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.ViewCompat;

import com.changelens.app.R;
import com.changelens.app.data.entity.Annotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Before/after comparison view.
 * SLIDER mode reveals the "after" image on one side of a draggable divider;
 * OVERLAY mode draws the "after" image with adjustable transparency on top of
 * the "before" image. Both images use identical fit-center scaling so they stay
 * perfectly aligned. Annotations are drawn with normalized coordinates.
 */
public class CompareView extends View {

    public static final int MODE_SLIDER = 0;
    public static final int MODE_OVERLAY = 1;

    public interface OnDividerChangedListener {
        void onDividerChanged(float fraction);
    }

    private Bitmap before;
    private Bitmap after;
    private int mode = MODE_SLIDER;
    private float divider = 0.5f;
    private float overlayAlpha = 0.5f;
    private boolean dragging = false;
    private float downX;
    private float lastAnnounceX = -1f;
    private OnDividerChangedListener dividerListener;

    private final Paint bitmapPaint = new Paint(Paint.FILTER_BITMAP_FLAG);
    private final Paint linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handleRingPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint annotationPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint annotationTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint annotationTextBgPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Rect srcRect = new Rect();
    private final RectF dstRect = new RectF();
    private final RectF handleRect = new RectF();
    private final List<Annotation> annotations = new ArrayList<>();

    public CompareView(Context context) {
        this(context, null);
    }

    public CompareView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CompareView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        float density = context.getResources().getDisplayMetrics().density;
        linePaint.setColor(Color.WHITE);
        linePaint.setStrokeWidth(3 * density);
        handlePaint.setColor(context.getColor(R.color.cl_slider_handle));
        handleRingPaint.setColor(Color.WHITE);
        handleRingPaint.setStrokeWidth(2.5f * density);
        handleRingPaint.setStyle(Paint.Style.STROKE);
        annotationPaint.setColor(context.getColor(R.color.cl_annotation));
        annotationTextPaint.setColor(Color.WHITE);
        annotationTextPaint.setTextSize(13 * density);
        annotationTextBgPaint.setColor(0xCC000000);
        setFocusable(true);
        setClickable(true);
        setContentDescription(context.getString(R.string.content_description_compare_view));
    }

    public void setBefore(@Nullable Bitmap b) {
        this.before = b;
        invalidate();
    }

    @Nullable
    public Bitmap getBefore() {
        return before;
    }

    public void setAfter(@Nullable Bitmap b) {
        this.after = b;
        invalidate();
    }

    @Nullable
    public Bitmap getAfter() {
        return after;
    }

    public void setMode(int mode) {
        this.mode = mode;
        invalidate();
    }

    public int getMode() {
        return mode;
    }

    public void setDivider(float fraction) {
        this.divider = clamp(fraction, 0.02f, 0.98f);
        invalidate();
    }

    public float getDivider() {
        return divider;
    }

    public void setOverlayAlpha(float alpha) {
        this.overlayAlpha = clamp(alpha, 0f, 1f);
        invalidate();
    }

    public float getOverlayAlpha() {
        return overlayAlpha;
    }

    public void setAnnotations(@Nullable List<Annotation> list) {
        annotations.clear();
        if (list != null) {
            annotations.addAll(list);
        }
        invalidate();
    }

    public void setOnDividerChangedListener(OnDividerChangedListener listener) {
        this.dividerListener = listener;
    }

    public void swapImages() {
        Bitmap tmp = before;
        before = after;
        after = tmp;
        invalidate();
    }

    private static float clamp(float v, float min, float max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth();
        int h = getHeight();
        if (w == 0 || h == 0) {
            return;
        }
        if (before == null && after == null) {
            return;
        }
        if (mode == MODE_OVERLAY) {
            drawOverlay(canvas, w, h);
        } else {
            drawSlider(canvas, w, h);
        }
        drawAnnotations(canvas, w, h);
    }

    private void drawFitCenter(@NonNull Canvas canvas, @Nullable Bitmap bitmap, int w, int h,
                               @Nullable Rect clip, float alpha) {
        if (bitmap == null || bitmap.isRecycled()) {
            return;
        }
        float scale = Math.min((float) w / bitmap.getWidth(), (float) h / bitmap.getHeight());
        float dw = bitmap.getWidth() * scale;
        float dh = bitmap.getHeight() * scale;
        float left = (w - dw) / 2f;
        float top = (h - dh) / 2f;
        dstRect.set(left, top, left + dw, top + dh);
        srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
        int save = canvas.save();
        if (clip != null) {
            canvas.clipRect(clip);
        }
        Paint p = bitmapPaint;
        int oldAlpha = p.getAlpha();
        if (alpha < 1f) {
            p.setAlpha(Math.round(alpha * 255));
        }
        canvas.drawBitmap(bitmap, srcRect, dstRect, p);
        p.setAlpha(oldAlpha);
        canvas.restoreToCount(save);
    }

    private void drawSlider(@NonNull Canvas canvas, int w, int h) {
        drawFitCenter(canvas, before, w, h, null, 1f);
        int dividerX = Math.round(w * divider);
        Rect afterClip = new Rect(dividerX, 0, w, h);
        drawFitCenter(canvas, after, w, h, afterClip, 1f);

        canvas.drawLine(dividerX, 0, dividerX, h, linePaint);
        float handleRadius = getResources().getDimension(R.dimen.cl_slider_handle_radius);
        handleRect.set(dividerX - handleRadius, h / 2f - handleRadius,
                dividerX + handleRadius, h / 2f + handleRadius);
        canvas.drawOval(handleRect, handlePaint);
        canvas.drawOval(handleRect, handleRingPaint);
        // Arrows inside the handle for affordance
        float cx = handleRect.centerX();
        float cy = handleRect.centerY();
        float a = handleRadius * 0.35f;
        Path left = new Path();
        left.moveTo(cx - a * 0.4f, cy);
        left.lineTo(cx + a * 0.2f, cy - a);
        left.lineTo(cx + a * 0.2f, cy + a);
        left.close();
        Path right = new Path();
        right.moveTo(cx + a * 0.4f, cy);
        right.lineTo(cx - a * 0.2f, cy - a);
        right.lineTo(cx - a * 0.2f, cy + a);
        right.close();
        Paint arrow = new Paint(Paint.ANTI_ALIAS_FLAG);
        arrow.setColor(Color.WHITE);
        canvas.drawPath(left, arrow);
        canvas.drawPath(right, arrow);
    }

    private void drawOverlay(@NonNull Canvas canvas, int w, int h) {
        drawFitCenter(canvas, before, w, h, null, 1f);
        drawFitCenter(canvas, after, w, h, null, overlayAlpha);
    }

    private void drawAnnotations(@NonNull Canvas canvas, int w, int h) {
        float radius = getResources().getDimension(R.dimen.cl_annotation_radius);
        for (Annotation a : annotations) {
            float x = a.normX * w;
            float y = a.normY * h;
            canvas.drawCircle(x, y, radius, annotationPaint);
            canvas.drawCircle(x, y, radius, handleRingPaint);
            if (a.text != null && !a.text.isEmpty()) {
                float textW = annotationTextPaint.measureText(a.text);
                float pad = 6 * getResources().getDisplayMetrics().density;
                float textH = annotationTextPaint.getTextSize();
                float left = clamp(x + radius + 4, 0, Math.max(0, w - textW - pad * 2));
                float top = clamp(y - textH / 2f - pad, 0, Math.max(0, h - textH - pad * 2));
                RectF bg = new RectF(left, top, left + textW + pad * 2, top + textH + pad * 2);
                canvas.drawRoundRect(bg, 6, 6, annotationTextBgPaint);
                canvas.drawText(a.text, left + pad, top + pad + textH * 0.8f, annotationTextPaint);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mode != MODE_SLIDER || getWidth() == 0) {
            return super.onTouchEvent(event);
        }
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getX();
                dragging = true;
                ViewCompat.setAccessibilityLiveRegion(this, ViewCompat.ACCESSIBILITY_LIVE_REGION_POLITE);
                getParent().requestDisallowInterceptTouchEvent(true);
                return true;
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                if (dragging) {
                    // Track the finger with a small offset so the handle stays visible.
                    float x = event.getX();
                    setDividerFromTouch(x);
                    if (event.getActionMasked() != MotionEvent.ACTION_MOVE) {
                        dragging = false;
                        ViewCompat.setAccessibilityLiveRegion(this, ViewCompat.ACCESSIBILITY_LIVE_REGION_NONE);
                        announceForAccessibility(describeDivider());
                    }
                    return true;
                }
                return super.onTouchEvent(event);
            default:
                return super.onTouchEvent(event);
        }
    }

    private void setDividerFromTouch(float x) {
        float fraction = clamp(x / getWidth(), 0.02f, 0.98f);
        divider = fraction;
        invalidate();
        if (dividerListener != null) {
            dividerListener.onDividerChanged(fraction);
        }
        announceThrottled(fraction);
    }

    private void announceThrottled(float fraction) {
        if (Math.abs(fraction - lastAnnounceX) > 0.05f) {
            lastAnnounceX = fraction;
            announceForAccessibility(describeDivider());
        }
    }

    private String describeDivider() {
        return getContext().getString(R.string.divider_position)
                + " " + String.format(Locale.US, "%d%%", Math.round(divider * 100));
    }

    /** Programmatic control used by the accessible SeekBar and keyboard. */
    public void setDividerFromExternal(float fraction) {
        setDividerFromTouch(fraction * getWidth());
    }

    public boolean isSliderMode() {
        return mode == MODE_SLIDER;
    }
}
