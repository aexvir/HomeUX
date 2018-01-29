package com.dravite.homeux.drawerobjects.structures;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.BitmapFactory;
import android.os.Process;

import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.general_helpers.FileManager;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.helpers.AppLauncherActivityInfoComparator;
import com.dravite.homeux.general_helpers.JsonHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeMap;

/**
 * Contains all apps in RB-Trees for them to be found and created quickly.
 */
public class DrawerTree {

    private static final String TAG = "DrawerTree";

    // A ParallelExecutor with 6 threads working on loading apps.
    //public ParallelExecutor mApplicationLoadingExecutor = new ParallelExecutor(4);

    /**
     * This listener is triggered as soon as an app has finished loading.
     */
    public interface LoadedListener{
        void onApplicationLoadingFinished(Application app);
    }

    // A global comparator which compares LauncherActivityInfos
    public final AppLauncherActivityInfoComparator mComparator;

    // A RB-Tree of all the apps (fast key access)
    // format: <label:packageName:className, app>
    private final TreeMap<String, Application> mFullAppList = new TreeMap<>();

    // A map of the app-dependent LoadingListener-Lists (same format as above, except the list of course)
    private final TreeMap<String, List<LoadedListener>> mPendingListeners = new TreeMap<>();

    // All the LauncherActivityInfos of installed apps
    private List<LauncherActivityInfo> infoList;

    private final LauncherApps mLauncherApps;
    private final Context mContext;

    /**
     * The constructor just initializes general Variables, as well as the LauncherActivityInfo list.
     * @param context The current Context
     */
    public DrawerTree(Context context){
        //Fetch all Launcher Apps
        mLauncherApps = (LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE);
        //infoList = mLauncherApps.getActivityList(null, Process.myUserHandle());
        mComparator = new AppLauncherActivityInfoComparator(context);
        //Collections.sort(infoList, mComparator);
        mContext = context;
    }

    /**
     * Fully refreshes and reloads the app list
     */
    public void fullReload(){
        mFullAppList.clear();
        mPendingListeners.clear();
        if(infoList!=null)
            infoList.clear();
        infoList = mLauncherApps.getActivityList(null, Process.myUserHandle());
        mComparator.refresh();
        Collections.sort(infoList, mComparator);
        loadApps();
    }

    /**
     * Reloads the app list when a package has been removed
     * @param packageName the removed package
     */
    public void remove(String packageName){
        List<String> toRemove = new ArrayList<>();
        for (String key:mFullAppList.keySet()) {
            if(key.split(":")[1].split("/")[0].endsWith(packageName)){
                toRemove.add(key);
            }
        }
        for (String s:toRemove){
            mFullAppList.remove(s);
        }
        infoList.clear();
        infoList = mLauncherApps.getActivityList(null, Process.myUserHandle());
        Collections.sort(infoList, mComparator);
    }

    /**
     * reloads the app list when a package has been added
     * @param packageName the added package
     */
    public void add(String packageName){
        List<LauncherActivityInfo> addedApps = mLauncherApps.getActivityList(packageName, Process.myUserHandle());

        infoList = mLauncherApps.getActivityList(null, Process.myUserHandle());
        Collections.sort(infoList, mComparator);

        loadApps(addedApps);
    }

    /**
     * reloads the app list when a package has been changed
     * @param packageName the changed package
     */
    public void change(String packageName){
        List<LauncherActivityInfo> addedApps = mLauncherApps.getActivityList(packageName, Process.myUserHandle());

        infoList = mLauncherApps.getActivityList(null, Process.myUserHandle());
        mComparator.refresh();
        Collections.sort(infoList, mComparator);

        loadApps(addedApps);
    }

    /**
     * reloads the app list when multiple packages has been removed
     * @param packageNames an array of the removed packages
     */
    public void removeAll(String... packageNames){
        for (String pn:packageNames) {
            remove(pn);
        }
    }

    /**
     * reloads the app list when multiple packages has been added
     * @param packageNames an array of the added packages
     */
    public void addAll(String... packageNames){
        List<LauncherActivityInfo> addedApps = new ArrayList<>();
        for (String pn: packageNames) {
            addedApps.addAll(mLauncherApps.getActivityList(pn, Process.myUserHandle()));
        }
        infoList = mLauncherApps.getActivityList(null, Process.myUserHandle());
        Collections.sort(infoList, mComparator);

        loadApps(addedApps);
    }

    /**
     * Loads an app synchronously (on the thread on which it has been started on)
     * @param info The app to load.
     */
    private void loadAppSync(final LauncherActivityInfo info){
        final Application app = new Application(info);
        String key = app.loadLabel(mContext) + ":" + info.getComponentName().toString();

   //     do{
            //Try to load the icon 5 times before cancelling when its not being loaded correctly.

//            if(app.icon!=null){
//                app.icon.recycle();
//            }
            app.icon = app.loadIcon(mContext, info);

      //  } while ((app.icon==null || app.icon.getHeight()<0  || app.icon.getWidth()<0) && i<=5);
        if(app.icon==null || app.icon.getHeight()<0  || app.icon.getWidth()<0)
            app.icon = BitmapFactory.decodeResource(mContext.getResources(), R.mipmap.ic_launcher);
        mFullAppList.put(key, app);
       // String key = app.loadLabel(mContext) + ":" + app.info.getComponentName().toString();
        if (mPendingListeners.get(key) != null) {
            for (final LoadedListener listener : mPendingListeners.get(key)) {
                ((LauncherActivity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        listener.onApplicationLoadingFinished(app);
                    }
                });
            }
            mPendingListeners.get(key).clear();
            mPendingListeners.remove(key);
        }
    }

    /**
     * Changes the label of an app. It only changes the apps search key, not on the file system.
     * @param oldLabel The old label
     * @param componentName The apps componentName.toString().
     * @param newLabel The new label
     */
    public void changeLabel(String oldLabel, String componentName, String newLabel){
        Application tmp = mFullAppList.remove(oldLabel+":"+componentName);
        mFullAppList.put(newLabel+":"+componentName, tmp);
        mComparator.refresh();
        Collections.sort(infoList, mComparator);
    }

    /**
     * Loads an app in a parallelized worker executor
     * @param info the app to be loaded
     */
    private void loadApp(final LauncherActivityInfo info){
        LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
            @Override
            public void run() {
                loadAppSync(info);
            }
        });
    }

    /**
     * Loads a list of apps in a parallelized worker executor
     * @param infList the app list to be loaded
     */
    private void loadApps(final List<LauncherActivityInfo> infList){
        final List<LauncherActivityInfo> paramInfoList = new ArrayList<>(infList);
        //go through infos
        for (final LauncherActivityInfo info: paramInfoList) {
            //Load each app asynchronously
            loadApp(info);

        }
    }

    /**
     * Loads all apps
     */
    private void loadApps(){
        loadApps(infoList);
    }

    /**
     * (For searching) Gets all applications which start with a given String.
     * @param start The string to start with
     * @return a list of apps starting with the value of start
     */
    public List<Application> getApplicationsStartingWith(String start){
        //Don't search for an empty String as it would return the whole app list.
        //Just return an empty list instead
        if(start.equals(""))
            return new ArrayList<>();
        List<Application> resultList = new ArrayList<>();
        for (Application application:mFullAppList.values()) {
            if(application.loadLabel(mContext).toLowerCase().startsWith(start.toLowerCase()))
                resultList.add(application);

        }
        return resultList;
    }

    /**
     * (For searching) Gets all applications containing a given String.
     * @param start The string to start with
     * @return a list of apps starting with the value of start
     */
    public List<Application> getApplicationsContaining(String start){
        //Don't search for an empty String as it would return the whole app list.
        //Just return an empty list instead
        if(start.equals(""))
            return new ArrayList<>();
        List<Application> resultList = new ArrayList<>();
        for (Application application:mFullAppList.values()) {
            if(application.loadLabel(mContext).toLowerCase().contains(start.toLowerCase()))
                resultList.add(application);

        }
        return resultList;
    }

    /**
     * Runs {@link #doWithApplication(LauncherActivityInfo, LoadedListener)}. With the LauncherActivityInfo at the given index.
     * @param index The LauncherActivityInfo index
     * @param listener A LoadedListener for when the app has been loaded.
     */
    public void doWithApplication(int index, LoadedListener listener){
        LauncherActivityInfo info = infoList.get(index);
        doWithApplication(info, listener);
    }

    /**
     * Runs {@link #doWithApplication(String, LauncherActivityInfo, LoadedListener)} in a parallelized worker executor with a key generated by a given LauncherActivityInfo.
     * @param info The LauncherActivityInfo
     * @param listener A LoadedListener for when the app has been loaded.
     */
    public void doWithApplication(final LauncherActivityInfo info, final LoadedListener listener){
        /*mApplicationLoadingExecutor.enqueue(new Runnable() {
            @Override
            public void run() {*/
                String label = info.getLabel().toString();
                if(FileManager.fileExists(mContext,  FileManager.PATH_APP_CACHE + info.getComponentName().getPackageName(), info.getComponentName().getClassName())) {
                    label =  FileManager.readTextFile(mContext, FileManager.PATH_APP_CACHE + info.getComponentName().getPackageName(), info.getComponentName().getClassName());
                }
                final String key = label + ":" + info.getComponentName().toString();
                        doWithApplication(key, info, listener);
            //}/*
        //});
    }

    public void doWithApplicatioByLabel(final String label, final LauncherActivityInfo info, final LoadedListener listener){
        final String key = label + ":" + info.getComponentName().toString();
        doWithApplication(key, info, listener);
    }

    /**
     * First check if the app has already been loaded (and returns it if so). If not, add the listener to the listener queue of the given application. Then load the app parallelized.
     * @param key The application map search key
     * @param info The LauncherActivityInfo
     * @param listener A LoadedListener for when the app has been loaded.
     */
    private void doWithApplication(String key, LauncherActivityInfo info, LoadedListener listener){
        if(mFullAppList.containsKey(key)){
            listener.onApplicationLoadingFinished(mFullAppList.get(key));
            return;
        }

        //ELSE
        if(mPendingListeners.containsKey(key)){
            mPendingListeners.get(key).add(listener);
        } else {
            ArrayList<LoadedListener> listenerList = new ArrayList<>();
            listenerList.add(listener);
            mPendingListeners.put(key, listenerList);
        }

        //TODO test stuff...
        loadApp(info);
    }

    /**
     * @return How many applications are installed on the device
     */
    public int getApplicationCount(){
        return infoList.size();
    }

    /**
     * Removes all hidden apps before returning the app list.
     * @return A list of all the apps excluding the hidden ones.
     */
    public List<LauncherActivityInfo> getAppsWithoutHidden(){
        ArrayList<LauncherActivityInfo> newInfos = new ArrayList<>(infoList);
        List<ComponentName> componentNames = JsonHelper.loadHiddenAppList(mContext);

        for (int i = 0; i < newInfos.size(); i++) {
            if(componentNames.contains(newInfos.get(i).getComponentName())){
                newInfos.remove(i);
                i--;
            }
        }
        return newInfos;
    }

    /**
     * Removes apps contained in another folder than "All" from a given LauncherActivityInfo list.
     * @param infoList The LauncherActivityInfo list
     * @param folderStructure The full FolderStructure
     */
    public void removeAppsFromOtherFolder(List<LauncherActivityInfo> infoList, FolderStructure folderStructure){


//        for (FolderStructure.Folder folder: folderStructure.folders) {
//            if(!folder.folderName.equals("All")) {
//                for (FolderStructure.Page page : folder.pages) {
//                    for (DrawerObject object : page.items) {
//                        if(object instanceof Application){
//                            for (int i = 0; i < infoList.size(); i++) {
//                                if(infoList.get(i).getComponentName().equals(new ComponentName(((Application) object).packageName, ((Application) object).className))){
//                                    infoList.remove(i);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
    }

}
