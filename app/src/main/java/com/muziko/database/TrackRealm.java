package com.muziko.database;

import com.muziko.MyApplication;
import com.muziko.R;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 17/07/2016.
 */
public class TrackRealm extends RealmObject {

    public long size;
    private String composer;
    private int track;
    @PrimaryKey
    private String data;
    private String cloudId;
    @Index
    private long id;
    private long song;
    @Index
    private long album;
    @Index
    private long artist;
    private String name;
    @Index
    private String title;
    @Index
    private String artist_name;
    @Index
    private String album_name;
    @Index
    private String genre_name;
    private String hash;
    private String duration;
    @Index
    private long date;
    @Index

    private long dateModified;
    @Index
    private long dateAdded;
    private int year;
    @Index
    private int storage;
    private String url;
    private String folder_name;
    @Index
    private String folder_path;
    @Index
    private boolean favorite;
    @Index
    private int songs;
    @Index
    private int playOrder;
    private boolean noCover;
    private long coverUpdated;
    private String lastPlayed;
    private int rating;
    private boolean removed;
    private String originalTitle;
    private String originalArtist;
    private String lyricsSourceUrl;
    private String lyricsCoverURL;
    private String lyrics;
    private String lyricsSource;
    private boolean lRC = false;
    private int lyricsFlag;
    @Index
    private String acrid;
    private int startFrom;
    @Index
    private boolean library;
    private boolean favsync;
    @Index
    private String md5;

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isFavsync() {
        return favsync;
    }

    public void setFavsync(boolean favsync) {
        this.favsync = favsync;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public boolean isLibrary() {
        return library;
    }

    public void setLibrary(boolean library) {
        this.library = library;
    }

    public int getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(int startFrom) {
        this.startFrom = startFrom;
    }

    public String getAcrid() {
        return acrid;
    }

    public void setAcrid(String acrid) {
        this.acrid = acrid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getSong() {
        return song;
    }

    public void setSong(long song) {
        this.song = song;
    }

    public long getAlbum() {
        return album;
    }

    public void setAlbum(long album) {
        this.album = album;
    }

    public long getArtist() {
        return artist;
    }

    public void setArtist(long artist) {
        this.artist = artist;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist_name() {

        if (artist_name == null || artist_name.isEmpty()) {
            return MyApplication.getInstance().getApplicationContext().getString(R.string.unknown_artist);
        } else {
            return artist_name;
        }
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getAlbum_name() {
        if (album_name == null || album_name.isEmpty()) {
            return MyApplication.getInstance().getApplicationContext().getString(R.string.unknown_album);
        } else {
            return album_name;
        }
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getGenre_name() {
        if (genre_name == null || genre_name.isEmpty()) {
            return MyApplication.getInstance().getApplicationContext().getString(R.string.unknown_genre);
        } else {
            return genre_name;
        }
    }

    public void setGenre_name(String genre_name) {
        this.genre_name = genre_name;
    }

    public String getComposer() {
        return composer;
    }

    public void setComposer(String composer) {
        this.composer = composer;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public Long getDate() {
        return date;
    }

    public void setDate(Long date) {
        this.date = date;
    }

    public long getDateModified() {
        return dateModified;
    }

    public void setDateModified(long dateModified) {
        this.dateModified = dateModified;
    }

    public int getTrack() {
        return track;
    }

    public void setTrack(int track) {
        this.track = track;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getStorage() {
        return storage;
    }

    public void setStorage(int storage) {
        this.storage = storage;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getFolder_name() {
        return folder_name;
    }

    public void setFolder_name(String folder_name) {
        this.folder_name = folder_name;
    }

    public String getFolder_path() {
        return folder_path;
    }

    public void setFolder_path(String folder_path) {
        this.folder_path = folder_path;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;

    }

    public int getSongs() {
        return songs;
    }

    public void setSongs(int songs) {
        this.songs = songs;
    }

    public int getPlayOrder() {
        return playOrder;
    }

    public void setPlayOrder(int playOrder) {
        this.playOrder = playOrder;
    }

    public boolean isNoCover() {
        return noCover;
    }

    public void setNoCover(boolean noCover) {
        this.noCover = noCover;
    }

    public long getCoverUpdated() {
        return coverUpdated;
    }

    public void setCoverUpdated(long coverUpdated) {
        this.coverUpdated = coverUpdated;
    }

    public String getLastPlayed() {
        return lastPlayed;
    }

    public void setLastPlayed(String lastPlayed) {
        this.lastPlayed = lastPlayed;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public String getOriginalArtist() {
        return originalArtist;
    }

    public void setOriginalArtist(String originalArtist) {
        this.originalArtist = originalArtist;
    }

    public String getLyricsSourceUrl() {
        return lyricsSourceUrl;
    }

    public void setLyricsSourceUrl(String lyricsSourceUrl) {
        this.lyricsSourceUrl = lyricsSourceUrl;
    }

    public String getLyricsCoverURL() {
        return lyricsCoverURL;
    }

    public void setLyricsCoverURL(String lyricsCoverURL) {
        this.lyricsCoverURL = lyricsCoverURL;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public String getLyricsSource() {
        return lyricsSource;
    }

    public void setLyricsSource(String lyricsSource) {
        this.lyricsSource = lyricsSource;
    }

    public boolean islRC() {
        return lRC;
    }

    public void setlRC(boolean lRC) {
        this.lRC = lRC;
    }

    public int getLyricsFlag() {
        return lyricsFlag;
    }

    public void setLyricsFlag(int lyricsFlag) {
        this.lyricsFlag = lyricsFlag;
    }
}
