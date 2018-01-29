package com.dravite.newlayouttest.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by Johannes on 13.10.2015.
 * This View displays an image inside a round shape with a set background color.<br/>
 * TODO: We use an outline, maybe its better to use some kind of alpha blend for antialiasing.
 */
public class RoundImageView extends ImageView {

    //Struct like thingy
    static class Color{
        int mRed, mGreen, mBlue, mAlpha;

        Color(int alpha, int red, int green, int blue){
            mRed = red;
            mAlpha = alpha;
            mGreen = green;
            mBlue = blue;
        }

        void set(int alpha, int red, int green, int blue){
            mRed = red;
            mAlpha = alpha;
            mGreen = green;
            mBlue = blue;
        }

        void set(int color){
            mAlpha = android.graphics.Color.alpha(color);
            mRed = android.graphics.Color.red(color);
            mGreen = android.graphics.Color.green(color);
            mBlue = android.graphics.Color.blue(color);
        }
    }

    private final Path clipPath = new Path();
    Color mColor = new Color(145, 0, 0, 0);

    public RoundImageView(Context context) {
        super(context);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public RoundImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr,
                     int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    public void setBackgroundColor(int color) {
        if(color==-1) mColor.set(145, 0, 0, 0);
        else mColor.set(color);
    }

    @Override
    protected void onDraw(Canvas canvas)
    {
        float radius = ((float) getWidth()) / 2;

        clipPath.reset();
        clipPath.addCircle(radius, radius, radius, Path.Direction.CW);

        canvas.clipPath(clipPath);
        canvas.drawARGB(mColor.mAlpha, mColor.mRed, mColor.mGreen, mColor.mBlue);
        super.onDraw(canvas);
    }
}
