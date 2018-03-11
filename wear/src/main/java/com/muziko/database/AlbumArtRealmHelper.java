package com.muziko.database;

import io.realm.Realm;

/**
 * Created by dev on 31/08/2016.
 */
public class AlbumArtRealmHelper {

    public static void updateStatus(String artist_name, String album_name) {

        Realm myRealm = Realm.getDefaultInstance();
        AlbumArtRealm albumArtRealm;

        String key = artist_name + album_name;
        albumArtRealm = myRealm.where(AlbumArtRealm.class)
                .equalTo("key", key)
                .findFirst();

        if (albumArtRealm != null) {

            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            if (albumArtRealm.isFailed()) {
                albumArtRealm.setFailed(false);
            } else {
                albumArtRealm.setFailed(true);
            }
            myRealm.copyToRealmOrUpdate(albumArtRealm);
            myRealm.commitTransaction();

            myRealm.close();
        } else {
            if (!myRealm.isInTransaction()) {
                myRealm.beginTransaction();
            }

            albumArtRealm = new AlbumArtRealm();
            albumArtRealm.setArtist_name(artist_name);
            albumArtRealm.setAlbum_name(album_name);
            albumArtRealm.setFailed(true);
            myRealm.copyToRealmOrUpdate(albumArtRealm);
            myRealm.commitTransaction();

            myRealm.close();
        }
    }

    public static boolean getStatus(String artist_name, String album_name) {

        boolean ret = false;
        Realm myRealm = Realm.getDefaultInstance();
        AlbumArtRealm albumArtRealm;

        String key = artist_name + album_name;
        albumArtRealm = myRealm.where(AlbumArtRealm.class)
                .equalTo("key", key)
                .findFirst();

        if (albumArtRealm != null) {

            ret = albumArtRealm.isFailed();

            myRealm.close();
        }

        return ret;
    }
}
