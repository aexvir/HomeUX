package com.dravite.newlayouttest.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.ImageButton;

import com.dravite.newlayouttest.R;

/**
 * An image button that can work like a {@link android.widget.ToggleButton} and be toggled on or off using {@link #toggle()} and {@link #setChecked(boolean)}.
 */
public class ToggleImageButton extends ImageButton implements Checkable {
    private OnCheckedChangeListener onCheckedChangeListener;
    private boolean mOnlyCheck;

    public ToggleImageButton(Context context) {
        super(context);
    }

    public ToggleImageButton(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public ToggleImageButton(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * @param attrs Takes custom attributes from here.
     */
    private void init(AttributeSet attrs) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.ToggleImageButton);
        setChecked(a.getBoolean(R.styleable.ToggleImageButton_android_checked, false)); //Default checked?
        mOnlyCheck = a.getBoolean(R.styleable.ToggleImageButton_only_check_no_uncheck, false); //Can be unchecked manually?
        a.recycle();
    }

    @Override
    public boolean isChecked() {
        return isSelected();
    }

    @Override
    public void setChecked(boolean checked) {
        setSelected(checked);

        if (onCheckedChangeListener != null) {
            onCheckedChangeListener.onCheckedChanged(this, checked);
        }
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    @Override
    public boolean performClick() {
        if(mOnlyCheck) setChecked(true);
        else toggle();
        return super.performClick();
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        if(enabled){
            setImageTintList(ColorStateList.valueOf(0xff333333));
        } else {
            setImageTintList(ColorStateList.valueOf(0xff909090));
        }
    }

    public OnCheckedChangeListener getOnCheckedChangeListener() {
        return onCheckedChangeListener;
    }

    public void setOnCheckedChangeListener(OnCheckedChangeListener onCheckedChangeListener) {
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public interface OnCheckedChangeListener {
        void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked);
    }
}