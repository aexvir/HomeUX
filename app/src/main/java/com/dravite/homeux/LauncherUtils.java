package com.dravite.homeux;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.TypedValue;
import android.view.View;

import com.dravite.homeux.views.AppIconView;

import java.util.List;

/**
 * Created by Johannes on 13.09.2015.
 * Some useful methods
 */
public class LauncherUtils {
    /**
     * Checks if an intent can be started
     */
    public static boolean isAvailable(Context ctx, Intent intent) {
        final PackageManager mgr = ctx.getPackageManager();
        List<ResolveInfo> list =  mgr.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    /**
     * Checks if a package is installed
     */
    public static boolean isPackageInstalled(String packagename, Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(packagename, PackageManager.GET_ACTIVITIES);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    /**
     * Checks if a point is inside a given View.
     */
    public static boolean pointInView(View v, float localX, float localY, float slop) {
        return localX >= -slop && localY >= -slop && localX < (v.getWidth() + slop) &&
                localY < (v.getHeight() + slop);
    }

    /**
     * Sets the view's text color
     */
    public static void colorAppIconView(AppIconView app, Context context){
        if(!((LauncherActivity)context).mHolder.showCard) {
            Resources resources = context.getResources();
            app.setTextColor(0xffffffff);
            float rad = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, resources.getDisplayMetrics());
            float dx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, resources.getDisplayMetrics());
            float dy = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 0.5f, resources.getDisplayMetrics());
            app.setShadowLayer(rad, dx, dy, 0x99000000);
        } else {
            app.setTextColor(0x8a000000);
            float rad = 0;
            float dx = 0;
            float dy = 0;
            app.setShadowLayer(rad, dx, dy, 0x0000000);
        }
    }

    /**
     * Checks if an app can be uninstalled.
     * @param packageName
     * @param context
     * @return
     */
    public static boolean canBeUninstalled(String packageName, Context context){
        try{
            return isUserApp(context.getPackageManager().getApplicationInfo(packageName, 0), true);
        } catch (PackageManager.NameNotFoundException e){
            return false;
        }
    }

    /**
     * Checks if an app is user or system.
     * @param ai
     * @param getUpdatedSystemApps
     * @return
     */
    public static boolean isUserApp(ApplicationInfo ai, boolean getUpdatedSystemApps){
        if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) != 0) {
            if(getUpdatedSystemApps){
                return (ai.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
            }
            return false;
        } else {
            return true;
        }
    }

    /**
     * Converts a drawable into a bitmap
     * @param drawable
     * @return
     */
    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        int width = drawable.getIntrinsicWidth();
        width = width > 0 ? width : 1;
        int height = drawable.getIntrinsicHeight();
        height = height > 0 ? height : 1;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    /**
     * Converts dp to pixels
     * @param dp
     * @param c
     * @return
     */
    public static int dpToPx(float dp, Context c) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    /**
     * Scale a given bitmap
     * @param bm
     * @param newHeight
     * @param newWidth
     * @return
     */
    public static Bitmap getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
        return resizedBitmap;
    }

    public static Matrix getResizedMatrix(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return matrix;
    }

    /**
     * Start App with a nice animation
     * @param context
     * @param view
     * @param intent
     * @param resultCode
     */
    public static void startActivityForResult(Activity context, View view, Intent intent, int resultCode){
        int[] pos = {0, 0};
        if(view!=null)
            view.getLocationInWindow(pos);
        ActivityOptions opts = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if(view!=null)
                opts = ActivityOptions.makeClipRevealAnimation(view, 0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
        } else {
            if(view!=null)
                opts = ActivityOptions.makeScaleUpAnimation(view, 0, 0,
                    view.getMeasuredWidth(), view.getMeasuredHeight());
        }
        try{
            context.startActivityForResult(intent, resultCode, opts == null ? null : opts.toBundle());
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * see {@link #startActivityForResult(Activity, View, Intent, int)}.
     */
    public static void startActivity(Activity context, View view, Intent intent){
        startActivityForResult(context, view, intent, -1);
    }

    /**
     * Creates a launchable intent.
     * @param intentWithComponent
     * @return
     */
    public static Intent makeLaunchIntent(Intent intentWithComponent) {
        return intentWithComponent.setAction(Intent.ACTION_MAIN)
                .addCategory(Intent.CATEGORY_LAUNCHER)
                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
    }

    /**
     * Converts a View into a bitmap
     */
    public static Bitmap loadBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);

        view.buildDrawingCache();

        return view.getDrawingCache();
    }
}
