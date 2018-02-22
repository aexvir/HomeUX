package com.dravite.homeux.settings.backup_restore;

import java.util.ArrayList;

/**
 * An object declaring the data for a backup.
 */
public class BackupObject {

    public String backupDate;
    public String madeWithVersion;
    public String backupName;
    public String backupSize;
    public ArrayList<String> backupComponents;

    public BackupObject(String backupDate, String madeWithVersion, String backupName, String backupSize, ArrayList<String> backupComponents) {
        this.backupDate = backupDate;
        this.madeWithVersion = madeWithVersion;
        this.backupName = backupName;
        this.backupSize = backupSize;
        this.backupComponents = backupComponents;
    }
}
