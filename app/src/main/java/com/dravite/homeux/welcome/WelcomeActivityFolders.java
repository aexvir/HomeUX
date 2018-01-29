package com.dravite.homeux.welcome;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LauncherApps;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.DrawerObject;
import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.folder_editor.FolderEditorActivity;
import com.dravite.homeux.general_helpers.JsonHelper;
import com.dravite.homeux.views.FolderButton;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by johannesbraun on 19.11.15.<br/>
 * Here, the user finds functionality to add/remove/edit folders in the setup process.
 */
public class WelcomeActivityFolders extends AppCompatActivity implements View.OnClickListener{

    private WelcomeFolderAdapter mAdapter;
    private GestureDetector mTapGestureDetector;
    private int mCurrentFolderIndex = -1, mClickedFolderIndex;
    private FrameLayout mDragLayer;
    private Vibrator mVibrator;

    private class SingleTapGestureDetector extends GestureDetector.SimpleOnGestureListener{
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return true;
        }
    }

    @Override
    public void onClick(final View v) {
        //Edit Folder1
        new AlertDialog.Builder(WelcomeActivityFolders.this, R.style.DialogTheme).setNegativeButton(android.R.string.cancel, null)
                .setItems(mAdapter.mFolderStructure.folders.get(mClickedFolderIndex).folderName.equals("All")?new String[]{"Edit", "Set as default"}:new String[]{"Edit", "Set as default", "Delete"}, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            case 0:
                                FolderEditorActivity.FolderPasser.passFolder = new WeakReference<>(mAdapter.mFolderStructure.folders.get(mClickedFolderIndex));

                                ArrayList<ComponentName> names = new ArrayList<>();

                                for (FolderStructure.Page page : mAdapter.mFolderStructure.folders.get(mClickedFolderIndex).pages) {
                                    for (DrawerObject item : page.items) {
                                        if (item instanceof Application) {
                                            names.add(new ComponentName(((Application) item).packageName, ((Application) item).className));
                                        }
                                    }
                                }

                                FolderEditorActivity.AppListPasser.passAlreadyContainedList(names);
                                Intent intent = new Intent(WelcomeActivityFolders.this, FolderEditorActivity.class);
                                intent.putExtra("requestCode", FolderEditorActivity.REQUEST_EDIT_FOLDER);
                                intent.putExtra("folderIndex", mAdapter.mFolderStructure.folders.indexOf(mAdapter.mFolderStructure.folders.get(mClickedFolderIndex)));

                                LauncherUtils.startActivityForResult(WelcomeActivityFolders.this, v, intent, FolderEditorActivity.REQUEST_EDIT_FOLDER);
                                break;
                            case 1:
                                int tmpOldIndex = mAdapter.mFolderStructure.getDefaultFolderIndex(WelcomeActivityFolders.this);
                                PreferenceManager.getDefaultSharedPreferences(WelcomeActivityFolders.this).edit()
                                        .putString(Const.Defaults.TAG_DEFAULT_FOLDER, mAdapter.mFolderStructure.folders.get(mClickedFolderIndex).folderName).apply();
                                mAdapter.notifyItemChanged(tmpOldIndex);
                                mAdapter.notifyItemChanged(mAdapter.mFolderStructure.getDefaultFolderIndex(WelcomeActivityFolders.this));
                                break;
                            case 2:
                                mAdapter.mFolderStructure.remove(mClickedFolderIndex);
                                mAdapter.notifyItemRemoved(mClickedFolderIndex);
                                mAdapter.notifyItemChanged(mAdapter.mFolderStructure.getDefaultFolderIndex(WelcomeActivityFolders.this));
                                JsonHelper.saveFolderStructure(WelcomeActivityFolders.this, mAdapter.mFolderStructure);
                                PreferenceManager.getDefaultSharedPreferences(WelcomeActivityFolders.this).edit()
                                        .putString(Const.Defaults.TAG_DEFAULT_FOLDER, "All").apply();

                                break;
                        }
                    }
                }).show();
    }

    ItemTouchHelper.Callback callback = new ItemTouchHelper.Callback() {
        @Override
        public int getMovementFlags(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder) {
            return makeFlag(ItemTouchHelper.ACTION_STATE_DRAG,
                    ItemTouchHelper.DOWN | ItemTouchHelper.UP | ItemTouchHelper.START | ItemTouchHelper.END);
        }

        @Override
        public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
            Collections.swap(mAdapter.mFolderStructure.folders, viewHolder.getAdapterPosition(), target.getAdapterPosition());
            JsonHelper.saveFolderStructure(WelcomeActivityFolders.this, mAdapter.getFolderStructure());
            mAdapter.notifyItemMoved(viewHolder.getAdapterPosition(), target.getAdapterPosition());
            return true;
        }

        @Override
        public boolean isLongPressDragEnabled() {
            return true;
        }

        @Override
        public void onMoved(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, int fromPos, RecyclerView.ViewHolder target, int toPos, int x, int y) {

            super.onMoved(recyclerView, viewHolder, fromPos, target, toPos, x, y);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        }
    };
    ItemTouchHelper mItemHelper = new ItemTouchHelper(callback); //Helps moving around stuff TODO probably useless without drag

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_folders);

        getWindow().setNavigationBarColor(0xff1976d2);
        getWindow().setStatusBarColor(0xff1976d2);

        mVibrator = (Vibrator)getSystemService(VIBRATOR_SERVICE);

        RecyclerView folderList = (RecyclerView)findViewById(R.id.folderList);
        folderList.setLayoutManager(new GridLayoutManager(this, 4));

        mItemHelper.attachToRecyclerView(folderList);

        mAdapter = new WelcomeFolderAdapter(this);
        folderList.setAdapter(mAdapter);

        mTapGestureDetector = new GestureDetector(this, new SingleTapGestureDetector());

        mDragLayer = (FrameLayout)findViewById(R.id.dragLayer);

        //Fab to add a folder
        FloatingActionButton fabAdd = (FloatingActionButton)findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderStructure.Folder newFolder = new FolderStructure.Folder();
                newFolder.headerImage = BitmapFactory.decodeResource(getResources(), R.drawable.welcome_header_small);
                newFolder.isAllFolder = false;
                newFolder.accentColor = 0xFF303F9F;
                newFolder.folderName = "";
                newFolder.folderIconRes = "ic_folder";
                newFolder.pages.add(new FolderStructure.Page());
                newFolder.mFolderType = FolderStructure.Folder.FolderType.TYPE_WIDGETS;
                FolderEditorActivity.FolderPasser.passFolder = new WeakReference<>(newFolder);
                Intent intent = new Intent(WelcomeActivityFolders.this, FolderEditorActivity.class);
                intent.putExtra("requestCode", FolderEditorActivity.REQUEST_ADD_FOLDER);

                LauncherUtils.startActivityForResult(WelcomeActivityFolders.this, v, intent, FolderEditorActivity.REQUEST_ADD_FOLDER);
            }
        });

        //next screen
        Button finishButton = (Button)findViewById(R.id.finish);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mainIntent = new Intent(WelcomeActivityFolders.this, WelcomeActivityTopPanelInfo.class);
                startActivity(mainIntent);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK)
            switch (requestCode){
                case FolderEditorActivity.REQUEST_ADD_FOLDER:
                    FolderStructure.Folder folder = FolderEditorActivity.FolderPasser.passFolder.get();


                    ArrayList arrList = data.getParcelableArrayListExtra("appList");

                    if(arrList.size()!=0){
                        folder.pages.clear();
                    }

                    int allPageCount = (int) Math.ceil(arrList.size()/16f);
                    for (int i = 0; i < allPageCount; i++) {
                        final FolderStructure.Page page = new FolderStructure.Page();
                        for (int j = 0; j < 16 && (i*16+j)<arrList.size(); j++) {

                                Intent appIntent = new Intent();
                                appIntent.setComponent((ComponentName) arrList.get(i * 16 + j));
                                page.add(new Application(((LauncherApps)getSystemService(LAUNCHER_APPS_SERVICE)).resolveActivity(appIntent, android.os.Process.myUserHandle())));

                        }
                        folder.add(page);
                    }


                    mAdapter.addFolder(folder);
                    FolderEditorActivity.FolderPasser.passFolder.clear();
                    JsonHelper.saveFolderStructure(this, mAdapter.getFolderStructure());
                    break;
                case FolderEditorActivity.REQUEST_EDIT_FOLDER:
                    if (data==null) break;
                    String iconRes = data.getStringExtra("iconRes");
                    int accent = data.getIntExtra("accent", Color.WHITE);
                    final int fIndex = data.getIntExtra("folderIndex", mClickedFolderIndex);
                    String name = data.getStringExtra("folderName");
                    mAdapter.mFolderStructure.folders.get(fIndex).folderIconRes = iconRes;
                    mAdapter.mFolderStructure.folders.get(fIndex).folderName = name;
                    mAdapter.mFolderStructure.folders.get(fIndex).accentColor = accent;

                    ArrayList addedList = data.getParcelableArrayListExtra("appList");

                    addedList.removeAll(JsonHelper.loadHiddenAppList(this));

                    if( mAdapter.mFolderStructure.folders.get(fIndex).pages.size()==1 &&  mAdapter.mFolderStructure.folders.get(fIndex).pages.get(0).items.size()==0)
                        mAdapter.mFolderStructure.folders.get(fIndex).pages.clear();

                    int pageCount = (int) Math.ceil(addedList.size()/16f);
                    for (int i = 0; i < pageCount; i++) {
                        final FolderStructure.Page page = new FolderStructure.Page();
                        for (int j = 0; j < 16 && (i*16+j)<addedList.size(); j++) {
                                Intent appIntent = new Intent();
                                appIntent.setComponent((ComponentName) addedList.get(i * 16 + j));
                                page.add(new Application(((LauncherApps) getSystemService(LAUNCHER_APPS_SERVICE)).resolveActivity(appIntent, android.os.Process.myUserHandle())));
                        }
                        mAdapter.mFolderStructure.folders.get(fIndex).add(page);
                    }

                    if( mAdapter.mFolderStructure.folders.get(fIndex).pages.size()==0)
                        mAdapter.mFolderStructure.folders.get(fIndex).pages.add(new FolderStructure.Page());

                    mAdapter.notifyItemChanged(fIndex);
                    JsonHelper.saveFolderStructure(this, mAdapter.mFolderStructure);
                    break;
            }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * @param pos Folder position
     * @return true, if the folder at the position is default.
     */
    boolean isDefaultFolder(int pos){
        return mAdapter.mFolderStructure.getDefaultFolderIndex(this)==pos;
    }

    /**
     * This adapter is like the {@link com.dravite.homeux.top_fragments.FolderListFragment.FolderListAdapter} with less functionality.
     */
    public class WelcomeFolderAdapter extends RecyclerView.Adapter<WelcomeFolderAdapter.WelcomeFolderHolder>{
        public class WelcomeFolderHolder extends RecyclerView.ViewHolder{
            public WelcomeFolderHolder(View view){
                super(view);
            }
        }

        private Context mContext;
        private FolderStructure mFolderStructure;
        RecyclerView parentView;

        public void addFolder(FolderStructure.Folder folder){
            mFolderStructure.add(folder);
            notifyItemInserted(mFolderStructure.folders.indexOf(folder));
        }

        public FolderStructure getFolderStructure() {
            return mFolderStructure;
        }

        public WelcomeFolderAdapter(Context context){
            mContext = context;
            mFolderStructure = LauncherActivity.mFolderStructure;
            if(mFolderStructure==null) {
                mFolderStructure = new FolderStructure();
                FolderStructure.Folder folder = new FolderStructure.Folder();
                folder.folderName = "All";
                folder.isAllFolder = true;
                folder.accentColor = 0xffef6e47;
                folder.folderIconRes = "ic_all";
                folder.headerImage = LauncherUtils.drawableToBitmap(context.getDrawable(R.drawable.welcome_header_small));
                mFolderStructure.add(folder);
            }
        }

        @Override
        public void onBindViewHolder(WelcomeFolderHolder holder, final int position) {
            final FolderButton folderButton = ((FolderButton) holder.itemView);
            folderButton.assignFolder(mFolderStructure.folders.get(position));
            if(isDefaultFolder(position)){
                folderButton.select(0xffFFA726);
            } else {
                folderButton.select(0xff42A5F5);
            }
            folderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mClickedFolderIndex = position;
                    WelcomeActivityFolders.this.onClick(v);
                }
            });
            if(position==mCurrentFolderIndex){
                folderButton.setVisibility(View.INVISIBLE);
            } else {
                folderButton.setVisibility(View.VISIBLE);
            }
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            parentView = recyclerView;
            super.onAttachedToRecyclerView(recyclerView);
        }

        @Override
        public WelcomeFolderHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new WelcomeFolderHolder(new FolderButton(mContext));
        }

        @Override
        public int getItemCount() {
            return mFolderStructure.folders.size();
        }
    }
}
