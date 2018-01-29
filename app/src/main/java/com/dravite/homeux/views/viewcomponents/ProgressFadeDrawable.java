package com.dravite.homeux.views.viewcomponents;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

/**
 * Created by Johannes on 23.10.2015.
 * A Drawable that fades between two drawables depending on a given progress value between 0 and 1,
 */
public class ProgressFadeDrawable extends Drawable {

    int mAlpha = 255;
    float mProgress = 0;
    Drawable mDrawableStart,
        mDrawableEnd;


    public ProgressFadeDrawable(Drawable start, Drawable end){
        this.mDrawableStart = start;
        this.mDrawableEnd = end;
    }

    @Override
    public void draw(Canvas canvas) {
        int endAlpha = (int)(mAlpha*mProgress);
        int startAlpha = (int)(mAlpha*(1f-mProgress));

        mDrawableStart.setBounds(getBounds());
        mDrawableEnd.setBounds(getBounds());

        mDrawableStart.setAlpha(startAlpha);
        mDrawableEnd.setAlpha(endAlpha);

        mDrawableStart.draw(canvas);
        mDrawableEnd.draw(canvas);
    }

    public void setDrawableStart(Drawable drawableStart) {
        this.mDrawableStart = drawableStart;
        invalidateSelf();

    }

    public void setDrawableEnd(Drawable drawableEnd) {
        this.mDrawableEnd = drawableEnd;
        invalidateSelf();
    }

    @Override
    public void setTint(int tintColor) {
        mDrawableEnd.setTint(tintColor);
        mDrawableStart.setTint(tintColor);
        invalidateSelf();
    }

    public void setProgress(float progress){
        mProgress = progress;
        invalidateSelf();
    }

    @Override
    public void setAlpha(int alpha) {
        this.mAlpha = alpha;
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        mDrawableStart.setColorFilter(cf);
        mDrawableEnd.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}
