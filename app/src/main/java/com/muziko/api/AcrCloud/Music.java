
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Music {

    @SerializedName("external_ids")
    @Expose
    private ExternalIds externalIds;
    @SerializedName("play_offset_ms")
    @Expose
    private Integer playOffsetMs;
    @SerializedName("external_metadata")
    @Expose
    private ExternalMetadata externalMetadata;
    @SerializedName("label")
    @Expose
    private String label;
    @SerializedName("release_date")
    @Expose
    private String releaseDate;
    @SerializedName("title")
    @Expose
    private String title;
    @SerializedName("duration_ms")
    @Expose
    private String durationMs;
    @SerializedName("album")
    @Expose
    private AcrAlbumFour album;
    @SerializedName("acrid")
    @Expose
    private String acrid;
    @SerializedName("result_from")
    @Expose
    private Integer resultFrom;
    @SerializedName("artists")
    @Expose
    private List<AcrArtistFour> artists = new ArrayList<AcrArtistFour>();

    /**
     * @return The externalIds
     */
    public ExternalIds getExternalIds() {
        return externalIds;
    }

    /**
     * @param externalIds The external_ids
     */
    public void setExternalIds(ExternalIds externalIds) {
        this.externalIds = externalIds;
    }

    /**
     * @return The playOffsetMs
     */
    public Integer getPlayOffsetMs() {
        return playOffsetMs;
    }

    /**
     * @param playOffsetMs The play_offset_ms
     */
    public void setPlayOffsetMs(Integer playOffsetMs) {
        this.playOffsetMs = playOffsetMs;
    }

    /**
     * @return The externalMetadata
     */
    public ExternalMetadata getExternalMetadata() {
        return externalMetadata;
    }

    /**
     * @param externalMetadata The external_metadata
     */
    public void setExternalMetadata(ExternalMetadata externalMetadata) {
        this.externalMetadata = externalMetadata;
    }

    /**
     * @return The label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label The label
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return The releaseDate
     */
    public String getReleaseDate() {
        return releaseDate;
    }

    /**
     * @param releaseDate The release_date
     */
    public void setReleaseDate(String releaseDate) {
        this.releaseDate = releaseDate;
    }

    /**
     * @return The title
     */
    public String getTitle() {
        return title;
    }

    /**
     * @param title The title
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * @return The durationMs
     */
    public String getDurationMs() {
        return durationMs;
    }

    /**
     * @param durationMs The duration_ms
     */
    public void setDurationMs(String durationMs) {
        this.durationMs = durationMs;
    }

    /** @return The album */
    public AcrAlbumFour getAlbum() {
        return album;
    }

    /** @param album The album */
    public void setAlbum(AcrAlbumFour album) {
        this.album = album;
    }

    /**
     * @return The acrid
     */
    public String getAcrid() {
        return acrid;
    }

    /**
     * @param acrid The acrid
     */
    public void setAcrid(String acrid) {
        this.acrid = acrid;
    }

    /**
     * @return The resultFrom
     */
    public Integer getResultFrom() {
        return resultFrom;
    }

    /**
     * @param resultFrom The result_from
     */
    public void setResultFrom(Integer resultFrom) {
        this.resultFrom = resultFrom;
    }

    /** @return The artists */
    public List<AcrArtistFour> getArtists() {
        return artists;
    }

    /** @param artists The artists */
    public void setArtists(List<AcrArtistFour> artists) {
        this.artists = artists;
    }

}
