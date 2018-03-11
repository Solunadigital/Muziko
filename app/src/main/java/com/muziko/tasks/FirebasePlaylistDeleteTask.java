package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudPlaylist;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by dev on 27/10/2016.
 */

public class FirebasePlaylistDeleteTask extends AsyncTask<Void, Double, Void> {

    private final ArrayList<QueueItem> queueItems;
    private final PlaylistItem playlistItem;
    private WeakReference<Context> mContext;
    private CloudPlaylist cloudPlaylist;
    private String playlistUid;
    private DatabaseReference playlistsRef;
    private DatabaseReference playlistTracksReference;

    public FirebasePlaylistDeleteTask(Context ctx, PlaylistItem playlistItem) {
        mContext = new WeakReference<>(ctx);
        this.playlistItem = playlistItem;
        playlistUid = playlistItem.hash;
        queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, playlistItem.id);
        for (CloudPlaylist cloudPlaylist : FirebaseManager.Instance().getFirebasePlaylistsList()) {
            if (cloudPlaylist.getUid().equalsIgnoreCase(playlistUid)) {
                this.cloudPlaylist = cloudPlaylist;
            }
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        playlistsRef = FirebaseManager.Instance().getPlaylistsRef();
        cloudPlaylist.setDeleted(true);
        cloudPlaylist.setCloudTracks(null);
        playlistsRef.child(FirebaseManager.Instance().getCurrentUserId()).child(playlistUid).setValue(cloudPlaylist, (error, firebase) -> {
            if (error != null) {
                AppController.toast(mContext.get(), "Network connection failed");
            }
        });

        playlistTracksReference = FirebaseManager.Instance().getPlaylistsTracksRef();
        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        for (QueueItem queueItem : queueItems) {
            for (CloudTrack cloudTrack : FirebaseManager.Instance().getFirebasePlaylistTracksList()) {
                if (cloudTrack.getMd5().equalsIgnoreCase(queueItem.getMd5())) {
                    storageRef.getReferenceFromUrl(cloudTrack.getUrl()).delete().addOnSuccessListener(o -> {

                        playlistTracksReference
                                .child(FirebaseManager.Instance().getCurrentUserId())
                                .child(cloudTrack.getUid())
                                .removeValue();

                    }).addOnFailureListener(throwable -> {
                        Crashlytics.logException(throwable);
                    });
                }
            }
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

    }

}
