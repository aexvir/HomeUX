package com.dravite.newlayouttest.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * Created by johannesbraun on 28.03.16.
 *
 */
public class RevealImageView extends View {

    private float mProgress = 0.5f;
    Drawable mForeground, mBackground;
    Paint mBackPaint;
    Paint mMaskPaint;
    Path mClipPath;
    PointF mRevealCenter = new PointF(0,0);

    public RevealImageView(Context context) {
        this(context, null);
    }

    public RevealImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RevealImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mBackPaint = new Paint();
        mBackPaint.setAntiAlias(true);

        mMaskPaint = new Paint();
        mMaskPaint.setAntiAlias(true);
        mMaskPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));

        mClipPath = new Path();
    }

    public void setRevealCenter(float x, float y){
        mRevealCenter.x = x;
        mRevealCenter.y = y;
    }

    @Override
    public void setForeground(Drawable foreground) {
        mForeground = foreground;
        layoutDrawable(mForeground);
        invalidate();
    }

    @Override
    public void setBackground(Drawable background) {
        mBackground = background;
        layoutDrawable(mBackground);
        invalidate();
    }

    public void setProgress(float progress){
        mProgress = progress;

        mClipPath.reset();
        mClipPath.addCircle(mRevealCenter.x,mRevealCenter.y, mProgress*(float)getCircleRadiusMax(), Path.Direction.CW);

        invalidate();
    }

    double getCircleRadiusMax(){
        double y = Math.max(mRevealCenter.y, getMeasuredHeight() - mRevealCenter.y);
        double x = Math.max(mRevealCenter.x, getMeasuredWidth() - mRevealCenter.x);

        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    void layoutDrawable(Drawable drawable){
        if(drawable!=null){

            // 1. get smallest downscale-value of width or height
            float maxDownScale = Math.min(drawable.getIntrinsicWidth()/(float)getMeasuredWidth(), drawable.getIntrinsicHeight()/(float)getMeasuredHeight());

            // 2. calculate l,t,r,b
            int drawableWidth = (int)(drawable.getIntrinsicWidth()/maxDownScale);
            int drawableHeight = (int)(drawable.getIntrinsicHeight()/maxDownScale);

            int left = getMeasuredWidth()/2 - drawableWidth/2;
            int right = getMeasuredWidth()/2 + drawableWidth/2;
            int top = getMeasuredHeight()/2 - drawableHeight/2;
            int bottom = getMeasuredHeight()/2 + drawableHeight/2;

            drawable.setBounds(left, top, right, bottom);

//            int height = ((int) ((drawable.getIntrinsicHeight() / (float) drawable.getIntrinsicWidth()) * getMeasuredWidth()));
//            drawable.setBounds(0, -height/2 + getMeasuredHeight()/2, getMeasuredWidth(), height/2 + getMeasuredHeight()/2);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        layoutDrawable(mForeground);
        layoutDrawable(mBackground);

        mClipPath.reset();
        mClipPath.addCircle(mRevealCenter.x,mRevealCenter.y, mProgress*(float)getCircleRadiusMax(), Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        if(mBackground!=null)
            mBackground.draw(canvas);
        canvas.clipPath(mClipPath);
        if(mForeground!=null)
            mForeground.draw(canvas);
    }
}
