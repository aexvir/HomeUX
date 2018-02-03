package com.dravite.homeux.settings;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dravite.homeux.settings.clock.ActivityClockSettings;
import com.dravite.homeux.general_helpers.FileManager;
import com.dravite.homeux.settings.backup_restore.BackupRestoreActivity;
import com.dravite.homeux.Const.Defaults;
import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.settings.hidden_apps.HiddenAppsActivity;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.R;
import com.dravite.homeux.folder_editor.SelectFolderIconActivity;
import com.dravite.homeux.welcome.WelcomeActivity;
import com.dravite.homeux.general_adapters.AppSelectAdapter;
import com.dravite.homeux.general_adapters.FontAdapter;
import com.dravite.homeux.general_dialogs.IconTextListDialog;
import com.dravite.homeux.settings.items.BaseItem;
import com.dravite.homeux.settings.items.CaptionSettingsItem;
import com.dravite.homeux.settings.items.GenericSettingsItem;
import com.dravite.homeux.settings.items.SwitchSettingsItem;
import com.dravite.homeux.settings.settings_fragments.SettingsFragmentAdapter;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.List;

/**
 * This Activity shows several lists of {@link BaseItem BaseItems} in several tabs.
 */
public class SettingsActivity extends SettingsBaseActivity {

    //Requested from LauncherActivity when opening this SettingsActivity
    public static final int REQUEST_SETTINGS = 425;

    //Requested when opening the backup&restore Activity.
    public static final int REQUEST_BACKUP_RESTORE = 340;

    //Requested when opening the icon picker Activity for the floating QuickAction button.
    public static final int REQUEST_PICK_QA_ICON = 840;

    //Just a reference to the LauncherActivity's FolderStructure Object
    public Reference<FolderStructure> mFolderStructure;
    private SharedPreferences preferences;

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        preferences = PreferenceManager.getDefaultSharedPreferences(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initiateFragments(SettingsFragmentAdapter adapter) {
        mFolderStructure = new SoftReference<>(LauncherActivity.mFolderStructure);
        initAppPage();
        initActionPage();
        initUXPage();
    }

    /**
     * Initializes the "Apps" tab page by adding some settings items.
     */
    void initAppPage(){
        final Context mContext = this;
        putPage(getString(R.string.page_apps), new AddItems() {
            @Override
            public void create(List<BaseItem> mItems) {
                int gridWidth = preferences.getInt(Defaults.TAG_APP_WIDTH, getResources().getInteger(R.integer.app_grid_width));
                int gridHeight = preferences.getInt(Defaults.TAG_APP_HEIGHT, getResources().getInteger(R.integer.app_grid_height));

                mItems.add(new GenericSettingsItem(mContext.getString(R.string.menu_action_hidden_apps), null, "", R.drawable.ic_hide, new Intent(SettingsActivity.this, HiddenAppsActivity.class)));
                mItems.add(new GenericSettingsItem(getString(R.string.app_grid_size), gridWidth + " wide, " + gridHeight + " high", "grid_size", R.drawable.ic_grid_on_black_24dp, mGridSizeListener));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.app_icon_size), (32+8*preferences.getInt(Defaults.TAG_ICON_SIZE, Defaults.getInt(Defaults.TAG_ICON_SIZE))) + "dp", "iconSize5", R.drawable.ic_icon_size, mIconSizeListener));
                mItems.add(new SwitchSettingsItem(mContext.getString(R.string.app_labels), null, Defaults.TAG_SHOW_LABELS, R.drawable.ic_show_labels, true, Defaults.getBoolean(Defaults.TAG_SHOW_LABELS)));
                mItems.add(new SwitchSettingsItem(mContext.getString(R.string.enable_notification_badges), "Show the notification count on the icons", Defaults.TAG_NOTIFICATIONS, R.drawable.ic_notifications_black_24dp, true, Defaults.getBoolean(Defaults.TAG_NOTIFICATIONS)));
                mItems.add(new GenericSettingsItem(getString(R.string.notification_badge_corner_radius), gridWidth + " wide, " + gridHeight + " high", "grid_size", R.drawable.ic_grid_on_black_24dp, mNotificationBadgeCustomizer));
                mItems.add(new SwitchSettingsItem(mContext.getString(R.string.hide_apps_from_all), null, Defaults.TAG_HIDE_ALL, R.drawable.ic_settings_applications_black_24dp, true));
                mItems.add(new CaptionSettingsItem(getString(R.string.category_pages)));
                mItems.add(new SwitchSettingsItem(mContext.getString(R.string.dis_wallpaper_scroll), null, Defaults.TAG_DISABLE_WALLPAPER_SCROLL, R.drawable.ic_wallpaper_black_24dp, true, Defaults.getBoolean(Defaults.TAG_DISABLE_WALLPAPER_SCROLL)));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.app_page_transition), getResources().getStringArray(R.array.page_transformer_names)[preferences.getInt(Defaults.TAG_TRANSFORMER_INT, Defaults.getInt(Defaults.TAG_TRANSFORMER_INT))], "transformer", R.drawable.ic_page_transitions, mPageTransitionListener));
            }
        });
    }

    /**
     * Initializes the "Action Panel" tab page by adding some settings items.
     */
    void initActionPage(){
        final Context mContext = this;
        putPage(getString(R.string.page_action_panel), new AddItems() {
            @Override
            public void create(List<BaseItem> mItems) {
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.category_clock_widget), null, "", R.drawable.ic_clock, new Intent(SettingsActivity.this, ActivityClockSettings.class)));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.action_panel_transparency), (int)(preferences.getFloat(Defaults.TAG_PANEL_TRANS, Defaults.getFloat(Defaults.TAG_PANEL_TRANS))*100) + "%", Defaults.TAG_PANEL_TRANS, R.drawable.ic_transparency, mOpacityListener));
                mItems.add(new SwitchSettingsItem(mContext.getString(R.string.transparent_status), null, Defaults.TAG_TRANSP_STATUS, R.drawable.ic_space_bar_black_24dp, true, Defaults.getBoolean(Defaults.TAG_TRANSP_STATUS)));
                mItems.add(new SwitchSettingsItem(mContext.getString(R.string.direct_reveal), mContext.getString(R.string.direct_reveal_desc), Defaults.TAG_DIRECT_REVEAL, R.drawable.ic_swap_vertical_circle_black_24dp, true, Defaults.getBoolean(Defaults.TAG_DIRECT_REVEAL)));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.default_home), preferences.getString(Defaults.TAG_DEFAULT_FOLDER, Defaults.getString(Defaults.TAG_DEFAULT_FOLDER)), Defaults.TAG_DEFAULT_FOLDER, R.drawable.ic_home_black_24dp, mDefaultHomeListener));
                mItems.add(new CaptionSettingsItem(getString(R.string.category_quick_actions)));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.quick_app_floating), preferences.getString(Defaults.TAG_QA_FAB, Defaults.getString(Defaults.TAG_QA_FAB)), Defaults.TAG_QA_FAB, R.drawable.ic_play_circle_filled_black_24dp, mQuickAppListener));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.quick_app_floating_icon), null, "", getDrawableFromPreference(Defaults.TAG_QA_ICON, Defaults.getString(Defaults.TAG_QA_ICON)), new Intent(SettingsActivity.this, SelectFolderIconActivity.class), REQUEST_PICK_QA_ICON));
                mItems.add(new SwitchSettingsItem(mContext.getString(R.string.switch_config), mContext.getString(R.string.switch_config_desc), Defaults.TAG_SWITCH_CONFIG, R.drawable.ic_swap_horiz_black_24dp, true, Defaults.getBoolean(Defaults.TAG_SWITCH_CONFIG)));
            }
        });
    }

    /**
     * Initializes the "HomeUX" tab page by adding some settings items.
     */
    void initUXPage() {
        final Context mContext = this;
        putPage(getString(R.string.page_home_ux), new AddItems() {
            @Override
            public void create(List<BaseItem> mItems) {
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.activity_backup_restore), null, "", R.drawable.ic_settings_backup_restore_black_24dp, new Intent(SettingsActivity.this, BackupRestoreActivity.class), REQUEST_BACKUP_RESTORE));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.redo_welcome), null, "", R.drawable.ic_info_outline_black_24dp, new Intent(SettingsActivity.this, WelcomeActivity.class)));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.reset), mContext.getString(R.string.reset_desc), "", R.drawable.ic_clear_black_24dp, mResetClickListener));
                mItems.add(new CaptionSettingsItem(getString(R.string.category_about)));

                String versionName="0", versionCode="0";
                try {
                    //retrieve version name and code.
                    versionName = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
                    versionCode = String.valueOf(getPackageManager().getPackageInfo(getPackageName(), 0).versionCode);
                } catch (PackageManager.NameNotFoundException e){
                    e.printStackTrace();
                }

                mItems.add(new GenericSettingsItem(mContext.getString(R.string.app_version), versionName, "", R.drawable.ic_home_ux, Uri.parse(getString(R.string.app_version_url))));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.app_version_code), versionCode, "", R.drawable.ic_home_ux, Uri.parse(getString(R.string.app_version_code_url))));
                mItems.add(new GenericSettingsItem(mContext.getString(R.string.about_dravite), mContext.getString(R.string.about_dravite_desc), "", R.drawable.ic_info_black_24dp, Uri.parse(getString(R.string.about_dravite_url))));
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==REQUEST_BACKUP_RESTORE){
            Intent intent = getIntent();
            intent.putExtra("page", 3);
            finish();
            startActivity(intent);
        } else if(requestCode==REQUEST_PICK_QA_ICON && resultCode==RESULT_OK){
            String resName = data.getStringExtra("iconRes");

            PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().putString(Defaults.TAG_QA_ICON, resName).apply();
            Intent intent = getIntent();
            intent.putExtra("page", 1);
            finish();
            startActivity(intent);
        }
    }

    /**
     * Loads a drawable ID String from the SharedPreferences and returns its ID Integer.
     * @param key The SharedPreference key
     * @param defName The ID String
     * @return The ID String's drawable resource ID.
     */
    private int getDrawableFromPreference(String key, String defName){
        return getResources().getIdentifier(PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getString(key, defName), "drawable", getPackageName());
    }


    //LISTENERS

    //The listener to reset HomeUX
    BaseItem.ItemViewHolder.OnItemClickListener mResetClickListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position) {
            AlertDialog.Builder builder = new AlertDialog.Builder(SettingsActivity.this);
            builder.setTitle(getString(R.string.reset_desc))
                    .setMessage(getString(R.string.reset_msg))
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // FIXME: 26.10.2015
                            LauncherActivity.updateAfterSettings = true;
                            boolean isLicensed = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).getBoolean(Defaults.TAG_LICENSED, false);
                            FileManager.deleteRecursive(new File(getApplicationInfo().dataDir + "/cache/apps/"));
                            FileManager.deleteRecursive(new File(getApplicationInfo().dataDir + "/cache/Shortcuts/"));
                            FileManager.deleteRecursive(new File(getApplicationInfo().dataDir + "/folderImg/"));
                            FileManager.deleteRecursive(new File(getApplicationInfo().dataDir + "/hiddenApps.json"));
                            FileManager.deleteRecursive(new File(getApplicationInfo().dataDir + "/quickApps.json"));
                            FileManager.deleteRecursive(new File(getApplicationInfo().dataDir + "/somedata.json"));
                            PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().clear().putBoolean(Defaults.TAG_LICENSED, isLicensed).apply();
                            PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this).edit().clear().putBoolean(Defaults.TAG_FIRST_START, false).apply();
                            Intent intent = new Intent(SettingsActivity.this, LauncherActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    }).show();
        }
    };

    //The clickListener for the clock font
    BaseItem.ItemViewHolder.OnItemClickListener mClockFontListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            final FontAdapter fontAdapter = new FontAdapter(getBaseContext());
            new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme)
                    .setTitle(R.string.clock_font)
                    .setAdapter(fontAdapter, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String result = (String)fontAdapter.getItem(which);
                                    preferences.edit().putString(Defaults.TAG_CLOCK_FONT, result).apply();
                                    ((GenericSettingsItem)item).setDescription(result);
                                    adapter.notifyItemChanged(position);
                                    dialog.dismiss();
                                }
                            })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        }
    };

    //The listener for the clock size
    BaseItem.ItemViewHolder.OnItemClickListener mClockSizeListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme).setTitle(R.string.clock_size)
                    .setItems(R.array.sizes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String result = getResources().getStringArray(R.array.sizes)[which];
                            preferences.edit().putString(Defaults.TAG_CLOCK_SIZE, result)
                                    .putInt(Defaults.TAG_CLOCK_SIZE_INT, which).apply();
                            ((GenericSettingsItem) item).setDescription(result);
                            adapter.notifyItemChanged(position);
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
        }
    };

    //The listener for the page transition
    BaseItem.ItemViewHolder.OnItemClickListener mPageTransitionListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme).setTitle(R.string.app_page_transition)
                    .setItems(R.array.page_transformer_names, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String result = getResources().getStringArray(R.array.page_transformer_names)[which];
                            preferences.edit().putString(Defaults.TAG_TRANSFORMER, result)
                                    .putInt(Defaults.TAG_TRANSFORMER_INT, which).apply();
                            ((GenericSettingsItem) item).setDescription(result);
                            adapter.notifyItemChanged(position);
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();
        }
    };

    //The listener for app icon sizes
    BaseItem.ItemViewHolder.OnItemClickListener mIconSizeListener = new BaseItem.ItemViewHolder.OnItemClickListener() {

        boolean hasClicked = false;

        void setChecked(View layout, TextView text, boolean checked){
            layout.setBackground(checked?new ColorDrawable(0xff448AFF):getDrawable(R.drawable.ripple_colored_white_darkripple));
            text.setTextColor(checked?Color.WHITE:0x91000000);
        }

        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            final Dialog dialog = new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme).setTitle(R.string.app_icon_size)
                    .setView(R.layout.app_icon_size_dialog).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            hasClicked = false;
                        }
                    }).setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasClicked = false;
                        }
                    })
                    .show();

            final View layout24dp = dialog.findViewById(R.id.layout24dp);
            final View layout32dp = dialog.findViewById(R.id.layout32dp);
            final View layout44dp = dialog.findViewById(R.id.layout44dp);
            final View layout48dp = dialog.findViewById(R.id.layout48dp);
            final View layout56dp = dialog.findViewById(R.id.layout56dp);
            final View layout64dp = dialog.findViewById(R.id.layout64dp);

            final TextView text24dp = (TextView)dialog.findViewById(R.id.tex24dp);
            final TextView text32dp = (TextView)dialog.findViewById(R.id.tex32dp);
            final TextView text44dp = (TextView)dialog.findViewById(R.id.tex44dp);
            final TextView text48dp = (TextView)dialog.findViewById(R.id.tex48dp);
            final TextView text56dp = (TextView)dialog.findViewById(R.id.tex56dp);
            final TextView text64dp = (TextView)dialog.findViewById(R.id.tex64dp);

            View.OnClickListener clickListener = new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int returnValue = 0;
                    switch (v.getId()){
                        case R.id.layout24dp:
                            setChecked(layout24dp, text24dp, true);
                            setChecked(layout32dp, text32dp, false);
                            setChecked(layout44dp, text44dp, false);
                            setChecked(layout48dp, text48dp, false);
                            setChecked(layout56dp, text56dp, false);
                            setChecked(layout64dp, text64dp, false);
                            returnValue = -1;
                            break;
                        case R.id.layout32dp:
                            setChecked(layout24dp, text24dp, false);
                            setChecked(layout32dp, text32dp, true);
                            setChecked(layout44dp, text44dp, false);
                            setChecked(layout48dp, text48dp, false);
                            setChecked(layout56dp, text56dp, false);
                            setChecked(layout64dp, text64dp, false);
                            returnValue = 0;
                            break;
                        case R.id.layout44dp:
                            setChecked(layout24dp, text24dp, false);
                            setChecked(layout32dp, text32dp, false);
                            setChecked(layout44dp, text44dp, true);
                            setChecked(layout48dp, text48dp, false);
                            setChecked(layout56dp, text56dp, false);
                            setChecked(layout64dp, text64dp, false);
                            returnValue = 1;
                            break;
                        case R.id.layout48dp:
                            setChecked(layout24dp, text24dp, false);
                            setChecked(layout32dp, text32dp, false);
                            setChecked(layout44dp, text44dp, false);
                            setChecked(layout48dp, text48dp, true);
                            setChecked(layout56dp, text56dp, false);
                            setChecked(layout64dp, text64dp, false);
                            returnValue = 2;
                            break;
                        case R.id.layout56dp:
                            setChecked(layout24dp, text24dp, false);
                            setChecked(layout32dp, text32dp, false);
                            setChecked(layout44dp, text44dp, false);
                            setChecked(layout48dp, text48dp, false);
                            setChecked(layout56dp, text56dp, true);
                            setChecked(layout64dp, text64dp, false);
                            returnValue = 3;
                            break;
                        case R.id.layout64dp:
                            setChecked(layout24dp, text24dp, false);
                            setChecked(layout32dp, text32dp, false);
                            setChecked(layout44dp, text44dp, false);
                            setChecked(layout48dp, text48dp, false);
                            setChecked(layout56dp, text56dp, false);
                            setChecked(layout64dp, text64dp, true);
                            returnValue = 4;
                            break;
                    }
                    preferences.edit().putInt(Defaults.TAG_ICON_SIZE, returnValue).apply();
                    ((GenericSettingsItem)item).setDescription((32+8*returnValue) + "dp");
                    adapter.notifyItemChanged(position);
                    if(hasClicked) {
                        hasClicked = false;
                        dialog.dismiss();
                    }
                }
            };

            layout24dp.setOnClickListener(clickListener);
            layout32dp.setOnClickListener(clickListener);
            layout44dp.setOnClickListener(clickListener);
            layout48dp.setOnClickListener(clickListener);
            layout56dp.setOnClickListener(clickListener);
            layout64dp.setOnClickListener(clickListener);

            //After all initializations, perform a fake click to activate the currently chosen size
            switch (preferences.getInt(Defaults.TAG_ICON_SIZE, Defaults.getInt(Defaults.TAG_ICON_SIZE))){
                case -1:
                    layout24dp.performClick();
                    break;
                case 0:
                    layout32dp.performClick();
                    break;
                case 1:
                    layout44dp.performClick();
                    break;
                case 2:
                    layout48dp.performClick();
                    break;
                case 3:
                    layout56dp.performClick();
                    break;
                case 4:
                    layout64dp.performClick();
                    break;
            }
            hasClicked = true;

        }
    };

    //The quick app selection listener
    BaseItem.ItemViewHolder.OnItemClickListener mQuickAppListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            final AppSelectAdapter appSelectAdapter = new AppSelectAdapter(SettingsActivity.this);
                new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).setTitle(R.string.quick_app_floating)
                        .setAdapter(appSelectAdapter, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                preferences.edit()
                                        .putString(Defaults.TAG_QA_FAB, appSelectAdapter.mInfos.get(which).getLabel().toString())
                                        .putString(Defaults.TAG_QA_FAB_PKG, appSelectAdapter.mInfos.get(which).getComponentName().getPackageName())
                                        .putString(Defaults.TAG_QA_FAB_CLS, appSelectAdapter.mInfos.get(which).getComponentName().getClassName())
                                        .apply();
                                ((GenericSettingsItem) item).setDescription(appSelectAdapter.mInfos.get(which).getLabel().toString());
                                adapter.notifyItemChanged(position);
                            }
                        }).show();
        }
    };

    //The default home folder listener
    BaseItem.ItemViewHolder.OnItemClickListener mDefaultHomeListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            new IconTextListDialog(SettingsActivity.this, R.style.DialogTheme, R.string.default_home)
                .doOnSubmit(new IconTextListDialog.OnDialogSubmitListener() {
                    @Override
                    public void onSubmit(int position2) {
                        FolderStructure.Folder folder = mFolderStructure.get().folders.get(position2);
                        preferences.edit().putString(Defaults.TAG_DEFAULT_FOLDER, folder.folderName).apply();

                        ((GenericSettingsItem) item).setDescription(folder.folderName);
                        adapter.notifyItemChanged(position);
                    }
                })
                .setItemModifier(new IconTextListDialog.ItemModifier() {
                    @Override
                    public String getText(int position) {
                        return mFolderStructure.get().folders.get(position).folderName;
                    }

                    @Override
                    public Drawable getIcon(int position) {
                        //TODO
                        return getDrawable(getResources().getIdentifier(mFolderStructure.get().folders.get(position).folderIconRes, "drawable", getPackageName()));
                    }

                    @Override
                    public int[] getIconSize(int position) {
                        int size = 24;
                        return new int[]{size, size};
                    }

                    @Override
                    public int getCount() {
                        return mFolderStructure.get().folders.size();
                    }

                    @Override
                    public ColorStateList getTint(int position) {
                        return ColorStateList.valueOf(0x8a000000);
                    }
                })
                .show();
        }
    };

    //The action panel opacity listener
    BaseItem.ItemViewHolder.OnItemClickListener mOpacityListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            final float[] saved = new float[1];
            Dialog dialog = new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme).setTitle(R.string.action_panel_transparency)
                    .setView(R.layout.action_panel_opacity).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putFloat(Defaults.TAG_PANEL_TRANS, saved[0]);
                            editor.apply();

                            ((GenericSettingsItem)item).setDescription((int)(saved[0]*100) + "%");
                            adapter.notifyItemChanged(position);
                        }
                    }).setNegativeButton(android.R.string.cancel, null).show();

            final SeekBar seekOpacity = (SeekBar)dialog.findViewById(R.id.seek_opacity);
            final TextView textOpacity = (TextView)dialog.findViewById(R.id.text_opactity);

            saved[0] = preferences.getFloat(Defaults.TAG_PANEL_TRANS, 1);
            seekOpacity.setMax(100);

            seekOpacity.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    saved[0] = progress/100.f;
                    textOpacity.setText(progress + "%");
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
            });
            seekOpacity.setProgress((int)(saved[0]*100));
        }
    };

    // Notification badge customization listener
    BaseItem.ItemViewHolder.OnItemClickListener mNotificationBadgeCustomizer = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, BaseItem item, RecyclerView.Adapter adapter, int position) {
            final HashMap<String, Integer> notificationBadgeSettings = new HashMap<>();

            Dialog dialog = new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme)
                    .setTitle(R.string.app_notification_badge_design)
                    .setView(R.layout.notification_badge_dialog)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = preferences.edit();
                            editor.putInt(Defaults.TAG_NOTIFICATIONS_RADIUS, notificationBadgeSettings.get("radius"));

                            editor.apply();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();

            int cornerRadiusValue = preferences.getInt(Defaults.TAG_NOTIFICATIONS_RADIUS, getResources().getInteger(R.integer.notification_badge_radius));
            notificationBadgeSettings.put("radius", cornerRadiusValue);

            DiscreteSeekBar cornerRadius = dialog.findViewById(R.id.cornerRadius);
            cornerRadius.setProgress(cornerRadiusValue);

            cornerRadius.setOnProgressChangeListener(new DiscreteSeekBar.OnProgressChangeListener() {
                @Override
                public void onProgressChanged(DiscreteSeekBar seekBar, int value, boolean fromUser) {
                    notificationBadgeSettings.put("radius", value);
                }

                @Override
                public void onStartTrackingTouch(DiscreteSeekBar seekBar) {}

                @Override
                public void onStopTrackingTouch(DiscreteSeekBar seekBar) {}
            });
        }
    };

    //The app drawer grid size listener
    BaseItem.ItemViewHolder.OnItemClickListener mGridSizeListener = new BaseItem.ItemViewHolder.OnItemClickListener() {
        @Override
        public void onClick(View v, final BaseItem item, final RecyclerView.Adapter adapter, final int position) {
            final int[] savedData = new int[2];

            Dialog dialog = new AlertDialog.Builder(SettingsActivity.this, R.style.DialogTheme)
                    .setTitle(R.string.app_grid_size)
                    .setView(R.layout.grid_size_dialog)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor = preferences.edit();

                            editor.putInt(Defaults.TAG_APP_WIDTH, savedData[0]);
                            editor.putInt(Defaults.TAG_APP_HEIGHT, savedData[1]);
                            editor.apply();

                            ((GenericSettingsItem)item).setDescription(savedData[0] + " wide, " + savedData[1] + " high");
                            adapter.notifyItemChanged(position);
                        }
                    }).setNegativeButton(android.R.string.cancel, null)
                    .show();

            final SeekBar seekWidth = (SeekBar)dialog.findViewById(R.id.seek_width);
            final SeekBar seekHeight = (SeekBar)dialog.findViewById(R.id.seek_height);

            final TextView textWidth = (TextView)dialog.findViewById(R.id.text_width);
            final TextView textHeight = (TextView)dialog.findViewById(R.id.text_height);

            savedData[0] = preferences.getInt(Defaults.TAG_APP_WIDTH, getResources().getInteger(R.integer.app_grid_width));
            savedData[1] = preferences.getInt(Defaults.TAG_APP_HEIGHT, getResources().getInteger(R.integer.app_grid_height));
            final int minWidth = getResources().getInteger(R.integer.min_app_grid_width)*10;
            final int minHeight = getResources().getInteger(R.integer.min_app_grid_height)*10;

            seekWidth.setProgress(savedData[0]*10-minWidth);
            seekHeight.setProgress(savedData[1]*10-minHeight);
            textWidth.setText(String.valueOf(savedData[0]));
            textHeight.setText(String.valueOf(savedData[1]));

            seekWidth.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser){
                        savedData[0] = (minWidth+progress+5)/10;
                        textWidth.setText(String.valueOf(savedData[0]));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            seekHeight.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if(fromUser){
                        savedData[1] = (minHeight+progress+5)/10;
                        textHeight.setText(String.valueOf(savedData[1]));
                    }
                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            dialog.show();
        }
    };

}
