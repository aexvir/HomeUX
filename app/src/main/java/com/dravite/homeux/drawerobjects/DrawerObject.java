package com.dravite.homeux.drawerobjects;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import com.dravite.homeux.views.CustomGridLayout;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.io.File;
import java.io.Serializable;

/**
 * (Abstract) An object that can be placed on a homescreen.
 */
public abstract class DrawerObject implements Parcelable,Serializable {

    public static final int TYPE_APP = 0;
    public static final int TYPE_SHORTCUT = 1;
    public static final int TYPE_WIDGET = 2;

    /**
     * Contains the data necessary to locate and/or place a DrawerObject onto a drawer grid.
     */
    public static class GridPositioning implements Serializable{
        public int row, col, rowSpan, colSpan;

        //Default constructor
        @SuppressWarnings("unused")
        public GridPositioning(){}

        /**
         * You should use this GridPositioning constructor in order to directly specify the data
         * @param r row
         * @param c column
         * @param rs rowSpan
         * @param cs columnSpan
         */
        public GridPositioning(int r, int c, int rs, int cs){
            row=r; col=c; rowSpan=rs; colSpan=cs;
        }

        /**
         * Creates a grid cell from its row and column
         * @return the grid cell
         */
        @JsonIgnore
        public CustomGridLayout.Cell getCell(){
            return new CustomGridLayout.Cell(row, col);
        }

        /**
         * Creates an exact copy of this GridPositioning
         * @return a copy of this GridPositioning object
         */
        public GridPositioning copy(){
            return new GridPositioning(row, col, rowSpan, colSpan);
        }

        /**
         * Sets this GridPositioning to the value of the given Parameters
         * @param cell contains row and column
         * @param span contains rowSpan and columnSpan
         */
        @JsonIgnore
        public void set(CustomGridLayout.Cell cell, CustomGridLayout.CellSpan span){
            row = cell.row;
            col = cell.column;
            rowSpan = span.rowSpan;
            colSpan = span.columnSpan;
        }
    }

    /**
     * This listener runs as soon as the DrawerObject finishes creating its View.
     */
    public interface OnViewCreatedListener{
        void onViewCreated(View view);
    }

    public GridPositioning mGridPosition;

    public DrawerObject(){}

    public DrawerObject(GridPositioning positioning){
        mGridPosition=positioning;
    }

    /**
     * Returns a File pointing at a given subpath of the cache.
     * @param context The current context
     * @param extension To be appended onto the cache path (like /cachepath + /extensionpath)
     * @param fileName The name of the file to address.
     * @return The targeted file
     */
    @JsonIgnore
    public File getCacheFile(Context context, String extension, String fileName){
        return new File(context.getCacheDir().getPath() + extension, fileName);
    }

    /**
     * @return whether the DrawerObject is an Application, a Widget or a Shortcut
     */
    public abstract int getObjectType();

    /**
     * Creates the View for this DrawerObject. Should call the listener.
     * @param parent The Grid layout to be inflated into.
     * @param inflater a LayoutInflater
     * @param listener The listener which should be called after finishing creating the View
     */
    public abstract void createView(CustomGridLayout parent, LayoutInflater inflater, OnViewCreatedListener listener);

    /**
     * Checks if the given DrawerObject is equal to this one excluding its position on the Grid
     * @param object the DrawerObject to check against
     * @return Whether the objects are the same
     */
    public abstract boolean equalType(DrawerObject object);

    /**
     * Copies a DrawerObject with all its variable values.
     * @return a copy of this DrawerObject
     */
    public abstract DrawerObject copy();


    // Parcelable constructor
    public DrawerObject(Parcel in){
        mGridPosition = (GridPositioning) in.readSerializable();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(mGridPosition);
    }

}
