package com.muziko.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;

import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.models.Lyrics;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.NotificationController;
import com.muziko.receivers.NotificationBroadcast;
import com.muziko.tasks.LyricsDownloader;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static br.com.zbra.androidlinq.Linq.stream;


public class LyricsDownloaderService extends Service implements Lyrics.Callback {

	// Sets the amount of time an idle thread will wait for a task before terminating
	private static final int KEEP_ALIVE_TIME = 1;
	// Sets the Time Unit to seconds
	private static final TimeUnit KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS;
	// Sets the threadpool size to 8
	private static final int CORE_POOL_SIZE = 2;
	private static final int MAXIMUM_POOL_SIZE = 8;
	private ThreadPoolExecutor mDownloadThreadPool;
	private int successCount = 0;
    private android.app.NotificationManager mNotifyManager;
    private NotificationCompat.Builder mBuilder;
    private int nID;
	private int total = 0;
	private int count = 0;
	private int mProgress = 0;
	private boolean cancel = false;

	@Override
	public void onCreate() {
		super.onCreate();

		mDownloadThreadPool = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE,
				KEEP_ALIVE_TIME, KEEP_ALIVE_TIME_UNIT, new LinkedBlockingQueue<>());

		int min = 0;
		int max = 1000;

		Random r = new Random();
		nID = r.nextInt(max - min + 1) + min;

        mNotifyManager =
                (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        mBuilder = new NotificationCompat.Builder(this, "Muziko");
        mBuilder.setContentTitle("Downloading missing lyrics")
                .setContentText("Download in progress - Swipe to cancel")
                .setSmallIcon(NotificationController.Instance().getDownloadNotificationIcon());

		mBuilder.setProgress(100, 0, false);
		mBuilder.setDeleteIntent(getDeleteIntent());
		mNotifyManager.notify(nID, mBuilder.build());
	}

	@SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (intent != null) {
			String action = intent.getAction();
			if (action != null) {

				if (action.equalsIgnoreCase(MyApplication.ACTION_UPDATE_LYRICS)) {
					SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getApplicationContext());
					boolean prefLyricsDownload = prefs.getBoolean("prefLyricsDownload", false);
					if (prefLyricsDownload) {
						ArrayList<QueueItem> trackList = new ArrayList<>();
						trackList.addAll(TrackRealmHelper.getTracks(0).values());

						ArrayList<QueueItem> missingLyrics = new ArrayList<>();

						missingLyrics.addAll(stream(trackList)
								.where(c -> c.lyrics == null)
								.toList());

						for (final QueueItem queueItem : missingLyrics) {
							total = missingLyrics.size();
							if (queueItem.lyrics == null) {
								mDownloadThreadPool.execute(LyricsDownloader.getRunnable(this, true, queueItem.artist_name, queueItem.title));
							}
						}
					}
				} else if (action.equalsIgnoreCase(MyApplication.ACTION_CANCEL_UPDATE_LYRICS)) {
					cancel = true;
					mDownloadThreadPool.shutdown();
				}
			}
		}
		return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent arg0) {
        return null;
    }

	@Override
	public void onLyricsDownloaded(Lyrics lyrics) {
		if (!cancel) {
			count++;
			updateProgress();
			if (lyrics.getFlag() == Lyrics.POSITIVE_RESULT) {
				TrackRealmHelper.saveLyrics(lyrics);
				successCount++;
			}
		} else {
			String text = getResources()
					.getQuantityString(R.plurals.lyrics_dl_finished_desc, successCount, successCount);
			mBuilder.setContentText(text);
			mBuilder.setProgress(0, 0, false);
			mNotifyManager.notify(nID, mBuilder.build());
			mNotifyManager.cancel(nID);
		}
	}

	private void updateProgress() {
		if (count < total) {
			double progress = (100.0 * count / total);
			mProgress = (int) progress;
			mBuilder.setProgress(100, mProgress, false);
			mBuilder.setContentText(String.format(getString(R.string.lyrics_dl_progress), count, total));
			mNotifyManager.notify(nID, mBuilder.build());
		} else {
			String text = getResources()
					.getQuantityString(R.plurals.lyrics_dl_finished_desc, successCount, successCount);
			mBuilder.setContentText(text);
			mBuilder.setProgress(0, 0, false);
			mNotifyManager.notify(nID, mBuilder.build());
			mNotifyManager.cancel(nID);
		}
	}

	private PendingIntent getDeleteIntent() {
		Intent intent = new Intent(this, NotificationBroadcast.class);
		intent.setAction(MyApplication.NOTIFY_CANCEL_LYRICS_DOWNLOAD);
		return PendingIntent.getBroadcast(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
	}
}