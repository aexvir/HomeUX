package com.dravite.newlayouttest.settings.items;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dravite.newlayouttest.R;

/**
 * Represents a SettingsItem for generalization in the settings screen's item list.
 */
public abstract class BaseItem<T extends BaseItem.ItemViewHolder> {

    /** A placeholder item without use. Just to be safe. */
    public static final int TYPE_NONE = -1;

    /** A generic item without any extra views inside like switches etc. or styles */
    public static final int TYPE_GENERIC = 0;

    /** Like a generic item, just without a special onClick action and a switch instead */
    public static final int TYPE_SWITCH = 1;

    /** A caption with a totally different style (height, font, no subtitle etc.) */
    public static final int TYPE_CAPTION = 2;

    //Type of the Item
    private int mType;

    //Listener of what should happen when you click this item
    private ItemViewHolder.OnItemClickListener mListener;

    //Is this item a premium option? If so show a badge and disable it when not using the premium key
    private boolean mPremium;

    private String mTag;

    //Is this item enabled aka clickable?
    private boolean mEnabled = true;

    /**
     * A general extension of the default {@link android.support.v7.widget.RecyclerView.ViewHolder} to generalize onClick events, inflations, icons, texts etc.
     */
    public static abstract class ItemViewHolder extends RecyclerView.ViewHolder{
        //LayoutParams for the subTitle when available (necessary for alignment)
        LinearLayout.LayoutParams mParamsDefault = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        //LayoutParams for the subTitle when not available (set height to 0 to center the title)
        LinearLayout.LayoutParams mParamsMinimized = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0);

        boolean mEnabled = true;

        //The views contained by the item
        public TextView mTitle, mDescription;
        public ImageView mIcon;

        /**
         * An extended OnClickListener which also gives the clicked BaseItem, Adapter and item position along with the clicked View.
         */
        public interface OnItemClickListener{
            void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position);
        }

        /**
         * The generalized ViewHolder.
         * @param resID The resource ID of the View to be inflated
         * @param context The current Context
         * @param root The root to inflate into. Mostly the RecyclerView itself.
         */
        public  ItemViewHolder(int resID, Context context, ViewGroup root){
            super(LayoutInflater.from(context).inflate(resID,root, false));

            mTitle = (TextView)itemView.findViewById(R.id.title_text);
            mDescription = (TextView)itemView.findViewById(R.id.desc_text);
            mIcon = (ImageView)itemView.findViewById(R.id.icon);
        }

        /**
         * Sets what should happen when clicking the item. The listener is only triggered, when the item itself is enabled.
         * @param listener The OnItemClickListener
         * @param item The item which has been clicked
         * @param adapter The adapter containing the item
         */
        public void setOnItemClickListener(final OnItemClickListener listener, final BaseItem item, final RecyclerView.Adapter adapter){
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(mEnabled) {
                        listener.onClick(v, item, adapter, getAdapterPosition());
                    }
                }
            });
        }

        /**
         * Sets an icon to the start of this item
         * @param resID the icon resource ID
         */
        public void setIcon(int resID){
            if(mIcon!=null){
                mIcon.setImageResource(resID);
            }
        }

        /**
         * Adds a title and a subtitle to the item. If the description is null or empty, set its height to 0 to center the title.
         * @param title The item title String
         * @param desc The item subtitle String
         */
        public void setTexts(String title, String desc){
            if(mDescription!=null) {
                if (desc == null || desc.equals("")) {
                    mDescription.setLayoutParams(mParamsMinimized);
                } else {
                    mDescription.setLayoutParams(mParamsDefault);
                    mDescription.setText(desc);
                }
            }
            mTitle.setText(title);
        }

        /**
         * Sets if the item is enabled aka clickable, alongside the corresponding alpha and color values.
         * @param enabled Is the item enabled?
         */
        public void setEnabled(boolean enabled){
            mEnabled = enabled;
            mTitle.setAlpha(enabled?0.89f:0.32f);
            if(mDescription!=null) mDescription.setAlpha(enabled?0.57f:0.12f);
            if(mIcon!=null) mIcon.setAlpha(enabled?0.57f:0.12f);
        }

        /*
         * @return true, if the item is enabled, false otherwise
         */
        public boolean isEnabled(){
            return mEnabled;
        }
    }

    /**
     * Initialize the BaseItem rudimentarily.
     * @param type The item type {@link BaseItem#mType}
     * @param premium Is this item a premium-only option?
     * @param tag a Tag used to save the value to the SharedPreferences
     */
    public BaseItem(int type, boolean premium, String tag){
        mType = type;
        mPremium = premium;
        mTag = tag;
    }

    //Getters and setters

    public int getType(){
        return mType;
    }

    public String getTag() {
        return mTag;
    }

    public boolean isPremium(){
        return mPremium;
    }

    public void setEnabled(boolean enabled) {
        this.mEnabled = enabled;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Sets the item's OnItemClickListener
     * @param listener The listener
     */
    public void setAction(ItemViewHolder.OnItemClickListener listener){
        mListener = listener;
    }

    /**
     * @return The item's OnItemClickListener
     */
    public ItemViewHolder.OnItemClickListener getAction(){
        return mListener;
    }

}
