package com.dravite.newlayouttest.views;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.dravite.newlayouttest.LauncherActivity;
import com.dravite.newlayouttest.LauncherUtils;
import com.dravite.newlayouttest.R;
import com.dravite.newlayouttest.drawerobjects.DrawerObject;
import com.dravite.newlayouttest.drawerobjects.structures.ClickableAppWidgetHostView;
import com.dravite.newlayouttest.drawerobjects.structures.FolderStructure;

import static android.view.View.MeasureSpec.EXACTLY;
import static android.view.View.MeasureSpec.makeMeasureSpec;


/**
 * Created by Johannes on 05.09.2015.<br/>
 * This ViewGroup contains one or more button-like Views for the user to drop objects like apps or widgets on.<br/>
 * Always contains a "remove" button.
 */
public class ObjectDropButtonStrip extends ViewGroup {

    private WidgetRemoveListener mListener;

    //Listener for like everything
    public interface WidgetRemoveListener{
        void removeWidget(DrawerObject data, ClickableAppWidgetHostView widgetHostView, int folderIndex, int pageIndex); //Widget is dropped on the remove button
        void removeOther(DrawerObject data, View widgetHostView, int folderIndex, int pageIndex); //Other thing is dropped on the remove button (App or Shortcut)
        void removeFolder(View button, FolderStructure.Folder folderName); //Folder is dropped on the remove button
        void editFolder(View button, FolderStructure.Folder folderName); //Folder is dropped on the edit button
        void doAction(DrawerObject data, View view, String action, int folderIndex, int pageIndex, CustomGridLayout parent); //Something is dropped on some other button
        void hovers(DrawerObject data, int x, int y, int overChildIndex); //Called when a drag is hovering over this bar.
        void notHovering(); //Called when nothing is hovering over here.
    }

    public ObjectDropButtonStrip(Context context){
        this(context, null);
    }

    public ObjectDropButtonStrip(Context context, AttributeSet attrs){
        this(context, attrs, 0);
    }

    public ObjectDropButtonStrip(Context context, AttributeSet attrs, int defStyleAttr){
        this(context, attrs, defStyleAttr, 0);
    }

    public ObjectDropButtonStrip(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes){
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setWidgetRemoveListener(WidgetRemoveListener listener){
        mListener = listener;
    }

    /**
     * @return a Point containing coordinates from {@link #getLocationInWindow(int[])}.
     */
    public Point getPosition(){
        int[] pos = new int[2];
        getLocationInWindow(pos);
        return new Point(pos[0], pos[1]);
    }

    /**
     * @param x absolute x position
     * @param y absolute y position
     * @return true, if the given position is inside the strip
     */
    public boolean isContaining(int x, int y){
        Rect viewRect = new Rect();
        getHitRect(viewRect);
        return viewRect.contains(x, y);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setPivotY(0);
        setPivotX(getMeasuredWidth() / 2f);
    }

    /**
     * Adds a button to the strip
     * @param label The button label
     * @param command A command to be referred to when dropped. (like "remove")
     * @param icon The button icon drawable
     */
    public void addButton(String label, String command, Drawable icon){
        TextView button1 = new TextView(getContext());
        icon.setTint(0xff000000);
        icon.setAlpha((int)(255*0.57f));
        icon.setBounds(0, 0, LauncherUtils.dpToPx(24, getContext()), LauncherUtils.dpToPx(24, getContext()));
        button1.setCompoundDrawablesRelative(null, icon, null, null);
        button1.setText(label);
        button1.setTag(command);
        button1.setPadding(LauncherUtils.dpToPx(4, getContext()), LauncherUtils.dpToPx(16, getContext()), LauncherUtils.dpToPx(4, getContext()), LauncherUtils.dpToPx(4, getContext()));
        button1.setGravity(Gravity.CENTER);
        button1.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        addView(button1);
    }

    /**
     * Removes all buttons
     */
    public void wipeAllButtons(){
        removeAllViews();
    }

    /**
     * Removes all buttons except the "remove" button
     */
    public void wipeButtons(){
        removeAllViews();
        addButton("Remove", "remove", getContext().getDrawable(R.drawable.ic_remove_black_24dp));
    }

    /**
     * Sets the icon for the remove button.
     * @param icon
     */
    public void setRemoveIcon(Drawable icon){
        TextView button = (TextView) findViewWithTag("remove");
        icon.setTint(0xff000000);
        icon.setAlpha((int)(255*0.57f));
        icon.setBounds(0, 0, LauncherUtils.dpToPx(24, getContext()), LauncherUtils.dpToPx(24, getContext()));
        button.setCompoundDrawablesRelative(null, icon, null, null);
    }

    /**
     * Sets the text for the remove button
     * @param text usually "remove" or "uninstall"
     */
    public void setRemoveText(String text){
        TextView button = (TextView) findViewWithTag("remove");
        button.setText(text);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);

            int childWidth = (right-left)/getChildCount();
            int x = i*childWidth;

            if (childWidth != child.getMeasuredWidth()) {
                child.measure(makeMeasureSpec(childWidth, EXACTLY), makeMeasureSpec(bottom-top, EXACTLY));
            }

            child.layout(x, 0, x + childWidth, bottom-top);
        }
    }

    /**
     * Notifies, that a drag has left the strip or is not hovering at all anymore
     */
    public void exitHover(){
        mListener.notHovering();
    }

    /**
     * Tells that the user hovers over the strip with a {@link DrawerObject}. Sets the button where its hovering to an "active" state with a different background color.
     * @param data The dragged object.
     * @param absX absolute x position
     * @param absY absolute y position
     * @return the action command of the hovered button
     */
    public String doHover(DrawerObject data, int absX, int absY){
        String command = "nothing";
        if(mListener==null) {
            exitHover();
            return command;
        }

        if(isContaining(absX, absY)){
            Point pos = getPosition();
            View child = getChildAt(absX-pos.x, absY-pos.y);
            if (child==null || child.getTag()==null) {
                exitHover();
                return command;
            }
            mListener.hovers(data, absX - pos.x, absY - pos.y, child == null ? -1 : indexOfChild(child));
            return child.getTag().toString();
        }
        exitHover();
        return command;
    }

    /**
     * @param view the dragged view
     * @param folder The folder assigned to the dragged view
     * @param command the last known button command for this hover
     * @return true, if the button should be removed or edited.
     */
    public boolean doRemoveFolder(View view, FolderStructure.Folder folder, String command){
        if(command.equals("remove")){
            mListener.removeFolder(view, folder);
            return true;
        } else if (command.equals("editFolder")){
            mListener.editFolder(view, folder);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Like {@link #doRemoveFolder(View, FolderStructure.Folder, String)}.
     * @param data The data assigned to the dragged view
     * @param view The dragged view
     * @param command the last known button command for this hover
     * @param parent The parent GridLayout of the DrawerObject
     * @param focusPager The parent ViewPager of the GridLayout of the DrawerObject
     * @param folderIndex The DrawerObject's folder index
     * @param pageIndex The DrawerObject's page number inside its folder
     * @param animateIfRemoved true, to do a scaling animation when removing
     * @param doAfterAnimation runs after the animation has been finished.
     * @param animToTop true, to indicate a "fling-removal", throwing the object to the top.
     * @return true, if there was any action command.
     */
    public boolean doRemove(final DrawerObject data, final View view, final String command, final CustomGridLayout parent, final ViewPager focusPager, final int folderIndex, final int pageIndex, boolean animateIfRemoved, @Nullable final Runnable doAfterAnimation, boolean animToTop){

        if(view instanceof ClickableAppWidgetHostView){
            if(command.equals("remove")){
                if(animateIfRemoved){
                    if(animToTop){
                        view.animate().scaleX(0).scaleY(0).alpha(0).y(LauncherUtils.dpToPx(120, getContext())).x(0).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                if (doAfterAnimation != null) doAfterAnimation.run();
                                mListener.removeWidget(data, (ClickableAppWidgetHostView) view, folderIndex, pageIndex);
                            }
                        });
                    } else {
                        view.animate().scaleX(0).scaleY(0).alpha(0).setDuration(150).withEndAction(new Runnable() {
                            @Override
                            public void run() {
                                if (doAfterAnimation != null) doAfterAnimation.run();
                                mListener.removeWidget(data, (ClickableAppWidgetHostView) view, folderIndex, pageIndex);
                            }
                        });
                    }
                } else {
                    mListener.removeWidget(data, (ClickableAppWidgetHostView) view, folderIndex, pageIndex);
                }
                return true;
            } else if(command.equals("nothing")){
                return false;
            }
            else{
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.doAction(data, view, command, folderIndex, pageIndex, parent);
                    }
                }, 50);
                return (command.contains("appAction")&& ((LauncherActivity) getContext()).isInAllFolder());
            }
        } else {
            if(command.equals("remove")){
                if(view!=null) {
                    if(animateIfRemoved){
                        if(animToTop){
                            view.animate().scaleX(0).scaleY(0).y(LauncherUtils.dpToPx(120, getContext())).x(0).alpha(0).setDuration(150).setInterpolator(new DecelerateInterpolator()).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (doAfterAnimation != null) doAfterAnimation.run();
                                    if (data != null && ((LauncherActivity) getContext()).mPager.getCurrentItem() == ((LauncherActivity) getContext()).mFolderStructure.folders.indexOf(((LauncherActivity) getContext()).mFolderStructure.getFolderWithName("All"))) {
                                        LauncherActivity.mFolderStructure.folders.get(((LauncherActivity) getContext()).mPager.getCurrentItem())
                                                .pages.get(focusPager.getCurrentItem()).add(parent.mDragData);
                                        data.createView(parent, (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), new DrawerObject.OnViewCreatedListener() {
                                            @Override
                                            public void onViewCreated(View view) {
                                                parent.addObject(view, data);
                                            }
                                        });
                                    }
                                    mListener.removeOther(data, view, folderIndex, pageIndex);
                                }
                            });
                        } else {
                            view.animate().scaleX(0).scaleY(0).alpha(0).setDuration(150).withEndAction(new Runnable() {
                                @Override
                                public void run() {
                                    if (doAfterAnimation != null) doAfterAnimation.run();
                                    if (data != null && ((LauncherActivity) getContext()).mPager.getCurrentItem() == ((LauncherActivity) getContext()).mFolderStructure.folders.indexOf(((LauncherActivity) getContext()).mFolderStructure.getFolderWithName("All"))) {
                                        LauncherActivity.mFolderStructure.folders.get(((LauncherActivity) getContext()).mPager.getCurrentItem())
                                                .pages.get(focusPager.getCurrentItem()).add(parent.mDragData);
                                        data.createView(parent, (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), new DrawerObject.OnViewCreatedListener() {
                                            @Override
                                            public void onViewCreated(View view) {
                                                parent.addObject(view, data);
                                            }
                                        });
                                    }
                                    mListener.removeOther(data, view, folderIndex, pageIndex);
                                }
                            });
                        }
                    } else {
                        if (data != null && ((LauncherActivity) getContext()).mPager.getCurrentItem() == ((LauncherActivity) getContext()).mFolderStructure.folders.indexOf(((LauncherActivity) getContext()).mFolderStructure.getFolderWithName("All"))) {
                            LauncherActivity.mFolderStructure.folders.get(((LauncherActivity) getContext()).mPager.getCurrentItem())
                                    .pages.get(focusPager.getCurrentItem()).add(parent.mDragData);
                            data.createView(parent, (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), new DrawerObject.OnViewCreatedListener() {
                                @Override
                                public void onViewCreated(View view) {
                                    parent.addObject(view, data);
                                }
                            });
                        }
                        mListener.removeOther(data, view, folderIndex, pageIndex);
                    }
                }
                else
                    mListener.removeOther(data, null, folderIndex, pageIndex);
                return true;
            }

            else if(command.equals("nothing"))

            {
                return false;
            }

            else

            {
                if(command.equals("justRemove") && view!=null) {
                    if (((LauncherActivity) getContext()).mPager.getCurrentItem() == ((LauncherActivity) getContext()).mFolderStructure.folders.indexOf(((LauncherActivity) getContext()).mFolderStructure.getFolderWithName("All"))) {
                        ((LauncherActivity) getContext()).mFolderStructure.folders.get(((LauncherActivity) getContext()).mPager.getCurrentItem())
                                .pages.get(focusPager.getCurrentItem()).add(parent.mDragData);

                        data.createView(parent, (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE), new DrawerObject.OnViewCreatedListener() {
                            @Override
                            public void onViewCreated(View view) {
                                parent.addObject(view,data);
                            }
                        });

                    }
                }
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        mListener.doAction(data, view, command, folderIndex, pageIndex, parent);
                    }
                }, 50);
                return (command.contains("appAction")&& ((LauncherActivity) getContext()).isInAllFolder());
            }
        }
    }

    /**
     * @param x absolute x position
     * @param y absolute y position
     * @return the view under the given position, or null if there is none.
     */
    public View getChildAt(int x, int y) {
        for (int i = 0; i < getChildCount(); i++) {
            View child = getChildAt(i);
            Rect rect = new Rect();
            child.getHitRect(rect);
            if(rect.contains(x, y))
                return child;
        }
        return null;
    }
}
