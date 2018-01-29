package com.dravite.homeux.views.viewcomponents;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by johannesbraun on 24.11.15.
 * This {@link android.support.v7.widget.RecyclerView.ItemDecoration} adds some space around items in the following pattern:<br/>
 * <b>left and right:</b> 1x space<br/>
 * <b>top:</b> no space<br/>
 * <b>bottom:</b> 2x space
 */

public class SpacesItemDecoration extends RecyclerView.ItemDecoration {
    private int space;

    public SpacesItemDecoration(int space) {
        this.space = space;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        outRect.left = space;
        outRect.right = space;
        outRect.bottom = 2*space;
    }
}

