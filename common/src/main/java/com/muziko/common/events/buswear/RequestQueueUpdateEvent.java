package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;


public class RequestQueueUpdateEvent implements Parcelable {

    public static final Creator<RequestQueueUpdateEvent> CREATOR = new Creator<RequestQueueUpdateEvent>() {
        @Override
        public RequestQueueUpdateEvent createFromParcel(Parcel in) {
            return new RequestQueueUpdateEvent(in);
        }

        @Override
        public RequestQueueUpdateEvent[] newArray(int size) {
            return new RequestQueueUpdateEvent[size];
        }
    };
    private String name;

    public RequestQueueUpdateEvent(String name) {
        this.name = name;
    }

    public RequestQueueUpdateEvent(Parcel in) {
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
