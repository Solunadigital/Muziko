package com.muziko.database;

/**
 * Created by dev on 17/07/2016.
 */
public class TrackRealmHelper {

//    public static int insertList(LinkedHashMap<String, QueueItem> queueItems) {
//        boolean deleted = false;
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).findAll();
//
//        if (trackRealms.size() == 0) {
//            PicassoTools.clearCache(Picasso.with(MuzikoWearApp.getInstance().getApplicationContext()));
//        }
//
//        if (!myRealm.isInTransaction()) {
//            myRealm.beginTransaction();
//        }
//
//        for (QueueItem queueItem : queueItems.values()) {
//            TrackRealm trackRealm;
//
//            // first run
//            if (trackRealms.size() == 0) {
//                trackRealm = new TrackRealm();
//                trackRealm.setData(queueItem.data);
//                trackRealm.setId(queueItem.id);
//                trackRealm.setSong(queueItem.song);
//                trackRealm.setAlbum(queueItem.album);
//                trackRealm.setArtist(queueItem.artist);
//                trackRealm.setName(queueItem.name);
//                trackRealm.setTitle(queueItem.title);
//                trackRealm.setArtist_name(queueItem.artist_name);
//                trackRealm.setAlbum_name(queueItem.album_name);
//                trackRealm.setGenre_name(queueItem.genre_name);
//                trackRealm.setHash(queueItem.hash);
//                trackRealm.setDuration(queueItem.duration);
//                trackRealm.setDate(queueItem.date);
//                if (queueItem.dateModified == null || queueItem.dateModified.isEmpty()) {
//                    trackRealm.setDateModified(System.currentTimeMillis());
//                } else {
//                    trackRealm.setDateModified(Long.parseLong(queueItem.dateModified));
//                }
//                trackRealm.setComposer(queueItem.composer);
//                trackRealm.setTrack(queueItem.track);
//                trackRealm.setYear(queueItem.year);
//                trackRealm.setStorage(queueItem.storage);
//                trackRealm.setFolder_name(queueItem.folder_name);
//                trackRealm.setFolder_path(queueItem.folder_path);
//                myRealm.insertOrUpdate(trackRealm);
//
//            } else {
//
//                // run already
//                trackRealm = myRealm.where(TrackRealm.class).equalTo("data", queueItem.data).findFirst();
//                // exists
//                if (trackRealm != null) {
//                    trackRealm.setId(queueItem.id);
//                    trackRealm.setSong(queueItem.song);
//                    trackRealm.setAlbum(queueItem.album);
//                    trackRealm.setArtist(queueItem.artist);
//                    trackRealm.setName(queueItem.name);
//                    trackRealm.setTitle(queueItem.title);
//                    trackRealm.setArtist_name(queueItem.artist_name);
//                    trackRealm.setAlbum_name(queueItem.album_name);
//                    trackRealm.setGenre_name(queueItem.genre_name);
//                    trackRealm.setHash(queueItem.hash);
//                    trackRealm.setDuration(queueItem.duration);
//                    trackRealm.setDate(queueItem.date);
//                    if (queueItem.dateModified == null || queueItem.dateModified.isEmpty()) {
//                        trackRealm.setDateModified(System.currentTimeMillis());
//                    } else {
//                        trackRealm.setDateModified(Long.parseLong(queueItem.dateModified));
//                    }
//                    trackRealm.setComposer(queueItem.composer);
//                    trackRealm.setTrack(queueItem.track);
//                    trackRealm.setYear(queueItem.year);
//                    trackRealm.setStorage(queueItem.storage);
//                    trackRealm.setFolder_name(queueItem.folder_name);
//                    trackRealm.setFolder_path(queueItem.folder_path);
//                    myRealm.copyToRealmOrUpdate(trackRealm);
//                } else {
//
//                    // new
//                    trackRealm = new TrackRealm();
//                    trackRealm.setData(queueItem.data);
//                    trackRealm.setId(queueItem.id);
//                    trackRealm.setSong(queueItem.song);
//                    trackRealm.setAlbum(queueItem.album);
//                    trackRealm.setArtist(queueItem.artist);
//                    trackRealm.setName(queueItem.name);
//                    trackRealm.setTitle(queueItem.title);
//                    trackRealm.setArtist_name(queueItem.artist_name);
//                    trackRealm.setAlbum_name(queueItem.album_name);
//                    trackRealm.setGenre_name(queueItem.genre_name);
//                    trackRealm.setHash(queueItem.hash);
//                    trackRealm.setDuration(queueItem.duration);
//                    trackRealm.setDate(queueItem.date);
//                    if (queueItem.dateModified == null || queueItem.dateModified.isEmpty()) {
//                        trackRealm.setDateModified(System.currentTimeMillis());
//                    } else {
//                        trackRealm.setDateModified(Long.parseLong(queueItem.dateModified));
//                    }
//                    trackRealm.setComposer(queueItem.composer);
//                    trackRealm.setTrack(queueItem.track);
//                    trackRealm.setYear(queueItem.year);
//                    trackRealm.setStorage(queueItem.storage);
//                    trackRealm.setFolder_name(queueItem.folder_name);
//                    trackRealm.setFolder_path(queueItem.folder_path);
//                    myRealm.insertOrUpdate(trackRealm);
//                }
//            }
//        }
//
//        myRealm.commitTransaction();
//
//        trackRealms = myRealm.where(TrackRealm.class).findAll();
//        if (!myRealm.isInTransaction()) {
//            myRealm.beginTransaction();
//        }
//        for (TrackRealm trackRealm : trackRealms) {
//
//            QueueItem queueItem = queueItems.get(trackRealm.getData());
//            if (queueItem == null) {
//                deleted = true;
//                trackRealm.deleteFromRealm();
//            }
//        }
//
//        myRealm.commitTransaction();
//        myRealm.close();
//
//        Prefs.setDatabaseReady(MuzikoWearApp.getInstance().getApplicationContext(), true);
//
//        if (deleted) {
//            Utils.toast(MuzikoWearApp.getInstance().getApplicationContext(), "Cleaned up removed tracks");
//        }
//        return queueItems.size();
//    }
//
//    public static void insertTrack(QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//
//        if (!myRealm.isInTransaction()) {
//            myRealm.beginTransaction();
//        }
//
//
//        TrackRealm trackRealm;
//
//        // run already
//        trackRealm = myRealm.where(TrackRealm.class).equalTo("data", queueItem.data).findFirst();
//        // exists
//        if (trackRealm != null) {
//            trackRealm.setId(queueItem.id);
//            trackRealm.setSong(queueItem.song);
//            trackRealm.setAlbum(queueItem.album);
//            trackRealm.setArtist(queueItem.artist);
//            trackRealm.setName(queueItem.name);
//            trackRealm.setTitle(queueItem.title);
//            trackRealm.setArtist_name(queueItem.artist_name);
//            trackRealm.setAlbum_name(queueItem.album_name);
//            trackRealm.setGenre_name(queueItem.genre_name);
//            trackRealm.setHash(queueItem.hash);
//            trackRealm.setDuration(queueItem.duration);
//            trackRealm.setDate(queueItem.date);
//            if (queueItem.dateModified == null || queueItem.dateModified.isEmpty()) {
//                trackRealm.setDateModified(System.currentTimeMillis());
//            } else {
//                trackRealm.setDateModified(Long.parseLong(queueItem.dateModified));
//            }
//            trackRealm.setComposer(queueItem.composer);
//            trackRealm.setTrack(queueItem.track);
//            trackRealm.setYear(queueItem.year);
//            trackRealm.setStorage(queueItem.storage);
//            trackRealm.setFolder_name(queueItem.folder_name);
//            trackRealm.setFolder_path(queueItem.folder_path);
//            myRealm.copyToRealmOrUpdate(trackRealm);
//        } else {
//
//            // new
//            trackRealm = new TrackRealm();
//            trackRealm.setData(queueItem.data);
//            trackRealm.setId(queueItem.id);
//            trackRealm.setSong(queueItem.song);
//            trackRealm.setAlbum(queueItem.album);
//            trackRealm.setArtist(queueItem.artist);
//            trackRealm.setName(queueItem.name);
//            trackRealm.setTitle(queueItem.title);
//            trackRealm.setArtist_name(queueItem.artist_name);
//            trackRealm.setAlbum_name(queueItem.album_name);
//            trackRealm.setGenre_name(queueItem.genre_name);
//            trackRealm.setHash(queueItem.hash);
//            trackRealm.setDuration(queueItem.duration);
//            trackRealm.setDate(queueItem.date);
//            if (queueItem.dateModified == null || queueItem.dateModified.isEmpty()) {
//                trackRealm.setDateModified(System.currentTimeMillis());
//            } else {
//                trackRealm.setDateModified(Long.parseLong(queueItem.dateModified));
//            }
//            trackRealm.setComposer(queueItem.composer);
//            trackRealm.setTrack(queueItem.track);
//            trackRealm.setYear(queueItem.year);
//            trackRealm.setStorage(queueItem.storage);
//            trackRealm.setFolder_name(queueItem.folder_name);
//            trackRealm.setFolder_path(queueItem.folder_path);
//            myRealm.insertOrUpdate(trackRealm);
//        }
//
//        myRealm.commitTransaction();
//        myRealm.close();
//
//    }
//
//    public static Boolean updateTrackTags(QueueItem queueItem, boolean lyricsChanged, boolean lRC) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//
//            trackRealm.setTitle(queueItem.title);
//            trackRealm.setArtist_name(queueItem.artist_name);
//            trackRealm.setAlbum_name(queueItem.album_name);
//            trackRealm.setTrack(queueItem.track);
//            trackRealm.setYear(queueItem.year);
//            trackRealm.setGenre_name(queueItem.genre_name);
//            trackRealm.setUrl(queueItem.url);
//            trackRealm.setLyrics(queueItem.lyrics);
//            trackRealm.setlRC(lRC);
//            if (lyricsChanged) {
//                trackRealm.setLyricsFlag(Lyrics.POSITIVE_RESULT);
//            }
//            myRealm.copyToRealmOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//
//            ArrayList<QueueItem> queueItems = new ArrayList<>();
//            queueItems.add(queueItem);
//            Gson gson = new Gson();
//            String tracks = gson.toJson(queueItems);
//            MuzikoWearApp.serviceUpdateCache(MuzikoWearApp.getInstance().getApplicationContext(), tracks);
//
//            return true;
//        }
//
//        myRealm.close();
//
//        return false;
//    }
//
//    public static Boolean updateMultiTrackTags(QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//
//            trackRealm.setTitle(queueItem.title);
//            trackRealm.setArtist_name(queueItem.artist_name);
//            trackRealm.setAlbum_name(queueItem.album_name);
//            trackRealm.setTrack(queueItem.track);
//            trackRealm.setYear(queueItem.year);
//            trackRealm.setGenre_name(queueItem.genre_name);
//            trackRealm.setUrl(queueItem.url);
//            myRealm.copyToRealmOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//            ArrayList<QueueItem> queueItems = new ArrayList<>();
//            queueItems.add(queueItem);
//            Gson gson = new Gson();
//            String tracks = gson.toJson(queueItems);
//            MuzikoWearApp.serviceUpdateCache(MuzikoWearApp.getInstance().getApplicationContext(), tracks);
//
//            return true;
//        }
//
//        myRealm.close();
//
//        return false;
//    }
//
//    public static void updateCoverArt(final QueueItem queueItem, final boolean noCoverArt) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//
//            trackRealm.setNoCover(noCoverArt);
//            trackRealm.setCoverUpdated(trackRealm.getDateModified());
//            myRealm.copyToRealmOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//            ArrayList<QueueItem> queueItems = new ArrayList<>();
//            queueItems.add(queueItem);
//            Gson gson = new Gson();
//            String tracks = gson.toJson(queueItems);
//            MuzikoWearApp.serviceUpdateCache(MuzikoWearApp.getInstance().getApplicationContext(), tracks);
//
//            return;
//        }
//
//        myRealm.close();
//    }
//
//    public static void updateRating(final QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//
//            trackRealm.setRating(queueItem.rating);
//            myRealm.copyToRealmOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//            return;
//        }
//
//        myRealm.close();
//    }
//
//    public static void movetoNegative(final QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//
//            trackRealm.setRemoved(true);
//            myRealm.copyToRealmOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//            return;
//        }
//
//        myRealm.close();
//    }
//
//    public static void moveoutofNegative(final QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//
//            trackRealm.setRemoved(false);
//            myRealm.copyToRealmOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//            return;
//        }
//
//        myRealm.close();
//    }
//
//
//    public static void updateCoverArtforAlbum(final QueueItem queueItem, final boolean noCoverArt) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).equalTo("album_name", queueItem.title).findAll();
//
//        if (!myRealm.isInTransaction()) {
//            myRealm.beginTransaction();
//        }
//
//        for (TrackRealm trackRealm : trackRealms) {
//            trackRealm.setNoCover(noCoverArt);
//            trackRealm.setCoverUpdated(System.currentTimeMillis());
//            myRealm.copyToRealmOrUpdate(trackRealm);
//        }
//        myRealm.commitTransaction();
//
//
//        myRealm.close();
//    }
//
//
//    public static boolean deleteTrack(String data) {
//        Realm myRealm = Realm.getDefaultInstance();
//
//        if (!myRealm.isInTransaction()) {
//            myRealm.beginTransaction();
//        }
//
//        myRealm.where(TrackRealm.class).contains("data", data, Case.INSENSITIVE).findAll().deleteAllFromRealm();
//
//        myRealm.commitTransaction();
//
//        myRealm.close();
//
//        return true;
//    }
//
//
//    public static int getCount() {
//
//        int results;
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).findAll();
//        results = realmResults.size();
//        myRealm.close();
//
//        return results;
//    }
//
//
//    public static QueueItem getTrack(String data) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//        TrackRealm trackRealm;
//
//        trackRealm = myRealm.where(TrackRealm.class).equalTo("data", data).findFirst();
//
//        QueueItem model = null;
//
//        if (trackRealm != null) {
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//        }
//        myRealm.close();
//        return model;
//    }
//
//    public static QueueItem getTrackforPlaylist(String data) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//        TrackRealm trackRealm;
//
//
//        trackRealm = myRealm.where(TrackRealm.class).equalTo("data", data).findFirst();
//
//        if (trackRealm.isRemoved()) {
//            return null;
//        }
//
//        QueueItem model = null;
//
//        if (trackRealm != null) {
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//        }
//        myRealm.close();
//        return model;
//    }
//
//    public static QueueItem getTrackByShare(Share share) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//        TrackRealm trackRealm;
//
//        trackRealm = myRealm.where(TrackRealm.class).equalTo("title", share.getTitle()).equalTo("artist_name", share.getArtist()).equalTo("album_name", share.getAlbum()).findFirst();
//
//        QueueItem model = null;
//
//        if (trackRealm != null) {
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//        }
//        myRealm.close();
//        return model;
//    }
//
//    public static QueueItem getTrackByAlbum(long album) {
//
//
//        Realm myRealm = Realm.getDefaultInstance();
//        TrackRealm trackRealm;
//
//        trackRealm = myRealm.where(TrackRealm.class).equalTo("album", album).findFirst();
//
//        QueueItem model = null;
//
//        if (trackRealm != null) {
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.folder_path = trackRealm.getFolder_path();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//        }
//        myRealm.close();
//        return model;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getTracks(int storageFilter) {
//
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//        switch (storageFilter) {
//            case 0:
//                realmResults = myRealm.where(TrackRealm.class).findAll();
//                break;
//            case 1:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("storage", false).findAll();
//                break;
//            case 2:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("storage", true).findAll();
//                break;
//            default:
//                realmResults = myRealm.where(TrackRealm.class).findAll();
//                break;
//        }
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.favorite = trackRealm.isFavorite();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            queueItemLinkedHashMap.put(trackRealm.getData(), model);
//        }
//
//        myRealm.close();
//
//        return queueItemLinkedHashMap;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getArtists() {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//        realmResults = myRealm.where(TrackRealm.class).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//            model = queueItemLinkedHashMap.get(trackRealm.getArtist_name());
//            if (model != null) {
//                model.songs++;
//                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
//                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);
//            } else {
//                model = new QueueItem();
//                model.id = 0;
//                model.album = trackRealm.getAlbum();
//                model.title = trackRealm.getArtist_name();
//                model.date = trackRealm.getDate();
//                model.songs = 1;
//                model.duration = trackRealm.getDuration();
//                model.dateModified = String.valueOf(trackRealm.getDateModified());
//                model.data = trackRealm.getData();
//                model.noCover = trackRealm.isNoCover();
//                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//
//                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);
//
//            }
//        }
//
//        myRealm.close();
//
//        return queueItemLinkedHashMap;
//    }
//
//    public static ArrayList<QueueItem> getTracksForArtist(String data) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        ArrayList<QueueItem> queueItems = new ArrayList<>();
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//        realmResults = myRealm.where(TrackRealm.class).equalTo("artist_name", data).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.favorite = trackRealm.isFavorite();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            queueItems.add(model);
//        }
//
//        myRealm.close();
//        return queueItems;
//    }
//
//
//    public static LinkedHashMap<String, QueueItem> getAlbums() {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//        realmResults = myRealm.where(TrackRealm.class).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//            model = queueItemLinkedHashMap.get(trackRealm.getAlbum_name());
//            if (model != null) {
//                model.songs++;
//                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
//                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);
//            } else {
//                model = new QueueItem();
//                model.id = 0;
//                model.album = trackRealm.getAlbum();
//                model.title = trackRealm.getAlbum_name();
//                model.artist_name = trackRealm.getArtist_name();
//                model.genre_name = trackRealm.getGenre_name();
//                model.date = trackRealm.getDate();
//                model.songs = 1;
//                model.duration = trackRealm.getDuration();
//                model.dateModified = String.valueOf(trackRealm.getDateModified());
//                model.data = trackRealm.getData();
//                model.noCover = trackRealm.isNoCover();
//                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//
//                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);
//
//            }
//        }
//
//        myRealm.close();
//
//        return queueItemLinkedHashMap;
//    }
//
//    public static ArrayList<QueueItem> getTracksForAlbum(String data) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        ArrayList<QueueItem> queueItems = new ArrayList<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//
//        realmResults = myRealm.where(TrackRealm.class).equalTo("album_name", data).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.favorite = trackRealm.isFavorite();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            queueItems.add(model);
//        }
//
//        myRealm.close();
//        return queueItems;
//    }
//
//    public static LinkedHashMap<Long, QueueItem> getTrackAlbumIdsForAlbum(String data) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//        LinkedHashMap<Long, QueueItem> queueItems = new LinkedHashMap<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//
//        realmResults = myRealm.where(TrackRealm.class).equalTo("album_name", data).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.favorite = trackRealm.isFavorite();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            queueItems.put(trackRealm.getAlbum(), model);
//
//        }
//
//        myRealm.close();
//        return queueItems;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getGenres() {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//        realmResults = myRealm.where(TrackRealm.class).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//            model = queueItemLinkedHashMap.get(trackRealm.getGenre_name());
//            if (model != null) {
//                model.songs++;
//                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
//                queueItemLinkedHashMap.put(trackRealm.getGenre_name(), model);
//            } else {
//                model = new QueueItem();
//                model.id = 0;
//                model.album = trackRealm.getAlbum();
//                model.title = trackRealm.getGenre_name();
//                model.date = trackRealm.getDate();
//                model.songs = 1;
//                model.duration = trackRealm.getDuration();
//                model.dateModified = String.valueOf(trackRealm.getDateModified());
//                model.data = trackRealm.getData();
//                model.noCover = trackRealm.isNoCover();
//                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//
//                queueItemLinkedHashMap.put(trackRealm.getGenre_name(), model);
//
//            }
//        }
//
//
//        myRealm.close();
//        return queueItemLinkedHashMap;
//    }
//
//    public static ArrayList<QueueItem> getTracksForGenre(String data) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        ArrayList<QueueItem> queueItems = new ArrayList<>();
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//        if (data.equals("Unknown Genre")) {
//            realmResults = myRealm.where(TrackRealm.class).beginGroup().equalTo("genre_name", data).or().equalTo("genre_name", "").endGroup().findAll();
//        } else {
//            realmResults = myRealm.where(TrackRealm.class).equalTo("genre_name", data).findAll();
//        }
//
//        for (TrackRealm trackRealm : realmResults) {
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.favorite = trackRealm.isFavorite();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            queueItems.add(model);
//        }
//
//        myRealm.close();
//        return queueItems;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getFolders() {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults;
//
//        realmResults = myRealm.where(TrackRealm.class).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//
//            QueueItem model;
//            model = queueItemLinkedHashMap.get(trackRealm.getFolder_path());
//            if (model != null) {
//                model.songs++;
//                queueItemLinkedHashMap.put(trackRealm.getFolder_path(), model);
//            } else {
//                model = new QueueItem();
//                model.id = 0;
////				model.album = trackRealm.getAlbum();
//                model.title = trackRealm.getFolder_name();
//                model.date = trackRealm.getDate();
//                model.data = trackRealm.getFolder_path();
//                model.songs = 1;
//                model.storage = trackRealm.isStorage();
//                model.folder = true;
//                queueItemLinkedHashMap.put(trackRealm.getFolder_path(), model);
//
//            }
//        }
//
//        myRealm.close();
//        return queueItemLinkedHashMap;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getFavorites(int storageFilter) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults;
//
//        switch (storageFilter) {
//            case 0:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).findAll();
//                break;
//            case 1:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).equalTo("storage", false).findAll();
//                break;
//            case 2:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).equalTo("storage", true).findAll();
//                break;
//            default:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).findAll();
//                break;
//        }
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            if (prefHideUnknown) {
//                if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist") || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            queueItemLinkedHashMap.put(trackRealm.getData(), model);
//        }
//
//        myRealm.close();
//        return queueItemLinkedHashMap;
//    }
//
//
//    public static ArrayList<String> getFavoritesList() {
//
//        ArrayList<String> arrayList = new ArrayList<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> realmResults;
//
//        realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).findAll();
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            arrayList.add(trackRealm.getData());
//        }
//
//        myRealm.close();
//        return arrayList;
//    }
//
//    public static Boolean addFavorite(String data) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//            trackRealm.setFavorite(true);
//            myRealm.insertOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//            return true;
//        }
//
//        myRealm.close();
//        return false;
//    }
//
//    public static Boolean removeFavorite(String data) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//            trackRealm.setFavorite(false);
//            myRealm.insertOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//
//            myRealm.close();
//
//            return true;
//        }
//
//        myRealm.close();
//        return false;
//    }
//
//
//    public static Boolean toggleFavorite(QueueItem queueItem) {
//
//        boolean ret = false;
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//            if (trackRealm.isFavorite()) {
//                trackRealm.setFavorite(false);
//                ret = false;
//            } else {
//                trackRealm.setFavorite(true);
//                ret = true;
//            }
//            myRealm.insertOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//        }
//
//        myRealm.close();
//        return ret;
//    }
//
//
//    public static Boolean removeAllFavorites() {
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults = myRealm.where(TrackRealm.class).equalTo("favorite", true).findAll();
//
//        if (!myRealm.isInTransaction()) {
//            myRealm.beginTransaction();
//        }
//
//        for (TrackRealm trackRealm : realmResults) {
//
//            trackRealm.setFavorite(false);
//        }
//        myRealm.commitTransaction();
//        myRealm.close();
//        return false;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getMostPlayed(int storageFilter, int size) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults;
//
//        switch (storageFilter) {
//            case 0:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("songs", 0).findAll();
//
//                break;
//            case 1:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("songs", 0).equalTo("storage", false).findAll();
//                break;
//            case 2:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("songs", 0).equalTo("storage", true).findAll();
//                break;
//            default:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("songs", 0).findAll();
//                break;
//        }
//
//        realmResults = realmResults.sort("lastPlayed", Sort.DESCENDING);
//        realmResults = realmResults.sort("songs", Sort.DESCENDING);
//
//        for (int i = 0; i < Math.min(realmResults.size(), size); i++) {
//
//            if (prefHideUnknown) {
//                if (realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist") || realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//            }
//
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(realmResults.get(i).getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (realmResults.get(i).isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = realmResults.get(i).getData();
//            model.id = realmResults.get(i).getId();
//            model.song = realmResults.get(i).getSong();
//            model.album = realmResults.get(i).getAlbum();
//            model.artist = realmResults.get(i).getArtist();
//            model.name = realmResults.get(i).getName();
//            model.title = realmResults.get(i).getTitle();
//            model.artist_name = realmResults.get(i).getArtist_name();
//            model.album_name = realmResults.get(i).getAlbum_name();
//            model.genre_name = realmResults.get(i).getGenre_name();
//            model.hash = realmResults.get(i).getHash();
//            model.duration = realmResults.get(i).getDuration();
//            model.composer = realmResults.get(i).getComposer();
//            model.date = realmResults.get(i).getDate();
//            model.dateModified = String.valueOf(realmResults.get(i).getDateModified());
//            model.track = realmResults.get(i).getTrack();
//            model.year = realmResults.get(i).getYear();
//            model.storage = realmResults.get(i).isStorage();
//            model.url = realmResults.get(i).getUrl();
//            model.songs = realmResults.get(i).getSongs();
//            model.noCover = realmResults.get(i).isNoCover();
//            model.coverUpdated = String.valueOf(realmResults.get(i).getCoverUpdated());
//            model.lastPlayed = realmResults.get(i).getLastPlayed();
//            model.rating = realmResults.get(i).getRating();
//            model.lyrics = realmResults.get(i).getLyrics();
//            model.lRC = realmResults.get(i).islRC();
//
//            queueItemLinkedHashMap.put(realmResults.get(i).getData(), model);
//        }
//
//        myRealm.close();
//        return queueItemLinkedHashMap;
//    }
//
//    public static Boolean increasePlayedCount(QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//            trackRealm.setSongs(trackRealm.getSongs() + 1);
//            trackRealm.setLastPlayed(String.valueOf(System.currentTimeMillis() / 1000));
//            myRealm.insertOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//        }
//
//        myRealm.close();
//        return true;
//    }
//
//    public static Boolean resetMostPlayedCount(QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//            trackRealm.setSongs(0);
//            trackRealm.setLastPlayed(String.valueOf(System.currentTimeMillis() / 1000));
//            myRealm.insertOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//        }
//
//        myRealm.close();
//        return true;
//    }
//
//    public static boolean resetPlayedCount() {
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).findAll();
//        myRealm.beginTransaction();
//
//        for (TrackRealm trackRealm : trackRealms) {
//
//            trackRealm.setSongs(0);
//
//        }
//        myRealm.commitTransaction();
//        myRealm.close();
//        return true;
//    }
//
//    public static boolean saveLyrics(Lyrics lyrics) {
//        Realm myRealm = Realm.getDefaultInstance();
//
//        RealmResults<TrackRealm> trackRealms = myRealm.where(TrackRealm.class).beginGroup()
//                .equalTo("artist_name", lyrics.getOriginalArtist())
//                .or()
//                .equalTo("artist_name", lyrics.getArtist())
//                .endGroup()
//                .beginGroup()
//                .equalTo("title", lyrics.getOriginalTrack())
//                .or()
//                .equalTo("title", lyrics.getTrack())
//                .endGroup()
//                .findAll();
//
//        myRealm.beginTransaction();
//
//        for (TrackRealm trackRealm : trackRealms) {
//
//            trackRealm.setOriginalArtist(lyrics.getOriginalArtist());
//            trackRealm.setOriginalTitle(lyrics.getOriginalTrack());
//            trackRealm.setLyricsSourceUrl(lyrics.getURL());
//            trackRealm.setLyricsCoverURL(lyrics.getCoverURL());
//            trackRealm.setLyrics(lyrics.getText());
//            trackRealm.setLyricsSource(lyrics.getSource());
//            trackRealm.setlRC(lyrics.isLRC());
//            trackRealm.setLyricsFlag(lyrics.getFlag());
//
//        }
//
//        myRealm.commitTransaction();
//        myRealm.close();
//        return true;
//    }
//
//    public static Lyrics getLyricsforTrack(String data) {
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", data)
//                .findFirst();
//
//        if (trackRealm != null) {
//            if (trackRealm.getLyrics() == null) {
//                myRealm.close();
//                return null;
//            } else {
//                Lyrics lyrics = new Lyrics(trackRealm.getLyricsFlag());
//                lyrics.setTitle(trackRealm.getTitle());
//                lyrics.setArtist(trackRealm.getArtist_name());
//                lyrics.setOriginalArtist(trackRealm.getOriginalArtist());
//                lyrics.setOriginalTitle(trackRealm.getOriginalTitle());
//                lyrics.setURL(trackRealm.getOriginalArtist());
//                lyrics.setCoverURL(trackRealm.getLyricsCoverURL());
//                lyrics.setText(trackRealm.getLyrics());
//                lyrics.setSource(trackRealm.getLyricsSource());
//                lyrics.setLRC(trackRealm.islRC());
//                myRealm.close();
//                return lyrics;
//            }
//        } else {
//            return null;
//        }
//    }
//
//    public static boolean resetRecentlyPlayedCount(QueueItem queueItem) {
//        Realm myRealm = Realm.getDefaultInstance();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//        myRealm.beginTransaction();
//        if (trackRealm != null) {
//
//            trackRealm.setPlay_order(0);
//
//        }
//        myRealm.commitTransaction();
//        myRealm.close();
//        return true;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getRecentlyPlayed(int storageFilter, int size) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults;
//
//        switch (storageFilter) {
//            case 0:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("play_order", 0).findAllSorted("play_order", Sort.DESCENDING);
//
//                break;
//            case 1:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("play_order", 0).equalTo("storage", false).findAllSorted("play_order", Sort.DESCENDING);
//                break;
//            case 2:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("play_order", 0).equalTo("storage", true).findAllSorted("play_order", Sort.DESCENDING);
//                break;
//            default:
//                realmResults = myRealm.where(TrackRealm.class).notEqualTo("play_order", 0).findAllSorted("play_order", Sort.DESCENDING);
//                break;
//        }
//
//        for (int i = 0; i < Math.min(realmResults.size(), size); i++) {
//
//            if (prefHideUnknown) {
//                if (realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist") || realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//            }
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(realmResults.get(i).getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            if (realmResults.get(i).isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = realmResults.get(i).getData();
//            model.id = realmResults.get(i).getId();
//            model.song = realmResults.get(i).getSong();
//            model.album = realmResults.get(i).getAlbum();
//            model.artist = realmResults.get(i).getArtist();
//            model.name = realmResults.get(i).getName();
//            model.title = realmResults.get(i).getTitle();
//            model.artist_name = realmResults.get(i).getArtist_name();
//            model.album_name = realmResults.get(i).getAlbum_name();
//            model.genre_name = realmResults.get(i).getGenre_name();
//            model.hash = realmResults.get(i).getHash();
//            model.duration = realmResults.get(i).getDuration();
//            model.composer = realmResults.get(i).getComposer();
//            model.date = realmResults.get(i).getDate();
//            model.dateModified = String.valueOf(realmResults.get(i).getDateModified());
//            model.track = realmResults.get(i).getTrack();
//            model.year = realmResults.get(i).getYear();
//            model.storage = realmResults.get(i).isStorage();
//            model.url = realmResults.get(i).getUrl();
//            model.songs = realmResults.get(i).getPlay_order();
//            model.noCover = realmResults.get(i).isNoCover();
//            model.coverUpdated = String.valueOf(realmResults.get(i).getCoverUpdated());
//            model.lastPlayed = realmResults.get(i).getLastPlayed();
//            model.rating = realmResults.get(i).getRating();
//            model.lyrics = realmResults.get(i).getLyrics();
//            model.lRC = realmResults.get(i).islRC();
//
//            queueItemLinkedHashMap.put(realmResults.get(i).getData(), model);
//        }
//
//        myRealm.close();
//        return queueItemLinkedHashMap;
//    }
//
//    public static QueueItem getMostRecentlyPlayed() {
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults;
//
//        realmResults = myRealm.where(TrackRealm.class).notEqualTo("play_order", 0).findAllSorted("play_order", Sort.DESCENDING);
//
//        QueueItem model = null;
//
//        if (realmResults.size() > 0) {
//
//            model = new QueueItem();
//            model.data = realmResults.get(0).getData();
//            model.id = realmResults.get(0).getId();
//            model.song = realmResults.get(0).getSong();
//            model.album = realmResults.get(0).getAlbum();
//            model.artist = realmResults.get(0).getArtist();
//            model.name = realmResults.get(0).getName();
//            model.title = realmResults.get(0).getTitle();
//            model.artist_name = realmResults.get(0).getArtist_name();
//            model.album_name = realmResults.get(0).getAlbum_name();
//            model.genre_name = realmResults.get(0).getGenre_name();
//            model.hash = realmResults.get(0).getHash();
//            model.duration = realmResults.get(0).getDuration();
//            model.composer = realmResults.get(0).getComposer();
//            model.date = realmResults.get(0).getDate();
//            model.dateModified = String.valueOf(realmResults.get(0).getDateModified());
//            model.track = realmResults.get(0).getTrack();
//            model.year = realmResults.get(0).getYear();
//            model.storage = realmResults.get(0).isStorage();
//            model.url = realmResults.get(0).getUrl();
//            model.songs = realmResults.get(0).getPlay_order();
//            model.noCover = realmResults.get(0).isNoCover();
//            model.coverUpdated = String.valueOf(realmResults.get(0).getCoverUpdated());
//            model.lastPlayed = realmResults.get(0).getLastPlayed();
//            model.rating = realmResults.get(0).getRating();
//            model.lyrics = realmResults.get(0).getLyrics();
//            model.lRC = realmResults.get(0).islRC();
//
//            myRealm.close();
//        }
//        return model;
//    }
//
//    public static QueueItem getSecondMostRecentlyPlayed() {
//
//        Realm myRealm = Realm.getDefaultInstance();
//        int currentid = myRealm.where(TrackRealm.class).max("play_order").intValue();
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class).equalTo("play_order", currentid - 1).findFirst();
//
//        QueueItem model = null;
//
//        if (trackRealm != null) {
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.songs = trackRealm.getPlay_order();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            myRealm.close();
//        }
//        return model;
//    }
//
//    public static Boolean updateRecentPlayed(QueueItem queueItem) {
//
//        Realm myRealm = Realm.getDefaultInstance();
//
//        int currentid = myRealm.where(TrackRealm.class).max("play_order").intValue();
//        int nextid;
//
//        if (currentid == 0) {
//            nextid = 1;
//        } else
//
//        {
//            nextid = (currentid + 1);
//        }
//
//
//        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
//                .equalTo("data", queueItem.data)
//                .findFirst();
//
//        if (trackRealm != null) {
//
//            if (!myRealm.isInTransaction()) {
//                myRealm.beginTransaction();
//            }
//            trackRealm.setPlay_order(nextid);
//            myRealm.insertOrUpdate(trackRealm);
//            myRealm.commitTransaction();
//        }
//
//        myRealm.close();
//        return true;
//    }
//
//    public static LinkedHashMap<String, QueueItem> getNegative(int storageFilter) {
//
//        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MuzikoWearApp.getInstance().getApplicationContext());
//        int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
//        if (prefHideSongDuration != 0) {
//            prefHideSongDuration = prefHideSongDuration * 1000;
//        }
//        boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        Realm myRealm = Realm.getDefaultInstance();
//        RealmResults<TrackRealm> realmResults;
//
//        switch (storageFilter) {
//            case 0:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("removed", true).findAll();
//
//                break;
//            case 1:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("removed", true).equalTo("storage", false).findAll();
//                break;
//            case 2:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("removed", true).equalTo("storage", true).findAll();
//                break;
//            default:
//                realmResults = myRealm.where(TrackRealm.class).equalTo("removed", true).findAll();
//                break;
//        }
//
//        for (int i = 0; i < realmResults.size(); i++) {
//
//            if (prefHideUnknown) {
//                if (realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist") || realmResults.get(i).getArtist_name().isEmpty() || realmResults.get(i).getArtist_name().equals("Unknown Artist")) {
//                    continue;
//                }
//            }
//
//            if (prefHideSongDuration > 0) {
//                if (Integer.parseInt(realmResults.get(i).getDuration()) < prefHideSongDuration) {
//                    continue;
//                }
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = realmResults.get(i).getData();
//            model.id = realmResults.get(i).getId();
//            model.song = realmResults.get(i).getSong();
//            model.album = realmResults.get(i).getAlbum();
//            model.artist = realmResults.get(i).getArtist();
//            model.name = realmResults.get(i).getName();
//            model.title = realmResults.get(i).getTitle();
//            model.artist_name = realmResults.get(i).getArtist_name();
//            model.album_name = realmResults.get(i).getAlbum_name();
//            model.genre_name = realmResults.get(i).getGenre_name();
//            model.hash = realmResults.get(i).getHash();
//            model.duration = realmResults.get(i).getDuration();
//            model.composer = realmResults.get(i).getComposer();
//            model.date = realmResults.get(i).getDate();
//            model.dateModified = String.valueOf(realmResults.get(i).getDateModified());
//            model.track = realmResults.get(i).getTrack();
//            model.year = realmResults.get(i).getYear();
//            model.storage = realmResults.get(i).isStorage();
//            model.url = realmResults.get(i).getUrl();
//            model.songs = realmResults.get(i).getSongs();
//            model.noCover = realmResults.get(i).isNoCover();
//            model.coverUpdated = String.valueOf(realmResults.get(i).getCoverUpdated());
//            model.lastPlayed = realmResults.get(i).getLastPlayed();
//            model.rating = realmResults.get(i).getRating();
//            model.lyrics = realmResults.get(i).getLyrics();
//            model.lRC = realmResults.get(i).islRC();
//
//            queueItemLinkedHashMap.put(realmResults.get(i).getData(), model);
//        }
//
//        myRealm.close();
//        return queueItemLinkedHashMap;
//    }
//
//    public static LinkedHashMap<String, QueueItem> searchTracks(RealmResults<TrackRealm> realmResults) {
//
////		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
////		int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
////		if (prefHideSongDuration != 0) {
////			prefHideSongDuration = prefHideSongDuration * 1000;
////		}
////		boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        for (TrackRealm trackRealm : realmResults) {
//
////			if (prefHideUnknown) {
////				if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist" || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist") {
////					continue;
////				}
////
////			}
////			if (prefHideSongDuration > 0) {
////				if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
////					continue;
////				}
////			}
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//
//            model = new QueueItem();
//            model.data = trackRealm.getData();
//            model.id = trackRealm.getId();
//            model.song = trackRealm.getSong();
//            model.album = trackRealm.getAlbum();
//            model.artist = trackRealm.getArtist();
//            model.name = trackRealm.getName();
//            model.title = trackRealm.getTitle();
//            model.artist_name = trackRealm.getArtist_name();
//            model.album_name = trackRealm.getAlbum_name();
//            model.genre_name = trackRealm.getGenre_name();
//            model.hash = trackRealm.getHash();
//            model.duration = trackRealm.getDuration();
//            model.composer = trackRealm.getComposer();
//            model.date = trackRealm.getDate();
//            model.dateModified = String.valueOf(trackRealm.getDateModified());
//            model.track = trackRealm.getTrack();
//            model.year = trackRealm.getYear();
//            model.storage = trackRealm.isStorage();
//            model.url = trackRealm.getUrl();
//            model.favorite = trackRealm.isFavorite();
//            model.noCover = trackRealm.isNoCover();
//            model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//            model.lastPlayed = trackRealm.getLastPlayed();
//            model.rating = trackRealm.getRating();
//            model.lyrics = trackRealm.getLyrics();
//            model.lRC = trackRealm.islRC();
//
//            queueItemLinkedHashMap.put(trackRealm.getData(), model);
//        }
//
//        return queueItemLinkedHashMap;
//    }
//
//    public static LinkedHashMap<String, QueueItem> searchAlbums(RealmResults<TrackRealm> realmResults) {
//
////		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
////		int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
////		if (prefHideSongDuration != 0) {
////			prefHideSongDuration = prefHideSongDuration * 1000;
////		}
////		boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        for (TrackRealm trackRealm : realmResults) {
////
////			if (prefHideUnknown) {
////				if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist" || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist") {
////					continue;
////				}
////
////			}
////			if (prefHideSongDuration > 0) {
////				if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
////					continue;
////				}
////			}
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//            model = queueItemLinkedHashMap.get(trackRealm.getAlbum_name());
//            if (model != null) {
//                model.songs++;
//                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
//                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);
//            } else {
//                model = new QueueItem();
//                model.id = 0;
//                model.album = trackRealm.getAlbum();
//                model.title = trackRealm.getAlbum_name();
//                model.artist_name = trackRealm.getArtist_name();
//                model.genre_name = trackRealm.getGenre_name();
//                model.date = trackRealm.getDate();
//                model.songs = 1;
//                model.duration = trackRealm.getDuration();
//                model.dateModified = String.valueOf(trackRealm.getDateModified());
//                model.data = trackRealm.getData();
//                model.noCover = trackRealm.isNoCover();
//                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//
//                queueItemLinkedHashMap.put(trackRealm.getAlbum_name(), model);
//
//            }
//        }
//
//        return queueItemLinkedHashMap;
//    }
//
//    public static LinkedHashMap<String, QueueItem> searchArtists(RealmResults<TrackRealm> realmResults) {
//
////		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
////		int prefHideSongDuration = prefs.getInt("prefHideSongDuration", 0);
////		if (prefHideSongDuration != 0) {
////			prefHideSongDuration = prefHideSongDuration * 1000;
////		}
////		boolean prefHideUnknown = prefs.getBoolean("prefHideUnknown", false);
//
//        LinkedHashMap<String, QueueItem> queueItemLinkedHashMap = new LinkedHashMap<>();
//
//        for (TrackRealm trackRealm : realmResults) {
////			if (prefHideUnknown) {
////				if (trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist" || trackRealm.getArtist_name().isEmpty() || trackRealm.getArtist_name() == "Unknown Artist") {
////					continue;
////				}
////
////			}
////			if (prefHideSongDuration > 0) {
////				if (Integer.parseInt(trackRealm.getDuration()) < prefHideSongDuration) {
////					continue;
////				}
////			}
//
//            if (trackRealm.isRemoved()) {
//                continue;
//            }
//
//            QueueItem model;
//            model = queueItemLinkedHashMap.get(trackRealm.getArtist_name());
//            if (model != null) {
//                model.songs++;
//                model.duration = String.valueOf(Utils.getInt(model.duration, 0) + Utils.getInt(trackRealm.getDuration(), 0));
//                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);
//            } else {
//                model = new QueueItem();
//                model.id = 0;
//                model.album = trackRealm.getAlbum();
//                model.title = trackRealm.getArtist_name();
//                model.date = trackRealm.getDate();
//                model.songs = 1;
//                model.duration = trackRealm.getDuration();
//                model.dateModified = String.valueOf(trackRealm.getDateModified());
//                model.data = trackRealm.getData();
//                model.noCover = trackRealm.isNoCover();
//                model.coverUpdated = String.valueOf(trackRealm.getCoverUpdated());
//
//                queueItemLinkedHashMap.put(trackRealm.getArtist_name(), model);
//
//            }
//        }
//
//        return queueItemLinkedHashMap;
//    }

}