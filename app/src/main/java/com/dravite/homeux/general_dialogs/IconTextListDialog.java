package com.dravite.homeux.general_dialogs;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.dravite.homeux.LauncherUtils;

/**
 * A dialog which shows a list of items consisting of a title and an image each.
 */
public class IconTextListDialog {

    private Context mContext;
    private AlertDialog.Builder mDialogBuilder;
    private ItemModifier mItemModifier;
    private IconTextAdapter mIconTextAdapter;

    /**
     * A listener which is triggered when clicking on an item.
     */
    public interface OnDialogSubmitListener{
        void onSubmit(int position);
    }

    /**
     * A generalized abstract form to externally apply a text and an icon alongside its size and tint, both depending on the item position, and the whole item count.
     */
    public static abstract class ItemModifier{

        public String getText(int position){
            return "";
        }

        public Drawable getIcon(int position){
            return null;
        }

        public int[] getIconSize(int position){
            return new int[2];
        }

        public ColorStateList getTint(int position){
            return null;
        }

        public int getCount(){
            return 0;
        }
    }

    /**
     * Create a new Dialog with a given theme ID
     * @param context The current Context
     * @param themeRes The theme resource ID
     */
    public IconTextListDialog(Context context, int themeRes){
        mContext = context;
        mDialogBuilder = new AlertDialog.Builder(context, themeRes);

        mDialogBuilder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        mIconTextAdapter = new IconTextAdapter();
    }

    /**
     * Create a new Dialog like {@link #IconTextListDialog(Context, int)}, while also specifying a title resource ID.
     * @param context The current Context
     * @param themeRes The theme resource ID
     * @param titleRes The title resource ID
     */
    public IconTextListDialog(Context context, int themeRes, int titleRes){
        this(context, themeRes);
        mDialogBuilder.setTitle(titleRes);
    }

    /**
     * Sets the {@link OnDialogSubmitListener} of this Dialog.
     * @param listener The listener
     * @return This Dialog itself for chaining methods
     */
    public IconTextListDialog doOnSubmit(final OnDialogSubmitListener listener){

        mDialogBuilder.setAdapter(mIconTextAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                listener.onSubmit(which);
            }
        });

        return this;
    }

    /**
     * Sets this dialog's {@link ItemModifier} for it's items.
     * @param modifier The ItemModifier to apply
     * @return This Dialog itself for chaining methods
     */
    public IconTextListDialog setItemModifier(ItemModifier modifier){
        mItemModifier = modifier;
        mIconTextAdapter.notifyDataSetChanged();
        return this;
    }

    /**
     * Shows the Dialog by calling {@link AlertDialog.Builder#show()}.
     */
    public void show(){
        mDialogBuilder.show();
    }

    /**
     * An adapter which just shows the items depending on how the enclosing {@link IconTextListDialog}'s ItemModifier is set.
     */
    private class IconTextAdapter extends BaseAdapter{
        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getCount() {
            return mItemModifier.getCount();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View baseView = convertView!=null?convertView:View.inflate(mContext, android.R.layout.simple_list_item_1, null);

            TextView text = (TextView)baseView.findViewById(android.R.id.text1);
            text.setText(mItemModifier.getText(position));
            Drawable icon = mItemModifier.getIcon(position).getConstantState().newDrawable();
            if(icon!=null){
                icon.setBounds(0, 0, LauncherUtils.dpToPx(mItemModifier.getIconSize(position)[0], mContext), LauncherUtils.dpToPx(mItemModifier.getIconSize(position)[1], mContext));
                icon.setTintList(mItemModifier.getTint(position));
                text.setCompoundDrawables(icon, null, null, null);
                text.setCompoundDrawablePadding(LauncherUtils.dpToPx(24, mContext));
            }

            return baseView;
        }
    }
}