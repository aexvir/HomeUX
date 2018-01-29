package com.dravite.homeux.views;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.View;

import com.dravite.homeux.LauncherLog;
import com.dravite.homeux.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by johannesbraun on 01.06.16.
 * A view showing dots below the AppDrawer to indicate the currently selected page and the page count.
 */
public class IndicatorView extends View {

    private static final float MAX_SIZE_DIFFERENCE = 8;

    //Sizes of the indicator circles
    private List<Float> mSizeDifferences = new ArrayList<>();

    private Paint mDotPaint;
    private int mDotColor;
    private Paint mShadowPaint;
    private int mDotCount = 0;
    private int mCurrentSelected = 2;

    private ViewPager mPager;


    public IndicatorView(Context context) {
        this(context, null);
    }

    public IndicatorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public IndicatorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    private void init(@Nullable AttributeSet attrs){
        mDotPaint = new Paint();
        mShadowPaint = new Paint();

        if(attrs==null){

            mDotColor = Color.WHITE;
            setDotCount(1);
        } else {
            TypedArray attribArray = getContext().obtainStyledAttributes(attrs, new int[]{R.attr.dotColor});
            mDotColor = attribArray.getColor(0, Color.WHITE);
            attribArray.recycle();

            attribArray = getContext().obtainStyledAttributes(attrs, new int[]{R.attr.dotCount});
            setDotCount(attribArray.getInt(0, 1));
            attribArray.recycle();
        }

        mDotPaint.setAntiAlias(true);
        mDotPaint.setColor(mDotColor);

        mShadowPaint.setAntiAlias(true);
        mShadowPaint.setColor(0x55000000);
    }
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        int widthSize = MeasureSpec.getSize(widthMeasureSpec);
        int height = 80;

        setMeasuredDimension(widthSize, height);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if(mDotCount>1) {
            for (int i = 0; i < mDotCount; i++) {
                canvas.drawCircle(getWidth() / 2 + (i - mDotCount / 2) * 40 + (1 - mDotCount % 2) * 20, getHeight() / 2 + 2, 11 + mSizeDifferences.get(i), mShadowPaint);
                canvas.drawCircle(getWidth() / 2 + (i - mDotCount / 2) * 40 + (1 - mDotCount % 2) * 20, getHeight() / 2, 10 + mSizeDifferences.get(i), mDotPaint);
            }
        }
    }

    public void setDotCount(int count){
        mDotCount = count;
        mSizeDifferences.clear();
        for (int i = 0; i < count; i++) {
            mSizeDifferences.add(mCurrentSelected==i?MAX_SIZE_DIFFERENCE:0f);
        }

        invalidate();
    }

    public void update(){
        setDotCount(mPager.getAdapter().getCount());
    }

    /**
     * Adjusts dot count to the given pager and assigns a PageChangeListener to it.
     * @param newPager
     */
    public void setPager(ViewPager newPager){
        mPager = newPager;
        setDotCount(mPager.getAdapter().getCount());
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                setCurrentSelectedAnimated(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    public void setCurrentSelectedAnimated(final int newSelection){
        LauncherLog.d(getClass().getName(), newSelection + " is new selected");

        final int oldSelection = mCurrentSelected;
        mCurrentSelected = newSelection;

        ValueAnimator animatorOut = ObjectAnimator.ofFloat(MAX_SIZE_DIFFERENCE, 0);
        animatorOut.addUpdateListener(makeAnimUpdateListener(oldSelection, mCurrentSelected));
        animatorOut.setDuration(200);
        animatorOut.start();
    }

    private ValueAnimator.AnimatorUpdateListener makeAnimUpdateListener(final int oldI, final int newI){
        return new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                if(oldI<mSizeDifferences.size()) mSizeDifferences.set(oldI, (float)animation.getAnimatedValue());
                mSizeDifferences.set(newI, MAX_SIZE_DIFFERENCE-(float)animation.getAnimatedValue());
                invalidate();
            }
        };
    }

    public void setCurrentSelectedInstant(int newSelection){
        for (int i = 0; i < mDotCount; i++) {
            mSizeDifferences.set(i, i==newSelection?5f:0f);
        }
        mCurrentSelected = newSelection;
        invalidate();
    }

    public void next(){
        if(mCurrentSelected+1<mDotCount)
            setCurrentSelectedAnimated(mCurrentSelected+1);
    }

    public void previous(){
        if(mCurrentSelected-1>=0)
            setCurrentSelectedAnimated(mCurrentSelected-1);
    }
}
