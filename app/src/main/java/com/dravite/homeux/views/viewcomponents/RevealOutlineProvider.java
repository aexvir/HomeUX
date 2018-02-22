package com.dravite.homeux.views.viewcomponents;

import android.annotation.TargetApi;
import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Created by Johannes on 21.10.2015.
 * This OutlineProvider creates a round outline and can be set to a progress to kind of "fade" between two given radii.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class RevealOutlineProvider extends ViewOutlineProvider {
    private int mCenterX;
    private int mCenterY;
    private float mRadius0;
    private float mRadius1;
    private int mCurrentRadius;
    private final Rect mOval;
    private View mView;
    private float mProgress;
    /**
     * @param x reveal center x
     * @param y reveal center y
     * @param r0 initial radius
     * @param r1 final radius
     */
    public RevealOutlineProvider(int x, int y, float r0, float r1) {
        mCenterX = x;
        mCenterY = y;
        mRadius0 = r0;
        mRadius1 = r1;
        mOval = new Rect();
    }

    public void setProgress(float progress) {
        this.mProgress = progress;
        mCurrentRadius = (int) ((1 - progress) * mRadius0 + progress * mRadius1); //lerp
        mOval.left = mCenterX - mCurrentRadius;
        mOval.top = mCenterY - mCurrentRadius;
        mOval.right = mCenterX + mCurrentRadius;
        mOval.bottom = mCenterY + mCurrentRadius;
        if(mView!=null)
            mView.invalidateOutline();
    }

    public float getProgress() {
        return mProgress;
    }

    public void assignView(View view){
        mView = view;
    }

    @Override
    public void getOutline(View v, Outline outline) {
        outline.setRoundRect(mOval, mCurrentRadius);
    }
}
