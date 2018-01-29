package com.dravite.newlayouttest.drawerobjects;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.LauncherLog;
import com.dravite.newlayouttest.views.AppIconView;
import com.dravite.newlayouttest.Const;
import com.dravite.newlayouttest.views.CustomGridLayout;
import com.dravite.newlayouttest.general_helpers.FileManager;
import com.dravite.newlayouttest.general_helpers.IconPackManager;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.drawerobjects.structures.DrawerTree;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.io.Serializable;

/**
 * Represents an application icon on a homescreen.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Application extends DrawerObject implements Serializable {

    private static final String TAG = "Application";

    public transient LauncherActivityInfo info;
    public transient Bitmap icon;
    public String packageName;
    public String className;
    public String label;

    //The intent to be started when tapping on the app icon.
    public transient Intent appIntent;

    //Default constructor
    private Application(){}

    /**
     * Constructs an application hy its LauncherActivityInfo.
     * @param info The LauncherActivityInfo of the app to be created.
     */
    public Application(LauncherActivityInfo info){
        //Just initialize the position to a default.
        super(new GridPositioning(Integer.MIN_VALUE, Integer.MIN_VALUE, 1, 1));
        this.info = info;
        this.packageName = info.getComponentName().getPackageName();
        this.className = info.getComponentName().getClassName();
        this.label = info.getLabel().toString();
        this.appIntent = new Intent();
        appIntent.setComponent(info.getComponentName());
    }

    //Some helper methods following

    /**
     * @return The path to this apps cached folder (contains custom icon and custom label, both only if set)
     */
    @JsonIgnore
    private String getAppPathExt(){
        return FileManager.PATH_APP_CACHE + info.getComponentName().getPackageName();
    }
    @JsonIgnore
    private String getLabelFileName(){
        return info.getComponentName().getClassName() + "_l";
    }
    @JsonIgnore
    private String getCustomIconFileName(){
        return info.getComponentName().getClassName() + "_t";
    }

    /**
     * Either loads the label from cache when there is a custom label or returns the currently set default label.
     * @param context The current Context
     * @return the label for this application
     */
    @JsonIgnore
    public String loadLabel(Context context){
        if(FileManager.fileExists(context, getAppPathExt(), getLabelFileName())) {
            return FileManager.readTextFile(context, getAppPathExt(), getLabelFileName());
        }
        else return label;
    }

    /**
     * Saves a custom label for this application. Set newLabel to null to remove the custom label.
     * @param context the current Context
     * @param newLabel a new label for the app or null
     */
    public void saveLabel(Context context, @Nullable String newLabel){
        if(newLabel==null){
            File f = getCacheFile(context, getAppPathExt(), getLabelFileName());
            FileManager.deleteRecursive(f);
        } else {
            FileManager.saveTextFile(context, getAppPathExt(), getLabelFileName(), newLabel);
        }
    }

    /**
     * Loads an icon Bitmap either from memory (if there is a custom icon), or from the current icon pack (if not null and if it has been loaded), or from the given LauncherActivityInfo if both other methods are not suitable.
     * @param context The current Context
     * @param info The LauncherActivityInfo for this application
     * @return the application icon Bitmap
     */
    public Bitmap loadIcon(Context context, LauncherActivityInfo info){

        if (hasCustomIcon(context)) {
            return loadCustomIcon(context);
        } else{
            if(((LauncherActivity) context).mCurrentIconPack!=null && ((LauncherActivity) context).mCurrentIconPack.isLoaded){
                return ((LauncherActivity) context).mCurrentIconPack.getIconBitmap(info.getComponentName().toString(), LauncherUtils.drawableToBitmap(info.getIcon(0)));
            } else {
                return LauncherUtils.drawableToBitmap(info.getIcon(0));
            }
        }
    }

    /**
     * Saves a custom icon to this applications cache. Set icon to null to delete the custom icon.
     * @param context The current Context.
     * @param icon The icon to be saved.
     */
    public void saveCustomIcon(Context context, @Nullable Bitmap icon){
        if(icon!=null){
            FileManager.saveBitmap(context, icon, getAppPathExt(), getCustomIconFileName());
        } else {
            File f = getCacheFile(context, getAppPathExt(), getCustomIconFileName());
            FileManager.deleteRecursive(f);
        }
    }

    /**
     * Loads this applications custom icon if it exists.
     * @param context The current Context.
     * @return The custom icon or null if not existent-
     */
    public Bitmap loadCustomIcon(Context context){
        if(hasCustomIcon(context)){
            return FileManager.loadBitmap(context, getAppPathExt(), getCustomIconFileName());
        }
        return null;
    }

    /**
     * Loads this applications icon from a given iconPack or LauncherActivityInfo, depending on whether iconPack is null or not. As a difference to loadIcon(), this method does not count in the custom icon.
     * @param iconPack The icon pack to be applied.
     * @param info The LauncherActivityInfo from which the icon is loaded when iconPack is null or not yet loaded.
     * @return The themed icon for an iconPack.
     */
    public Bitmap loadThemedIcon(@Nullable IconPackManager.IconPack iconPack, LauncherActivityInfo info){
        if(iconPack!=null && iconPack.isLoaded){
            return  iconPack.getIconBitmap(info.getComponentName().toString(), LauncherUtils.drawableToBitmap(info.getIcon(0)));
        } else {
            return LauncherUtils.drawableToBitmap(info.getIcon(0));
        }

    }

    /**
     * Checks if this application has a custom icon.
     * @param context The current Context
     * @return Whether the app has a custom icon or not
     */
    private boolean hasCustomIcon(Context context){
        return FileManager.fileExists(context, getAppPathExt(), getCustomIconFileName());
    }

    /**
     * Creates an AppIconView of this application with the corresponding non-themed icon and non-custom label.
     * @param context The current Context
     * @return the created View
     */
    public View createDefaultView(final Context context){
        //When this apps LauncherActivityInfo is null, load it by resolving it with its packageName and className.
        if(info==null){
            LauncherApps apps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
            Intent sIntent = new Intent();
            sIntent.setComponent(new ComponentName(packageName, className));
            info = apps.resolveActivity(sIntent, android.os.Process.myUserHandle());
        }

        final AppIconView v = (AppIconView)LayoutInflater.from(context).inflate(R.layout.icon, null);
        v.setText(loadLabel(context));
        if(appIntent == null){
            appIntent = new Intent();
            appIntent.setComponent(new ComponentName(packageName, className));
        }
        v.setTag(appIntent);
        v.setTag(R.id.TAG_ID_ISAPP, true);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((LauncherActivity)context).mAppClickListener.onClick(v);

                ((LauncherActivity)context).hideSearchMode();
            }
        });
        //Set Icon by letting it load with its label and component.
        //Then specify what should happen afterwards.
        LauncherActivity.mDrawerTree.doWithApplication(info, new DrawerTree.LoadedListener() {
            @Override
            public void onApplicationLoadingFinished(Application app) {
                if(app==null)
                    //remove placeholder icon when the returned app is null.
                    v.setIcon(null);
                else{
                    //Convert the Bitmap icon to a Drawable and set it.
                    Drawable drawable = new BitmapDrawable(context.getResources(), app.icon);
                    v.setIcon(drawable);
                }
            }
        });
        return v;
    }

    /**
     * Create this applications AppIconView for it to fit into the grid.
     * @param parent The Grid layout to be inflated into.
     * @param inflater a LayoutInflater
     * @param listener The listener which should be called after finishing creating the View
     */
    @Override
    public void createView(final CustomGridLayout parent, LayoutInflater inflater, OnViewCreatedListener listener) {
        //When this apps LauncherActivityInfo or Intent is null, load it by resolving it with its packageName and className.
        if(info==null || appIntent==null){
            LauncherApps apps = (LauncherApps)parent.getContext().getSystemService(Context.LAUNCHER_APPS_SERVICE);
            appIntent = new Intent();
            appIntent.setComponent(new ComponentName(packageName, className));
            info = apps.resolveActivity(appIntent, android.os.Process.myUserHandle());
        }
        //When info is still null, then it could not load, so don't proceed creating the View.
        if(info==null){
            LauncherLog.w(TAG, "Application's info is null: " + (label==null?"n/a":label));
            return;
        }

        //Create the AppIconView using the parent grid LayoutInflater
        final AppIconView mView = parent.inflateIcon();
        mView.setText(parent.mPreferences.getBoolean(Const.Defaults.TAG_SHOW_LABELS, Const.Defaults.getBoolean(Const.Defaults.TAG_SHOW_LABELS))?loadLabel(parent.getContext()):"");
        mView.setTag(appIntent);
        mView.setTag(R.id.TAG_ID_ISAPP, true);
        mView.setOnLongClickListener(parent);
        mView.setOnClickListener(parent.getMainActivity().mAppClickListener);

        //Set Icon by letting it load with its label and component.
        //Then specify what should happen afterwards.
        LauncherActivity.mDrawerTree.doWithApplicatioByLabel(loadLabel(parent.getMainActivity()), info, new DrawerTree.LoadedListener() {
            @Override
            public void onApplicationLoadingFinished(Application app) {
                if(app!=null){
                    Drawable drawable = new BitmapDrawable(parent.getResources(), app.icon);
                    mView.setIcon(drawable);

                }
            }
        });

       /* LauncherActivity.mIconLoaderPool.enqueue(new Runnable() {
            @Override
            public void run() {
                final Drawable icon = new BitmapDrawable(parent.getResources(), loadIcon(parent.getContext(), info));
                parent.getMainActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mView.setIcon(icon);
                    }
                });
            }
        });*/

        //Just let the listener run without the icon to be created to let it time to load while maintaining a decent loading time.
        listener.onViewCreated(mView);
    }




    //Some more overriden Methods and constructors.

    @Override
    public int getObjectType() {
        return TYPE_APP;
    }

    @Override
    public boolean equalType(DrawerObject object) {
        return (object instanceof Application) && label.equals(((Application) object).label)
                && (appIntent==null?((Application) object).appIntent==null:appIntent.equals(((Application) object).appIntent))
                && (icon==null?((Application) object).icon==null:icon.equals(((Application) object).icon))
                && (info==null?((Application) object).info==null:info.equals(((Application) object).info));
    }

    @Override
    public DrawerObject copy() {
        Application newApplication = new Application();
        newApplication.mGridPosition = mGridPosition.copy();
        newApplication.icon = icon;
        newApplication.info = info;
        newApplication.packageName = packageName;
        newApplication.className = className;
        newApplication.label = label;
        newApplication.appIntent = appIntent;
        return newApplication;
    }

    private Application(Parcel in){
        super(in);
        icon = in.readParcelable(Bitmap.class.getClassLoader());
        appIntent = in.readParcelable(Intent.class.getClassLoader());
        label = in.readString();
        packageName = in.readString();
        className = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeParcelable(icon, 0);
        dest.writeParcelable(appIntent, 0);
        dest.writeString(label);
        dest.writeString(packageName);
        dest.writeString(className);
    }

    @Override
    public int describeContents() {return 0;}

    public static final Parcelable.Creator<Application> CREATOR
            = new Parcelable.Creator<Application>() {
        public Application createFromParcel(Parcel in) {
            return new Application(in);
        }

        public Application[] newArray(int size) {
            return new Application[size];
        }
    };
}
