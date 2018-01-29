package com.dravite.newlayouttest.app_editor;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dravite.newlayouttest.iconpacks.IconPackIconAdapter;
import com.dravite.newlayouttest.general_helpers.IconPackManager;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.general_helpers.ExceptionLog;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.TreeSet;

/**
 * An Activity that holds a list of all icons in a selected iconPack in one of two HashMaps.
 */
public class AppEditorIconSelectActivity extends AppCompatActivity {

    //A task that loads all icons into one of the lists
    AsyncTask<Object, Object, Object> mIconLoader;

    //A map holding all icons if they are in int ID form
    public HashMap<String, Integer> mIconListWithInt = new HashMap<>();

    //A map holding all icons if they are in String identifier form
    public HashMap<String, String> mIconListWithString = new HashMap<>();

    //A Tree of all integer IDs
    public TreeSet<Integer> mValueSetInt = new TreeSet<>();

    //A Tree of all String identifiers
    public TreeSet<String> mValueSetString = new TreeSet<>();

    //The selected iconPack
    IconPackManager.IconPack mMainIconPack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_editor_icon_select);

        //Get the icon pack name and try to create it.
        final String iconPack = getIntent().getStringExtra("iconPack");
        try{
            mMainIconPack = new IconPackManager.IconPack(this, iconPack);
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }

        //color the UI
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //List initialization
        final RecyclerView iconList = (RecyclerView)findViewById(R.id.icon_list);
        iconList.setLayoutManager(new GridLayoutManager(this, 4));
        iconList.setAdapter(new IconPackIconAdapter(this, mMainIconPack, mValueSetInt, mValueSetString, new IconPackIconAdapter.OnAppSelectedInterface() {
            @Override
            public void onAppSelected(Bitmap icon) {
                returnIcon(icon);
            }
        }));

        //Init icon search
        final SearchView searchView = (SearchView)findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                TreeSet<String> stringSet = new TreeSet<>();
                TreeSet<Integer> intSet = new TreeSet<>();

                for (Integer item : mValueSetInt) {
                    if (item != 0x0 && mMainIconPack.mPackRes.getResourceEntryName(item).contains(searchView.getQuery())) {
                        intSet.add(item);
                    }
                }
                for (String item : mValueSetString) {
                    if (item.contains(searchView.getQuery())) {
                        stringSet.add(item);
                    }
                }

                iconList.setAdapter(new IconPackIconAdapter(AppEditorIconSelectActivity.this, mMainIconPack, intSet, stringSet, new IconPackIconAdapter.OnAppSelectedInterface() {
                    @Override
                    public void onAppSelected(Bitmap icon) {
                        returnIcon(icon);
                    }
                }));
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        //What happens when closing the search query
        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                iconList.setAdapter(new IconPackIconAdapter(AppEditorIconSelectActivity.this, mMainIconPack, mValueSetInt, mValueSetString, new IconPackIconAdapter.OnAppSelectedInterface() {
                    @Override
                    public void onAppSelected(Bitmap icon) {
                        Picasso.with(AppEditorIconSelectActivity.this).shutdown();
                        returnIcon(icon);
                    }
                }));
                return true;
            }
        });

        //Loads all icons into the trees
        mIconLoader = new AsyncTask<Object, Object, Object>() {

            ProgressBar mProgressBar;
            TextView mProgressText;

            @Override
            protected void onPreExecute() {
                mProgressBar = (ProgressBar)findViewById(R.id.progress);
                mProgressText = (TextView)findViewById(R.id.loadingText);
                mProgressBar.setIndeterminateTintList(ColorStateList.valueOf(ContextCompat.getColor(AppEditorIconSelectActivity.this, R.color.colorAccent)));
                mProgressBar.setVisibility(View.VISIBLE);
                mProgressText.setVisibility(View.VISIBLE);
                iconList.setEnabled(false);
            }

            @Override
            protected Object doInBackground(Object[] params) {
                try{
                    mMainIconPack.loadAll(null);
                    mIconListWithInt = mMainIconPack.mIconMap;
                    mIconListWithString = mMainIconPack.mIconMapStrings;
                    mValueSetInt = new TreeSet<>(mIconListWithInt.values());
                    mValueSetString = new TreeSet<>(mIconListWithString.values());
                } catch (Exception e){
                    ExceptionLog.e(e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object o) {
                mProgressBar.setVisibility(View.GONE);
                mProgressText.setVisibility(View.GONE);
                iconList.setEnabled(true);
                iconList.setAdapter(new IconPackIconAdapter(AppEditorIconSelectActivity.this, mMainIconPack, mValueSetInt, mValueSetString, new IconPackIconAdapter.OnAppSelectedInterface() {
                    @Override
                    public void onAppSelected(Bitmap icon) {
                        returnIcon(icon);
                    }
                }));
            }
        };
        mIconLoader.execute();
    }

    @Override
    protected void onPause() {
        //Cancel loader to not run in the background when pausing
        mIconLoader.cancel(true);
        super.onPause();
    }

    /**
     * Return an icon to the previous activity when selected
     * @param icon The icon Bitmap to be returned
     */
    public void returnIcon(Bitmap icon){
        Intent data = new Intent();
        data.putExtra("icon", icon);
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, data);
        }
        finish();
    }

}
