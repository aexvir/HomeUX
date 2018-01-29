package com.dravite.newlayouttest.general_adapters;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.annotation.DrawableRes;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.general_helpers.ExceptionLog;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * This adapter holds all icons whose id identifier starts on "ic_".
 */
public class AllDrawableAdapter extends RecyclerView.Adapter<AllDrawableAdapter.DrawableViewHolder> {

    /**
     * Just a simple ViewHolder
     */
    class DrawableViewHolder extends RecyclerView.ViewHolder{
        Drawable icon;

        public DrawableViewHolder(View view){
            super(view);
        }

        void loadImage(@DrawableRes final int resID, int row){
//                    icon = mContext.getDrawable(resID);
//                    icon.setAlpha(255);
//                    icon.setTint(0xff000000);
                    ((ImageView) itemView).setImageResource(resID);
                    ((ImageView) itemView).setAlpha(0.57f);
                    ((ImageView) itemView).setClickable(true);
        }
    }

    /**
     * An extended OnClickListener passing the Drawable, the drawable resource ID and the clicked index
     */
    public interface OnItemClickListener{
        void onItemClick(Drawable drawable, int index, int res);
    }

    //A list of resource IDs to not reload the whole list every time.
    private List<Integer> mResList = new ArrayList<>();
    private Context mContext;
    private OnItemClickListener mOnItemClickListener;
    private RecyclerView mList;

    private boolean hasLoaded = false;

    public AllDrawableAdapter(Context context, RecyclerView list, OnItemClickListener itemClickListener){
        mContext = context;
        mList = list;

        //Get all fields from the R.drawable class
        Field[] ID_Fields = R.drawable.class.getFields();

        //Go through all fields
        for(Field field : ID_Fields) {
            try {
                //add field value to the res ID list if its name starts with ic_ and doesn't contain aaaa and zzzz (earlier placeholders)
                if(field.getName().startsWith("ic_")) {
                    Log.e("call","Add mRest list");
                    mResList.add(field.getInt(null));
                }
            } catch (IllegalArgumentException|IllegalAccessException e) {
                ExceptionLog.w(e);
            }
        }
        hasLoaded = true;
        this.mOnItemClickListener = itemClickListener;
    }

    @Override
    public int getItemCount() {
        return hasLoaded?mResList.size():0;
    }

    @Override
    public void onBindViewHolder(final DrawableViewHolder holder, final int position) {
        final ImageButton v = (ImageButton)holder.itemView;

        holder.loadImage(mResList.get(position), position/6);

        //Set OnClickListener
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mOnItemClickListener!=null)
                    mOnItemClickListener.onItemClick(mContext.getDrawable(mResList.get(holder.getAdapterPosition())), holder.getAdapterPosition(), mResList.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public DrawableViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new DrawableViewHolder(View.inflate(mContext, R.layout.icon_item, null));
    }
}
