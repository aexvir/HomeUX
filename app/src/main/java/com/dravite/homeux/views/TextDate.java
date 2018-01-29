package com.dravite.homeux.views;

import android.content.Context;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Johannes on 20.10.2015.
 * This View works like a {@link android.widget.TextClock} but displays a date.
 */
public class TextDate extends TextView {

    private SimpleDateFormat mDateFormat;

    public TextDate(Context context) {
        this(context, null);
    }

    public TextDate(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, android.R.attr.textViewStyle);
    }

    public TextDate(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    @SuppressWarnings("deprecation")
    public TextDate(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        updateDate();
    }

    /**
     * Call this to update the date to the current date. (localized)
     */
    public void updateDate(){
        Date date = new Date(System.currentTimeMillis());
        String year = (new SimpleDateFormat("yyy", Locale.getDefault())).format(date);
        mDateFormat = (SimpleDateFormat)DateFormat.getDateInstance(DateFormat.FULL, Locale.getDefault());
        String text = mDateFormat.format(date);


        text = text.replace(year, "");
        StringBuilder builder = new StringBuilder(text);
        while (builder.toString().endsWith(" ") || builder.toString().endsWith(",") || builder.toString().endsWith(".")){
            builder.replace(builder.length()-1, builder.length(), "");
        }
        setText(builder.toString());
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
        super.onRestoreInstanceState(state);
        updateDate();
    }
}
