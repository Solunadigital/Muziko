package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

/**
 * Created by dev on 23/10/2016.
 */
@Keep
public class Contact {
    private String uid;
    private boolean blocked;
    private Object timestamp;

    public Contact() {

    }

    public Contact(String uid, boolean blocked, Object timestamp) {
        this.uid = uid;
        this.blocked = blocked;
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}