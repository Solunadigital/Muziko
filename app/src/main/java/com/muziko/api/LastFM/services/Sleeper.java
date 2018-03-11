package com.muziko.api.LastFM.services;

import android.content.Context;
import android.util.Log;

public class Sleeper extends NetRunnable {

	private static final String TAG = "Sleeper";

	// TODO: correct value
	private static final long START_TIME = 60 * 1000; // 1 min
	private static final long MAX_TIME = 10 * 60 * 1000; // 10 min 1,2,3...10 5*11 55min

	private long mSleepTime;

	public Sleeper(NetApp napp, Context ctx, Networker net) {
		super(napp, ctx, net);
		reset();
	}

	public void reset() {
		synchronized (this) {
			mSleepTime = START_TIME;
			this.notifyAll(); // if we were waiting, which we probably wasn't
		}
	}

	private void incSleepTime() {
		synchronized (this) {
			mSleepTime += 60 * 1000;  // TODO set 60 * 1000
			if (mSleepTime > MAX_TIME) {
				mSleepTime = MAX_TIME;
			}
		}
	}

	@Override
	public void run() {
		synchronized (this) {
			try {
				Log.d(TAG, "start sleeping: " + mSleepTime + ": "
						+ getNetApp().getName());
				this.wait(mSleepTime);
				Log.d(TAG, "woke up sleeping: " + getNetApp().getName());
			} catch (InterruptedException e) {
				Log.i(TAG, "Got interrupted: " + getNetApp().getName());
				Log.i(TAG, e.getMessage());
			}
			incSleepTime();
		}
	}

}
