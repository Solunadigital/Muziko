package com.muziko.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.PlayerConstants;

class TrackRename extends AsyncTask<String, int[], Boolean> {

    private final TrackRenameListener listener;
    private final Context ctx;
    private final int type;

    public TrackRename(Context ctx, int type, TrackRenameListener listener) {
        this.ctx = ctx;
        this.type = type;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(String... params) {
        boolean ret = false;

        String data = params[0];
        String name = params[1];

        if (Utils.renameFile(ctx, data, name)) {
            /*
            QueueItem.updateTitle(data, name);
            PlaylistQueueItem.updateTitle(data, name);
            FavoriteItem.updateTitle(data, name);
            RecentItem.updateTitle(data, name);
            MostItem.updateTitle(data, name);
*/
            for (QueueItem queue : PlayerConstants.QUEUE_LIST) {
                if (data.equals(queue.data)) {
                    queue.title = name;
                }
            }

            QueueItem item = TrackRealmHelper.getTrack(data);
            if (item != null) {
                item.title = name;
                // Application.tracks.put(data, item);
            }

            ctx.sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));

            if (type != PlayerConstants.QUEUE_TYPE_QUEUE)
                ctx.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));

            if (type != PlayerConstants.QUEUE_TYPE_TRACKS) {
                ctx.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
                ctx.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
            }
            AppController.Instance().serviceDirty();
            ret = true;
        }

        return ret;
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (listener != null) {
            if (result)
                listener.onTrackRenamed();
            else
                listener.onTrackErrored();
        }

        super.onPostExecute(result);
    }

    public interface TrackRenameListener {
        void onTrackRenamed();

        void onTrackErrored();
    }
}
