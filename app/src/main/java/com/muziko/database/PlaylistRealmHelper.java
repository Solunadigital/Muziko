package com.muziko.database;

import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.PlaylistQueueItem;
import com.muziko.common.models.QueueItem;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class PlaylistRealmHelper {
    public static final String TAG = PlaylistRealmHelper.class.getSimpleName();

    public static boolean delete(long id) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                .equalTo("id", id)
                .findFirst();

        if (playlistRealm != null) {
            myRealm.beginTransaction();
            playlistRealm.deleteFromRealm();
            myRealm.commitTransaction();
            myRealm.close();

            return true;
        } else {

            myRealm.close();

            return false;
        }
    }

    public static ArrayList<PlaylistItem> loadAllWithSync() {

        ArrayList<PlaylistItem> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<PlaylistRealm> realmResults = myRealm.where(PlaylistRealm.class).equalTo("sync", true).findAllSorted("order", Sort.ASCENDING);

        for (int i = 0; i < realmResults.size(); i++) {

            final ArrayList<QueueItem> queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, realmResults.get(i).getId());

            PlaylistItem item = new PlaylistItem();
            item.id = realmResults.get(i).getId();
            item.title = realmResults.get(i).getTitle();
            if (queueItems.size() > 0) {
                item.album = queueItems.get(0).album;
            } else {
                item.album = 0;
            }
            item.data = realmResults.get(i).getData();
            item.hash = realmResults.get(i).getHash();
            item.songs = queueItems.size();
            item.order = realmResults.get(i).getOrder();
            item.date = realmResults.get(i).getDate();
            item.duration = String.valueOf(PlaylistSongRealmHelper.getPlaylistDuration(realmResults.get(i).getId()));
            item.sync = realmResults.get(i).isSync();
            list.add(item);
        }


        myRealm.close();
        return list;
    }

    public static ArrayList<PlaylistItem> loadAll() {

        ArrayList<PlaylistItem> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<PlaylistRealm> realmResults = myRealm.where(PlaylistRealm.class).findAllSorted("order", Sort.ASCENDING);

        for (int i = 0; i < realmResults.size(); i++) {

            final ArrayList<QueueItem> queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, realmResults.get(i).getId());

            PlaylistItem item = new PlaylistItem();
            item.id = realmResults.get(i).getId();
            item.title = realmResults.get(i).getTitle();
            if (queueItems.size() > 0) {
                item.album = queueItems.get(0).album;
            } else {
                item.album = 0;
            }
            item.data = realmResults.get(i).getData();
            item.hash = realmResults.get(i).getHash();
            item.songs = queueItems.size();
            item.order = realmResults.get(i).getOrder();
            item.date = realmResults.get(i).getDate();
            item.duration = String.valueOf(PlaylistSongRealmHelper.getPlaylistDuration(realmResults.get(i).getId()));
            item.sync = realmResults.get(i).isSync();
            list.add(item);
        }


        myRealm.close();
        return list;
    }

    public static void update(PlaylistQueueItem playlistQueueItem, int songs, long duration) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                .equalTo("id", playlistQueueItem.playlist)
                .findFirst();

        if (playlistRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            if (playlistRealm.getData() == null || playlistRealm.getData().isEmpty()) {
                playlistRealm.setAlbum(playlistQueueItem.album);
                playlistRealm.setData(playlistQueueItem.data);
            }

            playlistRealm.setSongs(playlistRealm.getSongs() + songs);

            long totalduration = 0;
            if (playlistRealm.getDuration() == null) {

                totalduration = duration;
            } else {
                totalduration = Long.parseLong(playlistRealm.getDuration()) + duration;
            }

            playlistRealm.setDuration(String.valueOf(totalduration));
            playlistRealm.setDate(System.currentTimeMillis());

            myRealm.copyToRealmOrUpdate(playlistRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();
    }

    public static void updateOrder(PlaylistItem playlistItem, int order) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                .equalTo("id", playlistItem.id)
                .findFirst();

        if (playlistRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            playlistRealm.setOrder(order);
            playlistRealm.setDate(System.currentTimeMillis());

            myRealm.copyToRealmOrUpdate(playlistRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();
    }

    public static void clearPlaylist(long pl) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                .equalTo("id", pl)
                .findFirst();

        if (playlistRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            playlistRealm.setAlbum(0);
            playlistRealm.setData("");
            playlistRealm.setSongs(0);
            playlistRealm.setDuration("0");
            playlistRealm.setDate(System.currentTimeMillis());

            myRealm.copyToRealmOrUpdate(playlistRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();
    }

    public static void removeOneFromPlaylist(QueueItem queueItem) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                .equalTo("id", queueItem.playlist)
                .findFirst();

        if (playlistRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            if (playlistRealm.getData().equals(queueItem.data)) {
                PlaylistSongRealm playlistSongRealm = myRealm.where(PlaylistSongRealm.class).equalTo("playlist", queueItem.playlist).findFirst();
                if (playlistSongRealm != null) {
                    QueueItem trackRealm = TrackRealmHelper.getTrack(playlistSongRealm.getData());
                    playlistRealm.setAlbum(trackRealm.album);
                    playlistRealm.setData(trackRealm.data);
                }
            }

            playlistRealm.setSongs(playlistRealm.getSongs() - 1);
            playlistRealm.setDuration(String.valueOf(PlaylistSongRealmHelper.getPlaylistDuration(queueItem.playlist)));
            playlistRealm.setDate(System.currentTimeMillis());
            myRealm.copyToRealmOrUpdate(playlistRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return;
        }

        myRealm.close();
    }

    public static PlaylistItem getPlaylist(String hash) {
        PlaylistItem item = null;
        Realm myRealm = Realm.getDefaultInstance();
        try {
            PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                    .equalTo("hash", hash)
                    .findFirst();

            if (playlistRealm != null) {

                item = new PlaylistItem();
                item.id = playlistRealm.getId();
                item.hash = playlistRealm.getHash();
                item.title = playlistRealm.getTitle();
                item.album = playlistRealm.getAlbum();
                item.data = playlistRealm.getData();
                item.songs = playlistRealm.getSongs();
                item.order = playlistRealm.getOrder();
                item.date = playlistRealm.getDate();
                if (playlistRealm.getDuration() == null) {
                    item.duration = "0";
                } else {
                    item.duration = playlistRealm.getDuration();
                }
                item.sync = playlistRealm.isSync();
                myRealm.close();
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
            return item;
        }
    }

    public static PlaylistItem getPlaylist(long kid) {
        PlaylistItem playlistItem = null;

        Realm myRealm = Realm.getDefaultInstance();
        try {
            PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                    .equalTo("id", kid)
                    .findFirst();

            if (playlistRealm != null) {

                playlistItem = new PlaylistItem();
                playlistItem.id = playlistRealm.getId();
                playlistItem.hash = playlistRealm.getHash();
                playlistItem.title = playlistRealm.getTitle();
                playlistItem.album = playlistRealm.getAlbum();
                playlistItem.data = playlistRealm.getData();
                playlistItem.songs = playlistRealm.getSongs();
                playlistItem.order = playlistRealm.getOrder();
                playlistItem.date = playlistRealm.getDate();
                if (playlistRealm.getDuration() == null) {
                    playlistItem.duration = "0";
                } else {
                    playlistItem.duration = playlistRealm.getDuration();
                }
                playlistItem.sync = playlistRealm.isSync();
                myRealm.close();

            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
            return playlistItem;
        }
    }


    public static long insert(PlaylistItem playlistItem) {

        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<PlaylistRealm> realmResults = myRealm.where(PlaylistRealm.class).findAll();

        int nextorder;
        if (realmResults.size() == 0) {
            nextorder = 1;
        } else {
            nextorder = (myRealm.where(PlaylistRealm.class).max("order").intValue() + 1);
        }

        PlaylistRealm playlistRealm = new PlaylistRealm();
        playlistRealm.setId(playlistItem.id);
        playlistRealm.setHash(playlistItem.hash);
        playlistRealm.setTitle(playlistItem.title);
        playlistRealm.setData(playlistItem.data);
        playlistRealm.setSongs(playlistItem.songs);
        playlistRealm.setOrder(nextorder);
        playlistRealm.setDate(System.currentTimeMillis());
        playlistRealm.setSync(playlistItem.sync);
        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }
        myRealm.copyToRealmOrUpdate(playlistRealm);
        myRealm.commitTransaction();

        myRealm.close();

        return playlistItem.id;
    }

    public static void toggleSync(long playlistId, boolean sync) {

        Realm myRealm = Realm.getDefaultInstance();

        PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                .equalTo("id", playlistId)
                .findFirst();

        if (playlistRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }
            if (sync) {
                playlistRealm.setSync(true);
            } else {
                playlistRealm.setSync(false);
            }
            playlistRealm.setDate(System.currentTimeMillis());
            myRealm.insertOrUpdate(playlistRealm);
            myRealm.commitTransaction();
        }

        myRealm.close();
    }

    public static boolean update(PlaylistItem playlistItem) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistRealm playlistRealm = myRealm.where(PlaylistRealm.class)
                .equalTo("id", playlistItem.id)
                .findFirst();

        if (playlistRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            playlistRealm.setTitle(playlistItem.title);
            playlistRealm.setData(playlistItem.data);
            playlistRealm.setSongs(playlistItem.songs);
            playlistRealm.setDate(System.currentTimeMillis());

            myRealm.copyToRealmOrUpdate(playlistRealm);
            myRealm.commitTransaction();

            myRealm.close();

            return true;
        }

        myRealm.close();
        return false;
    }
}
