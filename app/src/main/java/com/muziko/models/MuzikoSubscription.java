package com.muziko.models;

import android.support.annotation.Keep;

import com.anjlab.android.iab.v3.PurchaseInfo;

/**
 * Created by Bradley on 28/10/2017.
 */

@Keep
public class MuzikoSubscription {
    private String subscriptionTypeID;
    private String orderId;
    private String purchaseToken;
    private long purchaseTime;
    private PurchaseInfo purchaseInfo;

    public MuzikoSubscription() {
        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public MuzikoSubscription(String subscriptionTypeID, String orderId, String purchaseToken, long purchaseTime, PurchaseInfo purchaseInfo) {
        this.subscriptionTypeID = subscriptionTypeID;
        this.orderId = orderId;
        this.purchaseToken = purchaseToken;
        this.purchaseTime = purchaseTime;
        this.purchaseInfo = purchaseInfo;
    }

    public String getSubscriptionTypeID() {
        return subscriptionTypeID;
    }

    public void setSubscriptionTypeID(String subscriptionTypeID) {
        this.subscriptionTypeID = subscriptionTypeID;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getPurchaseToken() {
        return purchaseToken;
    }

    public void setPurchaseToken(String purchaseToken) {
        this.purchaseToken = purchaseToken;
    }

    public long getPurchaseTime() {
        return purchaseTime;
    }

    public void setPurchaseTime(long purchaseTime) {
        this.purchaseTime = purchaseTime;
    }

    public PurchaseInfo getPurchaseInfo() {
        return purchaseInfo;
    }

    public void setPurchaseInfo(PurchaseInfo purchaseInfo) {
        this.purchaseInfo = purchaseInfo;
    }

}

