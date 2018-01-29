package com.dravite.homeux.add_quick_action;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;

import com.dravite.homeux.general_adapters.AllDrawableAdapter;
import com.dravite.homeux.views.viewcomponents.IconListDecoration;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;

/**
 * This Activity shows a list of icons to select from for adding or editing a QuickAction.
 */
public class AddQuickActionActivity extends AppCompatActivity {

    private int mQaIndex;
    private Application mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_qa);

        mQaIndex = getIntent().getIntExtra("index", 0);
        mData = getIntent().getParcelableExtra("data");

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        RecyclerView list = (RecyclerView)findViewById(R.id.icon_list);
        list.setLayoutManager(new GridLayoutManager(this, 6));
        final int space = LauncherUtils.dpToPx(8, this);
        //Set a little padding around the items.
        list.addItemDecoration(new IconListDecoration(space));
        list.setAdapter(new AllDrawableAdapter(this, list, new AllDrawableAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Drawable drawable, int index, int res) {
                returnData(mData, mQaIndex, res);
            }
        }));
    }

    /**
     * Returns The given QuickAction data with {@link Activity#RESULT_OK} as result.
     * @param data The application that should be added as QuickAction
     * @param index The QA index in the QuickActionBar
     * @param iconRes The selected icon drawable resource ID
     */
    private void returnData(Application data, int index, int iconRes){
        Intent result = new Intent();
        result.putExtra("app", (Parcelable)data);
        result.putExtra("index", index);
        result.putExtra("icon", iconRes);

        setResult(Activity.RESULT_OK, result);
        finish();
    }
}
