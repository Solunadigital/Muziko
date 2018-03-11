package com.muziko.common.models;

import android.support.annotation.Keep;

@Keep
class GenreModel {
    private final long id;
    private final long album;
    private final String name;
    private final String icon;
    private final int songs;
    private final String date;

    public GenreModel() {

        id = 0;
        album = 0;
        name = "";
        icon = "";
        songs = 0;
        date = "";
    }

    public GenreModel(long genreId, long genreAlbumId, String genreName, String genreIcon, int genreSongsNumber, String date) {
        this.id = genreId;
        this.album = genreAlbumId;
        this.name = genreName;
        this.icon = genreIcon;
        this.songs = genreSongsNumber;
        this.date = date;
    }
}
