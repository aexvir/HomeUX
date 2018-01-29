package com.dravite.newlayouttest.top_fragments;

import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.AlarmClock;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.Toast;

import com.dravite.newlayouttest.Const;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.views.QuickAppBar;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.views.TextDate;
import com.dravite.newlayouttest.general_helpers.JsonHelper;

/**
 * This Fragment shows the clock, a date and the QuickActionBar.
 */
public class ClockFragment extends Fragment {

    public QuickAppBar mQuickAppBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_clock, null);
        view.setTag("clock");
        return view;
    }

    /**
     * Updates the clock in terms of font, size, etc.
     */
    void updateClock(){
        TextClock clock = (TextClock)getView().findViewById(R.id.textClock);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        Boolean clockBold = preferences.getBoolean(Const.Defaults.TAG_CLOCK_BOLD, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_BOLD));
        Boolean clockItalic = preferences.getBoolean(Const.Defaults.TAG_CLOCK_ITALIC, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_ITALIC));
        String clockSize = preferences.getString(Const.Defaults.TAG_CLOCK_SIZE, Const.Defaults.getString(Const.Defaults.TAG_CLOCK_SIZE));
        boolean showAmPm = preferences.getBoolean(Const.Defaults.TAG_AM_PM, Const.Defaults.getBoolean(Const.Defaults.TAG_AM_PM));

        int fontStyle;

        if(clockBold){
            if(clockItalic)
                fontStyle=Typeface.BOLD_ITALIC;
            else
                fontStyle=Typeface.BOLD;
        } else {
            if(clockItalic)
                fontStyle=Typeface.ITALIC;
            else
                fontStyle=Typeface.NORMAL;
        }

        if (clockSize.equals(getString(R.string.small))){
            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 35);
        } else if (clockSize.equals(getString(R.string.medium))){
            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 50);
        } else {
            clock.setTextSize(TypedValue.COMPLEX_UNIT_SP, 65);
        }

        if (!android.text.format.DateFormat.is24HourFormat(getContext()) && showAmPm) clock.setFormat12Hour("h:mm a");
        else clock.setFormat12Hour("hh:mm");

        clock.setTypeface(Typeface.create(preferences.getString(Const.Defaults.TAG_CLOCK_FONT, Const.Defaults.getString(Const.Defaults.TAG_CLOCK_FONT)), fontStyle));
    }

    @Override
    public void onResume() {
        super.onResume();
        if(getView()!=null){
            TextClock clock = (TextClock) getView().findViewById(R.id.textClock);
            TextDate date = (TextDate) getView().findViewById(R.id.dateView);

            if(PreferenceManager.getDefaultSharedPreferences(getContext()).getBoolean(Const.Defaults.TAG_CLOCK_SHOW, Const.Defaults.getBoolean(Const.Defaults.TAG_CLOCK_SHOW))) {
                //Update and resize date and clock if needed

                date.updateDate();
                RelativeLayout.LayoutParams dParams = ((RelativeLayout.LayoutParams) date.getLayoutParams());
                RelativeLayout.LayoutParams cParams = ((RelativeLayout.LayoutParams) clock.getLayoutParams());


                boolean showAmPm = PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Const.Defaults.TAG_AM_PM, Const.Defaults.getBoolean(Const.Defaults.TAG_AM_PM));
                if (!android.text.format.DateFormat.is24HourFormat(getContext()) && showAmPm)
                    clock.setFormat12Hour("h:mm a");
                else clock.setFormat12Hour("h:mm");

                if (PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Const.Defaults.TAG_CENTER_CLOCK, Const.Defaults.getBoolean(Const.Defaults.TAG_CENTER_CLOCK))) {
                    dParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                    cParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                } else {
                    dParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                    cParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
                }
                date.setLayoutParams(dParams);
                clock.setLayoutParams(cParams);
                clock.setVisibility(View.VISIBLE);
                date.setVisibility(View.VISIBLE);
            } else {
                clock.setVisibility(View.INVISIBLE);
                date.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mQuickAppBar = (QuickAppBar) view.findViewById(R.id.quick_action_bar);

        //Load QuickApps
        JsonHelper.inflateQuickApps(this.getContext(), mQuickAppBar);
        mQuickAppBar.setDragSurfaceLayout(((LauncherActivity) getActivity()).mDragView);

        ((LauncherActivity)getActivity()).mDragView.setQuickActionBar(mQuickAppBar);

        TextDate date = (TextDate)view.findViewById(R.id.dateView);

        TextClock clock = (TextClock)view.findViewById(R.id.textClock);

        RelativeLayout.LayoutParams dParams = ((RelativeLayout.LayoutParams) date.getLayoutParams());
        RelativeLayout.LayoutParams cParams = ((RelativeLayout.LayoutParams) clock.getLayoutParams());

        if(PreferenceManager.getDefaultSharedPreferences(getActivity()).getBoolean(Const.Defaults.TAG_CENTER_CLOCK, Const.Defaults.getBoolean(Const.Defaults.TAG_CENTER_CLOCK))) {
            dParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
            cParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
        } else {
            dParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
            cParams.addRule(RelativeLayout.ALIGN_PARENT_START, RelativeLayout.TRUE);
        }
        date.setLayoutParams(dParams);
        clock.setLayoutParams(cParams);
        clock.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent mClockIntent = new Intent(AlarmClock.ACTION_SHOW_ALARMS);
                mClockIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                LauncherUtils.startActivity((LauncherActivity)getActivity(), v, mClockIntent);
            }
        });
        updateClock();

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Start the calendar app
                long startMillis = System.currentTimeMillis();
                Uri.Builder builder = CalendarContract.CONTENT_URI.buildUpon();
                builder.appendPath("time");
                ContentUris.appendId(builder, startMillis);
                Intent intent = new Intent(Intent.ACTION_VIEW).setData(builder.build());
                try{
                    LauncherUtils.startActivity((LauncherActivity) getActivity(), v, intent);
                } catch (ActivityNotFoundException e){
                    Toast.makeText(getActivity(), R.string.calendar_error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
