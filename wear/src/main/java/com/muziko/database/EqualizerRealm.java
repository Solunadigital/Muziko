package com.muziko.database;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by dev on 8/07/2016.
 */

public class EqualizerRealm extends RealmObject {

    @PrimaryKey
    private long id;
    private String title;
    private int band1;
    private int band2;
    private int band3;
    private int band4;
    private int band5;
    private int band6;
    private int band7;
    private int band8;
    private int band9;
    private int band10;

    private int bass;
    private int threed;
    private int loudness;
    private int reverb;
    private long Updated;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getBand1() {
        return band1;
    }

    public void setBand1(int band1) {
        this.band1 = band1;
    }

    public int getBand2() {
        return band2;
    }

    public void setBand2(int band2) {
        this.band2 = band2;
    }

    public int getBand3() {
        return band3;
    }

    public void setBand3(int band3) {
        this.band3 = band3;
    }

    public int getBand4() {
        return band4;
    }

    public void setBand4(int band4) {
        this.band4 = band4;
    }

    public int getBand5() {
        return band5;
    }

    public void setBand5(int band5) {
        this.band5 = band5;
    }

    public int getBand6() {
        return band6;
    }

    public void setBand6(int band6) {
        this.band6 = band6;
    }

    public int getBand7() {
        return band7;
    }

    public void setBand7(int band7) {
        this.band7 = band7;
    }

    public int getBand8() {
        return band8;
    }

    public void setBand8(int band8) {
        this.band8 = band8;
    }

    public int getBand9() {
        return band9;
    }

    public void setBand9(int band9) {
        this.band9 = band9;
    }

    public int getBand10() {
        return band10;
    }

    public void setBand10(int band10) {
        this.band10 = band10;
    }

    public int getBass() {
        return bass;
    }

    public void setBass(int bass) {
        this.bass = bass;
    }

    public int getThreed() {
        return threed;
    }

    public void setThreed(int threed) {
        this.threed = threed;
    }

    public int getLoudness() {
        return loudness;
    }

    public void setLoudness(int loudness) {
        this.loudness = loudness;
    }

    public int getReverb() {
        return reverb;
    }

    public void setReverb(int reverb) {
        this.reverb = reverb;
    }

    public long getUpdated() {
        return Updated;
    }

    public void setUpdated(long updated) {
        Updated = updated;
    }
}