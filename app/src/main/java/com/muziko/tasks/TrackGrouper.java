package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.mediaplayer.PlayerConstants;

import java.util.ArrayList;

public class TrackGrouper extends AsyncTask<String, String, Boolean> {
    private final ArrayList<QueueItem> list = new ArrayList<>();
    private final TrackGrouperListener listener;
    private int playType = 0;
    private int playDuration = 0;
    private String playData = "";

    public TrackGrouper(Context ctx, int type, String data, TrackGrouperListener listener) {
        Context ctx1 = ctx;
        this.playData = data;
        this.playType = type;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean take = false;

        switch (playType) {
            case PlayerConstants.QUEUE_TYPE_ALBUMS:

                list.addAll(TrackRealmHelper.getTracksForAlbum(playData));

//				for (QueueItem queueItem : TrackRealmHelper.getTracksForAlbum(playData).values()) {
//					playDuration += Utils.getInt(queueItem.duration, 0);
//					list.add(queueItem);
//				}
                break;
            case PlayerConstants.QUEUE_TYPE_ARTISTS:

                list.addAll(TrackRealmHelper.getTracksForArtist(playData));

//				for (QueueItem queueItem : TrackRealmHelper.getTracksForArtist(playData).values()) {
//					playDuration += Utils.getInt(queueItem.duration, 0);
//					list.add(queueItem);
//				}
                break;
            case PlayerConstants.QUEUE_TYPE_GENRES:

                list.addAll(TrackRealmHelper.getTracksForGenre(playData));

//				for (QueueItem queueItem : TrackRealmHelper.getTracksForGenre(playData).values()) {
//					playDuration += Utils.getInt(queueItem.duration, 0);
//					list.add(queueItem);
//				}
                break;
        }

        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        playDuration = 0;

        if (listener != null)
            listener.onTrackGrouperStarted();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (listener != null)
            listener.onTrackGrouperCompleted(list);
    }


    public interface TrackGrouperListener {
        void onTrackGrouperStarted();

        void onTrackGrouperCompleted(ArrayList<QueueItem> list);
    }
}
