
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class ExternalIds {

    @SerializedName("isrc")
    @Expose
    private String isrc;
    @SerializedName("upc")
    @Expose
    private String upc;

    /**
     * @return The isrc
     */
    public String getIsrc() {
        return isrc;
    }

    /**
     * @param isrc The isrc
     */
    public void setIsrc(String isrc) {
        this.isrc = isrc;
    }

    /**
     * @return The upc
     */
    public String getUpc() {
        return upc;
    }

    /**
     * @param upc The upc
     */
    public void setUpc(String upc) {
        this.upc = upc;
    }

}
