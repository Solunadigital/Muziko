package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

/**
 * Created by dev on 18/10/2016.
 */
@Keep
@JsonObject
public class ShareSalut {
    @JsonField
    public String uid;
    @JsonField
    public String shareData;
    @JsonField
    public String filename;
    @JsonField
    public String title;
    @JsonField
    public String artist;
    @JsonField
    public String album;
}

