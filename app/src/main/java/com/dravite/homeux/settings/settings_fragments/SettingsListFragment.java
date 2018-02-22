package com.dravite.homeux.settings.settings_fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dravite.homeux.R;
import com.dravite.homeux.settings.items.BaseItem;

import java.util.List;

/**
 * A Fragment containing a RecyclerView showing a list of {@link BaseItem BaseItems}.
 */
public class SettingsListFragment extends Fragment {

    public SettingsItemAdapter adapter;

    //The Caption of this Fragment shown in the tab bar.
    private String mCaption = "";

    public SettingsListFragment() {
        adapter = new SettingsItemAdapter(this);
    }

    /**
     * Creates a new instance of this Fragment having a caption and a list of {@link BaseItem BaseItems}.
     * @param mCaption The caption String
     * @param mOptions A list of BaseItems.
     * @return A new instance of {@link SettingsListFragment}.
     */
    public static SettingsListFragment create(String mCaption, List<BaseItem> mOptions) {
        SettingsListFragment f = new SettingsListFragment();
        f.setCaption(mCaption);
        f.addItems(mOptions);
        return f;
    }

    /**
     * Adds items to the adapter. See also {@link SettingsItemAdapter#addItems(List)}.
     * @param items A list of items to be added.
     */
    public void addItems(List<BaseItem> items) {
        adapter.addItems(items);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //inflate the settings fragment layout.
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Set a list adapter and layoutManager.
        final RecyclerView list = (RecyclerView) view.findViewById(R.id.settingList);
        list.setLayoutManager(new GridLayoutManager(view.getContext(), 1));
        list.setAdapter(adapter);

    }

    //Getters and setters

    public String getCaption() {
        return mCaption;
    }

    public void setCaption(String mCaption) {
        this.mCaption = mCaption;
    }
}
