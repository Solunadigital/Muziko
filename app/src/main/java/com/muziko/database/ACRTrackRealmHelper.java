package com.muziko.database;


import com.muziko.api.AcrCloud.TrackModel;
import com.muziko.common.models.Lyrics;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.MostPlayed;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by dev on 17/07/2016.
 */
public class ACRTrackRealmHelper {

    public static ArrayList<TrackModel> getACRTracks() {


        ArrayList<TrackModel> trackModels = new ArrayList<>();
        Realm myRealm = Realm.getDefaultInstance();

        RealmResults<ACRTrackRealm> realmResults;

        realmResults = myRealm.where(ACRTrackRealm.class).findAllSorted("played", Sort.DESCENDING);


        for (ACRTrackRealm acrTrackRealm : realmResults) {

            TrackModel trackModel;

            trackModel = new TrackModel();
            trackModel.acrid = acrTrackRealm.getAcrid();
            trackModel.title = acrTrackRealm.getTitle();
            trackModel.artist_name = acrTrackRealm.getArtist_name();
            trackModel.album_name = acrTrackRealm.getAlbum_name();
            trackModel.duration = acrTrackRealm.getDuration();
            trackModel.played = acrTrackRealm.getPlayed();
            trackModel.date = acrTrackRealm.getDate();
            trackModel.coverUrl = acrTrackRealm.getCoverUrl();
            trackModel.videoId = acrTrackRealm.getVideoId();

            QueueItem queueItem = TrackRealmHelper.getTrackByACRid(acrTrackRealm.getAcrid());
            if (queueItem != null) {
                trackModel.setData(queueItem.data);
            }

            trackModels.add(trackModel);
        }

        myRealm.close();

        return trackModels;
    }

    public static void insertTrack(TrackModel trackModel) {

        Realm myRealm = Realm.getDefaultInstance();


        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }


        ACRTrackRealm acrTrackRealm;

        // run already
        acrTrackRealm = myRealm.where(ACRTrackRealm.class).equalTo("acrid", trackModel.acrid).findFirst();
        // exists
        if (acrTrackRealm != null) {
            acrTrackRealm.setTitle(trackModel.title);
            acrTrackRealm.setArtist_name(trackModel.artist_name);
            acrTrackRealm.setAlbum_name(trackModel.album_name);
            acrTrackRealm.setDuration(trackModel.duration);
            acrTrackRealm.setDate(trackModel.date);
            acrTrackRealm.setPlayed(trackModel.position);
            acrTrackRealm.setCoverUrl(trackModel.getCoverUrl());
            acrTrackRealm.setVideoId(trackModel.getVideoId());
            myRealm.copyToRealmOrUpdate(acrTrackRealm);
        } else {

            // new
            acrTrackRealm = new ACRTrackRealm();
            acrTrackRealm.setAcrid(trackModel.acrid);
            acrTrackRealm.setTitle(trackModel.title);
            acrTrackRealm.setArtist_name(trackModel.artist_name);
            acrTrackRealm.setAlbum_name(trackModel.album_name);
            acrTrackRealm.setDuration(trackModel.duration);
            acrTrackRealm.setDate(trackModel.date);
            acrTrackRealm.setPlayed(trackModel.played);
            acrTrackRealm.setCoverUrl(trackModel.getCoverUrl());
            acrTrackRealm.setVideoId(trackModel.getVideoId());
            myRealm.insertOrUpdate(acrTrackRealm);
        }

        myRealm.commitTransaction();
        myRealm.close();

    }

    public static void insertTrack(MostPlayed trackModel) {

        Realm myRealm = Realm.getDefaultInstance();


        if (!myRealm.isInTransaction()) {
            myRealm.beginTransaction();
        }


        ACRTrackRealm acrTrackRealm;

        // run already
        acrTrackRealm = myRealm.where(ACRTrackRealm.class).equalTo("acrid", trackModel.getAcrid()).findFirst();
        // exists
        if (acrTrackRealm != null) {
            acrTrackRealm.setTitle(trackModel.getTitle());
            acrTrackRealm.setArtist_name(trackModel.getArtist_name());
            acrTrackRealm.setAlbum_name(trackModel.getAlbum_name());
            acrTrackRealm.setDuration(trackModel.getDuration());
            acrTrackRealm.setDate(trackModel.getDate());
            acrTrackRealm.setPlayed(trackModel.getPlayed());
            acrTrackRealm.setCoverUrl(trackModel.getCoverUrl());
            acrTrackRealm.setVideoId(trackModel.getVideoId());
            myRealm.copyToRealmOrUpdate(acrTrackRealm);
        } else {

            // new
            acrTrackRealm = new ACRTrackRealm();
            acrTrackRealm.setAcrid(trackModel.getAcrid());
            acrTrackRealm.setTitle(trackModel.getTitle());
            acrTrackRealm.setArtist_name(trackModel.getArtist_name());
            acrTrackRealm.setAlbum_name(trackModel.getAlbum_name());
            acrTrackRealm.setDuration(trackModel.getDuration());
            acrTrackRealm.setDate(trackModel.getDate());
            acrTrackRealm.setPlayed(trackModel.getPlayed());
            acrTrackRealm.setCoverUrl(trackModel.getCoverUrl());
            acrTrackRealm.setVideoId(trackModel.getVideoId());
            myRealm.insertOrUpdate(acrTrackRealm);
        }

        myRealm.commitTransaction();
        myRealm.close();

    }

    public static TrackModel getTrack(String acrid) {

        Realm myRealm = Realm.getDefaultInstance();
        ACRTrackRealm acrTrackRealm;

        acrTrackRealm = myRealm.where(ACRTrackRealm.class).equalTo("acrid", acrid).findFirst();

        TrackModel model = null;

        if (acrTrackRealm != null) {

            model = new TrackModel();
            model.acrid = acrTrackRealm.getAcrid();
            model.title = acrTrackRealm.getTitle();
            model.artist_name = acrTrackRealm.getArtist_name();
            model.album_name = acrTrackRealm.getAlbum_name();
            model.duration = acrTrackRealm.getDuration();
            model.played = acrTrackRealm.getPlayed();
            model.date = acrTrackRealm.getDate();
            model.coverUrl = acrTrackRealm.getCoverUrl();
            model.videoId = acrTrackRealm.getVideoId();
        }
        myRealm.close();
        return model;
    }


    public static void saveCoverUrl(TrackModel trackModel) {
        Realm myRealm = Realm.getDefaultInstance();

        ACRTrackRealm acrTrackRealm;

        acrTrackRealm = myRealm.where(ACRTrackRealm.class).equalTo("acrid", trackModel.acrid).findFirst();


        if (acrTrackRealm != null) {
            myRealm.beginTransaction();
            acrTrackRealm.setCoverUrl(trackModel.getCoverUrl());
            myRealm.commitTransaction();
        }

        myRealm.close();
    }

    public static void saveLyrics(TrackModel trackModel, Lyrics lyrics) {
        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm;
        trackRealm = myRealm.where(TrackRealm.class).equalTo("acrid", trackModel.acrid).findFirst();


        if (trackRealm != null) {
            myRealm.beginTransaction();
            trackRealm.setOriginalArtist(lyrics.getOriginalArtist());
            trackRealm.setOriginalTitle(lyrics.getOriginalTrack());
            trackRealm.setLyricsSourceUrl(lyrics.getURL());
            trackRealm.setLyricsCoverURL(lyrics.getCoverURL());
            trackRealm.setLyrics(lyrics.getText());
            trackRealm.setLyricsSource(lyrics.getSource());
            trackRealm.setlRC(lyrics.isLRC());
            trackRealm.setLyricsFlag(lyrics.getFlag());
            myRealm.commitTransaction();
        }

        myRealm.close();
    }

    public static Lyrics getLyricsforTrack(String acrid) {
        Realm myRealm = Realm.getDefaultInstance();

        TrackRealm trackRealm = myRealm.where(TrackRealm.class)
                .equalTo("acrid", acrid)
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

}