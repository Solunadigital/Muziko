
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class Youtube {

    @SerializedName("vid")
    @Expose
    private String vid;

    /**
     * @return The vid
     */
    public String getVid() {
        return vid;
    }

    /**
     * @param vid The vid
     */
    public void setVid(String vid) {
        this.vid = vid;
    }

}
