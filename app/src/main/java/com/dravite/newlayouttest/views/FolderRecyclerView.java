package com.dravite.newlayouttest.views;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by johannesbraun on 08.02.16.
 * An extension to the default {@link RecyclerView}, which includes the {@link #findChildViewUnder(float, float)} method.
 */
public class FolderRecyclerView extends RecyclerView {

    public FolderRecyclerView(Context context) {
        this(context, null);
    }

    public FolderRecyclerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FolderRecyclerView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @param x The requested X value
     * @param y The requested Y value
     * @return The child View thats under the given position or null if there is none.
     */
    public View findChildViewUnder(float x, float y) {
        final int count = getChildCount();
        for (int i = count - 1; i >= 0; i--) {
            final View child = getChildAt(i);
            if (x >= child.getLeft() && x <= child.getRight() &&
                    y >= child.getTop() &&
                    y <= child.getBottom()) {
                return child;
            }
        }
        return null;
    }

}
