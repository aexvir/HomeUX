package com.dravite.homeux.drawerobjects.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;

import com.dravite.homeux.general_helpers.FileManager;

import java.io.File;
import java.util.Comparator;
import java.util.HashMap;

/**
 * A Comparator that compares two LauncherActivityInfos
 */
public class AppLauncherActivityInfoComparator implements Comparator<LauncherActivityInfo> {

    private Context mContext;
    private HashMap<ComponentName, String> mCustomLabelMap = new HashMap<>();

    public AppLauncherActivityInfoComparator(Context context){
        mContext = context;
    }

    public void refresh(){
        //Clearing all old entries
        mCustomLabelMap.clear();
        //Opening AppCache directory
        File customDir = new File(mContext.getCacheDir().getPath() + "/" + FileManager.PATH_APP_CACHE);

        //Those folders are named after their according package names
//        String[] subDirs = customDir.list();
//        for (String name :
//                subDirs) {
//            //Open the folder containing custom files
//            File thisFolder = new File(customDir.getPath() + "/" + name);
//            //Filter for files ending on "_l"
//            String[] files = thisFolder.list(new FilenameFilter() {
//                @Override
//                public boolean accept(File dir, String filename) {
//                    return filename.toLowerCase().endsWith("_l");
//                }
//            });
//
//            if(files.length>0){
//                //If there are any label files
//                for (String file :
//                        files) {
//                    //Add to customLabelList
//                    //Set the component class to the filename, but remove the "_l"
//                    ComponentName componentName = new ComponentName(name, file.replaceFirst("(?s)(.*)" + "_l", "$1" + ""));
//                    mCustomLabelMap.put(componentName, readLabelFile(componentName));
//                }
//            }
//        }

    }

    String readLabelFile(ComponentName componentName){
        return  FileManager.readTextFile(mContext, FileManager.PATH_APP_CACHE + componentName.getPackageName(), componentName.getClassName() + "_l");
    }

    String loadedLabel, loadedLabel2;

    @Override
    public int compare(LauncherActivityInfo lhs, LauncherActivityInfo rhs) {

        loadedLabel = mCustomLabelMap.get(lhs.getComponentName());

        /*if(FileManager.fileExists(mContext,  FileManager.PATH_APP_CACHE + lhs.getComponentName().getPackageName(), lhs.getComponentName().getClassName() + "_l")) {
            lhsLabel = FileManager.readTextFile(mContext, FileManager.PATH_APP_CACHE + lhs.getComponentName().getPackageName(), lhs.getComponentName().getClassName() + "_l");
        }*/

        loadedLabel2 = mCustomLabelMap.get(rhs.getComponentName());

        /*if(FileManager.fileExists(mContext,  FileManager.PATH_APP_CACHE + rhs.getComponentName().getPackageName(), rhs.getComponentName().getClassName() + "_l")) {
            rhsLabel = FileManager.readTextFile(mContext, FileManager.PATH_APP_CACHE + rhs.getComponentName().getPackageName(), rhs.getComponentName().getClassName() + "_l");
        }*/

        return (loadedLabel==null?lhs.getLabel().toString():loadedLabel).toLowerCase().compareTo((loadedLabel2==null?rhs.getLabel().toString():loadedLabel2).toLowerCase());
    }
}
