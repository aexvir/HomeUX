package com.dravite.newlayouttest.general_adapters;

import android.content.Context;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dravite.newlayouttest.drawerobjects.structures.FolderStructure;
import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.R;

/**
 * Created by johannesbraun on 24.03.16.
 */
public class FolderDropAdapter extends RecyclerView.Adapter<FolderDropAdapter.FolderDropViewHolder> {

    public static class FolderDropViewHolder extends RecyclerView.ViewHolder{
        public FolderDropViewHolder(View view){
            super(view);
            itemTextView = (TextView)view;
        }

        TextView itemTextView;
    }

    private Context mContext;
    private int hovered;
    private Rect mParentRect = new Rect();

    private int mAllFolderIndex = 0;

    public FolderDropAdapter(Context context){
        mContext = context;
        mAllFolderIndex = LauncherActivity.mFolderStructure.getFolderIndexOfName("All");
    }

    @Override
    public FolderDropViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new FolderDropViewHolder(LayoutInflater.from(mContext).inflate(R.layout.folder_drop_icon, parent, false));
    }

    @Override
    public void onBindViewHolder(final FolderDropViewHolder holder, final int position) {

        final FolderStructure.Folder folder = LauncherActivity.mFolderStructure.folders.get(position<mAllFolderIndex?position:position+1);

        if(position==hovered){
            holder.itemTextView.setBackgroundColor(0x332196F3);
        } else {
            holder.itemTextView.setBackgroundColor(0x00ffffff);
        }

        holder.itemTextView.setText(folder.folderName);

   /*     LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
            @Override
            public void run() {*/
                final Drawable icon = mContext.getDrawable(mContext.getResources().getIdentifier(folder.folderIconRes, "drawable", mContext.getPackageName()));
                if(icon!=null)
                    icon.setTint(0x9f000000);
                if(position == holder.getAdapterPosition()){
                    holder.itemTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                    /*((Activity)mContext).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            holder.itemTextView.setCompoundDrawablesRelativeWithIntrinsicBounds(icon, null, null, null);
                        }
                    });*/
                }
 /*           }
        });*/
    }

    public int getHovered(){
        return hovered<mAllFolderIndex?hovered:hovered+1;
    }

    public void resetHovered(){
        hovered = -1;
    }

    public void hover(RecyclerView parent, float x, float y){
        parent.getGlobalVisibleRect(mParentRect);

        int newHover = parent.getChildAdapterPosition(parent.findChildViewUnder(x-mParentRect.left, y-mParentRect.top));

        if(newHover!=hovered){
            hovered = newHover;
            notifyDataSetChanged();
        }
    }

    @Override
    public int getItemCount() {
        return LauncherActivity.mFolderStructure.folders.size()-1;
    }
}
