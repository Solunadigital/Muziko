package com.muziko.cloud;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;
import com.googlecode.objectify.annotation.Index;

/**
 * The object model for the data we are sending through endpoints
 */
@Entity
public class FileAction {

    @Id
    Long id;

    private CommonConstants.CloudFileActions action;
    @Index
    private String url;
    @Index
    private String actionUser;
    private String waitingUser;
    private long actionTime;

    public FileAction() {
    }

    public CommonConstants.CloudFileActions getAction() {
        return action;
    }

    public void setAction(CommonConstants.CloudFileActions action) {
        this.action = action;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getActionUser() {
        return actionUser;
    }

    public void setActionUser(String actionUser) {
        this.actionUser = actionUser;
    }

    public long getActionTime() {
        return actionTime;
    }

    public void setActionTime(long actionTime) {
        this.actionTime = actionTime;
    }

    public String getWaitingUser() {
        return waitingUser;
    }

    public void setWaitingUser(String waitingUser) {
        this.waitingUser = waitingUser;
    }
}