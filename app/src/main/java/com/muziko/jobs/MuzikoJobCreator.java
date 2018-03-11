package com.muziko.jobs;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

/**
 * Created by dev on 26/09/2016.
 */


public class MuzikoJobCreator implements JobCreator {

	@Override
	public Job create(String tag) {

		switch (tag) {
			case CoverArtJob.TAG:
				return new CoverArtJob();
			case LyricsJob.TAG:
				return new LyricsJob();
			case CompactRealmJob.TAG:
				return new CompactRealmJob();
			default:
				return null;
		}
	}
}