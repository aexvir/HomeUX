package com.dravite.homeux;

import java.util.Collections;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A class just containing a load of constant values.
 */
public class Const {
    /** Determines, how much smaller the app grid will get when dragging a {@link com.dravite.homeux.drawerobjects.DrawerObject}. */
    public static final float APP_GRID_ZOOM_OUT_SCALE = 0.94f;
    public static int ICON_SIZE = 32;

    /**
     * Contains a sorted map of all SharedPreferences and their default values
     */
    public static class Defaults {

        public static final String TAG_APP_WIDTH = "appwidth";
        public static final String TAG_APP_HEIGHT = "appheight";
        public static final String TAG_ICON_SIZE = "iconsize";
        public static final String TAG_SHOW_LABELS = "showLabels";
        public static final String TAG_NOTIFICATIONS = "notifications";
        public static final String TAG_NOTIFICATIONS_RADIUS = "noticornerradius";
        public static final String TAG_NOTIFICATIONS_PADDING = "notipadding";
        public static final String TAG_NOTIFICATIONS_BACKGROUND_COLOR = "notibackgroundcolor";
        public static final String TAG_NOTIFICATIONS_TEXT_COLOR = "notitextcolor";
        public static final String TAG_HIDE_CARDS = "hidecards";
        public static final String TAG_DISABLE_WALLPAPER_SCROLL = "disablewallpaperscroll";
        public static final String TAG_TRANSFORMER_INT = "transformerINT";
        public static final String TAG_TRANSFORMER = "transformer";
        public static final String TAG_PANEL_TRANS = "panelTransparency";
        public static final String TAG_TRANSP_STATUS = "transpStatus";
        public static final String TAG_DEFAULT_FOLDER = "defaultFolder";
        public static final String TAG_QA_FAB = "qa_fab";
        public static final String TAG_QA_FAB_PKG = "qa_fab_pkg";
        public static final String TAG_QA_FAB_CLS = "qa_fab_cls";
        public static final String TAG_QA_ICON = "qaIcon";
        public static final String TAG_SWITCH_CONFIG = "switchConfig";
        public static final String TAG_AM_PM = "ampm";
        public static final String TAG_CENTER_CLOCK = "centerclock";
        public static final String TAG_CLOCK_FONT = "clock_font";
        public static final String TAG_CLOCK_SHOW = "show_clock";
        public static final String TAG_CLOCK_BOLD = "clock_bold";
        public static final String TAG_CLOCK_ITALIC = "clock_italic";
        public static final String TAG_CLOCK_SIZE = "clock_size";
        public static final String TAG_CLOCK_SIZE_INT = "clock_sizeINT";
        public static final String TAG_LICENSED = "isLicensed";
        public static final String TAG_FIRST_START = "firstStart";
        public static final String TAG_HAS_SHOWN_MESSAGE = "hasShownMessage";
        public static final String TAG_ICON_PACK = "iconPack";
        /*TODO*/ public static final String TAG_HIDE_ALL = "hideall";
        public static final String TAG_DIRECT_REVEAL = "directReveal";

        private static final SortedMap<String, Object> mDefaults;
        static {
            TreeMap<String, Object> defMap = new TreeMap<>();

            defMap.put(TAG_ICON_SIZE, 3);
            defMap.put(TAG_SHOW_LABELS, true);
            defMap.put(TAG_TRANSFORMER_INT, 3);
            defMap.put(TAG_DEFAULT_FOLDER, "All");
            defMap.put(TAG_QA_FAB, "none");
            defMap.put(TAG_QA_ICON, "ic_search_black_24dp");
            defMap.put(TAG_SWITCH_CONFIG, true);
            defMap.put(TAG_CENTER_CLOCK, true);
            defMap.put(TAG_HIDE_CARDS, false);
            defMap.put(TAG_CLOCK_SIZE, "Medium");
            defMap.put(TAG_CLOCK_SIZE_INT, 1);
            defMap.put(TAG_CLOCK_FONT, "sans-serif-medium");
            defMap.put(TAG_PANEL_TRANS, 1.0f);
            defMap.put(TAG_FIRST_START, true);
            defMap.put(TAG_QA_FAB_CLS, "");
            defMap.put(TAG_QA_FAB_PKG, "");
            defMap.put(TAG_NOTIFICATIONS, false);
            defMap.put(TAG_NOTIFICATIONS_RADIUS, 32);
            defMap.put(TAG_NOTIFICATIONS_PADDING, 6);
            defMap.put(TAG_NOTIFICATIONS_BACKGROUND_COLOR, R.color.dark_gray);
            // TODO: Define correct color
            defMap.put(TAG_NOTIFICATIONS_TEXT_COLOR, R.color.cardview_light_background);
            defMap.put(TAG_ICON_PACK, "");
            defMap.put(TAG_HIDE_ALL, false);
            defMap.put(TAG_LICENSED, false);
            defMap.put(TAG_HAS_SHOWN_MESSAGE, false);
            defMap.put(TAG_TRANSFORMER, "default");
            defMap.put(TAG_TRANSP_STATUS, false);
            defMap.put(TAG_AM_PM, false);
            defMap.put(TAG_CLOCK_BOLD, false);
            defMap.put(TAG_CLOCK_ITALIC, false);
            defMap.put(TAG_DISABLE_WALLPAPER_SCROLL, true);
            defMap.put(TAG_DIRECT_REVEAL, false);
            defMap.put(TAG_CLOCK_SHOW, true);

            mDefaults = Collections.unmodifiableSortedMap(defMap);
        }

        public static Object get(String key){
            return mDefaults.get(key);
        }

        public static boolean getBoolean(String key){
            return (boolean)get(key);
        }

        public static int getInt(String key){
            return (int)get(key);
        }

        public static String getString(String key){
            return (String)get(key);
        }

        public static float getFloat(String key){
            return (float)get(key);
        }
    }
}
