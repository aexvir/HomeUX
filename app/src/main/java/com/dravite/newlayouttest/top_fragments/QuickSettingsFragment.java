package com.dravite.newlayouttest.top_fragments;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dravite.newlayouttest.Const;
import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.iconpacks.ThemeAdapter;
import com.dravite.newlayouttest.drawerobjects.structures.FolderPagerAdapter;
import com.dravite.newlayouttest.general_helpers.IconPackManager;
import com.dravite.newlayouttest.general_helpers.UpdateListener;
import com.dravite.newlayouttest.settings.SettingsActivity;
import com.dravite.newlayouttest.views.QuickSettingsButton;

import java.util.List;

/**
 * Created by Johannes on 16.09.2015.
 * This is the left Fragment of the Top Panel, containing the 3 buttons for Wallpaper, Icon Pack and Settings
 */
public class QuickSettingsFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final LauncherActivity mainActivity = (LauncherActivity)getActivity();

        QuickSettingsButton btSettings = (QuickSettingsButton) view.findViewById(R.id.bt_settings);
        QuickSettingsButton btIconPack = (QuickSettingsButton) view.findViewById(R.id.bt_icon_pack);
        QuickSettingsButton btWallpaper = (QuickSettingsButton) view.findViewById(R.id.bt_wallpaper);

        //Settings
        btSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LauncherActivity.updateAfterSettings = false;
                Intent intent = new Intent(mainActivity, SettingsActivity.class);

                LauncherUtils.startActivityForResult(mainActivity, v, intent, SettingsActivity.REQUEST_SETTINGS);
            }
        });

        //Icon Pack
        btIconPack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
                final List<IconPackManager.Theme> themes = IconPackManager.getAllThemes(getActivity(), true);
                builder.setTitle(R.string.menu_action_icon_pack)
                .setAdapter(new ThemeAdapter(getActivity(), themes), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, final int which) {

                        //Loading task for the icon pack.
                        new AsyncTask<Void, Integer, Void>() {
                            long time;
                            ProgressDialog mProgressDialog;

                            @Override
                            protected void onPreExecute() {
                                try {
                                    mainActivity.mCurrentIconPack = new IconPackManager.IconPack(getActivity(), themes.get(which).packageName);
                                } catch (PackageManager.NameNotFoundException e) {
                                    e.printStackTrace();
                                }
                                mProgressDialog = new ProgressDialog(getActivity(), R.style.DialogTheme);
                                mProgressDialog.setIndeterminate(false);
                                mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                                mProgressDialog.setProgress(0);
                                mProgressDialog.setTitle(R.string.dialog_apply_icon_pack_title);
                                mProgressDialog.setMessage(getActivity().getString(R.string.dialog_apply_icon_pack_message));
                                mProgressDialog.setCancelable(false);
                                mProgressDialog.show();
                                time = System.currentTimeMillis();
                                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                editor.putString(Const.Defaults.TAG_ICON_PACK, mainActivity.mCurrentIconPack.mPackageName);
                                editor.apply();
                            }

                            @Override
                            protected Void doInBackground(Void... params) {
                                try {
                                    mainActivity.mCurrentIconPack.loadAllInstalled(new UpdateListener() {
                                        @Override
                                        public void update(int current, int max) {
                                            publishProgress((int) (((float) current / (float) max) * 100));
                                        }
                                    });
                                } catch (Exception e){
                                    e.printStackTrace();
                                }
                                LauncherActivity.mDrawerTree.fullReload();
                                return null;
                            }

                            @Override
                            protected void onPostExecute(Void aVoid) {
                                mProgressDialog.cancel();

                                ((FolderPagerAdapter) mainActivity.mPager.getAdapter()).notifyPagesChanged();
                            }

                            @Override
                            protected void onProgressUpdate(Integer... values) {
                                mProgressDialog.setProgress(values[0]);
                            }
                        }.execute();
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        }
                ).show();
                }
            });

        //Wallpaper
        btWallpaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_SET_WALLPAPER);
                LauncherUtils.startActivity(mainActivity, v, intent);
            }
        });
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.quick_settings_fragment, container, false);
    }
}
