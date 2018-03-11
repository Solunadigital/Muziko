
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Itunes {

    @SerializedName("album")
    @Expose
    private AcrAlbum acrAlbum;

    @SerializedName("artists")
    @Expose
    private List<AcrArtist> acrArtists = new ArrayList<AcrArtist>();

    @SerializedName("track")
    @Expose
    private AcrTrack acrTrack;

    /** @return The album */
    public AcrAlbum getAcrAlbum() {
        return acrAlbum;
    }

    /** @param acrAlbum The album */
    public void setAcrAlbum(AcrAlbum acrAlbum) {
        this.acrAlbum = acrAlbum;
    }

    /** @return The artists */
    public List<AcrArtist> getAcrArtists() {
        return acrArtists;
    }

    /** @param acrArtists The artists */
    public void setAcrArtists(List<AcrArtist> acrArtists) {
        this.acrArtists = acrArtists;
    }

    /** @return The track */
    public AcrTrack getAcrTrack() {
        return acrTrack;
    }

    /** @param acrTrack The track */
    public void setAcrTrack(AcrTrack acrTrack) {
        this.acrTrack = acrTrack;
    }

}
