package com.muziko.api.YouTube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.List;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class YouTubeDetails {

    @SerializedName("kind")
    @Expose
    private String kind;

    @SerializedName("pageInfo")
    @Expose
    private PageInfo pageInfo;

    @SerializedName("etag")
    @Expose
    private String etag;

    @SerializedName("items")
    @Expose
    private List<ItemsItem> items;

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(PageInfo pageInfo) {
        this.pageInfo = pageInfo;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }

    public List<ItemsItem> getItems() {
        return items;
    }

    public void setItems(List<ItemsItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return
                "YouTubeDetails{" +
                        "kind = '" + kind + '\'' +
                        ",pageInfo = '" + pageInfo + '\'' +
                        ",etag = '" + etag + '\'' +
                        ",items = '" + items + '\'' +
                        "}";
    }
}