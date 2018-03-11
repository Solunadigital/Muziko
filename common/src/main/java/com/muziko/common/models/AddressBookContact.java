package com.muziko.common.models;

import android.content.res.Resources;
import android.provider.ContactsContract;
import android.support.annotation.Keep;
import android.support.v4.util.LongSparseArray;
import android.text.SpannableStringBuilder;

@Keep
public class AddressBookContact {
    private long id;
    private Resources res;
    private String name;
    private LongSparseArray<String> emails;
    private LongSparseArray<String> phones;

    public AddressBookContact(long id, String name, Resources res) {
        this.id = id;
        this.name = name;
        this.res = res;
    }

    @Override
    public String toString() {
        return toString(false);
    }

    private String toString(boolean rich) {
        SpannableStringBuilder builder = new SpannableStringBuilder();
        if (rich) {
            builder.append("id: ").append(Long.toString(id))
                    .append(", name: ").append("\u001b[1m").append(name).append("\u001b[0m");
        } else {
            builder.append(name);
        }

        if (phones != null) {
            builder.append("\n\tphones: ");
            for (int i = 0; i < phones.size(); i++) {
                int type = (int) phones.keyAt(i);
                builder.append(ContactsContract.CommonDataKinds.Phone.getTypeLabel(res, type, ""))
                        .append(": ")
                        .append(phones.valueAt(i));
                if (i + 1 < phones.size()) {
                    builder.append(", ");
                }
            }
        }

        if (emails != null) {
            builder.append("\n\temails: ");
            for (int i = 0; i < emails.size(); i++) {
                int type = (int) emails.keyAt(i);
                builder.append(ContactsContract.CommonDataKinds.Email.getTypeLabel(res, type, ""))
                        .append(": ")
                        .append(emails.valueAt(i));
                if (i + 1 < emails.size()) {
                    builder.append(", ");
                }
            }
        }
        return builder.toString();
    }

    public void addEmail(int type, String address) {
        if (emails == null) {
            emails = new LongSparseArray<>();
        }
        emails.put(type, address);
    }

    public void addPhone(int type, String number) {
        if (phones == null) {
            phones = new LongSparseArray<>();
        }
        phones.put(type, number);
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LongSparseArray<String> getEmails() {
        return emails;
    }

    public LongSparseArray<String> getPhones() {
        return phones;
    }
}