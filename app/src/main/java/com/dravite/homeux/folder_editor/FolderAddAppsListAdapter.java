package com.dravite.homeux.folder_editor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.DrawerObject;
import com.dravite.homeux.drawerobjects.helpers.AppLauncherActivityInfoComparator;
import com.dravite.homeux.general_helpers.JsonHelper;
import com.dravite.homeux.views.AppIconView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Johannes on 13.04.2015.
 *  A RecyclerView Adapter showing all apps and enabling to have some of them selected.
 */
public class FolderAddAppsListAdapter extends RecyclerView.Adapter<FolderAddAppsListAdapter.AppViewHolder>{
    public static class AppViewHolder extends RecyclerView.ViewHolder{
        public AppViewHolder(Context context, ViewGroup parent){
            super(LayoutInflater.from(context).inflate(R.layout.icon, parent, false));
        }
    }

    private Context mContext;
    private List<LauncherActivityInfo> mAppInfos; //All apps
    public ArrayList<ComponentName> mSelectedAppsList; //Apps that are selected
    public ArrayList<ComponentName> mContainsList;  //Apps that are already contained in the folder
    private List<LauncherActivityInfo> mAppInfosWithoutOtherFolders; //All apps without those which are in another folder.
    private List<LauncherActivityInfo> mAppInfosWithOtherFolders; //Basically all apps.

    //Constructor
    public FolderAddAppsListAdapter(Context context, ArrayList<ComponentName> appList, ArrayList<ComponentName> containsList){
        mContext = context;

        mAppInfos = ((LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE)).getActivityList(null, android.os.Process.myUserHandle());
        Collections.sort(mAppInfos, new AppLauncherActivityInfoComparator(context));
        mSelectedAppsList = appList;

        mContainsList = containsList;

        ArrayList<LauncherActivityInfo> newInfos = new ArrayList<>(mAppInfos);
        List<ComponentName> componentNames = JsonHelper.loadHiddenAppList(mContext);

        for (int i = 0; i < newInfos.size(); i++) {
            if(componentNames.contains(newInfos.get(i).getComponentName()) || mContainsList.contains(newInfos.get(i).getComponentName())){
                newInfos.remove(i);
                i--;
            }
        }

        mAppInfosWithoutOtherFolders = new ArrayList<>(newInfos);
        mAppInfosWithOtherFolders = new ArrayList<>(newInfos);

        FolderStructure structure = LauncherActivity.mFolderStructure;

        //Filling up the list containing apps which are not already in any folder.
        for (FolderStructure.Folder f: structure.folders) {
            for (FolderStructure.Page p: f.pages){
                for (DrawerObject d:p.items){
                    for(int i=0; i<mAppInfosWithoutOtherFolders.size();i++){
                        if(!f.isAllFolder && d instanceof Application && mAppInfosWithoutOtherFolders.get(i).getComponentName().equals(new ComponentName(((Application) d).packageName, ((Application) d).className))){
                            mAppInfosWithoutOtherFolders.remove(i);

                            i--;
                        }
                    }
                }
            }
        }



        mAppInfos.clear();
        mAppInfos = mAppInfosWithOtherFolders;
    }

    @Override
    public void onViewRecycled(AppViewHolder holder) {
        ((AppIconView)holder.itemView).setIcon(null);
        super.onViewRecycled(holder);
    }

    /**
     * Sets whether apps should be selectable/visible if they are already in another folder.
     * @param doShow true to show the apps.
     */
    public void setShowInOthers(boolean doShow){
        mAppInfos = doShow?mAppInfosWithOtherFolders:mAppInfosWithoutOtherFolders;
        notifyDataSetChanged();

    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, final int position) {

        GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LauncherUtils.dpToPx(96, mContext));

        ((AppIconView)holder.itemView).overrideData(56);
        ((AppIconView) holder.itemView).setText(mAppInfos.get(position).getLabel());

        //Load icons asynchronously
        LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
            @Override
            public void run() {
                final Drawable drawable = mAppInfos.get(position).getIcon(0);
                drawable.setBounds(0, 0, LauncherUtils.dpToPx(64, mContext), LauncherUtils.dpToPx(64, mContext));

                ((Activity)mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(holder.getAdapterPosition()==position) {
                            if (mSelectedAppsList.contains(mAppInfos.get(position).getComponentName())) {
                                ((AppIconView) holder.itemView).setIcon(generateSelectedDrawable(drawable));
                            } else {
                                drawable.setTintList(null);
                                ((AppIconView) holder.itemView).setIcon(drawable);
                            }
                        }
                    }
                });

            }
        });



        ((AppIconView) holder.itemView).setLayoutParams(params);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mSelectedAppsList.contains(mAppInfos.get(holder.getAdapterPosition()).getComponentName())){
                    mSelectedAppsList.remove(mAppInfos.get(holder.getAdapterPosition()).getComponentName());
                } else {
                    mSelectedAppsList.add(mAppInfos.get(holder.getAdapterPosition()).getComponentName());
                }
                notifyItemChanged(holder.getAdapterPosition());
            }
        });
    }

    /**
     * Overlays a selection icon and tints the base icon into a red'ish color.
     * @param base The base app icon.
     * @return A "selected" version of it.
     */
    Drawable generateSelectedDrawable(Drawable base){
        base.setTint(Color.DKGRAY);
        base.setTintMode(PorterDuff.Mode.MULTIPLY);
        Drawable hiddenOverlay = mContext.getDrawable(R.drawable.ic_okay);
        hiddenOverlay.setTint(0xFF4CAF50);
        hiddenOverlay.setBounds(0, 0, LauncherUtils.dpToPx(24, mContext), LauncherUtils.dpToPx(24, mContext));
        LayerDrawable combined = new LayerDrawable(new Drawable[]{base, hiddenOverlay});
        int sixteen = LauncherUtils.dpToPx(12, mContext);
        combined.setLayerInset(1, sixteen, sixteen, sixteen, sixteen);
        combined.setBounds(0, 0, LauncherUtils.dpToPx(56, mContext), LauncherUtils.dpToPx(56, mContext));
        return combined;
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(mContext, parent);
    }

    @Override
    public int getItemCount() {
        return mAppInfos.size();
    }
}
