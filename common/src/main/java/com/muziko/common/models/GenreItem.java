package com.muziko.common.models;

import android.support.annotation.Keep;

@Keep
class GenreItem extends SongModel {
    public static final String TAG = GenreItem.class.getSimpleName();
/*
    public static final String DB_TABLE             = "queue";

    public static final String KEY_ID               = "_id";
    public static final String KEY_SONG             = "song";
    public static final String KEY_ALBUM            = "album";
    public static final String KEY_ARTIST           = "artist";

    public static final String KEY_TITLE            = "title";
    public static final String KEY_ARTIST_NAME      = "artist_name";
    public static final String KEY_ALBUM_NAME       = "album_name";
    public static final String KEY_GENRE_NAME       = "genre_name";

    public static final String KEY_DATA             = "data";

    public static final String KEY_DURATION         = "duration";
    public static final String KEY_DATE             = "date";

    public String genre_name;
*/

    private GenreItem() {
        id = 0;
        song = 0;
        album = 0;
        artist = 0;

        title = "";
        artist_name = "";
        album_name = "";
        genre_name = "";

        data = "";
        hash = "";

        duration = "0";
        date = 0L;
    }

    /*
    public boolean copy(Cursor cursor)
    {
        boolean ret = false;
        try
        {
            id          = cursor.getLong(cursor.getColumnIndex(KEY_ID));
            song        = cursor.getLong(cursor.getColumnIndex(KEY_SONG));
            album       = cursor.getLong(cursor.getColumnIndex(KEY_ALBUM));
            artist      = cursor.getLong(cursor.getColumnIndex(KEY_ARTIST));

            title       = cursor.getString(cursor.getColumnIndex(KEY_TITLE));
            artist_name = cursor.getString(cursor.getColumnIndex(KEY_ARTIST_NAME));
            album_name  = cursor.getString(cursor.getColumnIndex(KEY_ALBUM_NAME));
            genre_name  = cursor.getString(cursor.getColumnIndex(KEY_GENRE_NAME));

            data        = cursor.getString(cursor.getColumnIndex(KEY_DATA));

            duration    = cursor.getString(cursor.getColumnIndex(KEY_DURATION));
            date        = cursor.getString(cursor.getColumnIndex(KEY_DATE));
            ret = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return ret;
    }

    public ContentValues getValues()
    {
        ContentValues values = new ContentValues();

        values.put(KEY_SONG,        song);
        values.put(KEY_ALBUM,       album);
        values.put(KEY_ARTIST,      artist);

        values.put(KEY_TITLE,       title);
        values.put(KEY_ARTIST_NAME, artist_name);
        values.put(KEY_ALBUM_NAME,  album_name);
        values.put(KEY_GENRE_NAME,  genre_name);

        values.put(KEY_DATA,        data);

        values.put(KEY_DURATION,    duration);
        values.put(KEY_DATE,        date);

        return values;
    }

    public boolean get(long kid)
    {
        boolean ret = false;
        Cursor mCursor = null;
        try
        {
            String where = String.format(Locale.UK, "(%s = %d)", KEY_ID, kid);
            mCursor = Application.db.query(true, DB_TABLE, null, where, null, null, null, null, null);
            if (mCursor != null)
            {
                if (mCursor.moveToFirst())
                {
                    if (copy(mCursor))
                    {
                        ret = true;
                    }
                }
            }
        }
        catch (Exception e)
        {
        }
        finally
        {
            if (mCursor != null)
            {
                mCursor.close();
            }
        }
        return ret;
    }

    public static Cursor load()
    {
        return Application.db.query(DB_TABLE, null, null, null, null, null, "_id asc");
    }

    public static Cursor load(int off, int size)
    {
        String limit = String.format(Locale.US, "%d, %d", off, size);
        return Application.db.query(DB_TABLE, null, null, null, null, null, "_id asc", limit);
    }

    public long insert()
    {
        return Application.db.insert(DB_TABLE, null, getValues());
    }

    public boolean update()
    {
        return Application.db.update(DB_TABLE, getValues(), KEY_ID + "=" + id, null) > 0;
    }

    public static boolean updateTitle(String data, String title)
    {
        ContentValues values = new ContentValues();
        values.put(KEY_TITLE, title);

        return Application.db.update(DB_TABLE, values, KEY_DATA + "= ? ", new String[]{data}) > 0;
    }

    public boolean delete()
    {
        return Application.db.delete(DB_TABLE, KEY_ID + "=" + id, null) > 0;
    }

    public boolean delete(long id)
    {
        return Application.db.delete(DB_TABLE, KEY_ID + "=" + id, null) > 0;
    }

    public static boolean deleteByData(String data)
    {
        return Application.db.delete(DB_TABLE, KEY_DATA + "= ?", new String[]{data} ) > 0;
    }

    public static boolean deleteByHash(String data)
    {
        return Application.db.delete(DB_TABLE, KEY_DATA + "= ?", new String[]{data} ) > 0;
    }

    public static boolean deleteAll()
    {
        return Application.db.delete(DB_TABLE, null, null) > 0;
    }

    public static int getCount()
    {
        int count = 0;
        Cursor mCursor = null;
        try
        {
            mCursor = Application.db.query(true, DB_TABLE, null, null, null, null, null, null, null);
            if (mCursor != null)
            {
                count = mCursor.getCount();
            }
        }
        catch (Exception e)
        {
            Log.i(TAG, "Exception: " + e.toString());
        }
        finally
        {
            if (mCursor != null)
            {
                mCursor.close();
            }
        }
        return count;
    }

    public static ArrayList<GenreItem> loadAll()
    {
        Cursor c = load();
        ArrayList<GenreItem> list = new ArrayList<GenreItem>();

        while (c.moveToNext())
        {
            GenreItem item = new GenreItem();
            if(item.copy(c))
            {
                list.add(item);
            }
        }

        c.close();

        return list;
    }
*/
}
