package com.dravite.homeux.general_adapters;

import android.content.Context;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.helpers.AppLauncherActivityInfoComparator;

import java.util.Collections;
import java.util.List;

/**
 * An adapter to select an application from a list of installed apps
 */
public class AppSelectAdapter extends BaseAdapter {

    /**
     * A ViewHolder for an app with its icon
     */
    public static class AppHolder{
        TextView name;
        ImageView icon;
    }

    private final Context mContext;

    //A list of all shown apps
    public List<LauncherActivityInfo> mInfos;

    public AppSelectAdapter(Context context){
        this.mContext = context;
        mInfos = ((LauncherApps)context.getSystemService(Context.LAUNCHER_APPS_SERVICE)).getActivityList(null, android.os.Process.myUserHandle());
        Collections.sort(mInfos, new AppLauncherActivityInfoComparator(context));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        AppHolder holder;

        //Apply ViewHolder pattern
        if(convertView==null){
            LayoutInflater inflater = LayoutInflater.from(mContext);
            convertView = inflater.inflate(R.layout.folder_app_list_item, parent, false);
            holder = new AppHolder();
            holder.name = ((TextView) convertView.findViewById(R.id.name));
            holder.icon = ((ImageView) convertView.findViewById(R.id.appSelector));
            convertView.setTag(holder);
        } else {
            holder = (AppHolder) convertView.getTag();
        }

        holder.name.setText(mInfos.get(position).getLabel());
        holder.icon.setImageDrawable(mInfos.get(position).getIcon(0));
        holder.icon.setImageTintList(null);

        return convertView;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return mInfos.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }
}
