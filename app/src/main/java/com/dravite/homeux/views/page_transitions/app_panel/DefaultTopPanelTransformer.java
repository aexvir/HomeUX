package com.dravite.homeux.views.page_transitions.app_panel;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.view.ViewGroup;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;

/**
 * The default {@link android.support.v4.view.ViewPager.PageTransformer} for the launcher's top panel.
 */
public class DefaultTopPanelTransformer implements ViewPager.PageTransformer {

    private Context mContext;

    public DefaultTopPanelTransformer(Context context){
        mContext = context;
    }

    @Override
    public void transformPage(View page, float position) {

        if (Math.abs(position) == 1) {
            page.setVisibility(View.INVISIBLE);
            page.setTranslationY(0);
        } else {
            page.setVisibility(View.VISIBLE);
            if (((ViewGroup) page).getChildCount() == 3) {
                //Is clock page, just zoom out, alpha to 0 and stay at place
                page.setTranslationY(-1);
                page.setTranslationX(-position * page.getWidth());
                page.setAlpha(1 - Math.abs(position));
                page.setScaleX(1 - Math.abs(position));
                page.setScaleY(1 - Math.abs(position));
            } else {
                //Otherwise put the page to the foreground...
                page.setTranslationY(1);
                //...and apply a hierarchy-dependant x translation to each child of the page. (depending on which page it is of course)
                if(page.findViewById(R.id.quick_settings_bg)!=null){
                    for(int i=0; i< ((ViewGroup) page.findViewById(R.id.quick_settings_bg)).getChildCount(); i++){
                        ((ViewGroup) page.findViewById(R.id.quick_settings_bg)).getChildAt(i).setTranslationX((2-i)*position*LauncherUtils.dpToPx(100, mContext));

                        ((ViewGroup) page.findViewById(R.id.quick_settings_bg)).getChildAt(i).setScaleX(1-Math.abs(position));
                        ((ViewGroup) page.findViewById(R.id.quick_settings_bg)).getChildAt(i).setScaleY(1-Math.abs(position));
                    }
                } else if(page.findViewById(R.id.folder_list)!=null) {
                    for(int i=0; i< ((ViewGroup) page.findViewById(R.id.folder_list)).getChildCount(); i++){
                        ((ViewGroup) page.findViewById(R.id.folder_list)).getChildAt(i).setTranslationX((i%4)*position*LauncherUtils.dpToPx(100, mContext));

                        ((ViewGroup) page.findViewById(R.id.folder_list)).getChildAt(i).setScaleX(1-Math.abs(position));
                        ((ViewGroup) page.findViewById(R.id.folder_list)).getChildAt(i).setScaleY(1-Math.abs(position));
                    }
                }
            }
        }
    }

}
