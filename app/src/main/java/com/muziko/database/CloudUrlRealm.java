package com.muziko.database;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 17/07/2016.
 */
public class CloudUrlRealm extends RealmObject {

    @PrimaryKey
    private String cloudId;
    @Index
    private int cloudProvider;
    private String url;
    private Long created;

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public int getCloudProvider() {
        return cloudProvider;
    }

    public void setCloudProvider(int cloudProvider) {
        this.cloudProvider = cloudProvider;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Long getCreated() {
        return created;
    }

    public void setCreated(Long created) {
        this.created = created;
    }
}
