package com.muziko.common.models;

import android.support.annotation.Keep;

import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.Comparator;
import java.util.Random;

@Keep
@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS)

public class QueueItem extends SongModel {
    private static final String TAG = QueueItem.class.getSimpleName();
    public static SortAZComparator sortAZ = new QueueItem.SortAZComparator();
    public static SortZAComparator sortZA = new QueueItem.SortZAComparator();

    public QueueItem() {
        id = 0;
        song = 0;
        album = 0;
        artist = 0;

        title = "";
        artist_name = "";
        album_name = "";

        data = "";
        hash = "";
        level = 0;

        duration = "0";
        date = 0L;
        storage = 0;
        folder_name = "";
        folder_path = "";
        rating = 0;
        order = 0;
        removeafter = 0;
        played = 0;
        lyrics = "";
    }


    public boolean equals(Object o) {
        if (!(o instanceof QueueItem)) return false;
        QueueItem other = (QueueItem) o;
        return (this.data.equals(other.data)
        );
    }


    public void copyQueue(QueueItem item) {
        int min = 0;
        int max = 10000000;
        Random r = new Random();

        id = r.nextInt(max - min + 1) + min;
        song = item.song;
        album = item.album;
        artist = item.artist;
        playlist = item.playlist;

        name = item.name;
        title = item.title;
        artist_name = item.artist_name;
        album_name = item.album_name;
        genre_name = item.genre_name;
        composer = item.composer;

        data = item.data;
        hash = item.hash;

        duration = item.duration;
        date = item.date;

        songs = item.songs;
        url = item.url;
        storage = item.storage;

        //favorited = item.favorited;
        selected = item.selected;

        order = 0;
        removeafter = 0;
        played = 0;
        acrid = item.acrid;
        library = item.library;
        size = item.size;
        md5 = item.md5;
    }

    public void copyPlaylist(PlaylistItem item) {
        id = item.id;
        data = item.data;
        hash = item.hash;
        songs = item.songs;
        title = item.title;
        album = item.album;
        songs = item.songs;
        order = item.order;
        duration = item.duration;
        date = item.date;
    }


    public void hash() {
        String str = id + name + title + album_name + artist_name + data + date;

        int rand = new Random(System.currentTimeMillis()).nextInt(10000);
        hash = Math.abs(str.hashCode()) + "-" + String.valueOf(rand) + "-" + String.valueOf(System.currentTimeMillis());

        //hash = Utils.getSHA1(id + name + title + album_name + artist_name + data + date);
    }

    public static class SortAZComparator implements Comparator<QueueItem> {
        @Override
        public int compare(QueueItem s1, QueueItem s2) {
            return s1.title.compareToIgnoreCase(s2.title);
        }
    }

    public static class SortZAComparator implements Comparator<QueueItem> {
        @Override
        public int compare(QueueItem s1, QueueItem s2) {
            return s2.title.compareToIgnoreCase(s1.title);
        }
    }
}
