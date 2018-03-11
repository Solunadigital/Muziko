package com.muziko.models;

import android.support.annotation.Keep;

import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 31/08/2016.
 */
@Keep
public class CloudAccount {

    @PrimaryKey
    private int cloudAccountId;
    private String name;
    private String accountName;
    private String accessToken;
    private int cloudProvider;
    private boolean isDefault;
    private String sharedPrefKey;

    public String getSharedPrefKey() {
        return sharedPrefKey;
    }

    public void setSharedPrefKey(String sharedPrefKey) {
        this.sharedPrefKey = sharedPrefKey;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public void setDefault(boolean aDefault) {
        isDefault = aDefault;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public int getCloudAccountId() {
        return cloudAccountId;
    }

    public void setCloudAccountId(int cloudAccountId) {
        this.cloudAccountId = cloudAccountId;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public int getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(int cloudProvider) {
        this.cloudProvider = cloudProvider;
    }
}