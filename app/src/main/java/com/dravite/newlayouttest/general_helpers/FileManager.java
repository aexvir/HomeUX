package com.dravite.newlayouttest.general_helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

/**
 * A helper file that helps interacting with the internal memory. (As in saving Bitmaps or checking for file existence)
 */
public class FileManager {

    public static final String PATH_APP_CACHE = "/apps/";

    /**
     * Checks, if a given file exists.
     * @param context The current Context
     * @param cacheAppendix Add this to the app cache path
     * @param fileName This is your file to check for
     * @return true, if the file exists, false otherwise.
     */
    public static boolean fileExists(Context context, String cacheAppendix, String fileName){
        if(context == null)
            return false;
        File cacheDir = context.getCacheDir();
        File f = new File(cacheDir.getPath()+"/"+cacheAppendix, fileName);
        return f.exists();
    }



    /**
     * Saves a bitmap to the cache.
     * @param context The current context
     * @param bitmap Save this bitmap
     * @param cacheAppendix Add this to the app cache path
     * @param fileName This is your file to save to
     */
    public static void saveBitmap(final Context context, final Bitmap bitmap, final String cacheAppendix,final  String fileName){
                if(bitmap==null)
                    return;
                File cacheDir = context.getCacheDir();
                File dir = new File(cacheDir.getPath()+cacheAppendix);
                File f = new File(cacheDir.getPath()+cacheAppendix, fileName);

                dir.mkdirs();

                try {
                    FileOutputStream out = new FileOutputStream(
                            f);
                    bitmap.compress(
                            Bitmap.CompressFormat.PNG,
                            100, out);
                    out.flush();
                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

    }

    /**
     * Saves a bitmap to the appData/folderImg folder.
     * @param context The current Context
     * @param bitmap Save this bitmap
     * @param fileName This is your file to save to
     * @param force set to true to overwrite old files
     */
    public static void saveBitmapToData(final Context context, final Bitmap bitmap, final  String fileName, boolean force){
        if(bitmap==null)
            return;
        File dir = new File(context.getApplicationInfo().dataDir + "/folderImg/");
        File f = new File(dir.getPath(), fileName);

        if(!force && f.exists())
            return;

        dir.mkdirs();

        try {
            FileOutputStream out = new FileOutputStream(
                    f);
            bitmap.compress(
                    Bitmap.CompressFormat.PNG,
                    100, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * Deletes a full directory
     * @param fileOrDirectory The directory to be deleted
     */
    public static boolean deleteRecursive(File fileOrDirectory) {
        if (fileOrDirectory.isDirectory())
            for (File child : fileOrDirectory.listFiles())
                deleteRecursive(child);

        return fileOrDirectory.delete();
    }

    /**
     * Loads a bitmap from the appData/folderImg folder.
     * @param context The current Context
     * @param fileName Load from this file
     * @return The loaded Bitmap
     */
    public static Bitmap loadBitmapFromData(Context context, String fileName){
        File cacheDir = new File(context.getApplicationInfo().dataDir + "/folderImg/");
        File f = new File(cacheDir.getPath()+"/", fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(fis);
    }

    public static boolean renameBitmapFromData(Context context, String oldName, String newName){
        File cacheDir = new File(context.getApplicationInfo().dataDir + "/folderImg/");
        File f = new File(cacheDir.getPath()+"/", oldName);
        File fNew = new File(cacheDir.getPath()+"/", newName);

        return f.renameTo(fNew);
    }

    /**
     * Loads a bitmap from app cache.
     * @param context The current Context
     * @param cacheAppendix Append this to the cache path
     * @param fileName Load from this file
     * @return The loaded Bitmap
     */
    public static Bitmap loadBitmap(Context context, String cacheAppendix, String fileName){
        File cacheDir = context.getCacheDir();
        File f = new File(cacheDir.getPath()+"/"+cacheAppendix, fileName);
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(f);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return BitmapFactory.decodeStream(fis);
    }

    /**
     * Saves a text file to the cache.
     * @param context The current Context
     * @param cacheAppendix Append this to the cache path
     * @param fileName Save to this file
     * @param content String to save
     */
    public static void saveTextFile(Context context, String cacheAppendix, String fileName, String content){
        File cacheDir = context.getCacheDir();
        File toCreate = new File(cacheDir.getAbsolutePath() + cacheAppendix);
        toCreate.mkdirs();
        File file = new File (toCreate, fileName);

        try{
            FileOutputStream f = new FileOutputStream(file);
            f.write(content.getBytes());
            f.close();
        } catch (FileNotFoundException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    /**
     * Loads a String from a cached text file
     * @param context The current Context
     * @param cacheAppendix Append this to the cache path
     * @param fileName Load from this file
     * @return The String contained in the file
     */
    public static String readTextFile(Context context, String cacheAppendix, String fileName){
        File cacheDir = context.getCacheDir();
        File file = new File (cacheDir.getAbsolutePath() + cacheAppendix, fileName);

        StringBuilder text = new StringBuilder();

        try {
            BufferedReader br = new BufferedReader(new FileReader(file));
            String line;

            while ((line = br.readLine()) != null) {
                text.append(line);
                text.append('\n');
            }
            br.close();
        }
        catch (IOException e) {
            e.printStackTrace();
            return "";
        }
        return text.toString();
    }
}
