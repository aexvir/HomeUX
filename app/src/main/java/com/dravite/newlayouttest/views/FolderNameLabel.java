package com.dravite.newlayouttest.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import com.dravite.newlayouttest.drawerobjects.structures.FolderStructure;

/**
 * A text view that is displayed above the app drawer to show which folder is currently active
 */
public class FolderNameLabel extends TextView {


    public FolderNameLabel(Context context) {
        this(context, null);
    }

    public FolderNameLabel(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public FolderNameLabel(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public FolderNameLabel(
            Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setAllCaps(true);
    }

    /**
     * Assigns a folder to this button to change text and icon accordingly
     * @param folder Assign this folder
     */
    public void assignFolder(FolderStructure.Folder folder){
        setText(folder.folderName);
        Drawable drawable = getContext().getDrawable(getResources().getIdentifier(folder.folderIconRes, "drawable", getContext().getPackageName()));
        drawable.setTint(Color.WHITE);
        setCompoundDrawablesRelativeWithIntrinsicBounds(drawable, null, null, null);
    }
}
