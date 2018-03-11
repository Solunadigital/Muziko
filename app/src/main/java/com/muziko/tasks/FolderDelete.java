package com.muziko.tasks;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.MediaStore;

import com.muziko.common.models.QueueItem;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.QueueHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.MediaHelper;
import com.muziko.mediaplayer.PlayerConstants;

import java.util.ArrayList;

public class FolderDelete extends AsyncTask<String, int[], String> {

    private final FolderDeleteListener listener;
    private final Context mContext;

    public FolderDelete(Context mContext, FolderDeleteListener listener) {
        this.mContext = mContext;
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... params) {
        String path = params[0];

        Uri rootUri1 = MediaStore.Audio.Media.getContentUriForPath(path);

        ContentResolver cr = mContext.getContentResolver();

        ArrayList<String> paths = new ArrayList<>();
        Cursor c = cr.query(rootUri1, new String[]{MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DATA}, MediaStore.MediaColumns.DATA + " LIKE ?", new String[]{path + "%"}, null);
        if (c != null) {
            while (c.moveToNext()) {
                //long id = c.getLong(c.getColumnIndex(MediaStore.MediaColumns._ID));
                String data = c.getString(c.getColumnIndex(MediaStore.MediaColumns.DATA));

                paths.add(data);
            }
            c.close();
        }

        boolean ret = false;
        ret = PlaylistSongRealmHelper.deleteLikeData(path);

        ArrayList<QueueItem> del = new ArrayList<>();
        for (QueueItem item : PlayerConstants.QUEUE_LIST) {
            if (item.data.startsWith(path)) {
                del.add(item);
            }
        }
        PlayerConstants.QUEUE_LIST.removeAll(del);
        QueueHelper.saveQueue(mContext);
        AppController.Instance().updateQueueIndex();

        if (del.size() > 0) {
            AppController.Instance().serviceDirty();
        }

        String playing = null;
        for (String data : paths) {
            if (PlayerConstants.QUEUE_SONG.data.length() > 0 && PlayerConstants.QUEUE_SONG.data.equals(data)) {
                playing = data;
            }

            Utils.deleteSong(mContext, data);

            TrackRealmHelper.deleteTrack(data);
//            MyApplication.favorites.remove(data);
        }

        paths.clear();
        MediaHelper.Instance().loadMusic();

        AppController.Instance().refreshMusicData(path);

        if (playing != null) {
            AppController.Instance().serviceDelete(PlayerConstants.QUEUE_TYPE_FOLDERS, "", playing);
        } else {
            mContext.sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));

            mContext.sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));

            mContext.sendBroadcast(new Intent(AppController.INTENT_TRACK_DELETED));
        }
        return null;
    }

    @Override
    protected void onPostExecute(String s) {
        if (listener != null) {
            listener.onFolderDeleted();
        }

        super.onPostExecute(s);
    }

    public interface FolderDeleteListener {
        void onFolderDeleted();
    }
}
