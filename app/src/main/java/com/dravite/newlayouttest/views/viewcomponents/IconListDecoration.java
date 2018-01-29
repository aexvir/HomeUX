package com.dravite.newlayouttest.views.viewcomponents;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by johannesbraun on 03.02.16.
 * An ItemDecoration for the icon list shown when adding/editing a QuickApp or editing a folder icon.
 */
public class IconListDecoration extends RecyclerView.ItemDecoration {

    int mSpace;

    public IconListDecoration(int space){
        mSpace = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
            RecyclerView parent, RecyclerView.State state) {
        outRect.left = mSpace;
        outRect.right = mSpace;
        outRect.bottom = mSpace;
        outRect.top = mSpace;
    }

}
