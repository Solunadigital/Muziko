package com.muziko.tasks;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.muziko.R;
import com.muziko.activities.splash.LoaderActivity;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.tageditor.metadata.MusicMetadata;
import com.muziko.tageditor.metadata.MusicMetadataSet;
import com.muziko.tageditor.myid3.MyID3;

import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class MusicLoader extends AsyncTask<QueueItem, int[], Integer> {

    private static final MyID3 id3 = new MyID3();
    private final Context mContext;

    public MusicLoader(Context ctx) {
        this.mContext = ctx;
    }

    @Override
    protected Integer doInBackground(QueueItem... params) {

        try

        {
            boolean ret = false;

            HashMap<Long, Long> trackGenreMap = new HashMap<>();
            HashMap<Long, String> genreNameMap = new HashMap<>();

            String[] genresProj = {MediaStore.Audio.Genres.Members.AUDIO_ID, MediaStore.Audio.Genres.Members.GENRE_ID};
            Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://media/external/audio/genres/all/members"), genresProj, null, null, null);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                trackGenreMap.put(cursor.getLong(0), cursor.getLong(1));
            }
            cursor.close();

            final String[] genreNameColumns = {MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME};
            cursor = mContext.getContentResolver().query(MediaStore.Audio.Genres.getContentUri("external"), genreNameColumns, null, null, null);

            for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
                genreNameMap.put(cursor.getLong(0), cursor.getString(1));
            }
            cursor.close();

            LinkedHashMap<String, QueueItem> trackMap = new LinkedHashMap<>();
            LinkedHashMap<String, QueueItem> genreMap = new LinkedHashMap<>();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor c = mContext.getContentResolver().query(uri, null, MediaStore.Audio.Media.IS_MUSIC + " != 0", null, null);


            String genre = "";
            int folderCounter = 1;
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndex(MediaStore.Audio.Media._ID));
                long albumId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
                long artistId = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.ARTIST_ID));

                String name = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                String title = c.getString(c.getColumnIndex(MediaStore.Audio.Media.TITLE));
                String artist = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                String album = c.getString(c.getColumnIndex(MediaStore.Audio.Media.ALBUM));
                String data = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATA));
                String composer = c.getString(c.getColumnIndex(MediaStore.Audio.Media.COMPOSER));
                String dateAdded = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATE_ADDED));
                String dateModified = c.getString(c.getColumnIndex(MediaStore.Audio.Media.DATE_MODIFIED));
                long duration = c.getLong(c.getColumnIndex(MediaStore.Audio.Media.DURATION));
                int track = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.TRACK));
                int year = c.getInt(c.getColumnIndex(MediaStore.Audio.Media.YEAR));

                if (data == null || data.trim().length() == 0)
                    continue;

                QueueItem songData = new QueueItem();

                songData.id = id;
                songData.song = id;

                if (artist != null && !artist.contains("Muziko-Ringtone")) {
                    songData.name = name;
                    songData.title = title;

                    songData.album = albumId;
                    songData.artist = artistId;

                    songData.album_name = album;
                    songData.artist_name = artist;
                    songData.duration = duration + "";
                    songData.date = Long.valueOf(dateAdded);
                    songData.dateModified = Long.valueOf(dateModified);
                    songData.composer = composer;
                    songData.data = data;
                    if (!(data.toLowerCase().contains("emulated") || data.toLowerCase().contains("sdcard0"))) {
                        songData.storage = 2;
                    } else {
                        songData.storage = 1;
                    }

                    songData.track = track;
                    songData.year = year;

//				if (genre != null && !genre.isEmpty()) {
                    String gName = "";
                    long gID = 0;

                    if (trackGenreMap.containsKey(id)) {
                        gID = trackGenreMap.get(id);

                        gName = genreNameMap.get(gID);

                    }

                    if (gName != null && !gName.isEmpty()) {

                        songData.genre_name = gName;
                        QueueItem genreData = genreMap.get(gName);
                        if (genreData == null) {
                            genreData = new QueueItem();
                            genreData.id = gID;
                            genreData.album = albumId;
                            genreData.title = gName;
                            genreData.date = Long.valueOf(dateAdded);
                            genreData.songs = 1;
                        } else {
                            genreData.songs++;
                        }

                        genreMap.put(gName, genreData);
                    } else {

                        MusicMetadataSet id3data = null;
                        try {
                            File from = new File(data);
                            id3data = id3.read(from);      //read metadata

                            if (id3data != null) {
                                MusicMetadata metadata = (MusicMetadata) id3data.getSimplified();
                                if (metadata != null) {
                                    genre = metadata.getGenre();
                                    if (genre != null && !genre.isEmpty()) {

                                        songData.genre_name = genre;
                                        QueueItem genreData = genreMap.get(genre);
                                        if (genreData == null) {
                                            genreData = new QueueItem();
                                            genreData.id = 0;
                                            genreData.album = albumId;
                                            genreData.title = genre;
                                            genreData.date = Long.valueOf(dateAdded);
                                            genreData.songs = 1;
                                        } else {
                                            genreData.songs++;
                                        }
                                        genreMap.put(genre, genreData);

                                    } else {
                                        genre = mContext.getString(R.string.unknown_genre);
                                        songData.genre_name = genre;
                                        QueueItem genreData = genreMap.get(genre);
                                        if (genreData == null) {
                                            genreData = new QueueItem();
                                            genreData.id = 0;
                                            genreData.album = albumId;
                                            genreData.title = genre;
                                            genreData.date = Long.valueOf(dateAdded);
                                            genreData.songs = 1;
                                        } else {
                                            genreData.songs++;
                                        }
                                        genreMap.put(genre, genreData);
                                    }

                                }
                            }
                        } catch (Exception ex) {

                            Crashlytics.logException(ex);
                        }
                    }

                    songData.hash();

                    if (Integer.parseInt(songData.duration) > 1) {

                        String folderName = "";
                        File file = new File(data);
                        String path = file.getParent();
                        if (path != null)
                            folderName = path.substring(path.lastIndexOf("/") + 1);
                        else
                            folderName = path;

                        songData.folder_name = folderName;
                        songData.folder_path = path;
                        trackMap.put(data, songData);

                    }
                } else {
                    String basename = FilenameUtils.getBaseName(data);
                    songData.name = basename;
                    songData.title = basename;
                    songData.data = data;
                    songData.artist_name = mContext.getString(R.string.unknown_artist);
                    songData.album_name = mContext.getString(R.string.unknown_album);
                    songData.genre_name = mContext.getString(R.string.unknown_genre);
                    trackMap.put(data, songData);
                }

            }

            c.close();
            Log.d("musicFromDevice", "SIZE: " + trackMap.size());

            TrackRealmHelper.insertList(trackMap);

            AppController.Instance().startMd5Updater();

            genreMap.clear();
            genreNameMap.clear();
            trackGenreMap.clear();

            return trackMap.size();

        } catch (Exception e) {
            Crashlytics.logException(e);
            return -1;
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Integer s) {

        if (s == -1) {
            mContext.startActivity(new Intent(mContext, LoaderActivity.class));
        } else {
            Answers.getInstance().logCustom(new CustomEvent("Music Library Size")
                    .putCustomAttribute("Songs", s));

            EventBus.getDefault().post(new RefreshEvent(1000));
        }
        super.onPostExecute(s);
    }
}
