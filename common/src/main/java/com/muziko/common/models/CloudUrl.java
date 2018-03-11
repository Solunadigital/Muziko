package com.muziko.common.models;

import android.support.annotation.Keep;

/**
 * Created by Bradley on 29/08/2017.
 */
@Keep
public class CloudUrl {

    private String cloudId;
    private int cloudProvider;
    private String url;
    private Long created;

    public CloudUrl(String cloudId, int cloudProvider, String url, Long created) {
        this.cloudId = cloudId;
        this.cloudProvider = cloudProvider;
        this.url = url;
        this.created = created;
    }

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
