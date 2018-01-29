package com.dravite.homeux.drawerobjects.structures;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.animation.DecelerateInterpolator;

import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.general_helpers.JsonHelper;

/**
 * An adapter holding the app drawer sub-pages.
 */
public class AppDrawerPagerAdapter extends FragmentStatePagerAdapter {

    /**
     * An interface to use for listening for an adapter update (at notifyDatasetChanged()).
     */
    public interface AdapterUpdateListener{
        void onUpdate();
    }

    private Context mContext;

    //Determines if items are draggable
    public boolean mCanDragItems;

    FragmentManager mFragmentManager;
    ViewPager mPager;
    private AdapterUpdateListener mUpdateListener;

    //Index of the folder containing this Adapter
    int folderPos;

    //Flag which is set to true before fully updating this adapter.
    boolean doUpdate = false;

    public AppDrawerPagerAdapter(Context context, FragmentManager manager, ViewPager pager, int folderPos){
        super(manager);
        mFragmentManager = manager;
        mCanDragItems = true;
        mContext = context;
        mPager= pager;
        this.folderPos = folderPos;
    }

    /**
     * Dynamically adds an empty page to this adapter.
     */
    public void addPage(){
        LauncherActivity.mFolderStructure.folders.get(folderPos).add(new FolderStructure.Page());
        notifyDataSetChanged();
    }

    /**
     * Removes all pages that don't contain any elements
     */
    public void removeEmptyPages(){
        boolean animatedRemoval = false;
        for (int i = getCount()-1; i >= 0; i--) {
            int size = LauncherActivity.mFolderStructure.folders.get(folderPos).pages.get(i).items.size();
            if(size==0 && LauncherActivity.mFolderStructure.folders.get(folderPos).pages.size()>1){
                ((AppDrawerPageFragment) instantiateItem(mPager, i)).setRemovalTag(-1);
                LauncherActivity.mFolderStructure.folders.get(folderPos).pages.remove(i);
                animatedRemoval = animatedRemoval | mPager.getCurrentItem()==i;
            }
        }
        //Save FolderStructure
        JsonHelper.saveFolderStructure(mContext, LauncherActivity.mFolderStructure);
        if(animatedRemoval){
            mPager.setScaleX(0);
            mPager.setScaleY(0);
            mPager.animate().scaleX(1).scaleY(1).setInterpolator(new DecelerateInterpolator()).setDuration(150).withStartAction(new Runnable() {
                @Override
                public void run() {
                    notifyDataSetChanged();
                }
            });
        } else {
            notifyDataSetChanged();
        }
    }

    /**
     * Removes the page at the given index.
     * @param index Index of page to be deleted.
     */
    public void removePage(int index){
        ((AppDrawerPageFragment) instantiateItem(mPager, index)).setRemovalTag(-1);

        LauncherActivity.mFolderStructure.removePageAssignments(LauncherActivity.mFolderStructure.folders.get(folderPos).pages.get(index), LauncherActivity.mFolderStructure.folders.get(folderPos).folderName);

        if(LauncherActivity.mFolderStructure.folders.get(folderPos).pages.size()>1)
            LauncherActivity.mFolderStructure.folders.get(folderPos).pages.remove(index);
        else
            LauncherActivity.mFolderStructure.folders.get(folderPos).pages.get(index).items.clear();
        //Save FolderStructure
        JsonHelper.saveFolderStructure(mContext, LauncherActivity.mFolderStructure);

        ((LauncherActivity)mContext).refreshAllFolder(((LauncherActivity)mContext).mHolder.gridHeight, ((LauncherActivity)mContext).mHolder.gridWidth);

        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int position) {
        return AppDrawerPageFragment.newInstance(folderPos, position);
    }

    @Override
    public int getCount() {
        return LauncherActivity.mFolderStructure.folders.get(folderPos).pages.size();
    }

    //Setter
    public void setUpdateListener(AdapterUpdateListener listener){
        mUpdateListener = listener;
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        if(mUpdateListener!=null)
            mUpdateListener.onUpdate();
    }

    /**
     * Goes through all pages of this adapter and refreshes their appGrid
     */
    public void update(){
        doUpdate = true;

        try{
            notifyDataSetChanged();
        } catch (IllegalStateException e){
            e.printStackTrace();
        }
        doUpdate = false;
    }

    @Override
    public int getItemPosition(Object object) {
        if(((AppDrawerPageFragment) object).getRemovalTag()==-1 || doUpdate)
            return POSITION_NONE;
        return super.getItemPosition(object);
    }
}
