package com.muziko.manager;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.ContactsContract;
import android.support.v4.util.LongSparseArray;
import android.telephony.TelephonyManager;

import com.muziko.MyApplication;
import com.muziko.common.models.AddressBookContact;
import com.muziko.common.models.firebase.Person;

import java.util.LinkedList;
import java.util.List;

import io.michaelrocks.libphonenumber.android.NumberParseException;
import io.michaelrocks.libphonenumber.android.PhoneNumberUtil;
import io.michaelrocks.libphonenumber.android.Phonenumber;

/**
 * Created by dev on 4/11/2016.
 */

public class ContactManager {

    private static ContactManager instance;
    private Context mContext;
    private ContactHelperListener mListener;
    private PhoneNumberUtil util = null;
    private String countryCode;
    private Phonenumber.PhoneNumber phoneNumber;
    //no outer class can initialize this class's object
    private ContactManager() {
    }

    public static ContactManager Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new ContactManager();
        }
        return instance;
    }

    public void removeListener() {
        mListener = null;
    }

    public void addListener(ContactHelperListener mListener) {
        this.mListener = mListener;
    }

    public void init(Context context) {
        mContext = context;
        util = PhoneNumberUtil.createInstance(mContext);
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        countryCode = tm.getNetworkCountryIso();
    }

    public void getContacts() {
        MyApplication.phoneContactList.clear();

        List<AddressBookContact> list = new LinkedList<>();
        LongSparseArray<AddressBookContact> array = new LongSparseArray<>();

        String[] projection = {
                ContactsContract.Data.MIMETYPE,
                ContactsContract.Data.CONTACT_ID,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Contactables.DATA,
                ContactsContract.CommonDataKinds.Contactables.TYPE,
        };
        String selection = ContactsContract.Data.MIMETYPE + " in (?, ?)";
        String[] selectionArgs = {
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE,
        };
        String sortOrder = ContactsContract.Contacts.SORT_KEY_ALTERNATIVE;

        Uri uri;
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            uri = ContactsContract.CommonDataKinds.Contactables.CONTENT_URI;
        } else {
            uri = ContactsContract.Data.CONTENT_URI;
        }

// ok, let's work...
        Cursor cursor = mContext.getContentResolver().query(uri, projection, selection, selectionArgs, sortOrder);

        final int mimeTypeIdx = cursor.getColumnIndex(ContactsContract.Data.MIMETYPE);
        final int idIdx = cursor.getColumnIndex(ContactsContract.Data.CONTACT_ID);
        final int nameIdx = cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME);
        final int dataIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.DATA);
        final int typeIdx = cursor.getColumnIndex(ContactsContract.CommonDataKinds.Contactables.TYPE);

        while (cursor.moveToNext()) {
            long id = cursor.getLong(idIdx);
            AddressBookContact addressBookContact = array.get(id);
            if (addressBookContact == null) {
                addressBookContact = new AddressBookContact(id, cursor.getString(nameIdx), mContext.getResources());
                array.put(id, addressBookContact);
                list.add(addressBookContact);
            }
            int type = cursor.getInt(typeIdx);
            String data = cursor.getString(dataIdx);
            String mimeType = cursor.getString(mimeTypeIdx);
            if (mimeType.equals(ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)) {
                // mimeType == ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE
                addressBookContact.addEmail(type, data);
            } else {
                // mimeType == ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                addressBookContact.addPhone(type, data);
            }
        }
        cursor.close();


        for (AddressBookContact addressBookContact : list) {
            if (addressBookContact.getEmails() != null) {
                for (int i = 0; i < addressBookContact.getEmails().size(); i++) {
                    long key = addressBookContact.getEmails().keyAt(i);
                    // get the object by the key.
                    Object obj = addressBookContact.getEmails().get(key);
                    String email = String.valueOf(obj);
                    Person person = new Person(String.valueOf(addressBookContact.getId()), addressBookContact.getName(), "", "", email);
                    MyApplication.phoneContactList.add(person);
                }
            }

            if (addressBookContact.getPhones() != null) {
                for (int i = 0; i < addressBookContact.getPhones().size(); i++) {
                    long key = addressBookContact.getPhones().keyAt(i);
                    // get the object by the key.
                    Object obj = addressBookContact.getPhones().get(key);
                    String phone = String.valueOf(obj);
                    try {
                        phoneNumber = util.parse(phone, countryCode.toUpperCase());
                        phone = util.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.E164);
                    } catch (NumberParseException e) {
//                        Crashlytics.logException(e);

                    }
                    Person person = new Person(String.valueOf(addressBookContact.getId()), addressBookContact.getName(), "", phone, "");
                    MyApplication.phoneContactList.add(person);
                }
            }
        }

        if (mListener != null) {
            mListener.onContactsLoaded();
        }
    }

    public interface ContactHelperListener {

        void onContactsLoaded();
    }
}