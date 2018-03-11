package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;


public class GotUpdateEvent implements Parcelable {

    public static final Creator<GotUpdateEvent> CREATOR = new Creator<GotUpdateEvent>() {
        @Override
        public GotUpdateEvent createFromParcel(Parcel in) {
            return new GotUpdateEvent(in);
        }

        @Override
        public GotUpdateEvent[] newArray(int size) {
            return new GotUpdateEvent[size];
        }
    };
    private String name;

    public GotUpdateEvent(String name) {
        this.name = name;
    }

    public GotUpdateEvent(Parcel in) {
        this.name = in.readString();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
    }

    @Override
    public String toString() {
        return "CustomObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
