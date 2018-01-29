package com.dravite.newlayouttest.general_helpers;

import android.app.AlertDialog;
import android.content.Context;
import android.util.Log;

import com.dravite.newlayouttest.LauncherLog;
import com.dravite.newlayouttest.R;

/**
 * Helper for try{}catch(e){} blocks to not spam around stackTraces.
 */
public class ExceptionLog {
    private static final String TAG = "HomeUX Exception";

    public static void w(Exception e){
        LauncherLog.d(TAG, e.getMessage());
    }

    public static void e(Exception e){
        LauncherLog.w(TAG, e.getMessage());
    }

    public static void throwErrorMsg(Context context, String message){
        new AlertDialog.Builder(context, R.style.DialogTheme).setTitle(TAG)
                .setMessage("An error occurred:\n\n" + message)
                .setPositiveButton(android.R.string.ok, null)
                .show();
    }
}
