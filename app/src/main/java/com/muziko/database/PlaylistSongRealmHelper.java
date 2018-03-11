package com.muziko.database;

import com.muziko.common.models.PlaylistQueueItem;
import com.muziko.common.models.QueueItem;

import java.util.ArrayList;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class PlaylistSongRealmHelper {

    private static final String TAG = PlaylistSongRealmHelper.class.getSimpleName();


    public static boolean delete(long id) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistSongRealm playlistSongRealm = myRealm.where(PlaylistSongRealm.class)
                .equalTo("key", id)
                .findFirst();

        if (playlistSongRealm != null) {
            myRealm.beginTransaction();
            playlistSongRealm.deleteFromRealm();
            myRealm.commitTransaction();
            myRealm.close();

            return true;
        } else {

            myRealm.close();
            return false;
        }
    }

    public static void deleteByPlaylist(long pl) {
        Realm myRealm = Realm.getDefaultInstance();

        myRealm.beginTransaction();
        myRealm.where(PlaylistSongRealm.class).equalTo("playlist", pl).findAll().deleteAllFromRealm();
        myRealm.commitTransaction();
        myRealm.close();

        PlaylistRealmHelper.clearPlaylist(pl);

    }

    public static boolean deleteByData(String data) {
        Realm myRealm = Realm.getDefaultInstance();

        PlaylistSongRealm playlistSongRealm = myRealm.where(PlaylistSongRealm.class)
                .equalTo("data", data)
                .findFirst();

        if (playlistSongRealm != null) {
            myRealm.beginTransaction();
            playlistSongRealm.deleteFromRealm();
            myRealm.commitTransaction();
            myRealm.close();

            return true;
        } else {

            myRealm.close();
            return false;
        }
    }

    public static boolean deleteLikeData(String data) {
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults realmResults = myRealm.where(PlaylistSongRealm.class)
                .contains("data", data, Case.INSENSITIVE)
                .findAll();

        if (realmResults != null) {

            myRealm.beginTransaction();
            myRealm.where(PlaylistSongRealm.class).contains("data", data, Case.INSENSITIVE).findAll().deleteAllFromRealm();
            myRealm.commitTransaction();
            myRealm.close();

            return true;
        } else {
            myRealm.close();

            return false;
        }
    }

    public static int getCount() {
        int count = 0;
        Realm myRealm = Realm.getDefaultInstance();
        try {
            RealmResults<PlaylistSongRealm> realmResults = myRealm.where(PlaylistSongRealm.class).findAll();

            if (realmResults != null) {
                count = realmResults.size();
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
        }
        return count;
    }


    public static int getCountByPlaylist(long pl) {
        int count = 0;
        Realm myRealm = Realm.getDefaultInstance();
        try {
            RealmResults<PlaylistSongRealm> realmResults = myRealm.where(PlaylistSongRealm.class).equalTo("playlist", pl).findAll();

            if (realmResults != null) {
                count = realmResults.size();
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
        }
        return count;
    }


    public static int getPlaylistCount(long playlist) {
        int size = 0;

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<PlaylistSongRealm> realmResults;

        realmResults = myRealm.where(PlaylistSongRealm.class).equalTo("playlist", playlist).findAll();
        if (realmResults.size() > 0) {
            for (int i = 0; i < realmResults.size(); i++) {

                QueueItem queueItem = new QueueItem();
                queueItem = TrackRealmHelper.getTrackforPlaylist(realmResults.get(i).getData());
                if (queueItem != null) {
                    size++;
                }

            }
        }
        myRealm.close();
        return size;
    }

    public static long getPlaylistDuration(long playlist) {
        long duration = 0;

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<PlaylistSongRealm> realmResults;

        realmResults = myRealm.where(PlaylistSongRealm.class).equalTo("playlist", playlist).findAll();
        if (realmResults.size() > 0) {
            for (int i = 0; i < realmResults.size(); i++) {

                QueueItem queueItem;
                queueItem = TrackRealmHelper.getTrackforPlaylist(realmResults.get(i).getData());
                if (queueItem != null) {
                    duration = duration + Long.parseLong(queueItem.duration);
                }

            }
        }
        myRealm.close();
        return duration;
    }

    public static ArrayList<QueueItem> loadAllByPlaylist(int storageFilter, long playlist) {
        ArrayList<QueueItem> list = new ArrayList<>();

        Realm myRealm = Realm.getDefaultInstance();
        RealmResults<PlaylistSongRealm> realmResults;

        realmResults = myRealm.where(PlaylistSongRealm.class).equalTo("playlist", playlist).findAll();
        if (realmResults.size() > 0) {
            for (int i = 0; i < realmResults.size(); i++) {

                QueueItem queueItem = new QueueItem();
                queueItem = TrackRealmHelper.getTrackforPlaylist(realmResults.get(i).getData());
                if (queueItem != null) {
                    switch (storageFilter) {
                        case 0:
                            queueItem.id = realmResults.get(i).getKey();
                            queueItem.playlist = realmResults.get(i).getPlaylist();

                            if (queueItem != null) {
                                list.add(queueItem);
                            }
                            break;
                        case 1:
                            if (queueItem.storage == 0) {
                                queueItem.id = realmResults.get(i).getKey();
                                queueItem.playlist = realmResults.get(i).getPlaylist();

                                if (queueItem != null) {
                                    list.add(queueItem);
                                }
                            }
                            break;
                        case 2:
                            if (queueItem.storage == 1) {
                                queueItem.id = realmResults.get(i).getKey();
                                queueItem.playlist = realmResults.get(i).getPlaylist();

                                if (queueItem != null) {
                                    list.add(queueItem);
                                }
                            }
                            break;
                        default:
                            queueItem.id = realmResults.get(i).getKey();
                            queueItem.playlist = realmResults.get(i).getPlaylist();

                            if (queueItem != null) {
                                list.add(queueItem);
                            }
                            break;
                    }
                }

            }
        }
        myRealm.close();
        return list;
    }

    public static boolean get(long kid) {
        boolean ret = false;
        Realm myRealm = Realm.getDefaultInstance();
        try {
            PlaylistSongRealm playlistSongRealm = myRealm.where(PlaylistSongRealm.class)
                    .equalTo("id", kid)
                    .findFirst();

            if (playlistSongRealm != null) {
                myRealm.close();
                return true;
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
        }
        return ret;
    }

    public static boolean get(String data) {
        boolean ret = false;
        Realm myRealm = Realm.getDefaultInstance();
        try {
            PlaylistSongRealm playlistSongRealm = myRealm.where(PlaylistSongRealm.class)
                    .equalTo("data", data)
                    .findFirst();

            if (playlistSongRealm != null) {
                myRealm.close();
                return true;
            }
        } catch (Exception e) {
            myRealm.close();
        } finally {
            myRealm.close();
        }
        return ret;
    }

    public static long insert(PlaylistQueueItem playlistQueueItem, boolean override) {
        Realm myRealm = Realm.getDefaultInstance();
        int nextid;

        RealmResults<PlaylistSongRealm> realmResults = myRealm.where(PlaylistSongRealm.class).findAll();
        if (realmResults.size() == 0) {
            nextid = 1;
        } else {
            nextid = (myRealm.where(PlaylistSongRealm.class).max("key").intValue() + 1);
        }

        PlaylistSongRealm playlistSongRealm = new PlaylistSongRealm();

        if (!override) {
            playlistSongRealm.setKey(nextid);
            playlistSongRealm.setData(playlistQueueItem.data);
            playlistSongRealm.setPlaylist(playlistQueueItem.playlist);
        } else {
            PlaylistSongRealm existing = myRealm.where(PlaylistSongRealm.class)
                    .equalTo("data", playlistQueueItem.data)
                    .equalTo("playlist", playlistQueueItem.id)
                    .findFirst();

            if (existing == null) {
                playlistSongRealm.setKey(nextid);
                playlistSongRealm.setData(playlistQueueItem.data);
                playlistSongRealm.setPlaylist(playlistQueueItem.playlist);
            }
        }

        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }
        myRealm.insert(playlistSongRealm);
        myRealm.commitTransaction();

        myRealm.close();

        PlaylistRealmHelper.update(playlistQueueItem, 1, Long.parseLong(playlistQueueItem.duration));

        return nextid;
    }


    public static int insertList(ArrayList<PlaylistQueueItem> playlistQueueItems, boolean override) {

        Realm myRealm = Realm.getDefaultInstance();
        int nextid;
        int counter = 0;

        try {

            RealmResults<PlaylistSongRealm> realmResults = myRealm.where(PlaylistSongRealm.class).findAll();
            if (realmResults.size() == 0) {
                nextid = 1;
            } else

            {
                nextid = (myRealm.where(PlaylistSongRealm.class).max("key").intValue() + 1);
            }

            long duration = 0;

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            for (PlaylistQueueItem playlistQueueItem : playlistQueueItems) {
                PlaylistSongRealm playlistSongRealm = new PlaylistSongRealm();

                if (!override) {
                    playlistSongRealm.setKey(nextid);
                    playlistSongRealm.setData(playlistQueueItem.data);
                    playlistSongRealm.setPlaylist(playlistQueueItem.playlist);
                    myRealm.insert(playlistSongRealm);
                    nextid = nextid + 1;

                    duration = duration + Long.parseLong(playlistQueueItem.duration);
                    counter++;
                } else {
                    PlaylistSongRealm existing = myRealm.where(PlaylistSongRealm.class)
                            .equalTo("data", playlistQueueItem.data)
                            .equalTo("playlist", playlistQueueItem.playlist)
                            .findFirst();

                    if (existing == null) {
                        playlistSongRealm.setKey(nextid);
                        playlistSongRealm.setData(playlistQueueItem.data);
                        playlistSongRealm.setPlaylist(playlistQueueItem.playlist);
                        myRealm.insert(playlistSongRealm);
                        nextid = nextid + 1;

                        duration = duration + Long.parseLong(playlistQueueItem.duration);
                        counter++;
                    }
                }

            }

            myRealm.commitTransaction();

            myRealm.close();

            PlaylistRealmHelper.update(playlistQueueItems.get(0), counter, duration);
            return counter;
        } catch (Exception e) {
            myRealm.close();
            return counter;
        }
    }

}
