package com.dravite.newlayouttest.settings.clock;

import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextClock;

import com.dravite.newlayouttest.Const;
import com.dravite.newlayouttest.LauncherLog;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.views.TextDate;
import com.dravite.newlayouttest.views.ToggleImageButton;
import com.dravite.newlayouttest.general_adapters.FontAdapter;

import java.util.Arrays;

/**
 * This activity shows options to edit how the clock looks and is layouted.
 */
public class ActivityClockSettings extends AppCompatActivity {

    private SharedPreferences mPreferences;
    private TextClock mClockView;
    private TextDate mDateView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        setContentView(R.layout.activity_clock_settings);
        getWindow().setStatusBarColor(0xff1976D2);

        mClockView = (TextClock) findViewById(R.id.textClock);
        mDateView = (TextDate) findViewById(R.id.dateView);
        mDateView.updateDate();

        initSpinners();
        initAlignmentToggles();
        initFontStyleToggles();
        initAMPMToggle();
        initVisibilitySwitch();

        ImageButton upArrow = (ImageButton) findViewById(R.id.backArrow);
        upArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    /**
     * Initializes all spinner views.
     */
    void initSpinners() {
        //Initialize Font Spinner
        Spinner fontSpinner = (Spinner) findViewById(R.id.spinner);
        fontSpinner.setAdapter(new FontAdapter(this));
        int index = Arrays.asList(FontAdapter.fonts).indexOf(mPreferences.getString(Const.Defaults.TAG_CLOCK_FONT, Const.Defaults.getString(Const.Defaults.TAG_CLOCK_FONT)));
        fontSpinner.setSelection(index);
        fontSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                editor().putString(Const.Defaults.TAG_CLOCK_FONT, FontAdapter.fonts[position]).apply();
                updateFont();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //Initialize Size Spinner
        Spinner sizeSpinner = (Spinner) findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.sizes, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sizeSpinner.setAdapter(adapter);
        index = mPreferences.getInt(Const.Defaults.TAG_CLOCK_SIZE_INT, Const.Defaults.getInt(Const.Defaults.TAG_CLOCK_SIZE_INT));
        sizeSpinner.setSelection(index);
        sizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                LauncherLog.d(getClass().getName(), "Position: " + position);
                editor().putString(Const.Defaults.TAG_CLOCK_SIZE, getResources().getStringArray(R.array.sizes)[position])
                        .putInt(Const.Defaults.TAG_CLOCK_SIZE_INT, position).apply();
                updateSize();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //getResources().getStringArray(R.array.sizes)

    }

    /**
     * Initializes the toggle buttons for aligning the clock text left or center.
     */
    void initAlignmentToggles() {
        final ToggleImageButton centeredButton = (ToggleImageButton) findViewById(R.id.btn_align_center);
        final ToggleImageButton leftButton = (ToggleImageButton) findViewById(R.id.btn_align_left);

        boolean center = mPreferences.getBoolean(Const.Defaults.TAG_CENTER_CLOCK, Const.Defaults.getBoolean(Const.Defaults.TAG_CENTER_CLOCK));
        centeredButton.setSelected(center);
        leftButton.setSelected(!center);
        updateAlignment();

        centeredButton.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
                leftButton.setSelected(false);
                editor().putBoolean(Const.Defaults.TAG_CENTER_CLOCK, true).apply();
                updateAlignment();
            }
        });
        leftButton.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
                centeredButton.setSelected(false);
                editor().putBoolean(Const.Defaults.TAG_CENTER_CLOCK, false).apply();
                updateAlignment();
            }
        });
    }

    /**
     * Initializes the toggle buttons for editing the clock font style (bold or italic)
     */
    void initFontStyleToggles() {
        ToggleImageButton boldBtn = (ToggleImageButton) findViewById(R.id.btn_toggle_bold);
        ToggleImageButton italicBtn = (ToggleImageButton) findViewById(R.id.btn_toggle_italic);

        boldBtn.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
                editor().putBoolean(Const.Defaults.TAG_CLOCK_BOLD, isChecked).apply();
                updateBold();
            }
        });
        italicBtn.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
                editor().putBoolean(Const.Defaults.TAG_CLOCK_ITALIC, isChecked).apply();
                ;
                updateItalic();
            }
        });

        boldBtn.setChecked(mPreferences.getBoolean(Const.Defaults.TAG_CLOCK_BOLD, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_BOLD)));
        italicBtn.setChecked(mPreferences.getBoolean(Const.Defaults.TAG_CLOCK_ITALIC, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_ITALIC)));
    }

    /**
     * Initializes the AM/PM switch.
     */
    void initAMPMToggle() {
        final ToggleImageButton amPmButton = (ToggleImageButton) findViewById(R.id.btn_am_pm);
        amPmButton.setEnabled(!android.text.format.DateFormat.is24HourFormat(this));

        if (amPmButton.isEnabled()) {
            amPmButton.setOnCheckedChangeListener(new ToggleImageButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(ToggleImageButton buttonView, boolean isChecked) {
                    editor().putBoolean(Const.Defaults.TAG_AM_PM, isChecked).apply();
                    updateAMPM();
                }
            });
            amPmButton.setChecked(mPreferences.getBoolean(Const.Defaults.TAG_AM_PM, Const.Defaults.getBoolean(Const.Defaults.TAG_AM_PM)));
        }
    }

    /**
     * Initializes the general switch that switches on and off the clock.
     */
    void initVisibilitySwitch() {
        Switch visibilitySwitch = (Switch) findViewById(R.id.clockSwitch);
        visibilitySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Log.d(getClass().getName(), "Toggled Visibility");
                editor().putBoolean(Const.Defaults.TAG_CLOCK_SHOW, isChecked).apply();
                updateVisibility();
            }
        });
        visibilitySwitch.setChecked(mPreferences.getBoolean(Const.Defaults.TAG_CLOCK_SHOW, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_SHOW)));
    }

    /**
     * Just a helper method.
     *
     * @return The Editor for this Activitie's SharedPreferences.
     */
    SharedPreferences.Editor editor() {
        return mPreferences.edit();
    }

    /**
     * See also: {@link ActivityClockSettings#updateStyle()}
     */
    void updateFont() {
        updateStyle();
    }

    /**
     * Updates the clock font size.
     */
    void updateSize() {
        String clockSize = mPreferences.getString(Const.Defaults.TAG_CLOCK_SIZE, Const.Defaults.getString(Const.Defaults.TAG_CLOCK_SIZE));
        if (clockSize.equals(getString(R.string.small))) {
            mClockView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
        } else if (clockSize.equals(getString(R.string.medium))) {
            mClockView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        } else {
            mClockView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 65);
        }
    }

    /**
     * Updates the clock text alignment.
     */
    void updateAlignment() {
        RelativeLayout.LayoutParams dParams = ((RelativeLayout.LayoutParams) mDateView.getLayoutParams());
        RelativeLayout.LayoutParams cParams = ((RelativeLayout.LayoutParams) mClockView.getLayoutParams());

        if (mPreferences.getBoolean(Const.Defaults.TAG_CENTER_CLOCK, Const.Defaults.getBoolean(Const.Defaults.TAG_CENTER_CLOCK))) {
            LauncherLog.d(getClass().getName(), "Align in center");
            dParams.removeRule(RelativeLayout.ALIGN_PARENT_START);
            cParams.removeRule(RelativeLayout.ALIGN_PARENT_START);
            dParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            cParams.removeRule(RelativeLayout.ALIGN_PARENT_LEFT);
            dParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            cParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        } else {
            LauncherLog.d(getClass().getName(), "Align left");
            dParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            cParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        }

        mDateView.setLayoutParams(dParams);
        mClockView.setLayoutParams(cParams);
    }

    /**
     * Updates the clock font style (bold or italic or both)
     */
    void updateStyle() {
        Boolean clockBold = mPreferences.getBoolean(Const.Defaults.TAG_CLOCK_BOLD, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_BOLD));
        Boolean clockItalic = mPreferences.getBoolean(Const.Defaults.TAG_CLOCK_ITALIC, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_ITALIC));

        int fontStyle;

        if (clockBold) {
            if (clockItalic)
                fontStyle = Typeface.BOLD_ITALIC;
            else
                fontStyle = Typeface.BOLD;
        } else {
            if (clockItalic)
                fontStyle = Typeface.ITALIC;
            else
                fontStyle = Typeface.NORMAL;
        }
        mClockView.setTypeface(Typeface.create(mPreferences.getString(Const.Defaults.TAG_CLOCK_FONT, Const.Defaults.getString(Const.Defaults.TAG_CLOCK_FONT)), fontStyle));
    }

    /**
     * See also: {@link ActivityClockSettings#updateStyle()}
     */
    void updateBold() {
        updateStyle();
    }

    /**
     * See also: {@link ActivityClockSettings#updateStyle()}
     */
    void updateItalic() {
        updateStyle();
    }

    /**
     * Updates the AM/PM toggle and AM/PM text visibility.
     */
    void updateAMPM() {
        boolean showAmPm = mPreferences.getBoolean(Const.Defaults.TAG_AM_PM, Const.Defaults.getBoolean(Const.Defaults.TAG_AM_PM));
        if (!android.text.format.DateFormat.is24HourFormat(this) && showAmPm)
            mClockView.setFormat12Hour("h:mm a");
        else mClockView.setFormat12Hour("h:mm");
    }

    /**
     * Updates the whole clock visibility.
     */
    void updateVisibility() {
        boolean enabled = mPreferences.getBoolean(Const.Defaults.TAG_CLOCK_SHOW, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_SHOW));
        int visibility = enabled ? View.VISIBLE : View.INVISIBLE;

        mClockView.setVisibility(visibility);
        mDateView.setVisibility(visibility);

        findViewById(R.id.spinner).setEnabled(enabled);
        findViewById(R.id.spinner2).setEnabled(enabled);
        findViewById(R.id.btn_align_left).setEnabled(enabled);
        findViewById(R.id.btn_align_center).setEnabled(enabled);
        findViewById(R.id.btn_toggle_bold).setEnabled(enabled);
        findViewById(R.id.btn_toggle_italic).setEnabled(enabled);
        findViewById(R.id.btn_am_pm).setEnabled(!android.text.format.DateFormat.is24HourFormat(this) && enabled);
    }
}
