package com.dravite.homeux.views.viewcomponents;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;

/**
 * Draws a slightly darkish bluish text inside a white rounded rectangle.
 */
public class TextDrawable extends Drawable {

    private final String text;
    private final Paint paint;
    private final Paint bgPaint;
    private Rect mRect = new Rect();
    private Rect mTextBounds = new Rect();
    private int mCornerRadius;
    private int mPadding;

    public TextDrawable(String text, int cornerRadius, int padding) {

        this.text = text;
        this.mCornerRadius = cornerRadius;
        this.paint = new Paint();
        paint.setColor(0xffffffff);
        paint.setTextSize(33f);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        this.bgPaint = new Paint();
        bgPaint.setColor(0xff455A64);
        bgPaint.setAntiAlias(true);
        bgPaint.setShadowLayer(6f, .5f, 1.f, Color.BLACK);
        bgPaint.setStyle(Paint.Style.FILL);

        mPadding = padding;
    }

    @Override
    public void draw(Canvas canvas) {
        mRect = getBounds();
        paint.getTextBounds(text, 0, text.length(), mTextBounds);
        canvas.drawRoundRect(mRect.left, mRect.top, mRect.left+mTextBounds.width()+(4*mPadding), mRect.top+mTextBounds.height()+(2*mPadding), mCornerRadius, mCornerRadius, bgPaint);
        canvas.drawText(text, mRect.left+(2*mPadding), mRect.top + mTextBounds.height()+mPadding, paint);
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }
}