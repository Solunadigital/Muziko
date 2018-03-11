package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;

import com.crashlytics.android.Crashlytics;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.helpers.StorageHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.MediaHelper;

import org.apache.commons.io.FileUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by dev on 26/09/2016.
 */


public class ScanMediaFilesBackground extends AsyncTask<Void, String, Void> {

    private final Context context;
    private final List<File> mfiles = new ArrayList<>();
    private final String[] mExtensions;
    private int countBefore = 0;

    public ScanMediaFilesBackground(Context ctx, String[] extensions) {
        this.context = ctx;
        mExtensions = extensions;
    }

    @Override
    protected Void doInBackground(Void... params) {

        List<StorageHelper.StorageVolume> storageVolumes = StorageHelper.getStorages();

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            try {


                for (int i = 0; i < storageVolumes.size(); i++) {
                    mfiles.addAll(FileUtils.listFiles(storageVolumes.get(i).file, mExtensions, true));
                }

                for (final File file : mfiles) {
                    QueueItem existingTrack = TrackRealmHelper.getTrack(file.getAbsolutePath());
                    if (existingTrack == null) {
                        MediaHelper.Instance().loadMusicFromTrackAsync(file.getAbsolutePath(), false);
                    }
                }

            } catch (Exception e) {
                Crashlytics.logException(e);
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        countBefore = TrackRealmHelper.getCount();
    }

    @Override
    protected void onPostExecute(Void result) {
        super.onPostExecute(result);
        int countAfter = TrackRealmHelper.getCount();
        int diff = countAfter - countBefore;
        if (diff > 0) {
            AppController.toast(context, "Scan complete. " + diff + String.format(" song%s added", diff != 1 ? "s" : ""));
            EventBus.getDefault().post(new RefreshEvent(1000));
        }
    }
}
