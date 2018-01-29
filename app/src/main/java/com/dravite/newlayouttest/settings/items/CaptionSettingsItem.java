package com.dravite.newlayouttest.settings.items;

import android.content.Context;
import android.view.ViewGroup;

import com.dravite.newlayouttest.R;

/**
 * A {@link BaseItem} of type {@link BaseItem#TYPE_CAPTION}.
 */
public class CaptionSettingsItem extends BaseItem<CaptionSettingsItem.CaptionItemHolder> {

    String mTitle;

    public static class CaptionItemHolder extends BaseItem.ItemViewHolder{
        public CaptionItemHolder(Context context, ViewGroup root){
            super(R.layout.setting_caption, context, root);
        }

        @Override
        public void setEnabled(boolean enabled) {
            //Nothing to enable
        }
    }

    public CaptionSettingsItem(String title){
        super(TYPE_CAPTION, false, "");
        mTitle = title;
    }

    public String getTitle(){
        return mTitle;
    }
}
