package com.dravite.newlayouttest.general_helpers;

import android.content.ComponentName;

/**
 * A structure holding some SharedPreference values to prevent lookups every time.
 */
public class PreferenceHolder {
    public boolean showCard = true;
    public int pagerTransition = 4;
    public int gridHeight = 5;
    public int gridWidth = 4;
    public boolean isFirstStart = true;
    public ComponentName fabComponent = null;
    public boolean useDirectReveal = true;

    public int gridSize(){
        return gridHeight*gridWidth;
    }
}