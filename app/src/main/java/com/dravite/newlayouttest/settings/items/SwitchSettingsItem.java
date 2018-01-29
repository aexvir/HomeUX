package com.dravite.newlayouttest.settings.items;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;

import com.dravite.newlayouttest.R;

/**
 * A {@link BaseItem} of type {@link BaseItem#TYPE_SWITCH}.
 */
public class SwitchSettingsItem extends BaseItem<SwitchSettingsItem.SwitchItemHolder> {

    String mTitle;
    String mDescription;
    int mDrawableRes;

    //Is the switch checked or not.
    boolean mIsChecked;

    public static class SwitchItemHolder extends BaseItem.ItemViewHolder{
        public SwitchItemHolder(Context context, ViewGroup root){
            super(R.layout.setting_switch, context, root);

            mSwitch = (Switch)itemView.findViewById(R.id.pswitch);
        }

        @Override
        public void setOnItemClickListener(final OnItemClickListener listener, BaseItem item, RecyclerView.Adapter adapter) {
            super.setOnItemClickListener(new OnItemClickListener() {
                @Override
                public void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position) {
                    mSwitch.setChecked(!mSwitch.isChecked());
                    listener.onClick(v, item, adapter, position);
                }
            }, item, adapter);
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            mSwitch.setEnabled(enabled);
        }

        //An additional switch
        public Switch mSwitch;
    }


    /**
     * This constructor sets the default checked state of the switch explicitly. (Additionally to {@link #SwitchSettingsItem(String, String, String, int, boolean, boolean)}.
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     * @param enabled Is this switch item enabled?
     * @param isCheckedByDefault Is this switch checked by default?
     */
    public SwitchSettingsItem(String title, String description, String tag, int drawableRes, boolean premium, boolean enabled, boolean isCheckedByDefault){
        this(title, description, tag, drawableRes, premium, enabled);
        mIsChecked = isCheckedByDefault;
    }

    /**
     * This constructor sets the enabled state of the switch explicitely.
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     * @param enabled Is this switch item enabled?
     */
    public SwitchSettingsItem(String title, String description, String tag, int drawableRes, boolean premium, boolean enabled){
        this(title, description, tag, drawableRes, premium);
        setEnabled(enabled);
    }

    /**
     * The more or less "default" switch item constructor.
     * @param title The item title
     * @param description The item description
     * @param tag The preference tag
     * @param drawableRes The icon resource ID
     * @param premium Is this item premium-only?
     */
    public SwitchSettingsItem(String title, String description, String tag, int drawableRes, boolean premium){
        super(TYPE_SWITCH, premium, tag);
        mTitle = title;
        mDescription = description;
        mDrawableRes = drawableRes;
        setAction(new ItemViewHolder.OnItemClickListener() {
            @Override
            public void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position) {
                //Do nothing except switching the switch, which is done in SwitchItemHolder.setOnItemClickListener above.
            }
        });
    }

    //Getters and setters

    public String getTitle(){
        return mTitle;
    }

    public String getDescription(){
        return mDescription;
    }

    public int getDrawableRes(){
        return mDrawableRes;
    }

    public boolean isChecked(){
        return mIsChecked;
    }
}
