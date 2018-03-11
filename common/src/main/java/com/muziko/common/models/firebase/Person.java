package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

/**
 * Created by dev on 23/10/2016.
 */
@Keep
public class Person {
    private String uid;
    private String displayName;
    private String photoUrl;
    private String phone;
    private String email;
    private boolean connected;
    private Object lastOnline;
    private boolean blocked;
    private boolean friend;

    public Person() {

    }

    public Person(String uid, String displayName, String photoUrl, String phone, String email) {
        this.uid = uid;
        this.displayName = displayName;
        this.photoUrl = photoUrl;
        this.phone = phone;
        this.email = email;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Person)) return false;

        Person friend = (Person) o;

        return uid.equals(friend.uid);

    }

    public String getUid() {
        if (uid == null) {
            return "";
        } else {
            return uid;
        }
    }


    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhotoUrl() {
        if (photoUrl == null) {
            return "";
        } else {
            return photoUrl;
        }
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getPhone() {
        if (phone == null) {
            return "";
        } else {
            return phone;
        }
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        if (email == null) {
            return "";
        } else {
            return email;
        }
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isConnected() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    public Object getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Object lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean isBlocked() {
        return blocked;
    }

    public void setBlocked(boolean blocked) {
        this.blocked = blocked;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

}