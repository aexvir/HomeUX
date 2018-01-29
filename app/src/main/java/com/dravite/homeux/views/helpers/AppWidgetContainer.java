package com.dravite.homeux.views.helpers;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.dravite.homeux.LauncherActivity;
import com.dravite.homeux.LauncherLog;
import com.dravite.homeux.drawerobjects.structures.ClickableAppWidgetHost;
import com.dravite.homeux.drawerobjects.structures.ClickableAppWidgetHostView;
import com.dravite.homeux.drawerobjects.Shortcut;
import com.dravite.homeux.drawerobjects.Widget;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

/**
 * A kind of "container" holding all methods for creating, binding, adding and deleting widgets and shortcuts.
 */
public class AppWidgetContainer {

    private static final String TAG = "AppWidgetContainer";
    private static final int REQUEST_PICK_APPWIDGET = 664;
    private static final int REQUEST_CREATE_APPWIDGET = 665;
    private static final int REQUEST_PICK_SHORTCUT = 764;
    private static final int REQUEST_CREATE_SHORTCUT = 765;
    private static final int REQUEST_BIND_APPWIDGET = 7611;
    private static final int APPWIDGET_HOST_ID = 130;

    public final AppWidgetManager mAppWidgetManager;
    public ClickableAppWidgetHost mAppWidgetHost;
    private AppCompatActivity mParentActivity;
    //private ParallelExecutor mWidgetCreationExecutor;

    //Don't create widget views each time, better cache them
    private final TreeMap<Integer, View> widgetViews = new TreeMap<>();
    //List of widget listeners waiting for binding.
    private final List<WidgetAndListener> mWaitToBind = new ArrayList<>();

    public interface OnWidgetCreatedListener{
        void onWidgetCreated(View widget, int widgetId);
        void onShortcutCreated(Shortcut shortcut);
    }

    static class WidgetAndListener{
        final ComponentName provider;
        final OnWidgetCreatedListener listener;

        WidgetAndListener(ComponentName p, OnWidgetCreatedListener listener){
            this.provider = p;
            this.listener = listener;
        }
    }

    public AppWidgetContainer(Context context){
        mAppWidgetManager = AppWidgetManager.getInstance(context.getApplicationContext());
        mAppWidgetHost = new ClickableAppWidgetHost(context.getApplicationContext(), APPWIDGET_HOST_ID);

        //Lets keep this commented out
        //mAppWidgetHost.deleteHost();

        if(context instanceof AppCompatActivity){
            mParentActivity = (AppCompatActivity)context;
        } else {
            throw new IllegalArgumentException("This AppWidgetContainer needs an AppCompatActivity as its parent activity.");
        }

        //mWidgetCreationExecutor = new ParallelExecutor(4);
    }

    /**
     * Opens an intent with a list to select a widget to add
     */
    public void selectWidget() {
        int appWidgetId = this.mAppWidgetHost.allocateAppWidgetId();
        Intent pickIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_PICK);
        pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        addEmptyData(pickIntent);
        try{
            mParentActivity.startActivityForResult(pickIntent, REQUEST_PICK_APPWIDGET);
        } catch (ActivityNotFoundException e){
            Toast.makeText(mParentActivity, "No widget selection list found.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Start the widget configuration screen
     * @param data widget data
     * @return true when the screen opened properly, false when there is nothing to configure.
     */
    private boolean configureWidget(Intent data, OnWidgetCreatedListener listener) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo =
                mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        if (appWidgetInfo.configure != null) {
            Intent intent =
                    new Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE);
            intent.setComponent(appWidgetInfo.configure);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            mParentActivity.startActivityForResult(intent, REQUEST_CREATE_APPWIDGET);
            return true;
        } else {
            listener.onWidgetCreated(createWidget(data), appWidgetId);
            return false;
        }
    }

    /**
     * Adds placeholder data to the widget editor
     * @param pickIntent The widget picker intent
     */
    private void addEmptyData(Intent pickIntent) {
        ArrayList<AppWidgetProviderInfo> customInfo = new ArrayList<>();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, customInfo);
        ArrayList<Bundle> customExtras = new ArrayList<>();
        pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, customExtras);
    }

    /**
     * Creates a single widget after its selected and configured.
     * @param provider The widget provider
     * @param listener The widget listener which runs after the creation
     */
    private void addWidget(ComponentName provider, OnWidgetCreatedListener listener){
        int newID = mAppWidgetHost.allocateAppWidgetId();
        boolean canBind = mAppWidgetManager.bindAppWidgetIdIfAllowed(newID, provider);

        if(canBind){
            listener.onWidgetCreated(createWidget(newID), newID);
        } else {
            Intent bindIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
            bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newID);
            bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, provider);
            mParentActivity.startActivityForResult(bindIntent, REQUEST_BIND_APPWIDGET);
        }
    }

    /**
     * Restores or inflates a widget and runs a listener afterwards. It runs each creation parallelized while also binding IDs if allowed.
     * @param widget the widget to add
     * @param listener what should happen with the view.
     */
    public void restoreWidget(final Widget widget, final OnWidgetCreatedListener listener){

        if(widgetViews.containsKey(widget.widgetId)){
            //Widget view already loaded.
            View widgetView = widgetViews.get(widget.widgetId);
            listener.onWidgetCreated(widgetView, widget.widgetId);
        } else {
            //Load Widget parallelized
            LauncherActivity.mStaticParallelThreadPool.enqueue(new Runnable() {
                @Override
                public void run() {
                    if(mAppWidgetManager.getAppWidgetInfo(widget.widgetId)!=null){
                        //Woah... I was wrong... the widget actually IS there... sorry for inconveniences...
                        runWidgetListener(widget.widgetId, listener);
                        return;
                    }

                    if(widget.provider()==null){
                        //Nope, no widget app - no widget.
                        widget.widgetId = -1;
                        runWidgetListener(-1, listener);
                        return;
                    }

                    //Allocate a new widget id
                    final int newID = mAppWidgetHost.allocateAppWidgetId();

                    //Try to bind the widget so it can be added.
                    boolean canBind = (widget.provider()==null) || mAppWidgetManager.bindAppWidgetIdIfAllowed(newID, widget.provider());

                    if(canBind){
                        widget.widgetId=newID;
                        runWidgetListener(newID, listener);
                    } else {
                        mWaitToBind.add(new AppWidgetContainer.WidgetAndListener(widget.provider(), listener));
                        if(mWaitToBind.size()==1) {
                            Intent bindIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_BIND);
                            bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, newID);
                            bindIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_PROVIDER, widget.provider());
                            mParentActivity.startActivityForResult(bindIntent, REQUEST_BIND_APPWIDGET);
                        }
                    }
                }
            });
        }

    }

    /**
     * Helper method, for another thread to create a widget view in the UI thread
     * @param id The WidgetID
     * @param listener The OnWidgetCreatedListener
     */
    private void runWidgetListener(final int id, final OnWidgetCreatedListener listener){
        mParentActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View widgetView = createWidget(id);
                widgetViews.put(id, widgetView);
                LauncherLog.d(TAG, "Added widget " + id);
                listener.onWidgetCreated(widgetView, id);
            }
        });
    }

    /**
     * Creates a widget from a given data set.
     * @param data The widget data Intent
     * @return the widget hostView
     */
    private View createWidget(Intent data) {
        Bundle extras = data.getExtras();
        int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
        AppWidgetProviderInfo appWidgetInfo =
                mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        ClickableAppWidgetHostView hostView = (ClickableAppWidgetHostView)
                mAppWidgetHost.createView(mParentActivity, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);
        return hostView;
    }

    /**
     * Creates a widget from a given widget ID.
     * @param appWidgetId The widget ID
     * @return the widget hostView
     */
    public View createWidget(int appWidgetId) {
        if(mAppWidgetHost==null){
            mAppWidgetHost = new ClickableAppWidgetHost(mParentActivity.getApplicationContext(), APPWIDGET_HOST_ID);
        }
        AppWidgetProviderInfo appWidgetInfo =
                mAppWidgetManager.getAppWidgetInfo(appWidgetId);
        ClickableAppWidgetHostView hostView = (ClickableAppWidgetHostView)
                mAppWidgetHost.createView(mParentActivity, appWidgetId, appWidgetInfo);
        hostView.setAppWidget(appWidgetId, appWidgetInfo);

        return hostView;
    }

    /**
     * Deletes a widget and removes it from the AppWidgetHost
     * @param hostView The widget view
     */
    public void removeWidget(ClickableAppWidgetHostView hostView) {
        mAppWidgetHost.deleteAppWidgetId(hostView.getAppWidgetId());
        if(((ViewGroup) hostView.getParent())!=null)
            ((ViewGroup) hostView.getParent()).removeView(hostView);
    }

    /**
     * Start widget listening when starting the activity
     */
    public void onStartActivity(){
        mAppWidgetHost.startListening();
    }

    /**
     * Stop listening when stopping the activity
     */
    public void onStopActivity(){
        mAppWidgetHost.stopListening();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, OnWidgetCreatedListener listener){
        if(requestCode==REQUEST_BIND_APPWIDGET){
            if(resultCode==Activity.RESULT_OK) {

                //Binding widgets is allowed

                int newID = data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

                List<WidgetAndListener> tmp = new ArrayList<>(mWaitToBind);
                if(tmp.size()>=1) {
                    tmp.get(0).listener.onWidgetCreated(createWidget(newID), newID);
                    mWaitToBind.clear();
                    for (int i = 1; i < tmp.size(); i++) {
                        WidgetAndListener wal = tmp.get(i);
                        addWidget(wal.provider, wal.listener);
                        LauncherLog.d(TAG, "onActivityResult: result: " + mWaitToBind.size());
                    }
                }
            } else {
                LauncherLog.w(TAG, "onActivityResult: couldn't add widgets");
            }
        }
        else if (resultCode == Activity.RESULT_OK ) {
            switch (requestCode){
                case REQUEST_PICK_APPWIDGET:
                    //Configure after picking a widget
                    configureWidget(data, listener);
                    break;
                case REQUEST_CREATE_APPWIDGET:
                    //Create after configuring
                    listener.onWidgetCreated(createWidget(data), data.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1));
                    break;
                case REQUEST_PICK_SHORTCUT:
                    //Configure a shortcut after selecting
                    configureShortcut(data);
                    break;
                case REQUEST_CREATE_SHORTCUT:
                    //Create Shortcut after configuring
                    listener.onShortcutCreated(createShortcut(data));
                    break;
            }
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            //Delete widget IDs
            int appWidgetId =
                    data.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);
            if (appWidgetId != -1) {
                mAppWidgetHost.deleteAppWidgetId(appWidgetId);
            }
        }
    }

    /**
     * Start an Intent to select a shortcut from a list
     */
    public void selectShortcut(){
        Intent intent = new Intent(Intent.ACTION_PICK_ACTIVITY);
        intent.putExtra(Intent.EXTRA_INTENT, new Intent(Intent.ACTION_CREATE_SHORTCUT));
        mParentActivity.startActivityForResult(intent, REQUEST_PICK_SHORTCUT);
    }

    /**
     * Configure a shortcut just selected given by its data
     * @param data The shortcut data intent
     */
    private void configureShortcut(Intent data){
        mParentActivity.startActivityForResult(data, REQUEST_CREATE_SHORTCUT);
    }

    /**
     * Create a new Shortcut object.
     * @param intent The Shortcut Intent
     * @return a new Shortcut from this intent
     */
    private Shortcut createShortcut(Intent intent){
        return new Shortcut(intent, mParentActivity);
    }
}
