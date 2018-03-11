
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Spotify {

    @SerializedName("album")
    @Expose
    private AcrAlbumThree album;

    @SerializedName("artists")
    @Expose
    private List<AcrArtistThree> artists = new ArrayList<AcrArtistThree>();

    @SerializedName("track")
    @Expose
    private AcrTrackThree track;

    /** @return The album */
    public AcrAlbumThree getAlbum() {
        return album;
    }

    /** @param album The album */
    public void setAlbum(AcrAlbumThree album) {
        this.album = album;
    }

    /** @return The artists */
    public List<AcrArtistThree> getArtists() {
        return artists;
    }

    /** @param artists The artists */
    public void setArtists(List<AcrArtistThree> artists) {
        this.artists = artists;
    }

    /** @return The track */
    public AcrTrackThree getTrack() {
        return track;
    }

    /** @param track The track */
    public void setTrack(AcrTrackThree track) {
        this.track = track;
    }

}
