package com.dravite.homeux.iconpacks;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.dravite.homeux.R;
import com.dravite.homeux.general_helpers.IconPackManager;

import java.util.List;

/**
 * Created by Johannes on 22.09.2015.
 * Lists all available Icon Packs in a List.
 */
public class ThemeAdapter extends ArrayAdapter<IconPackManager.Theme>{

    static class ThemeHolder{
        TextView itemView;
    }

    public ThemeAdapter(Context context, List<IconPackManager.Theme> items) {
        super(context, 0, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ThemeHolder holder;

        //ViewHolder Pattern
        if(convertView==null){
            //Create new one.
            convertView = View.inflate(getContext(), R.layout.folder_drop_icon, null);
            holder = new ThemeHolder();
            holder.itemView=(TextView)convertView;

            convertView.setTag(holder);
        } else {
            //Re-use it.
            holder = ((ThemeHolder) convertView.getTag());
        }

        IconPackManager.Theme theme = getItem(position);

        holder.itemView.setText(theme.label);

        holder.itemView.setCompoundDrawablesRelative(theme.icon, null, null, null);

        return convertView;
    }
}
