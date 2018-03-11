package com.muziko.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 31/08/2016.
 */
public class AlbumArtRealm extends RealmObject {

    @PrimaryKey
    private String key;
    private String artist_name;
    private String album_name;
    private boolean failed;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getArtist_name() {
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }
}