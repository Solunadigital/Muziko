package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;


public class QueueAddEvent implements Parcelable {

    public static final Creator<QueueAddEvent> CREATOR = new Creator<QueueAddEvent>() {
        @Override
        public QueueAddEvent createFromParcel(Parcel in) {
            return new QueueAddEvent(in);
        }

        @Override
        public QueueAddEvent[] newArray(int size) {
            return new QueueAddEvent[size];
        }
    };
    private String name;
    private Integer position;

    public QueueAddEvent(String name, Integer position) {
        this.name = name;
        this.position = position;
    }

    public QueueAddEvent(Parcel in) {
        this.name = in.readString();
        this.position = in.readInt();
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
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
        dest.writeInt(position);
    }

    @Override
    public String toString() {
        return "CustomObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
