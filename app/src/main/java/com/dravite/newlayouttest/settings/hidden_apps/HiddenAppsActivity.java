package com.dravite.newlayouttest.settings.hidden_apps;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.drawerobjects.helpers.AppLauncherActivityInfoComparator;
import com.dravite.newlayouttest.general_helpers.JsonHelper;
import com.dravite.newlayouttest.views.AppIconView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by Johannes on 23.10.2015.
 * Shows a list of apps to choose which ones are hidden in the Launcher.
 */
public class HiddenAppsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.hidden_apps_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mHiddenAppList = (RecyclerView)findViewById(R.id.hiddenAppsList);
        mHiddenAppList.setLayoutManager(new GridLayoutManager(this, 4));
        mHiddenAppList.setAdapter(new HiddenAppsAdapter(this));

    }

    //RecyclerView Adapter
    public static class HiddenAppsAdapter extends RecyclerView.Adapter<HiddenAppsAdapter.AppViewHolder>{
        public static class AppViewHolder extends RecyclerView.ViewHolder{
            public AppViewHolder(Context context){
                super(LayoutInflater.from(context).inflate(R.layout.icon, null));
            }
        }

        private Context mContext;
        private List<LauncherActivityInfo> mAppInfos; //All apps
        private List<ComponentName> mHiddenComponents = new ArrayList<>(); //Hidden apps

        public HiddenAppsAdapter(Context context){
            mContext = context;
            mAppInfos = ((LauncherApps)context.getSystemService(LAUNCHER_APPS_SERVICE)).getActivityList(null, android.os.Process.myUserHandle());
            Collections.sort(mAppInfos, new AppLauncherActivityInfoComparator(context));
            mHiddenComponents = JsonHelper.loadHiddenAppList(mContext);
        }

        @Override
        public void onBindViewHolder(final AppViewHolder holder, final int position) {

            GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LauncherUtils.dpToPx(80, mContext));
            ((AppIconView) holder.itemView).overrideData(48);
            ((AppIconView) holder.itemView).setText(mAppInfos.get(position).getLabel());

            LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
                @Override
                public void run() {
                    final Drawable drawable = mAppInfos.get(position).getIcon(0);
                    drawable.setBounds(0, 0, LauncherUtils.dpToPx(56, mContext), LauncherUtils.dpToPx(56, mContext));
                    ((Activity) mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if(holder.getAdapterPosition() == position) {
                                if (mHiddenComponents.contains(mAppInfos.get(position).getComponentName())) {
                                    ((AppIconView) holder.itemView).setIcon(generateHiddenDrawable(drawable));
                                    ((AppIconView) holder.itemView).setTextColor(0x44000000);
                                } else {
                                    drawable.setTintList(null);
                                    ((AppIconView) holder.itemView).setIcon(drawable);
                                    ((AppIconView) holder.itemView).setTextColor(0xe1000000);
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
                    if(mHiddenComponents.contains(mAppInfos.get(position).getComponentName())){
                        mHiddenComponents.remove(mAppInfos.get(position).getComponentName());
                    } else {
                        mHiddenComponents.add(mAppInfos.get(position).getComponentName());
                    }
                    JsonHelper.saveHiddenAppList(mContext, mHiddenComponents);
                    notifyItemChanged(position);
                }
            });
        }

        /**
         * Overlays a hidden icon and tints the base icon into a greyish tone.
         * @param base
         * @return
         */
        Drawable generateHiddenDrawable(Drawable base){
            base.setTint(0x70F44336);
            Drawable hiddenOverlay = mContext.getDrawable(R.drawable.ic_hide);
            hiddenOverlay.setTint(0xffe1e1e1);
            hiddenOverlay.setBounds(0, 0, LauncherUtils.dpToPx(24, mContext), LauncherUtils.dpToPx(24, mContext));
            LayerDrawable combined = new LayerDrawable(new Drawable[]{base, hiddenOverlay});
            int sixteen = LauncherUtils.dpToPx(12, mContext);
            combined.setLayerInset(1, sixteen, sixteen, sixteen, sixteen);
            combined.setBounds(0, 0, LauncherUtils.dpToPx(56, mContext), LauncherUtils.dpToPx(56, mContext));
            return combined;
        }

        @Override
        public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AppViewHolder(mContext);
        }

        @Override
        public int getItemCount() {
            return mAppInfos.size();
        }
    }

}
