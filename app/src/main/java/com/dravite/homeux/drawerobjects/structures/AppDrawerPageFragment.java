package com.dravite.homeux.drawerobjects.structures;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.views.CustomGridLayout;
import com.dravite.homeux.folder_editor.FolderEditorActivity;
import com.dravite.homeux.folder_editor.FolderEditorAddActivity;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.DrawerObject;

import java.util.ArrayList;

/**
 * Created by Johannes on 04.09.2015.
 *
 */
public class AppDrawerPageFragment extends Fragment {
    //Vibration duration when long pressing on a free place
    private static final int VIBRATE_DROP = 20;

    public CustomGridLayout mAppGrid;
    public int mPage;
    public int mFolderPos;
    public ViewPager mPager;

    //if this tag is set to -1, the fragment will be removed
    public int cTag = 0;

    //Is the AppGrid populated yet?
    boolean mIsPopulated = false;

    /**
     * Creates a new {@link AppDrawerPageFragment} instance.
     * @param folderPos The index of the folder which contains this page
     * @param page The page index
     * @return A new {@link AppDrawerPageFragment} instance.
     */
    public static AppDrawerPageFragment newInstance(int folderPos, int page){
        AppDrawerPageFragment fragment = new AppDrawerPageFragment();
        Bundle args = new Bundle();
        args.putInt("page", page);
        args.putInt("folder", folderPos);
        fragment.setArguments(args);
        return fragment;
    }

    public void setRemovalTag(int cTag) {
        this.cTag = cTag;
    }

    public int getRemovalTag() {
        return cTag;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        if(savedInstanceState!=null){
            mIsPopulated = savedInstanceState.getBoolean("isPopulated");
        }
        mPage = getArguments().getInt("page", 0);
        mFolderPos = getArguments().getInt("folder", 0);
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mPager = ((ViewPager) container);
        return inflater.inflate(R.layout.grid_crad, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Only initialize if the contained AppGrid is not assigned yet.
        if(mAppGrid==null) {
            final LauncherActivity activity = ((LauncherActivity) getActivity());

            //Initialize AppGrid
            mAppGrid = (CustomGridLayout) view.findViewById(R.id.appGrid);
            mAppGrid.setAppWidgetContainer(activity.mAppWidgetContainer);
            mAppGrid.setDragSurface(activity.mDragView);
            mAppGrid.setPager(mPager);

            final Vibrator vibrator = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            mAppGrid.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //Open a dialog to select something to add to the screen
                    if (mAppGrid.getGridType() == FolderStructure.Folder.FolderType.TYPE_WIDGETS) {
                        vibrator.vibrate(VIBRATE_DROP);
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.DialogTheme);
                        builder.setItems(new CharSequence[]{"Widget", "Shortcut", "Apps", "Remove Page"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        mAppGrid.getAppWidgetContainer().selectWidget();
                                        break;
                                    case 1:
                                        mAppGrid.getAppWidgetContainer().selectShortcut();
                                        break;
                                    case 2:
                                        FolderStructure.Folder currentFolder = LauncherActivity.mFolderStructure.folders.get(((LauncherActivity)getActivity()).mPager.getCurrentItem());
                                        ArrayList<ComponentName> mCurrentContainsList = new ArrayList<>();
                                        for (FolderStructure.Page page:currentFolder.pages){
                                            for (DrawerObject object:page.items){
                                                if(object instanceof Application){
                                                    mCurrentContainsList.add(new ComponentName(((Application) object).packageName, ((Application) object).className));
                                                }
                                            }
                                        }
                                        Intent intent = new Intent(getActivity(), FolderEditorAddActivity.class);
                                        intent.putExtra("showSave", true);
                                        FolderEditorActivity.AppListPasser.passAlreadyContainedList(mCurrentContainsList);
                                        FolderEditorActivity.AppListPasser.passAppList(new ArrayList<ComponentName>());
                                        getActivity().startActivityForResult(intent, FolderEditorAddActivity.REQUEST_APP_LIST_MAIN);
                                        break;
                                    case 3:
                                        new AlertDialog.Builder(getActivity(), R.style.DialogTheme)
                                                .setTitle("Remove Page")
                                                .setMessage("Do you really want to remove this page and all its content?")
                                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {
                                                        ((AppDrawerPagerAdapter) mPager.getAdapter()).removePage(mPage);
                                                    }
                                                }).setNegativeButton(android.R.string.no, null)
                                                .show();
                                }
                            }
                        });
                        builder.setTitle("What to add...");
                        builder.show();
                    }
                    return false;
                }
            });

            //For the "All"-Folder set the corresponding grid typ
            if(LauncherActivity.mFolderStructure.folders.get(mFolderPos).isAllFolder){
                mAppGrid.setGridType(CustomGridLayout.GridType.TYPE_APPS_ONLY);
            }

            //Update AppGrid
            mAppGrid.setRowCount(activity.mHolder.gridHeight);
            mAppGrid.setColumnCount(activity.mHolder.gridWidth);
            mAppGrid.setSaveEnabled(true);
            mAppGrid.setSaveFromParentEnabled(true);
            mAppGrid.setPosition(mFolderPos, mPage);

            //Populate if not happened yet
            if (!mIsPopulated) {
                if(mPage< LauncherActivity.mFolderStructure.folders.get(mFolderPos).pages.size())
                   // mAppGrid.populate(LauncherActivity.mFolderStructure.folders.get(mFolderPos).pages.get(mPage).items);
                mAppGrid.refresh();
                mIsPopulated = true;
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
//        if(mAppGrid!=null){
//            //Check for removed apps.
//            mAppGrid.checkRemoved();
//        }
        View getView = getView();
        if(getView!=null){
            //Set card background if needed.
            CardView card = ((CardView) getView.findViewById(R.id.testMainCard));
            card.setCardBackgroundColor(!((LauncherActivity)getActivity()).mHolder.showCard?0x00000000:0xffffffff);
            card.setCardElevation(!((LauncherActivity)getActivity()).mHolder.showCard?0: LauncherUtils.dpToPx(4, getContext()));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isPopulated", mIsPopulated);
        super.onSaveInstanceState(outState);
    }
}
