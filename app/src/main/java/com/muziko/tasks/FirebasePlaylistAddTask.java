package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.iid.FirebaseInstanceId;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.FirebaseCloudEvent;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.CloudPlaylist;
import com.muziko.common.models.firebase.CloudTrack;
import com.muziko.database.PlaylistRealmHelper;
import com.muziko.database.PlaylistSongRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.MD5;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.MuzikoConstants;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dev on 27/10/2016.
 */

public class FirebasePlaylistAddTask extends AsyncTask<Void, Double, Void> {

    private final String playlistHash;
    private WeakReference<Context> mContext;
    private ArrayList<QueueItem> queueItems;
    private PlaylistItem playlistItem;
    private CloudPlaylist cloudPlaylist;
    private DatabaseReference playlistsRef;

    public FirebasePlaylistAddTask(Context ctx, String playlistHash) {
        mContext = new WeakReference<>(ctx);
        this.playlistHash = playlistHash;
        playlistItem = PlaylistRealmHelper.getPlaylist(playlistHash);
        queueItems = PlaylistSongRealmHelper.loadAllByPlaylist(0, playlistItem.id);
    }

    @Override
    protected Void doInBackground(Void... params) {

        playlistsRef = FirebaseManager.Instance().getPlaylistsRef().child(FirebaseManager.Instance().getCurrentUserId());
        List<String> cloudTracks = new ArrayList<>();
        for (QueueItem queueItem : queueItems) {
            cloudTracks.add(queueItem.md5);
        }
        cloudPlaylist = new CloudPlaylist(playlistHash, playlistItem.id, playlistItem.title, playlistItem.duration, playlistItem.date, cloudTracks);
        playlistsRef.child(playlistHash).setValue(cloudPlaylist, (error, firebase) -> {
            if (error != null) {
                AppController.toast(mContext.get(), "Network connection failed");
            } else {
                FirebaseManager.Instance().removePlaylistUploadTask(playlistHash);
            }
        });

        for (QueueItem queueItem : queueItems) {
            boolean found = false;
            if (queueItem.md5 == null) {
                queueItem.md5 = MD5.calculateMD5(new File(queueItem.data));
                TrackRealmHelper.updateMD5Hash(queueItem);
            }
            for (CloudTrack cloudTrack : FirebaseManager.Instance().getFirebasePlaylistTracksList()) {
                if (queueItem.md5.equals(cloudTrack.getMd5())) {
                    found = true;
                }
            }
            if (!found) {
//                FirebaseManager.Instance().uploadPlaylistTrack(queueItem, playlistItem.hash);
            }
        }

        return null;
    }


    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);

        AsyncJob.doInBackground(() -> {
            try {
                String cloudResult = CloudManager.Instance().getMuzikoCloud().upload(playlistHash, FirebaseInstanceId.getInstance().getToken()).execute().getData();
                String message = "";
                if (cloudResult.equals(MuzikoConstants.CloudFileActions.UPLOAD.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_uploading_uploading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent("Playlist - " + playlistItem.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancel(true);
                } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DOWNLOAD.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_downloading_uploading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent("Playlist - " + playlistItem.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancel(true);
                } else if (cloudResult.equals(MuzikoConstants.CloudFileActions.DELETE.name())) {
                    message = MyApplication.getInstance().getString(R.string.firebase_cloud_delete_uploading);
                    FirebaseCloudEvent firebaseCloudEvent = new FirebaseCloudEvent("Playlist - " + playlistItem.getTitle(), message);
                    EventBus.getDefault().post(firebaseCloudEvent);
                    cancel(true);
                }

            } catch (IOException e) {
                Crashlytics.logException(e);
            }
        });

    }

}
