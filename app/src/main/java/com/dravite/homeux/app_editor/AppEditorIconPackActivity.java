package com.dravite.homeux.app_editor;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dravite.homeux.general_helpers.IconPackManager;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;

import java.util.List;

/**
 * An Activity that shows all installed icon packs for the user to select from where he would like to get an icon for the edited app from.
 */
public class AppEditorIconPackActivity extends AppCompatActivity {

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_editor_icon_pack);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView iconPackList = (RecyclerView)findViewById(R.id.icon_pack_list);
        iconPackList.setLayoutManager(new GridLayoutManager(this, 1));
        iconPackList.setAdapter(new IconPackAdapter(this, IconPackManager.getAllThemes(this, false)));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode==RESULT_OK){
            if(requestCode == AppEditorActivity.REQUEST_PASS_ICON){
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, data);
                }
                //Just pass the icon through to the AppEditorActivity
                finish();
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * An adapter that holds all icon packs
     */
    public static class IconPackAdapter extends RecyclerView.Adapter<IconPackAdapter.IconPackViewHolder>{
        public static class IconPackViewHolder extends RecyclerView.ViewHolder{
            public IconPackViewHolder(View view){
                super(view);
                text = ((TextView) itemView);
            }

            public TextView text;
        }

        private Context mContext;
        private List<IconPackManager.Theme> mIconPacks;

        public IconPackAdapter(Context context, List<IconPackManager.Theme> iconPacks){
            mContext = context;
            mIconPacks = iconPacks;
        }

        @Override
        public void onBindViewHolder(final IconPackViewHolder holder, int position) {
            final IconPackManager.Theme pack = mIconPacks.get(position);
            holder.text.setText(pack.label);

            //Limit icon size to 40dp
            int iconSize = LauncherUtils.dpToPx(40, holder.itemView.getContext());

            pack.icon.setBounds(0, 0, iconSize, iconSize);
            holder.text.setCompoundDrawablesRelative(pack.icon, null, null, null);


            holder.text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Start icon select activity with colors and a given icon pack
                    Intent intent = new Intent(mContext, AppEditorIconSelectActivity.class);
                    intent.putExtra("iconPack", pack.packageName);

                    ((AppEditorIconPackActivity)mContext).startActivityForResult(intent, AppEditorActivity.REQUEST_PASS_ICON);
                }
            });
        }

        @Override
        public IconPackViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new IconPackViewHolder(View.inflate(mContext, R.layout.folder_drop_icon, null));
        }

        @Override
        public int getItemCount() {
            return mIconPacks.size();
        }
    }
}
