package com.dravite.homeux.drawerobjects.structures;

import android.appwidget.AppWidgetHostView;
import android.content.Context;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.ViewGroup;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.views.helpers.CheckForLongPressHelper;

/**
 * Created by Johannes on 01.09.2015.
 *
 */
public class ClickableAppWidgetHostView extends AppWidgetHostView {

    public CheckForLongPressHelper mLongPressHelper;
    private float mSlop;

    public ClickableAppWidgetHostView(Context context){
        super(context);
        mLongPressHelper = new CheckForLongPressHelper(this);
        mLongPressHelper.setLongPressTimeout(500);
    }


    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        mLongPressHelper.cancelLongPress();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();

        mSlop = ViewConfiguration.get(getContext()).getScaledTouchSlop();
    }

    float[] pos = new float[2];


    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        // Just in case the previous long press hasn't been cleared, we make sure to start fresh
        // on touch down.
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mLongPressHelper.cancelLongPress();
            pos = new float[] {ev.getX(), ev.getY()};
        }

        // Consume any touch events for ourselves after longpress is triggered
        if (mLongPressHelper.hasPerformedLongPress()) {
            mLongPressHelper.cancelLongPress();
            return false;
        }

        // Watch for longpress events at this level to make sure
        // users can always pick up this widget
        switch (ev.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                mLongPressHelper.postCheckForLongPress();
                mLongPressHelper.setLongPressPosition(ev.getX(), ev.getY());
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_MOVE:
                if(Math.abs(ev.getY() - pos[1])>mSlop || Math.abs(ev.getX() - pos[0])>mSlop) {
                    mLongPressHelper.cancelLongPress();
                } else {
                    mLongPressHelper.setLongPressPosition(ev.getX(), ev.getY());
                }
                break;
        }

        // Otherwise continue letting touch events fall through to children
        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If the widget does not handle touch, then cancel
        // long press when we release the touch
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
            case MotionEvent.ACTION_OUTSIDE:
                mLongPressHelper.cancelLongPress();
                break;
            case MotionEvent.ACTION_SCROLL:
            case MotionEvent.ACTION_MOVE:
                if (!LauncherUtils.pointInView(this, event.getX(), event.getY(), mSlop)) {
                    mLongPressHelper.cancelLongPress();
                    break;
                }
                break;
        }
        return true;
    }

    @Override
    public int getDescendantFocusability() {
        return ViewGroup.FOCUS_BLOCK_DESCENDANTS;
    }
}
