package com.dravite.homeux.general_adapters;
import android.content.Context;
import android.content.res.ColorStateList;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.dravite.homeux.R;

/**
 * An Adapter used in a {@link com.dravite.homeux.general_dialogs.ColorDialog} to display preset color fields.
 */
public class ColorPresetAdapter extends RecyclerView.Adapter<ColorPresetAdapter.Holder> {

    /**
     * A simple ViewHolder
     */
    public static class Holder extends RecyclerView.ViewHolder{
        public Holder(View view){
            super(view);
            t = (LinearLayout)view.findViewById(R.id.color_field);
        }

        LinearLayout t;
    }

    /**
     * A listener for listening to a selection in this adapter.
     */
    public interface ColorListener{
        void onSelected(int color);
    }

    Context mContext;
    ColorListener mListener;

    public ColorPresetAdapter(Context context, ColorListener listener){
        this.mContext = context;
        this.mListener = listener;
    }

    // Our preset colors.
    int[] colors = {
            0xffF44336,
            0xffE91E63,
            0xff9C27B0,
            0xff673AB7,
            0xff3F51B5,
            0xff2196F3,
            0xff03A9F4,
            0xff00BCD4,
            0xff009688,
            0xff4CAF50,
            0xff8BC34A,
            0xffCDDC39,
            0xffFFEB3B,
            0xffFFC107,
            0xffFF9800,
            0xffFF5722,
            0xff795548,
            0xff9E9E9E,
            0xff607D8B,
    };

    @Override
    public Holder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = View.inflate(mContext, R.layout.color_preset_item, null);
        return new Holder(view);
    }

    @Override
    public void onBindViewHolder(Holder holder, final int i) {
        holder.t.setLayoutParams(new ViewGroup.LayoutParams(144, 144));
        holder.t.setBackgroundTintList(ColorStateList.valueOf(colors[i]));

        //Just run the listener.
        holder.t.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onSelected(colors[i]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return colors.length;
    }
}
