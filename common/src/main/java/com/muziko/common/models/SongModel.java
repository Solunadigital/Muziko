package com.muziko.common.models;

import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.io.Serializable;

@JsonObject(fieldDetectionPolicy = JsonObject.FieldDetectionPolicy.NONPRIVATE_FIELDS)
public class SongModel implements Serializable {
    public long id;
    public long song;
    public long album;
    public long artist;
    public long playlist;

    public String name;
    public String title;
    public String artist_name;
    public String album_name;
    public String genre_name;
    public String composer;
    public String cloudId;
    public String data;
    public String hash;
    public String duration;
    public Long date;
    public Long dateModified;
    public long level;
    public int track;
    public int year;
    public int songs;
    public boolean folder;
    public boolean selected;
    public int storage;
    public String url;
    public String folder_name;
    public String folder_path;
    public boolean favorite;
    public boolean noCover;
    public String coverUpdated;
    public String lastPlayed;
    public int rating;
    public long order;
    public int removeafter;
    public int played;
    public String type;
    public String lyrics;
    public boolean lRC;
    public String acrid;
    public String videoId;
    public int position;
    public String coverUrl;
    public int startFrom;
    public boolean library;
    public long size;
    public boolean sync;
    public boolean removed;
    public String md5;

    protected SongModel() {
        id = 0;
        song = 0;
        album = 0;

        title = "";
        artist_name = "";
        album_name = "";
        genre_name = "";

        data = "";
        level = 0;

        duration = "0";
        date = 0L;
        dateModified = 0L;

        selected = false;
        favorite = false;

        removeafter = 0;
        played = 0;

    }

    public boolean isRemoved() {
        return removed;
    }

    public void setRemoved(boolean removed) {
        this.removed = removed;
    }

    public String getCloudId() {
        return cloudId;
    }

    public void setCloudId(String cloudId) {
        this.cloudId = cloudId;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isSync() {
        return sync;
    }

    public void setSync(boolean sync) {
        this.sync = sync;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getStartFrom() {
        return startFrom;
    }

    public void setStartFrom(int startFrom) {
        this.startFrom = startFrom;
    }

    public boolean isLibrary() {
        return library;
    }

    public void setLibrary(boolean library) {
        this.library = library;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public String getAcrid() {
        return acrid;
    }

    public void setAcrid(String acrid) {
        this.acrid = acrid;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
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

    public long getPlaylist() {
        return playlist;
    }

    public void setPlaylist(long playlist) {
        this.playlist = playlist;
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
        return artist_name;
    }

    public void setArtist_name(String artist_name) {
        this.artist_name = artist_name;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public void setAlbum_name(String album_name) {
        this.album_name = album_name;
    }

    public String getGenre_name() {
        return genre_name;
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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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

    public Long getDateModified() {
        return dateModified;
    }

    public void setDateModified(Long dateModified) {
        this.dateModified = dateModified;
    }

    public long getLevel() {
        return level;
    }

    public void setLevel(long level) {
        this.level = level;
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

    public int getSongs() {
        return songs;
    }

    public void setSongs(int songs) {
        this.songs = songs;
    }

    public boolean isFolder() {
        return folder;
    }

    public void setFolder(boolean folder) {
        this.folder = folder;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
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

    public boolean isNoCover() {
        return noCover;
    }

    public void setNoCover(boolean noCover) {
        this.noCover = noCover;
    }

    public String getCoverUpdated() {
        return coverUpdated;
    }

    public void setCoverUpdated(String coverUpdated) {
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

    public long getOrder() {
        return order;
    }

    public void setOrder(long order) {
        this.order = order;
    }

    public int getRemoveafter() {
        return removeafter;
    }

    public void setRemoveafter(int removeafter) {
        this.removeafter = removeafter;
    }

    public int getPlayed() {
        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLyrics() {
        return lyrics;
    }

    public void setLyrics(String lyrics) {
        this.lyrics = lyrics;
    }

    public boolean islRC() {
        return lRC;
    }

    public void setlRC(boolean lRC) {
        this.lRC = lRC;
    }

    @Override
    public int hashCode() {
        int result = (int) (id ^ (id >>> 32));
        result = 31 * result + (int) (song ^ (song >>> 32));
        result = 31 * result + (int) (album ^ (album >>> 32));
        result = 31 * result + (int) (artist ^ (artist >>> 32));
        result = 31 * result + (int) (playlist ^ (playlist >>> 32));
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (artist_name != null ? artist_name.hashCode() : 0);
        result = 31 * result + (album_name != null ? album_name.hashCode() : 0);
        result = 31 * result + (genre_name != null ? genre_name.hashCode() : 0);
        result = 31 * result + (composer != null ? composer.hashCode() : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (hash != null ? hash.hashCode() : 0);
        result = 31 * result + (duration != null ? duration.hashCode() : 0);
        result = 31 * result + (int) (date ^ (date >>> 32));
        result = 31 * result + (int) (dateModified ^ (dateModified >>> 32));
        result = 31 * result + (int) (level ^ (level >>> 32));
        result = 31 * result + track;
        result = 31 * result + year;
        result = 31 * result + songs;
        result = 31 * result + (folder ? 1 : 0);
        result = 31 * result + (selected ? 1 : 0);
        result = 31 * result + storage;
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (folder_name != null ? folder_name.hashCode() : 0);
        result = 31 * result + (folder_path != null ? folder_path.hashCode() : 0);
        result = 31 * result + (favorite ? 1 : 0);
        result = 31 * result + (noCover ? 1 : 0);
        result = 31 * result + (coverUpdated != null ? coverUpdated.hashCode() : 0);
        result = 31 * result + (lastPlayed != null ? lastPlayed.hashCode() : 0);
        result = 31 * result + rating;
        result = 31 * result + (int) (order ^ (order >>> 32));
        result = 31 * result + removeafter;
        result = 31 * result + played;
        result = 31 * result + (type != null ? type.hashCode() : 0);
        result = 31 * result + (lyrics != null ? lyrics.hashCode() : 0);
        result = 31 * result + (lRC ? 1 : 0);
        result = 31 * result + (acrid != null ? acrid.hashCode() : 0);
        result = 31 * result + (videoId != null ? videoId.hashCode() : 0);
        result = 31 * result + position;
        result = 31 * result + (coverUrl != null ? coverUrl.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SongModel songModel = (SongModel) o;

        if (id != songModel.id) return false;
        if (song != songModel.song) return false;
        if (album != songModel.album) return false;
        if (artist != songModel.artist) return false;
        if (playlist != songModel.playlist) return false;
        if (date != songModel.date) return false;
        if (dateModified != songModel.dateModified) return false;
        if (level != songModel.level) return false;
        if (track != songModel.track) return false;
        if (year != songModel.year) return false;
        if (songs != songModel.songs) return false;
        if (folder != songModel.folder) return false;
        if (selected != songModel.selected) return false;
        if (storage != songModel.storage) return false;
        if (favorite != songModel.favorite) return false;
        if (noCover != songModel.noCover) return false;
        if (rating != songModel.rating) return false;
        if (order != songModel.order) return false;
        if (removeafter != songModel.removeafter) return false;
        if (played != songModel.played) return false;
        if (lRC != songModel.lRC) return false;
        if (position != songModel.position) return false;
        if (name != null ? !name.equals(songModel.name) : songModel.name != null) return false;
        if (title != null ? !title.equals(songModel.title) : songModel.title != null) return false;
        if (artist_name != null ? !artist_name.equals(songModel.artist_name) : songModel.artist_name != null)
            return false;
        if (album_name != null ? !album_name.equals(songModel.album_name) : songModel.album_name != null)
            return false;
        if (genre_name != null ? !genre_name.equals(songModel.genre_name) : songModel.genre_name != null)
            return false;
        if (composer != null ? !composer.equals(songModel.composer) : songModel.composer != null)
            return false;
        if (data != null ? !data.equals(songModel.data) : songModel.data != null) return false;
        if (hash != null ? !hash.equals(songModel.hash) : songModel.hash != null) return false;
        if (duration != null ? !duration.equals(songModel.duration) : songModel.duration != null)
            return false;
        if (url != null ? !url.equals(songModel.url) : songModel.url != null) return false;
        if (folder_name != null ? !folder_name.equals(songModel.folder_name) : songModel.folder_name != null)
            return false;
        if (folder_path != null ? !folder_path.equals(songModel.folder_path) : songModel.folder_path != null)
            return false;
        if (coverUpdated != null ? !coverUpdated.equals(songModel.coverUpdated) : songModel.coverUpdated != null)
            return false;
        if (lastPlayed != null ? !lastPlayed.equals(songModel.lastPlayed) : songModel.lastPlayed != null)
            return false;
        if (type != null ? !type.equals(songModel.type) : songModel.type != null) return false;
        if (lyrics != null ? !lyrics.equals(songModel.lyrics) : songModel.lyrics != null)
            return false;
        if (acrid != null ? !acrid.equals(songModel.acrid) : songModel.acrid != null) return false;
        if (videoId != null ? !videoId.equals(songModel.videoId) : songModel.videoId != null)
            return false;
        return coverUrl != null ? coverUrl.equals(songModel.coverUrl) : songModel.coverUrl == null;

    }

    public void setTags(SongModel item) {
        this.title = item.title;
        this.album_name = item.album_name;
        this.artist_name = item.artist_name;
        this.genre_name = item.genre_name;
        this.track = item.track;
        this.year = item.year;
        this.noCover = item.noCover;
        this.coverUpdated = item.coverUpdated;
    }
}
