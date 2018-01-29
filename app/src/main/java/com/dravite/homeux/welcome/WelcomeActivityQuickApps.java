package com.dravite.homeux.welcome;

import android.app.Activity;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.QuickAction;
import com.dravite.homeux.folder_editor.SelectFolderIconActivity;
import com.dravite.homeux.general_helpers.JsonHelper;
import com.dravite.homeux.general_adapters.AppSelectAdapter;

import java.util.ArrayList;

/**
 * Created by johannesbraun on 19.11.15.
 * Here the user can find some settings for QuickApps, being able to set their own in a descriptive way.<br/>
 * There are 5 "+" buttons shown to add QuickApps.
 */
public class WelcomeActivityQuickApps extends AppCompatActivity implements View.OnClickListener {

    public static final int REQUEST_CHANGE_QA_ICON = 932;
    private int mSelectedButton = -1;
    ArrayList<QuickAction> quickActions = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_qa);

        getWindow().setNavigationBarColor(0xffFFA000);
        getWindow().setStatusBarColor(0xffFFA000);

        LinearLayout quickAppLayout = (LinearLayout)findViewById(R.id.quickAppLayout);
        for(int i=0; i<quickAppLayout.getChildCount(); i++){
            quickAppLayout.getChildAt(i).setOnClickListener(this);
        }

        //Load current QuickApps (for the case of pre-defined ones or when re-doing the setup)
        ArrayList tmp = JsonHelper.loadQuickApps(this);
        quickActions.clear();
        for (Object action:tmp
             ) {
            QuickAction a = (QuickAction)action;
            quickActions.add(a);
            ((ImageButton) quickAppLayout.getChildAt(a.qaIndex)).setImageResource(getResources().getIdentifier(a.iconRes, "drawable", getApplicationContext().getPackageName()));
        }

        for (int i=0; i<quickAppLayout.getChildCount(); i++){
            ImageButton img = (ImageButton)quickAppLayout.getChildAt(i);
            if(img.getDrawable()==null){
                img.setImageResource(R.drawable.ic_add_black_24dp);
            }
        }

        //Next screen
        Button nextButton = (Button)findViewById(R.id.next);
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivityQuickApps.this, WelcomeActivityFoldersInfo.class);

                startActivityForResult(intent, WelcomeActivity.REQUEST_FIRST_START);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }

    @Override
    public void onClick(final View v) {
        //Clicking on a button

        mSelectedButton = ((LinearLayout) findViewById(R.id.quickAppLayout)).indexOfChild(v);
        final AppSelectAdapter appSelectAdapter = new AppSelectAdapter(WelcomeActivityQuickApps.this);
        new android.support.v7.app.AlertDialog.Builder(WelcomeActivityQuickApps.this, R.style.DialogTheme).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setTitle("Select an App")
                .setAdapter(appSelectAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ComponentName name = appSelectAdapter.mInfos.get(which).getComponentName();
                        QuickAction action = new QuickAction("", name.getPackageName(), name.getClassName(), mSelectedButton);
                        boolean hasSet = false;
                        for (int i = 0; i < quickActions.size(); i++) {
                            if (quickActions.get(i).qaIndex == mSelectedButton) {
                                quickActions.set(i, action);
                                hasSet = true;
                            }
                        }
                        if (!hasSet) {
                            quickActions.add(action);
                        }
                        Intent intent = new Intent(WelcomeActivityQuickApps.this, SelectFolderIconActivity.class);
                        LauncherUtils.startActivityForResult(WelcomeActivityQuickApps.this, v, intent, REQUEST_CHANGE_QA_ICON);
                    }
                }).show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode== Activity.RESULT_OK){
            switch (requestCode){
                case REQUEST_CHANGE_QA_ICON:
                    String resName = data.getStringExtra("iconRes");
                    int resID = getResources().getIdentifier(resName, "drawable", getApplicationContext().getPackageName());
                    ((ImageButton) ((LinearLayout) findViewById(R.id.quickAppLayout)).getChildAt(mSelectedButton)).setImageResource(resID);

                    for (int i=0; i<quickActions.size(); i++){
                        if(quickActions.get(i).qaIndex==mSelectedButton){
                            quickActions.get(i).iconRes=resName;
                        }
                    }
                    JsonHelper.saveQuickApps(this, quickActions);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
