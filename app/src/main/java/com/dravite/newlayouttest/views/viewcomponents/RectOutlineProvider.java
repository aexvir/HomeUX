package com.dravite.newlayouttest.views.viewcomponents;

import android.graphics.Outline;
import android.view.View;
import android.view.ViewOutlineProvider;

/**
 * Created by johannesbraun on 01.06.16.
 * This OutlineProvider is used for the top panel background which needs a shadow despite it having a transparent background.
 */
public class RectOutlineProvider extends ViewOutlineProvider {

    @Override
    public void getOutline(View view, Outline outline) {
        outline.setRect(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
    }
}
