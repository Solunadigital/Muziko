package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.provider.DocumentFile;

import com.crashlytics.android.Crashlytics;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.SAFHelpers;

import java.io.File;

public class FileDateModifiedUpdater extends AsyncTask<Object, int[], Boolean> {

    private final Context mContext;

    public FileDateModifiedUpdater(Context mContext) {
        this.mContext = mContext;
    }

    @Override
    protected Boolean doInBackground(Object... params) {
        QueueItem queueItem = (QueueItem) params[0];
        File file = new File(queueItem.data);

        if (queueItem.storage == 1) {
            file.setLastModified(System.currentTimeMillis());
        } else if (queueItem.storage == 2) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && FileHelper.isOnExtSdCard(mContext, file)) {
                DocumentFile targetDocument = null;
                try {
                    targetDocument = SAFHelpers.getDocumentFile(new File(queueItem.data), false);
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }

                if (targetDocument != null) {
                    file.setLastModified(System.currentTimeMillis());
                }
            } else {
                file.setLastModified(System.currentTimeMillis());
            }
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean s) {

        super.onPostExecute(s);
    }
}
