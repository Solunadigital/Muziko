
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Metadata {

    @SerializedName("music")
    @Expose
    private List<Music> music = new ArrayList<Music>();
    @SerializedName("timestamp_utc")
    @Expose
    private String timestampUtc;

    /**
     * @return The music
     */
    public List<Music> getMusic() {
        return music;
    }

    /**
     * @param music The music
     */
    public void setMusic(List<Music> music) {
        this.music = music;
    }

    /**
     * @return The timestampUtc
     */
    public String getTimestampUtc() {
        return timestampUtc;
    }

    /**
     * @param timestampUtc The timestamp_utc
     */
    public void setTimestampUtc(String timestampUtc) {
        this.timestampUtc = timestampUtc;
    }

}
