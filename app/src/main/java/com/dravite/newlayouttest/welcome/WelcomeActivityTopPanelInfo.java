package com.dravite.newlayouttest.welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dravite.newlayouttest.Const;
import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.R;

/**
 * Created by johannesbraun on 20.11.15.
 * An info panel about the top panel in HomeUX
 */
public class WelcomeActivityTopPanelInfo extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_top_panel_info);

        getWindow().setNavigationBarColor(0xff7B1FA2);
        getWindow().setStatusBarColor(0xff7B1FA2);

        Button finishButton = (Button)findViewById(R.id.next);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //LauncherActivity.updatePagesOnResume=true;
                Intent mainIntent = new Intent(WelcomeActivityTopPanelInfo.this, LauncherActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(WelcomeActivityTopPanelInfo.this);

                SharedPreferences.Editor editor = preferences.edit();
                editor.putBoolean(Const.Defaults.TAG_FIRST_START, false);
                editor.apply();
            }
        });
    }
}
