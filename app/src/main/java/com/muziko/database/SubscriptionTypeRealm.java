package com.muziko.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Bradley on 28/10/2017.
 */


public class SubscriptionTypeRealm extends RealmObject {
    @PrimaryKey
    private String subscriptionTypeID;
    private String subscriptionName;
    private int songLimit;
    private long created;

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


