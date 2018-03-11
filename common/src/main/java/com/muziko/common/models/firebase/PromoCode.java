package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

/**
 * Created by dev on 19/10/2016.
 */
@Keep
public class PromoCode {
    private String promoCode;
    private String androidID;
    private Object timestamp;

    public PromoCode() {
        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public PromoCode(String promoCode, String androidID, Object timestamp) {
        this.promoCode = promoCode;
        this.androidID = androidID;
        this.timestamp = timestamp;
    }

    public String getPromoCode() {
        return promoCode;
    }

    public String getAndroidID() {
        return androidID;
    }

    public Object getTimestamp() {
        return timestamp;
    }
}

