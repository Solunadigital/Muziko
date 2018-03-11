
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Deezer {

    @SerializedName("album")
    @Expose
    private AcrAlbumTwo album;

    @SerializedName("artists")
    @Expose
    private List<AcrArtistTwo> artists = new ArrayList<AcrArtistTwo>();

    @SerializedName("track")
    @Expose
    private AcrTrackTwo track;

    /** @return The album */
    public AcrAlbumTwo getAlbum() {
        return album;
    }

    /** @param album The album */
    public void setAlbum(AcrAlbumTwo album) {
        this.album = album;
    }

    /** @return The artists */
    public List<AcrArtistTwo> getArtists() {
        return artists;
    }

    /** @param artists The artists */
    public void setArtists(List<AcrArtistTwo> artists) {
        this.artists = artists;
    }

    /** @return The track */
    public AcrTrackTwo getTrack() {
        return track;
    }

    /** @param track The track */
    public void setTrack(AcrTrackTwo track) {
        this.track = track;
    }

}
