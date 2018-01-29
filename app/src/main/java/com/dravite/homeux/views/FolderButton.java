package com.dravite.homeux.views;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ScaleDrawable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;

import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.general_helpers.ColorUtils;

/**
 * A Button that can get a HomeUX folder assigned to change image and text accordingly.
 */
public class FolderButton extends TextView {

    private boolean isSelected = false;

    public FolderButton(Context context) {
        this(context, null);
    }

    public FolderButton(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.buttonBarButtonStyle);
    }

    public FolderButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        int horizontalPadding = LauncherUtils.dpToPx(2, getContext());
        int verticalPadding = LauncherUtils.dpToPx(10, getContext());

        setBackground((getContext().getDrawable(R.drawable.circle_background_folder)));

        //Put ... at the end for long names
        setEllipsize(TextUtils.TruncateAt.END);

        setElevation(0);
        setTranslationZ(0);
        setClipToOutline(true);
        setSingleLine(true);
        setPadding(horizontalPadding,verticalPadding,horizontalPadding,verticalPadding);
        setCompoundDrawablePadding(LauncherUtils.dpToPx(10, getContext()));
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        if(getBackground()!=null) {
            int sideInset = getMeasuredWidth() / 2 - (LauncherUtils.dpToPx(22, getContext()));
            int bottomInset = getMeasuredHeight() - LauncherUtils.dpToPx(44, getContext());
            ((LayerDrawable) getBackground()).setLayerInset(1, sideInset, 0, sideInset, bottomInset);
        }
    }

    /**
     * Method to assign a folder to this button for it to change color and text accordingly.
     * @param folder Assign this folder
     */
    public void assignFolder(FolderStructure.Folder folder){
        setText(folder.folderName);
        Drawable drawable = new ScaleDrawable(getContext().getDrawable(getResources().getIdentifier(folder.folderIconRes, "drawable", getContext().getPackageName())), 0, LauncherUtils.dpToPx(11, getContext()),
                LauncherUtils.dpToPx(11, getContext())).getDrawable();;
        drawable.setTint(Color.WHITE);
        drawable.setBounds(0, 0, LauncherUtils.dpToPx(24, getContext()), LauncherUtils.dpToPx(24, getContext()));
        setCompoundDrawablesRelative(null, drawable, null, null);
    }

    /**
     * Sets this button as selected, thus giving it a circle background and a bold typeface.
     * @param selectionColour The color of the circle.
     */
    public void select(int selectionColour){
        Drawable d = getCompoundDrawablesRelative()[1];
        setBackground((getContext().getDrawable(R.drawable.circle_background_folder)));
        ((LayerDrawable) getBackground()).getDrawable(1).setTintMode(PorterDuff.Mode.SRC_IN);
        ((LayerDrawable) getBackground()).getDrawable(1).setTint(selectionColour);
        int tint = ColorUtils.isBrightColor(selectionColour) ?0xa8000000:0xffffffff;
        setTextColor(0xffffffff);
        d.setTint(tint);
        setTypeface(getTypeface(), Typeface.BOLD);

        isSelected = true;
    }

    /**
     * Sets this button as not selected, removing the circle and setting a normal typeface.
     */
    public void deselect(){
        setBackground(null);
        if(getBackground()==null)
            setBackground((getContext().getDrawable(R.drawable.circle_background_folder)));
        ((LayerDrawable) getBackground()).getDrawable(1).setTintMode(PorterDuff.Mode.MULTIPLY);
        ((LayerDrawable) getBackground()).getDrawable(1).setTint(Color.TRANSPARENT);
        setTypeface(null, Typeface.NORMAL);
        setTextColor(0xffffffff);
        Drawable d = getCompoundDrawablesRelative()[1];
        d.setTint(0xffffffff);
        d.setBounds(0, 0, LauncherUtils.dpToPx(24, getContext()), LauncherUtils.dpToPx(24, getContext()));
        setCompoundDrawablesRelative(null, d, null, null);

        isSelected = false;
    }

    @Override
    public boolean isSelected(){
        return isSelected;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }
}
