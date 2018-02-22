package com.dravite.homeux.iconpacks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.general_helpers.IconPackManager;
import com.squareup.picasso.LruCache;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.TreeSet;

/**
 * Created by Johannes on 19.03.2015.
 * Displays all icons of an icon pack in a list.
 */
public class IconPackIconAdapter extends RecyclerView.Adapter<IconPackIconAdapter.AppViewHolder> {

    OnAppSelectedInterface onAppSelectedInterface;
    ArrayList<Integer> mIconsInt;
    ArrayList<String> mIconsString;
    Context mContext;
    IconPackManager.IconPack mIconPack;
    Picasso mPicasso;
    LruCache mCache;

    public IconPackIconAdapter(Context context, IconPackManager.IconPack iconPack, TreeSet<Integer> iconPacksInt, TreeSet<String> iconPacksString, OnAppSelectedInterface onAppSelectedInterface) {
        this.onAppSelectedInterface = onAppSelectedInterface;
        mContext = context;
        mIconPack = iconPack;
        mIconsInt = new ArrayList<>(iconPacksInt);
        if(mIconsInt.size()!=0)
            mIconsInt.remove(0);
        mIconsString = new ArrayList<>(iconPacksString);
        if(mIconsString.size()!=0)
            mIconsString.remove(0);
        mCache = new LruCache(mContext);
        mPicasso = new Picasso.Builder(mContext).memoryCache(mCache).build();
    }

    @Override
    public void onBindViewHolder(final AppViewHolder holder, final int position) {
        try{
            holder.icon.setImageDrawable(mContext.getPackageManager().getApplicationIcon(mIconPack.mPackageName));
            int eight = LauncherUtils.dpToPx(8, mContext);
            holder.icon.setPadding(eight, eight, eight, eight);
        } catch (PackageManager.NameNotFoundException e){

        }
        holder.icon.setImageTintList(null);
        holder.icon.setAlpha(1f);
        holder.itemView.setAlpha(1f);

        Uri imgUri = mIconsInt.size()==0?resourceToUri(mIconsString.get(position)):resourceToUri(mIconsInt.get(position));

        mPicasso.load(imgUri).into(holder.icon);
        int eight = LauncherUtils.dpToPx(8, mContext);
        holder.icon.setPadding(eight, eight, eight, eight);

        holder.setClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCache.clear();
                mCache = null;
                mPicasso.shutdown();
                onAppSelectedInterface.onAppSelected(LauncherUtils.drawableToBitmap(holder.icon.getDrawable()));
            }
        });

        holder.itemView.setElevation(0);
        holder.icon.setImageTintList(null);
    }

    /**
     * @param resName
     * @return the resolved resource Uri
     */
    public Uri resourceToUri(String resName){
        return Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE + "://" +
                mIconPack.mPackageName + '/' +
                "drawable" + "/" +
                resName);
    }

    /**
     * @param resID
     * @return the resolved resource Uri
     */
    public Uri resourceToUri (int resID) {
        return resourceToUri(mIconPack.mPackRes.getResourceEntryName(resID));
    }

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AppViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.simple_icon, null));
    }

    @Override
    public int getItemCount() {
        return Math.max(mIconsInt.size(), mIconsString.size());
    }

    public interface OnAppSelectedInterface {
        void onAppSelected(Bitmap icon);
    }

    public static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;

        public AppViewHolder(View v) {
            super(v);
            icon = (ImageView) v.findViewById(R.id.icon);
        }

        public void setClickListener(View.OnClickListener c) {
            icon.setOnClickListener(c);
        }
    }
}
