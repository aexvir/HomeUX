package com.dravite.newlayouttest.folder_editor;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.folder_editor.FolderAddAppsListAdapter;
import com.dravite.newlayouttest.folder_editor.FolderEditorActivity;

import java.util.ArrayList;

public class FolderEditorAddActivity extends AppCompatActivity {

/*    public static final int REQUEST_APP_LIST = 2104;*/
    public static final int REQUEST_APP_LIST_MAIN = 2105;

    private boolean showSave = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_editor_add);

        Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null)
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //receiving extras
        Intent intent = getIntent();
        final ArrayList<ComponentName> appList = FolderEditorActivity.AppListPasser.receiveAppList();
        final ArrayList<ComponentName> containsList = FolderEditorActivity.AppListPasser.receiveContainedList();

        toolbar.setBackgroundColor(0xff2196F3);
        getWindow().setStatusBarColor(0xff1976D2);

        final RecyclerView appGrid = (RecyclerView)findViewById(R.id.appGrid);
        appGrid.setLayoutManager(new GridLayoutManager(this,4));
        appGrid.setAdapter(new FolderAddAppsListAdapter(this, appList, containsList));
        showSave = intent.getBooleanExtra("showSave", false);
        Button saveButton = (Button)findViewById(R.id.save);
        saveButton.setVisibility(showSave?View.VISIBLE:View.GONE);

        CheckBox checkBox = (CheckBox)findViewById(R.id.hideAppsInAnotherFolder);
        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((FolderAddAppsListAdapter) appGrid.getAdapter()).setShowInOthers(!isChecked);
            }
        });

        final Switch hideBoxSwitch = (Switch)findViewById(R.id.pswitch);
        hideBoxSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((FolderAddAppsListAdapter) appGrid.getAdapter()).setShowInOthers(!isChecked);
            }
        });

        View hideBox = findViewById(R.id.hide_option);
        hideBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideBoxSwitch.setChecked(!hideBoxSwitch.isChecked());
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FolderEditorActivity.AppListPasser.passAppList(((FolderAddAppsListAdapter) appGrid.getAdapter()).mSelectedAppsList);
                FolderEditorActivity.AppListPasser.passAlreadyContainedList(((FolderAddAppsListAdapter) appGrid.getAdapter()).mContainsList);

                Intent data = new Intent();
                if (getParent() == null) {
                    setResult(Activity.RESULT_OK, data);
                }
                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        backPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                    backPressed();
                    return (true);
        }

        return(super.onOptionsItemSelected(item));
    }

    /**
     * A helper method for passing the appLists.
     */
    void backPressed(){
        if(showSave){
            Intent data = new Intent();
            setResult(Activity.RESULT_CANCELED, data);
            super.onBackPressed();
        } else {
            final RecyclerView appGrid = (RecyclerView) findViewById(R.id.appGrid);
            FolderEditorActivity.AppListPasser.passAppList(((FolderAddAppsListAdapter) appGrid.getAdapter()).mSelectedAppsList);
            FolderEditorActivity.AppListPasser.passAlreadyContainedList(((FolderAddAppsListAdapter) appGrid.getAdapter()).mContainsList);

            Intent data = new Intent();
            if (getParent() == null) {
                setResult(Activity.RESULT_OK, data);
            }
            super.onBackPressed();
        }
    }
}
