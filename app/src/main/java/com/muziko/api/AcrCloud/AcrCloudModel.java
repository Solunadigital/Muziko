
package com.muziko.api.AcrCloud;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("org.jsonschema2pojo")
public class AcrCloudModel {

    @SerializedName("status")
    @Expose
    private Status status;
    @SerializedName("metadata")
    @Expose
    private Metadata metadata;
    @SerializedName("result_type")
    @Expose
    private Integer resultType;

    /**
     * @return The status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * @param status The status
     */
    public void setStatus(Status status) {
        this.status = status;
    }

    /**
     * @return The metadata
     */
    public Metadata getMetadata() {
        return metadata;
    }

    /**
     * @param metadata The metadata
     */
    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }

    /**
     * @return The resultType
     */
    public Integer getResultType() {
        return resultType;
    }

    /**
     * @param resultType The result_type
     */
    public void setResultType(Integer resultType) {
        this.resultType = resultType;
    }

}
