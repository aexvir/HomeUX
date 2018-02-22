package com.dravite.homeux.views.helpers;

import android.graphics.PointF;
import android.view.View;

/**
 * A Class which improves handling longClicks.
 */
public class CheckForLongPressHelper {

    View mView;
    View.OnLongClickListener mListener;
    boolean mHasPerformedLongPress;
    boolean canLongPress;

    //TODO doubled LongPressTimeout for testing reasons about accidentally lifting up stuff.
    private int mLongPressTimeout = 400;

    private CheckForLongPress mPendingCheckForLongPress;

    private PointF mLongPressPosition = new PointF();

    /**
     * A special Runnable that performs a long click check after being posted by {@link #postCheckForLongPress()}.
     */
    class CheckForLongPress implements Runnable {
        public void run() {
            if ((mView.getParent() != null) && mView.hasWindowFocus()
                    && !mHasPerformedLongPress) {
                canLongPress = true;
                boolean handled;
                if (mListener != null) {
                    handled = mListener.onLongClick(mView);
                } else {
                    handled = mView.performLongClick();
                }
                if (handled) {
                    mView.setPressed(false);
                    mHasPerformedLongPress = true;
                }
            }
        }
    }
    public CheckForLongPressHelper(View v) {
        mView = v;
    }

    /**
     * Sets the position where the long press has happened
     */
    public void setLongPressPosition(float x, float y){
        mLongPressPosition.x = x;
        mLongPressPosition.y = y;
    }

    public PointF getLongPressPosition(){
        return mLongPressPosition;
    }

    /**
     * Overrides the default long press timeout.
     */
    public void setLongPressTimeout(int longPressTimeout) {
        mLongPressTimeout = longPressTimeout;
    }

    /**
     * Posts a delayed CheckForLongPress runnable.
     */
    public void postCheckForLongPress() {
        mHasPerformedLongPress = false;
        if (mPendingCheckForLongPress == null) {
            mPendingCheckForLongPress = new CheckForLongPress();
        }
        mView.postDelayed(mPendingCheckForLongPress, mLongPressTimeout);
    }

    /**
     * Cancels the longpress
     */
    public void cancelLongPress() {
        mHasPerformedLongPress = false;
        canLongPress = false;
        if (mPendingCheckForLongPress != null) {
            mView.removeCallbacks(mPendingCheckForLongPress);
            mPendingCheckForLongPress = null;
        }
    }

    //Getters
    public boolean isCanLongPress(){
        return canLongPress;
    }
    public boolean hasPerformedLongPress() {
        return mHasPerformedLongPress;
    }
}
