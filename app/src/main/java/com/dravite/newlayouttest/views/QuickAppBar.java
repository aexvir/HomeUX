package com.dravite.newlayouttest.views;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.Snackbar;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.LauncherLog;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.add_quick_action.AddQuickActionActivity;
import com.dravite.newlayouttest.drawerobjects.Application;
import com.dravite.newlayouttest.drawerobjects.DrawerObject;
import com.dravite.newlayouttest.drawerobjects.QuickAction;
import com.dravite.newlayouttest.general_helpers.JsonHelper;

/**
 * Created by Johannes on 17.09.2015
 * This ViewGroup displays a row of {@link QuickAppIcon}s.
 */
public class QuickAppBar extends ViewGroup implements View.OnLongClickListener, View.OnTouchListener{

    public static final int REQUEST_ADD_QA = 378;
    public static final int REQUEST_EDIT_QA = 379;
    private DragSurfaceLayout mDragSurfaceLayout;
    public View mQaDragView; // Temporary view when dragging any quickAppIcon
    public PointF mTouchPosition = new PointF(0,0); //Touch position as offset for drag
    private boolean mIsDragging = false;

    public int mHoverIndex = -1;
    public boolean mIsHovering = false;

    public int mRealChildCount;
    public boolean mHasChanged = false;
    public Handler moveHandler = new Handler();
    public int mStartDragIndex; //Helper integer to remember on which position the icon View was when a drag has been initiated

    public QuickAppBar(Context context) {
        this(context, null);
    }

    public QuickAppBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public QuickAppBar(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public QuickAppBar(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setBackgroundColor(Color.TRANSPARENT);
        setClipChildren(false);
        setClipToPadding(false);
    }

    public void setDragSurfaceLayout(DragSurfaceLayout mDragSurfaceLayout) {
        this.mDragSurfaceLayout = mDragSurfaceLayout;
    }


    @Override
    public boolean onLongClick(View v) {
        if(mDragSurfaceLayout!=null){
            mIsDragging = true;
            v.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
            Rect hitRect = new Rect();
            v.getHitRect(hitRect);

            QALayoutParams p = (QALayoutParams)v.getLayoutParams();

            int[] startPosition = getAbsolutePositionCoords(p.position);
            mStartDragIndex = p.position;

            mTouchPosition.x -= hitRect.width()/2;
            mTouchPosition.y -= hitRect.height()/2;

            FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(hitRect.width(), hitRect.height());
            removeView(v);
            mRealChildCount--;

            mQaDragView = v;

            mDragSurfaceLayout.addView(mQaDragView, params);
            mQaDragView.setX(startPosition[0]);
            mQaDragView.setY(startPosition[1]);
            mQaDragView.setScaleY(1.2f);
            mQaDragView.setScaleX(1.2f);
            mQaDragView.setAlpha(0);
            mDragSurfaceLayout.setOnDragListener(mDragSurfaceLayout);
            mDragSurfaceLayout.startDrag(DragSurfaceLayout.DragType.TYPE_QA);
        }
        return true;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(!mIsDragging) {
            mTouchPosition.x = event.getX();
            mTouchPosition.y = event.getY();
        }
        return false;
    }

    public void endDrag(){
        mIsDragging = false;
        setBackgroundColor(Color.TRANSPARENT);
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            QALayoutParams params = (QALayoutParams) child.getLayoutParams();

            int pos = params.position;

            int left = pos*(getWidth()- LauncherUtils.dpToPx(88, getContext()))/5;
            int right = (pos+1)*(getWidth()-LauncherUtils.dpToPx(88, getContext()))/5;
            int top = getHeight()-LauncherUtils.dpToPx(48, getContext());
            int bottom = getHeight();

            params.top = top;
            params.left = left;

            child.layout(left, top, right, bottom);
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        measureChildren(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * Adds a QuickApp without animating it.
     * @param quickApp The app to be added
     * @return true, if the QuickApp was added successfully
     */
    public boolean add(QuickAction quickApp){
        if(getChildCount() >= 5 || quickApp.qaIndex<0 || quickApp.qaIndex>4){
            return false;
        }

        QuickAppIcon qView = new QuickAppIcon(getContext());
        qView.setIconRes(getResources().getIdentifier(quickApp.iconRes, "drawable", getContext().getPackageName()));
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(quickApp.intentPackage, quickApp.intentClass));
        qView.setTag(intent);
        qView.setBackground(getContext().getDrawable(R.drawable.ripple));
        qView.setOnLongClickListener(this);
        qView.setOnTouchListener(this);

        QALayoutParams params = new QALayoutParams(0, 0, quickApp.qaIndex);

        addView(qView, params);
        mRealChildCount++;
        return true;
    }

    /**
     * Adds a given View to this bar at a specified position.
     * @param app
     * @param position
     * @return true, if the view has been added successfully
     */
    public boolean addAnimated(final View app, final int position){
        if(getChildCount() >= 5 || position<0 || position>4){
            return false;
        }
        QALayoutParams params = new QALayoutParams(555, 555, position);

        addView(app, params);
        app.setX(0);
        app.setY(0);
        mRealChildCount++;

        app.post(new Runnable() {
            @Override
            public void run() {
                app.animate().alpha(1).scaleX(1).scaleY(1).setDuration(150);
            }
        });

        JsonHelper.saveQuickApps(getContext(), this);

        return true;
    }

    /**
     * See {@link #addAnimated(View, int)}.
     * @param res The icon resource ID
     * @param app The assigned application for this Icon View.
     * @param position
     * @return true, if the view has been added successfully
     */
    public boolean addAnimated(int res, Application app, int position){
        if(getChildCount() >= 5 || position<0 || position>4){
            return false;
        }

        final QuickAppIcon qView = new QuickAppIcon(getContext());
        qView.setScaleX(0);
        qView.setScaleY(0);
        qView.setIconRes(res);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(app.packageName, app.className));
        qView.setTag(intent);
        qView.setBackground(getContext().getDrawable(R.drawable.ripple));
        qView.setOnLongClickListener(this);
        QALayoutParams params = new QALayoutParams(45, 45, position); //TODO

        addView(qView, params);
        mRealChildCount++;

        qView.post(new Runnable() {
            @Override
            public void run() {
                qView.animate().scaleX(1).scaleY(1).setDuration(150);
            }
        });

        JsonHelper.saveQuickApps(getContext(), this);

        endDrag();
        return true;
    }

    /**
     * Switches out an icon after a given delay with a scaling animation.
     * @param newIcon
     * @param position The position index of the icon View to change.
     * @param delay
     */
    public void replaceIconDelayed(final int newIcon, final int position, int delay){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                final QuickAppIcon child = (QuickAppIcon) getChildAtPosition(position);
                child.animate().scaleX(0).scaleY(0).setDuration(150).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        child.setIconRes(newIcon);
                        child.animate().scaleX(1).scaleY(1).setDuration(150);
                        JsonHelper.saveQuickApps(getContext(), QuickAppBar.this);
                    }
                });
            }
        }, delay);
    }

    public int getRealChildCount() {
        return mRealChildCount;
    }

    /**
     * Called when dragging an icon and hovering over this QuickAppBar. Including moving aside icons under the hover position (animated.
     * @param absoluteX absolute X position of the hover event
     * @param absoluteY absolute Y position of the hover event
     */
    public void hoverAt(float absoluteX, float absoluteY){
        int[] viewRect = new int[2];
        getLocationInWindow(viewRect);
        int bottom = viewRect[1]+LauncherUtils.dpToPx(80, getContext());
        if(absoluteY<bottom && absoluteY>bottom-LauncherUtils.dpToPx(48, getContext()) && getRealChildCount()<5) {

            setBackgroundColor(0x22ffffff);

            int relativeX = viewRect[0]-(int)absoluteX;
            final int hoverIndex = -relativeX/((getWidth()-LauncherUtils.dpToPx(88, getContext()))/5);

            if(hoverIndex!=mHoverIndex && hoverIndex>=0 && hoverIndex<=4){
                moveHandler.removeCallbacksAndMessages(null);
                redoFreeSpace();
                mHoverIndex = hoverIndex;
                int delay = getChildAtPosition(hoverIndex)==null?0:300;

                moveHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        freeSpace(hoverIndex);
                        mIsHovering = true;
                        mHasChanged=true;
                    }
                }, delay);

            } else if (!(hoverIndex>=0 && hoverIndex<=4)){

            }
        } else {
            if(mIsHovering)
                redoFreeSpace();
            LauncherLog.d("QuickActionBar", "Outside hovering.");
            mIsHovering = false;
            setBackgroundColor(0x00ffffff);
            mHoverIndex = -1;
        }
    }

    /**
     * Stops all hovering callbacks and either removes the dragged View or adds it to this QuickAppBar. (Or does nothing)
     * @param view
     */
    public void stopHovering(final View view){
        moveHandler.removeCallbacksAndMessages(null);
        mHasChanged = false;
        if(mHoverIndex==-1){
            redoFreeSpace();

            Snackbar.make(((LauncherActivity)getContext()).mCoordinatorLayout, "QuickApp removed.", Snackbar.LENGTH_SHORT).setAction("Undo", new OnClickListener() {
                @Override
                public void onClick(View v) {
                    addAnimated(view, mStartDragIndex);
                }
            }).show();
            return;
        }

        if(mIsHovering && getChildCount()<5){
            mIsHovering = false;
            View child = getChildAtPosition(mHoverIndex);
            if(child!=null) {
                QALayoutParams params = ((QALayoutParams) child.getLayoutParams());
                params.position = getNextFreePos(params.position);
                child.setLayoutParams(params);
                child.setTranslationX(0);
            }

            addAnimated(view, mHoverIndex);
            mHoverIndex = -1;
        }
        JsonHelper.saveQuickApps(getContext(), this);
    }

    /**
     * See {@link #stopHovering(View)}. Called for dragging an app from the AppDrawer
     * @param app
     * @return True, when the app has found a place in the QuickAppBar to be added to
     */
    public boolean stopHovering(DrawerObject app){
        if(!(app instanceof Application))
            return false;
        if(mIsHovering && getChildCount()<5){
            mIsHovering = false;
            View child = getChildAtPosition(mHoverIndex);
            if(child!=null) {
                QALayoutParams params = ((QALayoutParams) child.getLayoutParams());
                params.position = getNextFreePos(params.position);
                child.setLayoutParams(params);
                child.setTranslationX(0);
            }


            final Intent intent = new Intent(getContext(), AddQuickActionActivity.class);
            intent.putExtra("data", (Parcelable)app);
            intent.putExtra("index", mHoverIndex);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    endDrag();
                    ((LauncherActivity) getContext()).startActivityForResult(intent, REQUEST_ADD_QA);
                }
            }, 240);
            mHoverIndex = -1;
            return true;
        }
        return false;
    }

    /**
     * Moves all moved icons back to their original places.
     */
    public void redoFreeSpace(){
        for (int i = 0; i < getChildCount(); i++) {
            getChildAt(i).animate().translationX(0).setDuration(150);
        }
    }

    /**
     * @param position The Icon position index to check
     * @return The absolute x and y coordinates of the Icon in the given position.
     */
    public int[] getAbsolutePositionCoords(int position){
        int left = position*(getWidth()-LauncherUtils.dpToPx(88, getContext()))/5;
        int top = getHeight()-LauncherUtils.dpToPx(48, getContext());
        int[] globalPosition = new int[2];
        getLocationInWindow(globalPosition);


        return new int[]{left+globalPosition[0], top+globalPosition[1]};
    }

    /**
     * Tries to move away other views from the given position.
     * @param index
     */
    public void freeSpace(int index){
        if(getChildCount() >= 5){
            return;
        }

        View v = getChildAtPosition(index);
        if(v!=null){
            moveViewToNextFree(v);
        }
    }

    /**
     * @param position The Icon position
     * @return The View that is currently located at the given position or null if there is none.
     */
    public View getChildAtPosition(int position){
        for (int j = 0; j < getChildCount(); j++) {
            if(((QALayoutParams) getChildAt(j).getLayoutParams()).position==position){
                return getChildAt(j);
            }
        }
        return null;
    }

    /**
     * Animates the View to the next free position.
     * @param view
     */
    public void moveViewToNextFree(final View view){
        int cPos = ((QALayoutParams)view.getLayoutParams()).position;
        int freePos = getNextFreePos(cPos);

        int difference = freePos-cPos;
        int transX = difference*((getWidth() - LauncherUtils.dpToPx(88, getContext())) / 5);

        view.animate().translationX(transX).withEndAction(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    public int getNextFreePos(int cPos){
        int difference = Integer.MAX_VALUE;
        int pos = -1;
        for (int i = 0; i < 5; i++) {
            if(getChildAtPosition(i)==null){
                if(difference>Math.abs(i-cPos)){
                    difference = Math.abs(i-cPos);
                    pos = i;
                }
            }
        }
        return pos;
    }

    /**
     * Extends the default LayoutParams to also contain the QuickApp's position.
     */
    public static class QALayoutParams extends ViewGroup.LayoutParams{

        public int position;
        public float top, left;

        public QALayoutParams(int width, int height, int position){
            super(width, height);
            this.position = position;
        }
    }
}
