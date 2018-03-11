package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.muziko.common.models.PlaylistItem;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.manager.FirebaseManager;

/**
 * Created by divyanshunegi on 11/23/15.
 */
public class PlaylistDelete extends AsyncTask<Long, int[], Boolean> {

    private final long playlistID;

    public PlaylistDelete(Context ctx, long playlistID) {
        Context context = ctx;
        this.playlistID = playlistID;
    }

    @Override
    protected Boolean doInBackground(Long... params) {

        boolean ret = false;

        PlaylistItem playlistItem = PlaylistRealmHelper.getPlaylist(playlistID);

        if (PlaylistRealmHelper.delete(playlistID)) {
            PlaylistSongRealmHelper.deleteByPlaylist(playlistID);
            FirebaseManager.Instance().deletePlaylist(playlistItem);
            ret = true;
        }
        return ret;
    }

    @Override
    protected void onPostExecute(Boolean s) {
//        if(listener!=null)
//        {
//            if(s.booleanValue())
//            {
//                listener.onPlaylistDeleted();
//            }
//            else
//            {
//                listener.onPlaylistErrored();
//            }
//        }
        super.onPostExecute(s);
    }

}
