package com.dravite.homeux.settings;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dravite.homeux.R;
import com.dravite.homeux.settings.items.BaseItem;
import com.dravite.homeux.settings.settings_fragments.SettingsFragmentAdapter;
import com.dravite.homeux.settings.settings_fragments.SettingsListFragment;
import com.dravite.homeux.views.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * An abstract base for {@link SettingsActivity} to be cleared up a little. Provides some helping methods.
 */
public abstract class SettingsBaseActivity extends AppCompatActivity {

    public static int PRIMARY_COLOR = 0xff4CAF50;
    public static int ACCENT_COLOR = 0xff3F51B5;

    /**
     * An interface to ease the addition of items to the fragments.
     */
    public interface AddItems{
        void create(List<BaseItem> itemList);
    }

    public SettingsFragmentAdapter mSettingsFragmentAdapter;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("primary", PRIMARY_COLOR);
        outState.putInt("accent", ACCENT_COLOR);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        ViewPager mPager;
        mPager = (ViewPager) findViewById(R.id.settings_pages);
        final SlidingTabLayout mTabLayout = (SlidingTabLayout) findViewById(R.id.tabs);

        mSettingsFragmentAdapter = new SettingsFragmentAdapter(getSupportFragmentManager());

        mPager.setAdapter(mSettingsFragmentAdapter);
        mPager.setOffscreenPageLimit(8);
        initiateFragments(mSettingsFragmentAdapter);

        mTabLayout.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                for (int i = 0; i < ((ViewGroup)mTabLayout.getSlidingTabStrip()).getChildCount(); i++) {
                    TextView text = (TextView) ((ViewGroup)mTabLayout.getSlidingTabStrip()).getChildAt(i);
                    if (i == position) {
                        text.setTextColor(Color.WHITE);
                    } else {
                        text.setTextColor(0x88ffffff);
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mTabLayout.setDistributeEvenly(true);
        mTabLayout.setViewPager(mPager);
        for (int i=0; i<((ViewGroup)mTabLayout.getSlidingTabStrip()).getChildCount(); i++){
            TextView text = (TextView)((ViewGroup)mTabLayout.getSlidingTabStrip()).getChildAt(i);
            if(i==0){
                text.setTextColor(Color.WHITE);
            } else {
                text.setTextColor(0x88ffffff);
            }
        }
        mTabLayout.setSelectedIndicatorColors(Color.WHITE);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ((TextView)toolbar.findViewById(R.id.title)).setTextColor(Color.WHITE);
        mTabLayout.setBackgroundColor(((ColorDrawable)toolbar.getBackground()).getColor());

        int page = getIntent().getIntExtra("page", 0);
        mPager.setCurrentItem(page);
    }

    /**
     * An abstract method for the {@link SettingsActivity} to add fragments to the adapter.
     * @param adapter The adapter to add fragments to
     */
    public abstract void initiateFragments(SettingsFragmentAdapter adapter);

    /**
     * Adds a page to the FragmentManager including its items.
     * @param caption The caption of this pages tab
     * @param addItems The interface for adding items.
     */
    public void putPage(String caption, AddItems addItems){
        List<BaseItem> items = new ArrayList<>();
        addItems.create(items);
        mSettingsFragmentAdapter.addFragment(SettingsListFragment.create(caption, items));
    }
}
