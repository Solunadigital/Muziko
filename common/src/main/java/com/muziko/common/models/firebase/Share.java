package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

import java.util.ArrayList;

/**
 * Created by dev on 18/10/2016.
 */

@Keep
public class Share {
    private String uid;
    private String senderId;
    private String senderName;
    private String receiverId;
    private String shareUrl;
    private String filename;
    private String title;
    private String artist;
    private String album;
    private Object timestamp;
    private int shareCount;
    private int downloads;
    private boolean friend;
    private String localfile;
    private Object downloaded;
    private boolean notified;
    private ArrayList<Person> receiverList;
    private int type;

    public Share() {
        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public Share(String uid, String senderId, String senderName, String receiverId, String shareUrl, String filename, String title, String artist, String album, Object timestamp, int shareCount, ArrayList<Person> receiverList) {
        this.uid = uid;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.shareUrl = shareUrl;
        this.filename = filename;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.timestamp = timestamp;
        this.shareCount = shareCount;
        this.receiverList = receiverList;
    }

    public ArrayList<Person> getReceiverList() {
        return receiverList;
    }

    public void setReceiverList(ArrayList<Person> receiverList) {
        this.receiverList = receiverList;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(String receiverId) {
        this.receiverId = receiverId;
    }

    public String getShareUrl() {
        return shareUrl;
    }

    public void setShareUrl(String shareUrl) {
        this.shareUrl = shareUrl;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
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

    public int getShareCount() {
        return shareCount;
    }

    public void setShareCount(int shareCount) {
        this.shareCount = shareCount;
    }

    public int getDownloads() {
        return downloads;
    }

    public void setDownloads(int downloads) {
        this.downloads = downloads;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    public String getLocalfile() {
        return localfile;
    }

    public void setLocalfile(String localfile) {
        this.localfile = localfile;
    }

    public Object getDownloaded() {
        if (downloaded == null) {
            return -1;
        } else {
            return downloaded;
        }
    }

    public void setDownloaded(Object downloaded) {
        this.downloaded = downloaded;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}

