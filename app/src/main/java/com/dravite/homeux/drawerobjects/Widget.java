package com.dravite.homeux.drawerobjects;

import android.content.ComponentName;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.LayoutInflater;
import android.view.View;

import com.dravite.homeux.views.helpers.AppWidgetContainer;
import com.dravite.homeux.views.CustomGridLayout;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * Represents a homescreen widget
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Widget extends DrawerObject implements Serializable{

    public int widgetId;
    public String packageName;
    public String className;

    public Widget(){}

    /**
     * Takes in informations about its position, ID and provider app.
     * @param gridPositioning The position in the grid
     * @param widgetID The ID
     * @param provider The widget provider application
     */
    public Widget(GridPositioning gridPositioning, int widgetID, ComponentName provider){
        super(gridPositioning);
        widgetId = widgetID;
        packageName = provider.getPackageName();
        className = provider.getClassName();
    }

    //Parcelable constructor
    public Widget(Parcel in){
        super(in);
        widgetId = in.readInt();
        packageName = in.readString();
        className = in.readString();
    }

    /**
     * Creates a ComponentName from packageName and class (both parcelable) or returns null if not available.
     * @return The ComponentName of the widget provider
     */
    public ComponentName provider(){
        if(packageName==null||className==null)
            return null;
        return new ComponentName(packageName, className);
    }

    @Override
    public boolean equalType(DrawerObject object) {
        return (object instanceof  Widget) && widgetId==((Widget) object).widgetId && packageName.equals(((Widget) object).packageName) && className.equals(((Widget) object).className);
    }

    @Override
    public DrawerObject copy() {
        return new Widget(mGridPosition.copy(), widgetId, new ComponentName(packageName, className));
    }

    @Override
    public void createView(final CustomGridLayout parent, LayoutInflater inflater, final OnViewCreatedListener listener) {

        //Restore the widget parallelized, so there is much less lag when scrolling
        parent.mAppWidgetContainer.restoreWidget(this, new AppWidgetContainer.OnWidgetCreatedListener() {
            @Override
            public void onWidgetCreated(View widget, int widgetId) {
                widget.setOnLongClickListener(parent);
                listener.onViewCreated(widget);
            }

            @Override
            public void onShortcutCreated(Shortcut shortcut) {

            }
        });
    }

    @Override
    public int getObjectType() {
        return TYPE_WIDGET;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(widgetId);
        dest.writeString(packageName);
        dest.writeString(className);
    }

    @Override
    public int describeContents() {return 0;}

    public static final Parcelable.Creator<Widget> CREATOR
            = new Parcelable.Creator<Widget>() {
        public Widget createFromParcel(Parcel in) {
            return new Widget(in);
        }

        public Widget[] newArray(int size) {
            return new Widget[size];
        }
    };

}
