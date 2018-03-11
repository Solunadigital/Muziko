package com.muziko.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.mediaplayer.PlayerConstants;

public class FavoriteEdit extends AsyncTask<QueueItem, int[], Boolean> {

    private final FavoriteEditListener listener;
    private final Context context;
    private int type = 0;
    private boolean faved = false;

    public FavoriteEdit(Context context, int type, FavoriteEditListener listener) {
        this.context = context;
        this.type = type;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(QueueItem... params) {

        QueueItem queue = params[0];

        faved = TrackRealmHelper.toggleFavorite(queue);

        if (faved) {
            context.sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));

            context.sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));

            if (type != PlayerConstants.QUEUE_TYPE_QUEUE)
                context.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));

            if (type != PlayerConstants.QUEUE_TYPE_TRACKS) {   // && type != PlayerConstants.QUEUE_TYPE_FOLDERS)
                context.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED));
                context.sendBroadcast(new Intent(AppController.INTENT_TRACK_CHANGED_HOME));
            }
        }
        return faved;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        if (listener != null) {
            listener.onFavoriteEdited(s);
        }

        if (s) {
            if (faved) {
                AppController.toast(context, context.getString(R.string.song_added_to_fav));
            } else {
                AppController.toast(context, context.getString(R.string.song_removed_from_favs));
            }
        }
        super.onPostExecute(s);
    }

    public interface FavoriteEditListener {
        void onFavoriteEdited(boolean s);
    }
}
