package com.dravite.homeux.app_editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LauncherActivityInfo;
import android.content.pm.LauncherApps;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;

import com.dravite.homeux.general_helpers.IconPackManager;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;

import java.lang.ref.SoftReference;

/**
 * An Activity that allows you to edit the label and icon of an application icon from the app drawer.
 */
public class AppEditorActivity extends AppCompatActivity {

    //Requested from LauncherActivity when opening this AppEditorActivity
    public static final int REQUEST_EDIT_APP = 4085;

    //Requested when opening the AppEditorIconPackActivity
    public static final int REQUEST_PASS_ICON = 4086;

    //Unthemed icon
    private Bitmap mDefaultIcon;

    //Either like mDefaultIcon or (if there is an icon pack applied) the themed version.
    private Bitmap mThemedIcon;

    //A custom icon selected by the user
    private Bitmap mCustomIcon;

    IconPackManager.IconPack mCurrentIconPack;

    //Current application label
    private String mAppLabel;

    //Current app
    private static Application mCurrentApp;

    //Sets, if the application has already been changed in this activity so there can be a Dialog when cancelling
    private boolean mHasChanged = false;

    /**
     * A static Object that helps passing an application and the current icon pack from the LauncherActivity to this AppEditorActivity
     */
    public static class PassApp{
        public static SoftReference<Application> softApp;
        public static SoftReference<IconPackManager.IconPack> iconPack;
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
                return(true);
        }

        return(super.onOptionsItemSelected(item));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_editor_new);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setBackgroundColor(ContextCompat.getColor(this, R.color.colorPrimary));
        getWindow().setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimaryDark));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable homeAsUpIndicatorIcon = getDrawable(R.drawable.ic_clear_black_24dp);
        homeAsUpIndicatorIcon.setTint(Color.WHITE);
        getSupportActionBar().setHomeAsUpIndicator(homeAsUpIndicatorIcon);
        initSaveButton();

        //Retrieve the passed app and icon pack
        mCurrentApp = PassApp.softApp.get();
        mCurrentIconPack = PassApp.iconPack.get();

        //If there was an error passing an app, show a dialog and close the Activity after that.
        if(mCurrentApp == null){
            new AlertDialog.Builder(this, R.style.DialogTheme).setCancelable(false).setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    AppEditorActivity.this.finish();
                }
            }).setTitle(R.string.error).setMessage(R.string.app_error_message).show();
        }

        //Get information about the current app
        LauncherApps launcherApps = (LauncherApps)getSystemService(LAUNCHER_APPS_SERVICE);
        Intent intent = new Intent();
        intent.setComponent(new ComponentName(mCurrentApp.packageName, mCurrentApp.className));
        LauncherActivityInfo mCurrentInfo = launcherApps.resolveActivity(intent, android.os.Process.myUserHandle());

        //Close this activity if the app has no LauncherActivityInfo
        if(mCurrentInfo==null)
            finish();

        //Set all icons
        mDefaultIcon = LauncherUtils.drawableToBitmap(mCurrentInfo.getIcon(0));
        mThemedIcon = mCurrentApp.loadThemedIcon(mCurrentIconPack, mCurrentInfo);
        mCustomIcon = mCurrentApp.loadCustomIcon(this);

        //Set the label
        mAppLabel = mCurrentApp.loadLabel(this);

        //Initialize UI elements
        //Label TextBox
        final EditText label = (EditText)findViewById(R.id.label);
        label.setEnabled(true);
        label.setText(mAppLabel.replace("\n", ""));
        label.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mAppLabel=s.toString();
                mHasChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //Label reset button
        ImageButton resetLabel = (ImageButton)findViewById(R.id.reset_label);
        resetLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                label.setText(mCurrentApp.label);
                mHasChanged = true;
            }
        });

        //Select icon from iconPack button
        final Button selectIconBtn = (Button) findViewById(R.id.btn_select_icon);
        selectIconBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AppEditorActivity.this, AppEditorIconPackActivity.class);
                startActivityForResult(intent, REQUEST_PASS_ICON);
            }
        });

        //Icon radio buttons
        final RadioButton def = (RadioButton)findViewById(R.id.checkDefault);
        final RadioButton orig = (RadioButton)findViewById(R.id.checkOriginal);
        final RadioButton icop = (RadioButton)findViewById(R.id.checkCustom);

        //Determine which RadioButton has to be checked
        boolean isDefault = mCustomIcon!=null && mDefaultIcon.sameAs(mCustomIcon);
        boolean isCustom = !isDefault && mCustomIcon!=null;

        if(mCustomIcon==null)
            mCustomIcon = mThemedIcon;

        final View defaultSelector = findViewById(R.id.defaultSelector);
        final View customSelector = findViewById(R.id.customSelector);
        final View originalSelector = findViewById(R.id.originalSelector);

        defaultSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                def.setChecked(true);
            }
        });
        customSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                icop.setChecked(true);
            }
        });
        originalSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                orig.setChecked(true);
            }
        });

        //Icon imageViews
        final ImageView circleDefault = (ImageView)findViewById(R.id.circleDefault);
        final ImageView circleOriginal = (ImageView)findViewById(R.id.circleOriginal);
        final ImageView circleCustom = (ImageView)findViewById(R.id.circleCustom);
        circleDefault.setImageBitmap(mCurrentApp.loadThemedIcon(mCurrentIconPack, mCurrentInfo));
        circleOriginal.setImageBitmap(mDefaultIcon);
        circleCustom.setImageBitmap(mCustomIcon);

        //RadioButton OnCheckedChangeListener for all three RBs
        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mHasChanged = true;

                InputMethodManager imm = (InputMethodManager)
                        getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(label.getWindowToken(), 0);

                switch (buttonView.getId()){
                    case R.id.checkDefault:
                        if(isChecked) {
                            orig.setChecked(false);
                            icop.setChecked(false);
                            def.setChecked(true);
                            circleDefault.setImageTintList(null);
                            circleOriginal.setImageTintList(ColorStateList.valueOf(0xbbffffff));
                            circleOriginal.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                            circleCustom.setImageTintList(ColorStateList.valueOf(0xbbffffff));
                            circleCustom.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                            selectIconBtn.setEnabled(false);
                            selectIconBtn.animate().scaleX(0).scaleY(0).setDuration(150);
                        }
                        break;
                    case R.id.checkOriginal:
                        if (isChecked) {
                            def.setChecked(false);
                            icop.setChecked(false);
                            orig.setChecked(true);
                            circleOriginal.setImageTintList(null);
                            circleDefault.setImageTintList(ColorStateList.valueOf(0xbbffffff));
                            circleDefault.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                            circleCustom.setImageTintList(ColorStateList.valueOf(0xbbffffff));
                            circleCustom.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                            selectIconBtn.setEnabled(false);
                            selectIconBtn.animate().scaleX(0).scaleY(0).setDuration(150);
                        }
                        break;
                    case R.id.checkCustom:
                        if(isChecked) {
                            orig.setChecked(false);
                            def.setChecked(false);
                            icop.setChecked(true);
                            circleCustom.setImageTintList(null);
                            circleOriginal.setImageTintList(ColorStateList.valueOf(0xbbffffff));
                            circleOriginal.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                            circleDefault.setImageTintList(ColorStateList.valueOf(0xbbffffff));
                            circleDefault.setImageTintMode(PorterDuff.Mode.SRC_ATOP);
                            selectIconBtn.setEnabled(true);
                            selectIconBtn.animate().scaleX(1).scaleY(1).setDuration(150);
                        }
                        break;
                }
            }
        };
        def.setOnCheckedChangeListener(listener);
        orig.setOnCheckedChangeListener(listener);
        icop.setOnCheckedChangeListener(listener);

        //Check the needed RadioButton
        if(isCustom) {
            icop.setChecked(true);
            circleCustom.setImageTintList(null);
        } else {
            if (isDefault) {
                orig.setChecked(true);
                circleOriginal.setImageTintList(null);
            } else {
                def.setChecked(true);
                circleDefault.setImageTintList(null);
            }
        }
    }

    @Override
    protected void onStop() {
        //Clear memory of the PassApp.iconPack (because it's static)
        PassApp.iconPack.clear();
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_PASS_ICON) {
                mCustomIcon = data.getParcelableExtra("icon");
                final ImageView currentIcon = (ImageView)findViewById(R.id.circleCustom);
                currentIcon.setImageBitmap(mCustomIcon);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Initializes what should happen when clicking on the "Save" button.
     */
    void initSaveButton() {
        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Only need two radiobuttons, the last one falls into the "else" case.
                final RadioButton def = (RadioButton)findViewById(R.id.checkDefault);
                final RadioButton orig = (RadioButton)findViewById(R.id.checkOriginal);

                if(def.isChecked()){
                    //Remove the custom icon and just use the themed one.
                    mCurrentApp.saveCustomIcon(AppEditorActivity.this, null);
                } else if (orig.isChecked()){
                    //Save the system default app icon as a fake custom icon
                    mCurrentApp.saveCustomIcon(AppEditorActivity.this, mDefaultIcon);
                } else {
                    //Save the selected icon as a custom icon
                    mCurrentApp.saveCustomIcon(AppEditorActivity.this, mCustomIcon);
                }

                //Change label to the new one
                LauncherActivity.mDrawerTree.changeLabel(mCurrentApp.loadLabel(AppEditorActivity.this), new ComponentName(mCurrentApp.packageName, mCurrentApp.className).toString(), mAppLabel);
                if(mAppLabel.equals(mCurrentApp.label)){
                    //Remove the custom label if its equal to the default label
                    mCurrentApp.saveLabel(AppEditorActivity.this, null);
                } else {
                    //Set the custom label
                    mCurrentApp.saveLabel(AppEditorActivity.this, mAppLabel);
                }

                //Finish the Activity with a RESULT_OK. Also pass the app back to the LauncherActivity
                Intent data = new Intent();
                setResult(Activity.RESULT_OK, data);
                PassApp.softApp=new SoftReference<>(mCurrentApp);
                mCurrentApp=null;
                finish();
            }
        });
    }

    /**
     * Generalize, what should happen when you either press back or the homeAsUpIndicator button.
     */
    void backPressed(){
        if(mHasChanged) {
            //Check if the app has been changed. If so, show a Dialog notifying the user what we should do next
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_MinWidth);
            alertDialogBuilder.setTitle(R.string.dialog_exit_saving_title)
                    .setMessage(R.string.dialog_exit_saving_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            AppEditorActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        } else {
            //If it hasn't changed anyways, just close the Activity.
            super.onBackPressed();
        }
    }
}
