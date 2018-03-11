package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudPlaylist;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by dev on 27/10/2016.
 */
public class FirebaseDeleteForUpdateTask extends AsyncTask<Void, Double, Boolean> {

    private ArrayList<QueueItem> queueItems;
    private WeakReference<Context> mContext;
    private QueueItem queueItem;
    private CloudTrack cloudTrack;
    private CloudPlaylist cloudPlaylist;
    private boolean deleted;
    private PlaylistItem playlistItem;
    private String playlistHash;
    private MuzikoConstants.FirebaseFileMode firebaseFileMode;
    private DatabaseReference databaseReference;

    public FirebaseDeleteForUpdateTask(Context ctx, QueueItem queueItem, String playlistHash, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        mContext = new WeakReference<>(ctx);
        this.queueItem = queueItem;
        this.playlistHash = playlistHash;
        playlistItem = PlaylistRealmHelper.getPlaylist(playlistHash);
        queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, playlistItem.id);
        this.firebaseFileMode = firebaseFileMode;

        for (CloudPlaylist cloudPlaylist : FirebaseManager.Instance().getFirebasePlaylistsList()) {
            if (cloudPlaylist.getUid().equals(playlistHash)) {
                this.cloudPlaylist = cloudPlaylist;
            }
        }

        for (CloudTrack cloudTrack : FirebaseManager.Instance().getFirebasePlaylistTracksList()) {
            if (cloudTrack.getMd5().equalsIgnoreCase(queueItem.getMd5())) {
                this.cloudTrack = cloudTrack;
            }
        }
    }

    public FirebaseDeleteForUpdateTask(QueueItem queueItem, MuzikoConstants.FirebaseFileMode firebaseFileMode) {
        this.queueItem = queueItem;
        this.firebaseFileMode = firebaseFileMode;

        switch (firebaseFileMode) {
            case LIBRARY:
                for (CloudTrack cloudTrack : FirebaseManager.Instance().getFirebaseLibraryList()) {
                    if (cloudTrack.getMd5().equalsIgnoreCase(queueItem.getMd5())) {
                        this.cloudTrack = cloudTrack;
                    }
                }
                break;
            case FAVS:
                for (CloudTrack cloudTrack : FirebaseManager.Instance().getFirebaseFavsList()) {
                    if (cloudTrack.getMd5().equalsIgnoreCase(queueItem.getMd5())) {
                        this.cloudTrack = cloudTrack;
                    }
                }
                break;
        }
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        if (cloudTrack == null) return false;

        try {
            String cloudResult = CloudManager.Instance().getMuzikoCloud().delete(queueItem.getMd5(), FirebaseInstanceId.getInstance().getToken()).execute().getData();

        } catch (IOException e) {
            Crashlytics.logException(e);
            return null;
        }

        FirebaseStorage storageRef = FirebaseStorage.getInstance();
        final StorageReference httpsReference = storageRef.getReferenceFromUrl(cloudTrack.getUrl());

        switch (firebaseFileMode) {
            case LIBRARY:
                databaseReference = FirebaseManager.Instance().getLibraryRef();
                break;
            case FAVS:
                databaseReference = FirebaseManager.Instance().getFavRef();
                break;
            case PLAYLISTS:
                databaseReference = FirebaseManager.Instance().getPlaylistsTracksRef();
                break;
        }

        httpsReference
                .delete()
                .addOnSuccessListener(o -> {

                    databaseReference
                            .child(FirebaseManager.Instance().getCurrentUserId())
                            .child(cloudTrack.getUid())
                            .removeValue();
                })
                .addOnFailureListener(throwable -> {
                    deleted = false;
                    Crashlytics.logException(throwable);
                });

        return deleted;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean deleted) {
        if (deleted) {
        }
        super.onPostExecute(deleted);
    }

    @Override
    protected void onProgressUpdate(Double... values) {
        super.onProgressUpdate(values);
    }
}
