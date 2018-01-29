package com.dravite.newlayouttest.drawerobjects.structures;

import android.content.ComponentName;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dravite.newlayouttest.Const;
import com.dravite.newlayouttest.LauncherLog;
import com.dravite.newlayouttest.drawerobjects.Application;
import com.dravite.newlayouttest.drawerobjects.DrawerObject;
import com.dravite.newlayouttest.general_helpers.FileManager;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Created by Johannes on 13.09.2015.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class FolderStructure implements Parcelable, Serializable{

    @JsonIgnore
    public TreeMap<ComponentName, LinkedList<String>> mApplicationToFolderMapper = new TreeMap<>();

    public interface IteratorHelper{
        void getObject(int folderIndex, Folder folder, int pageIndex, Page page, int itemIndex, DrawerObject object);
    }

    /**
     * A Folder containing Pages
     */
    public static class Folder implements Parcelable, Serializable{
        public static class FolderType{
            public static final int TYPE_WIDGETS = 0;
            public static final int TYPE_APPS_ONLY = 1;
        }

        public String folderName;
        public boolean isAllFolder = false;
        @JsonIgnore
        public transient Bitmap headerImage;
        public String folderIconRes;
        public int accentColor = Color.WHITE;
        public int mFolderType = FolderType.TYPE_WIDGETS;
        public List<Page> pages = new ArrayList<>();

        public void add(Page page){
            pages.add(page);
        }

        public Folder(){

        }

        /**
         * Saves the header image for this folder.
         * @param context
         * @param force true, to overwrite the old file if there is any.
         */
        public void saveImage(Context context, boolean force){
            FileManager.saveBitmapToData(context, headerImage, folderName, force);
        }

        /**
         * Deletes the header image for this folder
         * @param context
         */
        public void deleteImage(Context context){
            File dir = new File(context.getApplicationInfo().dataDir + "/folderImg/");
            File f = new File(dir.getPath(), folderName);
            FileManager.deleteRecursive(f);
        }


        /**
         * Loads the header image for this folder
         * @param context
         */
        public void loadImage(Context context){
            headerImage = FileManager.loadBitmapFromData(context, folderName);
        }

        public Folder(Parcel in){
            folderName = in.readString();
            isAllFolder = (boolean)in.readValue(Boolean.class.getClassLoader());
            accentColor = in.readInt();
            mFolderType = in.readInt();
            Object[] o = in.readParcelableArray(Page[].class.getClassLoader());
            pages = new ArrayList<>();
            for (int i = 0; i < o.length; i++) {
                pages.add((Page)o[i]);
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(folderName);
            dest.writeValue(isAllFolder);
            dest.writeInt(accentColor);
            dest.writeInt(mFolderType);
            Page[] pArray = new Page[pages.size()];
            for (int i = 0; i < pArray.length; i++) {
                pArray[i] = pages.get(i);
            }
            dest.writeParcelableArray(pArray, 0);
        }

        public static final Parcelable.Creator<Folder> CREATOR
                = new Parcelable.Creator<Folder>() {
            public Folder createFromParcel(Parcel in) {
                return new Folder(in);
            }

            public Folder[] newArray(int size) {
                return new Folder[size];
            }
        };
    }

    /**
     * An AppDrawer page contained in a folder
     */
    public static class Page implements Parcelable,Serializable{
        public ViewDataArrayList items = new ViewDataArrayList();

        public void add(DrawerObject data){
            items.add(data);
        }

        public Page(){

        }

        public Page(Parcel in){
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
        }

        public static final Parcelable.Creator<Page> CREATOR
                = new Parcelable.Creator<Page>() {
            public Page createFromParcel(Parcel in) {
                return new Page(in);
            }

            public Page[] newArray(int size) {
                return new Page[size];
            }
        };
    }

    //The list of folders
    public List<Folder> folders = new ArrayList<>();

    /**
     * @param name The name of the folder to search for
     * @return The folder with the given name.
     */
    public Folder getFolderWithName(String name){
        for (int i = 0; i < folders.size(); i++) {
            if(folders.get(i).folderName.equals(name)){
                return folders.get(i);
            }
        }
        return null;
    }

    /**
     * @param name The name of the folder to search for
     * @return The index of the folder with the given name.
     */
    public int getFolderIndexOfName(String name){
        for (int i = 0; i < folders.size(); i++) {
            if(folders.get(i).folderName.equals(name)){
                return i;
            }
        }
        return -1;
    }

    /**
     * @param context
     * @return The name of the folder which is set as default by the user.
     */
    public String getDefaultFolderName(Context context){
        return PreferenceManager.getDefaultSharedPreferences(context).getString(Const.Defaults.TAG_DEFAULT_FOLDER, Const.Defaults.getString(Const.Defaults.TAG_DEFAULT_FOLDER));
    }

    /**
     * @param context
     * @return The index of the folder which is set as default by the user.
     */
    public int getDefaultFolderIndex(Context context){
        return getFolderIndexOfName(getDefaultFolderName(context));
    }

    /**
     * Sets the name of the folder at the given index.
     * @param context
     * @param index folder index
     * @param newName new Folder name
     */
    public void setFolderName(Context context, int index, String newName){

        String oldName = folders.get(index).folderName;

        LauncherLog.d("FolderStructure", "Changed a relation Folder: " + oldName + " -> " + newName);

        for (ComponentName name : mApplicationToFolderMapper.keySet()) {
            LinkedList<String> folders = mApplicationToFolderMapper.get(name);

            if(folders!=null && folders.contains(oldName)){
                folders.remove(oldName);
                folders.add(newName);
                mApplicationToFolderMapper.put(name, folders);
            }
        }

        FileManager.renameBitmapFromData(context, oldName, newName);

        folders.get(index).folderName = newName;
    }

    /**
     * Adds a folder
     */
    public void add(Folder folder){
        folders.add(folder);

        if(!folder.folderName.equals("All")) {
            addFolderAssignments(folder);
        }
    }

    /**
     * Removes a folder
     * @param folderIndex index of the folder to be removed.
     */
    public void remove(int folderIndex){
        remove(folders.get(folderIndex));
    }

    /**
     * Removes a folder
     */
    public void remove(Folder folder){
        if(folder.folderName.equals("All"))
            return;

        removeFolderAssignments(folder);
        folders.remove(folder);
    }

    /**
     * Assigns an app component to a folder.
     * @param componentName
     * @param folderName
     */
    public void addFolderAssignment(ComponentName componentName, String folderName){
        LinkedList<String> assignedFolderList = mApplicationToFolderMapper.get(componentName);

        if(assignedFolderList==null)
            assignedFolderList = new LinkedList<>();

        if(!assignedFolderList.contains(folderName)) {
            assignedFolderList.add(folderName);
            mApplicationToFolderMapper.put(componentName, assignedFolderList);
        }

    }

    /**
     * see {@link #addFolderAssignment(ComponentName, String)}.
     */
    public void addFolderAssignments(Folder folder){
        for (Page page : folder.pages) {
            for(DrawerObject object : page.items){
                if(object instanceof Application){
                    ComponentName compName = new ComponentName(((Application) object).packageName, ((Application) object).className);
                    LinkedList<String> assignedFolderList = mApplicationToFolderMapper.get(compName);

                    if(assignedFolderList==null){
                        //Not yet assigned
                        assignedFolderList = new LinkedList<>();
                        assignedFolderList.add(folder.folderName);
                        mApplicationToFolderMapper.put(compName, assignedFolderList);
                    } else if(!assignedFolderList.contains(folder.folderName)){
                        //Assigned earlier but all assignments have been removed after that.
                        // OR
                        //Has assignments, but not yet the given folder. otherwise leave it.
                        assignedFolderList.add(folder.folderName);
                        //Just for safety.
                        mApplicationToFolderMapper.put(compName, assignedFolderList);
                    }
                }
            }
        }
    }

    public void removeAppPackage(ComponentName componentName){
        mApplicationToFolderMapper.remove(componentName);
    }

    /**
     * see {@link #addFolderAssignment(ComponentName, String)}.
     */
    public void removePageAssignments(Page page, String folderName){
        for(DrawerObject object : page.items){
            if(object instanceof Application){
                ComponentName compName = new ComponentName(((Application) object).packageName, ((Application) object).className);
                LinkedList<String> assignedFolderList = mApplicationToFolderMapper.get(compName);

                if(assignedFolderList==null){
                    //Not yet assigned
                    assignedFolderList = new LinkedList<>();
                    mApplicationToFolderMapper.put(compName, assignedFolderList);
                } else if(assignedFolderList.contains(folderName)){
                    //Assigned earlier but all assignments have been removed after that.
                    // OR
                    //Has assignments, but not yet the given folder. otherwise leave it.
                    assignedFolderList.remove(folderName);
                    //Just for safety.
                    mApplicationToFolderMapper.put(compName, assignedFolderList);
                }
            }
        }
    }

    /**
     * see {@link #addFolderAssignment(ComponentName, String)}.
     */
    public void removeFolderAssignments(Folder folder){
        for (Page page : folder.pages) {
            removePageAssignments(page, folder.folderName);
        }
    }

    /**
     * see {@link #addFolderAssignment(ComponentName, String)}.
     */
    public void removeFolderAssignment(ComponentName componentName, String folderName){
        LinkedList<String> assignedFolderList = mApplicationToFolderMapper.get(componentName);

        if(assignedFolderList == null){
            assignedFolderList = new LinkedList<>();
        } else {
            assignedFolderList.remove(folderName);
        }

        mApplicationToFolderMapper.put(componentName, assignedFolderList);
    }

    //DEFAULT CONSTRUCTOR
    public FolderStructure(){

    }

    @JsonIgnore
    public int getItemCount() {
        int i = 0;
        for (int f = 0; f < folders.size(); f++) {
            for (int p = 0; p < folders.get(f).pages.size(); p++) {
                i+=folders.get(f).pages.get(p).items.size();
            }
        }
        return i;
    }

    //Parcelable Constructor
    public FolderStructure(Parcel in){
        Object[] o = in.readParcelableArray(Folder[].class.getClassLoader());
        folders = new ArrayList<>();
        for (Object obj : o) {
            add((Folder)obj);
        }
    }

    /**
     * Nested for loops combined into one method.
     * @param includeAllFolder true, to also include the All folder when iterating
     * @param helper An interface to set what to do with each element
     */
    public void iterateThrough(boolean includeAllFolder, IteratorHelper helper){
        for (int i = 0; i < folders.size(); i++) {
            if(includeAllFolder || !folders.get(i).folderName.equals("All"))
            for (int j = 0; j < folders.get(i).pages.size(); j++) {
                for (int k = 0; k < folders.get(i).pages.get(j).items.size(); k++) {
                    helper.getObject(i, folders.get(i), j, folders.get(i).pages.get(j), k, folders.get(i).pages.get(j).items.get(k));
                }
            }
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        Folder[] pArray = new Folder[folders.size()];
        for (int i = 0; i < pArray.length; i++) {
            pArray[i] = folders.get(i);
        }
        dest.writeParcelableArray(pArray, 0);
    }
    
    /**
     * An extended {@link ArrayList} with special equality checks and additional methods.
     */
    public static class ViewDataArrayList extends ArrayList<DrawerObject> implements Serializable{

        @Override
        public DrawerObject get(int index) {
            return super.get(index);
        }

        public ViewDataArrayList(){

        }

        /**
         * Checks if the elements in this list are filling up an entire grid with the cell count.
         * @param fullSize The full cell count
         * @return true, if there is no free cell.
         */
        public boolean isFull(int fullSize){
            int size = 0;
            for (int i = 0; i < size(); i++) {
                size+=get(i).mGridPosition.colSpan*get(i).mGridPosition.rowSpan;
            }
            return size>=fullSize;
        }

        @Override
        public int indexOf(Object object) {
            DrawerObject data = (DrawerObject) object;
            if (data != null) {
                for (int i = 0; i < size(); i++) {
                    if (data.equals(get(i))) {
                        return i;
                    }
                }
            } else {
                for (int i = 0; i < size(); i++) {
                    if (get(i) == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        /**
         * Like {@link #indexOf(Object)} but without position equality.
         * @param data inherited
         * @return inherited
         */
        public int indexOfNotPosition(DrawerObject data) {
            if (data != null) {
                for (int i = 0; i < size(); i++) {
                    if (data.equalType(get(i))) {
                        return i;
                    }
                }
            } else {
                for (int i = 0; i < size(); i++) {
                    if (get(i) == null) {
                        return i;
                    }
                }
            }
            return -1;
        }

        /**
         * Like {@link #contains(Object)} but without position equality.
         * @param object inherited
         * @return inherited
         */
        public boolean containsNotPosition(DrawerObject object) {
            Object[] a = this.toArray();
            int s = size();
            if (object != null) {
                for (int i = 0; i < s; i++) {
                    if (object.equalType((DrawerObject)a[i])) {
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < s; i++) {
                    if (a[i] == null) {
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public boolean remove(Object object) {
            int s = size();
            if (object != null) {
                for (int i = 0; i < s; i++) {
                    if (((DrawerObject)object).equalType((DrawerObject) get(i))) {
                        remove(i);
                        return true;
                    }
                }
            } else {
                for (int i = 0; i < s; i++) {
                    if (get(i) == null) {
                        remove(i);
                        return true;
                    }
                }
            }
            return false;
        }
    }

    //Parcelable method
    public static final Parcelable.Creator<FolderStructure> CREATOR
            = new Parcelable.Creator<FolderStructure>() {
        public FolderStructure createFromParcel(Parcel in) {
            return new FolderStructure(in);
        }

        public FolderStructure[] newArray(int size) {
            return new FolderStructure[size];
        }
    };
}
