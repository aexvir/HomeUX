package com.dravite.homeux.settings.settings_fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * An adapter holding all the {@link SettingsListFragment SettingsListFragments} for a ViewPager.
 */
public class SettingsFragmentAdapter extends FragmentPagerAdapter {

    //A list of SettingsListFragments
    List<SettingsListFragment> fragments = new ArrayList<>();

    public SettingsFragmentAdapter(FragmentManager fm) {
        super(fm);
    }

    /**
     * Adds a fragment to this adapter and refreshes it.
     * @param fragment The {@link SettingsListFragment} to add.
     */
    public void addFragment(SettingsListFragment fragment) {
        fragments.add(fragment);
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments.get(position).getCaption();
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}
