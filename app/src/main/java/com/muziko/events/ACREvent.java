package com.muziko.events;

import com.muziko.api.AcrCloud.TrackModel;

/**
 * Created by Bradley on 16/03/2017.
 */

public class ACREvent {
    private boolean success;
    private TrackModel trackModel;

    public ACREvent(boolean success, TrackModel trackModel) {
        this.success = success;
        this.trackModel = trackModel;
    }

    public TrackModel getTrackModel() {
        return trackModel;
    }

    public void setTrackModel(TrackModel trackModel) {
        this.trackModel = trackModel;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }


}
