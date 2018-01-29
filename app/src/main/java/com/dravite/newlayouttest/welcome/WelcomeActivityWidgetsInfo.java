package com.dravite.newlayouttest.welcome;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dravite.newlayouttest.R;

/**
 * Created by johannesbraun on 20.11.15.
 * An info panel about Widgets and so on in HomeUX
 */
public class WelcomeActivityWidgetsInfo extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_widgets_info);

        getWindow().setNavigationBarColor(0xff388E3C);
        getWindow().setStatusBarColor(0xff388E3C);

        Button finishButton = (Button)findViewById(R.id.next);
        finishButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainIntent = new Intent(WelcomeActivityWidgetsInfo.this, WelcomeActivityQuickApps.class);
                startActivity(mainIntent);

                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);

            }
        });
    }
}
