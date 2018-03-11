package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;


public class WearActionEvent implements Parcelable {

    public static final Creator<WearActionEvent> CREATOR = new Creator<WearActionEvent>() {
        @Override
        public WearActionEvent createFromParcel(Parcel in) {
            return new WearActionEvent(in);
        }

        @Override
        public WearActionEvent[] newArray(int size) {
            return new WearActionEvent[size];
        }
    };
    private String name;
    private Integer position;

    public WearActionEvent(String name, Integer position) {
        this.name = name;
        this.position = position;
    }

    public WearActionEvent(Parcel in) {
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
