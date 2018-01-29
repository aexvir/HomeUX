package com.dravite.homeux.settings.settings_fragments;

import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.settings.items.BaseItem;
import com.dravite.homeux.settings.items.CaptionSettingsItem;
import com.dravite.homeux.settings.items.GenericSettingsItem;
import com.dravite.homeux.settings.items.SwitchSettingsItem;

import java.util.ArrayList;
import java.util.List;

/**
 * The Adapter holding all the {@link BaseItem BaseItems}.
 */
public class SettingsItemAdapter extends RecyclerView.Adapter<BaseItem.ItemViewHolder> {

    //The Fragment which contains this adapter
    SettingsListFragment mFragment;

    //The item list
    private List<BaseItem> mItems = new ArrayList<>();

    public SettingsItemAdapter(SettingsListFragment fragment){
        mFragment = fragment;
    }

    /**
     * Fills up the item list. Does not remove old entries.
     * @param items The item list to append.
     */
    public void addItems(List<BaseItem> items){
        mItems.addAll(items);
    }

    //Overridden Methods

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public BaseItem.ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Create different ViewHolders depending on the viewType
        switch (viewType){
            default:
            case BaseItem.TYPE_GENERIC:
                return new GenericSettingsItem.GeneralViewHolder(mFragment.getContext(), parent);
            case BaseItem.TYPE_SWITCH:
                return new SwitchSettingsItem.SwitchItemHolder(mFragment.getContext(), parent);
            case BaseItem.TYPE_CAPTION:
                return new CaptionSettingsItem.CaptionItemHolder(mFragment.getContext(), parent);
        }
    }

    @Override
    public void onBindViewHolder(BaseItem.ItemViewHolder holder, int position) {
        final BaseItem cItem = mItems.get(position);
        switch (cItem.getType()){
            case BaseItem.TYPE_SWITCH:
                holder.setTexts(((SwitchSettingsItem)cItem).getTitle(), ((SwitchSettingsItem)cItem).getDescription());
                holder.setIcon(((SwitchSettingsItem)cItem).getDrawableRes());
                ((SwitchSettingsItem.SwitchItemHolder)holder).mSwitch.setOnCheckedChangeListener(null);
                ((SwitchSettingsItem.SwitchItemHolder)holder).mSwitch.setChecked(PreferenceManager.getDefaultSharedPreferences(((SwitchSettingsItem.SwitchItemHolder) holder).itemView.getContext()).getBoolean(cItem.getTag(), ((SwitchSettingsItem) cItem).isChecked()));
                ((SwitchSettingsItem.SwitchItemHolder)holder).mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        PreferenceManager.getDefaultSharedPreferences(buttonView.getContext()).edit().putBoolean(cItem.getTag(), isChecked).apply();
                    }
                });
                break;
            case BaseItem.TYPE_GENERIC:
                holder.setTexts(((GenericSettingsItem)cItem).getTitle(), ((GenericSettingsItem)cItem).getDescription());
                holder.setIcon(((GenericSettingsItem)cItem).getDrawableRes());
                break;
            case BaseItem.TYPE_CAPTION:
                holder.setTexts(((CaptionSettingsItem)cItem).getTitle(), null);
                break;

        }

        //Enable or disable the item.
        holder.setEnabled(!cItem.isPremium() && cItem.getAction()!=null && cItem.isEnabled());

        //Set the click action when enabled and the action is not null.
        if(cItem.getAction()!=null && holder.isEnabled()) {
            holder.setOnItemClickListener(cItem.getAction(), cItem, this);
        }

        //Clear the premium overlay and...
        holder.itemView.getOverlay().clear();
        //re-add it only when the assigned item is a premium option AND the app itself is in a non-premium state.
        if(cItem.isPremium()) {
            Drawable premiumOverlay = mFragment.getContext().getDrawable(R.drawable.pro_badge);
            if(premiumOverlay!=null) {
                premiumOverlay.setBounds(0, LauncherUtils.dpToPx(8, mFragment.getContext()), LauncherUtils.dpToPx(56, mFragment.getContext()), LauncherUtils.dpToPx(64, mFragment.getContext()));
                holder.itemView.getOverlay().add(premiumOverlay);
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        return mItems.get(position).getType();
    }


}
