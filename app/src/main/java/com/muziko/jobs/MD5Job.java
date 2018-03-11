package com.muziko.jobs;

import android.support.annotation.NonNull;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.muziko.manager.AppController;

import java.util.concurrent.TimeUnit;

/**
 * Created by dev on 26/09/2016.
 */

public class MD5Job extends Job {

    public static final String TAG = "MD5Job";

    public void scheduleJob() {
        int jobId = new JobRequest.Builder(TAG)
                .setPeriodic(TimeUnit.MINUTES.toMillis(15), TimeUnit.MINUTES.toMillis(5))
                .setRequiresDeviceIdle(false)
                .setRequirementsEnforced(true)
//                .setPersisted(true)
                .build()
                .schedule();
    }

    @Override
    @NonNull
    protected Result onRunJob(Params params) {
        updateMissingHashes();
        return Result.SUCCESS;
    }

    private void updateMissingHashes() {

        AppController.Instance().startMd5Updater();
    }
}