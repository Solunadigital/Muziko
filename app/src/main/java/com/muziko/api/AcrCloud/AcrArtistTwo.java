
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class AcrArtistTwo {

    @SerializedName("id")
    @Expose
    private Integer id;

    /** @return The id */
    public Integer getId() {
        return id;
    }

    /** @param id The id */
    public void setId(Integer id) {
        this.id = id;
    }
}
