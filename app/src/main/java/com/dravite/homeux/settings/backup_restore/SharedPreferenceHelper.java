package com.dravite.homeux.settings.backup_restore;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Helps loading and saving the default SharedPreferences to the file system.
 */
public class SharedPreferenceHelper {

    /**
     * Saves default SharedPreferences to a given File
     * @param context The current context
     * @param dst The file to save the preferences to
     * @return If the preferences are saved correctly
     */
    public static boolean saveSharedPreferencesToFile(Context context, File dst) {
        boolean res = false;
        ObjectOutputStream output = null;
        try {
            res = dst.getParentFile().mkdirs();
            res |= dst.createNewFile();
            output = new ObjectOutputStream(new FileOutputStream(dst));
            SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
            output.writeObject(pref.getAll());

            res = true;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (output != null) {
                    output.flush();
                    output.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

    /**
     * Loads default SharedPreferences from a given File
     * @param context The current Context
     * @param src The file to load the preferences from
     * @return If the preferences has been loaded successfully
     */
    @SuppressWarnings({ "unchecked" })
    public static boolean loadSharedPreferencesFromFile(Context context, File src) {
        boolean res = false;
        ObjectInputStream input = null;
        try {
            input = new ObjectInputStream(new FileInputStream(src));
            SharedPreferences.Editor prefEdit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            prefEdit.clear();
            Map<String, ?> entries = (Map<String, ?>) input.readObject();
            for (Map.Entry<String, ?> entry : entries.entrySet()) {
                Object v = entry.getValue();
                String key = entry.getKey();

                if (v instanceof Boolean)
                    prefEdit.putBoolean(key, (boolean) v);
                else if (v instanceof Float)
                    prefEdit.putFloat(key, (float)v);
                else if (v instanceof Integer)
                    prefEdit.putInt(key, (int)v);
                else if (v instanceof Long)
                    prefEdit.putLong(key, (long)v);
                else if (v instanceof String)
                    prefEdit.putString(key, ((String) v));
            }
            prefEdit.apply();
            res = true;
        } catch (ClassNotFoundException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return res;
    }

}
