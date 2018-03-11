package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

/**
 * Created by dev on 18/10/2016.
 */
@Keep
public class CloudTrack {
    private String uid;
    private String fileName;
    private String url;
    private String data;
    private String title;
    private String artist;
    private String album;
    private long dateModified;
    private String duration;
    private Object timestamp;
    private boolean locked;
    private String lockedBy;
    private String md5;
    private boolean deleted;

    public CloudTrack() {
        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public CloudTrack(
            String uid,
            String fileName,
            String url,
            String data,
            String title,
            String artist,
            String album,
            String duration,
            String md5,
            long dateModified,
            Object timestamp) {
        this.uid = uid;
        this.fileName = fileName;
        this.url = url;
        this.data = data;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.md5 = md5;
        this.dateModified = dateModified;
        this.timestamp = timestamp;

    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
