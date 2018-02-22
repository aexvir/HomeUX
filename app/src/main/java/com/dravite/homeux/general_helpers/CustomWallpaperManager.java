package com.dravite.homeux.general_helpers;

import android.app.WallpaperManager;
import android.content.Context;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.IBinder;
import android.support.v7.graphics.Palette;

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

    public Palette getWallpaperPalette() {
        if (mWallpaperManager.getWallpaperInfo() != null) {
            // We can't extract colors from live wallpapers, so just use the default color always.
            return null;
        } else {
            Drawable mWallpaper = mWallpaperManager.getDrawable();
            return Palette.from(((BitmapDrawable) mWallpaper).getBitmap()).generate();
        }
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
