package com.dravite.homeux.views;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageButton;

import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;

/**
 * Created by Johannes on 06.09.2015.
 * A button showing a custom set icon and having a launch intent as tag, which gets started when tapping on it.
 */
public class QuickAppIcon extends ImageButton {

    Drawable mIcon;
    int mIconRes;

    public QuickAppIcon(Context context){
        this(context, null);
    }

    public QuickAppIcon(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public QuickAppIcon(Context context, AttributeSet attrs, int defStyleAttr){
        this(context, attrs, defStyleAttr, 0);
    }

    public QuickAppIcon(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        setScaleType(ScaleType.CENTER_INSIDE);

        setBackground(context.getDrawable(R.drawable.ripple));
    }

    public void setIconRes(int res){
        mIconRes = res;
        setIcon(getContext().getDrawable(res));
    }

    public int getIconRes(){
        return mIconRes;
    }

    public void setIcon(Drawable icon) {
        mIcon = icon.getConstantState().newDrawable();

        LayerDrawable finalTop;

        icon.setTint(0xffffffff);
        Drawable oldTop = icon.getConstantState().newDrawable();

        oldTop.setTint(0x88000000);
        int dimen = (int) getResources().getDimension(R.dimen.app_icon_text_padding_delta);
        Rect bounds = icon.getBounds();
        bounds.bottom += dimen;
        bounds.top += dimen;
        bounds.right += dimen / 2;
        oldTop.setBounds(bounds);

        finalTop = new LayerDrawable(new Drawable[]{oldTop, icon});
        finalTop.setLayerInset(0, dimen / 3, dimen / 2, -dimen / 3, -dimen / 2);

        finalTop.setBounds(0, 0, LauncherUtils.dpToPx(24, getContext()), LauncherUtils.dpToPx(24, getContext()));

        super.setImageDrawable(finalTop);
    }

    @Override
    public void setTag(final Object tag) {
        super.setTag(tag);

        if(!(tag instanceof Intent))
            throw new IllegalArgumentException("A QuickApp can only have an Intent as tag.");

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = LauncherUtils.makeLaunchIntent((Intent) v.getTag());
                LauncherUtils.startActivity((LauncherActivity) getContext(), QuickAppIcon.this, i);
            }
        });
    }
}
