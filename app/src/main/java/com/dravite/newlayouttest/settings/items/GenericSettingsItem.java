package com.dravite.newlayouttest.settings.items;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dravite.newlayouttest.R;

/**
 * A {@link BaseItem} of type {@link BaseItem#TYPE_GENERIC}.
 */
public class GenericSettingsItem extends BaseItem<GenericSettingsItem.GeneralViewHolder> {

    String mTitle;
    String mDescription;
    int mDrawableRes;

    public static class GeneralViewHolder extends BaseItem.ItemViewHolder{

        //As simple as that.
        public GeneralViewHolder(Context context, ViewGroup root){
            super(R.layout.setting_generic, context, root);
        }

    }

    /**
     * Will open an URI when clicked.
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     * @param uri The URI to open when clicked
     */
    public GenericSettingsItem(String title, String description, String tag, int drawableRes, boolean premium, final Uri uri){
        this(title, description, tag, drawableRes, premium);
        setAction(new ItemViewHolder.OnItemClickListener() {
            @Override
            public void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position) {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(uri);
                v.getContext().startActivity(i);
            }
        });
    }

    /**
     * Will open an intent with a given request code.
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     * @param intent The intent to open when clicked
     * @param requestCode The requestCode to return from the opened intent
     */
    public GenericSettingsItem(String title, String description, String tag, int drawableRes, boolean premium, final Intent intent, final int requestCode){
        this(title, description, tag, drawableRes, premium);
        setAction(new ItemViewHolder.OnItemClickListener() {
            @Override
            public void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position) {
                ((Activity)v.getContext()).startActivityForResult(intent, requestCode);
            }
        });
    }

    /**
     * Will open just an intent without any request code.
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     * @param intent The intent to open
     */
    public GenericSettingsItem(String title, String description, String tag, int drawableRes, boolean premium, final Intent intent){
        this(title, description, tag, drawableRes, premium);
        setAction(new ItemViewHolder.OnItemClickListener() {
            @Override
            public void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position) {
                v.getContext().startActivity(intent);
            }
        });
    }

    /**
     * Will run what's given by a {@link com.dravite.newlayouttest.settings.items.BaseItem.ItemViewHolder.OnItemClickListener}.
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     * @param listener The listener to run
     */
    public GenericSettingsItem(String title, String description, String tag, int drawableRes, boolean premium, ItemViewHolder.OnItemClickListener listener){
        this(title, description, tag, drawableRes, premium);
        setAction(listener);
    }

    /**
     * Will to nothing when clicked (Just a status entry)
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     */
    public GenericSettingsItem(String title, String description, String tag, int drawableRes, boolean premium){
        super(TYPE_GENERIC, premium, tag);
        mTitle = title;
        mDescription = description;
        mDrawableRes = drawableRes;
    }

    //Getters and setters

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getTitle(){
        return mTitle;
    }

    public String getDescription(){
        return mDescription;
    }

    public int getDrawableRes(){
        return mDrawableRes;
    }
}
