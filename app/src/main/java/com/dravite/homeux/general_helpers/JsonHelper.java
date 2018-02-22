package com.dravite.homeux.general_helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.views.QuickAppIcon;
import com.dravite.homeux.views.QuickAppBar;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.structures.DrawerTree;
import com.dravite.homeux.drawerobjects.QuickAction;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for saving and loading all kinds of stuff to and from json files.
 */
public class JsonHelper {

    //private static ParallelExecutor mImageSaveExecutor = new ParallelExecutor(4);

    /**
     * A serializable ComponentName equivalent
     */
    public static class SavableComponent implements Serializable{
        public String pkg, cls;

        public SavableComponent(){

        }

    }

    /**
     * Saves a list of ComponentNames as hidden apps to a json file.
     * @param context The current Context
     * @param hiddenComponents a list of hidden app components
     */
    public static void saveHiddenAppList(final Context context, final List<ComponentName> hiddenComponents){
        try {
            List<SavableComponent> components = new ArrayList<>();
            for (ComponentName comp :
                    hiddenComponents) {
                SavableComponent component = new SavableComponent();
                component.pkg = comp.getPackageName();
                component.cls = comp.getClassName();
                components.add(component);
            }
            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
            mapper.writeValue(new File(context.getApplicationInfo().dataDir + "/hiddenApps.json"), components);
        } catch (IOException e){
            ExceptionLog.w(e);
        }
    }

    /**
     * loads the hiddenApps.json file to a list of ComponentNames as hidden apps
     * @param context The current Context
     * @return a list of hidden app components
     */
    public static List<ComponentName> loadHiddenAppList(final Context context){
        File file = new File(context.getApplicationInfo().dataDir + "/hiddenApps.json");
        List<ComponentName> resultList = new ArrayList<>();
        if(file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
                ArrayList s = mapper.readValue(file, ArrayList.class);
                for (Object item : s) {
                    resultList.add(new ComponentName(((SavableComponent)item).pkg, ((SavableComponent)item).cls));
                }
                return resultList;
            } catch (IOException e) {
                ExceptionLog.w(e);
            }
        }
        return resultList;
    }

    /**
     * Saves a list of QuickActions to the quickApps.json file
     * @param context The current Context
     * @param quickActions a list of QuickActions zu save
     */
    public static void saveQuickApps(final Context context, final ArrayList<QuickAction> quickActions){
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
            mapper.writeValue(new File(context.getApplicationInfo().dataDir + "/quickApps.json"), quickActions);
        } catch (IOException e) {
            ExceptionLog.w(e);
        }
    }

    /**
     * Saves all QuickActions from a QuickActionBar to the quickApps.json file. See also {@link #saveQuickApps(Context, ArrayList)}
     * @param context The current Context
     * @param qaBar the QuickActionBar to save items from.
     */
    public static void saveQuickApps(final Context context, final QuickAppBar qaBar){
        //Create an ArrayList of QuickActions to pass to the other saveQuickApps method.
        ArrayList<QuickAction> quickActions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            QuickAppIcon c = ((QuickAppIcon) qaBar.getChildAtPosition(i));
            if(c!=null){
                Intent intent = ((Intent) c.getTag());
                ComponentName comp = intent.getComponent();
                quickActions.add(new QuickAction(context.getResources().getResourceName(c.getIconRes()), comp.getPackageName(), comp.getClassName(), i));
            }
        }
        saveQuickApps(context, quickActions);
    }

    /**
     * Loads an array of QuickActions from the quickApps.json file.
     * @param context The current Context
     * @return A list of all saved QuickActions (may be empty but never null)
     */
    public static ArrayList loadQuickApps(final Context context){
        File file = new File(context.getApplicationInfo().dataDir + "/quickApps.json");
        if(file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
                //Return the loaded quickApps
                return mapper.readValue(file, ArrayList.class);
            } catch (IOException e) {
                ExceptionLog.w(e);
            }
        }

        //When there was an error loading or when there have not been any saved quickActions yet:
        //Fill it with default QuickActions.
        return fillDefaultQuickActions();
    }

    /**
     * Loads the quickApps.json file and adds the QuickActions to a QuickActionBar. Also calls {@link #loadQuickApps(Context)}
     * @param context The current Context
     * @param qaBar The QuickActionBar to inflate into
     */
    public static void inflateQuickApps(final Context context, final QuickAppBar qaBar){
        for(Object qa : loadQuickApps(context)){
            //Just check if the loaded quickActions are really QuickActions.
            if(!(qa instanceof QuickAction))
                throw new RuntimeException("Loaded QuickAction is of a wrong format.");

            //if so, just add it to the QuickActionBar.
            qaBar.add((QuickAction)qa);
        }
    }

    /**
     * Fills in some default QuickActions to be accessed when {@link #loadQuickApps(Context) being loaded}
     * @return An array list of QuickActions (Seemingly just Objects but it should be QuickActions)
     */
    private static ArrayList fillDefaultQuickActions(){
        return new ArrayList();
    }

    /**
     * Saves the whole FolderStructure into the somedata.json file, while saving the header images parallelized.
     * @param context The current Context
     * @param structure the FolderStructure to be saved to.
     */
    public static void saveFolderStructure(final Context context, final FolderStructure structure){
        try {
            for (final FolderStructure.Folder folder: structure.folders) {
                LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
                    @Override
                    public void run() {
                        folder.saveImage(context, false);
                    }
                });
            }

            ObjectMapper mapper = new ObjectMapper();
            mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
            mapper.writeValue(new File(context.getApplicationInfo().dataDir + "/somedata.json"), structure);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the somedata.json file and creates a FolderStructure from it. If there is no file or if there was an error, {@link #createNew(Context, DrawerTree, PreferenceHolder)} is being called.
     * @param context The current Context
     * @param mDrawerTree The DrawerTree containing all the apps for the folders to know what to add to them.
     * @param mHolder A helper object which holds some SharedPreference values.
     * @return A reeeeeaaaallly new FolderStructure
     */
    public static FolderStructure loadFolderStructure(Context context, DrawerTree mDrawerTree, PreferenceHolder mHolder){
        File file = new File(context.getApplicationInfo().dataDir + "/somedata.json");
        if(file.exists()) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.OBJECT_AND_NON_CONCRETE);
                FolderStructure s = mapper.readValue(file, FolderStructure.class);
                for (FolderStructure.Folder folder : s.folders) {
                        folder.loadImage(context);
                    if(!folder.folderName.equals("All"))
                        s.addFolderAssignments(folder);
                }
                return s;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return createNew(context, mDrawerTree, mHolder);
    }

    /**
     * Creates a new FolderStructure containing just an "All" folder.
     * @param context The current Context
     * @param mDrawerTree The DrawerTree containing all the apps for the folders to know what to add to them.
     * @param mHolder A helper object which holds some SharedPreference values.
     * @return An even newer FolderStructure
     */
    static FolderStructure createNew(Context context, DrawerTree mDrawerTree, PreferenceHolder mHolder){
        FolderStructure mFolderStructure = new FolderStructure();

        //Create the folder and assign its values.
        FolderStructure.Folder folder = new FolderStructure.Folder();
        folder.folderName = "All";
        folder.isAllFolder = true;
        folder.accentColor = 0xffef6e47;
        folder.folderIconRes = "ic_all";
        folder.headerImage = LauncherUtils.drawableToBitmap(context.getDrawable(R.drawable.welcome_header_small));
        int allPageCount = (int) Math.ceil(mDrawerTree.getApplicationCount()/(float)mHolder.gridSize());

        //Add all the apps to the All folder.
        for (int i = 0; i < allPageCount; i++) {
            final FolderStructure.Page page = new FolderStructure.Page();
            for (int j = 0; j < mHolder.gridSize() && (i*mHolder.gridSize()+j)<mDrawerTree.getApplicationCount(); j++) {
                mDrawerTree.doWithApplication(i * mHolder.gridSize() + j, new DrawerTree.LoadedListener() {
                    @Override
                    public void onApplicationLoadingFinished(Application app) {
                        page.add(app);
                    }
                });
            }
            folder.add(page);
        }

        //Add the folder
        mFolderStructure.add(folder);

        //Better save it now.
        saveFolderStructure(context, mFolderStructure);

        return mFolderStructure;
    }

}
