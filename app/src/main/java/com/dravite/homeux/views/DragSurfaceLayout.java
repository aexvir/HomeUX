package com.dravite.homeux.views;

import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.LauncherLog;
import com.dravite.homeux.top_fragments.FolderListFragment;
import com.dravite.homeux.drawerobjects.structures.FolderPagerAdapter;
import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.add_quick_action.AddQuickActionActivity;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.DrawerObject;
import com.dravite.homeux.drawerobjects.structures.AppDrawerPageFragment;
import com.dravite.homeux.drawerobjects.structures.AppDrawerPagerAdapter;
import com.dravite.homeux.drawerobjects.structures.ClickableAppWidgetHostView;
import com.dravite.homeux.general_helpers.JsonHelper;

/**
 * Created by Johannes on 05.09.2015.
 *
 */
public class DragSurfaceLayout extends FrameLayout implements View.OnDragListener {

    //CONSTANTS
    private static final String TAG = DragSurfaceLayout.class.getName();
    private static final int PAGE_CHANGE_HOVER_MARGIN = 36;

    //HANDLERS/RUNNABLES
    Handler mPageChangeHandler = new Handler();
    Runnable mPageChangeRunnable = new Runnable() {
        @Override
        public void run() {
            changePage();
        }
    };

    //VIEWS
    CustomGridLayout mAppGrid;
    CustomGridLayout mInitialAppGrid;
    ObjectDropButtonStrip mObjectDropButtonStrip;
    QuickAppBar mQuickAppBar;
    ViewPager mPager;
    AppDrawerPageFragment mFocussedPage;
    LauncherActivity mActivity;
    FolderListFragment mFolderList;

    //INT
    int mStartPage;
    int mPageChangeDelta;
    private int mDragType = DragType.TYPE_APP_PAGE;

    //FLOAT
    float mOldY = 0; //For fling remove
    float mDragDeltaY = 0; //For fling remove

    //STRING
    String mWidgetRemoveAreaHoveredButton = "nothing";

    //BOOLEAN
    boolean mCanChangePage = true;
    boolean mShowPrevIndicator;
    boolean mShowNextIndicator;
    boolean mCanRemoveDragShadow = true;
    boolean mRemoveAnimationHasStarted = false;
    boolean mCanRedoFreeGrid = true;
    boolean doDispatchDragEventOverride = true;
    private boolean mIsOnAppPage = true;
    public boolean isDragging = false;

    //LISTENERS
    OnDragListener tmpOnDragListener;
    private DragDropListenerAppDrawer mListener;
    private DragDropListenerQuickApp mListenerQA;
    private DragDropListenerFolder mListenerFolder;

    /**
     * Being passed at {@link DragSurfaceLayout#startDrag(int)}. Determines the type of object being dragged.
     */
    public static class DragType {
        public static final int TYPE_APP_PAGE = 0;
        public static final int TYPE_QA = 1;
        public static final int TYPE_FOLDER = 2;
    }

    /**
     * A DragDropListener for dragging an app or widget
     */
    public interface DragDropListenerAppDrawer {
        void onStartDrag(View dragView, DrawerObject data, ViewPager focusPager);

        void onEndDrag();
    }

    /**
     * A DragDropListener for dragging a QuickApp
     */
    public interface DragDropListenerQuickApp {
        void onStartDrag(View dragView);

        void onEndDrag();
    }

    /**
     * A DragDropListener for dragging a Folder
     */
    public interface DragDropListenerFolder {
        void onStartDrag(View dragView, FolderStructure.Folder folder);

        void onEndDrag();
    }

    // DEFAULT CONSTRUCTORS
    public DragSurfaceLayout(Context context) {
        this(context, null);
    }

    public DragSurfaceLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragSurfaceLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DragSurfaceLayout(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setOnDragListener(this);
        if(!isInEditMode()) {
            mActivity = (LauncherActivity) context;
        }
    }

    /////////////////////////////////////////////////////////////////
    ////////
    ////////    SETTERS
    ////////
    /////////////////////////////////////////////////////////////////

    public void setObjectDropButtonStrip(ObjectDropButtonStrip area) {
        mObjectDropButtonStrip = area;
    }

    public void setQuickActionBar(QuickAppBar bar) {
        mQuickAppBar = bar;
    }

    public void setDragDropListenerAppdrawer(DragDropListenerAppDrawer listener) {
        this.mListener = listener;
    }

    public void setDragDropListenerQuickApp(DragDropListenerQuickApp listener) {
        this.mListenerQA = listener;
    }

    public void setDragDropListenerFolder(DragDropListenerFolder listener) {
        this.mListenerFolder = listener;
    }

    public void setPager(ViewPager pager) {
        mPager = pager;
    }

    public void setFocussedPagerGrid(CustomGridLayout appGrid) {
        mAppGrid = appGrid;
    }

    public void setInitialAppGrid(CustomGridLayout mInitialAppGrid) {
        this.mInitialAppGrid = mInitialAppGrid;
    }

    public void setFolderListFragment(FolderListFragment folderListFragment) {
        this.mFolderList = folderListFragment;
    }

    public void setIsOnAppPage(boolean isOnAppPage) {
        mIsOnAppPage = isOnAppPage;
    }


    /////////////////////////////////////////////////////////////////
    ////////
    ////////    GETTERS
    ////////
    /////////////////////////////////////////////////////////////////

    public AppDrawerPageFragment getFocussedPage() {
        return (AppDrawerPageFragment) mPager.getAdapter().instantiateItem(mPager, mPager.getCurrentItem());
    }

    /**
     * Scrolls the ViewPager of the current Folder back to its original position where the drag/drop was initiated.
     */
    public void resetPager() {
        mPager.setCurrentItem(mStartPage, true);
        mFocussedPage = getFocussedPage();
        mFocussedPage.mAppGrid.mDragHoverHandler = mAppGrid.mDragHoverHandler;
        mFocussedPage.mAppGrid.mDragShadowView = mAppGrid.mDragShadowView;
        mFocussedPage.mAppGrid.hoverPoint = mAppGrid.hoverPoint;
        mFocussedPage.mAppGrid.mDragData = mAppGrid.mDragData;
        mFocussedPage.mAppGrid.mDragChanged = mAppGrid.mDragChanged;
        mFocussedPage.mAppGrid.mDragView = mAppGrid.mDragView;
        mFocussedPage.mAppGrid.mDragStartPos = mAppGrid.mDragStartPos;
        mFocussedPage.mAppGrid.hLocY = mAppGrid.hLocY;
        mFocussedPage.mAppGrid.hLocX = mAppGrid.hLocX;
        mFocussedPage.mAppGrid.size = mAppGrid.size;
        mFocussedPage.mAppGrid.pos = mAppGrid.pos;
        mFocussedPage.mAppGrid.mHasDragged = mAppGrid.mHasDragged;
        mFocussedPage.mAppGrid.mDragStartIndex = mAppGrid.mDragStartIndex;

        setFocussedPagerGrid(mFocussedPage.mAppGrid);
        mCanChangePage = true;
    }

    /**
     * Scrolls the ViewPager of the current Folder by a given amount ({@link DragSurfaceLayout#mPageChangeDelta}).
     */
    private void changePage() {
        if (mPager.getCurrentItem() + mPageChangeDelta >= 0 && mPager.getCurrentItem() + mPageChangeDelta < mPager.getAdapter().getCount()) {
            mAppGrid.redoFreeGrid();
            mPager.setCurrentItem(mPager.getCurrentItem() + mPageChangeDelta, true);
            mFocussedPage = getFocussedPage();
            removeView(mAppGrid.mDragShadowView);
            mFocussedPage.mAppGrid.mDragHoverHandler = mAppGrid.mDragHoverHandler;
            mFocussedPage.mAppGrid.mDragShadowView = mAppGrid.mDragShadowView;
            mFocussedPage.mAppGrid.hoverPoint = new int[]{Integer.MIN_VALUE, Integer.MIN_VALUE};
            mFocussedPage.mAppGrid.mDragData = mAppGrid.mDragData;
            mFocussedPage.mAppGrid.mDragChanged = mAppGrid.mDragChanged;
            mFocussedPage.mAppGrid.mDragView = mAppGrid.mDragView;
            mFocussedPage.mAppGrid.mDragClickPoint = mAppGrid.mDragClickPoint;
            mFocussedPage.mAppGrid.mDragStartPos = mAppGrid.mDragStartPos;
            mFocussedPage.mAppGrid.hLocY = mAppGrid.hLocY;
            mFocussedPage.mAppGrid.hLocX = mAppGrid.hLocX;
            mFocussedPage.mAppGrid.size = mAppGrid.size;
            mFocussedPage.mAppGrid.pos = mAppGrid.pos;
            mFocussedPage.mAppGrid.mHasDragged = mAppGrid.mHasDragged;
            mFocussedPage.mAppGrid.mDragStartIndex = mAppGrid.mDragStartIndex;

            setFocussedPagerGrid(mFocussedPage.mAppGrid);
            mCanChangePage = true;
        }

        if (mPager.getCurrentItem() == 0) {
            hidePrevPageIndicator();
        } else {
            showPrevPageIndicator();
        }

        if (mPager.getCurrentItem() == mPager.getAdapter().getCount() - 1) {
            hideNextPageIndicator();
        } else {
            showNextPageIndicator();
        }
    }

    /**
     * Hides the left bar indicating another page to drag an object to. (Active while dragging)
     */
    public void hidePrevPageIndicator() {
        final View indicator = findViewWithTag("prev");
        if (indicator != null) {
            mShowPrevIndicator = false;
            indicator.animate().translationX(-(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics())).withEndAction(new Runnable() {
                @Override
                public void run() {
                    removeView(indicator);
                }
            });
        }
    }

    /**
     * Hides the right bar indicating another page to drag an object to. (Active while dragging)
     */
    public void hideNextPageIndicator() {
        final View indicator = findViewWithTag("next");
        if (indicator != null) {
            mShowNextIndicator = false;
            indicator.animate().translationX((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics())).withEndAction(new Runnable() {
                @Override
                public void run() {
                    removeView(indicator);
                }
            });
        }
    }

    /**
     * Shows the right bar indicating another page to drag an object to. (Active while dragging)
     */
    public void showNextPageIndicator() {
        mShowNextIndicator = true;
        if (findViewWithTag("next") != null || mAppGrid.getGridType() == CustomGridLayout.GridType.TYPE_APPS_ONLY) {
            return;
        }
        final View indicator = new View(getContext());
        indicator.setTag("next");
        indicator.setBackground(getContext().getDrawable(R.drawable.next_page_indicator));
        indicator.setElevation((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        LayoutParams params = new LayoutParams((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()), mAppGrid.getMeasuredHeight()
                - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16 + 8, getResources().getDisplayMetrics()));
        params.gravity = Gravity.END | Gravity.BOTTOM;
        params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16 + 8, getResources().getDisplayMetrics());

        indicator.setTranslationX((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics()));
        addView(indicator, params);
        indicator.post(new Runnable() {
            @Override
            public void run() {
                indicator.animate().translationX(LauncherUtils.dpToPx(14, getContext()));
            }
        });
    }

    /**
     * Shows the left bar indicating another page to drag an object to. (Active while dragging)
     */
    public void showPrevPageIndicator() {
        mShowPrevIndicator = true;
        if (findViewWithTag("prev") != null || mAppGrid.getGridType() == CustomGridLayout.GridType.TYPE_APPS_ONLY) {
            return;
        }
        final View indicator = new View(getContext());
        indicator.setTag("prev");
        indicator.setBackground(getContext().getDrawable(R.drawable.prev_page_indicator));
        indicator.setElevation((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics()));
        LayoutParams params = new LayoutParams(
                (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics()),
                mAppGrid.getMeasuredHeight() - (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16 + 8, getResources().getDisplayMetrics()));
        params.gravity = Gravity.START | Gravity.BOTTOM;
        params.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16 + 8, getResources().getDisplayMetrics());

        indicator.setTranslationX(-(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 28, getResources().getDisplayMetrics()));
        addView(indicator, params);
        indicator.post(new Runnable() {
            @Override
            public void run() {
                indicator.animate().translationX(-LauncherUtils.dpToPx(14, getContext()));
            }
        });

    }

    /**
     * Initiates a drag on this View.
     * @param dragType The type of object being dragged.
     */
    public void startDrag(int dragType) {
        mDragType = dragType;
        startDrag(ClipData.newPlainText("dt", "" + dragType), new DragShadowBuilder(), null, 0);
    }

    /**
     * {@link DragSurfaceLayout#doDispatchDragEventOverride} Sets the behavior, whether {@link DragSurfaceLayout#dispatchStartAppOrWidget()}
     * and {@link DragSurfaceLayout#dispatchEndAppOrWidget()} should be called or not.
     * @param val true to override
     */
    public void overrideDispatch(boolean val) {
        doDispatchDragEventOverride = val;
    }

    @Override
    public void setOnDragListener(OnDragListener l) {
        tmpOnDragListener = l;
        super.setOnDragListener(l);
    }

    @Override
    public boolean dispatchDragEvent(DragEvent event) {
        if (doDispatchDragEventOverride && mDragType == DragType.TYPE_APP_PAGE) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    if (mAppGrid == null)
                        return false;
                    dispatchStartAppOrWidget();
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    dispatchEndAppOrWidget();
                    break;
            }
        } else if (!doDispatchDragEventOverride && tmpOnDragListener instanceof CustomGridLayout.GripDragListener) {
            //Use the given custom dragListener.
            switch (event.getAction()) {
                case DragEvent.ACTION_DRAG_STARTED:
                    tmpOnDragListener.onDrag(null, event);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    tmpOnDragListener.onDrag(null, event);
                    break;
            }
        }
        return super.dispatchDragEvent(event);
    }

    @Override
    public boolean onDrag(View v, DragEvent event) {
        switch (mDragType) {
            case DragType.TYPE_APP_PAGE:
                return onDragAppOrWidget(event);
            case DragType.TYPE_QA:
                return onDragQuickAction(event);
            case DragType.TYPE_FOLDER:
                return onDragFolder(event);
        }
        return false;
    }

    /**
     * Displays a "splash" like animation when dropping an item (currently only applicable for QuickApps and experimental).
     * @param durationMS The splash duration in milliseconds
     * @param scale The target splash scale.
     */
    void animateDragViewDrop(final int durationMS, final int scale) {
        final ImageView tmpEffectView = new ImageView(getContext());
        Bitmap bmp = mAppGrid.mDragView.getDrawingCache();
        tmpEffectView.setLayoutParams(new LayoutParams(bmp.getWidth(), bmp.getHeight()));
        tmpEffectView.setImageBitmap(bmp);
        addView(tmpEffectView);
        tmpEffectView.setTranslationX(mAppGrid.mDragView.getTranslationX());
        tmpEffectView.setTranslationY(mAppGrid.mDragView.getTranslationY());
        tmpEffectView.post(new Runnable() {
            @Override
            public void run() {
                tmpEffectView.animate().scaleX(scale).scaleY(scale).alpha(0).setDuration(durationMS).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        if (tmpEffectView != null && tmpEffectView.getParent() != null)
                            removeView(tmpEffectView);
                    }
                });
            }
        });
    }

    /**
     * A split-off part of {@link #dispatchEndAppOrWidget()}, hides next and previous page drop targets and runs all reset animations.
     */
    void dropResetViews() {
        isDragging = false;
        hideNextPageIndicator();
        hidePrevPageIndicator();

        if (mQuickAppBar.stopHovering(mAppGrid.mDragData)) {
            animateDragViewDrop(130, 8);
        }
        hideNextPageIndicator();
        hidePrevPageIndicator();
        mObjectDropButtonStrip.exitHover();
        mAppGrid.mDragHoverHandler.removeCallbacksAndMessages(null);

        mActivity.mIndicator.animate().scaleX(1f / mPager.getAdapter().getCount()).translationX(mPager.getCurrentItem() * (mActivity.mAppBarLayout.getMeasuredWidth() / mPager.getAdapter().getCount()));

        if (mAppGrid.mDragShadowView != null) {
            mAppGrid.mDragShadowView.animate().scaleY(0).scaleX(0).alpha(0).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    removeView(mAppGrid.mDragShadowView);
                }
            });
        }
    }

    /**
     * Checks for a drag targeting any folder in the {@link LauncherActivity#mFolderDropAdapter}.
     * @return Whether any folder has been focussed or not.
     */
    boolean dropCheckFolderDropAdapter() {
        if (mActivity.mFolderDropAdapter != null && mActivity.mFolderDropAdapter.getHovered() != -1 && mAppGrid.mDragData instanceof Application) {
            mActivity.addAppToFolder((Application) mAppGrid.mDragData, mActivity.mFolderDropAdapter.getHovered());

            LauncherActivity.mFolderStructure.addFolderAssignments(LauncherActivity.mFolderStructure.folders.get(mActivity.mFolderDropAdapter.getHovered()));

            mActivity.mFolderDropAdapter.resetHovered();
            if (mAppGrid.mDragView != null)
                mAppGrid.mDragView.animate().scaleX(0).scaleY(0).alpha(0).setDuration(100).withEndAction(new Runnable() {
                    @Override
                    public void run() {

                        JsonHelper.saveFolderStructure(mActivity, LauncherActivity.mFolderStructure);
                        mActivity.refreshAllFolder(mActivity.mHolder.gridHeight, mActivity.mHolder.gridWidth);

                        removeAllViews();
                        mListener.onEndDrag();
                    }
                });
            return true;
        }
        return false;
    }

    /**
     * Checks if an app should be removed or not.
     * @return whether the app should be removed.
     */
    boolean dropCheckRemoval() {
        boolean animateToTop = false;
        if(mDragDeltaY>LauncherUtils.dpToPx(25, mActivity) && !mActivity.isInAllFolder()){
            LauncherLog.d(TAG, "Flung away an object, because delta is " + mDragDeltaY + ". Deleting now.");
            mWidgetRemoveAreaHoveredButton = "remove";
            animateToTop = true;
        }
        mOldY = 0;
        mDragDeltaY = 0;
        return mObjectDropButtonStrip.doRemove(mAppGrid.mDragData, mAppGrid.mDragView, mWidgetRemoveAreaHoveredButton, mAppGrid, mPager, mActivity.mPager.getCurrentItem(), mPager.getCurrentItem(), true, new Runnable() {
            @Override
            public void run() {
                mAppGrid.normalizeGrid();
                mAppGrid.redoFreeGrid();
                ((AppDrawerPagerAdapter) mPager.getAdapter()).removeEmptyPages();
                mListener.onEndDrag();

                removeAllViews();
                JsonHelper.saveFolderStructure(mActivity, LauncherActivity.mFolderStructure);
            }
        }, animateToTop);
    }

    /**
     * This method is called when an app has been dropped at any place other than its initial position.
     */
    void dropDispatchChanged() {
        final int targX = mAppGrid.hLocX,
                targY = mAppGrid.hLocY;
        int[] target = new int[]{mAppGrid.hLocY * (mAppGrid.size[1] / mAppGrid.getRowCount()), mAppGrid.hLocX * (mAppGrid.size[0] / mAppGrid.getColumnCount())};//mAppGrid.getCellCoords(mAppGrid.hLocY, mAppGrid.hLocX, 0.8f);
        mInitialAppGrid.fixFreeGrid();
        mAppGrid.fixFreeGrid();

        if (mAppGrid.mDragView == null)
            return;
        mAppGrid.mDragView.animate().x(target[1] + mAppGrid.pos.x).y(target[0] + mAppGrid.pos.y).scaleX(1).scaleY(1).setDuration(200).setInterpolator(new DecelerateInterpolator()).translationZ(0).alpha(mIsOnAppPage ? 1 : 0).withEndAction(new Runnable() {
            @Override
            public void run() {
                LauncherActivity.mFolderStructure.folders.get(mActivity.mPager.getCurrentItem()).pages.get(mPager.getCurrentItem()).items.remove(mAppGrid.mDragData);
                mAppGrid.freeCells(targY, targX, mAppGrid.mDragData.mGridPosition.rowSpan, mAppGrid.mDragData.mGridPosition.colSpan);
                mAppGrid.mDragData.mGridPosition.row = targY;
                mAppGrid.mDragData.mGridPosition.col = targX;
                LauncherActivity.mFolderStructure.folders.get(mActivity.mPager.getCurrentItem()).pages.get(mPager.getCurrentItem()).items.add(mAppGrid.mDragData);

                mAppGrid.addObject(mAppGrid.mDragData, 1, new AccelerateInterpolator(), 0, new Runnable() {
                    @Override
                    public void run() {
                        removeAllViews();
                    }
                });

                //clear stuff
                mAppGrid.mDragView = null;
                mAppGrid.hoverPoint[0] = -1;
                mAppGrid.hoverPoint[1] = -1;

                mAppGrid.mDragShadowView = null;
                JsonHelper.saveFolderStructure(mActivity, LauncherActivity.mFolderStructure);
            }
        });
    }

    /**
     * This method is called when an app has been dropped back onto its initial position.
     */
    void dropDispatchUnchanged() {
        resetPager();
        //mAppGrid.redoFreeGrid();
        if (mAppGrid.mDragView == null)
            return;
        mAppGrid.mDragView.animate().x(mAppGrid.mDragStartPos[0]).y(mAppGrid.mDragStartPos[1]).scaleX(1).scaleY(1).setDuration(200).setInterpolator(new DecelerateInterpolator()).alpha(mIsOnAppPage ? 1 : 0).withEndAction(new Runnable() {
            @Override
            public void run() {


                mAppGrid.addObject(mAppGrid.mDragData, 1, new AccelerateInterpolator(), 0, new Runnable() {
                    @Override
                    public void run() {
                        removeAllViews();
                    }
                });

                if ((mAppGrid.mDragView instanceof ClickableAppWidgetHostView)) {
                    mAppGrid.showResizeGrips(mAppGrid.mDragData); //
                }

                //clear stuff
                mAppGrid.mDragView = null;
                mAppGrid.hoverPoint[0] = -1;
                mAppGrid.hoverPoint[1] = -1;

                mAppGrid.mDragShadowView = null;

            }
        });
    }

    /**
     * Called on {@link DragEvent#ACTION_DRAG_ENDED} when an app has been dragged.
     */
    void dispatchEndAppOrWidget() {

        dropResetViews();

        if (dropCheckFolderDropAdapter()) {return;}
        if (dropCheckRemoval()) return;

        LauncherActivity.mFolderStructure.folders.get(mActivity.mPager.getCurrentItem()).pages.get(mPager.getCurrentItem()).items.add(mAppGrid.mDragData);

        final boolean coordsEqual = (mAppGrid.hoverPoint[0] == mAppGrid.mDragData.mGridPosition.row && mAppGrid.hoverPoint[1] == mAppGrid.mDragData.mGridPosition.col && mPager.getCurrentItem() == mStartPage);
        if (coordsEqual || !mAppGrid.mDragChanged) {
            dropDispatchUnchanged();
        } else {
            dropDispatchChanged();
        }
        mListener.onEndDrag();

        postDelayed(new Runnable() {
            @Override
            public void run() {
                ((AppDrawerPagerAdapter) mPager.getAdapter()).removeEmptyPages();
            }
        }, 200);

        //mActivity.fetchNotifications();
    }

    /**
     * Called on {@link DragEvent#ACTION_DRAG_STARTED} when an app is being dragged.
     */
    void dispatchStartAppOrWidget() {
        mAppGrid.mHasDragged = false;
        setPager(mAppGrid.mPager);
        mStartPage = mPager.getCurrentItem();
        mAppGrid.mVibrator.vibrate(CustomGridLayout.VIBRATE_LIFT);
        isDragging = true;
        mPageChangeDelta = 0;

        if (mAppGrid.getGridType() == CustomGridLayout.GridType.TYPE_WIDGETS) {
            ((AppDrawerPagerAdapter) mPager.getAdapter()).addPage();
        }
        mPageChangeRunnable.run();
        mOldY = mAppGrid.mDragStartPos[1];
        mListener.onStartDrag(mAppGrid.mDragView, mAppGrid.mDragData, mPager);

        mActivity.mIndicator.animate().scaleX(1f / mPager.getAdapter().getCount()).translationX(mPager.getCurrentItem() * (mActivity.mAppBarLayout.getMeasuredWidth() / mPager.getAdapter().getCount()));
        LauncherActivity.mFolderStructure.folders.get(mActivity.mPager.getCurrentItem()).pages.get(mStartPage).items.remove(mAppGrid.mDragData);
    }

    /**
     * Drag action for an app icon or a widget. Only used on {@link DragEvent#ACTION_DRAG_LOCATION}.
     * @param event "inherited" from {@link #onDrag(View, DragEvent)}.
     * @return "inherited" from {@link #onDrag(View, DragEvent)}.
     */
    public boolean onDragAppOrWidget(DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
            case DragEvent.ACTION_DROP:
            case DragEvent.ACTION_DRAG_ENDED:
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                if (mAppGrid.mDragView == null || mAppGrid.mDragData == null) {
                    //Check for data validity
                    return false;
                }

                mDragDeltaY = mOldY-event.getY();
                LauncherLog.d(TAG, "Difference: " +  mDragDeltaY);
                mOldY = event.getY();

                // The upper appearing drop target's action String.
                mWidgetRemoveAreaHoveredButton = mObjectDropButtonStrip.doHover(mAppGrid.mDragData, ((int) event.getX()), ((int) event.getY()));

                if (mAppGrid.mDragView instanceof AppIconView && mAppGrid.mDragData.getObjectType() == DrawerObject.TYPE_APP) {
                    // If the user is dragging an app, check the QuickAppBar
                    // and the FolderDropLayout for any hovering event.
                    if (mQuickAppBar != null)
                        mQuickAppBar.hoverAt(event.getX(), event.getY());

                    //FolderDropLayout actions
                    if (mActivity.isInFolderDropLocation(event.getX(), event.getY(), mQuickAppBar.mIsHovering || !mWidgetRemoveAreaHoveredButton.equals("nothing"))) {
                        mActivity.switchToFolderView();
                    } else {
                        mActivity.switchBackFromFolderView();
                    }
                }

                int[] appGridLoc = new int[2];
                mAppGrid.getLocationInWindow(appGridLoc);
                if (event.getY() >= appGridLoc[1] && event.getY() <= (appGridLoc[1] + mAppGrid.size[1] - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()))) {
                    mCanRemoveDragShadow = true;
                }
                if (mAppGrid.mDragClickPoint == null)
                    mAppGrid.mDragClickPoint = new PointF(0, 0);

                //Dragging the object with an offset defined by the relative initial touch position.
                mAppGrid.mDragView.setX(event.getX() - mAppGrid.mDragView.getWidth() / 2f - mAppGrid.mDragClickPoint.x);
                mAppGrid.mDragView.setY(event.getY() - mAppGrid.mDragView.getHeight() / 2f - mAppGrid.mDragClickPoint.y);

                //If necessary, initialize the drag cell
                if (mAppGrid.hLocX == Integer.MIN_VALUE || mAppGrid.hLocY == Integer.MIN_VALUE) {
                    mAppGrid.hLocY = mAppGrid.mDragData.mGridPosition.row;
                    mAppGrid.hLocX = mAppGrid.mDragData.mGridPosition.col;
                }

                int oldLocX = mAppGrid.hLocX;
                int oldLocY = mAppGrid.hLocY;

                //Zoom out correction
                float scale = Const.APP_GRID_ZOOM_OUT_SCALE;

                int calcY = ((int) ((event.getY() - appGridLoc[1] + TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics())))) - mAppGrid.mDragView.getMeasuredHeight() / 2;
                int calcX = ((int) ((event.getX() - appGridLoc[0]))) - mAppGrid.mDragView.getMeasuredWidth() / 2;

                mAppGrid.hLocX = Math.max(0, (int) Math.min(((calcX + 0.5f * mAppGrid.getColumnWidth() * scale - mAppGrid.mDragClickPoint.x)) / (mAppGrid.getColumnWidth() * scale), mAppGrid.getColumnCount() - mAppGrid.mDragData.mGridPosition.colSpan));
                mAppGrid.hLocY = Math.max(0, (int) Math.min(((calcY + 0.5f * mAppGrid.getRowHeight() * scale - mAppGrid.mDragClickPoint.y)) / (mAppGrid.getRowHeight() * scale), mAppGrid.getRowCount() - mAppGrid.mDragData.mGridPosition.rowSpan));

                mCanRemoveDragShadow = true;
                mCanRemoveDragShadow = ((event.getY() < appGridLoc[1] || event.getY() > (appGridLoc[1] + mAppGrid.size[1] - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 48, getResources().getDisplayMetrics()))) && mCanRemoveDragShadow);


                if (!mCanRemoveDragShadow && (mAppGrid.hLocY + mAppGrid.mDragData.mGridPosition.rowSpan - 1 < mAppGrid.getRowCount() && mAppGrid.hLocX >= 0) && mAppGrid.hLocX + mAppGrid.mDragData.mGridPosition.colSpan - 1 < mAppGrid.getColumnCount() && mAppGrid.hLocY >= 0) {

                    //Inside AppGrid
                    mRemoveAnimationHasStarted = false;
                    if (mIsOnAppPage)
                        mAppGrid.hoverAt(oldLocY, oldLocX, mAppGrid.hLocY, mAppGrid.hLocX);
                    else
                        removeView(mAppGrid.mDragShadowView);
                    mCanRedoFreeGrid = true;
                } else {

                    //Outside AppGrid
                    if (mCanRedoFreeGrid) {
                        mAppGrid.redoFreeGrid();
                        mCanRedoFreeGrid = false;
                    }
                    mAppGrid.mDragHoverHandler.removeCallbacksAndMessages(null);
                    mAppGrid.hoverPoint[0] = Integer.MIN_VALUE;
                    mAppGrid.hoverPoint[1] = Integer.MIN_VALUE;
                    mAppGrid.hoverPoint[0] = mAppGrid.mDragData.mGridPosition.row;
                    mAppGrid.hoverPoint[1] = mAppGrid.mDragData.mGridPosition.col;
                    removeView(mAppGrid.mDragShadowView);
                    mAppGrid.mDragChanged = false;
                    if (event.getY() < appGridLoc[1] || event.getY() > appGridLoc[1] + mAppGrid.getMeasuredHeight())
                        resetPager();
                }

                //Some code to check if the object is dragged like as if the user would like to change the page
                if (mIsOnAppPage && mAppGrid.getGridType() != CustomGridLayout.GridType.TYPE_APPS_ONLY) {
                    if (event.getX() >= getRight() - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PAGE_CHANGE_HOVER_MARGIN, getResources().getDisplayMetrics())) {
                        if (mCanChangePage) {

                            mCanChangePage = false;
                            mPageChangeDelta = 1;

                            mPageChangeHandler.postDelayed(mPageChangeRunnable, 600);
                        }
                    } else if (event.getX() <= TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PAGE_CHANGE_HOVER_MARGIN, getResources().getDisplayMetrics())) {
                        if (mCanChangePage) {
                            mCanChangePage = false;
                            mPageChangeDelta = -1;

                            mPageChangeHandler.postDelayed(mPageChangeRunnable, 600);
                        }
                    } else {

                        mPageChangeHandler.removeCallbacksAndMessages(null);
                        mCanChangePage = true;
                    }
                }

                if (event.getY() < appGridLoc[1]) {
                    if (!mRemoveAnimationHasStarted) {
                        mRemoveAnimationHasStarted = true;
                    }
                }
                break;
        }
        return true;
    }

    /**
     * Drag action for a QuickApp.
     * @param event "inherited" from {@link #onDrag(View, DragEvent)}.
     * @return "inherited" from {@link #onDrag(View, DragEvent)}.
     */
    public boolean onDragQuickAction(DragEvent event) {
        if (mQuickAppBar.mQaDragView != null) {
            switch (event.getAction()) {
                case DragEvent.ACTION_DROP:
                    break;
                case DragEvent.ACTION_DRAG_STARTED:
                    mListenerQA.onStartDrag(mQuickAppBar.mQaDragView);
                    break;
                case DragEvent.ACTION_DRAG_ENDED:
                    mQuickAppBar.endDrag();

                    int[] targetPos = mQuickAppBar.getAbsolutePositionCoords(mQuickAppBar.mHoverIndex);

                    if (mWidgetRemoveAreaHoveredButton.equals("remove")) {

                        //Should remove QuickApp?
                        mQuickAppBar.mQaDragView.animate().scaleX(0).scaleY(0).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                mQuickAppBar.mHoverIndex = -1;
                                removeView(mQuickAppBar.mQaDragView);
                                JsonHelper.saveQuickApps(getContext(), mQuickAppBar);
                                mQuickAppBar.stopHovering(mQuickAppBar.mQaDragView);
                            }
                        });
                    } else if (mWidgetRemoveAreaHoveredButton.equals("editQa")) {

                        //Should edit QuickApp?
                        int[] startPos = mQuickAppBar.getAbsolutePositionCoords(mQuickAppBar.mStartDragIndex);
                        mQuickAppBar.mHoverIndex = mQuickAppBar.mStartDragIndex;
                        mQuickAppBar.moveHandler.removeCallbacksAndMessages(null);
                        mQuickAppBar.redoFreeSpace();
                        mQuickAppBar.mQaDragView.animate().x(startPos[0]).y(startPos[1]).scaleX(1).scaleY(1).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                removeView(mQuickAppBar.mQaDragView);
                                mQuickAppBar.mIsHovering = true;
                                mQuickAppBar.stopHovering(mQuickAppBar.mQaDragView);

                                final Intent intent = new Intent(getContext(), AddQuickActionActivity.class);
                                intent.putExtra("index", mQuickAppBar.mStartDragIndex);

                                mActivity.startActivityForResult(intent, QuickAppBar.REQUEST_EDIT_QA);
                            }
                        });
                    } else if (targetPos[0] < 0 || !mQuickAppBar.mHasChanged) {

                        int[] startPos = mQuickAppBar.getAbsolutePositionCoords(mQuickAppBar.mStartDragIndex);
                        mQuickAppBar.moveHandler.removeCallbacksAndMessages(null);
                        mQuickAppBar.redoFreeSpace();
                        mQuickAppBar.mQaDragView.animate().x(startPos[0]).y(startPos[1]).scaleX(1).scaleY(1).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                mQuickAppBar.mHoverIndex = mQuickAppBar.mStartDragIndex;
                                removeView(mQuickAppBar.mQaDragView);
                                mQuickAppBar.mIsHovering = true;
                                mQuickAppBar.stopHovering(mQuickAppBar.mQaDragView);
                            }
                        });
                    } else {
                        mQuickAppBar.mQaDragView.animate().x(targetPos[0]).y(targetPos[1]).scaleX(1).scaleY(1).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                removeView(mQuickAppBar.mQaDragView);

                                mQuickAppBar.stopHovering(mQuickAppBar.mQaDragView);
                            }
                        });
                    }
                    mListenerQA.onEndDrag();
                    break;
                default:
                    //ACTION_DRAG_LOCATION
                    mQuickAppBar.mQaDragView.setAlpha(1);
                    mWidgetRemoveAreaHoveredButton = mObjectDropButtonStrip.doHover(null, (int) event.getX(), (int) event.getY());

                    float xPos = event.getX() - mQuickAppBar.mQaDragView.getMeasuredWidth() / 2f - mQuickAppBar.mTouchPosition.x;
                    float yPos = event.getY() - mQuickAppBar.mQaDragView.getMeasuredHeight() / 2f - mQuickAppBar.mTouchPosition.y;
                    mQuickAppBar.mQaDragView.setX(xPos);
                    mQuickAppBar.mQaDragView.setY(yPos);

                    mQuickAppBar.hoverAt(event.getX(), event.getY());
                    break;
            }
        }
        return true;
    }

    /**
     * Drag action for a Folder.
     * @param event "inherited" from {@link #onDrag(View, DragEvent)}.
     * @return "inherited" from {@link #onDrag(View, DragEvent)}.
     */
    public boolean onDragFolder(DragEvent event) {
        switch (event.getAction()) {
            case DragEvent.ACTION_DRAG_STARTED:
                if (mFolderList.mDragButton == null) {
                    return false;
                }
                mFolderList.mDragButton.animate().translationZ(LauncherUtils.dpToPx(4, mActivity)).alpha(0.8f).scaleY(1.1f).scaleX(1.1f).setDuration(150);
                mListenerFolder.onStartDrag(mFolderList.mDragButton, mFolderList.mDragFolder);
                break;
            case DragEvent.ACTION_DRAG_LOCATION:
                mWidgetRemoveAreaHoveredButton = mObjectDropButtonStrip.doHover(null, (int) (event.getX()), ((int) event.getY()));
                mFolderList.hover((int) (event.getX() - mFolderList.mTouchPosition.x), (int) (event.getY() - mFolderList.mTouchPosition.y));
                mFolderList.mDragButton.setX(event.getX() - mFolderList.mDragButton.getMeasuredWidth() / 2 - mFolderList.mTouchPosition.x);
                mFolderList.mDragButton.setY(event.getY() - mFolderList.mDragButton.getMeasuredHeight() / 2 - mFolderList.mTouchPosition.y);
                break;
            case DragEvent.ACTION_DRAG_ENDED:

                if (mObjectDropButtonStrip.doRemoveFolder(mFolderList.mDragButton, mFolderList.mDragFolder, mWidgetRemoveAreaHoveredButton)) {//null, mFolderList.mDragButton, mWidgetRemoveAreaHoveredButton.equals("remove")?"remove\n"+mFolderList.mDragFolder.folderName:mWidgetRemoveAreaHoveredButton, null, null, -1, -1)){
                    //removing folder.
                    mFolderList.mAdapter.cancelDrag();

                    mFolderList.mAdapter.notifyItemChanged(LauncherActivity.mFolderStructure.folders.indexOf(mFolderList.mDragFolder));
                    mFolderList.mDragButton.animate().alpha(0).scaleY(0).scaleX(0).setDuration(150).withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            removeAllViews();
                        }
                    });
                    mFolderList.mDragFolder = null;
                    mListenerFolder.onEndDrag();
                    return true;
                }

                //First save the new configuration
                ((FolderPagerAdapter) mActivity.mPager.getAdapter()).notifyPagesChanged();
                JsonHelper.saveFolderStructure(mActivity, LauncherActivity.mFolderStructure);
                mFolderList.mDragFolder = null;
                mActivity.mPager.setCurrentItem(LauncherActivity.mFolderStructure.folders.indexOf(LauncherActivity.mFolderStructure.getFolderWithName(mFolderList.mCurrentlySelectedFolder)), false);

                //Then cancel the drag and animate to the new target position
                mFolderList.mAdapter.cancelDrag();

                if(mFolderList.mDragStartIndex == mFolderList.mDragCurrentIndex){
                    mFolderList.mFolderList.scrollTo(0, mFolderList.mDragInitialScroll);
                }

                mFolderList.mDragButton.animate().alpha(1).scaleY(1f).scaleX(1f).x(mFolderList.mDragTargetPosition[0]).y(mFolderList.mDragTargetPosition[1]).translationZ(LauncherUtils.dpToPx(0, mActivity)).setDuration(150).withEndAction(new Runnable() {
                    @Override
                    public void run() {
                        removeAllViews();
                        mFolderList.mAdapter.select(mActivity.mPager.getCurrentItem());
                        //Then animate the whole drag UI back to where it was before
                        mListenerFolder.onEndDrag();
                        mActivity.revealColor(LauncherActivity.mFolderStructure.folders.get(mActivity.mPager.getCurrentItem()).headerImage);
                    }
                });
                break;
        }
        return true;
    }
}
