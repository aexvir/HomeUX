package com.dravite.homeux.views.viewcomponents;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Draws a slightly darkish bluish text inside a white rounded rectangle.
 */
public class TextDrawable extends Drawable {
    private final String text;
    private final Paint paint;
    private final Paint bgPaint;
    private Rect mTextBounds = new Rect();
    private float mTextSize = 40f;
    private float mTextWidth = 0;
    private int mCornerPercentage;
    private int mPadding;

    public TextDrawable(String text, int cornerRadius, int padding, int textColor, int backgroundColor) {
        int textValue = Integer.valueOf(text);
        this.text = (textValue >= 100 ? "99+" : text);
        this.mCornerPercentage = cornerRadius;
        this.paint = new Paint();
        paint.setColor(textColor);
        paint.setTextSize(mTextSize);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);
        paint.setTypeface(Typeface.DEFAULT_BOLD);
        this.mTextWidth = paint.measureText(text);
        this.bgPaint = new Paint();
        bgPaint.setColor(backgroundColor);
        bgPaint.setAntiAlias(true);
        bgPaint.setShadowLayer(10f, 0, 2.5f, 0x7F000000);
        bgPaint.setStyle(Paint.Style.FILL);

        mPadding = padding;
    }

    private int calculateRadius(int height, int percentage) {
        float radius = ((height / 2.0f) * percentage) / 100;
        return (int) radius;
    }

    @Override
    public void draw(Canvas canvas) {
        Rect mRect = getBounds();
        paint.getTextBounds(text, 0, text.length(), mTextBounds);

        int badgeHeight = mTextBounds.height() + (2 * mPadding);
        int badgeWidth = Math.max(mTextBounds.width() + (2 * mPadding), badgeHeight);
        mRect.offset(canvas.getWidth() - badgeWidth, canvas.getHeight() / 2);
        mRect.offset(0, (int) (badgeHeight * -1.5f));

        int mCornerRadius = calculateRadius(badgeHeight, mCornerPercentage);

        canvas.drawRoundRect(
            mRect.left,
            mRect.top,
            mRect.left + badgeWidth, // In order to make it square for low numbers
            mRect.top + badgeHeight,
            mCornerRadius,
            mCornerRadius,
            bgPaint
        );
        canvas.drawText(text, mRect.left + ((int) badgeWidth / 2.0f) - ((int) mTextWidth / 2.0f), mRect.top + mTextBounds.height() + mPadding, paint);
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