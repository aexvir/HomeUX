package com.dravite.homeux.drawerobjects.structures;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;

import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.views.IndicatorView;
import com.dravite.homeux.general_helpers.PageTransitionManager;
import com.dravite.homeux.R;
import com.dravite.homeux.views.FolderNameLabel;

/**
 * Created by Johannes on 11.09.2015.
 * A Fragment in the vertically scrolling Folder ViewPager.
 * Contains a horizontally scrolling AppDrawer ViewPager
 */
public class FolderDrawerPageFragment extends Fragment {

    public ViewPager mPager; //The "AppDrawer"
    int pos;

    /**
     * Creates a new instance of this Fragment while passing its position to it.
     * @param pos The Folder index of this Fragment
     * @return a new FolderDrawerPageFragment instance.
     */
    public static FolderDrawerPageFragment newInstance(int pos){
        FolderDrawerPageFragment fragment = new FolderDrawerPageFragment();
        Bundle bundle = new Bundle();
        bundle.putInt("pos", pos);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pos = getArguments().getInt("pos", 0);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.folder_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LauncherActivity activity = (LauncherActivity)getActivity();

        if(mPager==null || mPager.getAdapter()==null) {

            //Initialize AppDrawer ViewPager
            mPager = (ViewPager) view.findViewById(R.id.folder_pager);

            mPager.setPageTransformer(false, PageTransitionManager.getTransformer(activity.mHolder.pagerTransition));
            mPager.setOffscreenPageLimit(100);

            final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
            mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

                float oldxOffset = 0;

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (positionOffset == 0) {
                        ((AppDrawerPagerAdapter) mPager.getAdapter()).mCanDragItems = true;
                    } else {
                        ((AppDrawerPagerAdapter) mPager.getAdapter()).mCanDragItems = false;
                    }

                    if (!preferences.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL, Const.Defaults.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL))) {

                        int cnt = mPager.getAdapter().getCount();
                        float allOffset = position + positionOffset;
                        float xOffset = Math.max(0, Math.min(allOffset / (float) cnt, 1));

                        oldxOffset = xOffset;

                        activity.mWallpaperManager.setWallpaperOffsets(mPager.getWindowToken(), xOffset, activity.mWallpaperManager.getWallpaperOffsets().y);
                    }
                }

                @Override
                public void onPageSelected(int position) {
                    activity.mIndicator.animate().scaleX(1f / mPager.getAdapter().getCount());
                    activity.mIndicator.animate().translationX(position * (activity.mAppBarLayout.getMeasuredWidth() / mPager.getAdapter().getCount()));
                    ((AppDrawerPagerAdapter) mPager.getAdapter()).mCanDragItems = true;

                }

                @Override
                public void onPageScrollStateChanged(int state) {
                    if (state != AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        ((AppDrawerPagerAdapter) mPager.getAdapter()).mCanDragItems = true;
                    }
                }
            });

            mPager.setAdapter(new AppDrawerPagerAdapter(getActivity(), getChildFragmentManager(), mPager, pos));

            final IndicatorView indicator = (IndicatorView)view.findViewById(R.id.indicatorView);
            indicator.setPager(mPager);
            indicator.setCurrentSelectedInstant(0);

            ((AppDrawerPagerAdapter)mPager.getAdapter()).setUpdateListener(new AppDrawerPagerAdapter.AdapterUpdateListener() {
                @Override
                public void onUpdate() {
                    indicator.update();
                }
            });
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();

        if(mPager!=null)
            mPager.setPageTransformer(false, PageTransitionManager.getTransformer(((LauncherActivity)getActivity()).mHolder.pagerTransition));

        if(view!=null){
            FolderNameLabel name = (FolderNameLabel) view.findViewById(R.id.name);
            name.assignFolder(LauncherActivity.mFolderStructure.folders.get(pos));
        }

    }

    /**
     * @return the current instance of the currently selected ViewPager page.
     */
    public AppDrawerPageFragment getCurrentPagerCard(){
        return getPagerCard(mPager.getCurrentItem());
    }

    /**
     * @return the current instance of the ViewPager page at the given position.
     */
    public AppDrawerPageFragment getPagerCard(int pos){
        if(mPager==null) return null;
        return (AppDrawerPageFragment)mPager.getAdapter().instantiateItem(mPager, pos);
    }
}
