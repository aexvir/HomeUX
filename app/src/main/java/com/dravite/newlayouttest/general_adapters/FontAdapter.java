package com.dravite.newlayouttest.general_adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * An adapter to select a font, even showing how the font looks like.
 */
public class FontAdapter extends BaseAdapter {

    //A list of available fonts
    public static String[] fonts = {
            "sans-serif-thin",
            "sans-serif-light",
            "sans-serif-condensed",
            "sans-serif",
            "sans-serif-medium",
            "sans-serif-black",
    };

    private Context mContext;

    public FontAdapter(Context context){
        this.mContext = context;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public int getCount() {
        return fonts.length;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = View.inflate(mContext, android.R.layout.simple_list_item_1, null);
        TextView text = (TextView)view.findViewById(android.R.id.text1);

        text.setText(fonts[position]);
        text.setTypeface(Typeface.create(fonts[position], Typeface.NORMAL));

        return view;
    }

    @Override
    public Object getItem(int position) {
        return fonts[position];
    }
}
