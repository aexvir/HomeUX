package com.dravite.newlayouttest.general_helpers.notifications;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import com.dravite.newlayouttest.R;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by johannesbraun on 05.06.16.
 * TODO For now just shows up a notification about newly installed apps
 */
public class Notifications {
    public static final int ID_APPS_INSTALLED = 1;

    NotificationManagerCompat mNotificationManager;
    Context mContext;
    Map<Integer, NotificationCompat.Builder> mNotificationBuilders = new TreeMap<>();

    public Notifications(Context context){
        mContext = context;
        mNotificationManager = NotificationManagerCompat.from(context);
        mNotificationBuilders.put(ID_APPS_INSTALLED, (new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.ic_apps_black_24dp)
                .setColor(ContextCompat.getColor(context, R.color.colorPrimary))
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher))));
    }

    /**
     * Shows or updates the notification.
     * @param id The notification ID to determine which notification should be shown/updated. TODO: For now only {@link #ID_APPS_INSTALLED}.
     * @param value A generalized Value object. I.e. the number of newly installed apps.
     */
    public void show(int id, Object value){
        switch (id){
            case ID_APPS_INSTALLED:
                mNotificationBuilders.get(ID_APPS_INSTALLED)
                    .setContentTitle(((int)value) + " new app" + (((int)value)==1?"":"s") + " detected")
                    .setContentText("Click to put them into folders.");
                mNotificationManager.notify(ID_APPS_INSTALLED, mNotificationBuilders.get(ID_APPS_INSTALLED).build());
                break;
            default:
                break;
        }
    }

}
