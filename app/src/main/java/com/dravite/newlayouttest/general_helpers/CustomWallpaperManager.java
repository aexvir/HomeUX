package com.dravite.newlayouttest.general_helpers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.PointF;
import android.os.IBinder;

/**
 * A helper class that holds a wallpaper manager and retrieves it's offsets as it's not possible with the default one.
 */
public class CustomWallpaperManager {

    WallpaperManager mWallpaperManager;

    //Here's where the magic happens
    float xOffset, yOffset;

    public CustomWallpaperManager(Context context){
        mWallpaperManager = WallpaperManager.getInstance(context);
    }

    /**
     * Moves the wallpaper by a given offset.
     * @param token The window token
     * @param xOffset Move this much in the x-direction
     * @param yOffset Move this much in the y-direction
     */
    public void setWallpaperOffsets(IBinder token, float xOffset, float yOffset){
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        if(token!=null && token.isBinderAlive()){
            mWallpaperManager.setWallpaperOffsets(token, xOffset, yOffset);
        }
    }

    //Getter
    public PointF getWallpaperOffsets(){
        return new PointF(xOffset, yOffset);
    }
}
