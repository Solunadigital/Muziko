
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ExternalMetadata {

    @SerializedName("itunes")
    @Expose
    private Itunes itunes;
    @SerializedName("youtube")
    @Expose
    private Youtube youtube;
    @SerializedName("deezer")
    @Expose
    private Deezer deezer;
    @SerializedName("spotify")
    @Expose
    private Spotify spotify;

    /**
     * @return The itunes
     */
    public Itunes getItunes() {
        return itunes;
    }

    /**
     * @param itunes The itunes
     */
    public void setItunes(Itunes itunes) {
        this.itunes = itunes;
    }

    /**
     * @return The youtube
     */
    public Youtube getYoutube() {
        return youtube;
    }

    /**
     * @param youtube The youtube
     */
    public void setYoutube(Youtube youtube) {
        this.youtube = youtube;
    }

    /**
     * @return The deezer
     */
    public Deezer getDeezer() {
        return deezer;
    }

    /**
     * @param deezer The deezer
     */
    public void setDeezer(Deezer deezer) {
        this.deezer = deezer;
    }

    /**
     * @return The spotify
     */
    public Spotify getSpotify() {
        return spotify;
    }

    /**
     * @param spotify The spotify
     */
    public void setSpotify(Spotify spotify) {
        this.spotify = spotify;
    }

}
