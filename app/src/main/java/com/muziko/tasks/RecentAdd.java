package com.muziko.tasks;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;

import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;

public class RecentAdd extends AsyncTask<QueueItem, int[], Boolean> {
    private final Context ctx;

    public RecentAdd(Context ctx) {
        this.ctx = ctx;
    }

    @Override
    protected Boolean doInBackground(QueueItem... params) {
        QueueItem queue = params[0];

        if (doMost(queue)) {
            ctx.sendBroadcast(new Intent(AppController.INTENT_MOST_CHANGED));
        }

        if (doRecent(queue)) {
            ctx.sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
        }

        return true;
    }

    @Override
    protected void onPostExecute(Boolean s) {
        if (s) {
        }

        super.onPostExecute(s);
    }

    private boolean doRecent(QueueItem queue) {
        return TrackRealmHelper.updateRecentPlayed(queue);
//
//        RecentItem recent = new RecentItem();
//        recent.copyQueue(queue);
//	    recent.date = String.valueOf(System.currentTimeMillis() / 1000);
//	    return (recent.insert(recent) > 0);
    }

    private boolean doMost(QueueItem queue) {
        return TrackRealmHelper.increasePlayedCount(queue);

//        boolean ret = false;
//        MostItem most = new MostItem();
//        most.copyQueue(queue);
//
//        if(most.getByData(queue.data))
//        {
//            most.songs++;
//            most.date = String.valueOf(System.currentTimeMillis() / 1000);
//            ret = most.update(most);
//        }
//        else
//        {
//            most.songs = 1;
//            ret = most.insert(most) > 0;
//        }
//
//        return ret;
    }
}
