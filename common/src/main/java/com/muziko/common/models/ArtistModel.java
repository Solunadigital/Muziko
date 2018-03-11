package com.muziko.common.models;

import android.support.annotation.Keep;

@Keep
class ArtistModel {
    private final String icon;
    private final String date;
    private long id;
    private String name;
    private int songs;
    private long album;

    public ArtistModel() {

        id = 0;
        album = 0;
        name = "";
        icon = "";
        songs = 0;
        date = "";
    }

    public ArtistModel(long artistId, long albumId, String artistName, String artistIcon, int artistSongsNumber, String date) {
        this.id = artistId;
        this.id = albumId;
        this.name = artistName;
        this.icon = artistIcon;
        this.songs = artistSongsNumber;
        this.date = date;
    }
}
