package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;

import static android.R.attr.name;


public class QueueRemoveEvent implements Parcelable {

    public static final Creator<QueueRemoveEvent> CREATOR = new Creator<QueueRemoveEvent>() {
        @Override
        public QueueRemoveEvent createFromParcel(Parcel in) {
            return new QueueRemoveEvent(in);
        }

        @Override
        public QueueRemoveEvent[] newArray(int size) {
            return new QueueRemoveEvent[size];
        }
    };
    private Integer position;

    public QueueRemoveEvent(Integer position) {
        this.position = position;
    }

    public QueueRemoveEvent(Parcel in) {
        this.position = in.readInt();
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(position);
    }

    @Override
    public String toString() {
        return "CustomObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
