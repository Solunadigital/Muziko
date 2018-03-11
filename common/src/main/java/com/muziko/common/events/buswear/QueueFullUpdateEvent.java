package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;


public class QueueFullUpdateEvent implements Parcelable {

    public static final Creator<QueueFullUpdateEvent> CREATOR = new Creator<QueueFullUpdateEvent>() {
        @Override
        public QueueFullUpdateEvent createFromParcel(Parcel in) {
            return new QueueFullUpdateEvent(in);
        }

        @Override
        public QueueFullUpdateEvent[] newArray(int size) {
            return new QueueFullUpdateEvent[size];
        }
    };
    private String name;
    private Integer position;
    private Integer state;
    private Integer fresh;

    public QueueFullUpdateEvent(String name, Integer position, Integer state, Integer fresh) {
        this.name = name;
        this.position = position;
        this.state = state;
        this.fresh = fresh;
    }

    public QueueFullUpdateEvent(Parcel in) {
        this.name = in.readString();
        this.position = in.readInt();
        this.state = in.readInt();
        this.fresh = in.readInt();
    }

    public Integer getFresh() {
        return fresh;
    }

    public void setFresh(Integer fresh) {
        this.fresh = fresh;
    }

    public Integer getPosition() {
        return position;
    }

    public void setPosition(Integer position) {
        this.position = position;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
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
        dest.writeInt(state);
        dest.writeInt(fresh);
    }

    @Override
    public String toString() {
        return "CustomObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
