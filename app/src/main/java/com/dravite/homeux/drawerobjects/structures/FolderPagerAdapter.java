package com.dravite.homeux.drawerobjects.structures;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.LauncherLog;

/**
 * Created by Johannes on 11.09.2015.
 * The general adapter for the FolderDrawer
 */
public class FolderPagerAdapter extends FragmentStatePagerAdapter {

    private static final String TAG = "FolderPagerAdapter";
    LauncherActivity mMainActivity;
    private boolean isInNullState;

    public FolderPagerAdapter(LauncherActivity mainActivity, FragmentManager fm){
        super(fm);
        mMainActivity = mainActivity;
    }

    @Override
    public Fragment getItem(int position) {
        return FolderDrawerPageFragment.newInstance(position);
    }

    /**
     * Hides all pages
     */
    public void switchToNullState(){
        isInNullState = true;
        notifyDataSetChanged();
    }

    /**
     * Shows all pages
     */
    public void switchFromNullState(){
        isInNullState = false;
        notifyPagesChanged();
    }

    @Override
    public int getCount() {
        return isInNullState?0: LauncherActivity.mFolderStructure.folders.size();
    }

    //dummy Variable (for getItemPosition)
    boolean doUpdate = false;

    /**
     * Fully updates the whole Drawer Fragment Tree down to the AppDrawers
     */
    public void update(){
        doUpdate = true;
        try{
            notifyDataSetChanged();
        }catch (IllegalStateException e){
            LauncherLog.w("FolderPagerAdapter", e.getMessage());
        }

        for (int i = Math.max(0,mMainActivity.mPager.getCurrentItem()-1); i < Math.min(getCount(), mMainActivity.mPager.getCurrentItem() + 1); i++) {
            FolderDrawerPageFragment frag = ((FolderDrawerPageFragment) instantiateItem(mMainActivity.mPager, i));
            if(frag.mPager!=null){
                ((AppDrawerPagerAdapter) frag.mPager.getAdapter()).update();
            }
        }
        doUpdate = false;
    }

    /**
     * Updates only the Folder Pages (Not necessarily the AppDrawers).
     */
    public void notifyPagesChanged(){
        doUpdate = true;
        try {
            notifyDataSetChanged();
        } catch (IllegalStateException e){
            LauncherLog.d(TAG, "notifyPagesChanged: " + e.toString());
        }
        doUpdate = false;
    }

    @Override
    public int getItemPosition(Object object) {
        return doUpdate?POSITION_NONE:super.getItemPosition(object);
    }
}
