package com.dravite.newlayouttest.general_dialogs;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;

import com.dravite.newlayouttest.general_adapters.ColorPresetAdapter;
import com.dravite.newlayouttest.general_dialogs.helpers.ColorWatcher;
import com.dravite.newlayouttest.general_helpers.ColorUtils;
import com.dravite.newlayouttest.R;

/**
 * A Dialog that shows a color selector with HSL color format sliders.
 */
public class ColorDialog {
    private Context mContext;
    private Dialog mDialog;
    private View mContent;
    private String mTitle;
    private int mColor;

    public ColorDialog(Context context, String title, int startColor, final ColorWatcher watcher){
        this.mContext = context;
        mDialog = new Dialog(context, R.style.DialogTheme);
        mContent = View.inflate(context, R.layout.empty_editor, null);
        mTitle = title;
        mColor = startColor;

        mDialog.setContentView(mContent);
        Button okay = (Button)mContent.findViewById(R.id.buttonOk);
        okay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                watcher.onColorSubmitted(mColor);
                mDialog.dismiss();
            }
        });

        Button cancel = (Button)mContent.findViewById(R.id.buttonCancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.dismiss();
            }
        });
        initColorDialog();
    }

    /**
     * Initializes all views from this dialog and what should happen when sliding sliders or selecting a preset.
     */
    void initColorDialog(){
        ((TextView) mDialog.findViewById(R.id.folderName)).setText(mTitle);
        ((TextView) mDialog.findViewById(R.id.folderName)).setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);

        final View colorChooser = View.inflate(mContext, R.layout.color_chooser_layout, null);
        colorChooser.setTag("colorLayout");
        colorChooser.setAlpha(0);
        ((ViewGroup) mContent.findViewById(R.id.content)).addView(colorChooser);

        //Init Seekbars
        final SeekBar hue = (SeekBar) colorChooser.findViewById(R.id.hue);
        final SeekBar saturation = (SeekBar) colorChooser.findViewById(R.id.saturation);
        final SeekBar value = (SeekBar) colorChooser.findViewById(R.id.value);


        float[] hsl = ColorUtils.colorToHSL(mColor);
        hue.setProgress((int) hsl[0]);
        saturation.setProgress((int) (1000f * hsl[1]));
        value.setProgress((int) (1000f * hsl[2]));

        updateColor(colorChooser, mColor);

        SeekBar.OnSeekBarChangeListener listener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser) {
                    float[] hsl = new float[3];

                    hsl[0] = hue.getProgress();
                    hsl[1] = saturation.getProgress() / 1000f;
                    hsl[2] = value.getProgress() / 1000f;

                    int color = ColorUtils.HSLtoColor(hsl);

                    updateColor(colorChooser, color);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        };

        hue.setOnSeekBarChangeListener(listener);
        saturation.setOnSeekBarChangeListener(listener);
        value.setOnSeekBarChangeListener(listener);

        final EditText hexValue = (EditText) colorChooser.findViewById(R.id.hexValue);
        final ImageButton button = (ImageButton) colorChooser.findViewById(R.id.submitButton);
        button.setImageTintList(ColorStateList.valueOf(ColorUtils.getDarkerColor(mColor)));
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hexValue.getText().toString().matches("^[#][A-F,0-9,a-f]{6}$")) {

                    int color = Color.parseColor(hexValue.getText().toString());

                    float[] hsl = ColorUtils.colorToHSL(color);

                    hue.setProgress((int) hsl[0]);
                    saturation.setProgress((int) (1000f * hsl[1]));
                    value.setProgress((int) (1000f * hsl[2]));

                    updateColor(colorChooser, color);
                }
            }
        });

        hexValue.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    button.performClick();
                    InputMethodManager imm = (InputMethodManager)mContext.getSystemService(
                            Context.INPUT_METHOD_SERVICE);
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    return true;
                }
                return false;
            }
        });


        RecyclerView presets = (RecyclerView) colorChooser.findViewById(R.id.presets);
        presets.setLayoutManager(new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false));

        presets.setAdapter(new ColorPresetAdapter(mContext, new ColorPresetAdapter.ColorListener() {
            @Override
            public void onSelected(final int color) {

                int oldColor = ((ColorDrawable)colorChooser.findViewById(R.id.colorView).getBackground()).getColor();

                float[] oldHSL = ColorUtils.colorToHSL(oldColor);
                float[] newHSL = ColorUtils.colorToHSL(color);

                ObjectAnimator topAnim = ObjectAnimator.ofArgb(mDialog.findViewById(R.id.folder_darker_panel), "backgroundColor", ColorUtils.getDarkerColor(oldColor), ColorUtils.getDarkerColor(color));
                topAnim.setInterpolator(new DecelerateInterpolator());
                topAnim.setDuration(400);
                topAnim.start();

                ObjectAnimator hueAnimator = ObjectAnimator.ofInt(hue, "progress", (int)oldHSL[0], (int)newHSL[0]);
                ObjectAnimator satAnimator = ObjectAnimator.ofInt(saturation, "progress", (int)(oldHSL[1]*1000f), (int)(newHSL[1]*1000f));
                ObjectAnimator valAnimator = ObjectAnimator.ofInt(value, "progress", (int)(oldHSL[2]*1000f), (int)(newHSL[2]*1000f));
                ObjectAnimator colorAnimator = ObjectAnimator.ofArgb(colorChooser.findViewById(R.id.colorView), "backgroundColor",
                        oldColor, color);
                if(ColorUtils.isBrightColor(ColorUtils.getDarkerColor(color))){
                    ((TextView) mDialog.findViewById(R.id.folderName)).setTextColor(0x8a000000);
                } else {
                    ((TextView) mDialog.findViewById(R.id.folderName)).setTextColor(Color.WHITE);
                }

                AnimatorSet set = new AnimatorSet();
                set.playTogether(hueAnimator, satAnimator, valAnimator);
                set.setInterpolator(new OvershootInterpolator());
                set.setDuration(500);
                set.addListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {
                        updateColor(colorChooser, color);
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
                set.start();

                colorAnimator.setInterpolator(new DecelerateInterpolator());
                colorAnimator.setDuration(400);
                colorAnimator.start();

                hexValue.setText(String.format("#%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color)));
            }
        }));

        colorChooser.post(new Runnable() {
            @Override
            public void run() {
                colorChooser.animate().alpha(1);
            }
        });

        final Button switchTo = (Button) colorChooser.findViewById(R.id.switchTo);
        switchTo.setVisibility(View.GONE);

    }

    /**
     * Animates all sliders and the main color field to the given color values.
     * @param root The dialogs root view.
     * @param color The color to animate to.
     */
    void updateColor(View root, int color) {
        View colorView = root.findViewById(R.id.colorView);
        EditText hexValue = (EditText) root.findViewById(R.id.hexValue);
        mColor = color;

                mDialog.findViewById(R.id.folder_darker_panel).setBackgroundColor(ColorUtils.getDarkerColor(color));

                ((TextView) mDialog.findViewById(R.id.buttonOk)).setTextColor(ColorUtils.getDarkerColor(color));
        ((TextView) mDialog.findViewById(R.id.buttonCancel)).setTextColor(ColorUtils.getDarkerColor(color));

//                Switch alphabeticalSwitch = ((Switch) mDialog.findViewById(R.id.alphabeticalSwitch));
                ((ImageButton)mDialog.findViewById(R.id.submitButton)).setImageTintList(ColorStateList.valueOf(ColorUtils.getDarkerColor(color)));


                if (ColorUtils.isBrightColor(color)) {
                    ((TextView) mDialog.findViewById(R.id.folderName)).setTextColor(0x8a000000);
                    ((Button)mDialog.findViewById(R.id.switchTo)).setTextColor(0x8a000000);
                } else {
                    ((TextView) mDialog.findViewById(R.id.folderName)).setTextColor(Color.WHITE);
                    ((Button)mDialog.findViewById(R.id.switchTo)).setTextColor(Color.WHITE);
                }

        hexValue.setText(String.format("#%02X%02X%02X", Color.red(color), Color.green(color), Color.blue(color)));
        colorView.setBackgroundColor(color);
    }

    /**
     * Displays the Dialog.
     */
    public void show(){
        mDialog.show();
    }
}
