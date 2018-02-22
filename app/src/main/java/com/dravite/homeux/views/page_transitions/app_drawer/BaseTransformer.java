package com.dravite.homeux.views.page_transitions.app_drawer;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * A more direct abstract version of the {@link android.support.v4.view.ViewPager.PageTransformer}.
 */
public abstract class BaseTransformer implements ViewPager.PageTransformer {

    @Override
    public void transformPage(View view, float position) {
        if(Math.abs(position)>1){
            view.setVisibility(View.INVISIBLE);
        } else {
            view.setVisibility(View.VISIBLE);
            transformRaw(view, position);
        }
    }

    /**
     * Transforms the given View only when visible, so no checking for the position being >1 or <-1 needed. See {@link android.support.v4.view.ViewPager.PageTransformer#transformPage(View, float)}.
     * @param view The View to transform
     * @param position The position of the View.
     */
    public abstract void transformRaw(View view, float position);
}
