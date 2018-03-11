package com.muziko.jobs;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.muziko.MyApplication;
import com.muziko.manager.SettingsManager;
import com.muziko.service.LyricsDownloaderService;

import java.util.concurrent.TimeUnit;


public class LyricsJob extends Job {

	public static final String TAG = "LyricsJob";

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
		getLyrics();
		return Result.SUCCESS;
	}

	private void getLyrics() {
		boolean prefLyricsDownload = SettingsManager.Instance().getPrefs().getBoolean("prefLyricsDownload", false);
		if (prefLyricsDownload) {

			Intent intent = new Intent(getContext(), LyricsDownloaderService.class);
			intent.setAction(MyApplication.ACTION_UPDATE_LYRICS);
			getContext().startService(intent);
		}
	}
}