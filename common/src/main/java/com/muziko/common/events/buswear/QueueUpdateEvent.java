package com.muziko.common.events.buswear;

import android.os.Parcel;
import android.os.Parcelable;

import static android.R.attr.name;


public class QueueUpdateEvent implements Parcelable {

    public static final Creator<QueueUpdateEvent> CREATOR = new Creator<QueueUpdateEvent>() {
        @Override
        public QueueUpdateEvent createFromParcel(Parcel in) {
            return new QueueUpdateEvent(in);
        }

        @Override
        public QueueUpdateEvent[] newArray(int size) {
            return new QueueUpdateEvent[size];
        }
    };
    private Integer queueindex;
    private Integer state;
    private Integer shuffle;
    private Integer repeat;

    public QueueUpdateEvent(Integer queueindex, Integer state, Integer shuffle, Integer repeat) {
        this.queueindex = queueindex;
        this.state = state;
        this.shuffle = shuffle;
        this.repeat = repeat;
    }

    public QueueUpdateEvent(Parcel in) {
        this.queueindex = in.readInt();
        this.state = in.readInt();
        this.shuffle = in.readInt();
        this.repeat = in.readInt();
    }

    public Integer getShuffle() {
        return shuffle;
    }

    public void setShuffle(Integer shuffle) {
        this.shuffle = shuffle;
    }

    public Integer getRepeat() {
        return repeat;
    }

    public void setRepeat(Integer repeat) {
        this.repeat = repeat;
    }

    public Integer getQueueindex() {
        return queueindex;
    }

    public void setQueueindex(Integer queueindex) {
        this.queueindex = queueindex;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(queueindex);
        dest.writeInt(state);
        dest.writeInt(shuffle);
        dest.writeInt(repeat);
    }

    @Override
    public String toString() {
        return "CustomObject{" +
                "name='" + name + '\'' +
                '}';
    }
}
