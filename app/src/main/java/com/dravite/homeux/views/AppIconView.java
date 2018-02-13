package com.dravite.homeux.views;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Button;

import com.dravite.homeux.R;
import com.dravite.homeux.views.helpers.CheckForLongPressHelper;
import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.views.viewcomponents.TextDrawable;

/**
 * A Button view that represents an app on the home screen having an icon and a one-line label
 */
public class AppIconView extends Button {

    Rect mTextBounds = new Rect();

    //Icon sizes, one in DP and one in Pixels
    int mIconSizeDP = 56;
    int mIconSizePixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIconSizeDP, getResources().getDisplayMetrics());

    //Is the label visible?
    boolean mLabelVisibility = true;

    //Do you want to use a set icon size?
    boolean mDoOverrideData = false;

    //maximal touch variation
    private float mSlop;

    //LongPressHelper
    public CheckForLongPressHelper mLongPressHelper;

    //An overlay showing a message count
    Drawable mOverlay;

    //The counter overlay value
    int mCounterValue = 0;
    int mCounterRadius = 0;
    int mCounterBackgroundColor = Color.WHITE;
    int mCounterTextColor = Color.BLACK;

    //Icon clip bounds
    Rect mRect = new Rect();

    // Shared preferences
    SharedPreferences mPreferences;

    public AppIconView(Context context){
        this(context, null);
    }

    public AppIconView(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public AppIconView(Context context, AttributeSet attrs, int defStyleAttr){
        this(context, attrs, defStyleAttr, 0);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    public AppIconView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
        mLongPressHelper = new CheckForLongPressHelper(this);
        mLongPressHelper.setLongPressTimeout(200);
        setWillNotDraw(true);
        setCompoundDrawablePadding(LauncherUtils.dpToPx(-1, getContext()));
        setLayerType(LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mOverlay!=null) {
            //Draw counter overlay if available
            canvas.getClipBounds(mRect);
            mOverlay.setBounds(mRect);
            mOverlay.draw(canvas);
        }

        //setCounterOverlay(20);
    }

    /**
     * This method overrides this icon's size for use in a unified environment like the hidden apps list.
     * @param iconSize The new overridden iconSize.
     */
    public void overrideData(int iconSize){
        mDoOverrideData = true;
        mIconSizeDP = iconSize;
        this.mIconSizePixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIconSizeDP, getResources().getDisplayMetrics());
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        getPaint().getTextBounds(getText().toString(), 0, getText().length(), mTextBounds);

        int iconPadding = (getMeasuredHeight() / 2) - (mIconSizePixels / 2);
        int additionalPadding = mLabelVisibility ? LauncherUtils.dpToPx(4, getContext()):0;
        int textPadding = mLabelVisibility ? mTextBounds.height() : 0;

        setPadding(0, iconPadding - textPadding + additionalPadding, 0, 0);
    }

    @Override
    public boolean canScrollHorizontally(int direction) {
        return false;
    }

    @Override
    public boolean canScrollVertically(int direction) {
        return false;
    }

    /**
     * Adds a small number counter to the corner of this icon.
     * @param counterValue The value of the counter. Set to 0 to remove it.
     */
    public void setCounterOverlay(int counterValue){
        if(counterValue==0){
            removeOverlay();
            return;
        }
        mCounterValue = counterValue;
        mOverlay = new TextDrawable(
            String.valueOf(counterValue),
            mPreferences.getInt(Const.Defaults.TAG_NOTIFICATIONS_RADIUS, getResources().getInteger(R.integer.notification_badge_radius)),
            LauncherUtils.dpToPx(mPreferences.getInt(Const.Defaults.TAG_NOTIFICATIONS_PADDING, getResources().getInteger(R.integer.notification_badge_padding)), getContext()),
            mPreferences.getInt(Const.Defaults.TAG_NOTIFICATIONS_TEXT_COLOR, getResources().getColor(R.color.notificationBadgeText)),
            mPreferences.getInt(Const.Defaults.TAG_NOTIFICATIONS_BACKGROUND_COLOR, getResources().getColor(R.color.notificationBadgeBackground))
        );
        invalidate();
    }

    /**
     * Removes the counterOverlay from being drawn
     */
    public void removeOverlay(){
        mOverlay = null;
        invalidate();
    }

    /**
     * @return The icon drawable of this app.
     */
    public Drawable getIcon(){
        return getCompoundDrawables()[1];
    }

    /**
     * Sets a drawable as an icon for this app after scaling it properly
     * @param icon The new icon drawable
     */
    public void setIcon(Drawable icon) {
        int iconSize = mDoOverrideData ? mIconSizePixels : LauncherUtils.dpToPx(Const.ICON_SIZE, getContext());
        if(icon!=null){
            icon.setBounds(
                0,
                0,
                iconSize,
                iconSize
            );
        }
        setCompoundDrawables(null, icon, null, null);
    }

    /**
     * Sets, whether the app label should be shown or not.
     * @param visible true if the label should be shown, false otherwise
     */
    public void setLabelVisibility(boolean visible){
        mLabelVisibility = visible;
        invalidate();
        measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
    }

    @Override
    public void setTextColor(int color) {
        if(mDoOverrideData || mLabelVisibility)
            super.setTextColor(color);
        else
            super.setTextColor(Color.TRANSPARENT);
    }

    @Override
    public void setShadowLayer(float radius, float dx, float dy, int color) {
        //Add a shadow to the text
        if(mDoOverrideData||mLabelVisibility)
            super.setShadowLayer(radius, dx, dy, color);
        else
            super.setShadowLayer(0, 0, 0, Color.TRANSPARENT);
    }

    /**
     * Sets the icon size, given as a dp value, and also calculates that into a pixel value.
     * @param mIconSize The new iconSize in DP
     */
    public void setIconSizeInDP(int mIconSize) {
        mIconSizePixels = (int)TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, mIconSize, getResources().getDisplayMetrics());
        this.mIconSizeDP = mIconSize;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // Call the superclass onTouchEvent first, because sometimes it changes the state to
        // isPressed() on an ACTION_UP
        boolean result = super.onTouchEvent(event);
        // Check for a stylus button press, if it occurs cancel any long press checks.

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mLongPressHelper.postCheckForLongPress();
                mLongPressHelper.setLongPressPosition(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_UP:
                // If we've touched down and up on an item, and it's still not "pressed", then
                // destroy the pressed outline
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_MOVE:
                if(!LauncherUtils.pointInView(this, event.getX(), event.getY(), mSlop)) {
                   // Log.d("CheckPressHelper", "cancelled Long Press");
                    mLongPressHelper.cancelLongPress();
                } else {
                    mLongPressHelper.setLongPressPosition(event.getX(), event.getY());
                }
                break;
        }
        return result;
    }
}
