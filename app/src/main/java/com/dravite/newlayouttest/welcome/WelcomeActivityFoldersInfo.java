package com.dravite.newlayouttest.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dravite.newlayouttest.R;

/**
 * Created by johannesbraun on 20.11.15. <br/>
 * An info panel with a description about folders in HomeUX
 */
public class WelcomeActivityFoldersInfo extends AppCompatActivity{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_folders_info);

        getWindow().setNavigationBarColor(0xff1976d2);
        getWindow().setStatusBarColor(0xff1976d2);

        Button finishButton = (Button)findViewById(R.id.next);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(WelcomeActivityFoldersInfo.this, WelcomeActivityFolders.class);
                startActivityForResult(mainIntent, WelcomeActivity.REQUEST_FIRST_START);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
            }
        });
    }
}
