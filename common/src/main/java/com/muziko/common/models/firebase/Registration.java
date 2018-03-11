package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

/**
 * Created by dev on 18/10/2016.
 */
@Keep
public class Registration {
    private String androidId;
    private String registrationCode;
    private String deviceName;
    private Object timestamp;

    public Registration() {
        // empty default constructor, necessary for Firebase to be able to deserialize comments
    }

    public Registration(String androidId, String registrationCode, String deviceName, Object timestamp) {
        this.androidId = androidId;
        this.registrationCode = registrationCode;
        this.deviceName = deviceName;
        this.timestamp = timestamp;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public String getRegistrationCode() {
        return registrationCode;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public String getAndroidId() {
        return androidId;
    }


}

