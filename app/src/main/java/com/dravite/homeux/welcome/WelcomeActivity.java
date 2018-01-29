package com.dravite.homeux.welcome;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.R;

/**
 * Created by johannesbraun on 19.11.15.<br/>
 * First activity to be shown on first start. Just working like an info-screen.<br/>
 * From here, the user can decide whether he wants to do the setup or just skip it.
 */
public class WelcomeActivity extends AppCompatActivity {

    public static final int REQUEST_FIRST_START = 463;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        getWindow().setNavigationBarColor(0xff00796B);
        getWindow().setStatusBarColor(0xff00796B);

        //SKIP
        Button skip = (Button) findViewById(R.id.skip);
        skip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(WelcomeActivity.this, LauncherActivity.class);
                mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(mainIntent);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
                SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(WelcomeActivity.this).edit();
                editor.putBoolean(Const.Defaults.TAG_FIRST_START, false);
                editor.apply();
            }
        });

        //Do Setup
        Button floatingActionButton = (Button)findViewById(R.id.start);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, WelcomeActivityWidgetsInfo.class);
                startActivityForResult(intent, REQUEST_FIRST_START);

                overridePendingTransition(R.anim.slide_in_right,R.anim.slide_out_left);

            }
        });
    }
}
