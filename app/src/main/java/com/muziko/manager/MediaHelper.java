package com.muziko.manager;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.CoordinatorLayout;
import android.util.Log;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.MD5;
import com.muziko.tasks.MusicLoader;
import com.muziko.tasks.ScanMediaFiles;

import org.apache.commons.io.FilenameUtils;
import com.muziko.tageditor.metadata.MusicMetadata;
import com.muziko.tageditor.metadata.MusicMetadataSet;
import com.muziko.tageditor.myid3.MyID3;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedHashMap;

/**
 * Created by dev on 20/07/2016.
 */
public class MediaHelper {

    private static final MyID3 id3 = new MyID3();
    private static MediaHelper instance;
    private Context mContext;
    private boolean updateRunning;
    private MaterialDialog scan;
    private ScanMediaFiles scanMediaFiles = null;

    //no outer class can initialize this class's object
    private MediaHelper() {
    }

    public static MediaHelper Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new MediaHelper();
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
    }

    public void loadMusicFromTrack(final String trackData, final boolean notify) {

        try {

            final HashMap<Long, Long> trackGenreMap = new HashMap<>();
            final HashMap<Long, String> genreNameMap = new HashMap<>();

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

            final LinkedHashMap<String, QueueItem> trackMap = new LinkedHashMap<>();
            final LinkedHashMap<String, QueueItem> genreMap = new LinkedHashMap<>();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor c = mContext.getContentResolver().query(uri, null, MediaStore.Images.Media.DATA + " like ? ",
                    new String[]{trackData}, null);

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

                if (!data.equals(trackData)) {
                    continue;
                }

                QueueItem songData = new QueueItem();
                if (artist != null && !artist.contains("Muziko-Ringtone")) {
                    songData.id = id;
                    songData.song = id;
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
                        songData.md5 = MD5.calculateMD5(file);
                        songData.folder_name = folderName;
                        songData.folder_path = path;
                        trackMap.put(data, songData);

                    }
                }

            }

            c.close();
            Log.d("musicFromDevice", "SIZE: " + trackMap.size());

            QueueItem queueItem = trackMap.get(trackData);
            if (queueItem != null) {
                TrackRealmHelper.insertTrack(queueItem);
            } else {
                queueItem = new QueueItem();
                queueItem.title = FilenameUtils.getBaseName(trackData);
                queueItem.artist_name = mContext.getString(R.string.unknown_artist);
                queueItem.album_name = mContext.getString(R.string.unknown_album);
                queueItem.genre_name = mContext.getString(R.string.unknown_genre);
                queueItem.data = trackData;
                TrackRealmHelper.insertTrack(queueItem);
                if (notify) {
                    AppController.toast(mContext, "Problem reading song info");
                }
            }

            genreMap.clear();

            genreNameMap.clear();
            trackGenreMap.clear();

        } catch (Exception ex) {

            Crashlytics.logException(ex);
        }
    }

    public void loadMusicWrapper(boolean notify) {

        if (PrefsManager.Instance().getNeedsUpdate() || !PrefsManager.Instance().getDatabaseReady() || TrackRealmHelper.getCount() == 0 || notify) {

            PrefsManager.Instance().setNeedsUpdate(false);

            MusicLoader musicLoader = new MusicLoader(mContext);
            musicLoader.execute();

        }
    }


    public void scanMediaFiles(Activity activity, final CoordinatorLayout coordinatorlayout) {

        String[] extensions = {
                "mp3",
        };


        if (scanMediaFiles != null) {
            scanMediaFiles.cancel(true);
            scanMediaFiles = null;
        }

        scanMediaFiles = new ScanMediaFiles(activity, coordinatorlayout, extensions);
        scanMediaFiles.execute();
    }

    public void scanMedia() {

        if (updateRunning) {
            return;
        }

        updateRunning = true;

//		if (getStorageList().size() < 2) {
//			PrefsManager.Instance().setHasSD(context, false);
//		} else {
//			PrefsManager.Instance().setHasSD(context, true);
//		}

        scan = new MaterialDialog.Builder(mContext).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue).title("Scan Media")
                .content("Starting").positiveText("Background").onPositive((dialog, which) -> dialog.dismiss())
                .show();

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

            scan.setContent(data);

            if (artist != null) {// && !artist.contains("Muziko-Ringtone")) {
                songData.id = id;
                songData.song = id;
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
            }

        }

        c.close();
        Log.d("musicFromDevice", "SIZE: " + trackMap.size());

        TrackRealmHelper.insertList(trackMap);

        AppController.Instance().startMd5Updater();

        genreMap.clear();

        genreNameMap.clear();
        trackGenreMap.clear();

        scan.setContent("Scan Complete");
        // Execute some code after 2 seconds have passed
        WeakHandler handler = new WeakHandler();
        handler.postDelayed(() -> scan.dismiss(), 3000);

        updateRunning = false;

    }

    public void loadMusic() {

        if (updateRunning) {
            return;
        }

        updateRunning = true;

//		if (getStorageList().size() < 2) {
//			PrefsManager.Instance().setHasSD(context, false);
//		} else {
//			PrefsManager.Instance().setHasSD(context, true);
//		}


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

        updateRunning = false;
        trackMap.size();
    }

    public void loadMusicFromTrackAsync(final String trackData, final boolean notify) {

        AsyncJob.doInBackground(() -> {

            final HashMap<Long, Long> trackGenreMap = new HashMap<>();
            final HashMap<Long, String> genreNameMap = new HashMap<>();

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

            final LinkedHashMap<String, QueueItem> trackMap = new LinkedHashMap<>();
            final LinkedHashMap<String, QueueItem> genreMap = new LinkedHashMap<>();

            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            Cursor c = mContext.getContentResolver().query(uri, null, MediaStore.Images.Media.DATA + " like ? ",
                    new String[]{trackData}, null);

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

                if (!data.equals(trackData)) {
                    continue;
                }

                QueueItem songData = new QueueItem();
                if (artist != null && !artist.contains("Muziko-Ringtone")) {
                    songData.id = id;
                    songData.song = id;
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
                }

            }

            c.close();
            Log.d("musicFromDevice", "SIZE: " + trackMap.size());

            // Create a fake result (MUST be final)
            final boolean result = true;

            // Send the result to the UI thread and show it on a Toast
            AsyncJob.doOnMainThread(() -> {
                QueueItem queueItem = trackMap.get(trackData);

                if (queueItem != null) {
                    queueItem.dateModified = System.currentTimeMillis();
                    TrackRealmHelper.insertTrack(queueItem);
                } else {
                    queueItem = new QueueItem();
                    queueItem.title = FilenameUtils.getBaseName(trackData);
                    queueItem.artist_name = mContext.getString(R.string.unknown_artist);
                    queueItem.album_name = mContext.getString(R.string.unknown_album);
                    queueItem.genre_name = mContext.getString(R.string.unknown_genre);
                    queueItem.data = trackData;
                    queueItem.dateModified = System.currentTimeMillis();
                    TrackRealmHelper.insertTrack(queueItem);
                    if (notify) {
                        AppController.toast(mContext, "Problem reading song info");
                    }
                }

                genreMap.clear();

                genreNameMap.clear();
                trackGenreMap.clear();
            });
        });


    }

}
