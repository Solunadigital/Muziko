package com.muziko.database;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;


import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.common.models.Lyrics;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.Share;
import com.muziko.common.models.firebase.TrackFingerprint;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.GsonManager;
import com.muziko.manager.PrefsManager;
import com.muziko.models.StorageFolder;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static com.muziko.manager.CloudManager.connectedCloudDrives;

/**
 * Created by dev on 17/07/2016.
 */
public class TrackRealmHelper {

    public static void insertList(LinkedHashMap<String, QueueItem> queueItems) {
        boolean deleted = false;

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).findAll();

        if (trackRealms.size() == 0) {
            PicassoTools.clearCache(Picasso.with(MyApplication.getInstance().getApplicationContext()));
        }

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        for (QueueItem queueItem : queueItems.values()) {
            TrackRealm trackRealm;

            // first run
            if (trackRealms.size() == 0) {
                trackRealm = new TrackRealm();
                trackRealm.setData(queueItem.data);
                trackRealm.setId(queueItem.id);
                trackRealm.setSong(queueItem.song);
                trackRealm.setAlbum(queueItem.album);
                trackRealm.setArtist(queueItem.artist);
                trackRealm.setName(queueItem.name);
                trackRealm.setTitle(queueItem.title);
                trackRealm.setArtist_name(queueItem.artist_name);
                trackRealm.setAlbum_name(queueItem.album_name);
                trackRealm.setGenre_name(queueItem.genre_name);
                trackRealm.setHash(queueItem.hash);
                trackRealm.setDuration(queueItem.duration);
                trackRealm.setDate(queueItem.date);
                if (queueItem.dateModified == null) {
                    trackRealm.setDateModified(System.currentTimeMillis());
                } else {
                    trackRealm.setDateModified(queueItem.dateModified);
                }
                trackRealm.setDateAdded(System.currentTimeMillis());
                trackRealm.setComposer(queueItem.composer);
                trackRealm.setTrack(queueItem.track);
                trackRealm.setYear(queueItem.year);
                if (!(queueItem.data.toLowerCase().contains("emulated") || queueItem.data.toLowerCase().contains("sdcard0"))) {
                    trackRealm.setStorage(2);
                } else {
                    trackRealm.setStorage(1);
                }
                trackRealm.setFolder_name(queueItem.folder_name);
                trackRealm.setFolder_path(queueItem.folder_path);
                myRealm.insertOrUpdate(trackRealm);

            } else {

                // run already
                trackRealm = myRealm.where(TrackRealm.class).equalTo("data", queueItem.data).findFirst();
                // exists
                if (trackRealm != null) {
                    trackRealm.setId(queueItem.id);
                    trackRealm.setSong(queueItem.song);
                    trackRealm.setAlbum(queueItem.album);
                    trackRealm.setArtist(queueItem.artist);
                    trackRealm.setName(queueItem.name);
                    trackRealm.setTitle(queueItem.title);
                    trackRealm.setArtist_name(queueItem.artist_name);
                    trackRealm.setAlbum_name(queueItem.album_name);
                    trackRealm.setGenre_name(queueItem.genre_name);
                    trackRealm.setHash(queueItem.hash);
                    trackRealm.setDuration(queueItem.duration);
                    trackRealm.setDate(queueItem.date);
                    if (queueItem.dateModified == null) {
                        trackRealm.setDateModified(System.currentTimeMillis());
                    } else {
                        trackRealm.setDateModified(queueItem.dateModified);
                    }
                    trackRealm.setDateAdded(trackRealm.getDateAdded());
                    trackRealm.setComposer(queueItem.composer);
                    trackRealm.setTrack(queueItem.track);
                    trackRealm.setYear(queueItem.year);
                    if (!(queueItem.data.toLowerCase().contains("emulated") || queueItem.data.toLowerCase().contains("sdcard0"))) {
                        trackRealm.setStorage(2);
                    } else {
                        trackRealm.setStorage(1);
                    }
                    trackRealm.setFolder_name(queueItem.folder_name);
                    trackRealm.setFolder_path(queueItem.folder_path);
                    myRealm.copyToRealmOrUpdate(trackRealm);
                } else {

                    // new
                    trackRealm = new TrackRealm();
                    trackRealm.setData(queueItem.data);
                    trackRealm.setId(queueItem.id);
                    trackRealm.setSong(queueItem.song);
                    trackRealm.setAlbum(queueItem.album);
                    trackRealm.setArtist(queueItem.artist);
                    trackRealm.setName(queueItem.name);
                    trackRealm.setTitle(queueItem.title);
                    trackRealm.setArtist_name(queueItem.artist_name);
                    trackRealm.setAlbum_name(queueItem.album_name);
                    trackRealm.setGenre_name(queueItem.genre_name);
                    trackRealm.setHash(queueItem.hash);
                    trackRealm.setDuration(queueItem.duration);
                    trackRealm.setDate(queueItem.date);
                    if (queueItem.dateModified == null) {
                        trackRealm.setDateModified(System.currentTimeMillis());
                    } else {
                        trackRealm.setDateModified(queueItem.dateModified);
                    }
                    trackRealm.setDateAdded(System.currentTimeMillis());
                    trackRealm.setComposer(queueItem.composer);
                    trackRealm.setTrack(queueItem.track);
                    trackRealm.setYear(queueItem.year);
                    if (!(queueItem.data.toLowerCase().contains("emulated") || queueItem.data.toLowerCase().contains("sdcard0"))) {
                        trackRealm.setStorage(2);
                    } else {
                        trackRealm.setStorage(1);
                    }
                    trackRealm.setFolder_name(queueItem.folder_name);
                    trackRealm.setFolder_path(queueItem.folder_path);
                    myRealm.insertOrUpdate(trackRealm);
                }
            }
        }

        trackRealms = myRealm.where(TrackRealm.class).findAll();

        for (TrackRealm trackRealm : trackRealms) {

            if (trackRealm.getStorage() == 1 || trackRealm.getStorage() == 2) {
                QueueItem queueItem = queueItems.get(trackRealm.getData());
                if (queueItem == null) {
                    deleted = true;
                    trackRealm.deleteFromRealm();
                }
            }
        }
        myRealm.commitTransaction();
        myRealm.close();
        PrefsManager.Instance().setDatabaseReady(true);

        queueItems.size();
    }

    public static boolean insertCloudList(int cloudAccountId, LinkedHashMap<String, QueueItem> queueItems) {

        boolean ret = false;

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).equalTo("storage", cloudAccountId).findAll();

        if (queueItems.size() == 0) {
            myRealm.close();
            return false;

        }
        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        for (QueueItem queueItem : queueItems.values()) {
            TrackRealm trackRealm;

            // first run
            if (trackRealms.size() == 0) {
                ret = true;
                trackRealm = new TrackRealm();
                trackRealm.setData(queueItem.data);
                trackRealm.setCloudId(queueItem.cloudId);
                trackRealm.setId(queueItem.id);
                trackRealm.setSong(queueItem.song);
                trackRealm.setAlbum(queueItem.album);
                trackRealm.setArtist(queueItem.artist);
                trackRealm.setName(queueItem.name);
                trackRealm.setTitle(queueItem.title);
                trackRealm.setArtist_name(queueItem.artist_name);
                trackRealm.setAlbum_name(queueItem.album_name);
                trackRealm.setGenre_name(queueItem.genre_name);
                trackRealm.setHash(queueItem.hash);
                trackRealm.setDuration(queueItem.duration);
                trackRealm.setDate(queueItem.date);
                if (queueItem.dateModified == null) {
                    trackRealm.setDateModified(System.currentTimeMillis());
                } else {
                    trackRealm.setDateModified(queueItem.dateModified);
                }
                trackRealm.setDateAdded(System.currentTimeMillis());
                trackRealm.setComposer(queueItem.composer);
                trackRealm.setTrack(queueItem.track);
                trackRealm.setYear(queueItem.year);
                trackRealm.setStorage(queueItem.storage);
                trackRealm.setFolder_name(queueItem.folder_name);
                trackRealm.setFolder_path(queueItem.folder_path);
                trackRealm.setSize(queueItem.size);
                myRealm.insertOrUpdate(trackRealm);

            } else {

                // run already
                trackRealm = myRealm.where(TrackRealm.class).equalTo("data", queueItem.data).findFirst();
                QueueItem existingQueue = getTrack(queueItem.data);

                // exists
                if (existingQueue != null && !queueItem.equals(existingQueue)) {
                    ret = true;
                    trackRealm.setId(queueItem.id);
                    trackRealm.setSong(queueItem.song);
                    trackRealm.setAlbum(queueItem.album);
                    trackRealm.setArtist(queueItem.artist);
                    trackRealm.setName(queueItem.name);
                    trackRealm.setTitle(queueItem.title);
                    trackRealm.setArtist_name(queueItem.artist_name);
                    trackRealm.setAlbum_name(queueItem.album_name);
                    trackRealm.setGenre_name(queueItem.genre_name);
                    trackRealm.setHash(queueItem.hash);
                    trackRealm.setDuration(queueItem.duration);
                    trackRealm.setDate(queueItem.date);
                    if (queueItem.dateModified == null) {
                        trackRealm.setDateModified(System.currentTimeMillis());
                    } else {
                        trackRealm.setDateModified(queueItem.dateModified);
                    }
                    trackRealm.setDateAdded(trackRealm.getDateAdded());
                    trackRealm.setComposer(queueItem.composer);
                    trackRealm.setTrack(queueItem.track);
                    trackRealm.setYear(queueItem.year);
                    trackRealm.setStorage(queueItem.storage);
                    trackRealm.setFolder_name(queueItem.folder_name);
                    trackRealm.setFolder_path(queueItem.folder_path);
                    trackRealm.setSize(queueItem.size);
                    trackRealm.setFavorite(trackRealm.isFavorite());
                    myRealm.copyToRealmOrUpdate(trackRealm);
                } else if (existingQueue == null) {

                    ret = true;
                    trackRealm = new TrackRealm();
                    trackRealm.setData(queueItem.data);
                    trackRealm.setCloudId(queueItem.cloudId);
                    trackRealm.setId(queueItem.id);
                    trackRealm.setSong(queueItem.song);
                    trackRealm.setAlbum(queueItem.album);
                    trackRealm.setArtist(queueItem.artist);
                    trackRealm.setName(queueItem.name);
                    trackRealm.setTitle(queueItem.title);
                    trackRealm.setArtist_name(queueItem.artist_name);
                    trackRealm.setAlbum_name(queueItem.album_name);
                    trackRealm.setGenre_name(queueItem.genre_name);
                    trackRealm.setHash(queueItem.hash);
                    trackRealm.setDuration(queueItem.duration);
                    trackRealm.setDate(queueItem.date);
                    if (queueItem.dateModified == null) {
                        trackRealm.setDateModified(System.currentTimeMillis());
                    } else {
                        trackRealm.setDateModified(queueItem.dateModified);
                    }
                    trackRealm.setDateAdded(System.currentTimeMillis());
                    trackRealm.setComposer(queueItem.composer);
                    trackRealm.setTrack(queueItem.track);
                    trackRealm.setYear(queueItem.year);
                    trackRealm.setStorage(queueItem.storage);
                    trackRealm.setFolder_name(queueItem.folder_name);
                    trackRealm.setFolder_path(queueItem.folder_path);
                    trackRealm.setSize(queueItem.size);
                    myRealm.insertOrUpdate(trackRealm);
                }
            }
        }
        myRealm.commitTransaction();

        myRealm.close();
        return ret;
    }

    public static boolean removedCloudTracks(int cloudAccountId, LinkedHashMap<String, QueueItem> queueItems) {
        boolean result = false;
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).equalTo("storage", cloudAccountId).findAll();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }
        for (TrackRealm trackRealm : trackRealms) {
            String trackData = trackRealm.getData();
            QueueItem queueItem = queueItems.get(trackData);
            if (queueItem == null) {
                result = true;
                trackRealm.deleteFromRealm();
            }
        }
        myRealm.commitTransaction();
        myRealm.close();
        return result;
    }

    public static void insertTrack(QueueItem queueItem) {

        if (queueItem.data.toLowerCase().contains("Muziko_shared_auclip")) {
            return;
        }

        Realm myRealm = Realm.getDefaultInstance();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }


        TrackRealm trackRealm;

        // run already
        trackRealm = myRealm.where(TrackRealm.class).equalTo("data", queueItem.data).findFirst();
        // exists
        if (trackRealm != null) {
            trackRealm.setId(queueItem.id);
            trackRealm.setSong(queueItem.song);
            trackRealm.setAlbum(queueItem.album);
            trackRealm.setArtist(queueItem.artist);
            trackRealm.setName(queueItem.name);
            trackRealm.setTitle(queueItem.title);
            trackRealm.setArtist_name(queueItem.artist_name);
            trackRealm.setAlbum_name(queueItem.album_name);
            trackRealm.setGenre_name(queueItem.genre_name);
            trackRealm.setHash(queueItem.hash);
            trackRealm.setDuration(queueItem.duration);
            if (queueItem.date == null) {
                trackRealm.setDateModified(System.currentTimeMillis());
            } else {
                trackRealm.setDateModified(queueItem.date);
            }
            if (queueItem.dateModified == null) {
                trackRealm.setDateModified(System.currentTimeMillis());
            } else {
                trackRealm.setDateModified(queueItem.dateModified);
            }
            trackRealm.setDateAdded(trackRealm.getDateAdded());
            trackRealm.setComposer(queueItem.composer);
            trackRealm.setTrack(queueItem.track);
            trackRealm.setYear(queueItem.year);
            if (!(queueItem.data.toLowerCase().contains("emulated") || queueItem.data.toLowerCase().contains("sdcard0"))) {
                trackRealm.setStorage(2);
            } else {
                trackRealm.setStorage(1);
            }
            trackRealm.setFolder_name(queueItem.folder_name);
            trackRealm.setFolder_path(queueItem.folder_path);
            trackRealm.setMd5(queueItem.md5);
            trackRealm.setSize(queueItem.size);
            trackRealm.setFavorite(trackRealm.isFavorite());
            trackRealm.setFavsync(trackRealm.isFavsync());
            trackRealm.setLibrary(trackRealm.isLibrary());
            myRealm.copyToRealmOrUpdate(trackRealm);
        } else {

            // new
            trackRealm = new TrackRealm();
            trackRealm.setData(queueItem.data);
            trackRealm.setId(queueItem.id);
            trackRealm.setSong(queueItem.song);
            trackRealm.setAlbum(queueItem.album);
            trackRealm.setArtist(queueItem.artist);
            trackRealm.setName(queueItem.name);
            trackRealm.setTitle(queueItem.title);
            trackRealm.setArtist_name(queueItem.artist_name);
            trackRealm.setAlbum_name(queueItem.album_name);
            trackRealm.setGenre_name(queueItem.genre_name);
            trackRealm.setHash(queueItem.hash);
            trackRealm.setDuration(queueItem.duration);
            if (queueItem.date == null) {
                trackRealm.setDateModified(System.currentTimeMillis());
            } else {
                trackRealm.setDateModified(queueItem.date);
            }
            if (queueItem.dateModified == null) {
                trackRealm.setDateModified(System.currentTimeMillis());
            } else {
                trackRealm.setDateModified(queueItem.dateModified);
            }
            trackRealm.setDateAdded(System.currentTimeMillis());
            trackRealm.setComposer(queueItem.composer);
            trackRealm.setTrack(queueItem.track);
            trackRealm.setYear(queueItem.year);
            trackRealm.setStorage(queueItem.storage);
            trackRealm.setFolder_name(queueItem.folder_name);
            trackRealm.setFolder_path(queueItem.folder_path);
            trackRealm.setMd5(queueItem.md5);
            trackRealm.setSize(queueItem.size);
            myRealm.insertOrUpdate(trackRealm);
        }

        myRealm.commitTransaction();
        myRealm.close();

    }

    public static void updateMD5Hashs(ArrayList<QueueItem> queueItems) {

        Realm myRealm = Realm.getDefaultInstance();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        for (QueueItem queueItem : queueItems) {
            TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                    .equalTo("data", queueItem.getData())
                    .findFirst();

            if (trackRealm != null) {
                trackRealm.setMd5(queueItem.md5);
                myRealm.copyToRealmOrUpdate(trackRealm);
            }
        }
        myRealm.commitTransaction();
        myRealm.close();

    }

    public static void updateMD5Hash(QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.getData())
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setMd5(queueItem.md5);
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();

    }

    public static void updateACRKey(TrackFingerprint trackFingerprint) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", trackFingerprint.getData())
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setAcrid(trackFingerprint.getAcrid());
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();

    }

    public static void updateACRKey(QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setAcrid(queueItem.acrid);
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();

    }


    public static void updateStartTime(QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setStartFrom(queueItem.startFrom);
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();

    }

    public static void updateTrackTags(QueueItem queueItem, boolean lyricsChanged, boolean lRC) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setTitle(queueItem.title);
            trackRealm.setArtist_name(queueItem.artist_name);
            trackRealm.setAlbum_name(queueItem.album_name);
            trackRealm.setTrack(queueItem.track);
            trackRealm.setYear(queueItem.year);
            trackRealm.setGenre_name(queueItem.genre_name);
            trackRealm.setUrl(queueItem.url);
            trackRealm.setLyrics(queueItem.lyrics);
            trackRealm.setlRC(lRC);
            if (lyricsChanged) {
                trackRealm.setLyricsFlag(Lyrics.POSITIVE_RESULT);
            }
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();


            ArrayList<QueueItem> queueItems = new ArrayList<>();
            queueItems.add(queueItem);
            String tracks = GsonManager.Instance().getGson().toJson(queueItems);
            AppController.Instance().serviceUpdateCache(tracks);

            return;
        }

        myRealm.close();

    }

    public static void updateMultiTrackTags(QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setTitle(queueItem.title);
            trackRealm.setArtist_name(queueItem.artist_name);
            trackRealm.setAlbum_name(queueItem.album_name);
            trackRealm.setTrack(queueItem.track);
            trackRealm.setYear(queueItem.year);
            trackRealm.setGenre_name(queueItem.genre_name);
            trackRealm.setUrl(queueItem.url);
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            ArrayList<QueueItem> queueItems = new ArrayList<>();
            queueItems.add(queueItem);
            String tracks = GsonManager.Instance().getGson().toJson(queueItems);
            AppController.Instance().serviceUpdateCache(tracks);

            return;
        }

        myRealm.close();

    }

    public static void updateCoverArt(final QueueItem queueItem, final boolean noCoverArt) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setNoCover(noCoverArt);
            trackRealm.setCoverUpdated(trackRealm.getDateModified());
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            ArrayList<QueueItem> queueItems = new ArrayList<>();
            queueItems.add(queueItem);
            String tracks = GsonManager.Instance().getGson().toJson(queueItems);
            AppController.Instance().serviceUpdateCache(tracks);

            return;
        }

        myRealm.close();
    }

    public static void updateRating(final QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setRating(queueItem.rating);
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();
    }

    public static boolean movetoNegative(final QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setRemoved(true);
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return true;
        }

        myRealm.close();
        return false;
    }

    public static void moveoutofNegative(final QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            trackRealm.setRemoved(false);
            myRealm.copyToRealmOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();
    }


    public static void updateCoverArtforAlbum(final QueueItem queueItem, final boolean noCoverArt) {

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).equalTo("album_name", queueItem.title).findAll();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        for (TrackRealm trackRealm : trackRealms) {
            trackRealm.setNoCover(noCoverArt);
            trackRealm.setCoverUpdated(System.currentTimeMillis());
            myRealm.copyToRealmOrUpdate(trackRealm);
        }
        myRealm.commitTransaction();


        myRealm.close();
    }

    public static void deleteCloudTracks(int cloudAccountId) {
        Realm myRealm = Realm.getDefaultInstance();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        myRealm.where(TrackRealm.class).equalTo("storage", cloudAccountId).findAll().deleteAllFromRealm();

        myRealm.commitTransaction();

        myRealm.close();

    }


    public static boolean deleteStreamingTracks() {
        Realm myRealm = Realm.getDefaultInstance();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        myRealm.where(TrackRealm.class).beginGroup().notEqualTo("storage", 0).notEqualTo("storage", 1).endGroup().findAll().deleteAllFromRealm();

        myRealm.commitTransaction();

        myRealm.close();

        return true;
    }

    public static void deleteTrack(String data) {
        Realm myRealm = Realm.getDefaultInstance();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        myRealm.where(TrackRealm.class).contains("data", data, Case.INSENSITIVE).findAll().deleteAllFromRealm();

        myRealm.commitTransaction();

        myRealm.close();

    }


    public static int getCount() {

        long results = 0;

        try {
            Realm myRealm = Realm.getDefaultInstance();

            results = myRealm.where(TrackRealm.class).count();
            myRealm.close();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }

        return (int) results;
    }


    public static QueueItem getTrack(String data) {

        Realm myRealm = Realm.getDefaultInstance();
        TrackRealm trackRealm;

        trackRealm = myRealm.where(TrackRealm.class).equalTo("data", data).findFirst();

        QueueItem queueItem = null;

        if (trackRealm != null) {
            queueItem = getQueueItemFromTrack(trackRealm);
        }
        myRealm.close();
        return queueItem;
    }

    public static void deleteFirebaseTrackByMD5(String MD5) {
        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).findAll();
        for (TrackRealm trackRealm : realmResults) {
            if (trackRealm.getMd5() != null && trackRealm.getMd5().equals(MD5) && trackRealm.getAlbum() == CloudManager.FIREBASE) {
                if (!myRealm.isInTransaction()) {
                    myRealm.beginTransaction();
                }
                trackRealm.deleteFromRealm();
                myRealm.commitTransaction();
                break;
            }
        }

        myRealm.close();
    }

    public static QueueItem getTrackByMD5(String MD5) {

        Realm myRealm = Realm.getDefaultInstance();
        QueueItem queueItem = null;

        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).findAll();
        for (TrackRealm trackRealm : realmResults) {
            if (trackRealm.getMd5() != null && trackRealm.getMd5().equals(MD5)) {
                queueItem = getQueueItemFromTrack(trackRealm);
                break;
            }
        }

        myRealm.close();
        return queueItem;
    }

    public static QueueItem getTrackByACRid(String acrid) {

        Realm myRealm = Realm.getDefaultInstance();
        TrackRealm trackRealm;

        trackRealm = myRealm.where(TrackRealm.class).equalTo("acrid", acrid).findFirst();

        QueueItem queueItem = null;

        if (trackRealm != null) {
            queueItem = getQueueItemFromTrack(trackRealm);
        }
        myRealm.close();
        return queueItem;
    }

    public static QueueItem getTrackforPlaylist(String data) {

        Realm myRealm = Realm.getDefaultInstance();
        TrackRealm trackRealm;


        trackRealm = myRealm.where(TrackRealm.class).equalTo("data", data).findFirst();

        if (trackRealm == null) {
            return null;
        }

        if (trackRealm.isRemoved()) {
            return null;
        }

        QueueItem queueItem = null;

        if (trackRealm != null) {

            queueItem = getQueueItemFromTrack(trackRealm);
        }
        myRealm.close();
        return queueItem;
    }

    public static QueueItem getTrackByShare(Share share) {

        Realm myRealm = Realm.getDefaultInstance();
        TrackRealm trackRealm;

        trackRealm = myRealm.where(TrackRealm.class).equalTo("title", share.getTitle()).equalTo("artist_name", share.getArtist()).equalTo("album_name", share.getAlbum()).findFirst();

        QueueItem queueItem = null;

        if (trackRealm != null) {

            queueItem = getQueueItemFromTrack(trackRealm);
        }
        myRealm.close();
        return queueItem;
    }

    public static QueueItem getTrackByAlbum(long album) {


        Realm myRealm = Realm.getDefaultInstance();
        TrackRealm trackRealm;

        trackRealm = myRealm.where(TrackRealm.class).equalTo("album", album).findFirst();

        QueueItem queueItem = null;

        if (trackRealm != null) {
            queueItem = getQueueItemFromTrack(trackRealm);
        }
        myRealm.close();
        return queueItem;
    }

    public static LinkedHashMap<String, QueueItem> getTracks(int storageFilter) {


        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);

        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        if (storageFilter == -1) {
            realmResults = myRealm.where(TrackRealm.class)
                    .beginGroup()
                    .notEqualTo("storage", 0)
                    .notEqualTo("storage", 1)
                    .notEqualTo("storage", 2)
                    .endGroup()
                    .findAll();
        } else if (storageFilter == 0) {
            realmResults = myRealm.where(TrackRealm.class).findAll();
        } else {
            realmResults = myRealm.where(TrackRealm.class).equalTo("storage", storageFilter).findAll();
        }

        QueueItem queueItem;
        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            if (trackRealm.getStorage() == CloudManager.FIREBASE && !trackRealm.isFavsync() && !trackRealm.isLibrary()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            queueItem = getQueueItemFromTrack(trackRealm);
            queueItemLinkedHashMap.put(trackRealm.getData(), queueItem);
        }

        myRealm.close();

        return queueItemLinkedHashMap;
    }

    public static LinkedHashMap<String, QueueItem> getTracks() {

        SharedPreferences prefs =
                PreferenceManager.getDefaultSharedPreferences(
                        MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);

        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).findAll();
        QueueItem queueItem;
        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty()
                        || trackRealm.getArtist_name().equals("Unknown Artist")
                        || trackRealm.getArtist_name().isEmpty()
                        || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }
            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            queueItem = getQueueItemFromTrack(trackRealm);
            queueItemLinkedHashMap.put(trackRealm.getData(), queueItem);
        }

        myRealm.close();

        return queueItemLinkedHashMap;
    }

    public static LinkedHashMap<String, QueueItem> getArtists() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).findAll();

        for (TrackRealm trackRealm : realmResults) {
            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem model;
            model = queueItemLinkedHashMap.get(trackRealm.getArtist_name());
            if (model != null) {
                model.songs++;
                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);
            } else {
                model = new QueueItem();
                model.id = 0;
                model.album = trackRealm.getAlbum();
                model.title = trackRealm.getArtist_name();
                model.songs = 1;
                model.duration = trackRealm.getDuration();
                model.date = trackRealm.getDate();
                model.dateModified = trackRealm.getDateModified();
                model.data = trackRealm.getData();
                model.noCover = trackRealm.isNoCover();
                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());

                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);

            }
        }

        myRealm.close();

        return queueItemLinkedHashMap;
    }

    public static ArrayList<TrackFingerprint> getTrackFingerprints() {


        ArrayList<TrackFingerprint> trackFingerprints = new ArrayList<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).isNotNull("acrid").findAll();

        for (TrackRealm trackRealm : realmResults) {


            TrackFingerprint model;

            model = new TrackFingerprint();
            model.setAcrid(trackRealm.getAcrid());
            model.setData(trackRealm.getData());

            trackFingerprints.add(model);
        }

        myRealm.close();
        return trackFingerprints;
    }

    public static ArrayList<QueueItem> getTracksForArtist(String data) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        ArrayList<QueueItem> queueItems = new ArrayList<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).equalTo("artist_name", data).findAll();
        QueueItem queueItem;
        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            queueItem = getQueueItemFromTrack(trackRealm);
            queueItems.add(queueItem);
        }

        myRealm.close();
        return queueItems;
    }


    public static LinkedHashMap<String, QueueItem> getAlbums() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).findAll();

        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem model;
            model = queueItemLinkedHashMap.get(trackRealm.getAlbum_name());
            if (model != null) {
                model.songs++;
                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);
            } else {
                model = new QueueItem();
                model.id = 0;
                model.album = trackRealm.getAlbum();
                model.title = trackRealm.getAlbum_name();
                model.artist_name = trackRealm.getArtist_name();
                model.genre_name = trackRealm.getGenre_name();
                model.date = trackRealm.getDate();
                model.songs = 1;
                model.duration = trackRealm.getDuration();
                model.dateModified = trackRealm.getDateModified();
                model.data = trackRealm.getData();
                model.noCover = trackRealm.isNoCover();
                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());

                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);

            }
        }

        myRealm.close();

        return queueItemLinkedHashMap;
    }

    public static ArrayList<QueueItem> getTracksForAlbum(String data) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        ArrayList<QueueItem> queueItems = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;


        realmResults = myRealm.where(TrackRealm.class).equalTo("album_name", data).findAll();
        QueueItem queueItem;
        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            queueItem = getQueueItemFromTrack(trackRealm);
            queueItems.add(queueItem);
        }

        myRealm.close();
        return queueItems;
    }

    public static ArrayList<QueueItem> getTracksWithoutMD5() {

        ArrayList<QueueItem> queueItems = new ArrayList<>();
        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).findAll();
        QueueItem queueItem;
        for (TrackRealm trackRealm : realmResults) {
            if ((trackRealm.getStorage() == 1 || trackRealm.getStorage() == 2) && trackRealm.getMd5() == null) {
                queueItem = getQueueItemFromTrack(trackRealm);
                queueItems.add(queueItem);
            }
        }

        myRealm.close();
        return queueItems;
    }

    public static LinkedHashMap<Long, QueueItem> getTrackAlbumIdsForAlbum(String data) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);

        LinkedHashMap<Long, QueueItem> queueItems = new LinkedHashMap<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;


        realmResults = myRealm.where(TrackRealm.class).equalTo("album_name", data).findAll();

        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem queueItem = getQueueItemFromTrack(trackRealm);
            queueItems.put(trackRealm.getAlbum(), queueItem);

        }

        myRealm.close();
        return queueItems;
    }

    public static LinkedHashMap<String, QueueItem> getGenres() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).findAll();

        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem model;
            model = queueItemLinkedHashMap.get(trackRealm.getGenre_name());
            if (model != null) {
                model.songs++;
                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
                queueItemLinkedHashMap.put(trackRealm.getGenre_name(), model);
            } else {
                model = new QueueItem();
                model.id = 0;
                model.album = trackRealm.getAlbum();
                model.title = trackRealm.getGenre_name();
                model.songs = 1;
                model.duration = trackRealm.getDuration();
                model.date = trackRealm.getDate();
                model.dateModified = trackRealm.getDateModified();
                model.data = trackRealm.getData();
                model.noCover = trackRealm.isNoCover();
                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());

                queueItemLinkedHashMap.put(trackRealm.getGenre_name(), model);

            }
        }


        myRealm.close();
        return queueItemLinkedHashMap;
    }

    public static ArrayList<QueueItem> getTracksForGenre(String data) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        ArrayList<QueueItem> queueItems = new ArrayList<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        if (data.equals("Unknown Genre")) {
            realmResults = myRealm.where(TrackRealm.class).beginGroup().equalTo("genre_name", data).or().equalTo("genre_name", "").endGroup().findAll();
        } else {
            realmResults = myRealm.where(TrackRealm.class).equalTo("genre_name", data).findAll();
        }


        QueueItem queueItem;

        for (TrackRealm trackRealm : realmResults) {
            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            queueItem = getQueueItemFromTrack(trackRealm);
            queueItems.add(queueItem);
        }

        myRealm.close();
        return queueItems;
    }

    public static LinkedHashMap<String, QueueItem> getFolders() {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).findAll();

        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }


            QueueItem model;
            model = queueItemLinkedHashMap.get(trackRealm.getFolder_path());
            if (model != null) {
                model.songs++;
                queueItemLinkedHashMap.put(trackRealm.getFolder_path(), model);
            } else {
                model = new QueueItem();
                model.id = 0;
//				model.album = trackRealm.getAlbum();
                model.title = trackRealm.getFolder_name();
                model.date = trackRealm.getDate();
                model.dateModified = trackRealm.getDateModified();
                model.data = trackRealm.getFolder_path();
                model.songs = 1;
                model.storage = trackRealm.getStorage();
                model.folder = true;
                queueItemLinkedHashMap.put(trackRealm.getFolder_path(), model);

            }
        }

        myRealm.close();
        return queueItemLinkedHashMap;
    }

    public static LinkedHashMap<String, QueueItem> getFavorites(int storageFilter) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults;

        if (storageFilter == -1) {
            if (connectedCloudDrives.size() == 0) {
                return queueItemLinkedHashMap;
            }
            realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).in("storage", connectedCloudDrives.toArray(new Integer[connectedCloudDrives.size()])).findAll();
        } else if (storageFilter == 0) {
            realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).findAll();
        } else {
            realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).equalTo("storage", storageFilter).findAll();
        }

        for (TrackRealm trackRealm : realmResults) {

            if (prefHideUnknown) {
                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
                    continue;
                }

            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (trackRealm.isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (trackRealm.getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem queueItem = getQueueItemFromTrack(trackRealm);

            queueItemLinkedHashMap.put(trackRealm.getData(), queueItem);
        }

        myRealm.close();
        return queueItemLinkedHashMap;
    }

    public static ArrayList<QueueItem> getSyncFavorites() {

        ArrayList<QueueItem> queueItems = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).beginGroup().equalTo("favorite", true)
                .equalTo("favsync", true)
                .endGroup().findAll();

        for (TrackRealm trackRealm : realmResults) {
            QueueItem queueItem = getQueueItemFromTrack(trackRealm);
            queueItems.add(queueItem);
        }

        myRealm.close();
        return queueItems;
    }

    public static ArrayList<QueueItem> getLibrary() {

        ArrayList<QueueItem> queueItems = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).equalTo("library", true).findAll();

        for (TrackRealm trackRealm : realmResults) {
            QueueItem queueItem = getQueueItemFromTrack(trackRealm);
            queueItems.add(queueItem);
        }

        myRealm.close();
        return queueItems;
    }

    public static ArrayList<QueueItem> getLocalLibrary() {

        ArrayList<QueueItem> queueItems = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).beginGroup().equalTo("library", true).notEqualTo("storage", CloudManager.FIREBASE).endGroup().findAll();

        for (TrackRealm trackRealm : realmResults) {
            QueueItem queueItem = getQueueItemFromTrack(trackRealm);
            queueItems.add(queueItem);
        }

        myRealm.close();
        return queueItems;
    }

    public static ArrayList<String> getLibraryList() {

        ArrayList<String> arrayList = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).beginGroup().equalTo("library", true).notEqualTo("storage", CloudManager.FIREBASE).endGroup().findAll();

        for (TrackRealm trackRealm : realmResults) {

            arrayList.add(trackRealm.getData());
        }

        myRealm.close();
        return arrayList;
    }

    public static ArrayList<String> getFavoritesList() {

        ArrayList<String> arrayList = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).findAll();

        for (TrackRealm trackRealm : realmResults) {

            arrayList.add(trackRealm.getData());
        }

        myRealm.close();
        return arrayList;
    }

    public static Boolean addFavorite(String data) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            trackRealm.setFavorite(true);
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return true;
        }

        myRealm.close();
        return false;
    }

    public static Boolean removeFavorite(String data) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            trackRealm.setFavorite(false);
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return true;
        }

        myRealm.close();
        return false;
    }

    public static void updateDuration(String data, String duration) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            trackRealm.setDuration(duration);
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();
    }

    public static Boolean toggleLibrary(QueueItem queueItem, boolean isLibrary) {
        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            if (isLibrary) {
                trackRealm.setLibrary(true);
            } else {
                trackRealm.setLibrary(false);
            }
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
        return isLibrary;
    }

    public static void toggleSync(QueueItem queueItem, boolean sync) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            if (sync) {
                trackRealm.setFavsync(true);
            } else {
                trackRealm.setFavsync(false);
            }
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
    }

    public static Boolean toggleFavorite(QueueItem queueItem) {

        boolean ret = false;

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            if (trackRealm.isFavorite()) {
                trackRealm.setFavorite(false);
                ret = false;
            } else {
                trackRealm.setFavorite(true);
                ret = true;
            }
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
        return ret;
    }

    public static void toggleFavorite(QueueItem queueItem, boolean fav) {

        boolean ret = false;

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            if (!fav) {
                trackRealm.setFavorite(false);
                ret = false;
            } else {
                trackRealm.setFavorite(true);
                ret = true;
            }
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
    }

    public static void removeAllFavorites() {

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).findAll();

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }

        for (TrackRealm trackRealm : realmResults) {

            trackRealm.setFavorite(false);
        }
        myRealm.commitTransaction();
        myRealm.close();
    }

    public static LinkedHashMap<String, QueueItem> getMostPlayed(int size) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).notEqualTo("songs", 0).findAll();

        realmResults = realmResults.sort("lastPlayed", Sort.DESCENDING);
        realmResults = realmResults.sort("songs", Sort.DESCENDING);

        for (int i = 0; i < Math.min(realmResults.size(), size); i++) {

            if (prefHideUnknown) {
                if (realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist") || realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist")) {
                    continue;
                }
            }

            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(realmResults.get(i).getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (realmResults.get(i).isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (realmResults.get(i).getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem queueItem = getQueueItemFromTrack(realmResults.get(i));
            queueItemLinkedHashMap.put(realmResults.get(i).getData(), queueItem);
        }

        myRealm.close();
        return queueItemLinkedHashMap;
    }

    public static Boolean increasePlayedCount(QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            trackRealm.setSongs(trackRealm.getSongs() + 1);
            trackRealm.setLastPlayed(String.valueOf(System.currentTimeMillis() / 1000));
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
        return true;
    }

    public static Boolean resetMostPlayedCount(QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            trackRealm.setSongs(0);
            trackRealm.setLastPlayed(String.valueOf(System.currentTimeMillis() / 1000));
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
        return true;
    }

    public static boolean resetPlayedCount() {
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).findAll();
        myRealm.beginTransaction();

        for (TrackRealm trackRealm : trackRealms) {

            trackRealm.setSongs(0);

        }
        myRealm.commitTransaction();
        myRealm.close();
        return true;
    }

    public static void saveLyrics(Lyrics lyrics) {
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).beginGroup()
                .equalTo("artist_name", lyrics.getOriginalArtist())
                .or()
                .equalTo("artist_name", lyrics.getArtist())
                .endGroup()
                .beginGroup()
                .equalTo("title", lyrics.getOriginalTrack())
                .or()
                .equalTo("title", lyrics.getTrack())
                .endGroup()
                .findAll();

        myRealm.beginTransaction();

        for (TrackRealm trackRealm : trackRealms) {

            trackRealm.setOriginalArtist(lyrics.getOriginalArtist());
            trackRealm.setOriginalTitle(lyrics.getOriginalTrack());
            trackRealm.setLyricsSourceUrl(lyrics.getURL());
            trackRealm.setLyricsCoverURL(lyrics.getCoverURL());
            trackRealm.setLyrics(lyrics.getText());
            trackRealm.setLyricsSource(lyrics.getSource());
            trackRealm.setlRC(lyrics.isLRC());
            trackRealm.setLyricsFlag(lyrics.getFlag());

        }

        myRealm.commitTransaction();
        myRealm.close();
    }

    public static Lyrics getLyricsforTrack(String data) {
        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", data)
                .findFirst();

        if (trackRealm != null) {
            if (trackRealm.getLyrics() == null) {
                myRealm.close();
                return null;
            } else {
                Lyrics lyrics = new Lyrics(trackRealm.getLyricsFlag());
                lyrics.setTitle(trackRealm.getTitle());
                lyrics.setArtist(trackRealm.getArtist_name());
                lyrics.setOriginalArtist(trackRealm.getOriginalArtist());
                lyrics.setOriginalTitle(trackRealm.getOriginalTitle());
                lyrics.setURL(trackRealm.getOriginalArtist());
                lyrics.setCoverURL(trackRealm.getLyricsCoverURL());
                lyrics.setText(trackRealm.getLyrics());
                lyrics.setSource(trackRealm.getLyricsSource());
                lyrics.setLRC(trackRealm.islRC());
                myRealm.close();
                return lyrics;
            }
        } else {
            myRealm.close();
            return null;

        }
    }

    public static boolean resetRecentlyPlayedCount(QueueItem queueItem) {
        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();
        myRealm.beginTransaction();
        if (trackRealm != null) {

            trackRealm.setPlayOrder(0);

        }
        myRealm.commitTransaction();
        myRealm.close();
        return true;
    }

    public static LinkedHashMap<String, QueueItem> getRecentlyAdded(int size) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).notEqualTo("dateAdded", 0).findAllSorted("dateAdded", Sort.DESCENDING);

        for (int i = 0; i < Math.min(realmResults.size(), size); i++) {

            if (prefHideUnknown) {
                if (realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist") || realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist")) {
                    continue;
                }
            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(realmResults.get(i).getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (realmResults.get(i).isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (realmResults.get(i).getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem queueItem = getQueueItemFromTrack(realmResults.get(i));
            queueItemLinkedHashMap.put(realmResults.get(i).getData(), queueItem);
        }

        myRealm.close();
        return queueItemLinkedHashMap;
    }

    public static LinkedHashMap<String, QueueItem> getRecentlyPlayed(int size) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).notEqualTo("playOrder", 0).sort("playOrder", Sort.DESCENDING).findAll();

        for (int i = 0; i < Math.min(realmResults.size(), size); i++) {

            if (prefHideUnknown) {
                if (realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist") || realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist")) {
                    continue;
                }
            }
            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(realmResults.get(i).getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            if (realmResults.get(i).isRemoved()) {
                continue;
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (realmResults.get(i).getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem queueItem = getQueueItemFromTrack(realmResults.get(i));
            queueItemLinkedHashMap.put(realmResults.get(i).getData(), queueItem);
        }

        myRealm.close();
        return queueItemLinkedHashMap;
    }

    public static QueueItem getMostRecentlyPlayed() {

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults;

        realmResults = myRealm.where(TrackRealm.class).notEqualTo("playOrder", 0).findAllSorted("playOrder", Sort.DESCENDING);

        QueueItem queueItem = null;

        if (realmResults.size() > 0) {
            queueItem = getQueueItemFromTrack(realmResults.get(0));
        }
        myRealm.close();
        return queueItem;
    }

    public static QueueItem getSecondMostRecentlyPlayed() {

        Realm myRealm = Realm.getDefaultInstance();
        int currentid = myRealm.where(TrackRealm.class).max("playOrder").intValue();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class).equalTo("playOrder", currentid - 1).findFirst();

        QueueItem queueItem = null;

        if (trackRealm != null) {
            queueItem = getQueueItemFromTrack(trackRealm);
        }
        myRealm.close();
        return queueItem;
    }

    public static Boolean updateRecentPlayed(QueueItem queueItem) {

        Realm myRealm = Realm.getDefaultInstance();

        int currentid = myRealm.where(TrackRealm.class).max("playOrder").intValue();
        int nextid;

        if (currentid == 0) {
            nextid = 1;
        } else

        {
            nextid = (currentid + 1);
        }


        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            trackRealm.setPlayOrder(nextid);
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
        return true;
    }

    public static Boolean updateDateModified(QueueItem queueItem, long dateModified) {

        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("data", queueItem.data)
                .findFirst();

        if (trackRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            trackRealm.setDateModified(dateModified);
            myRealm.insertOrUpdate(trackRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
        return true;
    }

    public static LinkedHashMap<String, QueueItem> getNegative(int storageFilter) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        if (prefHideSongDuration != 0) {
            prefHideSongDuration = prefHideSongDuration * 1000;
        }
        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);


        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<TrackRealm> realmResults;

        if (storageFilter == -1) {
            if (connectedCloudDrives.size() == 0) {
                return queueItemLinkedHashMap;
            }
            realmResults = myRealm.where(TrackRealm.class).equalTo("removed", true).in("storage", connectedCloudDrives.toArray(new Integer[connectedCloudDrives.size()])).findAll();
        } else if (storageFilter == 0) {
            realmResults = myRealm.where(TrackRealm.class).equalTo("removed", true).findAll();
        } else {
            realmResults = myRealm.where(TrackRealm.class).equalTo("removed", true).equalTo("storage", storageFilter).findAll();
        }


        for (int i = 0; i < realmResults.size(); i++) {

            if (prefHideUnknown) {
                if (realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist") || realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist")) {
                    continue;
                }
            }

            if (prefHideSongDuration > 0) {
                if (Integer.parseInt(realmResults.get(i).getDuration()) < prefHideSongDuration) {
                    continue;
                }
            }

            boolean excluded = false;
            for (StorageFolder storageFolder : StorageFolderRealmHelper.getExcludedFolders()) {
                if (realmResults.get(i).getFolder_path().toLowerCase().contains(storageFolder.getPath().toLowerCase())) {
                    excluded = true;
                    continue;
                }
            }

            if (excluded) {
                continue;
            }

            QueueItem queueItem = getQueueItemFromTrack(realmResults.get(i));
            queueItemLinkedHashMap.put(realmResults.get(i).getData(), queueItem);
        }

        myRealm.close();
        return queueItemLinkedHashMap;
    }

    public static LinkedHashMap<String, QueueItem> searchTracks(RealmResults<TrackRealm> realmResults) {

        //		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.Instance().getApplicationContext());
        //		int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        //		if (prefHideSongDuration != 0) {
        //			prefHideSongDuration = prefHideSongDuration * 1000;
        //		}
        //		boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);

        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();


        QueueItem queueItem;
        for (TrackRealm trackRealm : realmResults) {

            if (trackRealm.isRemoved()) {
                continue;
            }

            queueItem = getQueueItemFromTrack(trackRealm);
            queueItemLinkedHashMap.put(trackRealm.getData(), queueItem);
        }

        return queueItemLinkedHashMap;
    }

    public static LinkedHashMap<String, QueueItem> searchAlbums(RealmResults<TrackRealm> realmResults) {

        //		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.Instance().getApplicationContext());
        //		int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        //		if (prefHideSongDuration != 0) {
        //			prefHideSongDuration = prefHideSongDuration * 1000;
        //		}
        //		boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);

        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        for (TrackRealm trackRealm : realmResults) {
//
//			if (prefHideUnknown) {
//				if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist" || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist") {
//					continue;
//				}
//
//			}
//			if (prefHideSongDuration > 0) {
//				if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//					continue;
//				}
//			}

            if (trackRealm.isRemoved()) {
                continue;
            }

            QueueItem model;
            model = queueItemLinkedHashMap.get(trackRealm.getAlbum_name());
            if (model != null) {
                model.songs++;
                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);
            } else {
                model = new QueueItem();
                model.id = 0;
                model.album = trackRealm.getAlbum();
                model.title = trackRealm.getAlbum_name();
                model.artist_name = trackRealm.getArtist_name();
                model.genre_name = trackRealm.getGenre_name();
                model.date = trackRealm.getDate();
                model.songs = 1;
                model.duration = trackRealm.getDuration();
                model.dateModified = trackRealm.getDateModified();
                model.data = trackRealm.getData();
                model.noCover = trackRealm.isNoCover();
                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());

                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);

            }
        }

        return queueItemLinkedHashMap;
    }

    public static LinkedHashMap<String, QueueItem> searchArtists(RealmResults<TrackRealm> realmResults) {

        //		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.Instance().getApplicationContext());
        //		int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
        //		if (prefHideSongDuration != 0) {
        //			prefHideSongDuration = prefHideSongDuration * 1000;
        //		}
        //		boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);

        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();

        for (TrackRealm trackRealm : realmResults) {
//			if (prefHideUnknown) {
//				if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist" || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist") {
//					continue;
//				}
//
//			}
//			if (prefHideSongDuration > 0) {
//				if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//					continue;
//				}
//			}

            if (trackRealm.isRemoved()) {
                continue;
            }

            QueueItem model;
            model = queueItemLinkedHashMap.get(trackRealm.getArtist_name());
            if (model != null) {
                model.songs++;
                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);
            } else {
                model = new QueueItem();
                model.id = 0;
                model.album = trackRealm.getAlbum();
                model.title = trackRealm.getArtist_name();
                model.songs = 1;
                model.duration = trackRealm.getDuration();
                model.date = trackRealm.getDate();
                model.dateModified = trackRealm.getDateModified();
                model.data = trackRealm.getData();
                model.noCover = trackRealm.isNoCover();
                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());

                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);

            }
        }

        return queueItemLinkedHashMap;
    }

    public static boolean trackExists(String folderPath, long cloudAccountId) {
        Realm myRealm = Realm.getDefaultInstance();
        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .beginGroup()
                .equalTo("folder_path", folderPath)
                .equalTo("storage", cloudAccountId)
                .endGroup()
                .findFirst();
        if (trackRealm == null) {
            myRealm.close();
            return false;
        } else {
            myRealm.close();
            return true;
        }
    }

    private static QueueItem getQueueItemFromTrack(TrackRealm trackRealm) {
        QueueItem queueItem;
        queueItem = new QueueItem();
        queueItem.data = trackRealm.getData();
        queueItem.cloudId = trackRealm.getCloudId();
        queueItem.id = trackRealm.getId();
        queueItem.song = trackRealm.getSong();
        queueItem.album = trackRealm.getAlbum();
        queueItem.artist = trackRealm.getArtist();
        queueItem.name = trackRealm.getName();
        queueItem.title = trackRealm.getTitle();
        queueItem.artist_name = trackRealm.getArtist_name();
        queueItem.album_name = trackRealm.getAlbum_name();
        queueItem.genre_name = trackRealm.getGenre_name();
        queueItem.hash = trackRealm.getHash();
        queueItem.duration = trackRealm.getDuration();
        queueItem.composer = trackRealm.getComposer();
        queueItem.date = trackRealm.getDate();
        queueItem.dateModified = trackRealm.getDateModified();
        queueItem.track = trackRealm.getTrack();
        queueItem.year = trackRealm.getYear();
        queueItem.storage = trackRealm.getStorage();
        queueItem.url = trackRealm.getUrl();
        queueItem.noCover = trackRealm.isNoCover();
        queueItem.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
        queueItem.lastPlayed = trackRealm.getLastPlayed();
        queueItem.songs = trackRealm.getSongs();
        queueItem.rating = trackRealm.getRating();
        queueItem.lyrics = trackRealm.getLyrics();
        queueItem.lRC = trackRealm.islRC();
        queueItem.acrid = trackRealm.getAcrid();
        queueItem.startFrom = trackRealm.getStartFrom();
        queueItem.library = trackRealm.isLibrary();
        queueItem.size = trackRealm.getSize();
        queueItem.folder_path = trackRealm.getFolder_path();
        queueItem.folder_name = trackRealm.getFolder_name();
        queueItem.md5 = trackRealm.getMd5();
        queueItem.sync = trackRealm.isFavsync();
        queueItem.favorite = trackRealm.isFavorite();
        queueItem.removed = trackRealm.isRemoved();

        return queueItem;
    }
}