package com.dravite.homeux.drawerobjects;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import com.dravite.homeux.views.AppIconView;
import com.dravite.homeux.views.CustomGridLayout;
import com.dravite.homeux.general_helpers.FileManager;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.R;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.net.URISyntaxException;

/**
 * Represents a Shortcut on a Homescreen.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Shortcut extends DrawerObject implements Serializable {

    /**
     * Represents a serializable way of saving a Intent.ShortcutIconResource Object.
     */
    public static class SIconResource implements Serializable{
        public String packageName, resourceName;
        public boolean valid;

        /**
         * Creates a new ShortcutIconResource from given package and resource names.
         * @return a new ShortcutIconResource with given package and resource name.
         */
        public Intent.ShortcutIconResource createRes(){
            if(!valid)
                return null;
            Intent.ShortcutIconResource ir = new Intent.ShortcutIconResource();
            ir.packageName = packageName;
            ir.resourceName = resourceName;
            return ir;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof SIconResource &&
                    packageName==null?((SIconResource) o).packageName==null:packageName.equals(((SIconResource) o).packageName)
                    && resourceName==null?((SIconResource) o).resourceName==null:resourceName.equals(((SIconResource) o).resourceName)
                    && valid==((SIconResource) o).valid;
        }

        /**
         * Default Constructor
         */
        public SIconResource(){

        }

        /**
         * The constructor taking a Intent.ShortcutIconResource.
         * @param resource the ShortcutIconResource
         */
        public SIconResource(Intent.ShortcutIconResource resource){
            if (resource==null){
                valid = false;
                packageName = "";
                resourceName = "";
            } else {
                valid = true;
                packageName = resource.packageName;
                resourceName = resource.resourceName;
            }
        }
    }

    public String shortcutIntentUri;

    //The saveable iconResource
    public SIconResource iconResource;

    //The non-saveable icon Bitmap
    @JsonIgnore
    public transient Bitmap icon;
    public String shortcutLabel;

    /**
     * Default constructor
     */
    public Shortcut(){}

    /**
     * Constructor creating a Shortcut from a given Intent
     * @param shortIntent the Shortcut Intent
     * @param context The current Context
     */
    public Shortcut(Intent shortIntent, Context context){
        iconResource    = new SIconResource((Intent.ShortcutIconResource)shortIntent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE));
        icon            = shortIntent.getParcelableExtra(Intent.EXTRA_SHORTCUT_ICON);
        shortcutLabel   = shortIntent.getStringExtra(Intent.EXTRA_SHORTCUT_NAME);
        Intent intent  = shortIntent.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
        shortcutIntentUri = intent.toUri(0);
        FileManager.saveBitmap(context, icon, "/Shortcuts", shortcutIntentUri.replaceAll("/", ""));
    }

    /**
     * Parcelable constructor
     * @param in Parcel
     */
    public Shortcut(Parcel in){
        super(in);
        shortcutIntentUri = in.readString();
        iconResource = (SIconResource)in.readSerializable();
        shortcutLabel = in.readString();
        icon = in.readParcelable(Bitmap.class.getClassLoader());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeString(shortcutIntentUri);
        dest.writeSerializable(iconResource);
        dest.writeString(shortcutLabel);
        dest.writeParcelable(icon, 0);
    }

    @Override
    public int getObjectType() {
        return TYPE_SHORTCUT;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Create this shortcuts AppIconView for it to fit into the grid.
     * @param parent The Grid layout to be inflated into.
     * @param inflater a LayoutInflater
     * @param listener The listener which should be called after finishing creating the View
     */
    @Override
    public void createView(CustomGridLayout parent, LayoutInflater inflater, OnViewCreatedListener listener) {

        Intent intent = null;
        try{
            intent = Intent.parseUri(shortcutIntentUri, 0);
        } catch (URISyntaxException e){
            e.printStackTrace();
            listener.onViewCreated(null);
        }

        //If icon is not yet loaded, just load it.
        if (icon==null){
            Intent.ShortcutIconResource iconRes = iconResource.createRes();
            if (iconRes!=null){
                Resources resources =null;
                try {
                    resources = parent.getContext().getPackageManager().getResourcesForApplication(iconRes.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                if (resources != null) {
                    int id = resources.getIdentifier(iconRes.resourceName, null, iconRes.packageName);
                    icon = LauncherUtils.drawableToBitmap(resources.getDrawable(id, parent.getContext().getTheme()));
                }
                FileManager.saveBitmap(parent.getContext(), icon, "/Shortcuts", shortcutIntentUri.replaceAll("/", ""));
            } else {
                icon = FileManager.loadBitmap(parent.getContext(), "/Shortcuts", shortcutIntentUri.replaceAll("/", ""));
            }
        }

        //If everything is loaded, assign everything needed to the AppIconView
        if (shortcutLabel!=null && intent!=null && icon!=null){
            AppIconView v = (AppIconView)View.inflate(parent.getContext(), R.layout.icon, null);
            Drawable drawable = new BitmapDrawable(parent.getContext().getResources(),icon);
            v.setIcon(drawable);
            v.setText(shortcutLabel);
            v.setTag(intent);
            v.setOnLongClickListener(parent);
            v.setOnClickListener(((LauncherActivity) parent.getContext()).mShortcutClickListener);
            listener.onViewCreated(v);
            return;
        }
        //Otherwise just return a null View
        listener.onViewCreated(null);
    }

    @Override
    public boolean equalType(DrawerObject object) {
        return (object instanceof Shortcut) && (shortcutIntentUri==null?((Shortcut) object).shortcutIntentUri==null:shortcutIntentUri.equals(((Shortcut) object).shortcutIntentUri))
                && (shortcutLabel==null?((Shortcut) object).shortcutLabel==null:shortcutLabel.equals(((Shortcut) object).shortcutLabel))
                && (icon==null?((Shortcut) object).icon==null:icon.equals(((Shortcut) object).icon))
                && (iconResource.equals(((Shortcut) object).iconResource));
    }

    @Override
    public DrawerObject copy() {
        Shortcut shortcut = new Shortcut();
        shortcut.mGridPosition = mGridPosition;
        shortcut.icon = icon;
        shortcut.shortcutIntentUri = shortcutIntentUri;
        shortcut.iconResource = iconResource;
        shortcut.shortcutLabel = shortcutLabel;
        return shortcut;
    }

    public static final Parcelable.Creator<Shortcut> CREATOR
            = new Parcelable.Creator<Shortcut>() {
        public Shortcut createFromParcel(Parcel in) {
            return new Shortcut(in);
        }

        public Shortcut[] newArray(int size) {
            return new Shortcut[size];
        }
    };
}
