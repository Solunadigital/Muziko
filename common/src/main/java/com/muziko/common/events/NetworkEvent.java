package com.muziko.common.events;

import android.support.annotation.Keep;

/**
 * Created by Bradley on 10/02/2017.
 */
@Keep
public class NetworkEvent {
    private int state;
    private int type;

    public NetworkEvent(int state, int type) {
        this.state = state;
        this.type = type;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

}
