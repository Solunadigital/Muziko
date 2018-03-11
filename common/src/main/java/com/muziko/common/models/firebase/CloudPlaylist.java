package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by dev on 18/10/2016.
 */

@Keep
public class CloudPlaylist {
    private String uid;
    private long playlistid;
    private String title;
    private String duration;
    private long dateModified;
    private List<String> cloudTracks = new ArrayList<>();
    private Object timestamp;
    private boolean deleted;

    public CloudPlaylist() {

        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public CloudPlaylist(String uid, long playlistid, String title, String duration, long dateModified, List<String> cloudTracks) {
        this.uid = uid;
        this.playlistid = playlistid;
        this.title = title;
        this.duration = duration;
        this.dateModified = dateModified;
        this.cloudTracks = cloudTracks;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public long getPlaylistid() {
        return playlistid;
    }

    public void setPlaylistid(long playlistid) {
        this.playlistid = playlistid;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public List<String> getCloudTracks() {
        return cloudTracks;
    }

    public void setCloudTracks(List<String> cloudTracks) {
        this.cloudTracks = cloudTracks;
    }

    public void addCloudTrack(String cloudTrack) {
        this.cloudTracks.add(cloudTrack);
    }

    public void removeCloudTrack(CloudTrack cloudTrack) {
        this.cloudTracks.remove(cloudTrack);
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}

