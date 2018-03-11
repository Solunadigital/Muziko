package com.muziko.common.models;

import android.content.ContentValues;
import android.database.Cursor;
import android.support.annotation.Keep;

import java.util.Random;

@Keep
public class PlaylistItem extends SongModel {
    public static final String TAG = PlaylistItem.class.getSimpleName();

    public static final String DB_TABLE = "playlists";

    private static final String KEY_ID = "_id";
    private static final String KEY_TITLE = "title";
    private static final String KEY_DATA = "data";
    private static final String KEY_SONGS = "songs";
    private static final String KEY_ORDER = "order";
    private static final String KEY_DATE = "date";

    public PlaylistItem() {
        id = 0;
        title = "";
        songs = 0;
        order = 0;
    }


    public static PlaylistItem copyQueue(QueueItem queue) {
        PlaylistItem item = new PlaylistItem();

        item.id = queue.id;
        item.title = queue.title;
        item.album = queue.album;
        item.songs = queue.songs;
        item.url = queue.url;
        item.date = queue.date;
        return item;
    }

    public boolean copy(Cursor cursor) {
        boolean ret = false;
        try {
            id = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            title = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            data = cursor.getString(cursor.getColumnIndex(KEY_DATA));
            songs = cursor.getInt(cursor.getColumnIndex(KEY_SONGS));
            order = cursor.getInt(cursor.getColumnIndex(KEY_ORDER));
            date = cursor.getLong(cursor.getColumnIndex(KEY_DATE));

            ret = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return ret;
    }

    public ContentValues getValues() {
        ContentValues values = new ContentValues();

        values.put(KEY_TITLE, title);

        return values;
    }

    public void hash() {
        String str = title + date;

        int rand = new Random(System.currentTimeMillis()).nextInt(10000);
        hash = Math.abs(str.hashCode()) + "-" + String.valueOf(rand) + "-" + String.valueOf(System.currentTimeMillis());

        //hash = Utils.getSHA1(id + name + title + album_name + artist_name + data + date);
    }

}
