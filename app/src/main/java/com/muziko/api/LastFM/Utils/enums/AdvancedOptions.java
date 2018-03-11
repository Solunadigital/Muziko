package com.muziko.api.LastFM.Utils.enums;

import android.content.Context;
import android.util.Log;

import com.muziko.R;

import java.util.HashMap;
import java.util.Map;

public enum AdvancedOptions {
	// the values below for SAME will be ignored
	SAME_AS_BATTERY(
			"ao_same_as_battery", true, true, true, AdvancedOptionsWhen.AFTER_1, true, NetworkOptions.ANY, true,
			R.string.advanced_options_type_same_as_battery_name),
	STANDARD(
			"ao_standard", true, true, true, AdvancedOptionsWhen.AFTER_1, true, NetworkOptions.ANY, true,
			R.string.advanced_options_type_standard_name),
	// not available for plugged in
	BATTERY_SAVING(
			"ao_battery", true, true, false, AdvancedOptionsWhen.AFTER_10, true, NetworkOptions.ANY, false,
			R.string.advanced_options_type_battery_name),
	// the values below for CUSTOM will be ignored
	CUSTOM(
			"ao_custom", true, true, true, AdvancedOptionsWhen.AFTER_1, true, NetworkOptions.ANY, true,
			R.string.advanced_options_type_custom_name);

	private static final String TAG = "SLSAdvancedOptions";
	private static Map<String, AdvancedOptions> mSAOMap;

	static {
		AdvancedOptions[] aos = AdvancedOptions.values();
		mSAOMap = new HashMap<>(aos.length);
		for (AdvancedOptions ao : aos)
			mSAOMap.put(ao.getSettingsVal(), ao);
	}

	private final String settingsVal;
	private final boolean enableNotify;
	private final boolean enableScrobbling;
	private final boolean enableNp;
	private final AdvancedOptionsWhen when;
	private final boolean alsoOnComplete;
	private final NetworkOptions networkOptions;

	// these methods are intentionally package-private, they are only used
	// by AppSettings
	private final boolean roaming;
	private final int nameRID;

	AdvancedOptions(String settingsVal, boolean enableNotify, boolean enableScrobbling, boolean enableNp, AdvancedOptionsWhen when,
	                boolean alsoOnComplete, NetworkOptions networkOptions, boolean roaming, int nameRID) {
		this.settingsVal = settingsVal;
		this.enableNotify = enableNotify;
		this.enableScrobbling = enableScrobbling;
		this.enableNp = enableNp;
		this.when = when;
		this.alsoOnComplete = alsoOnComplete;
		this.networkOptions = networkOptions;
		this.roaming = roaming;
		this.nameRID = nameRID;
	}

	public static AdvancedOptions fromSettingsVal(String s) {
		AdvancedOptions ao = mSAOMap.get(s);
		if (ao == null) {
			Log.e(TAG, "got null advanced option from settings, defaulting to standard");
			ao = AdvancedOptions.STANDARD;
		}
		return ao;
	}

	public String getSettingsVal() {
		return settingsVal;
	}

	public boolean isNotifyEnabled() {
		return enableNotify;
	}

	public boolean isScrobblingEnabled() {
		return enableScrobbling;
	}

	public boolean isNpEnabled() {
		return enableNp;
	}

	public AdvancedOptionsWhen getWhen() {
		return when;
	}

	public boolean getAlsoOnComplete() {
		return alsoOnComplete;
	}

	public NetworkOptions getNetworkOptions() {
		return networkOptions;
	}

	public boolean getRoaming() {
		return roaming;
	}

	public String getName(Context ctx) {
		return ctx.getString(nameRID);
	}
}