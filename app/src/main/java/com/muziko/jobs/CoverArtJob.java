package com.muziko.jobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.muziko.manager.SettingsManager;
import com.muziko.manager.ThreadManager;
import com.muziko.tasks.CoverArtDownloader;

import java.util.concurrent.TimeUnit;

import static com.muziko.MyApplication.coverArtDownloaders;

/**
 * Created by dev on 26/09/2016.
 */

public class CoverArtJob extends Job {

	public static final String TAG = "AlbumArtJob";

	public void scheduleJob() {
		int jobId = new JobRequest.Builder(TAG)
				.setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(2))
				.setRequiresDeviceIdle(true)
				.setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
				.setRequirementsEnforced(true)
//				.setPersisted(true)
				.build()
				.schedule();
	}

	@Override
	@NonNull
	protected Result onRunJob(Params params) {
		getAlbumArt();
		return Result.SUCCESS;
	}

	private void getAlbumArt() {

		boolean prefArtworkDownload = SettingsManager.Instance().getPrefs().getBoolean("prefArtworkDownload", false);
		if (prefArtworkDownload) {

			CoverArtDownloader coverArtDownloader = new CoverArtDownloader(getContext());
			coverArtDownloaders.add(coverArtDownloader);
            coverArtDownloader.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }
	}
}