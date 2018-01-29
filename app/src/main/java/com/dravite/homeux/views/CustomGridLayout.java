package com.dravite.homeux.views;

import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.Space;
import android.support.v7.widget.GridLayout;
import android.util.AttributeSet;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.LauncherLog;
import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.DrawerObject;
import com.dravite.homeux.drawerobjects.Shortcut;
import com.dravite.homeux.drawerobjects.Widget;
import com.dravite.homeux.drawerobjects.structures.AppDrawerPagerAdapter;
import com.dravite.homeux.drawerobjects.structures.ClickableAppWidgetHostView;
import com.dravite.homeux.general_helpers.JsonHelper;
import com.dravite.homeux.views.helpers.AppWidgetContainer;
import com.dravite.homeux.views.helpers.CheckForLongPressHelper;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.UNSPECIFIED;
import static android.view.View.MeasureSpec.makeMeasureSpec;

/**
 * A GridLayout type of View that arranges {@link DrawerObject DrawerObjects} in a grid defined by their row, column, rowSpan and columnSpan. (See {@link Cell}).
 */
public class CustomGridLayout extends GridLayout implements View.OnLongClickListener {

    ///////////////////////////////////////////////////////////////////////////
    // Static Variables
    ///////////////////////////////////////////////////////////////////////////

    private static final String TAG = "CGridLayout";

    //Little vibration with a very short duration when lifting up an item when longPressing.
    public static final int VIBRATE_LIFT = 35;
    private static final int DEFAULT_ROW_COUNT = 5;
    private static final int DEFAULT_COLUMN_COUNT = 4;





    ///////////////////////////////////////////////////////////////////////////
    // Non-Static Variables
    ///////////////////////////////////////////////////////////////////////////

    //A temporary list for when dragging an item to remember, which other items have been moved out of the way
    public List<View> mMovedViewList = new ArrayList<>();
    //...And some others to remember cells and spans (especially important for widgets and such)
    private final List<CellSpan> mMovedViewSpans = new ArrayList<>();
    private final List<Cell> mMovedViewTargetCells = new ArrayList<>();

    //A representation of the grid's fill state. A "true" value means a cell is filled by something (can also mean that there is a widget overlapping)
    //Goes from top to bottom through each line from left to right.
    private boolean[] mCellUsed;
    //Same as mCellUsed, but for items that were moved out of the way when dragging. In use to not intercept and to be able to "redo" the dragging process.
    private boolean[] mCellUsedTemp;

    //The absolute pixel size of this GridLayout
    public int[] size = new int[2];
    //The pixel position of this GridLayout
    public Point pos;
    //The cell row and columns for when dragging any view.
    public int hLocX = Integer.MIN_VALUE,
            hLocY = Integer.MIN_VALUE;

    public Vibrator mVibrator;
    public AppWidgetContainer mAppWidgetContainer;

    //A layout being used to resize mWidgetViewListForRemovedCheck
    private RelativeLayout mResizeGrips;
    //The type of the grid (See CustomGridLayout.GridType)
    private int mGridType = GridType.TYPE_WIDGETS;
    //Are notification badges on app icons enabled?
    private boolean notificationsEnabled = false;

    //A view cache for app icon views. May improve performance
    ArrayList<AppIconView> applicationViews = new ArrayList<>();
    LayoutInflater mInflater;
    public SharedPreferences mPreferences;
    //A parallelized executor for checking if any apps have been removed after resuming
//    private ParallelExecutor mRemovedCheckExecutor = new ParallelExecutor(8);
//    private ParallelExecutor mRefreshExecutor = new ParallelExecutor(4);

    //Indicates whether any view has been dragged or not
    public boolean mHasDragged = false;
    //The pager containing this grid
    public ViewPager mPager;
    //The folder index and the page index (in mPager) of this grid
    private int mFolderPage, mAppPage;



    ///////////////////////////////////////////////////////////////////////////
    // Drag helper objects and variables
    ///////////////////////////////////////////////////////////////////////////
    private DragSurfaceLayout mDragSurface;
    public PointF mDragClickPoint; //the relative touch offset on the view when dragging
    public View mDragView; //the currently dragged view
    public DrawerObject mDragData; //the DrawerItem data of the dragged view for readding it after dragging
    public ImageView mDragShadowView; //A shadow for dragging. TODO: remove.
    public int mDragStartIndex; //The last view hierarchy index of the dragged view
    public int[] mDragStartPos = new int[2]; //The location where a drag started
    public int[] hoverPoint = new int[] {-1, -1}; //The location at which the dragged view currently is
    public Handler mDragHoverHandler = new Handler(); //A handler to delay the "move-away" motion for hovered views
    public boolean mDragChanged = false; //Indicates that a view has been dragged around somewhere other than it's initial position
    private Bitmap mDragShadow; //A shadow image of the dragged view. TODO: remove.
    private boolean mShadowIsAnimating = false; //see above. TODO: remove.





    ///////////////////////////////////////////////////////////////////////////
    // Nested classes
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Used when resizing a Widget to determine the direction of a resize handle to be dragged to
     */
    private enum Direction{
        DIRECTION_TOP,
        DIRECTION_BOTTOM,
        DIRECTION_LEFT,
        DIRECTION_RIGHT
    }

    /**
     * The type of grid this GridView represents. Either it can contain widgets and freely sort every object, or it can only contain app icons.
     */
    public static class GridType{
        public static final int TYPE_WIDGETS = 0;
        public static final int TYPE_APPS_ONLY = 1;
    }

    /**
     * Represents a cell in this GridView consisting of it's row and column.
     */
    public static class Cell {
        public final int row, column;

        public Cell(int r, int c){
            row=r;
            column=c;
        }

        /**
         * @return The column of this cell
         */
        public int x(){
            return column;
        }

        /**
         * @return The row of this cell
         */
        public int y(){
            return row;
        }
    }

    /**
     * Represents a cell span of an object in this GridView.
     */
    public static class CellSpan{
        public final int rowSpan, columnSpan;

        public CellSpan(int rowSpan, int columnSpan){
            this.rowSpan = rowSpan;
            this.columnSpan = columnSpan;
        }

        /**
         * @return The columnSpan
         */
        public int x(){
            return columnSpan;
        }

        /**
         * @return The rowSpan
         */
        public int y(){
            return rowSpan;
        }
    }

    /**
     * A state that is used in {@link #onSaveInstanceState()} and {@link #onRestoreInstanceState(Parcelable)} and which contains the folder index and app page index.
     */
    public static class GridState extends BaseSavedState{

        final int folderPage, appPage;

        /**
         * Constructor used when reading from a parcel. Reads the state of the superclass.
         *
         */
        public GridState(Parcel source) {
            super(source);
            folderPage = source.readInt();
            appPage = source.readInt();
        }

        /**
         * Constructor called by derived classes when creating their SavedState objects
         *
         * @param superState The state of the superclass of this view
         */
        public GridState(Parcelable superState, int folderPage, int appPage) {
            super(superState);
            this.folderPage = folderPage;
            this.appPage = appPage;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(folderPage);
            dest.writeInt(appPage);
        }


        public static final Parcelable.Creator<BaseSavedState> CREATOR =
                new Parcelable.Creator<BaseSavedState>() {
                    public BaseSavedState createFromParcel(Parcel in) {
                        return new BaseSavedState(in);
                    }

                    public BaseSavedState[] newArray(int size) {
                        return new BaseSavedState[size];
                    }
                };

    }

    /**
     * Extended GridView LayoutParams which can better provide rows, columns and spans without hacks
     */
    public class GridLayoutParams extends LayoutParams{

        public final int rowSpan, colSpan;
        public int row, col;
        public DrawerObject viewData;

        public GridLayoutParams(Cell cell, CellSpan cellSpan, DrawerObject data){
            this(cell.row, cell.column, cellSpan.rowSpan, cellSpan.columnSpan, data);
        }

        public GridLayoutParams(int row, int col, int rowSpan, int colSpan, DrawerObject data){
            super(spec(row, rowSpan), spec(col, colSpan));
            this.row = row;
            this.col = col;
            this.rowSpan = rowSpan;
            this.colSpan = colSpan;
            this.viewData = data;
        }


    }

    /**
     * An OnDragListener used for the resize grip handles.
     */
    public class GripDragListener implements OnDragListener{

        final DrawerObject mStartData;
        final Direction mDirection;
        final AppWidgetProviderInfo mWidgetInfo;
        float mStartPos;
        float mCurrentPos;
        float mStartSize;
        int mCurrentSize;
        int mCellSizeDelta;

        public GripDragListener(DrawerObject startData, Direction direction){
            mStartData = startData;
            mWidgetInfo = mAppWidgetContainer.mAppWidgetManager.getAppWidgetInfo(((Widget)startData).widgetId);
            mDirection = direction;
        }

        @Override
        public boolean onDrag(View v, DragEvent event) {
            final int diameter = (int)getContext().getResources().getDimension(R.dimen.handle_diameter);

            if (mDirection==Direction.DIRECTION_TOP || mDirection==Direction.DIRECTION_BOTTOM){
                switch (event.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        mStartPos = mResizeGrips.getY();
                        mStartSize = mResizeGrips.getLayoutParams().height;
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        if(mResizeGrips==null)
                            return false;
                        mCurrentSize = mDirection==Direction.DIRECTION_TOP?
                                ((int) (mStartSize + (mStartPos - event.getY())+diameter/2)):
                                ((int) (event.getY()-mStartPos+diameter/2));

                        if(mCurrentSize -diameter>getRowHeight()) {
                            mCurrentPos = event.getY()-diameter/2;
                        } else {
                            mCurrentSize = getRowHeight()+diameter;
                            mCurrentPos = mStartPos+mStartSize-diameter-getRowHeight();
                        }

                        if (mDirection == Direction.DIRECTION_TOP) {
                            mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(mResizeGrips.getLayoutParams().width, mCurrentSize));
                            mResizeGrips.setY(mCurrentPos);

                            final int rowDelta = Math.round((mStartPos - mCurrentPos)/ ((float) getRowHeight()));


                            if(mStartData.mGridPosition.row-rowDelta>-1 && mStartSize+rowDelta*getRowHeight()>=mWidgetInfo.minResizeHeight) {

                                if(rowDelta>mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);
                                    if(!isCellGridUsedFull(mStartData.mGridPosition.row-rowDelta, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan + rowDelta, mStartData.mGridPosition.colSpan)) {
                                        mCellSizeDelta = rowDelta;

                                        View v1 = getChildAt(mStartData.mGridPosition.row+mStartData.mGridPosition.rowSpan-1, mStartData.mGridPosition.col);
                                        removeView(v1);
                                        if(v1!=null) {
                                            addViewTmp(v1, mStartData.mGridPosition.row - rowDelta, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan + rowDelta, mStartData.mGridPosition.colSpan, mStartData);
                                            if (v1 instanceof ClickableAppWidgetHostView)
                                                ((ClickableAppWidgetHostView) v1).updateAppWidgetSize(null, getColumnWidth(), getRowHeight(), getColumnWidth() * getColumnCount(), getRowHeight() * getRowCount());
                                        }
                                    }
                                } else if(rowDelta<mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);
                                    mCellSizeDelta = rowDelta;

                                    View v1 = getChildAt(mStartData.mGridPosition.row+mStartData.mGridPosition.rowSpan-1, mStartData.mGridPosition.col);
                                    removeView(v1);
                                    if(v1!=null) {
                                        addViewTmp(v1, mStartData.mGridPosition.row - rowDelta, mStartData.mGridPosition.col,
                                                mStartData.mGridPosition.rowSpan + rowDelta, mStartData.mGridPosition.colSpan, mStartData);
                                        if (v1 instanceof ClickableAppWidgetHostView)
                                            ((ClickableAppWidgetHostView) v1).updateAppWidgetSize(null, getColumnWidth(), getRowHeight(), getColumnWidth() * getColumnCount(), getRowHeight() * getRowCount());
                                    }
                                }
                            }
                        } else {
                            mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(mResizeGrips.getLayoutParams().width, mCurrentSize));

                            final int rowDelta = Math.round((mCurrentSize - mStartSize)/ ((float) getRowHeight()));

                            if(mStartData.mGridPosition.row+mStartData.mGridPosition.rowSpan+rowDelta<=getRowCount() && mStartSize+rowDelta*getRowHeight()>=mWidgetInfo.minResizeHeight) {
                                if(rowDelta>mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);
                                    if(!isCellGridUsedFull(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan + rowDelta, mStartData.mGridPosition.colSpan)) {
                                        mCellSizeDelta = rowDelta;

                                        View v1 = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col);
                                        removeView(v1);
                                        if(v1!=null) {
                                            addViewTmp(v1, mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan + rowDelta, mStartData.mGridPosition.colSpan, mStartData);
                                            if (v1 instanceof ClickableAppWidgetHostView)
                                                ((ClickableAppWidgetHostView) v1).updateAppWidgetSize(null, getColumnWidth(), getRowHeight(), getColumnWidth() * getColumnCount(), getRowHeight() * getRowCount());
                                        }
                                    }
                                } else if(rowDelta<mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);

                                    mCellSizeDelta = rowDelta;

                                    View v1 = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col);
                                    removeView(v1);
                                    if(v1!=null) {
                                        addViewTmp(v1, mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan + rowDelta, mStartData.mGridPosition.colSpan, mStartData);
                                        if (v1 instanceof ClickableAppWidgetHostView)
                                            ((ClickableAppWidgetHostView) v1).updateAppWidgetSize(null, getColumnWidth(), getRowHeight(), getColumnWidth() * getColumnCount(), getRowHeight() * getRowCount());
                                    }
                                }

                            }
                        }
                        mResizeGrips.requestLayout();

                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        mStartData.mGridPosition.rowSpan +=mCellSizeDelta;
                        mStartData.mGridPosition.row -= mDirection==Direction.DIRECTION_TOP?mCellSizeDelta:0;
                        int finalDelta = mCellSizeDelta*getRowHeight();
                        fixFreeGrid();

                        View view = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col);
                        int id = view==null?-1:view.getId();

                        mAppWidgetContainer.mAppWidgetManager.notifyAppWidgetViewDataChanged(((Widget) mStartData).widgetId, id);

                        ValueAnimator animator = ValueAnimator.ofFloat(mCurrentPos, mStartPos-finalDelta);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                if(mDirection==Direction.DIRECTION_TOP) {
                                    mCurrentPos = (float) animation.getAnimatedValue();
                                    mResizeGrips.setY(mCurrentPos);
                                    mResizeGrips.requestLayout();
                                }
                            }
                        });
                        ValueAnimator animator2 = ValueAnimator.ofFloat(((float) mCurrentSize), mStartSize+finalDelta);
                        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                if(mResizeGrips==null || mResizeGrips.getLayoutParams()==null)
                                    return;
                                int h = ((int) (float) animation.getAnimatedValue());
                                if (mDirection == Direction.DIRECTION_TOP) {
                                    mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(mResizeGrips.getLayoutParams().width, h));
                                } else {
                                    mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(mResizeGrips.getLayoutParams().width, h));
                                }
                                mResizeGrips.requestLayout();
                            }
                        });

                        AnimatorSet set  = new AnimatorSet();
                        set.playTogether(animator, animator2);
                        set.start();
                        break;
                }
            } else {
                switch (event.getAction()){
                    case DragEvent.ACTION_DRAG_STARTED:
                        mStartPos = mResizeGrips.getX();
                        mStartSize = mResizeGrips.getLayoutParams().width;
                        break;
                    case DragEvent.ACTION_DRAG_LOCATION:
                        mCurrentSize = mDirection==Direction.DIRECTION_LEFT?
                                ((int) (mStartSize + (mStartPos - event.getX())+diameter/2)):
                                ((int) (event.getX()-mStartPos+diameter/2));

                        if(mCurrentSize -diameter>getColumnWidth()) {
                            mCurrentPos = event.getX()-diameter/2;
                        } else {
                            mCurrentSize = getColumnWidth()+diameter;
                            mCurrentPos = mStartPos+mStartSize-diameter-getColumnWidth();
                        }

                        if (mDirection == Direction.DIRECTION_LEFT) {
                            mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(mCurrentSize, mResizeGrips.getLayoutParams().height));
                            mResizeGrips.setX(mCurrentPos);

                            final int colDelta = Math.round((mStartPos - mCurrentPos)/ ((float) getColumnWidth()));

                            if(mStartData.mGridPosition.col-colDelta>-1 && mStartSize+colDelta*getColumnWidth()>=mWidgetInfo.minResizeWidth) {
                                if(colDelta>mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);
                                    if(!isCellGridUsedFull(mStartData.mGridPosition.row, mStartData.mGridPosition.col-colDelta, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan + colDelta)) {
                                        mCellSizeDelta = colDelta;

                                        View v1 = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col+mStartData.mGridPosition.colSpan-1);
                                        removeView(v1);
                                        if(v1!=null) {
                                            addViewTmp(v1, mStartData.mGridPosition.row, mStartData.mGridPosition.col - colDelta, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan + colDelta, mStartData);
                                        }
                                    }
                                } else if(colDelta<mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);
                                    mCellSizeDelta = colDelta;

                                    View v1 = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col+mStartData.mGridPosition.colSpan-1);
                                    removeView(v1);
                                    if(v1!=null) {
                                        addViewTmp(v1, mStartData.mGridPosition.row, mStartData.mGridPosition.col - colDelta, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan + colDelta, mStartData);
                                    }
                                }
                            }
                        } else {
                            mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(mCurrentSize, mResizeGrips.getLayoutParams().height));

                            final int colDelta = Math.round((mCurrentSize - mStartSize)/ ((float) getColumnWidth()));

                            if(mStartData.mGridPosition.col+mStartData.mGridPosition.colSpan+colDelta<=getColumnWidth() &&
                                    mStartSize+colDelta*getColumnWidth()>=mWidgetInfo.minResizeWidth) {
                                if(colDelta>mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);
                                    if(!isCellGridUsedFull(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan + colDelta)) {
                                        mCellSizeDelta = colDelta;

                                        View v1 = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col);
                                        removeView(v1);
                                        if(v1!=null) {
                                            addViewTmp(v1, mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan + colDelta, mStartData);
                                        }
                                    }
                                } else if(colDelta<mCellSizeDelta){
                                    freeCells(mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan);
                                    mCellSizeDelta = colDelta;

                                    View v1 = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col);
                                    removeView(v1);
                                    if(v1!=null) {
                                        addViewTmp(v1, mStartData.mGridPosition.row, mStartData.mGridPosition.col, mStartData.mGridPosition.rowSpan, mStartData.mGridPosition.colSpan + colDelta, mStartData);
                                    }
                                }
                            }
                        }
                        mResizeGrips.requestLayout();
                        break;
                    case DragEvent.ACTION_DRAG_ENDED:
                        mStartData.mGridPosition.colSpan +=mCellSizeDelta;
                        mStartData.mGridPosition.col -= mDirection==Direction.DIRECTION_LEFT?mCellSizeDelta:0;
                        int finalDelta = mCellSizeDelta*getColumnWidth();

                        fixFreeGrid();

                        View view = getChildAt(mStartData.mGridPosition.row, mStartData.mGridPosition.col);
                        int id = view==null?-1:view.getId();

                        mAppWidgetContainer.mAppWidgetManager.notifyAppWidgetViewDataChanged(((Widget)mStartData).widgetId, id);

                        ValueAnimator animator = ValueAnimator.ofFloat(mCurrentPos, mStartPos-finalDelta);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                if(mDirection==Direction.DIRECTION_LEFT) {
                                    mCurrentPos = (float) animation.getAnimatedValue();
                                    mResizeGrips.setX(mCurrentPos);
                                    mResizeGrips.requestLayout();
                                }
                            }
                        });
                        ValueAnimator animator2 = ValueAnimator.ofFloat(((float) mCurrentSize), mStartSize+finalDelta);
                        animator2.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int h = ((int) (float) animation.getAnimatedValue());
                                if (mDirection == Direction.DIRECTION_LEFT) {
                                    mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(h, mResizeGrips.getLayoutParams().height));
                                } else {
                                    mResizeGrips.setLayoutParams(new FrameLayout.LayoutParams(h, mResizeGrips.getLayoutParams().height));
                                }
                                mResizeGrips.requestLayout();
                            }
                        });

                        AnimatorSet set  = new AnimatorSet();
                        set.playTogether(animator, animator2);
                        set.start();
                        break;
                }
            }
            return true;
        }
    }





    ///////////////////////////////////////////////////////////////////////////
    // Constructors
    ///////////////////////////////////////////////////////////////////////////

    public CustomGridLayout(Context context){
        this(context, null);
    }

    public CustomGridLayout(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public CustomGridLayout(Context context, AttributeSet attrs, int defStyleAttr){
        super(context, attrs, defStyleAttr);
        setSaveEnabled(true);
        if(!isInEditMode())
            mVibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        mCellUsed = new boolean[getRowCount()*getColumnCount()];
        setLayerType(LAYER_TYPE_HARDWARE, null);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        notificationsEnabled = mPreferences.getBoolean(Const.Defaults.TAG_NOTIFICATIONS, Const.Defaults.getBoolean(Const.Defaults.TAG_NOTIFICATIONS));
        mInflater = LayoutInflater.from(context);
    }





    ///////////////////////////////////////////////////////////////////////////
    // Methods
    ///////////////////////////////////////////////////////////////////////////



    ///////////////////////////////////////////////////////////////////////////
    // External or helper methods
    ///////////////////////////////////////////////////////////////////////////
    /**
     * Inflates a new {@link AppIconView} for an {@link Application} to be assigned to.
     * If there is any AppIconView that has been inflated earlier but doesn't have a parent anymore, it's just returning that one.
     * @return An AppIconView for an Application to be assigned to.
     */
    public AppIconView inflateIcon(){
//        for(AppIconView aiv : applicationViews)
//            if(aiv.getParent() == null)
//                return aiv;
        return (AppIconView)mInflater.inflate(R.layout.icon, this, false);
    }



    ///////////////////////////////////////////////////////////////////////////
    // General getters and setters
    ///////////////////////////////////////////////////////////////////////////

    /** See {@link GridType}. */
    public int getGridType() {
        return mGridType;
    }

    /** Simplified {@link #getLocationInWindow(int[])}. */
    private Point getLocationInWindow() {
        int[] location = new int[2];
        super.getLocationInWindow(location);
        return new Point(location[0], location[1]);
    }

    /** A Quicker Way to parse the Context to {@link LauncherActivity}. */
    public LauncherActivity getMainActivity(){
        if(getContext() instanceof LauncherActivity) {
            return (LauncherActivity)getContext();
        }
        throw new RuntimeException("This GridLayout can only exist attached to a LauncherActivity.");
    }

    /** @return a unique View id.*/
    private int getUniqueID(){
        Random random = new Random(System.currentTimeMillis());
        int id;
        do {
            id = random.nextInt(Integer.MAX_VALUE);
        } while (getRootView().findViewById(id)!=null);
        return id;
    }

    /** See {@link GridType}. */
    public void setGridType(int mGridType) {
        this.mGridType = mGridType;
    }

    public void setPager(ViewPager pager){
        mPager = pager;
    }

    public void setDragSurface(DragSurfaceLayout dragSurface){
        mDragSurface = dragSurface;
    }

    public void setAppWidgetContainer(AppWidgetContainer appWidgetContainer){
        this.mAppWidgetContainer=appWidgetContainer;
    }

    public AppWidgetContainer getAppWidgetContainer() {
        return mAppWidgetContainer;
    }

    public int getRowHeight(){
        return getMeasuredHeight()/getRowCount();
    }

    public int getColumnWidth(){
        return getMeasuredWidth()/getColumnCount();
    }

    /** @return the maximum count of 1x1-items this grid can handle.*/
    private int getMaxItemCount(){
        return getColumnCount()*getRowCount();
    }

    private CellSpan getChildSpan(int row, int column){
        return getChildSpan(getChildAt(row, column));
    }

    /** @return Finds the cell of the given child */
    public Cell getChildCell(View child){
        if(child==null || !(child.getLayoutParams() instanceof GridLayoutParams))
            return new Cell(-1,-1);
        GridLayoutParams params = ((GridLayoutParams) child.getLayoutParams());
        return new Cell(params.row, params.col);
    }

    /** Finds the cell span of the given child */
    public CellSpan getChildSpan(View child){
        if(child==null || !(child.getLayoutParams() instanceof GridLayoutParams))
            return new CellSpan(-1,-1);
        GridLayoutParams params = ((GridLayoutParams) child.getLayoutParams());
        return new CellSpan(params.rowSpan, params.colSpan);
    }

    /**
     * Initializes or changes the global folder/page position of this GridLayout
     * @param folder
     * @param appPage
     */
    public void setPosition(int folder, int appPage){
        mFolderPage = folder;
        mAppPage = appPage;
    }


    ///////////////////////////////////////////////////////////////////////////
    // Anything to do with resizing
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Displays some resize grips and an indicator border around a view.
     * @param data The view's DrawerObject data
     */
    public void showResizeGrips(final DrawerObject data){
        final AppWidgetProviderInfo widgetInfo = mAppWidgetContainer.mAppWidgetManager.getAppWidgetInfo(((Widget)data).widgetId);

        mDragSurface.removeView(mResizeGrips);

        if(widgetInfo==null || widgetInfo.resizeMode==AppWidgetProviderInfo.RESIZE_NONE){
            return;
        }

        int diameter = (int)getContext().getResources().getDimension(R.dimen.handle);
        int margin = (int)getContext().getResources().getDimension(R.dimen.handle_margin);

        mResizeGrips = new RelativeLayout(getContext());
        GradientDrawable drawable = ((GradientDrawable) getContext().getDrawable(R.drawable.rectangle));
        InsetDrawable insetDrawable = new InsetDrawable(drawable, LauncherUtils.dpToPx(20, getContext()));
        mResizeGrips.setBackground(insetDrawable);

        Cell cell = data.mGridPosition.getCell();
        Point viewLocation = getLocationInWindow();

        int width = data.mGridPosition.colSpan* getColumnWidth()+diameter;
        int height = data.mGridPosition.rowSpan*getRowHeight()+diameter;
        int gripSize = LauncherUtils.dpToPx(48, getContext());

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( width, height);

        mResizeGrips.setElevation(48);
        mResizeGrips.setX(viewLocation.x + cell.x() * getColumnWidth()-diameter/2);
        mResizeGrips.setY(viewLocation.y + cell.y() * getRowHeight()-diameter/2);
        mResizeGrips.setScaleX(1.6f);
        mResizeGrips.setScaleY(1.6f);
        mResizeGrips.setAlpha(0);
        mResizeGrips.setClipToPadding(false);
        mResizeGrips.setClipChildren(false);

        //add handles

        RelativeLayout.LayoutParams topParams = new RelativeLayout.LayoutParams(gripSize, gripSize); //TOP
        topParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, 1);
        topParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 1);
        topParams.setMargins(0, -margin, 0, 0);

        RelativeLayout.LayoutParams bottomParams = new RelativeLayout.LayoutParams(gripSize, gripSize); //BOTTOM
        bottomParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, 1);
        bottomParams.addRule(RelativeLayout.CENTER_HORIZONTAL, 1);
        bottomParams.setMargins(0, 0, 0, -margin);

        RelativeLayout.LayoutParams leftParams = new RelativeLayout.LayoutParams(gripSize, gripSize); //LEFT
        leftParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, 1);
        leftParams.addRule(RelativeLayout.CENTER_VERTICAL, 1);
        leftParams.setMargins(-margin, 0, 0, 0);

        RelativeLayout.LayoutParams rightParams = new RelativeLayout.LayoutParams(gripSize, gripSize); //RIGHT
        rightParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, 1);
        rightParams.addRule(RelativeLayout.CENTER_VERTICAL, 1);
        rightParams.setMargins(0, 0, -margin, 0);

        View topHandle = createResizeHandle();
        topHandle.setRotation(180);
        View bottomHandle = createResizeHandle();
        View leftHandle = createResizeHandle();
        leftHandle.setRotation(90);
        View rightHandle = createResizeHandle();
        rightHandle.setRotation(-90);

       // if(widgetInfo.resizeMode==AppWidgetProviderInfo.RESIZE_HORIZONTAL || widgetInfo.resizeMode==AppWidgetProviderInfo.RESIZE_BOTH) {
            mResizeGrips.addView(leftHandle, leftParams);
            mResizeGrips.addView(rightHandle, rightParams);
       // }
        //if(widgetInfo.resizeMode==AppWidgetProviderInfo.RESIZE_VERTICAL || widgetInfo.resizeMode==AppWidgetProviderInfo.RESIZE_BOTH) {
            mResizeGrips.addView(topHandle, topParams);
            mResizeGrips.addView(bottomHandle, bottomParams);
        //}

        topHandle.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDragSurface.overrideDispatch(false);
                mDragSurface.setOnDragListener(new GripDragListener(data, Direction.DIRECTION_TOP));
                mDragSurface.startDrag(DragSurfaceLayout.DragType.TYPE_APP_PAGE);
                return false;
            }
        });

        bottomHandle.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDragSurface.overrideDispatch(false);
                mDragSurface.setOnDragListener(new GripDragListener(data, Direction.DIRECTION_BOTTOM));
                mDragSurface.startDrag(DragSurfaceLayout.DragType.TYPE_APP_PAGE);
                return false;
            }
        });

        leftHandle.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDragSurface.overrideDispatch(false);
                mDragSurface.setOnDragListener(new GripDragListener(data, Direction.DIRECTION_LEFT));
                mDragSurface.startDrag(DragSurfaceLayout.DragType.TYPE_APP_PAGE);
                return false;
            }
        });

        rightHandle.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mDragSurface.overrideDispatch(false);
                mDragSurface.setOnDragListener(new GripDragListener(data, Direction.DIRECTION_RIGHT));
                mDragSurface.startDrag(DragSurfaceLayout.DragType.TYPE_APP_PAGE);
                return false;
            }
        });


        mResizeGrips.invalidate();

        mDragSurface.addView(mResizeGrips, params);
        mResizeGrips.post(new Runnable() {
            @Override
            public void run() {
                mResizeGrips.animate().alpha(1).scaleX(1).scaleY(1).setInterpolator(new OvershootInterpolator());
            }
        });

        mDragSurface.setClickable(true);
        mDragSurface.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mDragSurface.setOnDragListener( null );
                mDragSurface.setOnClickListener( null );
                mDragSurface.setClickable( false );
                hideResizeGrips();
            }
        });
    }

    /** Hides the resizeGrips from {@link #showResizeGrips(DrawerObject)} */
    private void hideResizeGrips(){
        mResizeGrips.animate().alpha(0).withEndAction(new Runnable() {
            @Override
            public void run() {
                mDragSurface.removeView(mResizeGrips);

                mResizeGrips = null;
                JsonHelper.saveFolderStructure(getContext(), LauncherActivity.mFolderStructure);
            }
        });
    }

    /**
     * @return A View having the shape of a round resize handle.
     */
    private View createResizeHandle(){
        /*
        TODO: Add drag padding
         */
        ImageView view = new ImageView(getContext());
        view.setScaleType(ImageView.ScaleType.CENTER);
        view.setImageDrawable(getContext().getDrawable(R.drawable.resize_handle));
        return view;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Grid or cell oriented methods
    ///////////////////////////////////////////////////////////////////////////

    /**
     * Finds and removes the object that lays at the given grid position.
     * @param row
     * @param col
     */
    private void removeViewAt(int row, int col){
        removeView(getChildAt(row, col));
        requestLayout();
        invalidate();
    }

    /**
     * Adds the given child view with an according DrawerObject to this grid in an animated way.<br/>
     * Runs the usual grid integrity checks before adding.
     * @param child Logically shouldn't have any DrawerObject assigned yet
     * @param data
     */
    public void addObject(@NonNull final View child, final DrawerObject data){
        if(data.mGridPosition.row == Integer.MIN_VALUE || data.mGridPosition.col == Integer.MIN_VALUE){
            Point cell = findFirstFreeCell(data.mGridPosition.rowSpan, data.mGridPosition.colSpan);
            data.mGridPosition.row = cell.x;
            data.mGridPosition.col = cell.y;
        }
        if(freeGrid(data.mGridPosition.row, data.mGridPosition.col, data.mGridPosition.rowSpan, data.mGridPosition.colSpan)) {

            fixFreeGrid();
            freeCells(data.mGridPosition.row, data.mGridPosition.col, data.mGridPosition.rowSpan, data.mGridPosition.colSpan);
            addViewAnimated(child, data);
            normalizeGrid();

        } else {
            redoFreeGrid();
        }
    }

    /**
     * Creates a new child from a given DrawerObject and adds it in an animated way<br/>
     * Runs the usual grid integrity checks before adding.
     * @param data
     * @param startScale The scale from where it will animate to 1.0f
     * @param interpolator The scale interpolator
     * @param animateMS The animatorTime in ms
     * @param doOnPostAdding A Runnable that will run after the view has been added and properly scaled
     */
    public void addObject(final DrawerObject data, final float startScale, final Interpolator interpolator, final int animateMS, final Runnable doOnPostAdding){
        if(data.mGridPosition.row == Integer.MIN_VALUE || data.mGridPosition.col == Integer.MIN_VALUE){
            Point cell = findFirstFreeCell(data.mGridPosition.rowSpan, data.mGridPosition.colSpan);
            data.mGridPosition.row = cell.x;
            data.mGridPosition.col = cell.y;
        }

        if(freeGrid(data.mGridPosition.row, data.mGridPosition.col, data.mGridPosition.rowSpan, data.mGridPosition.colSpan)) {

            fixFreeGrid();
            freeCells(data.mGridPosition.row, data.mGridPosition.col, data.mGridPosition.rowSpan, data.mGridPosition.colSpan);
            data.createView(this, mInflater, new DrawerObject.OnViewCreatedListener() {
                @Override
                public void onViewCreated(View view) {
                    if(data instanceof Application){

                        final Drawable icon = new BitmapDrawable(getResources(), ((Application) data).loadIcon(getContext(), ((Application) data).info));
                        ((AppIconView)view).setIcon(icon);
                    }
                    view.setTranslationX(0);
                    view.setTranslationY(0);
                    addViewAnimated(view, data, startScale, interpolator, animateMS, doOnPostAdding);
                }
            });
            normalizeGrid();

        } else {
            redoFreeGrid();
        }
    }

    /**
     * Adds a child View with a scaling animation. Does not run any grid integrity checks.
     * @param child
     * @param data
     * @param startScale The scale from where it will animate to 1.0f
     * @param interpolator The scale interpolator
     * @param millis The animatorTime in ms
     * @param doOnPostAdding A Runnable that will run after the view has been added and properly scaled
     */
    private void addViewAnimated(@NonNull final View child, DrawerObject data, float startScale, final Interpolator interpolator, final int millis, final Runnable doOnPostAdding){

        doOnPostAdding.run();

        addView(child, data);
        GridLayoutParams vParams = ((GridLayoutParams) child.getLayoutParams());
        if(vParams==null){
            removeView(child);
            return;
        }
        child.setVisibility(INVISIBLE);
        vParams.height = getMeasuredHeight()/getRowCount() * data.mGridPosition.rowSpan;
        vParams.width = getMeasuredWidth()/getColumnCount() * data.mGridPosition.colSpan;
        child.setLayoutParams(vParams);
        child.setScaleX(startScale);
        child.setScaleY(startScale);
        child.setVisibility(VISIBLE);
        child.animate().scaleY(1).scaleX(1).setDuration(millis).setInterpolator(interpolator);

        if(child instanceof AppIconView) {
            boolean showLabels = mPreferences.getBoolean(Const.Defaults.TAG_SHOW_LABELS, Const.Defaults.getBoolean(Const.Defaults.TAG_SHOW_LABELS));
            if((data instanceof Application && notificationsEnabled && ((LauncherActivity)getContext()).mStatusBarNotifications.contains(((Application) data)
                    .info.getComponentName().getPackageName()))){
                int count = ((LauncherActivity)getContext()).mStatusBarNotificationCounts[((LauncherActivity)getContext()).mStatusBarNotifications.indexOf(((Application) data)
                        .info.getComponentName().getPackageName())];
                if(count==0)
                    count++;
                ((AppIconView) child).setCounterOverlay(count);
            } else {
                ((AppIconView) child).removeOverlay();
            }
            ((AppIconView) child).setLabelVisibility(showLabels);
            LauncherUtils.colorAppIconView(((AppIconView) child), getContext());
        }
    }

    /** Calls {@link #addViewAnimated(View, DrawerObject, float, Interpolator, int, Runnable)} with an empty Runnable. */
    private void addViewAnimated(@NonNull final View child, DrawerObject data, float startScale, final Interpolator interpolator, final int millis){
        addViewAnimated(child, data, startScale, interpolator, millis, new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    /** calls {@link #addViewAnimated(View, DrawerObject, float, Interpolator, int)} with default values. <br/><br/>
     * <b>startScale</b> defaults to 0<br/>
     * <b>interpolator</b> defaults to {@code new OvershootInterpolator()}<br/>
     * <b>millis</b> defaults to 100
     */
    private void addViewAnimated(@NonNull final View child, DrawerObject data){
        addViewAnimated(child, data, 0, new OvershootInterpolator(), 200);
    }

    /**
     * Adds a temporary view, used while resizing to not occupy places.
     * @param child The View to add
     * @param row
     * @param col
     * @param rowSpan
     * @param colSpan
     * @param data The DrawerObject data of the added view
     */
    private void addViewTmp(@NonNull View child, int row, int col, int rowSpan, int colSpan, DrawerObject data){

        occupyCellsTemporarily(row, col, rowSpan, colSpan);

        GridLayoutParams params = new GridLayoutParams(row, col, rowSpan, colSpan, data);
        params.setGravity(Gravity.FILL);
        params.width = getColumnWidth()*colSpan;
        params.height = getRowHeight()*rowSpan;
        super.addView(child, params);
    }

    /**
     * Adds a view to the assigned {@link DragSurfaceLayout} at the exact location it would be on this grid.
     * @param child The View to add
     * @param row
     * @param col
     * @param rowSpan
     * @param colSpan
     * @param viewScale The scale of the added View.
     */
    private void addViewOver(@NonNull View child, int row, int col, int rowSpan, int colSpan, float viewScale){
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams((int)((colSpan*getColumnWidth())*viewScale), (int)(rowSpan*getRowHeight()*viewScale));
        mDragSurface.removeView(child);
        mDragSurface.addView(child, params);
        int[] pos = new int[2];
        getLocationInWindow(pos);

        int calcY = ((int) ((row * getRowHeight() * viewScale + pos[1])));
        int calcX = ((int) ((col * getColumnWidth() * viewScale + pos[0])));

        child.setX(calcX);
        child.setY(calcY);
        child.setTranslationZ(0);
    }

    /**
     * Sets the given cell space to an "available" state.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     */
    public void freeCells(int row, int column, int rowSpan, int colSpan){
        freeCellImpl(row, column, rowSpan, colSpan, getRowCount(), getColumnCount());
    }

    /** See {@link #freeCells(int, int, int, int)}.*/
    private void freeCellImpl(int row, int column, int rowSpan, int colSpan, int rowCount, int colCount){
        if(row<0 || column<0){
            return;
        }
        for (int i = row; i < Math.min(row + rowSpan, rowCount); i++) {
            for (int j = column; j < Math.min(column + colSpan, colCount); j++) {
                mCellUsed[i * getColumnCount() + j] = false;
            }
        }
    }

    /**
     * Equivalent to {@link #freeCells(int, int, int, int)}, but used while dragging.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     */
    private void freeTemporaryCells(int row, int column, int rowSpan, int colSpan){
        if (mCellUsedTemp == null){
            mCellUsedTemp=new boolean[mCellUsed.length];
            return;
        }
        for (int i = row; i < Math.min(row + rowSpan, getRowCount()); i++) {
            for (int j = column; j < Math.min(column + colSpan, getColumnCount()); j++) {
                mCellUsedTemp[i * getColumnCount() + j] = false;
            }
        }
    }

    /**
     * Checks if the cell space of a {@link DrawerObject} is valid. That means if the cell is inside the grid and if its not already used.
     * @param data The DrawerObject data of the object to check.
     * @return null if the cell space is invalid, the top left cell of the cell space itself if not.
     */
    @Nullable
    private Point isPointValid(DrawerObject data){
        Point cell = new Point(data.mGridPosition.row, data.mGridPosition.col);
        if(data.mGridPosition.row == Integer.MIN_VALUE || data.mGridPosition.col == Integer.MIN_VALUE){
            cell = findFirstFreeCell(data.mGridPosition.rowSpan, data.mGridPosition.colSpan);
            data.mGridPosition.row = cell.x;
            data.mGridPosition.col = cell.y;
        }
        boolean isSpaceUsed = false;
        for (int i = data.mGridPosition.row; i < data.mGridPosition.row + data.mGridPosition.rowSpan; i++) {
            for (int j = data.mGridPosition.col; j < data.mGridPosition.col + data.mGridPosition.colSpan; j++) {
                try {
                    isSpaceUsed = isSpaceUsed || mCellUsed[i * getColumnCount() + j];
                } catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                    return null;
                }
            }
        }

        if(!isSpaceUsed) {
            GridLayoutParams params = new GridLayoutParams(data.mGridPosition.row, data.mGridPosition.col, data.mGridPosition.rowSpan, data.mGridPosition.colSpan, data);
            try {
                if(!checkLayoutParams(params)){
                    return null;
                }
            } catch (IllegalArgumentException e){
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
        return cell;
    }

    /**
     * Statically (that is not animated) adds a child view to this grid
     * @param child
     * @param data Take your cell and spans from here
     * @return
     */
    @Nullable
    private Point addView(@NonNull final View child, DrawerObject data){
        Point cell = new Point(data.mGridPosition.row, data.mGridPosition.col);
        if(data.mGridPosition.row == Integer.MIN_VALUE || data.mGridPosition.col == Integer.MIN_VALUE){
            cell = findFirstFreeCell(data.mGridPosition.rowSpan, data.mGridPosition.colSpan);
            data.mGridPosition.row = cell.x;
            data.mGridPosition.col = cell.y;
        }
        boolean isSpaceUsed = false;
        for (int i = data.mGridPosition.row; i < data.mGridPosition.row + data.mGridPosition.rowSpan && i<getRowCount(); i++) {
            for (int j = data.mGridPosition.col; j < data.mGridPosition.col + data.mGridPosition.colSpan && j<getColumnCount(); j++) {
                isSpaceUsed = isSpaceUsed || mCellUsed[i * getColumnCount() + j];
            }
        }

        if(!isSpaceUsed) {

            occupyCells(data.mGridPosition.row, data.mGridPosition.col, data.mGridPosition.rowSpan, data.mGridPosition.colSpan);

            GridLayoutParams params = new GridLayoutParams(data.mGridPosition.row, data.mGridPosition.col, data.mGridPosition.rowSpan, data.mGridPosition.colSpan, data);
            params.setGravity(Gravity.FILL);
            params.width = getColumnWidth()*data.mGridPosition.colSpan;
            params.height = getRowHeight()*data.mGridPosition.rowSpan;
            child.setSaveEnabled(true);
            child.setSaveFromParentEnabled(true);
           // child.setId(getUniqueID());
            child.setOnLongClickListener(this);
            if(child instanceof AppIconView && child.getTag()!=null && child.getTag() instanceof Intent){
                final boolean isShortcut = data instanceof Shortcut;

                if(isShortcut)
                    child.setOnClickListener(((LauncherActivity)getContext()).mShortcutClickListener);
                else
                    child.setOnClickListener(((LauncherActivity)getContext()).mAppClickListener);
            }

            if(child instanceof AppIconView){
                LauncherUtils.colorAppIconView((AppIconView)child, getContext());
            }
            try {
               // if (true){//checkLayoutParams(params)) {
                    super.addView(child, params);
                    if(child instanceof ClickableAppWidgetHostView){
                        ((ClickableAppWidgetHostView) child).updateAppWidgetSize(null, getColumnWidth(), getRowHeight(), getColumnWidth()*getColumnCount(), getRowHeight()*getRowCount());
                    }
               // }
            } catch (IllegalArgumentException e){
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }

        return cell;
    }

    /**
     * Looks for the first free cell, where an object with the given spans can be placed freely.
     * @param rowSpan
     * @param colSpan
     * @return The found cell or null if none was found.
     */
    public Point findFirstFreeCell(int rowSpan, int colSpan){
        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                if(!isCellGridUsedFull(i, j, rowSpan, colSpan)){
                    return new Point(i, j);
                }
            }
        }
        return new Point(0,0);
    }

    /**
     * Sets a given cell space to a "not-available" state.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     */
    public void occupyCells(int row, int column, int rowSpan, int colSpan){
        for (int i = row; i < row + rowSpan; i++) {
            for (int j = column; j < column + colSpan; j++) {
                mCellUsed[i * getColumnCount() + j] = true;
            }
        }
    }

    /**
     * Sets the cell space for a given view to a "not-available" state.
     * @param child
     */
    private void occupyCells(View child){
        Cell pos = getChildCell(child);
        CellSpan span = getChildSpan(child);

        GridLayoutParams p = ((GridLayoutParams) child.getLayoutParams());
        if(p.viewData==null)
            return;
        p.viewData.mGridPosition = new DrawerObject.GridPositioning(pos.row, pos.column, span.rowSpan, span.columnSpan);

        for (int i = pos.row; i < pos.row + span.rowSpan; i++) {
            for (int j = pos.column; j < pos.column + span.columnSpan; j++) {
                mCellUsed[i * getColumnCount() + j] = true;
            }
        }
    }

    /**
     * Sets a given cell space temporarily unavailable (used when dragging or resizing).
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     */
    private void occupyCellsTemporarily(int row, int column, int rowSpan, int colSpan){
        for (int i = row; i < Math.min(row + rowSpan, getRowCount()); i++) {
            for (int j = column; j < Math.min(column + colSpan, getColumnCount()); j++) {
                mCellUsedTemp[i * getColumnCount() + j] = true;
            }
        }
    }

    /** @return The view that overlaps the given cell */
    @Nullable
    private View getChildAt(int row, int column){

        for (int i = 0; i < getChildCount(); i++) {
            View ch = getChildAt(i);

            GridLayoutParams params = ((GridLayoutParams) ch.getLayoutParams());

            Rect rect = new Rect(params.col, params.row, params.col+params.colSpan, params.row+params.rowSpan);

            if(rect.contains(column, row)){
                return ch;
            }
        }
        return null;
    }

    /**
     * Tries to move away (animated) all children that happen to be in the given cell space.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return whether the grid can be freed or not.
     */
    private boolean freeGrid(int row, int column, int rowSpan, int colSpan){
        mCellUsedTemp = mCellUsed.clone();

        for (int r = row; r < Math.min(row + rowSpan, getRowCount()); r++) {
            for (int c = column; c < Math.min(column + colSpan, getColumnCount()); c++) {
                try {
                    View view = getChildAt(r, c);
                    if (view != null && !mMovedViewList.contains(view)) {
                        occupyCellsTemporarily(row, column, rowSpan, colSpan);
                        Cell cell = moveChildToNextEmptyPlace(r, c, row, column, rowSpan, colSpan);
                        occupyCellsTemporarily(row, column, rowSpan, colSpan);
                        CellSpan spans = getChildSpan(view);
                        mMovedViewTargetCells.add(cell);
                        mMovedViewSpans.add(spans);
                        mMovedViewList.add(view);
                    }
                } catch (ArrayIndexOutOfBoundsException e){
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * The reverse operation of {@link #freeCells(int, int, int, int)}.
     */
    public void redoFreeGrid(){
        redoFreeGrid(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    /**
     * See {@link #redoFreeGrid()}.
     * @param withEndAction The action that should be run after the action has been reverted
     */
    private void redoFreeGrid(Runnable withEndAction){

        mCellUsedTemp = mCellUsed.clone();
        boolean run = false;

        for (int i = 0; i < getChildCount(); i++) {

            if(!(getChildAt(i) instanceof Space) && (getChildAt(i).getTranslationX()!=0 || getChildAt(i).getTranslationY()!=0)) {
                run = true;
                getChildAt(i).animate().translationY(0).translationX(0).setDuration(200).withEndAction(i == 0 ? withEndAction : new Runnable() {
                    @Override
                    public void run() {

                    }
                });
            }
        }
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(!(child instanceof Space)){
                occupyCells(child);
            }
        }

        mMovedViewList.clear();
        mMovedViewSpans.clear();
        mMovedViewTargetCells.clear();

        if(!run)
            withEndAction.run();
    }

    /**
     * Occupies all cells that are covered by any view
     */
    public void normalizeGrid(){
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(!(child instanceof Space)){
                occupyCells(child);
            }
        }
    }

    /**
     * "Fixes" the fake translated views from {@link #freeCells(int, int, int, int)} into places.
     */
    public void fixFreeGrid() {
        if(mCellUsedTemp==null){
            mCellUsedTemp = new boolean[getColumnCount()*getRowCount()];
        }
        for (int i = 0; i < mCellUsedTemp.length; i++) {
            mCellUsedTemp[i] = false;
        }
        mCellUsed = mCellUsedTemp.clone();
        for (int i = 0; i < mMovedViewList.size(); i++) {
            final CellSpan spans = mMovedViewSpans.get(i);
            final Cell cell = mMovedViewTargetCells.get(i);

            FolderStructure.ViewDataArrayList dataArrayList = LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.get(mAppPage).items;

            DrawerObject data = ((GridLayoutParams) mMovedViewList.get(i).getLayoutParams()).viewData;

            if(data.mGridPosition==null)
                continue;

            dataArrayList.remove(data);

            data.mGridPosition.set(cell, spans);

            LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.get(mAppPage).items.add(data);

            GridLayoutParams vParams = new GridLayoutParams(cell, spans, data);
            vParams.height = getMeasuredHeight() / getRowCount() * spans.rowSpan;
            vParams.width = getMeasuredWidth() / getColumnCount() * spans.columnSpan;

            mMovedViewList.get(i).setLayoutParams(vParams);
            mMovedViewList.get(i).setTranslationX(0);
            mMovedViewList.get(i).setTranslationY(0);
        }

        for (int i = 0; i < mMovedViewList.size(); i++) {
            mMovedViewList.get(i).animate().translationY(0).translationX(0);
        }
        mMovedViewList.clear();
        mMovedViewSpans.clear();
        mMovedViewTargetCells.clear();

        //normalize mCellUsed
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            if(!(child instanceof Space)){
                occupyCells(child);
            }
        }
    }

    /**
     * Moves a view from a given location to the next suitable free place.
     * @param row row to get the view from
     * @param column column to get the view from
     * @param occupiedRow marks the full pre-occupied grid in which there can be no other view.
     * @param occupiedColumn marks the full pre-occupied grid in which there can be no other view.
     * @param occRowSpan marks the full pre-occupied grid in which there can be no other view.
     * @param occColSpan marks the full pre-occupied grid in which there can be no other view.
     * @return The cell that got occupied or null, if there is no view at the place.
     */
    @Nullable
    private Cell moveChildToNextEmptyPlace(final int row, final int column, int occupiedRow, int occupiedColumn, int occRowSpan, int occColSpan){
        final View animateView = getChildAt(row, column);
        if(animateView==null)
            return null;

        GridLayoutParams params = ((GridLayoutParams) animateView.getLayoutParams());

        final CellSpan spans = getChildSpan(row, column);
        final Cell oldCell = getChildCell(animateView);
        freeTemporaryCells(oldCell.row, oldCell.column, spans.rowSpan, spans.columnSpan);
        occupyCellsTemporarily(occupiedRow, occupiedColumn, occRowSpan, occColSpan);
        final Cell cell = findNearestEmptyCellForSpannedChild(row, column, spans.rowSpan, spans.columnSpan);
        final Point target = getCellCoordinates(cell.row, cell.column);

        final float oldX = params.col*getColumnWidth();
        final float oldY = params.row*getRowHeight();
        final float translationX = target.x-oldX;
        final float translationY = target.y-oldY;

        //first free old cells, then occupy target ones
        occupyCellsTemporarily(cell.row, cell.column, spans.rowSpan, spans.columnSpan);

        animateView.animate().translationX(translationX).translationY(translationY).setDuration(120);
        return cell;
    }

    private boolean isTempCellUsed(int row, int column){
        return mCellUsedTemp[row*getColumnCount()+column];
    }

    private boolean isCellUsed(int row, int column){
        return mCellUsed[row*getColumnCount()+column];
    }

    /**
     * Checks if a given cell space is unavailable in the temporary state.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return true, if the full cell space is currently unavailable (in use)
     */
    private boolean isCellGridUsed(int row, int column, int rowSpan, int colSpan){
        boolean cellUsed = false;
        for (int r = row; r < row+rowSpan; r++) {
            for (int c = column; c < column+colSpan; c++) {
                if(r>=getRowCount() || c>=getColumnCount())
                    return true;
                cellUsed = cellUsed || isTempCellUsed(r, c);
            }
        }

        return cellUsed;
    }

    /**
     * Checks if a given cell space is unavailable.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return true, if the full cell space is currently unavailable (in use)
     */
    public boolean isCellGridUsedFull(int row, int column, int rowSpan, int colSpan){

        boolean cellUsed = false;
        for (int r = row; r < row+rowSpan; r++) {
            for (int c = column; c < column+colSpan; c++) {
                if(r>=getRowCount() || c>=getColumnCount())
                    return true;
                cellUsed = cellUsed || isCellUsed(r, c);
            }
        }

        return cellUsed;
    }

    /**
     * Gets the most top-left cell of the nearest free cell grid that can hold the given cell space.
     * @param row
     * @param column
     * @param rowSpan
     * @param colSpan
     * @return The nearest free cell grid.
     */
    private Cell findNearestEmptyCellForSpannedChild(int row, int column, int rowSpan, int colSpan){
        if(!isCellGridUsed(row, column, rowSpan, colSpan)){
            return new Cell(row, column);
        }

        List<Cell> suitablePlaces = new ArrayList<>();
        Cell finalTarget;

        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnWidth(); j++) {
                //check in grid for free space
                if(!isCellGridUsed(i, j, rowSpan, colSpan))
                    suitablePlaces.add(new Cell(i, j));
            }
        }
        if(suitablePlaces.size()==0){
            return new Cell(-1, -1);
        }

        float finalTargetDistance = getDistanceBetweenPoints(row, column, suitablePlaces.get(0).row, suitablePlaces.get(0).column);
        finalTarget = suitablePlaces.get(0);

        for (int i = 1; i < suitablePlaces.size(); i++) {
            float tmpDistance = getDistanceBetweenPoints(row, column, suitablePlaces.get(i).row, suitablePlaces.get(i).column);
            if(tmpDistance<finalTargetDistance){
                finalTargetDistance = tmpDistance;
                finalTarget = suitablePlaces.get(i);
            }
        }

        return finalTarget;
    }

    /** Calculates pixel coordinates from row and column */
    private Point getCellCoordinates(int row, int column){
        return new Point((column*getColumnWidth()), ((row * getRowHeight())));
    }

    /**
     * A collection of redoing previous freeing steps and freeing another location by the given row and column.
     * @param oldRow The old hover location
     * @param oldCol The old hover location
     * @param row
     * @param col
     */
    public void hoverAt(final int oldRow, final int oldCol, final int row, final int col){
        if((hoverPoint[0] == row && hoverPoint[1] == col) || mGridType==GridType.TYPE_APPS_ONLY)
            return;

        LauncherLog.d(TAG, "Dragging at " + row + ", "+ col);

        mDragChanged = false;

        hoverPoint[0] = row;
        hoverPoint[1] = col;

        freeCells(oldRow, oldCol, mDragData.mGridPosition.rowSpan, mDragData.mGridPosition.colSpan);
        mDragHoverHandler.removeCallbacksAndMessages(null);
        redoFreeGrid();

        if(mDragShadowView==null){
            mDragShadowView = new ImageView(getContext());
        }
        if(!mShadowIsAnimating) {
            mShadowIsAnimating = true;
            mDragShadowView.animate().scaleY(0).scaleX(0).alpha(0).setDuration(100).withEndAction(new Runnable() {
                @Override
                public void run() {
                    mDragSurface.removeView(mDragShadowView);
                    mShadowIsAnimating = false;
                }
            });
        }

        int delay = (isCellGridUsedFull(row, col, mDragData.mGridPosition.rowSpan, mDragData.mGridPosition.colSpan))?300:0;
        mDragHoverHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if(mDragView==null)
                    return;
                mDragShadow = LauncherUtils.loadBitmapFromView(mDragView);
                mDragShadowView.setImageDrawable(new BitmapDrawable(getResources(), mDragShadow));
                if (mDragData != null && freeGrid(row, col, mDragData.mGridPosition.rowSpan, mDragData.mGridPosition.colSpan)) {
                    mDragShadowView.setImageTintList(ColorStateList.valueOf(0xff1976D2));
                    mDragChanged = true;

                    final DrawerObject tmpData = mDragData;

                    addViewOver(mDragShadowView, row, col, tmpData.mGridPosition.rowSpan, tmpData.mGridPosition.colSpan, 0);
                    if(mDragShadowView!=null) {
                        mDragShadowView.post(new Runnable() {
                            @Override
                            public void run() {
                                mDragShadowView.animate().setDuration(100).scaleY(0.9f).scaleX(0.9f).alpha(0.5f);
                            }
                        });
                    }

                } else {
                    hoverPoint[0] = oldRow;
                    hoverPoint[1] = oldCol;
                }
            }
        }, delay);
    }

    /**
     * Adds all views that are contained in the main FolderStructure for this gridLayout.
     */
    public void refresh(){
        FolderStructure.ViewDataArrayList list = new FolderStructure.ViewDataArrayList();
        if(LauncherActivity.mFolderStructure==null)
            return;
        if(LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.size()>mAppPage){
            list = LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.get(mAppPage).items;
        }


        final int cols = ((LauncherActivity)getContext()).mHolder.gridWidth;
        final int rows = ((LauncherActivity)getContext()).mHolder.gridHeight;

        final boolean showLabels = mPreferences.getBoolean("showLabels", true);

        for(int i=0; i<list.size(); i++) {
            final DrawerObject object = list.get(i);
            LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
                @Override
                public void run() {
                            if(object.mGridPosition.row+object.mGridPosition.rowSpan<=rows && object.mGridPosition.col+object.mGridPosition.colSpan<=cols){
                                getMainActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        addViewFromViewData(object, showLabels);
                                    }
                                });
                            } else {
                                LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.get(mAppPage).items.remove(object);
                            }
                }
            });
        }
    }

    /**
     * Check for app icons in this ViewGroup that shouldn't be there because the app doesnt exist anymore.
     */
    public void checkRemoved(){
//        if(true) return;
        for (int i = 0; i < getChildCount(); i++) {
            final int iVal = i;
            LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
                @Override
                public void run() {
                    getMainActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            View v = getChildAt(iVal);
                            if(v instanceof AppIconView){
                                LauncherUtils.colorAppIconView((AppIconView)v, getContext());
                            }
                            if(v!=null && v.getTag()!=null && v.getTag() instanceof Intent){
                                if(!LauncherUtils.isAvailable(getContext(), ((Intent) v.getTag()))){
                                    removeView(v);
                                }
                            }
                        }
                    });
                }
            });
        }
    }

    /**
     * Adds a list of Views to the grid, calls {@link #addViewFromViewData(DrawerObject, boolean)}.
     * @param data The list of data.
     */
    private void addViewsFromViewData(FolderStructure.ViewDataArrayList data){
        boolean showLabels = mPreferences.getBoolean(Const.Defaults.TAG_SHOW_LABELS, Const.Defaults.getBoolean(Const.Defaults.TAG_SHOW_LABELS));
        for (int i = 0; i < data.size(); i++) {
            addViewFromViewData(data.get(i), showLabels);
        }
    }

    /**
     * Inflates any given {@link DrawerObject} into this GridLayout.
     * @param d
     * @param showAppLabel From preferences: shall the labels on AppIconViews be shown?
     * @return
     */
    @Nullable
    private Point addViewFromViewData(final DrawerObject d, final boolean showAppLabel){

        if(mInflater == null){
            mInflater = ((LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
        }
        if (d==null)
            return null;
        d.createView(this, mInflater, new DrawerObject.OnViewCreatedListener() {
            @Override
            public void onViewCreated(View view) {
                if(view instanceof AppIconView) {
                    if((d instanceof Application && notificationsEnabled && ((LauncherActivity)getContext()).mStatusBarNotifications.contains(((Application) d).info.getComponentName().getPackageName()))){
                        int count = ((LauncherActivity)getContext()).mStatusBarNotificationCounts[((LauncherActivity)getContext()).mStatusBarNotifications.indexOf(((Application) d).info.getComponentName().getPackageName())];
                        if(count==0)
                            count++;
                        ((AppIconView) view).setCounterOverlay(count);
                    } else {
                        ((AppIconView) view).removeOverlay();
                    }
                    ((AppIconView) view).setLabelVisibility(showAppLabel);
                    LauncherUtils.colorAppIconView(((AppIconView) view), getContext());
                }

                if(view!=null){

                    if(view.getParent()!=null) {
                        ((ViewGroup) view.getParent()).removeView(view);
                    }
                    Point p = addView(view, d);
                    view.setTranslationX(0);
                    view.setTranslationY(0);
                    if(d instanceof Widget)
                        if(p==null){
                            LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.get(mAppPage).items.remove(d);
                        }
                } else {
                    LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.get(mAppPage).items.remove(d);
                }
            }
        });
        return isPointValid(d);
    }

    /**
     * Changes grid view content according to the main FolderStructure. Animates (should at least...) when visible by user.
     * @param isActive is visible by user?
     */
    public void notifyDataSetChanged(boolean isActive){

        if(!isActive){

            removeAllViews();
            FolderStructure.ViewDataArrayList list = LauncherActivity.mFolderStructure.folders.get(mFolderPage).pages.get(mAppPage).items;

            addViewsFromViewData(list);

            return;
        }
        FolderStructure.ViewDataArrayList oldData = new FolderStructure.ViewDataArrayList();
        FolderStructure.ViewDataArrayList newData = LauncherActivity.mFolderStructure.folders.get(Math.min(mFolderPage, LauncherActivity.mFolderStructure.folders.size()-1))
                .pages.get(Math.min(mAppPage, LauncherActivity.mFolderStructure.folders.get(Math.min(mFolderPage, LauncherActivity.mFolderStructure.folders.size()-1)).pages.size()-1)).items;

        List<View> viewsToRemove = new ArrayList<>();
        for (int i = 0; i < getChildCount(); i++) {
            oldData.add(((GridLayoutParams) getChildAt(i).getLayoutParams()).viewData);
            if(!newData.containsNotPosition(((GridLayoutParams) getChildAt(i).getLayoutParams()).viewData)){
                viewsToRemove.add(getChildAt(i));
            }
        }

        for (int i = 0; i < viewsToRemove.size(); i++) {
            removeView(viewsToRemove.get(i));
        }
        for (final DrawerObject data:newData){
            if(oldData.containsNotPosition(data)){
                transformObject(oldData.get(oldData.indexOfNotPosition(data)), data);
            } else {
                boolean showLabels = mPreferences.getBoolean(Const.Defaults.TAG_SHOW_LABELS, Const.Defaults.getBoolean(Const.Defaults.TAG_SHOW_LABELS));
                addViewFromViewData(data, showLabels);
            }
        }

        normalizeGrid();
    }

    /**
     * Animates a DrawerObject view to translate to another DrawerObject position.
     * @param from
     * @param to
     */
    private void transformObject(DrawerObject from, final DrawerObject to){
        if(from.equals(to))
            return;

        final View child = getChildAt(from.mGridPosition.row, from.mGridPosition.col);
        if(child==null)
            return;

        Point fromCoordinate = getCellCoordinates(from.mGridPosition.row, from.mGridPosition.col);
        Point toCoordinate = getCellCoordinates(to.mGridPosition.row, to.mGridPosition.col);

        int transformX = toCoordinate.x-fromCoordinate.x;
        int transformY = toCoordinate.y-fromCoordinate.y;

        child.animate().translationX(transformX).translationY(transformY).setDuration(150).withEndAction(new Runnable() {
            @Override
            public void run() {
                GridLayoutParams params = ((GridLayoutParams) child.getLayoutParams());
                params.viewData = to;
                params.row = to.mGridPosition.row;
                params.col = to.mGridPosition.col;
                params.rowSpec = spec(to.mGridPosition.row, to.mGridPosition.rowSpan);
                params.columnSpec = spec(to.mGridPosition.col, to.mGridPosition.colSpan);

                child.setTranslationX(0);
                child.setTranslationY(0);
                child.setLayoutParams(params);
            }
        });
    }

    public void populate(List<? extends DrawerObject> dataCollection){
        boolean showLabels = mPreferences.getBoolean(Const.Defaults.TAG_SHOW_LABELS, Const.Defaults.getBoolean(Const.Defaults.TAG_SHOW_LABELS));
        for (int i = 0; i < Math.min(getRowCount() * getColumnCount(), dataCollection.size()); i++) {
            Point p = addViewFromViewData(dataCollection.get(i), showLabels);
            if(p!=null){
                dataCollection.get(i).mGridPosition.row = p.x;
                dataCollection.get(i).mGridPosition.col = p.y;
            }
        }
    }




    ///////////////////////////////////////////////////////////////////////////
    // Overridden methods
    ///////////////////////////////////////////////////////////////////////////

    @Override
    public boolean onLongClick(View v) {
        LauncherLog.d(TAG, "LongClicked " + (v instanceof ClickableAppWidgetHostView && !((ClickableAppWidgetHostView) v).mLongPressHelper.isCanLongPress()));

        if((v instanceof ClickableAppWidgetHostView && !((ClickableAppWidgetHostView) v).mLongPressHelper.isCanLongPress()) ||
                (v instanceof AppIconView && !((AppIconView) v).mLongPressHelper.isCanLongPress())){
            return false;
        }

        SoftReference<CheckForLongPressHelper> helper = new SoftReference<CheckForLongPressHelper>(v instanceof ClickableAppWidgetHostView ? ((ClickableAppWidgetHostView) v).mLongPressHelper : ((AppIconView) v).mLongPressHelper);

        mDragView = v;
        mDragSurface.overrideDispatch(true);

        size = new int[] {getMeasuredWidth(), getMeasuredHeight()};

        mDragSurface.setOnDragListener(mDragSurface);
        mDragSurface.setFocussedPagerGrid(this);
        mDragSurface.setInitialAppGrid(this);

        mDragShadow = LauncherUtils.loadBitmapFromView(v);

        final Cell cell = getChildCell(v);
        CellSpan span = getChildSpan(v);
        Point xy = getCellCoordinates(cell.row, cell.column);

        freeCells(cell.row, cell.column, span.rowSpan, span.columnSpan);

        final int[] viewLocationDragView = new int[2];

        pos = getLocationInWindow();
        v.getLocationInWindow(viewLocationDragView);

        if(!(v.getLayoutParams() instanceof GridLayoutParams)){
            return false;
        }
        GridLayoutParams dragParams = (GridLayoutParams)v.getLayoutParams();
        if(dragParams.viewData!=null){
            mDragData = dragParams.viewData;
        }

        mDragStartIndex = indexOfChild(v);
        removeView(v);
        mDragView.measure(UNSPECIFIED, UNSPECIFIED);
        GridLayoutParams mainParams = ((GridLayoutParams) mDragView.getLayoutParams());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(mainParams.colSpan*getColumnWidth(), mainParams.rowSpan*getRowHeight());
        mDragSurface.addView(mDragView, params);
        mDragView.setTranslationX(0);
        mDragView.setTranslationY(0);

        mDragClickPoint = helper.get().getLongPressPosition();
        mDragClickPoint.x -= 0.5f*mainParams.colSpan*getColumnWidth();
        mDragClickPoint.y -= 0.5f*mainParams.rowSpan*getRowHeight();

        mDragView.setX(2 * (cell.x() * getColumnWidth()) + pos.x);
        mDragView.setY(2 * (cell.y() * getRowHeight()) + pos.y);

        mDragStartPos = new int[] {(pos.x + xy.x), ((pos.y + xy.y))};

        mHasDragged = true;
        mDragSurface.setPager(mPager);


        if(mDragView==null)
            return false;

        hLocY = cell.y();
        hLocX = cell.x();

        LauncherLog.d(TAG, "Long pressed at: " + mDragClickPoint.toString());

        mDragView.post(new Runnable() {
            @Override
            public void run() {
                mDragSurface.startDrag(DragSurfaceLayout.DragType.TYPE_APP_PAGE);

                mDragView.animate().setDuration(100).scaleX(1.07f).scaleY(1.07f).translationZ(48).alpha(1f).withEndAction(new Runnable() {
                    @Override
                    public void run() {


                        hoverPoint[0] = Integer.MIN_VALUE;
                        hoverPoint[1] = Integer.MIN_VALUE;
                        //final boolean showLabels = mPreferences.getBoolean(Const.Defaults.TAG_SHOW_LABELS, Const.Defaults.getBoolean(Const.Defaults.TAG_SHOW_LABELS));
                        if (mHasDragged || mPager==null || mPager.getAdapter()==null || !((AppDrawerPagerAdapter) mPager.getAdapter()).mCanDragItems) {
                            mDragSurface.removeAllViews();
                            redoFreeGrid();
                            if(mDragData!=null)
                                addObject(mDragData, 1.07f, new DecelerateInterpolator(), 50, new Runnable() {
                                    @Override
                                    public void run() {

                                    }
                                });

                            //clear stuff
                            mDragView = null;
                            hoverPoint[0] = -1;
                            hoverPoint[1] = -1;
                            mDragShadowView = null;
                        }
                    }
                });
            }
        });

        return false;
    }

    @Override
    public void cancelLongPress() {
        super.cancelLongPress();
        // Cancel long press for all children
        final int count = getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = getChildAt(i);
            child.cancelLongPress();
        }
    }

    @Override
    public int getRowCount() {
        int rc = super.getRowCount();
        if(rc <=0){
            rc = DEFAULT_ROW_COUNT;
        }
        return rc;
    }

    @Override
    public int getColumnCount() {
        int cc = super.getColumnCount();
        if(cc<=0){
            cc = DEFAULT_COLUMN_COUNT;
        }
        return cc;
    }

    @Override
    public void setRowCount(int rowCount) {
        if(rowCount<=0)
            rowCount = DEFAULT_ROW_COUNT;

        boolean[] newUsedCells = new boolean[rowCount * getColumnCount()];
        if(mCellUsed == null){
            mCellUsed = newUsedCells;
        }   else {
            System.arraycopy(mCellUsed, 0, newUsedCells, 0, Math.min(newUsedCells.length, mCellUsed.length));
        }

        mCellUsed = newUsedCells;

        //add or remove views
        int oldRowCount = getRowCount();

        if(oldRowCount>rowCount){
            for (int r = rowCount; r < oldRowCount; r++) {
                for (int c = 0; c < getColumnCount(); c++) {
                    removeViewAt(r, c);
                }
            }
        }

        super.setRowCount(rowCount);
    }

    @Override
    public void setColumnCount(int columnCount) {
        if(columnCount<=0)
            columnCount = DEFAULT_COLUMN_COUNT;
        boolean[] newUsedCells = new boolean[columnCount*getRowCount()];
        if(mCellUsed == null){
            mCellUsed = newUsedCells;
        }   else {
            System.arraycopy(mCellUsed, 0, newUsedCells, 0, Math.min(newUsedCells.length, mCellUsed.length));
        }

        mCellUsed = newUsedCells;

        //add or remove views
        final int oldColCount = getColumnCount();
        if(oldColCount>columnCount) {
            for (int c = columnCount; c < oldColCount; c++) {
                for (int r = 0; r < getRowCount(); r++) {
                    removeViewAt(r, c);
                }
            }
        }

        super.setColumnCount(columnCount);
    }

    @Override
    public void addView(@Nullable View child, int index) {
        if(index<mCellUsed.length) {
            if (child == null) {
                child = new Space(getContext());
            } else {
               /* if(child instanceof ClickableAppWidgetHostView){
                    child.setOnTouchListener(this);
                }*/
                child.setOnLongClickListener(this);
            }
            child.setSaveEnabled(true);
            child.setSaveFromParentEnabled(true);
            child.setId(getUniqueID());
            super.addView(child, index);
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            View c = getChildAt(i);
            GridLayoutParams params = ((GridLayoutParams) c.getLayoutParams());
            int x = params.col * getColumnWidth();
            int y = params.row * getRowHeight();
            int width = params.colSpan*getColumnWidth();
            int height = params.rowSpan*getRowHeight();

            if (width != c.getMeasuredWidth() || height != c.getMeasuredHeight()) {
                c.measure(makeMeasureSpec(width, EXACTLY), makeMeasureSpec(height, EXACTLY));
            }

            c.layout(x, y, x + width, y + height);
        }

    }

    @Override
    public String toString() {
        if(mCellUsedTemp==null)
            mCellUsedTemp = new boolean[getMaxItemCount()];

        String outString = "";

        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                outString = outString + ((mCellUsed[i*getColumnCount()+j]?1:0) + " ");
            }
            outString = "";
        }

        for (int i = 0; i < getRowCount(); i++) {
            for (int j = 0; j < getColumnCount(); j++) {
                outString = outString + ((mCellUsedTemp[i*getColumnCount() +j]?1:0) + " ");
            }
            outString = "";
        }

        return super.toString();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Parcelable p = super.onSaveInstanceState();
        return new GridState(p, mFolderPage, mAppPage);
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if(!(state instanceof BaseSavedState)){
            super.onRestoreInstanceState(state);
            return;
        }
        if(!(state instanceof GridState)) {
            super.onRestoreInstanceState(((BaseSavedState) state).getSuperState());
            return;
        }

        notificationsEnabled = mPreferences.getBoolean(Const.Defaults.TAG_NOTIFICATIONS, Const.Defaults.getBoolean(Const.Defaults.TAG_NOTIFICATIONS));

        GridState gridState = (GridState) state;
        super.onRestoreInstanceState(gridState.getSuperState());

        mCellUsed = new boolean[getRowCount()*getColumnCount()];
        mCellUsedTemp = new boolean[getRowCount()*getColumnCount()];

        mFolderPage = gridState.folderPage;
        mAppPage = gridState.appPage;

        removeAllViews();
        refresh();

    }


    ///////////////////////////////////////////////////////////////////////////
    // Other helpers
    ///////////////////////////////////////////////////////////////////////////

    private float getDistanceBetweenPoints(int x1, int y1, int x2, int y2){
        return ((float) Math.sqrt((x2 - x1) * (x2 - x1) + (y2 - y1) * (y2 - y1)));
    }
}