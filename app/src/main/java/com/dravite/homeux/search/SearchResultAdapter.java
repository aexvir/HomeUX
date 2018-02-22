package com.dravite.homeux.search;

import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.dravite.homeux.LauncherUtils;
import com.dravite.homeux.R;
import com.dravite.homeux.drawerobjects.Application;
import com.dravite.homeux.drawerobjects.helpers.ContactUtil;
import com.dravite.homeux.views.AppIconView;
import com.dravite.homeux.views.RoundImageView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Johannes on 23.10.2015.
 * This adapter shows a bunch of search results in the launcher.
 */
public class SearchResultAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_APP = 0;       //Results for found Apps
    public static final int VIEW_SEPARATOR = 1; //Separator between Apps, Websearch and Contacts
    public static final int VIEW_CONTACT = 2;   //Results for found Contacts
    public static final int VIEW_WEBSEARCH = 3; //A simple button re-routing to the browser doing a google search
    
    ///////////////////////////////////////////////////////////////////////////
    // Several ViewHolders for different types.
    ///////////////////////////////////////////////////////////////////////////

    public static class SearchResultViewHolder extends RecyclerView.ViewHolder{
        public SearchResultViewHolder(Context context){
            super(new FrameLayout(context));
        }
    }

    public static class SearchSeparatorViewHolder extends RecyclerView.ViewHolder{
        public SearchSeparatorViewHolder(Context context, ViewGroup parent){
            super(LayoutInflater.from(context).inflate(R.layout.search_result_separator, parent, false));
        }
    }

    public static class SearchWebSearchViewHolder extends RecyclerView.ViewHolder{
        public SearchWebSearchViewHolder(Context context, ViewGroup parent){
            super(LayoutInflater.from(context).inflate(R.layout.search_result_websearch, parent, false));
            clickSpace = itemView.findViewById(R.id.search_button);
            searchText = (TextView)itemView.findViewById(R.id.searchText);
        }

        View clickSpace;
        TextView searchText;
    }

    public static class SearchContactViewHolder extends RecyclerView.ViewHolder{
        public SearchContactViewHolder(Context context, ViewGroup parent){
            super(LayoutInflater.from(context).inflate(R.layout.search_result_contact, parent, false));
            nameLabel = (TextView)itemView.findViewById(R.id.contact_name);
            image = (RoundImageView)itemView.findViewById(R.id.contact_img);
        }

        TextView nameLabel;
        RoundImageView image;
    }

    ///////////////////////////////////////////////////////////////////////////
    // Vars
    ///////////////////////////////////////////////////////////////////////////

    private final Context mContext;
    private final List<Application> mQueryResult = new ArrayList<>();
    private final List<ContactUtil.Contact> mQueryResultContacts = new ArrayList<>();
    private String mQuery = "";

    public SearchResultAdapter(Context context){
        this.mContext = context;
    }

    public void setQuery(String query){
        mQuery = query;
        notifyItemChanged(0);
    }

    /**
     * Adds a list of applications to the query results
     * @param application
     */
    public void addToQueryResult(Collection<Application> application){
        mQueryResult.addAll(application);
        notifyItemRangeChanged(0, mQueryResult.size());
    }

    /**
     * Adds a list of contacts to the Query results
     * @param contacts
     */
    public void addToContactsQueryResult(Collection<ContactUtil.Contact> contacts){
        mQueryResultContacts.addAll(contacts);
        notifyItemRangeChanged(mQueryResult.size()+1, mQueryResultContacts.size()+1);
    }

    /**
     * Removes all items from this Adapter
     */
    public void clearQuery(){
        mQueryResult.clear();
        mQueryResultContacts.clear();
        notifyDataSetChanged();
        mQuery = "";
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType){
            case VIEW_APP: return new SearchResultViewHolder(mContext);
            case VIEW_SEPARATOR: return new SearchSeparatorViewHolder(mContext, parent);
            case VIEW_CONTACT: return new SearchContactViewHolder(mContext, parent);
            case VIEW_WEBSEARCH: return new SearchWebSearchViewHolder(mContext, parent);
            default: return new SearchResultViewHolder(mContext);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        switch (getItemViewType(position)){
            case VIEW_WEBSEARCH:
                ((SearchWebSearchViewHolder)holder).clickSpace.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startWebSearch();
                    }
                });
                String viewText = "Websearch for \"" + mQuery + "\"";
                ((SearchWebSearchViewHolder) holder).searchText.setText(viewText);
                break;
            case VIEW_APP:
                Application thisApp = mQueryResult.get(position-1);
                FrameLayout appContent = ((FrameLayout) holder.itemView);

                GridLayoutManager.LayoutParams params = new GridLayoutManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LauncherUtils.dpToPx(84, mContext));

                appContent.removeAllViews();
                AppIconView iconView = (AppIconView) thisApp.createDefaultView(mContext);
                iconView.overrideData(56);
                iconView.setIconSizeInDP(56);
                iconView.setIcon(iconView.getIcon());
                appContent.addView(iconView);
                appContent.setLayoutParams(params);
                appContent.setBackgroundColor(Color.WHITE);
                break;
            case VIEW_CONTACT:
                final SearchContactViewHolder cHolder = ((SearchContactViewHolder) holder);
                final ContactUtil.Contact cContact = mQueryResultContacts.get(getContactIndex(position));
                if(cContact.mThumbnailUri!=null) {
                    cHolder.image.setImageTintList(null);
                    cHolder.image.setImageURI(cContact.mThumbnailUri);
                    cHolder.image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                } else {
                    cHolder.image.setImageTintList(ColorStateList.valueOf(Color.WHITE));
                    cHolder.image.setBackgroundColor(0xff2196F3);
                    cHolder.image.setImageResource(R.drawable.ic_person_black_24dp);
                    cHolder.image.setScaleType(ImageView.ScaleType.CENTER);
                }
                cHolder.nameLabel.setText(cContact.mName);

                cHolder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        Uri look = Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_LOOKUP_URI, cContact.mLookupKey);
                        intent.setData(look);
                        mContext.startActivity(intent);
                    }
                });
                break;
            case VIEW_SEPARATOR: break;
            default: break;
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(position==0) return VIEW_WEBSEARCH;
        else if(position<mQueryResult.size()+1) return VIEW_APP;
        else if(!mQueryResult.isEmpty() && position==mQueryResult.size()+1) return VIEW_SEPARATOR;
        else return VIEW_CONTACT;
    }

    @Override
    public int getItemCount() {
        if(mQuery.equals("")) return 0;
        else if(mQueryResult.isEmpty() && mQueryResultContacts.isEmpty()) return 1;
        else if(mQueryResultContacts.isEmpty()) return 1+mQueryResult.size();
        else if(mQueryResult.isEmpty()) return 1+mQueryResultContacts.size();
        else return 2 + mQueryResult.size() + mQueryResultContacts.size();
    }

    /**
     * @return Translates the Adapter position to the index of the contact in its query result list
     */
    public int getContactIndex(int position){
        if(mQueryResult.isEmpty()) return position-1;
        else return position-1-1-mQueryResult.size();
    }

    /**
     * @return true, if there are no search results
     */
    public boolean hasEmptyQueries(){
        return mQueryResult.isEmpty() && mQueryResultContacts.isEmpty() && !mQuery.equals("");
    }

    /**
     * Opens a browser intent for a google search
     */
    public void startWebSearch(){
        Uri uri = Uri.parse("http://www.google.com/#q=" + mQuery);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        mContext.startActivity(intent);
    }
}
