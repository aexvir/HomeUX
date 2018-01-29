package com.dravite.homeux.folder_editor;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.general_adapters.AllDrawableAdapter;
import com.dravite.homeux.views.viewcomponents.IconListDecoration;

/**
 * Created by Johannes on 13.10.2015.
 * Nearly exactly like {@link com.dravite.homeux.add_quick_action.AddQuickActionActivity}, just with a different description text.
 */
public class SelectFolderIconActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_folder_icon);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView list = (RecyclerView)findViewById(R.id.icon_list);
        list.setLayoutManager(new GridLayoutManager(this, 6));
        final int space = LauncherUtils.dpToPx(8, this);
        list.addItemDecoration(new IconListDecoration(space));
        list.setAdapter(new AllDrawableAdapter(this, list, new AllDrawableAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Drawable drawable, int index, int res) {
                returnData(res);
            }
        }));
    }

    /**
     * @param iconRes The resource ID for the selected icon to be returned to the requesting Activity
     */
    private void returnData(int iconRes){
        String resName = getResources().getResourceName(iconRes);
        Intent result = new Intent();
        result.putExtra("iconRes", resName);

        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
