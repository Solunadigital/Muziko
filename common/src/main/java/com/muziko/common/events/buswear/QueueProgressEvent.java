package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;

import static android.R.attr.name;


public class QueueProgressEvent implements Parcelable {

    public static final Creator<QueueProgressEvent> CREATOR = new Creator<QueueProgressEvent>() {
        @Override
        public QueueProgressEvent createFromParcel(Parcel in) {
            return new QueueProgressEvent(in);
        }

        @Override
        public QueueProgressEvent[] newArray(int size) {
            return new QueueProgressEvent[size];
        }
    };
    private Integer position;
    private Integer duration;

    public QueueProgressEvent(Integer position, Integer duration) {
        this.position = position;
        this.duration = duration;
    }

    public QueueProgressEvent(Parcel in) {
        this.position = in.readInt();
        this.duration = in.readInt();

    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
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
        dest.writeInt(duration);
    }

    @Override
    public String toString() {
        return "CustomObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
