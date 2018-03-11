package com.muziko.api.YouTube;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
public class Statistics {

    @SerializedName("dislikeCount")
    @Expose
    private String dislikeCount;

    @SerializedName("likeCount")
    @Expose
    private String likeCount;

    @SerializedName("viewCount")
    @Expose
    private String viewCount;

    @SerializedName("favoriteCount")
    @Expose
    private String favoriteCount;

    @SerializedName("commentCount")
    @Expose
    private String commentCount;

    public String getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(String dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public String getViewCount() {
        return viewCount;
    }

    public void setViewCount(String viewCount) {
        this.viewCount = viewCount;
    }

    public String getFavoriteCount() {
        return favoriteCount;
    }

    public void setFavoriteCount(String favoriteCount) {
        this.favoriteCount = favoriteCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }

    @Override
    public String toString() {
        return
                "Statistics{" +
                        "dislikeCount = '" + dislikeCount + '\'' +
                        ",likeCount = '" + likeCount + '\'' +
                        ",viewCount = '" + viewCount + '\'' +
                        ",favoriteCount = '" + favoriteCount + '\'' +
                        ",commentCount = '" + commentCount + '\'' +
                        "}";
    }
}