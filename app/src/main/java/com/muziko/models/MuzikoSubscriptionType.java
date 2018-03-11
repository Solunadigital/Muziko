package com.muziko.models;

import android.support.annotation.Keep;

/**
 * Created by Bradley on 28/10/2017.
 */

@Keep
public class MuzikoSubscriptionType {
    private String subscriptionTypeID;
    private String subscriptionName;
    private int songLimit;
    private long created;

    public MuzikoSubscriptionType() {
        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public MuzikoSubscriptionType(String subscriptionTypeID, String subscriptionName, int songLimit, long created) {
        this.subscriptionTypeID = subscriptionTypeID;
        this.subscriptionName = subscriptionName;
        this.songLimit = songLimit;
        this.created = created;
    }

    public String getSubscriptionTypeID() {
        return subscriptionTypeID;
    }

    public void setSubscriptionTypeID(String subscriptionTypeID) {
        this.subscriptionTypeID = subscriptionTypeID;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public int getSongLimit() {
        return songLimit;
    }

    public void setSongLimit(int songLimit) {
        this.songLimit = songLimit;
    }

    public long getCreated() {
        return created;
    }

    public void setCreated(long created) {
        this.created = created;
    }
}


