package com.dravite.homeux.settings.backup_restore;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.dravite.homeux.Const;
import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.LauncherLog;
import com.dravite.homeux.general_helpers.FileManager;
import com.dravite.homeux.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * An Activity to Backup and Restore all HomeUX data like Folders, Apps, etc.
 */
public class BackupRestoreActivity extends AppCompatActivity {

    //A flag to determine how item clicks should be handled.
    //If true, a click on an item will select it for deletion.
    public boolean isInDeleteMode = false;

    private FloatingActionButton floatingActionButton;
    private BackupAdapter mAdapter;

    //A security request code to access the storage to save backups
    private static final int PERM_REQUEST_READ_STORAGE = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.backup_restore_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
//        toolbar.setBackgroundColor(0xff2196F3);
//        toolbar.
        if(getSupportActionBar()!=null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RecyclerView mBackupList = (RecyclerView)findViewById(R.id.backupList);
        mBackupList.setLayoutManager(new GridLayoutManager(this, 1));

        //Check for storage permission
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED || (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED)) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_EXTERNAL_STORAGE) || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        PERM_REQUEST_READ_STORAGE);
            }
        } else {
            //Permission granted
            List<BackupObject> backups = new ArrayList<>();
            getBackups(backups);

            mAdapter = new BackupAdapter(this, backups);
            mBackupList.setAdapter(mAdapter);

            floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
            floatingActionButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isInDeleteMode) {
                        //If the delete mode flag is set, this button is red and will toggle a deletion of all selected backups
                        new AlertDialog.Builder(BackupRestoreActivity.this, R.style.DialogTheme)
                                .setTitle("Deleting")
                                .setMessage("Are you sure to delete those backups?")
                                .setNegativeButton(android.R.string.no, null)
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        for (BackupObject obj : mAdapter.mSelected) {

                                            File fileToDelete = new File(Environment.getExternalStorageDirectory().getPath() + "/HomeUX/" + obj.backupName);
                                            FileManager.deleteRecursive(fileToDelete);

                                            int index = mAdapter.mAllBackups.indexOf(obj);
                                            mAdapter.mAllBackups.remove(index);
                                            mAdapter.notifyItemRemoved(index);
                                        }
                                        for (int i = 0; i < mAdapter.mAllBackups.size(); i++) {
                                            mAdapter.notifyItemChanged(i);
                                        }
                                        toggleDeleteMode(false);
                                    }
                                }).show();
                    } else {
                        //...otherwise it will just create a new Backup
                        View dialogView = LayoutInflater.from(BackupRestoreActivity.this).inflate(R.layout.restore_view, null);
                        TextView description = (TextView) dialogView.findViewById(R.id.description);

                        //Add several checkboxes for the user to select what he wants to backup
                        description.setText("Please select which components you want to backup.");

                        final CheckBox cbFolders = (CheckBox) dialogView.findViewById(R.id.folders);
                        final CheckBox cbQA = (CheckBox) dialogView.findViewById(R.id.quickapps);
                        final CheckBox cbHidden = (CheckBox) dialogView.findViewById(R.id.hiddenapps);
                        final CheckBox cbGeneral = (CheckBox) dialogView.findViewById(R.id.general);

                        final AlertDialog dialog = new AlertDialog.Builder(BackupRestoreActivity.this, R.style.DialogTheme)
                                .setTitle(R.string.dialog_restore_title)
                                .setView(dialogView)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.dialog_restore_title, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        //Back it up
                                        backup(cbFolders.isChecked(), cbQA.isChecked(), cbHidden.isChecked(), cbGeneral.isChecked(), "Backup-" + System.currentTimeMillis());
                                        getBackups(mAdapter.mAllBackups);
                                        mAdapter.notifyDataSetChanged();
                                    }
                                }).show();

                        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!(!cbFolders.isChecked() && !cbGeneral.isChecked() && !cbHidden.isChecked() && !cbQA.isChecked()));
                            }
                        };

                        cbFolders.setOnCheckedChangeListener(listener);
                        cbQA.setOnCheckedChangeListener(listener);
                        cbHidden.setOnCheckedChangeListener(listener);
                        cbGeneral.setOnCheckedChangeListener(listener);
                    }
                }
            });
        }
    }

    /**
     * Sets the inDeleteMode flag and changes the floating action button color accordingly
     * @param enable whether isInDeleteMode should be true or false
     */
    void toggleDeleteMode(boolean enable){
        isInDeleteMode = enable;
        if(enable){
            floatingActionButton.animate().scaleX(0).scaleY(0).setDuration(100).setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    //Tint it red
                    floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(0xFFF44336));
                    floatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_delete_black_24dp));
                    floatingActionButton.animate().scaleY(1).scaleX(1).setDuration(100).setInterpolator(new DecelerateInterpolator());
                }
            });

        } else {
            floatingActionButton.animate().scaleX(0).scaleY(0).setDuration(100).setInterpolator(new AccelerateInterpolator()).withEndAction(new Runnable() {
                @Override
                public void run() {
                    //Tint it green
                    floatingActionButton.setBackgroundTintList(ColorStateList.valueOf(0xFF4CAF50));
                    floatingActionButton.setImageDrawable(getDrawable(R.drawable.ic_playlist_add_black_24dp));
                    floatingActionButton.animate().scaleY(1).scaleX(1).setDuration(100).setInterpolator(new DecelerateInterpolator());
                }
            });

        }
    }

    @Override
    public void onBackPressed() {
        if(isInDeleteMode){
            toggleDeleteMode(false);
            mAdapter.notifyDataSetChanged();
        } else {
            super.onBackPressed();
        }
    }

    /**
     * An Adapter that holds the backup items
     */
    public class BackupAdapter extends RecyclerView.Adapter<BackupAdapter.BackupHolder>{
        public class BackupHolder extends RecyclerView.ViewHolder{
            public BackupHolder(View view){
                super(view);
                clickView = view.findViewById(R.id.clickView);
                titleView = (TextView)view.findViewById(R.id.titleView);
                dataView = (TextView)view.findViewById(R.id.infoText);
            }

            View clickView;
            TextView titleView;
            TextView dataView;
        }

        //A list of all backups
        List<BackupObject> mAllBackups = new ArrayList<>();

        //A list of all selected backups (in isInDeleteMode)
        List<BackupObject> mSelected = new ArrayList<>();
        Context mContext;

        public BackupAdapter(Context context, List<BackupObject> allBackups){
            mContext = context;
            mAllBackups = allBackups;
        }

        @Override
        public BackupHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new BackupHolder(LayoutInflater.from(mContext).inflate(R.layout.backup_item, parent, false));
        }

        @Override
        public void onBindViewHolder(BackupHolder holder, final int position) {
            holder.titleView.setText("Backup from " + mAllBackups.get(position).backupDate);
            holder.dataView.setText(createDataString(mAllBackups.get(position)));

            holder.clickView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (!isInDeleteMode) {
                        //When longpressing an item, it should toggle the deletion mode
                        mSelected.clear();
                        mSelected.add(mAllBackups.get(position));
                        notifyItemChanged(position);
                        toggleDeleteMode(true);
                    }
                    return true;
                }
            });

            if(mSelected.contains(mAllBackups.get(position)) && isInDeleteMode){
                //Tint it reddish when its in deletion mode
                holder.clickView.setBackgroundTintList(ColorStateList.valueOf(0xFFF44336));
//                holder.titleView.setTextColor(Color.WHITE);
//                holder.dataView.setTextColor(Color.WHITE);
            } else {
                //Otherwise let it white
                holder.clickView.setBackgroundTintList(null);
                holder.titleView.setTextColor(Color.BLACK);
                holder.dataView.setTextColor(Color.BLACK);
            }

            holder.clickView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(isInDeleteMode){
                        //Add items to the selection
                        if(mSelected.contains(mAllBackups.get(position)) && isInDeleteMode){
                            mSelected.remove(mAllBackups.get(position));
                            if(mSelected.size()==0){
                                toggleDeleteMode(false);
                            }
                        } else {
                            mSelected.add(mAllBackups.get(position));
                        }
                        //And notify a change so it crossfades the colors
                        notifyItemChanged(position);
                    } else {
                        //Otherwise, open a restore dialog asking for what exactly the user wants to restore
                        View dialogView = LayoutInflater.from(mContext).inflate(R.layout.restore_view, null);

                        final CheckBox cbFolders = (CheckBox) dialogView.findViewById(R.id.folders);
                        final CheckBox cbQA = (CheckBox) dialogView.findViewById(R.id.quickapps);
                        final CheckBox cbHidden = (CheckBox) dialogView.findViewById(R.id.hiddenapps);
                        final CheckBox cbGeneral = (CheckBox) dialogView.findViewById(R.id.general);

                        final BackupObject object = mAllBackups.get(position);
                        File backupRoot = new File(Environment.getExternalStorageDirectory().getPath() + "/HomeUX/" + mAllBackups.get(position).backupName);

                        cbFolders.setChecked(new File(backupRoot.getPath() + "/folders").exists());
                        cbQA.setChecked(new File(backupRoot.getPath() + "/quickApps").exists());
                        cbGeneral.setChecked(new File(backupRoot.getPath() + "/general").exists());
                        cbHidden.setChecked(new File(backupRoot.getPath() + "/hiddenApps").exists());

                        if(!cbFolders.isChecked()){
                            cbFolders.setVisibility(View.GONE);
                        }
                        if(!cbQA.isChecked()){
                            cbQA.setVisibility(View.GONE);
                        }
                        if(!cbGeneral.isChecked()){
                            cbGeneral.setVisibility(View.GONE);
                        }
                        if(!cbHidden.isChecked()){
                            cbHidden.setVisibility(View.GONE);
                        }

                        final AlertDialog dialog = new AlertDialog.Builder(mContext, R.style.DialogTheme)
                                .setTitle("Restore (" + mAllBackups.get(position).backupDate + ")")
                                .setView(dialogView)
                                .setNegativeButton(android.R.string.cancel, null)
                                .setPositiveButton(R.string.dialog_restore, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        restore(cbFolders.isChecked(), cbQA.isChecked(), cbHidden.isChecked(), cbGeneral.isChecked(), object.backupName);
                                    }
                                }).show();

                        CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(!(!cbFolders.isChecked() && !cbGeneral.isChecked() && !cbHidden.isChecked() && !cbQA.isChecked()));
                            }
                        };

                        cbFolders.setOnCheckedChangeListener(listener);
                        cbQA.setOnCheckedChangeListener(listener);
                        cbHidden.setOnCheckedChangeListener(listener);
                        cbGeneral.setOnCheckedChangeListener(listener);
                    }
                }
            });
        }

        /**
         * Just a helper method creating a three-lined description String for the Backup objects
         * @param object The object to be translated
         * @return a three-lined String of the date, version and size
         */
        public String createDataString(BackupObject object){
            return /*object.backupDate + "\n" + object.madeWithVersion + "\n" +*/object.backupSize;
        }

        @Override
        public int getItemCount() {
            return mAllBackups.size();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode){
            case PERM_REQUEST_READ_STORAGE: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    List<BackupObject> backups = new ArrayList<>();
                    getBackups(backups);

                    RecyclerView mBackupList = (RecyclerView) findViewById(R.id.backupList);
                    mAdapter = new BackupAdapter(this, backups);
                    mBackupList.setAdapter(mAdapter);

                    floatingActionButton = (FloatingActionButton) findViewById(R.id.floatingActionButton);
                    floatingActionButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (isInDeleteMode) {
                                new AlertDialog.Builder(BackupRestoreActivity.this, R.style.DialogTheme)
                                        .setTitle("Deleting")
                                        .setMessage("Are you sure to delete those backups?")
                                        .setNegativeButton(android.R.string.no, null)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {

                                                for (BackupObject obj : mAdapter.mSelected) {

                                                    File fileToDelete = new File(Environment.getExternalStorageDirectory().getPath() + "/HomeUX/" + obj.backupName);
                                                    FileManager.deleteRecursive(fileToDelete);

                                                    int index = mAdapter.mAllBackups.indexOf(obj);
                                                    mAdapter.mAllBackups.remove(index);
                                                    mAdapter.notifyItemRemoved(index);
                                                }
                                                for (int i = 0; i < mAdapter.mAllBackups.size(); i++) {
                                                    mAdapter.notifyItemChanged(i);
                                                }
                                                toggleDeleteMode(false);
                                            }
                                        }).show();
                            } else {
                                View dialogView = LayoutInflater.from(BackupRestoreActivity.this).inflate(R.layout.restore_view, null);
                                TextView description = (TextView) dialogView.findViewById(R.id.description);
                                description.setText("Please select which components you want to backup.");

                                final CheckBox cbFolders = (CheckBox) dialogView.findViewById(R.id.folders);
                                final CheckBox cbQA = (CheckBox) dialogView.findViewById(R.id.quickapps);
                                final CheckBox cbHidden = (CheckBox) dialogView.findViewById(R.id.hiddenapps);
                                final CheckBox cbGeneral = (CheckBox) dialogView.findViewById(R.id.general);

                                final AlertDialog dialog = new AlertDialog.Builder(BackupRestoreActivity.this, R.style.DialogTheme)
                                        .setTitle(R.string.dialog_restore_title)
                                        .setView(dialogView)
                                        .setNegativeButton(android.R.string.cancel, null)
                                        .setPositiveButton(R.string.dialog_restore_title, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                backup(cbFolders.isChecked(), cbQA.isChecked(), cbHidden.isChecked(), cbGeneral.isChecked(), "Backup-" + System.currentTimeMillis());
                                                getBackups(mAdapter.mAllBackups);
                                                mAdapter.notifyDataSetChanged();
                                            }
                                        }).show();

                                CompoundButton.OnCheckedChangeListener listener = new CompoundButton.OnCheckedChangeListener() {
                                    @Override
                                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                        dialog.getButton(AlertDialog.BUTTON1).setEnabled(!(!cbFolders.isChecked() && !cbGeneral.isChecked() && !cbHidden.isChecked() && !cbQA.isChecked()));
                                    }
                                };

                                cbFolders.setOnCheckedChangeListener(listener);
                                cbQA.setOnCheckedChangeListener(listener);
                                cbHidden.setOnCheckedChangeListener(listener);
                                cbGeneral.setOnCheckedChangeListener(listener);
                            }
                        }
                    });
                } else {
                    //Don't show anything because meh
                }
            }
        }
    }

    /**
     * Loads all backups from the file system into a list
     * @param listToFill the list which later contains the backups
     */
    void getBackups(List<BackupObject> listToFill){
        listToFill.clear();

        File extStore = Environment.getExternalStorageDirectory();
        File backupFolder = new File(extStore.getPath()+"/HomeUX/");
        if(!backupFolder.exists())
            backupFolder.mkdirs();
        for (File subFolder :
                backupFolder.listFiles()) {
            if(subFolder.isDirectory() && new File(subFolder.getPath()+"/data").exists()){
                listToFill.add(new BackupObject(getFolderDate(subFolder),
                        "1.0",
                        subFolder.getName(),
                        formatFileSize(getFolderSize(subFolder)), new ArrayList<String>()));
            }
        }
    }

    /**
     * Returns the creation date of a given folder File
     * @param folder The folder to check
     * @return The localized creation date string of the folder
     */
    String getFolderDate(File folder){
        Date date = new Date(folder.lastModified());
        SimpleDateFormat mDateFormat = (SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault());

        return mDateFormat.format(date);
    }

    /**
     * Creates a suiting String for a given Byte size. It's calculating the smallest size having a number >1 before the comma.
     * @param size The size in Bytes
     * @return a String of the size with a size extension (MB, Bytes, GB etc.) appended
     */
    public static String formatFileSize(long size) {
        String hrSize;

        double b = size;
        double k = size/1024.0;
        double m = ((size/1024.0)/1024.0);
        double g = (((size/1024.0)/1024.0)/1024.0);
        double t = ((((size/1024.0)/1024.0)/1024.0)/1024.0);

        DecimalFormat dec = new DecimalFormat("0.00");

        if ( t>1 ) {
            hrSize = dec.format(t).concat(" TB");
        } else if ( g>1 ) {
            hrSize = dec.format(g).concat(" GB");
        } else if ( m>1 ) {
            hrSize = dec.format(m).concat(" MB");
        } else if ( k>1 ) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
    }

    /**
     * Reads the folder size in Bytes
     * @param folder The folder to read the size from
     * @return The folder size in Bytes
     */
    long getFolderSize(File folder){
        long size = 0;
        if (folder.isDirectory()) {
            for (File file : folder.listFiles()) {
                size += getFolderSize(file);
            }
        } else {
            size=folder.length();
        }
        return size;
    }

    /**
     * Restores a backup given by its backup name by looking for its directory and restoring according to set booleans.
     * @param bFolders Shall the folders be restored?
     * @param bQA Shall the QuickActions be restored?
     * @param bHidden Shall the hidden apps be restored?
     * @param bGeneral Shall general app settings be restored?
     * @param name The name of the backup
     */
    void restore(boolean bFolders, boolean bQA, boolean bHidden, boolean bGeneral, String name){
        LauncherActivity.updateAfterSettings = true;
        File destFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/HomeUX/" + name + "/");
        File internal = new File(getApplicationInfo().dataDir);


        File cacheFolder = getCacheDir();
        FileManager.deleteRecursive(cacheFolder);

        if(bFolders){
            backupTo(internal, new File(destFolder.getPath() + "/folders" + "/folderImg/"), new File(destFolder.getPath() + "/folders" + "/somedata.json"));
            try{
                new File(getCacheDir().getPath() + "/Shortcuts/").mkdirs();
                copyDirectoryOneLocationToAnotherLocation(new File(destFolder.getPath() + "/folders/Shortcuts/"), new File(getCacheDir().getPath() + "/Shortcuts/"));
            } catch (IOException e){
                e.printStackTrace();
            }
            try{
                new File(getCacheDir().getPath() + "/apps/").mkdirs();
                copyDirectoryOneLocationToAnotherLocation(new File(destFolder.getPath() + "/folders/apps/"), new File(getCacheDir().getPath() + "/apps/"));
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        if(bQA){
            backupTo(internal, new File(destFolder.getPath() + "/quickApps" + "/quickApps.json"));
        }
        if(bGeneral){
            boolean isLicensed = PreferenceManager.getDefaultSharedPreferences(this).getBoolean(Const.Defaults.TAG_LICENSED, Const.Defaults.getBoolean(Const.Defaults.TAG_LICENSED));
            SharedPreferenceHelper.loadSharedPreferencesFromFile(this, new File(destFolder.getPath() + "/general/", "/com.dravite.homeux_preferences.xml"));
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(Const.Defaults.TAG_LICENSED, isLicensed).apply();
        }
        if(bHidden){
            backupTo(internal, new File(destFolder.getPath() + "/hiddenApps" + "/hiddenApps.json"));
        }
    }

    /**
     * Creates a backup to the backup path under the given name direction and according to set booleans.
     * @param bFolders Shall the folders be backed up?
     * @param bQA Shall the QuickActions be backed up?
     * @param bHidden Shall the hidden apps be backed up?
     * @param bGeneral Shall general app settings be backed up?
     * @param name The name of the backup.
     */
    void backup(boolean bFolders, boolean bQA, boolean bHidden, boolean bGeneral, String name){
        File targetFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/HomeUX/" + name + "/");

        targetFolder.mkdirs();
        if(bFolders){
            backupTo(new File(targetFolder.getPath() + "/folders/"),getFileFromInternal("/cache/Shortcuts/"), getFileFromInternal("/cache/apps/"), getFileFromInternal("/folderImg/"), getFileFromInternal("/somedata.json"));
        }
        if(bQA){
            backupTo(new File(targetFolder.getPath() + "/quickApps/"), getFileFromInternal("/quickApps.json"));
        }
        if(bGeneral){
            SharedPreferenceHelper.saveSharedPreferencesToFile(this, new File(targetFolder.getPath() + "/general/", "/com.dravite.homeux_preferences.xml"));
        }
        if(bHidden){
            backupTo(new File(targetFolder.getPath() + "/hiddenApps/"), getFileFromInternal("/hiddenApps.json"));
        }
        try{
            createDataFile(targetFolder, getPackageManager().getPackageInfo(getPackageName(), 0).versionName);
        } catch (PackageManager.NameNotFoundException e){
            e.printStackTrace();
        }
    }

    /**
     * Retrieves a file from HomeUX's internal data directory
     * @param endPath The sub-directory
     * @return A file pointing to the given subdirector
     */
    File getFileFromInternal(String endPath){
        File internal = new File(getApplicationInfo().dataDir);
        return new File(internal.getPath() + "/" + endPath);
    }

    /**
     * Helper method: backs up several files to a target folder.
     * @param targetFolder The folder to backup the files to
     * @param destFiles The files to be backed up
     */
    void backupTo(File targetFolder, File... destFiles){
        for (File file: destFiles) {
            try{
                File newFile = new File(targetFolder.getPath(),file.getName());
                if(newFile.exists())
                    newFile.delete();

                targetFolder.mkdirs();
                copyDirectoryOneLocationToAnotherLocation(file, newFile);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    /**
     * Creates a file describing the backup
     * @param targetFolder The folder pointing to the backup directory
     * @param appVersion The HomeUX version String
     */
    void createDataFile(File targetFolder, String appVersion){
        try {
            File file = new File(targetFolder.getPath(), "/data");
            file.createNewFile();
            FileOutputStream fos = new FileOutputStream(file);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fos);
            outputStreamWriter.write(appVersion);
            outputStreamWriter.close();
            fos.close();
        }
        catch (IOException e) {
            LauncherLog.w("Exception", "File write failed: " + e.toString());
        }
    }

    /**
     * Copies a directory entirely to a new directory
     * @param sourceLocation The directory to copy
     * @param targetLocation The targeted directory
     * @throws IOException
     */
    public static void copyDirectoryOneLocationToAnotherLocation(File sourceLocation, File targetLocation)
            throws IOException {

        if (sourceLocation.isDirectory()) {
            if (!targetLocation.exists()) {
                targetLocation.mkdir();
            }

            String[] children = sourceLocation.list();
            for (int i = 0; i < sourceLocation.listFiles().length; i++) {

                copyDirectoryOneLocationToAnotherLocation(new File(sourceLocation, children[i]),
                        new File(targetLocation, children[i]));
            }
        } else {

            InputStream in = new FileInputStream(sourceLocation);

            OutputStream out = new FileOutputStream(targetLocation);

            // Copy the bits from instream to outstream
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            in.close();
            out.close();
        }

    }

}
