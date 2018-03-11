package com.muziko.api.LastFM.services;

import android.content.Context;

import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.ScrobblesDatabase;
import com.muziko.api.LastFM.Utils.Track;

import java.util.Iterator;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Networker {
	@SuppressWarnings("unused")
	private static final String TAG = "Networker";

	private final AppSettings settings;

	private final NetApp mNetApp;

	private final Context mCtx;
	private final ScrobblesDatabase mDb;

	private final ThreadPoolExecutor mExecutor;

	private final NetRunnableComparator mComparator;

	private final NetworkWaiter mNetworkWaiter;
	private final Sleeper mSleeper;

	private Handshaker.HandshakeResult hInfo;

	public Networker(NetApp napp, Context ctx, ScrobblesDatabase db) {
		settings = new AppSettings(ctx);

		mNetApp = napp;
		mCtx = ctx;
		mDb = db;

		mComparator = new NetRunnableComparator();

		// TODO: what should the keepAliveTime/unit be?
		mExecutor = new ThreadPoolExecutor(1, 1, 2, TimeUnit.SECONDS,
				new PriorityBlockingQueue<>(1, mComparator));

		mSleeper = new Sleeper(mNetApp, ctx, this);
		mNetworkWaiter = new NetworkWaiter(mNetApp, ctx, this);
		hInfo = null;
	}

	public void launchAuthenticator() {
		launchHandshaker(Handshaker.HandshakeAction.AUTH);
	}

	public void launchClearCreds() {
		settings.clearCreds(mNetApp);

		mDb.deleteAllScrobbles(mNetApp);
		mDb.cleanUpTracks();

		launchHandshaker(Handshaker.HandshakeAction.CLEAR_CREDS);
	}

	public void launchHandshaker() {
		launchHandshaker(Handshaker.HandshakeAction.HANDSHAKE);
	}

	public void launchHandshaker(Handshaker.HandshakeAction hsAction) {
		Handshaker h = new Handshaker(mNetApp, mCtx, this, hsAction);
		mExecutor.execute(h);
	}

	public void launchScrobbler() {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == Scrobbler.class) {
				i.remove();
			}
		}

		Scrobbler s = new Scrobbler(mNetApp, mCtx, this, mDb);
		mExecutor.execute(s);
	}

	public void launchNPNotifier(Track track) {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == NPNotifier.class) {
				i.remove();
			}
		}

		NPNotifier n = new NPNotifier(mNetApp, mCtx, this, track);
		mExecutor.execute(n);
	}

	public void launchHeartTrack(Track track) {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == Heart.class) {
				i.remove();
			}
		}

		Heart n = new Heart(mNetApp, mCtx, this, track, settings);
		mExecutor.execute(n);
	}

	public void launchUserInfo() {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == UserInfo.class) {
				i.remove();
			}
		}

		UserInfo n = new UserInfo(mNetApp, mCtx, this, settings);
		mExecutor.execute(n);
	}

	public void launchSleeper() {
		mExecutor.execute(mSleeper);
	}

	public void resetSleeper() {
		mSleeper.reset();
	}

	public void launchNetworkWaiter() {
		mExecutor.execute(mNetworkWaiter);
	}

	public void unlaunchScrobblingAndNPNotifying() {
		Iterator<Runnable> i = mExecutor.getQueue().iterator();
		while (i.hasNext()) {
			Runnable r = i.next();
			if (r.getClass() == Scrobbler.class
					|| r.getClass() == NPNotifier.class) {
				i.remove();
			}
		}
	}

	public Handshaker.HandshakeResult getHandshakeResult() {
		return hInfo;
	}

	public void setHandshakeResult(Handshaker.HandshakeResult h) {
		hInfo = h;
	}

}
