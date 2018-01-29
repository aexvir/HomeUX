package com.dravite.homeux;

import android.util.Log;

/**
 * Created by johannesbraun on 22.06.16.
 * Logging utilities for being able to turn on/off debug logs with one boolean.
 */
public class LauncherLog {

    /**enables/disables debug logs*/
    public static final boolean DEBUG = false;

    /**
     * Prints a warning log.
     * @param c This classes name string will be used as log tag.
     * @param s The log message
     */
    public static void w(Class c, String s){
        w(c.getName(), s);
    }

    /**
     * Same as {@link Log#w(String, String)}.
     */
    public static void w(String tag, String msg){
        if(DEBUG){
            Log.w(tag, msg);
        }
    }

    /**
     * Prints a debug log.
     * @param c This classes name string will be used as log tag.
     * @param s The log message
     */
    public static void d(Class c, String s){
        d(c.getName(), s);
    }

    /**
     * Same as {@link Log#d(String, String)}.
     */
    public static void d(String tag, String msg){
        if(DEBUG){
            Log.d(tag, msg);
        }
    }
}
