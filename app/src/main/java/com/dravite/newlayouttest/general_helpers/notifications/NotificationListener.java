package com.dravite.newlayouttest.general_helpers.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.dravite.newlayouttest.LauncherLog;

/**
 * Created by Johannes on 16.10.2015.
 * Listens for popping up notifications for displaying numbers on their icons
 */
public class NotificationListener extends NotificationListenerService {

    public class NotificationServiceReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            Intent i = new Intent("com.dravite.homeux.NOTIFICATION_LISTENER");
            try {
                if (getActiveNotifications() == null)
                    return;
                String[] strings = new String[getActiveNotifications().length];
                int[] numbers = new int[getActiveNotifications().length];
                for (int j = 0; j < getActiveNotifications().length; j++) {
                    strings[j] = getActiveNotifications()[j].getPackageName();
                    numbers[j] = getActiveNotifications()[j].getNotification().number;
                }
                i.putExtra("notifications", strings);
                i.putExtra("numbers", numbers);
                sendBroadcast(i);
            } catch (RuntimeException e){
                LauncherLog.w("Notifications", e.getMessage());
            }
        }
    }

    private NotificationServiceReceiver mServiceReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        mServiceReceiver = new NotificationServiceReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.dravite.homeux.NOTIFICATION_LISTENER_SERVICE");
        registerReceiver(mServiceReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mServiceReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Intent i = new Intent("com.dravite.homeux.NOTIFICATION_LISTENER");
        String[] strings = new String[getActiveNotifications().length];
        int[] numbers = new int[getActiveNotifications().length];
        for (int j = 0; j < strings.length; j++) {
            strings[j] = getActiveNotifications()[j].getPackageName();
            numbers[j] = getActiveNotifications()[j].getNotification().number;
        }
        i.putExtra("notifications", strings);
        i.putExtra("numbers", numbers);
        sendBroadcast(i);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Intent i = new Intent("com.dravite.homeux.NOTIFICATION_LISTENER");
        String[] strings = new String[getActiveNotifications().length];
        int[] numbers = new int[getActiveNotifications().length];
        for (int j = 0; j < strings.length; j++) {
            strings[j] = getActiveNotifications()[j].getPackageName();
            numbers[j] = getActiveNotifications()[j].getNotification().number;
        }
        i.putExtra("notifications", strings);
        i.putExtra("numbers", numbers);
        sendBroadcast(i);
    }
}
