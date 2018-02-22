package com.dravite.homeux.general_adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dravite.homeux.top_fragments.ClockFragment;
import com.dravite.homeux.top_fragments.FolderListFragment;
import com.dravite.homeux.top_fragments.QuickSettingsFragment;

/**
 * An adapter that adds the 3 fragmets (QuickSettings, Clock and Folders) to the top panel.
 */
public class AppBarPagerAdapter extends FragmentStatePagerAdapter {

    public AppBarPagerAdapter(FragmentManager manager){
        super(manager);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){
            case 0:
                return new QuickSettingsFragment();
            case 1:
                return new ClockFragment();
            case 2:
                return new FolderListFragment();
        }
        return new ClockFragment();
    }

    @Override
    public int getCount() {
        return 3;
    }
}
