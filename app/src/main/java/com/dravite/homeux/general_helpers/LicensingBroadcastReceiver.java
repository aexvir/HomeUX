package com.dravite.homeux.general_helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.dravite.homeux.LauncherLog;
import com.dravite.homeux.iconpacks.LicensingObserver;

/**
 * Created by Johannes on 03.11.2015.
 * This Receiver listens for the PRO app to send a license update.
 */
public class LicensingBroadcastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        LauncherLog.d("LicensingReceiver", "Got a broadcast...");
        LicensingObserver.getInstance().updateValue(intent);
    }
}
