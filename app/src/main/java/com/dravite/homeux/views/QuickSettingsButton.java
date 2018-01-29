package com.dravite.homeux.views;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.Button;

import com.dravite.homeux.R;

/**
 * Created by Johannes on 16.09.2015.
 * This is a button for the left part of the Top Panel containing those 3 buttons (Wallpaper, Icon Packs and Settings)
 */
public class QuickSettingsButton extends Button {
    Rect mTextBounds = new Rect();

    public QuickSettingsButton(Context context){
        this(context, null);
    }

    public QuickSettingsButton(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public QuickSettingsButton(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);

        Drawable dTop = getCompoundDrawables()[1];

        dTop.setTint(0xffffffff);
        setCompoundDrawablesWithIntrinsicBounds(null, dTop, null, null);

        setTextColor(0xffffffff);
        setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);

        setGravity(Gravity.CENTER);

        setBackground(context.getDrawable(R.drawable.ripple));

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Drawable drawableTop = getCompoundDrawables()[1];
        int drawableHeight = drawableTop.getBounds().height();
        int drawablePadding = getCompoundDrawablePadding();
        getPaint().getTextBounds(getText().toString(), 0, getText().length(), mTextBounds);

        int padding = (drawableHeight+drawablePadding+mTextBounds.height()-getMeasuredHeight())/2;

        setPadding(0, ((int) (-padding - ((float) getResources().getDimension(R.dimen.app_icon_text_padding_delta)))), 0, 0);
    }
}
