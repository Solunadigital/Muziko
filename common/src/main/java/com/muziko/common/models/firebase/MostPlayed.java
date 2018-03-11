package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

/**
 * Created by Bradley on 16/03/2017.
 */
@Keep
public class MostPlayed {

    private String acrid;
    private String title;
    private String artist_name;
    private String album_name;
    private int duration;
    private String date;
    private String videoId;
    private String coverUrl;
    private int played;

    public MostPlayed() {

    }

    public MostPlayed(String acrid, String title, String artist_name, String album_name, int duration, String date, String videoId, String coverUrl, int played) {
        this.acrid = acrid;
        this.title = title;
        this.artist_name = artist_name;
        this.album_name = album_name;
        this.duration = duration;
        this.date = date;
        this.videoId = videoId;
        this.coverUrl = coverUrl;
        this.played = played;
    }

    public String getAcrid() {
        return acrid;
    }

    public void setAcrid(String acrid) {
        this.acrid = acrid;
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

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    @Override
    public int hashCode() {
        int result = acrid.hashCode();
        result = 31 * result + title.hashCode();
        result = 31 * result + (artist_name != null ? artist_name.hashCode() : 0);
        result = 31 * result + (album_name != null ? album_name.hashCode() : 0);
        result = 31 * result + duration;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (videoId != null ? videoId.hashCode() : 0);
        result = 31 * result + (coverUrl != null ? coverUrl.hashCode() : 0);
        result = 31 * result + played;
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MostPlayed that = (MostPlayed) o;

        if (duration != that.duration) return false;
        if (played != that.played) return false;
        if (!acrid.equals(that.acrid)) return false;
        if (!title.equals(that.title)) return false;
        if (artist_name != null ? !artist_name.equals(that.artist_name) : that.artist_name != null)
            return false;
        if (album_name != null ? !album_name.equals(that.album_name) : that.album_name != null)
            return false;
        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (videoId != null ? !videoId.equals(that.videoId) : that.videoId != null) return false;
        return coverUrl != null ? coverUrl.equals(that.coverUrl) : that.coverUrl == null;

    }

    public int getPlayed() {

        return played;
    }

    public void setPlayed(int played) {
        this.played = played;
    }
}

