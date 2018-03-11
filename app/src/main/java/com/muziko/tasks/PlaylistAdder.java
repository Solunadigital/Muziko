package com.muziko.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.PlaylistQueueItem;
import com.muziko.common.models.QueueItem;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.manager.AppController;

import java.util.ArrayList;

public class PlaylistAdder extends AsyncTask<Void, int[], Boolean> {
    private static final String TAG = PlaylistAdder.class.getSimpleName();

    private final Context ctx;
    private final PlaylistItem playlist;
    private final ArrayList<QueueItem> items;
    private final boolean override;
    private int counter = 0;

    public PlaylistAdder(Context ctx, PlaylistItem playlist, ArrayList<QueueItem> items, boolean override) {
        this.ctx = ctx;
        this.playlist = playlist;
        this.items = new ArrayList<>(items);
        this.override = override;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        counter = 0;
        Log.e(TAG, "playlist save start");


        for (QueueItem queue : items) {

            PlaylistQueueItem item = new PlaylistQueueItem();
            item.copyQueue(queue);
            item.playlist = playlist.id;
            PlaylistSongRealmHelper.insert(item, override);
            counter++;
//            FirebaseManager.Instance().uploadPlaylist(playlist.hash);
        }
        Log.e(TAG, "playlist save done");

        return true;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean s) {
        super.onPostExecute(s);

        Intent intent = new Intent(AppController.INTENT_PLAYLIST_CHANGED);
        intent.putExtra("id", playlist.id);
        ctx.sendBroadcast(intent);
        AppController.toast(ctx, String.format("%d song%s saved in playlist", counter, counter != 1 ? "s" : ""));
    }
}


