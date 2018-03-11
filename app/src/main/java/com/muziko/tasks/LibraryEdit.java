package com.muziko.tasks;

import android.content.Context;
import android.os.AsyncTask;

import com.muziko.R;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;

import pl.tajchert.buswear.EventBus;

public class LibraryEdit extends AsyncTask<QueueItem, int[], Boolean> {

    private final LibraryEditListener listener;
    private final Context context;
    private boolean dontNotify = false;
    private boolean remove;
    private boolean addedToLibrary = false;
    private QueueItem queueItem;

    public LibraryEdit(Context context, LibraryEditListener listener) {
        this.context = context;
        this.listener = listener;
    }

    public LibraryEdit(Context context, boolean remove, LibraryEditListener listener) {
        this.context = context;
        this.remove = remove;
        this.listener = listener;
    }

    public LibraryEdit(Context context, boolean remove, boolean dontNotify, LibraryEditListener listener) {
        this.context = context;
        this.remove = remove;
        this.dontNotify = dontNotify;
        this.listener = listener;
    }

    @Override
    protected Boolean doInBackground(QueueItem... params) {

        queueItem = params[0];

        if (remove) {
            addedToLibrary = TrackRealmHelper.toggleLibrary(queueItem, false);
        } else {
            addedToLibrary = TrackRealmHelper.toggleLibrary(queueItem, true);
        }

        return addedToLibrary;
    }

    @Override
    protected void onPostExecute(Boolean addedToLibrary) {
        if (listener != null) {
            listener.onLibraryEdited(addedToLibrary);
        }

        if (addedToLibrary) {
            FirebaseManager.Instance().checkforLibraryTransfers();
            if (!dontNotify) {
                AppController.toast(context, context.getString(R.string.song_added_to_library));
            }

        } else {
            FirebaseManager.Instance().deleteLibrary(queueItem);
            if (!dontNotify) {
                AppController.toast(context, context.getString(R.string.song_removed_from_library));
            }
        }
        EventBus.getDefault(context).postLocal(new RefreshEvent(1000));
        super.onPostExecute(addedToLibrary);
    }

    public interface LibraryEditListener {
        void onLibraryEdited(boolean addedToLibrary);
    }
}
