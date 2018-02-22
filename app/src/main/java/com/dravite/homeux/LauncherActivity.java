package com.dravite.homeux;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.os.Process;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.helpers.ContactUtil;
import com.dravite.homeux.drawerobjects.DrawerObject;
import com.dravite.homeux.drawerobjects.structures.AppDrawerPageFragment;
import com.dravite.homeux.drawerobjects.structures.AppDrawerPagerAdapter;
import com.dravite.homeux.drawerobjects.structures.ClickableAppWidgetHostView;
import com.dravite.homeux.drawerobjects.structures.DrawerTree;
import com.dravite.homeux.drawerobjects.Shortcut;
import com.dravite.homeux.drawerobjects.Widget;
import com.dravite.homeux.app_editor.AppEditorActivity;
import com.dravite.homeux.drawerobjects.structures.FolderDrawerPageFragment;
import com.dravite.homeux.drawerobjects.structures.FolderPagerAdapter;
import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.folder_editor.FolderEditorActivity;
import com.dravite.homeux.folder_editor.FolderEditorAddActivity;
import com.dravite.homeux.general_adapters.AppBarPagerAdapter;
import com.dravite.homeux.general_adapters.FolderDropAdapter;
import com.dravite.homeux.general_helpers.ColorUtils;
import com.dravite.homeux.general_helpers.CustomWallpaperManager;
import com.dravite.homeux.general_helpers.ExceptionLog;
import com.dravite.homeux.general_helpers.FileManager;
import com.dravite.homeux.general_helpers.IconPackManager;
import com.dravite.homeux.general_helpers.JsonHelper;
import com.dravite.homeux.general_helpers.PageTransitionManager;
import com.dravite.homeux.general_helpers.ParallelExecutor;
import com.dravite.homeux.general_helpers.PreferenceHolder;
import com.dravite.homeux.iconpacks.LicensingObserver;
import com.dravite.homeux.search.SearchResultAdapter;
import com.dravite.homeux.top_fragments.ClockFragment;
import com.dravite.homeux.top_fragments.FolderListFragment;
import com.dravite.homeux.views.AppIconView;
import com.dravite.homeux.views.CustomGridLayout;
import com.dravite.homeux.views.DragSurfaceLayout;
import com.dravite.homeux.views.FolderButton;
import com.dravite.homeux.views.ObjectDropButtonStrip;
import com.dravite.homeux.views.QuickAppBar;
import com.dravite.homeux.views.RevealImageView;
import com.dravite.homeux.views.VerticalViewPager;
import com.dravite.homeux.views.helpers.AppWidgetContainer;
import com.dravite.homeux.views.page_transitions.app_panel.DefaultTopPanelTransformer;
import com.dravite.homeux.settings.SettingsActivity;
import com.dravite.homeux.views.viewcomponents.ProgressFadeDrawable;
import com.dravite.homeux.views.viewcomponents.RectOutlineProvider;
import com.dravite.homeux.views.viewcomponents.RevealOutlineProvider;
import com.dravite.homeux.welcome.WelcomeActivity;
import com.google.common.primitives.Ints;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import javax.xml.parsers.ParserConfigurationException;

/**
 * Just the main activity launching around.<br/>
 * extension for <code>Observer</code> is necessary for the {@link NotificationReceiver} etc.
 */
public class LauncherActivity extends AppCompatActivity implements Observer {

    // CONSTANTS
    public static boolean updateAfterSettings = false;
    public static final int ANIM_DURATION_DEFAULT = 170;      //Default anim time in ms
    public static final int MAX_OFFSCREEN_FOLDERS = 100;      //Max non-visible folders
    public static final int THREAD_COUNT = 16;       //Thread count for the mStaticParallelThreadPool
    public static final int REQUEST_CHANGE_WALLPAPER = 50;

    @SuppressWarnings("unused")
    private static final String TAG = "LauncherActivity"; //Log Tag

    //STATIC
    public static FolderStructure mFolderStructure;   //Contains all folders
    public static DrawerTree mDrawerTree;        //Contains all apps etc.
    public static ParallelExecutor mStaticParallelThreadPool = new ParallelExecutor(THREAD_COUNT); //Used for parallelized loading for everything

    //NORMAL
    public ArrayList<String> mStatusBarNotifications = new ArrayList<>(); //A list that should contain all notifications
    public int mCurrentAccent; //The current accent color.
    public AppWidgetContainer mAppWidgetContainer; //Contains i.e. an AppWidgetHost and some functionality
    public ViewGroup mAppBarLayout;      //The top panel
    public CoordinatorLayout mCoordinatorLayout; //The whole CoordinatorLayout container
    public DragSurfaceLayout mDragView;          //The drag surface overlay
    @Deprecated
    public View mIndicator;         //TODO remove (old indicator)
    public VerticalViewPager mPager;             //The folder pager
    public ViewPager mAppBarPager;       //The top panel pager
    public CustomWallpaperManager mWallpaperManager;  //The wallpaper manager to keep track of the scrolling
    public SharedPreferences mPreferences;       //A global SharedPreferences object
    public PreferenceHolder mHolder;            //Holds a bunch of SharedPreferences to not fetch them all the time
    public IconPackManager.IconPack mCurrentIconPack;   //The current iconpack
    private CustomGridLayout mAppGrid;           //A temporary GridLayout to use i.e. when having it as a focussed grid when adding a widget
    private ProgressFadeDrawable mProgressFadeDrawable;  //The drawable assigned to the FAB
    private RevealOutlineProvider mRevealOutlineProvider; //The outlineProvider for the FAB, changing its size when switching top panel page
    private ObjectDropButtonStrip mObjectDropButtonStrip; //General drop action strip
    private FloatingActionButton mFolderDropButton;  //A button that pops up from below when dragging an app in the all folder to put it into another folder
    private CardView mFolderDropCard;    //the CardView containing the mFolderDropList. Pops up when hovering over the mFolderDropButton
    private RecyclerView mFolderDropList;    //Contains all the folders except the All folder for the user to drop (move) apps into.
    private FloatingActionButton mFloatingActionButton;  //The main FAB at the top right
    private View mSearchInputLayout; //The search Layout containing the search bar
    private View mSearchResultLayout;    //The search Result list
    private NotificationReceiver mNotificationReceiver;  //Listens for popping up notifications.
    private Intent mLicenseIntent = new Intent(); //Started when checking for a valid pro license
    private FolderListFragment mFolderListFragment;    //The right panel of the top panel
    private boolean mIsInSearchMode = false;    //A flag that shows if we are currently searching.
    private float mCurrentFabOutlineProgress; //The progress of the FABs outlineprovider
    private ImageButton mSearchBackButton;      //The back button of the search layout
    private EditText mSearchInput;           //The text input of the search layout
    private SearchResultAdapter mSearchResultAdapter;   //The search result list adapter
    private List<Integer> mPaletteColors = new ArrayList<Integer>();
    Rect mDropFabRect = new Rect();  //The bounding box of the FolderDrop FAB
    public FolderDropAdapter mFolderDropAdapter;   //The Adapter for the FolderDrop list
    boolean isInFolderView = false; //Flag to show if the FolderDrop list is currently opened.

    //What should happen when long pressing the FAB?
    private View.OnLongClickListener mFabLongClickListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            if (mAppBarPager.getCurrentItem() != 2) {
                if (mPreferences.getBoolean(Const.Defaults.TAG_SWITCH_CONFIG, Const.Defaults.getBoolean(Const.Defaults.TAG_SWITCH_CONFIG)))
                    clickFab(v);
                else toggleSearchMode();
            }
            return true;
        }
    };

    //What should happen when tapping on the FAB
    private View.OnClickListener mFabClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (mAppBarPager.getCurrentItem() == 2) {
                FolderStructure.Folder newFolder = new FolderStructure.Folder();
                newFolder.headerImage = BitmapFactory.decodeResource(getResources(), R.drawable.welcome_header_small);
                newFolder.isAllFolder = false;
                newFolder.accentColor = 0xFF303F9F;
                newFolder.folderName = "";
                newFolder.folderIconRes = "ic_folder";
                newFolder.pages.add(new FolderStructure.Page());
                newFolder.mFolderType = FolderStructure.Folder.FolderType.TYPE_WIDGETS;
                FolderEditorActivity.FolderPasser.passFolder = new WeakReference<>(newFolder);
                Intent intent = new Intent(LauncherActivity.this, FolderEditorActivity.class);
                intent.putExtra("requestCode", FolderEditorActivity.REQUEST_ADD_FOLDER);
                intent.putExtra("wallpaperPalette", Ints.toArray(mPaletteColors));
                LauncherUtils.startActivityForResult(LauncherActivity.this, v, intent, FolderEditorActivity.REQUEST_ADD_FOLDER);
            } else {
                if (mPreferences.getBoolean(Const.Defaults.TAG_SWITCH_CONFIG, Const.Defaults.getBoolean(Const.Defaults.TAG_SWITCH_CONFIG))) {
                    toggleSearchMode();
                } else {
                    clickFab(v);
                }
            }
        }
    };
    //Listener for all shortcuts. Gets the launch intent from the view
    public View.OnClickListener mShortcutClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = (Intent) v.getTag();
            LauncherUtils.startActivity(LauncherActivity.this, v, i);
        }
    };
    //Listener for all apps. Gets the intent from the view and creates a launch intent out of it
    public View.OnClickListener mAppClickListener = new View.OnClickListener() {
        @Override
        @SuppressWarnings("")
        public void onClick(View v) {

            Intent i = LauncherUtils.makeLaunchIntent((Intent) v.getTag());
            LauncherUtils.startActivity(LauncherActivity.this, v, i);
        }
    };

    /**
     * Listens for new notifications
     */
    public class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String[] notifications = intent.getStringArrayExtra("notifications");
            mStatusBarNotifications.clear();
            if (notifications == null) {
                refreshNotificationIcons();
                return;
            }
            Collections.addAll(mStatusBarNotifications, notifications);
            refreshNotificationIcons();
        }
    }

    /**
     * Updates the preferences in the {@link #mHolder}.
     */
    public void updateHolder() {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        mHolder.showCard = mPreferences.getBoolean(Const.Defaults.TAG_HIDE_CARDS, Const.Defaults.getBoolean(Const.Defaults.TAG_HIDE_CARDS));
        mHolder.pagerTransition = mPreferences.getInt(Const.Defaults.TAG_TRANSFORMER_INT, Const.Defaults.getInt(Const.Defaults.TAG_TRANSFORMER_INT));
        mHolder.gridHeight = mPreferences.getInt(Const.Defaults.TAG_APP_HEIGHT, getResources().getInteger(R.integer.app_grid_height));
        mHolder.gridWidth = mPreferences.getInt(Const.Defaults.TAG_APP_WIDTH, getResources().getInteger(R.integer.app_grid_width));
        mHolder.isFirstStart = mPreferences.getBoolean(Const.Defaults.TAG_FIRST_START, Const.Defaults.getBoolean(Const.Defaults.TAG_FIRST_START));
        mHolder.useDirectReveal = mPreferences.getBoolean(Const.Defaults.TAG_DIRECT_REVEAL, Const.Defaults.getBoolean(Const.Defaults.TAG_DIRECT_REVEAL));

        String pkg = mPreferences.getString(Const.Defaults.TAG_QA_FAB_PKG, Const.Defaults.getString(Const.Defaults.TAG_QA_FAB_PKG));
        String cls = mPreferences.getString(Const.Defaults.TAG_QA_FAB_CLS, Const.Defaults.getString(Const.Defaults.TAG_QA_FAB_CLS));

        if (!(pkg.equals("") && cls.equals(""))) {
            mHolder.fabComponent = new ComponentName(pkg, cls);
        } else {
            mHolder.fabComponent = null;
        }

        mPreferences.edit().putBoolean(Const.Defaults.TAG_HIDE_CARDS, false).apply();
    }

    /**
     * Checks for new Notifications.
     */
    public void fetchNotifications() {
        if (!mPreferences.getBoolean(Const.Defaults.TAG_NOTIFICATIONS, Const.Defaults.getBoolean(Const.Defaults.TAG_NOTIFICATIONS))) {
            mStatusBarNotifications.clear();
            refreshNotificationIcons();
            return;
        }
        Intent intent = new Intent("com.dravite.homeux.NOTIFICATION_LISTENER_SERVICE");
        sendBroadcast(intent);
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mNotificationReceiver);
        mAppWidgetContainer.onStopActivity();
        super.onDestroy();
    }

    @Override
    public void update(Observable observable, Object data) {
        if (data != null && data instanceof Intent) {
            Intent intent = (Intent) data;

            SharedPreferences.Editor editor = mPreferences.edit();
            editor.apply();

            stopService(mLicenseIntent);
        }
    }

    /**
     * Called in {@link #onStart()}, starts up all Widget services.
     */
    private void startAppWidgetContainer() {
        if (mAppWidgetContainer == null)
            mAppWidgetContainer = new AppWidgetContainer(this);
        mAppWidgetContainer.onStartActivity();
    }

    /**
     * Registers the {@link #mNotificationReceiver}.
     */
    private void registerReceivers() {
        mNotificationReceiver = new NotificationReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.dravite.homeux.NOTIFICATION_LISTENER");
        registerReceiver(mNotificationReceiver, filter);
    }

    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
        //Make it fullscreen!
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    }

    /**
     * Initializes icon pack, page transitions, the {@link DrawerTree}, the {@link FolderStructure}, the {@link android.content.pm.LauncherApps.Callback}, and the {@link CustomWallpaperManager}.
     */
    public void initializeLauncherData() {
        try {
            LauncherLog.d(TAG, "initializeLauncherData: Start icon pack loading...");
            long time = System.currentTimeMillis();
            mCurrentIconPack = new IconPackManager.IconPack(this, mPreferences.getString(Const.Defaults.TAG_ICON_PACK, ""));
            mCurrentIconPack.loadAllInstalled(null);
            LauncherLog.d(TAG, "initializeLauncherData: Loading icons took " + (System.currentTimeMillis() - time) + "ms");
        } catch (PackageManager.NameNotFoundException | SAXException | IOException | ParserConfigurationException e) {
            e.printStackTrace();
        }

        mDrawerTree = new DrawerTree(this);
        mDrawerTree.fullReload();
        PageTransitionManager.initialize(this);

        LauncherApps launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
        launcherApps.registerCallback(new LauncherApps.Callback() {
            @Override
            public void onPackageRemoved(String packageName, UserHandle user) {
                onAppRemoved(packageName);
            }

            @Override
            public void onPackageAdded(String packageName, UserHandle user) {
                onAppAdded(packageName);
            }

            @Override
            public void onPackageChanged(String packageName, UserHandle user) {
                onAppChanged(packageName);
            }

            @Override
            public void onPackagesAvailable(String[] packageNames, UserHandle user, boolean replacing) {
                onAppsAdded(packageNames);
            }

            @Override
            public void onPackagesUnavailable(String[] packageNames, UserHandle user, boolean replacing) {
                onAppsRemoved(packageNames);
            }
        });

        if (mFolderStructure == null)
            mFolderStructure = JsonHelper.loadFolderStructure(this, mDrawerTree, mHolder);

        mWallpaperManager = new CustomWallpaperManager(this);
    }

    public void generateWallpaperPalette() {
        mPaletteColors.clear();
        Palette mWallpaperColors = mWallpaperManager.getWallpaperPalette();

        for(Palette.Swatch s : mWallpaperColors.getSwatches()) {
            mPaletteColors.add(ColorUtils.HSLtoColor(s.getHsl()));
        }

        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(
            Const.Defaults.TAG_NOTIFICATIONS_BACKGROUND_COLOR,
            mWallpaperColors.getDarkVibrantColor(getResources().getColor(R.color.notificationBadgeBackground))
        ).apply();

        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt(
            Const.Defaults.TAG_NOTIFICATIONS_TEXT_COLOR,
            mWallpaperColors.getLightVibrantColor(getResources().getColor(R.color.notificationBadgeText))
        ).apply();
    }

    /**
     * Initializes all class-global Views.
     */
    public void initViews() {
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        mAppBarLayout = (ViewGroup) findViewById(R.id.appBarLayout);
        mAppBarLayout.setOutlineProvider(new RectOutlineProvider());
        mObjectDropButtonStrip = (ObjectDropButtonStrip) findViewById(R.id.widgetArea);
        mFolderDropButton = (FloatingActionButton) findViewById(R.id.folder_drop_fab);
        mFolderDropCard = (CardView) findViewById(R.id.folder_drop_card);
        mFolderDropList = (RecyclerView) findViewById(R.id.folder_drop_list);

        mIndicator = findViewById(R.id.indicator);
        mDragView = (DragSurfaceLayout) findViewById(R.id.dragView);
        mPager = (VerticalViewPager) findViewById(R.id.homePager);
        mAppBarPager = (ViewPager) findViewById(R.id.appBarPager);
        mAppBarPager.setNestedScrollingEnabled(true);
        mFloatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
        mSearchInputLayout = findViewById(R.id.searchInputLayout);
        mSearchBackButton = (ImageButton) findViewById(R.id.searchBackButton);
        mSearchInput = (EditText) findViewById(R.id.searchInput);
        mSearchResultLayout = findViewById(R.id.searchResultLayout);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        //License observer
        LicensingObserver.getInstance().addObserver(this);

        //Widget container
        startAppWidgetContainer();

        //Remove all previously running instances TODO: why exactly did I do that?
        if (savedInstanceState != null)
            savedInstanceState.clear();
        super.onCreate(savedInstanceState);

        //Content
        setContentView(R.layout.activity_main);


        //PreferenceHolder
        mHolder = new PreferenceHolder();
        updateHolder();
        Const.ICON_SIZE = 32 + (mPreferences.getInt(Const.Defaults.TAG_ICON_SIZE, Const.Defaults.getInt(Const.Defaults.TAG_ICON_SIZE)) * 8);

        //Notifications
        registerReceivers();
        fetchNotifications();

        checkFirstStart();


        //Initializations
        initializeLauncherData();
        initViews();
        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);


        //Init ObjectDropButtonStrip
        mObjectDropButtonStrip.setAlpha(0);
        mObjectDropButtonStrip.setScaleX(0.7f);
        mObjectDropButtonStrip.setScaleY(0.7f);
        mObjectDropButtonStrip.setWidgetRemoveListener(new ObjectDropButtonStrip.WidgetRemoveListener() {
            View mHoveredView;

            @Override
            public void editFolder(final View button, final FolderStructure.Folder folderName) {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        FolderEditorActivity.FolderPasser.passFolder = new WeakReference<>(folderName);
                        ArrayList<ComponentName> names = new ArrayList<>();

                        for (FolderStructure.Page page : folderName.pages)
                            for (DrawerObject item : page.items)
                                if (item instanceof Application)
                                    names.add(new ComponentName(((Application) item).packageName, ((Application) item).className));

                        FolderEditorActivity.AppListPasser.passAlreadyContainedList(names);
                        Intent intent = new Intent(LauncherActivity.this, FolderEditorActivity.class);
                        intent.putExtra("requestCode", FolderEditorActivity.REQUEST_EDIT_FOLDER);
                        intent.putExtra("folderIndex", mFolderStructure.folders.indexOf(folderName));
                        intent.putExtra("wallpaperPalette", Ints.toArray(mPaletteColors));

                        LauncherUtils.startActivityForResult(LauncherActivity.this, button, intent, FolderEditorActivity.REQUEST_EDIT_FOLDER);
                    }
                }, 200);
            }

            @Override
            public void removeFolder(final View button, final FolderStructure.Folder folder) {
                button.animate().scaleX(0).scaleY(0).alpha(0).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mDragView.removeView(button);
                    }
                });

                LauncherLog.d(TAG, "Folder removed");

                new AlertDialog.Builder(LauncherActivity.this, R.style.DialogTheme).setTitle("Delete Folder")
                        .setMessage("Are you sure to delete this Folder?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                int index = (mPager.getCurrentItem());
                                int newCount = (mFolderStructure.folders.size() - 1);
                                int newIndex = Math.max(0, Math.min(index, newCount - 1));

                                folder.deleteImage(LauncherActivity.this);
                                int removed = mFolderStructure.folders.indexOf(folder);
                                mFolderStructure.remove(folder);
                                JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);
                                ((FolderPagerAdapter) mPager.getAdapter()).notifyPagesChanged();
                                ((FolderListFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 2)).mAdapter.notifyItemRemoved(removed);
                                (((FolderListFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 2)).mAdapter).select(newIndex);
                                mPager.setCurrentItem(newIndex, false);
                                revealColor(LauncherActivity.mFolderStructure.folders.get(newIndex).headerImage);
                            }
                        }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ((FolderListFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 2)).mAdapter.notifyDataSetChanged();
                        ((FolderPagerAdapter) mPager.getAdapter()).notifyPagesChanged();
                    }
                }).show();
            }

            @Override
            public void removeWidget(DrawerObject data, ClickableAppWidgetHostView widgetHostView, int folderIndex, int pageIndex) {
                mAppWidgetContainer.removeWidget(widgetHostView);
                final AppDrawerPageFragment frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).getCurrentPagerCard();
                mAppGrid = frag.mAppGrid;
                mAppGrid.normalizeGrid();
                try {
                    mFolderStructure.folders.get(folderIndex).pages.get(pageIndex).items.remove(data);
                } catch (IndexOutOfBoundsException e) {
                    //-Nothing-
                }
                JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);
            }

            @Override
            public void removeOther(final DrawerObject data, View view, final int folderIndex, final int pageIndex) {

                if (view instanceof FolderButton) {
                    //REMOVED FOLDER

                    return;
                }

                if (view != null && view.getParent() != null && !isInAllFolder()) {
                    ((ViewGroup) view.getParent()).removeView(view);
                    mFolderStructure.folders.get(folderIndex).pages.get(pageIndex).items.remove(data);
                    if (!isInAllFolder() && mPreferences.getBoolean(Const.Defaults.TAG_HIDE_ALL, Const.Defaults.getBoolean(Const.Defaults.TAG_HIDE_ALL))) {
                        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
                    }
                    JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);

                    if (data instanceof Application) {
                        mFolderStructure.removeFolderAssignment(new ComponentName(((Application) data).packageName, ((Application) data).className), mFolderStructure.folders.get(folderIndex).folderName);
                        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
                    }

                    Snackbar.make(mCoordinatorLayout, "App removed from this folder.", Snackbar.LENGTH_SHORT).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            FolderDrawerPageFragment f = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, folderIndex));
                            if (pageIndex >= mFolderStructure.folders.get(folderIndex).pages.size()) {
                                mFolderStructure.folders.get(folderIndex).pages.add(new FolderStructure.Page());
                                f.mPager.getAdapter().notifyDataSetChanged();
                                f.mPager.setCurrentItem(pageIndex, true);
                            }
                            mFolderStructure.folders.get(folderIndex).pages.get(pageIndex).add(data);
                            JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);

                            if (data instanceof Application) {
                                //TODO testing refresh
                                //TODO re-added application "data" from folder "folderIndex"
                                mFolderStructure.addFolderAssignment(new ComponentName(((Application) data).packageName, ((Application) data).className), mFolderStructure.folders.get(folderIndex).folderName);
                                refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
                            }

                            try {
                                AppDrawerPageFragment frag = f.getPagerCard(pageIndex);
                                mAppGrid = frag.mAppGrid;
                                data.createView(mAppGrid, (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE), new DrawerObject.OnViewCreatedListener() {
                                    @Override
                                    public void onViewCreated(View view) {
                                        mAppGrid.addObject(view, data);
                                    }
                                });
                            } catch (NullPointerException e) {
                                e.printStackTrace();
                            }
                        }
                    }).show();
                }

                if (data != null && isInAllFolder() && LauncherUtils.isPackageInstalled(((Application) data).appIntent.getComponent().getPackageName(), LauncherActivity.this)) {
                    if (LauncherUtils.canBeUninstalled(((Application) data).appIntent.getComponent().getPackageName(), LauncherActivity.this)) {
                        Intent intent = new Intent(Intent.ACTION_DELETE);
                        intent.setData(Uri.parse("package:" + ((Application) data).appIntent.getComponent().getPackageName()));
                        startActivity(intent);
                    } else {
                        Snackbar.make(mCoordinatorLayout, "This is a system app and cannot be uninstalled.", Snackbar.LENGTH_SHORT).show();
                    }
                }
            }

            void editApp(Application application, View view) {
                Intent intent = new Intent(LauncherActivity.this, AppEditorActivity.class);
                AppEditorActivity.PassApp.softApp = new SoftReference<>(application);
                AppEditorActivity.PassApp.iconPack = new SoftReference<>(mCurrentIconPack);
                LauncherUtils.startActivityForResult(LauncherActivity.this, view, intent, AppEditorActivity.REQUEST_EDIT_APP);
            }

            @Override
            public void doAction(final DrawerObject data, final View view, String action, final int folderIndex, final int pageIndex, final CustomGridLayout parent) {
                switch (action) {
                    case "appInfo":
                        final Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                        Uri uri = Uri.fromParts("package", ((Application) data).packageName, null);
                        intent.setData(uri);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startActivity(intent);
                            }
                        }, 180);
                        break;
                    case "editApp":
                        if (data instanceof Application) {
                            editApp((Application) data, view);
                        }
                        break;
                    case "uninstall":
                        if (data != null && LauncherUtils.isPackageInstalled(((Application) data).appIntent.getComponent().getPackageName(), LauncherActivity.this)) {
                            if (LauncherUtils.canBeUninstalled(((Application) data).appIntent.getComponent().getPackageName(), LauncherActivity.this)) {
                                Intent i = new Intent(Intent.ACTION_DELETE);
                                i.setData(Uri.parse("package:" + ((Application) data).appIntent.getComponent().getPackageName()));
                                startActivity(i);
                            } else {
                                Snackbar.make(mCoordinatorLayout, "This is a system app and cannot be uninstalled.", Snackbar.LENGTH_SHORT).show();
                            }
                        }
                        break;
                }

                if (action.contains("appAction") && data != null) {
                    DrawerObject copyData = data.copy();

                    String[] parted = action.split("\n");
                    if (parted.length < 3) {
                        return;
                    }
                    int targetFolderIndex = mFolderStructure.folders.indexOf(mFolderStructure.getFolderWithName(parted[2]));

                    if ((mPreferences.getBoolean(Const.Defaults.TAG_HIDE_ALL, Const.Defaults.getBoolean(Const.Defaults.TAG_HIDE_ALL)) && isInAllFolder())) {
                        mFolderStructure.folders.get(folderIndex).pages.get(pageIndex).items.remove(data);
                        if (view != null && view.getParent() != null) {
                            parent.removeView(parent.findViewWithTag(((Application) copyData).appIntent));
                        }
                    }

                    copyData.mGridPosition.col = Integer.MIN_VALUE;
                    copyData.mGridPosition.row = Integer.MIN_VALUE;

                    addAppToFolder((Application) copyData, targetFolderIndex);
                    if (mPreferences.getBoolean(Const.Defaults.TAG_HIDE_ALL, Const.Defaults.getBoolean(Const.Defaults.TAG_HIDE_ALL))) {
                        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
                        ((FolderPagerAdapter) mPager.getAdapter()).notifyPagesChanged();
                    }
                    JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);
                }
            }

            @Override
            public void hovers(DrawerObject data, int x, int y, int overChildIndex) {
                if (mHoveredView != mObjectDropButtonStrip.getChildAt(overChildIndex) || (0x332196F3 != ((ColorDrawable) mObjectDropButtonStrip.getChildAt(overChildIndex).getBackground()).getColor())) {
                    if (mHoveredView != null)
                        mHoveredView.setBackgroundColor(0x00ffffff);
                    mHoveredView = mObjectDropButtonStrip.getChildAt(overChildIndex);
                    mHoveredView.setBackgroundColor(0x332196F3);
                }
            }

            @Override
            public void notHovering() {
                if (mHoveredView != null) {
                    mHoveredView.setBackgroundColor(0x00ffffff);
                }
            }
        });

        //Top-Level ViewPagers base init
        mPager.setAdapter(new FolderPagerAdapter(this, getSupportFragmentManager()));
        mPager.setOffscreenPageLimit(MAX_OFFSCREEN_FOLDERS);
        mPager.setPageTransformer(false, new ViewPager.PageTransformer() {
            @Override
            public void transformPage(View page, float position) {
                //Bottom: 1
                //Top: -1

                if (Math.abs(position) == 1) {
                    page.setVisibility(View.INVISIBLE);
                } else if (position < 0) {
                    page.setVisibility(View.VISIBLE);
                    page.setScaleX((1 + Math.max(-1, 2 * position)) * 0.5f + 0.5f);
                    page.setScaleY((1 + Math.max(-1, 2 * position)) * 0.5f + 0.5f);
                    page.setAlpha((1 + Math.max(-1, 2 * position)));
                    page.setTranslationY(page.getHeight() * (Math.abs(position)));
                } else if (position > 0) {
                    page.setVisibility(View.VISIBLE);
                } else {
                    page.setVisibility(View.VISIBLE);
                }
            }
        });
        mAppBarPager.setAdapter(new AppBarPagerAdapter(getSupportFragmentManager()));
        mAppBarPager.setCurrentItem(1);
        mAppBarPager.setOffscreenPageLimit(2);
        mAppBarPager.setPageTransformer(true, new DefaultTopPanelTransformer(this));

        //FAB init
        mRevealOutlineProvider = new RevealOutlineProvider(LauncherUtils.dpToPx(28, this), LauncherUtils.dpToPx(28, this), 0, LauncherUtils.dpToPx(28, this));
        mFloatingActionButton.setOutlineProvider(mRevealOutlineProvider);
        mFloatingActionButton.setClipToOutline(true);
        mRevealOutlineProvider.setProgress(1);
        mProgressFadeDrawable = new ProgressFadeDrawable(getDrawable(R.drawable.ic_search_black_24dp), getDrawable(R.drawable.ic_add_black_24dp));
        mProgressFadeDrawable.setTint(Color.WHITE);
        mFloatingActionButton.setImageDrawable(mProgressFadeDrawable);

        //Top panel pager pageChangeListener
        mAppBarPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1 && positionOffset > 0) {
                    mRevealOutlineProvider.setProgress(0.8f + (0.2f * (1 - positionOffset)));
                    mFloatingActionButton.invalidateOutline();
                    mFloatingActionButton.setRotation(positionOffset * 90);
                    mProgressFadeDrawable.setProgress(positionOffset);
                } else if (position != 2) {
                    mRevealOutlineProvider.setProgress(1);
                    mFloatingActionButton.invalidateOutline();
                    mFloatingActionButton.setRotation(0);
                    mProgressFadeDrawable.setProgress(0);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        //DragView init
        mDragView.setObjectDropButtonStrip(mObjectDropButtonStrip);
        //App or Widget
        mDragView.setDragDropListenerAppdrawer(new DragSurfaceLayout.DragDropListenerAppDrawer() {
            ViewPager mFocusPager;

            @Override
            public void onStartDrag(final View dragView, final DrawerObject data, ViewPager focusPager) {
                mObjectDropButtonStrip.wipeButtons();
                mFocusPager = focusPager;
                mAppBarPager.setCurrentItem(1, true);

                if (data instanceof Application) {
                    mObjectDropButtonStrip.setRemoveIcon(isInAllFolder() ? getDrawable(R.drawable.ic_action_uninstall) : getDrawable(R.drawable.ic_remove_black_24dp));
                    mObjectDropButtonStrip.setRemoveText(isInAllFolder() ? "Uninstall" : "Remove");
                    if (!isInAllFolder())
                        mObjectDropButtonStrip.addButton("Uninstall", "uninstall", getDrawable(R.drawable.ic_action_uninstall));
                    mObjectDropButtonStrip.addButton("Info", "appInfo", getDrawable(R.drawable.ic_action_info));
                    mObjectDropButtonStrip.addButton("Edit", "editApp", getDrawable(R.drawable.ic_action_edit));
                }

                if (!mHolder.showCard) {
                    mFocusPager.setBackgroundColor(0x1E000000);
                }

                mObjectDropButtonStrip.post(new Runnable() {
                    @Override
                    public void run() {
                        mAppBarLayout.animate().setDuration(ANIM_DURATION_DEFAULT).translationY(-TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
                        mIndicator.animate().scaleY(1).setDuration(ANIM_DURATION_DEFAULT).translationY(-TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()));
                        mFloatingActionButton.animate().setDuration(ANIM_DURATION_DEFAULT)
                                .translationY(-TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics()))
                                .scaleX(0).scaleY(0).rotation(90);
                        mObjectDropButtonStrip.animate().setDuration(ANIM_DURATION_DEFAULT).alpha(1).scaleX(1).scaleY(1);
                    }
                });

                mFocusPager.animate().scaleY(Const.APP_GRID_ZOOM_OUT_SCALE).scaleX(Const.APP_GRID_ZOOM_OUT_SCALE).setDuration(ANIM_DURATION_DEFAULT);

                if (mPager.getCurrentItem() == mFolderStructure.getFolderIndexOfName("All")) {
                    mFolderDropButton.animate().scaleY(1).scaleX(1).setInterpolator(new OvershootInterpolator()).setDuration(ANIM_DURATION_DEFAULT).withStartAction(new Runnable() {
                        @Override
                        public void run() {
                            mFolderDropButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            }

            @Override
            public void onEndDrag() {
                mAppBarLayout.animate().translationY(0).setDuration(ANIM_DURATION_DEFAULT);
                int pageCount = mFocusPager.getAdapter().getCount();
                mIndicator.animate().translationY(0).setDuration(ANIM_DURATION_DEFAULT).scaleX(1f / pageCount)
                        .translationX(mFocusPager.getCurrentItem() * (mAppBarLayout.getMeasuredWidth() / pageCount));

                if (!mHolder.showCard) {
                    mFocusPager.setBackgroundColor(0x00000000);
                }

                mFloatingActionButton.animate().translationY(0).scaleX(1).scaleY(1).rotation(0).setDuration(ANIM_DURATION_DEFAULT);
                mObjectDropButtonStrip.animate().alpha(0).scaleX(0.75f).scaleY(0.75f).setDuration(ANIM_DURATION_DEFAULT);
                // mFolderDropLayout.animate().alpha(0).scaleX(0.75f).scaleY(0.75f).setDuration(ANIM_DURATION_DEFAULT); //TODO
                switchBackFromFolderView();
                mFolderDropButton.animate().scaleX(0).scaleY(0).setDuration(ANIM_DURATION_DEFAULT).setInterpolator(new AnticipateInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        mFolderDropButton.setVisibility(View.GONE);
                    }
                });


                mFocusPager.animate().scaleY(1).scaleX(1).setDuration(ANIM_DURATION_DEFAULT);
                mFocusPager = null;
                fetchNotifications();

                mDragView.hideNextPageIndicator();
                mDragView.hidePrevPageIndicator();

            }
        });
        //QuickApp
        mDragView.setDragDropListenerQuickApp(new DragSurfaceLayout.DragDropListenerQuickApp() {
            @Override
            public void onStartDrag(View dragView) {
                mObjectDropButtonStrip.wipeButtons();
                mObjectDropButtonStrip.addButton("Edit", "editQa", getDrawable(R.drawable.ic_action_edit));
                mPager.animate().scaleX(0).scaleY(0).alpha(0).setDuration(ANIM_DURATION_DEFAULT);
                mObjectDropButtonStrip.animate().alpha(1).scaleX(1).scaleY(1).translationY(LauncherUtils.dpToPx(100, LauncherActivity.this)).setDuration(ANIM_DURATION_DEFAULT);
                mFloatingActionButton.animate().setDuration(ANIM_DURATION_DEFAULT).scaleX(0).scaleY(0).rotation(90);
            }

            @Override
            public void onEndDrag() {
                mPager.animate().scaleX(1).scaleY(1).alpha(1).setDuration(ANIM_DURATION_DEFAULT);
                mObjectDropButtonStrip.animate().alpha(0).scaleX(0.75f).scaleY(0.75f).translationY(0).setDuration(ANIM_DURATION_DEFAULT);
                mFloatingActionButton.animate().scaleX(1).scaleY(1).rotation(0).setDuration(ANIM_DURATION_DEFAULT);
                mDragView.hideNextPageIndicator();
                mDragView.hidePrevPageIndicator();
            }
        });
        //Folder
        mDragView.setDragDropListenerFolder(new DragSurfaceLayout.DragDropListenerFolder() {
            @Override
            public void onStartDrag(View dragView, FolderStructure.Folder folder) {
                if (folder == null)
                    return;
                if (folder.folderName.equals("All")) {
                    mObjectDropButtonStrip.wipeAllButtons();
                } else {
                    mObjectDropButtonStrip.wipeButtons();
                }
                mObjectDropButtonStrip.addButton("Edit", "editFolder", getDrawable(R.drawable.ic_action_edit));

                mPager.animate().scaleX(0).scaleY(0).alpha(0).setDuration(ANIM_DURATION_DEFAULT).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        ((FolderPagerAdapter) mPager.getAdapter()).switchToNullState();
                    }
                });
                mObjectDropButtonStrip.animate().alpha(1).scaleX(1).scaleY(1).translationY(LauncherUtils.dpToPx(100, LauncherActivity.this)).setDuration(ANIM_DURATION_DEFAULT);
                mFloatingActionButton.animate().setDuration(ANIM_DURATION_DEFAULT).scaleX(0).scaleY(0).rotation(90);
            }

            @Override
            public void onEndDrag() {
                ((FolderPagerAdapter) mPager.getAdapter()).switchFromNullState();
                mPager.animate().scaleX(1).scaleY(1).alpha(1).setDuration(ANIM_DURATION_DEFAULT);
                mObjectDropButtonStrip.animate().alpha(0).scaleX(0.75f).scaleY(0.75f).translationY(0).setDuration(ANIM_DURATION_DEFAULT);
                mFloatingActionButton.animate().scaleX(1).scaleY(1).rotation(0).setDuration(ANIM_DURATION_DEFAULT);
                mDragView.hideNextPageIndicator();
                mDragView.hidePrevPageIndicator();
            }
        });

        //Top panel reveal background
        final RevealImageView revImgView = (RevealImageView) findViewById(R.id.revealImgView);

        //Folder pager PageChangeListener
        mPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            ViewPager focusPager;

            public int calcColorValue(int oldC, int newC, float ratio) {
                int oRed = Color.red(oldC),
                        nRed = Color.red(newC);
                int oGreen = Color.green(oldC),
                        nGreen = Color.green(newC);
                int oBlue = Color.blue(oldC),
                        nBlue = Color.blue(newC);

                int cRed = (int) (nRed * ratio + oRed * (1f - ratio));
                int cGreen = (int) (nGreen * ratio + oGreen * (1f - ratio));
                int cBlue = (int) (nBlue * ratio + oBlue * (1f - ratio));

                return Color.argb(255, cRed, cGreen, cBlue);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                float linearSizeScale = -(float) (10f * Math.log(1 - positionOffset) * (1 - positionOffset) * (1 - positionOffset) * (1 - positionOffset) * (1 - positionOffset));
                mIndicator.setScaleY(1 - linearSizeScale);

                //fading colors
                int fadeColor1 = mFolderStructure.folders.get(Math.max(0, position)).accentColor;
                int fadeColor2 = mFolderStructure.folders.get(Math.min(mFolderStructure.folders.size() - 1, position + 1)).accentColor;
                int fade = calcColorValue(fadeColor1, fadeColor2, positionOffset);
                int drawableFade = calcColorValue(ColorUtils.isBrightColor(fadeColor1) ? Color.BLACK : Color.WHITE,
                        ColorUtils.isBrightColor(fadeColor2) ? Color.BLACK : Color.WHITE, positionOffset);

                //applying colors
                mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(fade));
                mProgressFadeDrawable.setTint(drawableFade);
                mIndicator.setBackgroundTintList(ColorStateList.valueOf(fade));

                if (mHolder.useDirectReveal) {

                    int[] cntr = {0, 0};
                    //if(positionOffset==0)
                    cntr = mAppBarPager.getCurrentItem() != 2 || mFolderListFragment == null ?
                            new int[]{(revImgView.getMeasuredWidth() / 2), (revImgView.getMeasuredHeight() / 2)} :
                            mFolderListFragment.getRevealXCenter(Math.min(mFolderStructure.folders.size() - 1, position + 1), new int[]{(revImgView.getMeasuredWidth() / 2), (revImgView.getMeasuredHeight() / 2)});
                    //
                    revImgView.setRevealCenter(cntr[0], cntr[1]);//revImgView.getMeasuredWidth()/2, revImgView.getMeasuredHeight()/2);

                    revImgView.setBackground(new BitmapDrawable(getResources(), mFolderStructure.folders.get(position).headerImage));
                    revImgView.setForeground(new BitmapDrawable(getResources(), mFolderStructure.folders.get(Math.min(mFolderStructure.folders.size() - 1, position + 1)).headerImage));
                    revImgView.setProgress(positionOffset);
//
//                Log.d(TAG, "" + (position+positionOffset));
                }
            }

            @Override
            public void onPageSelected(final int position) {
                View view = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).getView();
                if (view != null) {
                    //That all works.
                    focusPager = (ViewPager) view.findViewById(R.id.folder_pager);
                    mDragView.setPager(focusPager);

                    if (!mPreferences.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL, Const.Defaults.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL))) {
                        ValueAnimator animator = ValueAnimator.ofFloat(mWallpaperManager.getWallpaperOffsets().x, focusPager.getCurrentItem() / (float) focusPager.getAdapter().getCount());
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                mWallpaperManager.setWallpaperOffsets(mPager.getWindowToken(), ((float) animation.getAnimatedValue()), 0);
                            }
                        });
                        animator.start();
                    }

                    ((GradientDrawable) mIndicator.getBackground()).setColor(mFolderStructure.folders.get(position).accentColor);
                    int cnt = focusPager.getAdapter().getCount() <= 0 ? 1 : focusPager.getAdapter().getCount();
                    mIndicator.animate().scaleX(1f / cnt)
                            .translationX(focusPager.getCurrentItem() * (mAppBarLayout.getMeasuredWidth() / cnt))
                            .scaleY(1);
                }

                //Works kind of
                if (!mHolder.useDirectReveal)
                    revealColor(mFolderStructure.folders.get(position).headerImage);

                mCurrentAccent = mFolderStructure.folders.get(position).accentColor;

                if (mFolderListFragment == null) {
                    mFolderListFragment = ((FolderListFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 2));
                }

                if (mFolderListFragment.mAdapter != null)
                    //Does not work.
                    mFolderListFragment.mAdapter.select(position);
//                else
//                    ExceptionLog.throwErrorMsg(LauncherActivity.this, "The FolderListFragment's adapter is null.");
            }

            @Override
            public void onPageScrollStateChanged(int state) {}
        });

        //Fit CoordinatorLayout to Window
        mCoordinatorLayout.requestApplyInsets();

        //More FAB init
        mFloatingActionButton.setOnClickListener(mFabClickListener);
        mFloatingActionButton.setOnLongClickListener(mFabLongClickListener);

        //initialize to the default folder
        int index = mFolderStructure.getDefaultFolderIndex(this);
        View view = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, index == -1 ? 0 : index)).getView();
        if (view != null) {
            ViewPager focusPager = (ViewPager) view.findViewById(R.id.folder_pager);
            mDragView.setPager(focusPager);

            if (!mPreferences.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL, Const.Defaults.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL))) {
                ValueAnimator animator = ValueAnimator.ofFloat(mWallpaperManager.getWallpaperOffsets().x, focusPager.getCurrentItem() / (float) focusPager.getAdapter().getCount());
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mWallpaperManager.setWallpaperOffsets(mPager.getWindowToken(), ((float) animation.getAnimatedValue()), 0);
                    }
                });
                animator.start();
            }

            ((GradientDrawable) mIndicator.getBackground()).setColor(mFolderStructure.folders.get(mFolderStructure.getDefaultFolderIndex(this)).accentColor);
        }
        FolderStructure.Folder folder = mFolderStructure.getFolderWithName(mPreferences.getString(Const.Defaults.TAG_DEFAULT_FOLDER, Const.Defaults.getString(Const.Defaults.TAG_DEFAULT_FOLDER)));
        mIndicator.animate().scaleX(1f / (folder == null ? mFolderStructure.getFolderWithName("All") : folder).pages.size()).translationX(0).scaleY(1);

        //Set All folder as default
        if (folder == null) {
            mPreferences.edit().putString(Const.Defaults.TAG_DEFAULT_FOLDER, Const.Defaults.getString(Const.Defaults.TAG_DEFAULT_FOLDER)).apply();
        }

        //Make reveal animation, load accent and set pager to its desired page
        revealColor(mFolderStructure.folders.get(mFolderStructure.getDefaultFolderIndex(this)).headerImage);
        mCurrentAccent = mFolderStructure.folders.get(mFolderStructure.getDefaultFolderIndex(LauncherActivity.this)).accentColor;
        mPager.setCurrentItem(mFolderStructure.getDefaultFolderIndex(this));

        //Notifications... again.
        fetchNotifications();

        //Initialize Search Views etc.
        RecyclerView mSearchResultList = (RecyclerView) findViewById(R.id.searchResultList);
        mSearchResultAdapter = new SearchResultAdapter(this);
        GridLayoutManager searchResultLayoutManager = new GridLayoutManager(this, 4);
        mSearchResultList.setLayoutManager(searchResultLayoutManager);
        mSearchResultList.setItemAnimator(new DefaultItemAnimator());
        searchResultLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                switch (mSearchResultAdapter.getItemViewType(position)) {
                    case SearchResultAdapter.VIEW_WEBSEARCH:
                        return 4;
                    case SearchResultAdapter.VIEW_APP:
                        return 1;
                    case SearchResultAdapter.VIEW_SEPARATOR:
                        return 4;
                    case SearchResultAdapter.VIEW_CONTACT:
                        return 2;
                    default:
                        return 1;
                }
            }
        });
        mSearchResultList.setAdapter(mSearchResultAdapter);

        //What happens when you press "enter" in search
        mSearchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (mSearchResultAdapter.hasEmptyQueries() && actionId == EditorInfo.IME_ACTION_DONE) {
                    mSearchResultAdapter.startWebSearch();
                    return true;
                }
                return false;
            }
        });

        //live search
        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                List<Application> queryResult = mDrawerTree.getApplicationsContaining(s.toString());
                mSearchResultAdapter.clearQuery();
                if (!queryResult.isEmpty())
                    mSearchResultAdapter.addToQueryResult(queryResult);
                mSearchResultAdapter.setQuery(s.toString());
                mSearchResultAdapter.addToContactsQueryResult(ContactUtil.getContactList(LauncherActivity.this, s.toString()));
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

    }

    /**
     * Switches to search view when pressing on the FAB.
     */
    public void toggleSearchMode() {
        mIsInSearchMode = true;
        getWindow().setNavigationBarColor(Color.BLACK);
        getWindow().setStatusBarColor(0xff455A64);

        //Clear Query button
        ImageButton clearButton = (ImageButton) findViewById(R.id.searchClearButton);
        clearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSearchInput.setText("");
            }
        });

        //Back button
        mSearchBackButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSearchMode();
            }
        });

        mSearchResultLayout.setVisibility(View.VISIBLE);
        mSearchResultLayout.setAlpha(0);
        mSearchResultLayout.animate().alpha(1).setStartDelay(50).setDuration(150);

        mSearchInputLayout.setVisibility(View.VISIBLE);
        mSearchInputLayout.setAlpha(0);
        mSearchInputLayout.setScaleX(0.9f);
        mSearchInputLayout.setScaleY(0.9f);
        mSearchInputLayout.animate().alpha(1).scaleX(1).scaleY(1).setStartDelay(50).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                if (mSearchInput.requestFocus()) {
                    InputMethodManager imm = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.showSoftInput(mSearchInput, InputMethodManager.SHOW_IMPLICIT);
                }
            }
        });

        //Animate all other stuff away
        mIndicator.animate().scaleY(0).alpha(0).setDuration(150);
        mAppBarLayout.animate().setDuration(150).alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                mAppBarLayout.setVisibility(View.GONE);
                mFloatingActionButton.setVisibility(View.GONE);
            }
        });
        mCurrentFabOutlineProgress = mRevealOutlineProvider.getProgress();
        ValueAnimator fabAnim = ObjectAnimator.ofFloat(mCurrentFabOutlineProgress, 0);
        fabAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRevealOutlineProvider.setProgress(mCurrentFabOutlineProgress * (1f - animation.getAnimatedFraction()));
            }
        });
        fabAnim.setDuration(150);
        fabAnim.start();
        mAppBarPager.animate().alpha(0).setDuration(150);
        mPager.animate().scaleX(0.7f).scaleY(0.7f).alpha(0).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                mPager.setVisibility(View.GONE);
                mAppBarPager.setVisibility(View.GONE);
                mRevealOutlineProvider.setProgress(0);
                mFloatingActionButton.invalidateOutline();
            }
        });
    }

    /**
     * Switches back from search mode to normal mode.
     */
    public void hideSearchMode() {
        mIsInSearchMode = false;

        //First clear query
        mSearchInput.setText("");
        mSearchResultAdapter.clearQuery();

        getWindow().setStatusBarColor(mPreferences.getBoolean(Const.Defaults.TAG_TRANSP_STATUS, Const.Defaults.getBoolean(Const.Defaults.TAG_TRANSP_STATUS)) ? Color.TRANSPARENT : 0x40000000);
        getWindow().setNavigationBarColor(Color.TRANSPARENT);

        //Animate back everything
        mFloatingActionButton.setVisibility(View.VISIBLE);
        mSearchResultLayout.animate().alpha(0).setDuration(100).withEndAction(new Runnable() {
            @Override
            public void run() {
                mSearchResultLayout.setVisibility(View.GONE);
                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mSearchInput.getWindowToken(), 0);
            }
        });
        mSearchInputLayout.animate().alpha(0).scaleX(0.9f).scaleY(0.9f).setDuration(100).withEndAction(new Runnable() {
            @Override
            public void run() {
                mSearchInputLayout.setVisibility(View.GONE);
            }
        });
        mPager.setVisibility(View.VISIBLE);
        mAppBarPager.setVisibility(View.VISIBLE);
        mAppBarLayout.setVisibility(View.VISIBLE);
        mAppBarLayout.animate().setDuration(150).alpha(1);
        mIndicator.animate().scaleY(1).alpha(1).setDuration(150);
        ValueAnimator fabAnim = ObjectAnimator.ofFloat(mRevealOutlineProvider.getProgress(), mCurrentFabOutlineProgress);
        fabAnim.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mRevealOutlineProvider.setProgress(mCurrentFabOutlineProgress * animation.getAnimatedFraction());
                mFloatingActionButton.invalidateOutline();
            }
        });
        fabAnim.setDuration(150);
        fabAnim.start();
        mAppBarPager.animate().alpha(1).setDuration(150);
        mPager.animate().scaleX(1f).scaleY(1f).alpha(1).setDuration(150);

    }

    /**
     * Opens the assigned app from the FAB.
     *
     * @param v The FAB itself
     */
    void clickFab(View v) {
        if (mHolder.fabComponent != null) {
            try {
                Intent intentToLaunch = new Intent();
                intentToLaunch.setComponent(mHolder.fabComponent);
                LauncherUtils.startActivity(LauncherActivity.this, v, LauncherUtils.makeLaunchIntent(intentToLaunch));
            } catch (ActivityNotFoundException e) {
                Snackbar.make(mCoordinatorLayout, "The assigned QuickApp is not installed.", Snackbar.LENGTH_SHORT).show();
            }
        } else {
            Snackbar.make(mCoordinatorLayout, "You have not assigned any QuickApp yet.", Snackbar.LENGTH_SHORT).show();
        }

    }

    /**
     * @param x                      absolute x coordinate
     * @param y                      absolute y coordinate
     * @param quickActionBarHovering is currently hovering over the QuickAppBar
     * @return <code>true</code>, if hovering inside the Folder drop list.
     */
    public boolean isInFolderDropLocation(float x, float y, boolean quickActionBarHovering) {
        if (mPager.getCurrentItem() != mFolderStructure.getFolderIndexOfName("All"))
            return false;

        //If rect is valid and contains coords
        if (isInFolderView) {

            if (mFolderDropAdapter != null) {
                mFolderDropAdapter.hover(mFolderDropList, x, y);
            }

            return !quickActionBarHovering;
        } else {
            if (mFolderDropButton.getGlobalVisibleRect(mDropFabRect)) {
                return mDropFabRect.contains((int) x, (int) y);
            }
        }

        return false;
    }

    /**
     * Opens the FolderDrop list
     */
    public void switchToFolderView() {

        if (isInFolderView)
            return;

        isInFolderView = true;

        mFolderDropButton.animate().scaleY(0).scaleX(0).setInterpolator(new AnticipateInterpolator()).setDuration(ANIM_DURATION_DEFAULT);
        mPager.animate().scaleX(0.75f).scaleY(0.75f).alpha(0);
        mFolderDropCard.animate().scaleX(1).scaleY(1).alpha(1).translationY(0).withStartAction(new Runnable() {
            @Override
            public void run() {
                mFolderDropCard.setVisibility(View.VISIBLE);
                mFolderDropList.setLayoutManager(new GridLayoutManager(LauncherActivity.this, 2));
                mFolderDropAdapter = new FolderDropAdapter(LauncherActivity.this);
                mFolderDropList.setAdapter(mFolderDropAdapter);
            }
        });

    }

    /**
     * Closes the FolderDrop List
     */
    public void switchBackFromFolderView() {

        if (!isInFolderView)
            return;

        isInFolderView = false;

        mFolderDropButton.animate().scaleY(1).scaleX(1).setInterpolator(new OvershootInterpolator()).setDuration(ANIM_DURATION_DEFAULT);
        mPager.animate().scaleX(1).scaleY(1).alpha(1);
        mFolderDropCard.animate().scaleX(0.5f).scaleY(0.5f).translationY(LauncherUtils.dpToPx(100, this)).alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                mFolderDropCard.setVisibility(View.GONE);
            }
        });

    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //Show QuickSettings page
            if (mAppBarPager != null)
                mAppBarPager.setCurrentItem(0);
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        //Reset changed views.
        if (mIsInSearchMode) {
            hideSearchMode();
        } else {
            View view = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).getView();
            if (view != null) {
                ViewPager focusPager = (ViewPager) view.findViewById(R.id.folder_pager);
                if (focusPager.getCurrentItem() == 0) {
                    mPager.setCurrentItem(mFolderStructure.getDefaultFolderIndex(this), true);
                } else {
                    focusPager.setCurrentItem(0, true);
                }
                mAppBarPager.setCurrentItem(1, true);
            }
        }

        //Don't close the app.
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        //Delete savedInstanceState. TODO: Still don't know why I did that.
        savedInstanceState.clear();
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        //Update mHolder again.
        updateHolder();

        //Is it the first app start?
        checkFirstStart();

        //Casually refresh the Folder ViewPager
        if (mPager != null && mPager.getAdapter() != null)
            mPager.getAdapter().notifyDataSetChanged();

        //Re-Assign SharedPreferences just in case and refresh several settings
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        final SharedPreferences mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Const.ICON_SIZE = 32 + (mPreferences.getInt(Const.Defaults.TAG_ICON_SIZE, Const.Defaults.getInt(Const.Defaults.TAG_ICON_SIZE)) * 8);

        //Refresh FAB icon
        mProgressFadeDrawable.setDrawableStart(getDrawable(getResources().getIdentifier(PreferenceManager.getDefaultSharedPreferences(this).getString(Const.Defaults.TAG_QA_ICON, Const.Defaults.getString(Const.Defaults.TAG_QA_ICON)), "drawable", getPackageName())));
        mProgressFadeDrawable.setTint(ColorUtils.isBrightColor(mCurrentAccent) ? Color.BLACK : Color.WHITE);

        //Notifications again.
        boolean isEnabled = Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners") != null && Settings.Secure.getString(getContentResolver(),
                "enabled_notification_listeners").contains(getPackageName());
        if (mPreferences.getBoolean(Const.Defaults.TAG_NOTIFICATIONS, Const.Defaults.getBoolean(Const.Defaults.TAG_NOTIFICATIONS)) && !isEnabled)
            new AlertDialog.Builder(this, R.style.DialogTheme).setTitle("Notification Access")
                    .setMessage("Please check if notification access for HomeUX is activated. (If you cancel this dialog, no notification badges are shown on the icons)")
                    .setPositiveButton("Check", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                            startActivity(intent);
                        }
                    }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor editor = mPreferences.edit();
                    editor.putBoolean(Const.Defaults.TAG_NOTIFICATIONS, false);
                    editor.apply();
                }
            }).show();
        fetchNotifications();

        //Refresh reveal layout.
        FrameLayout revealLayout = (FrameLayout) findViewById(R.id.revealLayout);
        float alpha = mPreferences.getFloat(Const.Defaults.TAG_PANEL_TRANS, Const.Defaults.getFloat(Const.Defaults.TAG_PANEL_TRANS));
        revealLayout.setAlpha(alpha);
        findViewById(R.id.revealImgView).setAlpha(alpha);
        if (mIsInSearchMode) {
            getWindow().setStatusBarColor(0xff455A64);
        } else {
            if (alpha == 0) {
                mAppBarLayout.setElevation(LauncherUtils.dpToPx(1, this));
                getWindow().setStatusBarColor(Color.TRANSPARENT);
            } else {
                mAppBarLayout.setElevation(LauncherUtils.dpToPx(4, this));
                getWindow().setStatusBarColor(mPreferences.getBoolean(Const.Defaults.TAG_TRANSP_STATUS, Const.Defaults.getBoolean(Const.Defaults.TAG_TRANSP_STATUS)) ? Color.TRANSPARENT : 0x40000000);
            }
        }
        //Set the needed Layout visible depending on the reveal mode
        if (mHolder.useDirectReveal) {
            revealLayout.setVisibility(View.GONE);
            findViewById(R.id.revealImgView).setVisibility(View.VISIBLE);
        } else {
            revealLayout.setVisibility(View.VISIBLE);
            findViewById(R.id.revealImgView).setVisibility(View.GONE);
        }

        //Wallpaper scrolling etc.
        if (!mPreferences.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL, Const.Defaults.getBoolean(Const.Defaults.TAG_DISABLE_WALLPAPER_SCROLL))) {
            if (mPager != null && mWallpaperManager != null) {
                View view = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).getView();
                if (view != null) {
                    ViewPager focusPager = (ViewPager) view.findViewById(R.id.folder_pager);

                    mWallpaperManager.setWallpaperOffsets(mPager.getWindowToken(), focusPager.getCurrentItem() / (float) focusPager.getAdapter().getCount(), 0);
                }
            }
        }
    }

    /**
     * Updates all app icons with their notification counts.
     */
    void refreshNotificationIcons() {
        //reset old
        if (mPager == null)
            return;
        for (int i = Math.max(0, mPager.getCurrentItem() - 1); i < Math.min(mPager.getAdapter().getCount(), mPager.getCurrentItem() + 2); i++) {
            //Iterate through folders
            FolderDrawerPageFragment fragment = (FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, i);
            ViewPager cPager = fragment.mPager;
            if (cPager == null)
                continue;
            for (int j = Math.max(0, cPager.getCurrentItem() - 1); j < Math.min(cPager.getAdapter().getCount(), cPager.getCurrentItem() + 2); j++) {
                //iterate through pages
                AppDrawerPageFragment cFragment = ((AppDrawerPageFragment) cPager.getAdapter().instantiateItem(cPager, j));
                if (cFragment.mAppGrid == null)
                    continue;
                for (int child = 0; child < cFragment.mAppGrid.getChildCount(); child++) {
                    //iterate through elements
                    if (cFragment.mAppGrid.getChildAt(child) instanceof AppIconView) {
                        CustomGridLayout.GridLayoutParams params = ((CustomGridLayout.GridLayoutParams) cFragment.mAppGrid.getChildAt(child).getLayoutParams());
                        String packageName = ((Application) params.viewData).info.getComponentName().getPackageName();
                        if (params.viewData instanceof Application && mStatusBarNotifications.contains(packageName)) {
                            int cnt = Collections.frequency(mStatusBarNotifications, packageName);
                            ((AppIconView) cFragment.mAppGrid.getChildAt(child)).setCounterOverlay((cnt > 3 ? cnt - 1 : cnt)); // Ugly hack, to deal with grouped notifications
                        } else {
                            ((AppIconView) cFragment.mAppGrid.getChildAt(child)).removeOverlay();
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        //HOME BUTTON PRESS
        super.onNewIntent(intent);

        if (hasWindowFocus()) {

            //When already back in app
            if (mIsInSearchMode)
                hideSearchMode();
            final ViewPager frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).mPager;
            frag.getAdapter().notifyDataSetChanged();
            frag.setCurrentItem(0, true);
            final int defIndex = mFolderStructure.getDefaultFolderIndex(LauncherActivity.this);
            mPager.setCurrentItem(defIndex, true);

            //Delay that for a bit.
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    final ViewPager frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, defIndex)).mPager;
                    frag.setCurrentItem(0, true);
                    mAppBarPager.setCurrentItem(1, true);
                }
            }, 100);
        }
    }

    /**
     * Adding apps from long pressing on the home screen.
     */
    private void onAddAppsRequest() {
        final int pos = mPager.getCurrentItem();

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                for (ComponentName a : FolderEditorActivity.AppListPasser.receiveAppList()) {
                    Intent intent = new Intent();
                    intent.setComponent(a);
                    LauncherActivityInfo info = ((LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE)).resolveActivity(intent, android.os.Process.myUserHandle());
                    final Application application = new Application(info);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            addAppToFolder(application, pos);
                        }
                    });
                    try {
                        Thread.sleep(20);
                        //Add one after the other, reduces accidentally overlaying icons.
                    } catch (InterruptedException e) {
                        LauncherLog.w(TAG, e.toString());
                    }
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                // added app "app" to folder at index "folderIndex"
                mFolderStructure.addFolderAssignments(mFolderStructure.folders.get(pos));
                refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
                JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);
            }
        }.execute();
    }

    /**
     * Adding a folder after the Folder Editor Activity has been ended on <code>RESULT_OK</code>.
     *
     * @param data Intent return data containing folder data.
     */
    private void onAddFolderRequest(Intent data) {
        FolderStructure.Folder newFolder = FolderEditorActivity.FolderPasser.passFolder.get();
        FolderEditorActivity.FolderPasser.passFolder.clear();
        if (newFolder == null)
            return;

        ArrayList arrList = data.getParcelableArrayListExtra("appList");

        if (arrList.size() != 0) {
            newFolder.pages.clear();
        }

        //Remove all hidden apps.
        arrList.removeAll(JsonHelper.loadHiddenAppList(this));

        //Add all apps.
        int allPageCount = (int) Math.ceil(arrList.size() / (float) mHolder.gridSize());
        for (int i = 0; i < allPageCount; i++) {
            final FolderStructure.Page page = new FolderStructure.Page();
            for (int j = 0; j < mHolder.gridSize() && (i * mHolder.gridSize() + j) < arrList.size(); j++) {
                LauncherApps launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
                Intent intent = new Intent();
                intent.setComponent((ComponentName) arrList.get(i * mHolder.gridSize() + j));
                LauncherActivityInfo info = launcherApps.resolveActivity(intent, android.os.Process.myUserHandle());
                if (info == null) continue;
                mDrawerTree.doWithApplication(info, new DrawerTree.LoadedListener() {
                    @Override
                    public void onApplicationLoadingFinished(Application app) {
                        page.add(app);
                    }
                });
            }
            newFolder.add(page);
        }

        //Add folder...
        mFolderStructure.add(newFolder);
        JsonHelper.saveFolderStructure(this, mFolderStructure);
        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
        if (mPager.getAdapter() != null)
            mPager.getAdapter().notifyDataSetChanged();
        ((FolderListFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 2)).mAdapter.notifyDataSetChanged();
    }

    /**
     * Changing a folder after the Folder Editor Activity has been ended on <code>RESULT_OK</code>.
     *
     * @param data Intent return data containing folder data.
     */
    private void onEditFolderRequest(Intent data) {
        if (data == null) return;
        String iconRes = data.getStringExtra("iconRes");
        int accent = data.getIntExtra("accent", Color.WHITE);
        final int fIndex = data.getIntExtra("folderIndex", mPager.getCurrentItem());
        String name = data.getStringExtra("folderName");
        mFolderStructure.folders.get(fIndex).folderIconRes = iconRes;
        mFolderStructure.setFolderName(this, fIndex, name);
        mFolderStructure.folders.get(fIndex).accentColor = accent;

        if (fIndex == mPager.getCurrentItem()) {
            ((ImageView) findViewById(R.id.revealLayout).findViewById(R.id.reveal_bg))
                    .setImageBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.welcome_header_small));
        }
        mFolderStructure.folders.get(fIndex).loadImage(this);

        ArrayList addedList = data.getParcelableArrayListExtra("appList");

        //Remove hidden apps
        addedList.removeAll(JsonHelper.loadHiddenAppList(this));

        //Add all new apps.
        for (Object addedItem : addedList) {
            LauncherApps launcherApps = (LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE);
            Intent intent = new Intent();
            intent.setComponent((ComponentName) addedItem);
            LauncherActivityInfo info = launcherApps.resolveActivity(intent, android.os.Process.myUserHandle());
            if (info == null) continue;
            mDrawerTree.doWithApplication(info, new DrawerTree.LoadedListener() {
                @Override
                public void onApplicationLoadingFinished(Application app) {
                    addAppToFolder(app, fIndex);
                }
            });
        }

        if (mFolderStructure.folders.get(fIndex).pages.size() == 0)
            mFolderStructure.folders.get(fIndex).pages.add(new FolderStructure.Page());

        if (fIndex == mPager.getCurrentItem()) {

            Palette.from(mFolderStructure.folders.get(fIndex).headerImage != null && !mFolderStructure.folders.get(fIndex).headerImage.isRecycled() ? mFolderStructure.folders.get(fIndex).headerImage : BitmapFactory.decodeResource(getResources(), R.drawable.welcome_header_small)).generate(new Palette.PaletteAsyncListener() {
                public void onGenerated(Palette p) {
                    mCurrentAccent = mFolderStructure.folders.get(fIndex).accentColor;
                }
            });
            revealColor(mFolderStructure.folders.get(fIndex).headerImage);
            mFloatingActionButton.setBackgroundTintList(ColorStateList.valueOf(accent));
            mIndicator.setBackgroundTintList(ColorStateList.valueOf(accent));
        }

        ((FolderListFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 2)).mAdapter.notifyDataSetChanged();
        JsonHelper.saveFolderStructure(this, mFolderStructure);

        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
        ((FolderPagerAdapter) mPager.getAdapter()).notifyPagesChanged();
    }

    /**
     * Called after returning from settings.<br/>
     * Here, everything is updated
     */
    public void onSettingsRequest() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected void onPreExecute() {
            }

            @Override
            protected Void doInBackground(Void... params) {
                updateHolder();
                if (updateAfterSettings) {
                    mFolderStructure = JsonHelper.loadFolderStructure(LauncherActivity.this, mDrawerTree, mHolder);
                    mDrawerTree.fullReload();
                }
                System.gc(); //TODO do we need that?
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                revealColor(mFolderStructure.folders.get(Math.min(mPager.getCurrentItem(), mPager.getAdapter().getCount() - 1)).headerImage);
                mAppBarPager.setAdapter(new AppBarPagerAdapter(getSupportFragmentManager()));
                refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
                if (mPager != null && mPager.getAdapter() != null)
                    ((FolderPagerAdapter) mPager.getAdapter()).update();
                mFolderListFragment = null;
            }
        }.execute();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case LauncherActivity.REQUEST_CHANGE_WALLPAPER:
                generateWallpaperPalette();
                break;
            case FolderEditorAddActivity.REQUEST_APP_LIST_MAIN:
                if (resultCode == RESULT_OK) {
                    onAddAppsRequest();
                }
                break;
            case FolderEditorActivity.REQUEST_ADD_FOLDER:
                onAddFolderRequest(data);
                break;
            case FolderEditorActivity.REQUEST_EDIT_FOLDER:
                onEditFolderRequest(data);
                break;
            case SettingsActivity.REQUEST_SETTINGS:
                onSettingsRequest();
                break;
            case QuickAppBar.REQUEST_ADD_QA:
                //Adding a QuickApp
                Log.e("call", "add quick app");
                if (resultCode == RESULT_OK) {
                    Application app = data.getParcelableExtra("app");
                    int index = data.getIntExtra("index", -1);
                    int res = data.getIntExtra("icon", -1);

                    if (index != -1)
                        ((ClockFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 1)).mQuickAppBar.addAnimated(res, app, index);
                } else {
                    ((ClockFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 1)).mQuickAppBar.endDrag();
                }
                break;
            case QuickAppBar.REQUEST_EDIT_QA:
                //Editing a QuickApp
                if (resultCode == RESULT_OK) {
                    int index = data.getIntExtra("index", -1);
                    int icon = data.getIntExtra("icon", -1);
                    if (index != -1)
                        ((ClockFragment) mAppBarPager.getAdapter().instantiateItem(mAppBarPager, 1)).mQuickAppBar.replaceIconDelayed(icon, index, 150);
                }
                break;
            case AppEditorActivity.REQUEST_EDIT_APP:
                //Editing an AppDrawer App
                if (resultCode == RESULT_OK) {
                    Application edited = AppEditorActivity.PassApp.softApp.get();
                    mDrawerTree.change(edited.packageName);
                    refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
                    ((FolderPagerAdapter) mPager.getAdapter()).update();
                }
                AppEditorActivity.PassApp.softApp.clear();
                break;
        }

        //Widget actions for after picking and configuring a Widget
        mAppWidgetContainer.onActivityResult(requestCode, resultCode, data, new AppWidgetContainer.OnWidgetCreatedListener() {
            @Override
            public void onWidgetCreated(final View widget, final int widgetId) {
                int height = mAppWidgetContainer.mAppWidgetManager.getAppWidgetInfo(widgetId).minHeight;
                int width = mAppWidgetContainer.mAppWidgetManager.getAppWidgetInfo(widgetId).minWidth;

                //grab current AppDrawer Grid
                final AppDrawerPageFragment frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).getCurrentPagerCard();
                mAppGrid = frag.mAppGrid;
                height = height + mAppGrid.getRowHeight();
                width = width + mAppGrid.getColumnWidth();
                final int rowSpan = Math.min(height / mAppGrid.getRowHeight(), mAppGrid.getRowCount());
                final int colSpan = Math.min(width / mAppGrid.getColumnWidth(), mAppGrid.getColumnCount());
                final Point addPoint = mAppGrid.findFirstFreeCell(rowSpan, colSpan);

                //The widget View.
                ClickableAppWidgetHostView v = (ClickableAppWidgetHostView) widget;
                final Widget d = new Widget(new DrawerObject.GridPositioning(0, 0, rowSpan, colSpan), widgetId, v.getAppWidgetInfo().provider);

                if (addPoint.x == 0 && addPoint.y == 0 && mAppGrid.isCellGridUsedFull(0, 0, rowSpan, colSpan)) {
                    //There is no place for the Widget. Asking if the user wants to put it onto another page.
                    new AlertDialog.Builder(LauncherActivity.this, R.style.DialogTheme).setTitle("No suitable place")
                            .setMessage("There is no suitable place in this page where the widget could fit. Would you like to append it at the end of this folder on a new page?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    addWidgetToNewPage(d);
                                    JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);
                                }
                            }).setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Snackbar.make(mCoordinatorLayout, "This widget could not be added.", Snackbar.LENGTH_SHORT).show();
                            mAppWidgetContainer.mAppWidgetHost.deleteAppWidgetId(widgetId);
                        }
                    }).setOnCancelListener(new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            Snackbar.make(mCoordinatorLayout, "This widget could not be added.", Snackbar.LENGTH_SHORT).show();
                            mAppWidgetContainer.mAppWidgetHost.deleteAppWidgetId(widgetId);
                        }
                    }).show();
                    return;
                }

                //Add and save it.
                d.mGridPosition.row = addPoint.x;
                d.mGridPosition.col = addPoint.y;
                mFolderStructure.folders.get(frag.mFolderPos).pages.get(frag.mPage).items.add(d);
                JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);
                mAppGrid.addObject(widget, d);
            }

            void addWidgetToNewPage(Widget widget) {
                //Adds a page with the widget on it.
                final FolderDrawerPageFragment frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem()));

                FolderStructure.Page newPage = new FolderStructure.Page();
                newPage.add(widget);
                mFolderStructure.folders.get(mPager.getCurrentItem()).pages.add(newPage);
                (frag.mPager.getAdapter()).notifyDataSetChanged();
                frag.mPager.setCurrentItem(frag.mPager.getAdapter().getCount() - 1);

            }

            @Override
            public void onShortcutCreated(final Shortcut shortcut) {
                //Like onWidgetCreated, just for shortcuts
                final AppDrawerPageFragment frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem())).getCurrentPagerCard();
                mAppGrid = frag.mAppGrid;
                final Point addPoint = mAppGrid.findFirstFreeCell(1, 1);
                shortcut.mGridPosition = new DrawerObject.GridPositioning(addPoint.x, addPoint.y, 1, 1);
                shortcut.createView(mAppGrid, (LayoutInflater.from(LauncherActivity.this)), new DrawerObject.OnViewCreatedListener() {
                    @Override
                    public void onViewCreated(View view) {
                        mAppGrid.addObject(view, shortcut);
                    }
                });
                mFolderStructure.folders.get(frag.mFolderPos).pages.get(frag.mPage).items.add(shortcut);
                JsonHelper.saveFolderStructure(LauncherActivity.this, mFolderStructure);
            }
        });
    }

    /**
     * Adds an app to the folder at the given index. Animated. if possible at least.
     *
     * @param app
     * @param folderIndex
     */
    public void addAppToFolder(final Application app, int folderIndex) {

        app.mGridPosition.row = Integer.MIN_VALUE;
        app.mGridPosition.col = Integer.MIN_VALUE;

        List<FolderStructure.Page> pages = mFolderStructure.folders.get(folderIndex).pages;

        //Check for another occurence of the app in this folder.
        for (FolderStructure.Page page : pages) {
            for (DrawerObject obj : page.items) {
                if (obj instanceof Application && ((Application) obj).packageName.equals(app.packageName) && ((Application) obj).className.equals(app.className)) {
                    Snackbar.make(mCoordinatorLayout, "This app is already in this folder.", Snackbar.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        int i = -1;
        FolderStructure.Page focusPage;
        FolderDrawerPageFragment frag = null;

        do {
            i++;
            focusPage = pages.get(i);
        } while (focusPage.items.isFull(mHolder.gridSize()) && i < pages.size() - 1);

        if (!focusPage.items.isFull(mHolder.gridSize())) {
            //When there is place, add it there.
            frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, folderIndex));
            //Scroll to page

            final CustomGridLayout grid = frag.mPager == null ? null : ((AppDrawerPageFragment) frag.mPager.getAdapter().instantiateItem(frag.mPager, i)).mAppGrid;

            if (grid != null) {
                final Point point = grid.findFirstFreeCell(1, 1);

                app.mGridPosition.row = point.x;
                app.mGridPosition.col = point.y;

                //Occupy for further addition processes.
                grid.occupyCells(app.mGridPosition.row, app.mGridPosition.col, 1, 1);

                app.createView(grid, (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE), new DrawerObject.OnViewCreatedListener() {
                    @Override
                    public void onViewCreated(View view) {
                        grid.addObject(view, app);
                    }
                });
            }
            focusPage.items.add(app);
        } else {
            //When there is no place, put it on a new page
            FolderStructure.Page newPage = new FolderStructure.Page();
            newPage.add(app);
            pages.add(newPage);
            frag = ((FolderDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, folderIndex));
            frag.mPager.getAdapter().notifyDataSetChanged();
        }


        /*TODO REMOVE
        if( frag!=null && frag.mPager!=null && Math.abs(mPager.getCurrentItem()-folderIndex)<2) {
            frag.mPager.getAdapter().notifyDataSetChanged();

            if(frag.mPager.getCurrentItem() != i)
                frag.mPager.setCurrentItem(i, true);
        }
        */
    }

    /**
     * Called when an app has been uninstalled.
     *
     * @param packageName The package containing the app(s)
     */
    public void onAppRemoved(final String packageName) {
        FileManager.deleteRecursive(new File(getCacheDir().getPath() + "/apps/" + packageName));

        mDrawerTree.remove(packageName);

        File dir = new File(getCacheDir().getPath() + FileManager.PATH_APP_CACHE + packageName);
        FileManager.deleteRecursive(dir);
        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);

        //Remove all occurences.
        mFolderStructure.iterateThrough(false, new FolderStructure.IteratorHelper() {
            @Override
            public void getObject(int folderIndex, FolderStructure.Folder folder, int pageIndex, FolderStructure.Page page, int itemIndex, DrawerObject object) {
                if (object instanceof Application && ((Application) object).packageName.equals(packageName)) {
                    page.items.remove(object);
                    mFolderStructure.removeAppPackage(new ComponentName(((Application) object).packageName, ((Application) object).className));
                }
            }
        });

        JsonHelper.saveFolderStructure(this, mFolderStructure);

        ((FolderPagerAdapter) mPager.getAdapter()).notifyPagesChanged();
    }

    /**
     * Called when an app has been installed.
     *
     * @param packageName the package containing the new app(s)
     */
    public void onAppAdded(final String packageName) {
        try {
            mCurrentIconPack.loadSelectedPackage(null, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDrawerTree.add(packageName);

        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
    }

    /**
     * Called when a list of apps has been installed
     *
     * @param packageNames all packageNames of the newly installed apps.
     */
    public void onAppsAdded(final String... packageNames) {

        List<LauncherActivityInfo> infos = new ArrayList<>();
        for (String pn : packageNames) {
            infos.addAll(((LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE)).getActivityList(pn, Process.myUserHandle()));
        }
        try {
            mCurrentIconPack.loadSelected(null, infos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDrawerTree.addAll(packageNames);

        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);

    }

    /**
     * Called when a list of apps has been removed
     *
     * @param packageNames all packageNames of the uninstalled apps
     */
    public void onAppsRemoved(String... packageNames) {
        mDrawerTree.removeAll(packageNames);

        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
    }


    /**
     * Called when an app has been changed (updated etc.)
     *
     * @param packageName the package containing the app(s)
     */
    public void onAppChanged(String packageName) {
        LauncherLog.d(TAG, "App changed: " + packageName);

        try {
            mCurrentIconPack.loadSelectedPackage(null, packageName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        mDrawerTree.change(packageName);

        refreshAllFolder(mHolder.gridHeight, mHolder.gridWidth);
    }

    /**
     * Refreshes the "All" folder
     *
     * @param rowCnt deprecated
     * @param colCnt deprecated
     */
    public void refreshAllFolder(@Deprecated final int rowCnt, @Deprecated final int colCnt) {
        //Get all unhidden apps.
        List<LauncherActivityInfo> infos = new ArrayList<>(mDrawerTree.getAppsWithoutHidden());

        if (mPreferences.getBoolean(Const.Defaults.TAG_HIDE_ALL, Const.Defaults.getBoolean(Const.Defaults.TAG_HIDE_ALL))) {
            //Remove apps that are in another folder if Hide-all is checked.
            for (int i = 0; i < infos.size(); i++) {
                LinkedList associations = mFolderStructure.mApplicationToFolderMapper.get(infos.get(i).getComponentName());
                if (associations != null && !associations.isEmpty()) {
                    infos.remove(i);
                    i--;
                }
            }
        }

        //Determine how the apps should be laid out.
        int maxAppsPerPage = rowCnt * colCnt;
        FolderStructure.Folder folder = mFolderStructure.getFolderWithName("All");
        int index = mFolderStructure.folders.indexOf(folder);
        folder.pages.clear();
        int allPageCount = (int) Math.ceil(infos.size() / (float) maxAppsPerPage);

        //Add 'em all!!!
        for (int i = 0; i < allPageCount; i++) {
            final FolderStructure.Page page = new FolderStructure.Page();
            for (int j = 0; j < maxAppsPerPage && (i * maxAppsPerPage + j) < infos.size(); j++) {
                //Log.d(TAG, "Pos: "+j);
                LauncherActivityInfo item = infos.get(i * maxAppsPerPage + j);
                Application app = new Application(item);
                app.mGridPosition.row = j / colCnt;
                app.mGridPosition.col = j % colCnt;
                page.add(app);
            }
            folder.add(page);
        }
        mFolderStructure.folders.set(index, folder);
        //...and save
        JsonHelper.saveFolderStructure(this, mFolderStructure);

        if (mPager != null && mPager.getAdapter() != null) {
            try {
                //Aaand update
                ((AppDrawerPagerAdapter) ((FolderDrawerPageFragment) (mPager.getAdapter())
                        .instantiateItem(mPager, mFolderStructure.getFolderIndexOfName("All"))).mPager.getAdapter()).update();
            } catch (Exception e) {
                ExceptionLog.w(e);
            }
        }
    }

    /**
     * Make a fluid reveal animation with the given background Bitmap
     */
    public void revealColor(final Bitmap background) {
        final FrameLayout vContainer = (FrameLayout) findViewById(R.id.revealLayout);
        final ImageView bg = (ImageView) vContainer.findViewById(R.id.reveal_bg);
        final ImageView vColorView = new ImageView(this);

        vColorView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        vColorView.setImageDrawable(new BitmapDrawable(getResources(), background));
        vColorView.setAlpha(0f);

        vContainer.addView(vColorView);

        //Wait until fully added.
        vColorView.post(new Runnable() {
            @Override
            public void run() {
                int[] revealCenter = mFolderListFragment == null ?
                        new int[]{(vColorView.getRight() + vColorView.getLeft()), (vColorView.getTop() + vColorView.getBottom())} :
                        mFolderListFragment.getRevealXCenter(mPager.getCurrentItem(), new int[]{(vColorView.getRight() + vColorView.getLeft()) / 2, (vColorView.getTop() + vColorView.getBottom()) / 2});

                int centerX = mAppBarPager.getCurrentItem() != 2 ?
                        (vColorView.getRight() + vColorView.getLeft()) / 2 : revealCenter[0];
                int centerY = mAppBarPager.getCurrentItem() != 2 ?
                        (vColorView.getTop() + vColorView.getBottom()) / 2 : revealCenter[1];

                try {
                    Animator vColorViewAnimator = ViewAnimationUtils.createCircularReveal(vColorView, centerX, centerY, mAppBarPager.getCurrentItem() == 2 ? LauncherUtils.dpToPx(20, LauncherActivity.this) : 0, vColorView.getWidth());

                    vColorViewAnimator.addListener(new Animator.AnimatorListener() {
                        @Override
                        public void onAnimationStart(Animator animation) {
                            vColorView.setAlpha(1f);
                        }

                        @Override
                        public void onAnimationEnd(Animator animation) {
                            bg.setImageDrawable(new BitmapDrawable(getResources(), background));
                            vContainer.removeView(vColorView);
                        }

                        @Override
                        public void onAnimationCancel(Animator animation) {

                        }

                        @Override
                        public void onAnimationRepeat(Animator animation) {

                        }
                    });

                    AnimatorSet vRevealSet = new AnimatorSet();
                    vRevealSet.playTogether(vColorViewAnimator);
                    vRevealSet.setDuration(800);

                    vRevealSet.start();
                } catch (Exception e) {
                    bg.setImageDrawable(new BitmapDrawable(getResources(), background));
                    vContainer.removeView(vColorView);
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * @return true, if the user is currently looking at the "All" folder
     */
    public boolean isInAllFolder() {
        return mPager != null && mPager.getCurrentItem() == mFolderStructure.folders.indexOf(mFolderStructure.getFolderWithName("All"));
    }

    /**
     * Checks, if the app has been started before. If not, start the {@link WelcomeActivity}.
     *
     * @return true, if its the first start.
     */
    public boolean checkFirstStart() {
        final FrameLayout infoLayout = (FrameLayout) findViewById(R.id.infoOverlay);
        infoLayout.setClickable(false);
        if (mHolder.isFirstStart) {
            //Clear WidgetHost, so it does not fill up unnecessarily
            mAppWidgetContainer.mAppWidgetHost.deleteHost();
            Intent intent = new Intent(this, WelcomeActivity.class);
            LauncherUtils.startActivityForResult(this, mAppBarPager, intent, WelcomeActivity.REQUEST_FIRST_START);
            finish();
            return true;
        }
        return false;
    }
}