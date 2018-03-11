package com.muziko.common.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Keep;

@Keep
public class PlaylistQueueItem extends SongModel {
    public static final String DB_TABLE = "playlists_queue";
    private static final String TAG = PlaylistQueueItem.class.getSimpleName();
    private static final String KEY_ID = "_id";
    private static final String KEY_SONG = "song";
    private static final String KEY_ALBUM = "album";
    private static final String KEY_PLAYLIST = "playlist";

    private static final String KEY_TITLE = "title";
    private static final String KEY_ARTIST_NAME = "artist_name";
    private static final String KEY_ALBUM_NAME = "album_name";

    private static final String KEY_DATA = "data";

    private static final String KEY_DURATION = "duration";
    private static final String KEY_DATE = "date";
    private static final String KEY_URL = "URL";

    public PlaylistQueueItem() {
        id = 0;
        song = 0;
        album = 0;
        playlist = 0;

        title = "";
        artist_name = "";
        album_name = "";

        data = "";

        duration = "0";
        date = 0L;
        url = "";
    }

    public PlaylistQueueItem(SongModel item) {
        id = item.id;
        song = item.song;
        album = item.album;
        playlist = item.playlist;

        title = item.title;
        artist_name = item.artist_name;
        album_name = item.album_name;

        data = item.data;

        duration = item.duration;
        date = item.date;
        url = item.url;
    }


    public boolean copy(Cursor cursor) {
        boolean ret = false;
        try {
            id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            song = cursor.getLong(cursor.getColumnIndex(KEY_SONG));
            album = cursor.getLong(cursor.getColumnIndex(KEY_ALBUM));
            playlist = cursor.getLong(cursor.getColumnIndex(KEY_PLAYLIST));

            title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            artist_name = cursor.getString(cursor.getColumnIndex(KEY_ARTIST_NAME));
            album_name = cursor.getString(cursor.getColumnIndex(KEY_ALBUM_NAME));

            data = cursor.getString(cursor.getColumnIndex(KEY_DATA));

            duration = cursor.getString(cursor.getColumnIndex(KEY_DURATION));
            date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));
            url = cursor.getString(cursor.getColumnIndex(KEY_URL));

            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();

        values.put(KEY_SONG, song);
        values.put(KEY_ALBUM, album);
        values.put(KEY_PLAYLIST, playlist);

        values.put(KEY_TITLE, title);
        values.put(KEY_ARTIST_NAME, artist_name);
        values.put(KEY_ALBUM_NAME, album_name);

        values.put(KEY_DATA, data);

        values.put(KEY_DURATION, duration);
        values.put(KEY_DATE, date);
        values.put(KEY_URL, url);

        return values;
    }


    public void copyQueue(QueueItem item) {
        id = item.id;
        song = item.song;
        album = item.album;

        title = item.title;
        artist_name = item.artist_name;
        album_name = item.album_name;

        data = item.data;

        duration = item.duration;
        date = item.date;
        url = item.url;
    }
}
