package com.dravite.homeux.general_helpers;

import android.content.Context;
import android.support.v4.view.ViewPager;

import com.dravite.homeux.R;
import com.dravite.homeux.views.page_transitions.app_drawer.AccordionTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.BackgroundToForegroundTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.CubeOutTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.DefaultTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.DelayTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.DepthPageTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.FlipHorizontalTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.FlipVerticalTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.ForegroundToBackgroundTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.RotateDownTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.RotateUpTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.ScaleInOutTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.StackTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.TabletTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.ZoomInTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.ZoomOutSlideTransformer;
import com.dravite.homeux.views.page_transitions.app_drawer.ZoomOutTranformer;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Johannes on 28.03.2015.
 * Collects and provides diverse PageTransitions for the AppDrawer ViewPager.
 */
public class PageTransitionManager {
    public static String[] mTransitionNames;    //Literal names from XML
    public static Map<String, ViewPager.PageTransformer> pageTransformerMap = new HashMap<>(); //IDs map to their Transformer classes
    private static String[] mTransitionIds;     //IDs from XML

    /**
     * Puts all the Transformers into the map.
     * @param context
     */
    public static void initialize(Context context) {
        mTransitionIds = context.getResources().getStringArray(R.array.page_transformer_ids);
        mTransitionNames = context.getResources().getStringArray(R.array.page_transformer_names);
        pageTransformerMap.put(mTransitionIds[0], new AccordionTransformer());
        pageTransformerMap.put(mTransitionIds[1], new BackgroundToForegroundTransformer());
        pageTransformerMap.put(mTransitionIds[2], new CubeOutTransformer());
        pageTransformerMap.put(mTransitionIds[3], new DefaultTransformer());
        pageTransformerMap.put(mTransitionIds[4], new DepthPageTransformer());
        pageTransformerMap.put(mTransitionIds[5], new FlipHorizontalTransformer());
        pageTransformerMap.put(mTransitionIds[6], new FlipVerticalTransformer());
        pageTransformerMap.put(mTransitionIds[7], new ForegroundToBackgroundTransformer());
        pageTransformerMap.put(mTransitionIds[8], new RotateDownTransformer());
        pageTransformerMap.put(mTransitionIds[9], new RotateUpTransformer());
        pageTransformerMap.put(mTransitionIds[10], new ScaleInOutTransformer());
        pageTransformerMap.put(mTransitionIds[11], new StackTransformer());
        pageTransformerMap.put(mTransitionIds[12], new TabletTransformer());
        pageTransformerMap.put(mTransitionIds[13], new ZoomInTransformer());
        pageTransformerMap.put(mTransitionIds[14], new ZoomOutSlideTransformer());
        pageTransformerMap.put(mTransitionIds[15], new ZoomOutTranformer());
        pageTransformerMap.put(mTransitionIds[16], new DelayTransformer());
    }

    public static ViewPager.PageTransformer getTransformer(int index) {
        return pageTransformerMap.get(mTransitionIds[index]);
    }
}
