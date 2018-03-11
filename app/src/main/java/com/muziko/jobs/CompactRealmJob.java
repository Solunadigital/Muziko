package com.muziko.jobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.muziko.manager.AppController;

import java.util.concurrent.TimeUnit;

/**
 * Created by dev on 26/09/2016.
 */

public class CompactRealmJob extends Job {

	public static final String TAG = "CompactRealmJob";

	public void scheduleJob() {
		int jobId = new JobRequest.Builder(TAG)
                .setPeriodic(TimeUnit.DAYS.toMillis(1), TimeUnit.HOURS.toMillis(2))
//                .setPersisted(true)
				.build()
				.schedule();
	}

	@Override
	@NonNull
	protected Result onRunJob(Params params) {

        AppController.Instance().CompactMuzikoDB();
        return Result.SUCCESS;
    }

}