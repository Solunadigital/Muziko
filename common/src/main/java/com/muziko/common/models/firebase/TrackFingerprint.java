package com.muziko.common.models.firebase;

import android.support.annotation.Keep;

/**
 * Created by Bradley on 16/03/2017.
 */
@Keep
public class TrackFingerprint {

    private String acrid;
    private String data;

    public TrackFingerprint() {

    }

    public TrackFingerprint(String acrid, String data) {
        this.acrid = acrid;
        this.data = data;
    }

    public String getAcrid() {
        return acrid;
    }

    public void setAcrid(String acrid) {
        this.acrid = acrid;
    }

    @Override
    public int hashCode() {
        int result = acrid.hashCode();
        result = 31 * result + data.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TrackFingerprint that = (TrackFingerprint) o;

        if (!acrid.equals(that.acrid)) return false;
        return data.equals(that.data);

    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}

