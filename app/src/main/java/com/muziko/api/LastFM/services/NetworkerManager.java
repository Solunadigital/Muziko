package com.muziko.api.LastFM.services;

import android.content.Context;

import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.ScrobblesDatabase;
import com.muziko.api.LastFM.Utils.Track;

import java.util.EnumMap;
import java.util.Map;

/**
 * Created by dev on 28/08/2016.
 */
public class NetworkerManager {

	@SuppressWarnings("unused")
	private static final String TAG = "SLSNetManager";

	private final AppSettings settings;

	private Map<NetApp, Networker> mSupportedNetApps;

	public NetworkerManager(Context ctx, ScrobblesDatabase db) {
		this.settings = new AppSettings(ctx);
		mSupportedNetApps = new EnumMap<>(NetApp.class);
		for (NetApp napp : NetApp.values())
			mSupportedNetApps.put(napp, new Networker(napp, ctx, db));
	}

	public void launchAuthenticator(NetApp napp) {
		mSupportedNetApps.get(napp).launchAuthenticator();
	}

	public void launchClearCreds(NetApp napp) {
		mSupportedNetApps.get(napp).launchClearCreds();
	}

	private void launchHandshaker(NetApp napp) {
		mSupportedNetApps.get(napp).launchHandshaker();
	}

	public void launchHandshakers() {
		for (NetApp napp : NetApp.values()) {
			launchHandshaker(napp);
		}
	}

	public void launchClearAllCreds() {
		for (Networker nw : mSupportedNetApps.values())
			nw.launchClearCreds();
	}

	public void launchNPNotifier(Track track) {
		for (NetApp napp : NetApp.values()) {
			if (settings.isAuthenticated(napp)) {
				mSupportedNetApps.get(napp).launchNPNotifier(track);
			}
		}
	}

	public void launchScrobbler(NetApp napp) {
		if (settings.isAuthenticated(napp)) {
			mSupportedNetApps.get(napp).launchScrobbler();
		}
	}

	public void launchAllScrobblers() {
		for (NetApp napp : NetApp.values()) {
			launchScrobbler(napp);
		}
	}

	public void launchHeartTrack(Track track, NetApp napp) {
		mSupportedNetApps.get(napp).launchHeartTrack(track);
	}

	public void launchGetUserInfo(NetApp napp) {
		mSupportedNetApps.get(napp).launchUserInfo();
	}

}
