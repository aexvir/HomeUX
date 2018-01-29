package com.dravite.homeux.drawerobjects.helpers;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;

/**
 * A Utility class for loading Contacts (i.e. for showing them as search results)
 */
public class ContactUtil {

    @SuppressWarnings("unused")
    private static final String TAG = "ContactUtil";

    //Various Contact constants
    private static final String CONTACT_NAME = ContactsContract.Contacts.DISPLAY_NAME;
    private static final String CONTACT_IMG_URI = ContactsContract.Contacts.PHOTO_THUMBNAIL_URI;
    private static final String CONTACT_LOOKUP_KEY = ContactsContract.Contacts.LOOKUP_KEY;
    private static final Uri CONTENT_URI = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
    private static final int REQUEST_READ_CONTACTS = 37;

    /**
     * Gets a list of contacts that either begin with a given String or all of them if the string is null.
     * @param context The current Context
     * @param beginsWith The starting String
     * @return a list of contacts starting with beginsWith
     */
    public static ArrayList<Contact> getContactList(Context context, String beginsWith) {

        if(beginsWith==null || beginsWith.equals("")) return new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (!ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                    Manifest.permission.READ_CONTACTS)) {
                ActivityCompat.requestPermissions((Activity)context,
                        new String[]{Manifest.permission.READ_CONTACTS},
                        REQUEST_READ_CONTACTS);
            }
            return new ArrayList<>();
        }

        ContentResolver cr = context.getContentResolver();

        ArrayList<Contact> alContacts = new ArrayList<>();
        Cursor pCur = cr.query(CONTENT_URI, null, CONTACT_NAME + " LIKE '%" + beginsWith + "%' AND " + ContactsContract.Contacts.IN_VISIBLE_GROUP + "=1", null, CONTACT_NAME + " ASC");
        if (pCur!=null && pCur.moveToFirst()) {
            do {
                String contactId = pCur.getString(pCur.getColumnIndex(CONTACT_LOOKUP_KEY));
                String contactName = pCur.getString(pCur.getColumnIndex(CONTACT_NAME));
                String thumbUriString = pCur.getString(pCur.getColumnIndex(CONTACT_IMG_URI));
                Uri contactThumbUri = thumbUriString==null?null:Uri.parse(thumbUriString);
                Contact contact = new Contact(contactId, contactName, contactThumbUri);
                if(!alContacts.contains(contact))
                    alContacts.add(contact);
            } while (pCur.moveToNext());
        }
        if(pCur!=null)
            pCur.close();
        return alContacts;
    }

    /**
     * Object that represents a Contact including name, key and thumbnail.
     */
    public static class Contact {
        public final String mName;
        public final String mLookupKey;
        public final Uri mThumbnailUri;

        Contact(String id, String name, Uri imgUri){
            mName = name;
            mLookupKey = id;
            mThumbnailUri = imgUri;
        }

        @Override
        public boolean equals(Object o) {
            return o!=null && o instanceof Contact && mLookupKey.equals(((Contact) o).mLookupKey);
        }
    }

}
