package com.muziko.models;

import android.support.annotation.Keep;

import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 31/08/2016.
 */
@Keep
public class StorageFolder {

    @PrimaryKey
    private int uid;
    private int cloudAccountId;
    private String path;
    private String folder_path;
    private String title;
    private boolean homeFolder;
    private boolean excludedFolder;
    private boolean hiddenFolder;

    public String getFolder_path() {
        return folder_path;
    }

    public void setFolder_path(String folder_path) {
        this.folder_path = folder_path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getUid() {
        return uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public int getCloudAccountId() {
        return cloudAccountId;
    }

    public void setCloudAccountId(int cloudAccountId) {
        this.cloudAccountId = cloudAccountId;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isHomeFolder() {
        return homeFolder;
    }

    public void setHomeFolder(boolean homeFolder) {
        this.homeFolder = homeFolder;
    }

    public boolean isExcludedFolder() {
        return excludedFolder;
    }

    public void setExcludedFolder(boolean excludedFolder) {
        this.excludedFolder = excludedFolder;
    }

    public boolean isHiddenFolder() {
        return hiddenFolder;
    }

    public void setHiddenFolder(boolean hiddenFolder) {
        this.hiddenFolder = hiddenFolder;
    }
}