package com.muziko.api.LastFM.services;

import android.util.SparseArray;

import com.muziko.api.LastFM.Utils.AppSettings;

public enum NetApp {
	LASTFM(
			0x01, "Last.fm", "http://post.audioscrobbler.com/?hs=true", "",
			"https://www.last.fm/join", "https://www.last.fm/user/%1"), //
	LIBREFM(
			0x02, "Libre.fm", "http://turtle.libre.fm/?hs=true", "librefm",
			"https://libre.fm/", "https://libre.fm/user/%1");

	private static SparseArray<NetApp> mValNetAppMap;

	static {

		mValNetAppMap = new SparseArray<>();
		for (NetApp napp : NetApp.values()) {
			mValNetAppMap.put(napp.getValue(), napp);
		}
	}

	private final int val;
	private final String name;
	private final String handshakeUrl;
	private final String settingsPrefix;
	private final String signUpUrl;
	private final String profileUrl;

	NetApp(int val, String name, String handshakeUrl,
	       String settingsPrefix, String signUpUrl, String profileUrl) {
		this.val = val;
		this.name = name;
		this.handshakeUrl = handshakeUrl;
		this.settingsPrefix = settingsPrefix;
		this.signUpUrl = signUpUrl;
		this.profileUrl = profileUrl;
	}

	public static NetApp fromValue(int value) {
		NetApp napp = mValNetAppMap.get(value);
		if (napp == null) {
			throw new IllegalArgumentException("Got null NetApp in fromValue: "
					+ value);
		}
		return napp;
	}

	public String getIntentExtraValue() {
		return toString();
	}

	public int getValue() {
		return this.val;
	}

	public String getName() {
		return this.name;
	}

	public String getHandshakeUrl() {
		return this.handshakeUrl;
	}

	public String getSettingsPrefix() {
		return settingsPrefix;
	}

	public String getProfileUrl(AppSettings settings) {
		return profileUrl.replaceAll("%1", settings.getUsername(this));
	}

	public String getSignUpUrl() {
		return signUpUrl;
	}

}

