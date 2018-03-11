package com.muziko.api.LastFM.services;

import android.content.Context;
import android.util.Log;

import com.muziko.R;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.Track;
import com.muziko.api.LastFM.Utils.Util;
import com.muziko.api.LastFM.Utils.enums.SubmissionType;


public abstract class AbstractSubmitter extends NetRunnable {

	private static final String TAG = "ASubmitter";

	private final AppSettings settings;

	AbstractSubmitter(NetApp napp, Context ctx, Networker net) {
		super(napp, ctx, net);
		this.settings = new AppSettings(ctx);
	}

	@Override
	public final void run() {

		// check network status
		Util.NetworkStatus ns = Util.checkForOkNetwork(getContext());
		if (ns != Util.NetworkStatus.OK) {
			Log.d(TAG, "Waits on network, network-status: " + ns);
			getNetworker().launchNetworkWaiter();
			relaunchThis();
			return;
		}

		Handshaker.HandshakeResult hInfo = getNetworker().getHandshakeResult();
		if (hInfo == null) {
			getNetworker().launchHandshaker();
			relaunchThis();
		} else {
			int rCount = 0;
			boolean retry;
			do {
				retry = !doRun(hInfo);
				rCount++;
			} while (retry && rCount < 3);

			if (rCount >= 3) {
				getNetworker().launchHandshaker();
				relaunchThis();
			}
		}
	}

	void notifySubmissionStatusSuccessful(SubmissionType stype,
	                                      Track track, int statsInc) {
		settings.setLastSubmissionSuccess(getNetApp(), stype, true);
		settings.setLastSubmissionTime(getNetApp(), stype, Util
				.currentTimeMillisLocal());
		settings.setNumberOfSubmissions(getNetApp(), stype, settings
				.getNumberOfSubmissions(getNetApp(), stype)
				+ statsInc);
		settings
				.setLastSubmissionInfo(getNetApp(), stype, "\""
						+ track.getTrack() + "\" "
						+ getContext().getString(R.string.by) + " "
						+ track.getArtist());
		notifyStatusUpdate();
	}

	void notifySubmissionStatusFailure(SubmissionType stype,
	                                   String reason) {
		settings.setLastSubmissionSuccess(getNetApp(), stype, false);
		settings.setLastSubmissionTime(getNetApp(), stype, Util
				.currentTimeMillisLocal());
		settings.setLastSubmissionInfo(getNetApp(), stype, reason);
		notifyStatusUpdate();
	}

	/**
	 * @param hInfo struct with urls and stuff
	 * @return true if successful, false otherwise
	 */
	protected abstract boolean doRun(Handshaker.HandshakeResult hInfo);

	protected abstract void relaunchThis();
}
