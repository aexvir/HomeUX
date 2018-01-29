package com.dravite.newlayouttest.top_fragments;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.LauncherLog;
import com.dravite.newlayouttest.views.FolderRecyclerView;
import com.dravite.newlayouttest.drawerobjects.structures.FolderStructure;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.views.viewcomponents.SpacesItemDecoration;
import com.dravite.newlayouttest.views.DragSurfaceLayout;
import com.dravite.newlayouttest.views.FolderButton;

/**
 * Created by Johannes on 21.09.2015.
 *
 */
public class FolderListFragment extends Fragment {

    private QuickSettingsFragment mQuickSettingsFragment;
    private Handler                 returnHandler           = new Handler();
    public boolean                  hasChanged              = false;
    private boolean                 canChange               = true;
    public int                      mDragStartIndex;
    public int                      mDragCurrentIndex;
    public int[]                    mDragStartPosition;
    public int                      mDragInitialScroll;
    public PointF                   mTouchPosition          = new PointF(0, 0);
    public FolderListAdapter        mAdapter;
    public View                     mDragButton;
    public FolderStructure.Folder mDragFolder;
    public String                   mCurrentlySelectedFolder;
    public int[]                    mDragTargetPosition     = new int[2];
    public RecyclerView             mFolderList;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_folder_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        LauncherActivity activity = (LauncherActivity)getActivity();
        activity.mDragView.setFolderListFragment(this);

        mFolderList = (RecyclerView)view.findViewById(R.id.folder_list);
        mFolderList.setLayoutManager(new GridLayoutManager(activity, 4));

        mAdapter = new FolderListAdapter(activity);

        //Init FolderList RecyclerView
        mFolderList.setAdapter(mAdapter);
        mFolderList.addItemDecoration(new SpacesItemDecoration(LauncherUtils.dpToPx(2, activity)));
        mFolderList.setEnabled(true);
        mFolderList.setFocusable(true);
        mFolderList.setFocusableInTouchMode(true);
        mFolderList.setNestedScrollingEnabled(true);
        DefaultItemAnimator animator =  new DefaultItemAnimator();
        animator.setChangeDuration(80);
        animator.setMoveDuration(200);
        animator.setRemoveDuration(100);
        animator.setAddDuration(100);
        mFolderList.setItemAnimator(animator);
    }

    /**
     * Determines the top panel background reveal center point depending on the index of a folder so that its right behind its viewHolder.
     * @param pos The folder index.
     * @param defaultC A default value for when something went wrong
     * @return Either the absolute center of the ViewHolder at the given position or just the default value.
     */
    public int[] getRevealXCenter(int pos, int[] defaultC){
        if(getView()!=null){
            RecyclerView mFolderList = (RecyclerView)getView().findViewById(R.id.folder_list);
            RecyclerView.ViewHolder h = mFolderList.findViewHolderForAdapterPosition(pos);
            int[] p = new int[2];
            if(h!=null) {
                h.itemView.getLocationInWindow(p);
                p[1] += LauncherUtils.dpToPx(22, getContext());
                p[0] += h.itemView.getMeasuredWidth()/2;
                return p;
            }
        }
        return defaultC;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mQuickSettingsFragment == null) {
            mQuickSettingsFragment = ((QuickSettingsFragment) ((LauncherActivity) getActivity()).mAppBarPager.getAdapter().instantiateItem(((LauncherActivity) getActivity()).mAppBarPager, 0));
        }
    }

    /**
     * Initiates a hovering event, which may replace and animate other folders when hovering over them.
     * @param x The absolute hover x position
     * @param y The absolute hover y position
     */
    public void hover(int x, int y){
        if(getView()==null)
            return;
        FolderRecyclerView mFolderList = (FolderRecyclerView)getView().findViewById(R.id.folder_list);
        int[] positionOfList = new int[2];
        int scrollPadding = LauncherUtils.dpToPx(24, getActivity());
        mFolderList.getLocationInWindow(positionOfList);

        Rect scrollUpRect = new Rect(positionOfList[0], positionOfList[1], positionOfList[0]+mFolderList.getMeasuredWidth(), positionOfList[1]+2*scrollPadding);
        Rect scrollDownRect = new Rect(positionOfList[0], positionOfList[1]+mFolderList.getMeasuredHeight()-scrollPadding, positionOfList[0]+mFolderList.getMeasuredWidth(), positionOfList[1]+mFolderList.getMeasuredHeight());

        GridLayoutManager layoutManager = ((GridLayoutManager)mFolderList.getLayoutManager());

        if(scrollUpRect.contains(x,y)){
            mFolderList.scrollBy(0, -LauncherUtils.dpToPx(8, getActivity()));
            return;
        }
        else if(scrollDownRect.contains(x,y) && layoutManager.findLastCompletelyVisibleItemPosition()!=mFolderList.getAdapter().getItemCount()-1){
            mFolderList.scrollBy(0, LauncherUtils.dpToPx(8, getActivity()));
            return;
        }

        int hoverIndex = mFolderList.indexOfChild(mFolderList.findChildViewUnder(x, y));

        LauncherLog.d("FLF", hoverIndex + "  ...  " + mDragCurrentIndex);

        if (canChange&&hoverIndex!=-1&&hoverIndex!=mDragCurrentIndex && hoverIndex < LauncherActivity.mFolderStructure.folders.size()){
            canChange = false;
            hasChanged = true;
            returnHandler.removeCallbacksAndMessages(null);
            LauncherLog.d("FLF", "Change...");
            FolderStructure.Folder tmp = LauncherActivity.mFolderStructure.folders.get(mDragCurrentIndex);
            LauncherActivity.mFolderStructure.folders.remove(mDragCurrentIndex);
            LauncherActivity.mFolderStructure.folders.add(hoverIndex, tmp);
            mAdapter.notifyItemMoved(mDragCurrentIndex, hoverIndex);
            mFolderList.getChildAt(hoverIndex).getLocationInWindow(mDragTargetPosition);
            mDragCurrentIndex = hoverIndex;
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    canChange = true;
                }
            }, 150);
        }

        if( hasChanged && scrollDownRect.bottom<y && mDragCurrentIndex!=mDragStartIndex){
            hasChanged = false;
            FolderStructure.Folder tmp = LauncherActivity.mFolderStructure.folders.get(mDragCurrentIndex);
            LauncherActivity.mFolderStructure.folders.remove(mDragCurrentIndex);
            LauncherActivity.mFolderStructure.folders.add(mDragStartIndex, tmp);
            mAdapter.notifyItemMoved(mDragCurrentIndex, mDragStartIndex);

            mDragTargetPosition = mDragStartPosition.clone();
            mDragCurrentIndex = mDragStartIndex;
        }
    }

    /**
     * The Adapter for the FolderList.
     */
    public class FolderListAdapter extends RecyclerView.Adapter<FolderListAdapter.FolderHolder>{
        public class FolderHolder extends RecyclerView.ViewHolder{
            public FolderHolder(View view){
                super(view);
            }
        }

        Context mContext;

        public FolderListAdapter(Context context){
            mContext = context;
        }

        @Override
        public int getItemCount() {
            return LauncherActivity.mFolderStructure.folders.size();
        }

        @Override
        public FolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FolderHolder(new FolderButton(mContext));
        }

        @Override
        public void onBindViewHolder(FolderHolder holder, final int position) {
            final LauncherActivity activity = (LauncherActivity)getActivity();
            final int selected = activity.mPager.getCurrentItem();

            final FolderButton button = (FolderButton)holder.itemView;

            RecyclerView.LayoutParams params =
                    new RecyclerView.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

            button.setLayoutParams(params);

            button.assignFolder(LauncherActivity.mFolderStructure.folders.get(position));
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
            if(position==selected && !isDragging
                    ){
                button.select(LauncherActivity.mFolderStructure.folders.get(position).accentColor);
            } else {
                button.deselect();
            }

            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((LauncherActivity)getActivity()).mPager.setCurrentItem(position, true);
                    select(position);
                }
            });

            if(LauncherActivity.mFolderStructure.folders.get(position).equals(mDragFolder)){
                button.setVisibility(View.INVISIBLE);
            } else {
                button.setVisibility(View.VISIBLE);
            }

            button.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if(!isDragging) {
                        mTouchPosition.x = event.getX();
                        mTouchPosition.y = event.getY();
                    }
                    return false;
                }
            });

            button.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    isDragging = true;

                    mDragInitialScroll = mFolderList.computeVerticalScrollRange();
                    LauncherLog.d(getClass(), mDragInitialScroll + " scrolled");

                    mCurrentlySelectedFolder = LauncherActivity.mFolderStructure.folders.get(activity.mPager.getCurrentItem()).folderName;
                    int width = v.getMeasuredWidth();
                    int height = v.getMeasuredHeight();
                    if(mDragStartPosition==null)
                        mDragStartPosition = new int[2];
                    v.getLocationInWindow(mDragStartPosition);

                    FolderButton button1 = new FolderButton(getContext());
                    button1.assignFolder(LauncherActivity.mFolderStructure.folders.get(position));
                    button1.deselect();
                    mDragFolder = LauncherActivity.mFolderStructure.folders.get(position);
                    ((LauncherActivity) getActivity()).mDragView.addView(button1, new FrameLayout.LayoutParams(width, height));

                    mTouchPosition.x -= width/2;
                    mTouchPosition.y -= height/2;

                    button1.setTextColor(0xffffffff);
                    button1.setX(mDragStartPosition[0]);
                    button1.setY(mDragStartPosition[1]);
                    mDragTargetPosition = mDragStartPosition.clone();
                    mDragButton = button1;
                    mDragCurrentIndex = position;
                    mDragStartIndex = position;
                    notifyDataSetChanged();
                    activity.mDragView.setOnDragListener(activity.mDragView);
                    activity.mDragView.startDrag(DragSurfaceLayout.DragType.TYPE_FOLDER);
                    return true;
                }
            });

            button.setTranslationZ(0);
            button.setElevation(0);
        }

        boolean isDragging = false;

        public void cancelDrag(){
            isDragging = false;
        }

        public void select(int position){
            notifyDataSetChanged();
        }
    }
}
