package com.muziko.common.models;

import android.support.annotation.Keep;

@Keep
class AlbumModel {
    private final long id;
    private final String name;
    private final String icon;
    private final int songs;
    private final String date;

    public AlbumModel() {

        id = 0;
        name = "";
        icon = "";
        songs = 0;
        date = "";
    }

    public AlbumModel(long albumId, String albumName, String albumIcon, int albumSongsNumber, String date) {
        this.id = albumId;
        this.name = albumName;
        this.icon = albumIcon;
        this.songs = albumSongsNumber;
        this.date = date;
    }
}
