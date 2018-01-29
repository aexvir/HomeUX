
package com.dravite.newlayouttest.views.page_transitions.app_drawer;

import android.view.View;

public class ScaleInOutTransformer extends BaseTransformer {

    @Override
    public void transformRaw(View view, float position) {
        view.setPivotX(position > 0 ? 0 : view.getWidth());
        view.setPivotY(view.getHeight() / 2f);
        float scale = position < 0 ? 1f + position : 1f - position;
        view.setScaleX(scale);
        view.setScaleY(scale);
    }
}
