package com.dravite.newlayouttest.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.WindowInsets;
import android.widget.LinearLayout;

/**
 * Created by johannesbraun on 07.02.16.
 * This Layout has a fake fitsSystemWindows without being root View
 */
public class SearchResultLayout extends LinearLayout{

    public SearchResultLayout(Context context) {
        this(context, null);
    }

    public SearchResultLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SearchResultLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    @Override
    public WindowInsets onApplyWindowInsets(WindowInsets insets) {
        setPadding(getPaddingLeft(),
                getPaddingTop(),
                getPaddingRight(),
                insets.getSystemWindowInsetBottom()); //FitsSystemWindows
        return super.onApplyWindowInsets(insets);
    }
}
