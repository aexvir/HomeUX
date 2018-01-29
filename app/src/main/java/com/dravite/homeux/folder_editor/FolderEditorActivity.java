package com.dravite.homeux.folder_editor;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.*;
import android.preference.PreferenceManager;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.drawerobjects.structures.FolderStructure;
import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.general_dialogs.ColorDialog;
import com.dravite.homeux.general_dialogs.helpers.ColorWatcher;
import com.dravite.homeux.general_helpers.FileManager;

import java.io.FileNotFoundException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class FolderEditorActivity extends AppCompatActivity {

    public static final int REQUEST_ADD_FOLDER = 4025;
    public static final int REQUEST_EDIT_FOLDER = 4026;
    private static final int REQUEST_CROP_IMAGE = 928;
    private static final int REQUEST_GET_ICON = 929;

    /**
     * Passes lists to this activity, one with all apps and one with only contained apps.
     */
    public static class AppListPasser {
        static ArrayList<ComponentName> appList = new ArrayList<>();
        static ArrayList<ComponentName> alreadyContainedList = new ArrayList<>();

        public static void passAppList(ArrayList<ComponentName> appList) {
            AppListPasser.appList = appList;
        }

        public static void passAlreadyContainedList(ArrayList<ComponentName> alreadyContainedList){
            AppListPasser.alreadyContainedList = alreadyContainedList;
        }

        public static ArrayList<ComponentName> receiveContainedList() {
            ArrayList<ComponentName> apps = new ArrayList<>(alreadyContainedList);
            //Clear to not keep static references in memory
            alreadyContainedList.clear();
            return apps;
        }

        public static ArrayList<ComponentName> receiveAppList() {
            ArrayList<ComponentName> apps = new ArrayList<>(appList);
            //Clear to not keep static references in memory
            appList.clear();
            return apps;
        }
    }

    private Bitmap mCurrentPanelImage;
    private int mCurrentAccent = 0xffffffff;
    private String mCurrentIconRes = "";

    private int requestCode = REQUEST_EDIT_FOLDER;
    private int folderIndex = 0;
    private boolean mHasChanged = false;
    boolean hasImageChanged = false;

    private ArrayList<ComponentName> mCurrentAppList = new ArrayList<>();
    private ArrayList<ComponentName> mCurrentContainsList = new ArrayList<>();

    private String mCurrentFolderName = "", mOldFolderName = "";
    private FolderStructure.Folder mCurrentFolder;
    private FolderStructure mFolderStructure;

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

    /**
     * Passes a folder to edit it as a WeakReference.
     */
    public static class FolderPasser{
        public static WeakReference<FolderStructure.Folder> passFolder;
    }
/*
    public void createAppIconStackInto(List<ComponentName> collection, RoundImageView imageView){
        LauncherApps launcherApps = (LauncherApps)getSystemService(LAUNCHER_APPS_SERVICE);
        Drawable[] drawables = new Drawable[Math.min(collection.size(), 3)];
        int end = 3;
        for(int i=0; i<collection.size() && i<end; i++){
            Intent intent = new Intent();
            intent.setComponent(collection.get(i));
            LauncherActivityInfo inf = (launcherApps.resolveActivity(intent, android.os.Process.myUserHandle()));
            if(inf!=null){
                drawables[Math.min(collection.size(), end)-1-i] = inf.getIcon(0);
                drawables[Math.min(collection.size(), end)-1-i].setTintList(null);
            } else {
                end++;
            }
        }

        Drawable drawable;
        if(drawables.length>0 && drawables[0]==null)
            drawable = new TextDrawable("err", 2, LauncherUtils.dpToPx(4, this));
        else
            drawable = new LayerDrawable(drawables);
        int padding = LauncherUtils.dpToPx(12, this);
        int vOffset = LauncherUtils.dpToPx(6, this);
        if(drawable instanceof LayerDrawable) {
            for (int i = 0; i < Math.min(collection.size(), 3); i++) {
                ((LayerDrawable)drawable).setLayerInset(i,
                        padding + i * LauncherUtils.dpToPx(-2, this),
                        padding - vOffset + i * LauncherUtils.dpToPx(2, this),
                        padding + i * LauncherUtils.dpToPx(-2, this),
                        padding + vOffset + i * LauncherUtils.dpToPx(-6, this));
            }
        }
        imageView.setImageDrawable(drawable);
    }*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_editor_new);

        mHasChanged = false;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        Drawable haui = getDrawable(R.drawable.ic_clear_black_24dp);
        haui.setTint(Color.WHITE);
        getSupportActionBar().setHomeAsUpIndicator(haui);
        initSaveButton();

        getWindow().setStatusBarColor(0xff1976D2);

        mCurrentContainsList = AppListPasser.receiveContainedList();

        //createAppIconStackInto(mCurrentContainsList, (RoundImageView)findViewById(R.id.circleApps));
        ((TextView)findViewById(R.id.chooseApps)).setText("Contains " + mCurrentContainsList.size() + " apps.");

        //loadAll Folder
        requestCode = getIntent().getIntExtra("requestCode", REQUEST_EDIT_FOLDER);
        folderIndex = getIntent().getIntExtra("folderIndex", 0);
        if(FolderPasser.passFolder==null||FolderPasser.passFolder.get()==null){

            //If null, create a new folder.
            mCurrentFolder = new FolderStructure.Folder();
            mCurrentFolder.headerImage = BitmapFactory.decodeResource(getResources(), R.drawable.welcome_header_small);
            mCurrentFolder.isAllFolder = false;
            mCurrentFolder.accentColor = 0xFF303F9F;
            mCurrentFolder.folderName = "";
            mCurrentFolder.folderIconRes = "ic_folder";
            mCurrentFolder.pages.add(new FolderStructure.Page());
            mCurrentFolder.mFolderType = FolderStructure.Folder.FolderType.TYPE_WIDGETS;
        } else {
            mCurrentFolder = FolderPasser.passFolder.get();
        }

        if(mCurrentFolder.folderName==null)
            mCurrentFolder.folderName="";
        if(FolderPasser.passFolder!=null)
            FolderPasser.passFolder.clear();

        //Receive Folder Data.
        mCurrentFolder.loadImage(this);
        mCurrentPanelImage = mCurrentFolder.headerImage;
        if(mCurrentPanelImage==null)
            mCurrentPanelImage = BitmapFactory.decodeResource(getResources(), R.drawable.welcome_header_small);
        ((ImageView) findViewById(R.id.circlePrimary)).setImageBitmap(mCurrentPanelImage);

        mCurrentAccent = mCurrentFolder.accentColor;
        mCurrentIconRes = mCurrentFolder.folderIconRes;
        mCurrentIconRes=mCurrentIconRes==null?"ic_folder":mCurrentIconRes;
        mCurrentFolderName = mCurrentFolder.folderName;
        mOldFolderName = mCurrentFolder.folderName;
        boolean isAllFolder = mCurrentFolder.isAllFolder;

        if(mCurrentFolder.folderName.equals("All")){
            findViewById(R.id.folderName).setEnabled(false);
        } else {
            findViewById(R.id.folderName).setEnabled(true);
        }

        mFolderStructure = LauncherActivity.mFolderStructure;

        //Set Activity title according to whether you edit or add a folder.
        getSupportActionBar().setTitle(mCurrentFolderName.equals("") ? getString(R.string.activity_folder_editor_new) : getString(R.string.activity_folder_editor_edit));
        setColor(mCurrentAccent);

        //
        // Initialize Views.
        //

        final EditText folderName = (EditText) findViewById(R.id.folderName);
        folderName.setText(mCurrentFolderName);
        folderName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCurrentFolderName = s.toString();
                folderName.setBackgroundColor(Color.TRANSPARENT);
                mHasChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        folderName.setEnabled(!isAllFolder);

        final ImageView iconFolder = (ImageView) findViewById(R.id.circleIcon);
        iconFolder.setImageDrawable(getDrawable(getResources().getIdentifier(mCurrentIconRes, "drawable", getPackageName())));
        iconFolder.setImageTintList(ColorStateList.valueOf(Color.BLACK));
        View chooseFolderIcon = findViewById(R.id.circleIcon);
        chooseFolderIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FolderEditorActivity.this, SelectFolderIconActivity.class);
                startActivityForResult(intent, REQUEST_GET_ICON);
            }
        });

        View primarySelector = findViewById(R.id.primarySelector);
        primarySelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHasChanged=true;

                new AlertDialog.Builder(FolderEditorActivity.this, R.style.DialogTheme)
                        .setTitle("Select Type")
                        .setItems(new CharSequence[]{"Colour", "Image"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                switch (which) {
                                    case 0:
                                        new ColorDialog(FolderEditorActivity.this, getString(R.string.dialog_primary_color), 0xffF44336, new ColorWatcher() {
                                            @Override
                                            public void onColorSubmitted(int color) {
                                                hasImageChanged = true;
                                                mCurrentPanelImage = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888);
                                                mCurrentPanelImage.eraseColor(color);
                                                ((ImageView)findViewById(R.id.circlePrimary)).setImageBitmap(mCurrentPanelImage);
                                            }
                                        }).show();
                                        break;
                                    case 1:
                                        Intent i = new Intent(Intent.ACTION_PICK);
                                        i.setType("image/*");
                                        i.putExtra("return-data", true);
                                        i.setAction(Intent.ACTION_GET_CONTENT);
                                        startActivityForResult(i, REQUEST_CROP_IMAGE);
                                        break;
                                }
                            }
                        }).show();
            }
        });

        View accentSelector = findViewById(R.id.accentSelector);
        accentSelector.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHasChanged=true;
                new ColorDialog(FolderEditorActivity.this, getString(R.string.dialog_accent_color), mCurrentAccent, new ColorWatcher() {
                    @Override
                    public void onColorSubmitted(int color) {
                        setColor(color);
                        mCurrentAccent = color;
                    }
                }).show();
            }
        });

        final RecyclerView appGrid = (RecyclerView)findViewById(R.id.appList);
        if(isAllFolder) {
            ((ViewGroup)appGrid.getParent()).setVisibility(View.GONE);
        } else {
            ((ViewGroup)appGrid.getParent()).setVisibility(View.VISIBLE);
            appGrid.setLayoutManager(new GridLayoutManager(this,4));
            appGrid.setAdapter(new FolderAddAppsListAdapter(this, mCurrentAppList, mCurrentContainsList));
        }


        final Switch appSwitch = (Switch)findViewById(R.id.appSwitch);
        appGrid.addOnScrollListener(new RecyclerView.OnScrollListener() {
            boolean upAnimRunning, downAnimRunning;

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if(dy> LauncherUtils.dpToPx(2, FolderEditorActivity.this) && !upAnimRunning){
                    upAnimRunning = true;
                    downAnimRunning = false;
                    appSwitch.animate().translationY(LauncherUtils.dpToPx(48, FolderEditorActivity.this));
                } else if(dy<LauncherUtils.dpToPx(-2, FolderEditorActivity.this) && !downAnimRunning){
                    upAnimRunning = false;
                    downAnimRunning = true;
                    appSwitch.animate().translationY(0);
                }
            }
        });
        appSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                ((FolderAddAppsListAdapter) appGrid.getAdapter()).setShowInOthers(!isChecked);
            }
        });

    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // Raw height and width of image
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {

            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) > reqHeight
                    && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    public Bitmap decodeSampledBitmapFromResource(Resources res, Uri uri, int reqWidth, int reqHeight) throws FileNotFoundException {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), new Rect(0, 0, 0, 0), options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeStream(getContentResolver().openInputStream(uri), new Rect(0, 0,0,0), options);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            /*if (requestCode == FolderEditorAddActivity.REQUEST_APP_LIST) {
                mCurrentAppList = AppListPasser.receiveAppList();
                mCurrentContainsList = AppListPasser.receiveContainedList();

                ArrayList<ComponentName> tmpList = new ArrayList<>(mCurrentAppList);
                tmpList.removeAll(mCurrentContainsList);
                tmpList.addAll(mCurrentContainsList);

                *//*((TextView)findViewById(R.id.chooseApps)).setText("Contains " + (tmpList.size()) + " apps.");*//*
            }*/

            switch (requestCode) {
                case REQUEST_CROP_IMAGE:
                    if(resultCode==RESULT_OK){
                        hasImageChanged = true;
                        try {
                            Uri selectedImageUri = data.getData();
                            ((ImageView)findViewById(R.id.circlePrimary)).setImageBitmap(null);
                            mCurrentPanelImage = decodeSampledBitmapFromResource(getResources(), selectedImageUri, findViewById(android.R.id.content).getMeasuredWidth(), findViewById(android.R.id.content).getMeasuredWidth());
                            ((ImageView)findViewById(R.id.circlePrimary)).setImageBitmap(mCurrentPanelImage);
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case REQUEST_GET_ICON:
                    if(resultCode==RESULT_OK){
                        String res = data.getStringExtra("iconRes");
                        mCurrentIconRes = res;

                        ImageView v = ((ImageView)findViewById(R.id.circleIcon));
                        v.setImageResource(getResources().getIdentifier(mCurrentIconRes, "drawable", getPackageName()));
                        v.setImageTintList(ColorStateList.valueOf(Color.BLACK));
                        v.setAlpha(0.57f);

                    }
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    boolean isFolderNameExisting() {
        /*
        When adding a folder, only check if name exists,
        When editing a folder, check if name exists but is not equal from current.
         */
        return requestCode==REQUEST_ADD_FOLDER ? mFolderStructure.getFolderWithName(((EditText) findViewById(R.id.folderName)).getText().toString())!=null :
                mFolderStructure.getFolderWithName(((EditText) findViewById(R.id.folderName)).getText().toString())!=null &&
                        !mCurrentFolder.folderName.equals(((EditText) findViewById(R.id.folderName)).getText().toString());
    }

    /**
     * Initializes the save button - obviously :P
     */
    void initSaveButton() {
        Button saveButton = (Button) findViewById(R.id.save);
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (((EditText) findViewById(R.id.folderName)).getText().toString().equals("")) {
                    TextInputLayout inputLayout = (TextInputLayout) findViewById(R.id.nameInput);
                    inputLayout.setError(getString(R.string.folder_no_name_error));
                    return;
                }
                if (isFolderNameExisting()) {
                    TextInputLayout inputLayout = (TextInputLayout) findViewById(R.id.nameInput);
                    inputLayout.setError(getString(R.string.folder_name_already_taken_error));
                    return;
                }

                if(requestCode==REQUEST_ADD_FOLDER){
                    mCurrentFolder.headerImage = mCurrentPanelImage;
                    mCurrentFolder.accentColor = mCurrentAccent;
                    mCurrentFolder.folderName = mCurrentFolderName;
                    mCurrentFolder.folderIconRes = mCurrentIconRes;
                    FolderPasser.passFolder = new WeakReference<>(mCurrentFolder);
                    Intent intent = new Intent();

                    Comparator<ComponentName> componentNameComparator = new Comparator<ComponentName>() {
                        @Override
                        public int compare(ComponentName lhs, ComponentName rhs) {
                            try {
                                String labelL = getPackageManager().getActivityInfo(lhs, 0).loadLabel(getPackageManager()).toString();
                                String labelR = getPackageManager().getActivityInfo(rhs, 0).loadLabel(getPackageManager()).toString();
                                return labelL.toLowerCase().compareTo(labelR.toLowerCase());
                            } catch (PackageManager.NameNotFoundException e){
                                return lhs.compareTo(rhs);
                            }
                        }
                    };

                    Collections.sort(mCurrentAppList, componentNameComparator);
                    intent.putExtra("appList", mCurrentAppList);
                    setResult(Activity.RESULT_OK, intent);
                    finish();
                    return;
                }

                //Save default folder if name changed
                if (mOldFolderName.equals(PreferenceManager.getDefaultSharedPreferences(FolderEditorActivity.this).getString(Const.Defaults.TAG_DEFAULT_FOLDER, Const.Defaults.getString(Const.Defaults.TAG_DEFAULT_FOLDER)))) {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(FolderEditorActivity.this).edit();
                    editor.putString(Const.Defaults.TAG_DEFAULT_FOLDER, mCurrentFolderName);
                    editor.apply();
                }

                new AsyncTask<Void,Void,Void>(){
                    ProgressDialog dialog = new ProgressDialog(FolderEditorActivity.this, R.style.DialogTheme);
                    @Override
                    protected void onPreExecute() {
                        dialog.setTitle("Saving");
                        dialog.setMessage("Please wait while the settings are being saved...");
                        dialog.show();
                    }

                    @Override
                    protected Void doInBackground(Void... params) {

                        if(hasImageChanged) {
                            if (!mCurrentPanelImage.isRecycled()){
                                //Save header image async
                                FileManager.saveBitmapToData(FolderEditorActivity.this, mCurrentPanelImage, mCurrentFolderName, true);
                            }
                            hasImageChanged = false;
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Void aVoid) {
                        dialog.cancel();

                        Intent data = new Intent();
                        data.putExtra("iconRes", mCurrentIconRes);
                        data.putExtra("folderName", mCurrentFolderName);
                        data.putExtra("accent", mCurrentAccent);
                        data.putExtra("folderIndex", folderIndex);

                        Comparator<ComponentName> componentNameComparator = new Comparator<ComponentName>() {
                            @Override
                            public int compare(ComponentName lhs, ComponentName rhs) {
                                try {
                                    String labelL = getPackageManager().getActivityInfo(lhs, 0).loadLabel(getPackageManager()).toString();
                                    String labelR = getPackageManager().getActivityInfo(rhs, 0).loadLabel(getPackageManager()).toString();
                                    return labelL.toLowerCase().compareTo(labelR.toLowerCase());
                                } catch (PackageManager.NameNotFoundException e){
                                    return lhs.compareTo(rhs);
                                }
                            }
                        };

                        Collections.sort(mCurrentAppList, componentNameComparator);
                        data.putExtra("appList", mCurrentAppList);
                        setResult(Activity.RESULT_OK, data);
                        finish();
                    }
                }.execute();
            }
        });
    }

    /**
     * Sets the accent color circle background.
     * @param accent the target color
     */
    void setColor(final int accent) {
        ImageView circleAccent = (ImageView) findViewById(R.id.circleAccent);
        circleAccent.setImageTintList(ColorStateList.valueOf(accent));

    }

    /**
     * A helper method, showing a dialog if there are unsaved changes
     */
    void backPressed(){
        if(mHasChanged) {
            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_MinWidth);
            alertDialogBuilder.setTitle(R.string.dialog_exit_saving_title)
                    .setMessage(R.string.dialog_exit_saving_message)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            FolderEditorActivity.super.onBackPressed();
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    }).show();
        } else {
            super.onBackPressed();
        }
    }
}
