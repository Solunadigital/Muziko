package com.muziko.api.LastFM.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.crashlytics.android.Crashlytics;
import com.muziko.api.LastFM.Utils.enums.AdvancedOptions;
import com.muziko.api.LastFM.Utils.enums.AdvancedOptionsWhen;
import com.muziko.api.LastFM.Utils.enums.NetworkOptions;
import com.muziko.api.LastFM.Utils.enums.PowerOptions;
import com.muziko.api.LastFM.Utils.enums.SortField;
import com.muziko.api.LastFM.Utils.enums.SubmissionType;
import com.muziko.api.LastFM.services.NetApp;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESKeySpec;

/**
 * Created by dev on 28/08/2016.
 */
public class AppSettings {

	public static final String ACTION_NETWORK_OPTIONS_CHANGED = "com.adam.aslfms.service.bcast.onnetoptions";
	private static final String TAG = "SLSAppSettings";
	private static final String SETTINGS_NAME = "settings";

	private static final String KEY_USERNAME = "username";
	private static final String KEY_PASSWORD = "password";
	private static final String KEY_PWDMD5 = "pwdMd5";
	private static final String KEY_SESSION = "sessionKey";
	private static final String KEY_SCROBBLES = "totalScrobbles";

	private static final String KEY_NOTIFY_ENABLE = "enable_notify";
	private static final String KEY_SCROBBLING_ENABLE = "enable_scrobbling";
	private static final String KEY_NOWPLAYING_ENABLE = "enable_nowplaying";

	private static final String KEY_AUTH_STATUS = "authstatus";

	private static final String KEY_WHATSNEW_VIEWED_VERSION = "whatsnew_viewed_version";

	private static final String KEY_VIEW_CACHE_SORTFIELD = "view_cache_sortfield";

	private static final String KEY_SCROBBLE_POINT = "scrobble_point";
	private static final String KEY_ADVANCED_OPTIONS = "advanced_options_type";
	private static final String KEY_ADVANCED_OPTIONS_WHEN = "advanced_options_when";
	private static final String KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE = "scrobbling_options_also_on_complete";
	private static final String KEY_ADVANCED_OPTIONS_NETWORK = "advanced_options_network";
	private static final String KEY_ADVANCED_OPTIONS_ROAMING = "advanced_options_roaming";

	// Widget stuff
	private static final String KEY_WIDGET_ALSO_DISABLE_NP = "widget_also_disable_np";

	private final Context mCtx;
	private final SharedPreferences prefs;

	public AppSettings(Context ctx) {
		super();
		mCtx = ctx;
		prefs = ctx.getSharedPreferences(SETTINGS_NAME, 0);
	}

	public void clearCreds(NetApp napp) {
		setUsername(napp, "");
		setPassword(napp, "");
		setPwdMd5(napp, "");
		setSessionKey(napp, "");
		setAuthStatus(napp, AuthStatus.AUTHSTATUS_NOAUTH);
	}

	public boolean hasCreds(NetApp napp) {
		return getAuthStatus(napp) != AuthStatus.AUTHSTATUS_NOAUTH
				|| getUsername(napp).length() != 0
				|| getPassword(napp).length() != 0
				|| getPwdMd5(napp).length() != 0
				|| getSessionKey(napp).length() != 0;
	}

	public boolean hasAnyCreds() {
		for (NetApp napp : NetApp.values())
			if (hasCreds(napp))
				return true;
		return false;
	}

	public void setUsername(NetApp napp, String s) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_USERNAME, s);
		e.apply();
	}

	public String getUsername(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_USERNAME, "");
	}

	/**
	 * Saves the password in plain-text for a user account at the {@link NetApp}
	 * {@code napp}. This is only used as an intermediary step, and is removed
	 * when the authentication is successful in {@link Handshaker#run()}
	 *
	 * @param napp the {@code NetApp} for which a user account has this password
	 * @param s    the password, in plain-text
	 * @see #setPwdMd5(NetApp)
	 */
	public void setPassword(NetApp napp, String s) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_PASSWORD, s);
		e.apply();
	}

	/**
	 * Returns the password in plain-text for a user account at the
	 * {@link NetApp} {@code napp}. This is only used as an intermediary step,
	 * and is removed when the authentication is successful in
	 * {@link Handshaker#run()}
	 *
	 * @param napp the {@code NetApp} for which a user account has this password
	 * @return the password, in plain-text
	 * @see #getPwdMd5(NetApp)
	 */
	public String getPassword(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_PASSWORD, "");
	}

	/**
	 * Saves sessionKey in plain text
	 *
	 * @param napp
	 * @param s
	 */
	public void setSessionKey(NetApp napp, String s) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_SESSION, s);
		e.apply();
	}

	/**
	 * Returns sessionKey in plain text
	 *
	 * @param napp
	 * @return
	 */
	public String getSessionKey(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_SESSION, "");
	}

	public void setTotalScrobbles(NetApp napp, String s) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_SCROBBLES, s);
		e.apply();
	}

	public String getTotalScrobbles(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_SCROBBLES, "");
	}

	public String getAPIkey() {
		return "EoyDuAbyW8RGZ8exvi+J205QfXC8qy3eoEhgqrjbU9krAbk7zsobQQ==";
	}

	public String getSecret() {
		return "u5w3f1fzTIEshjiQdLJ8jqzSEtLIeCkwItIWFVien1srAbk7zsobQQ==";
	}

	private String getSecret2() {
		return MD5.getHashString("unComplicatedHash345623@%^#@^$0");
	}

	/**
	 * Saves an MD5 hash of the password for a user account at the
	 * {@link NetApp} {@code napp}. This is stored in the settings file until it
	 * is cleared by the user through {@link UserCredActivity}. It is "safe" to
	 * store a password this way, as it would take a very, very long time to
	 * extract the original password from the MD5 hash.
	 *
	 * @param napp the {@code NetApp} for which a user account has this password
	 * @param s    the password, as an MD5 hash
	 */
	public void setPwdMd5(NetApp napp, String s) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + KEY_PWDMD5, s);
		e.apply();
	}

	/**
	 * Returns the password as an MD5 hash for a user account at the
	 * {@link NetApp} {@code napp}.
	 *
	 * @param napp the {@code NetApp} for which a user account has this password
	 * @return the password, as an MD5 hash
	 * @see #getPwdMd5(NetApp)
	 */
	public String getPwdMd5(NetApp napp) {
		return prefs.getString(napp.getSettingsPrefix() + KEY_PWDMD5, "");
	}

	public void setAuthStatus(NetApp napp, int i) {
		SharedPreferences.Editor e = prefs.edit();
		e.putInt(napp.getSettingsPrefix() + KEY_AUTH_STATUS, i);
		e.apply();
	}

	public int getAuthStatus(NetApp napp) {
		return prefs.getInt(napp.getSettingsPrefix() + KEY_AUTH_STATUS,
				AuthStatus.AUTHSTATUS_NOAUTH);
	}

	public boolean isAnyAuthenticated() {
		for (NetApp napp : NetApp.values())
			if (isAuthenticated(napp))
				return true;
		return false;
	}

	public boolean isAuthenticated(NetApp napp) {
		return getAuthStatus(napp) == AuthStatus.AUTHSTATUS_OK;
	}

	public int getWhatsNewViewedVersion() {
		return prefs.getInt(KEY_WHATSNEW_VIEWED_VERSION, 0);
	}

	public void setWhatsNewViewedVersion(int i) {
		SharedPreferences.Editor e = prefs.edit();
		e.putInt(KEY_WHATSNEW_VIEWED_VERSION, i);
		e.apply();
	}

	// status stuff

	public void clearSubmissionStats(NetApp napp) {
		for (SubmissionType st : SubmissionType.values()) {
			setLastSubmissionTime(napp, st, -1);
			setLastSubmissionSuccess(napp, st, true);
			setLastSubmissionInfo(napp, st, "");
			setNumberOfSubmissions(napp, st, 0);
		}
	}

	// submission notifying
	public void setLastSubmissionTime(NetApp napp, SubmissionType stype,
	                                  long time) {
		SharedPreferences.Editor e = prefs.edit();
		e.putLong(napp.getSettingsPrefix() + stype.getLastPrefix() + "_time",
				time);
		e.apply();
	}

	public long getLastSubmissionTime(NetApp napp, SubmissionType stype) {
		return prefs.getLong(napp.getSettingsPrefix() + stype.getLastPrefix()
				+ "_time", -1);
	}

	public void setLastSubmissionSuccess(NetApp napp, SubmissionType stype,
	                                     boolean b) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(napp.getSettingsPrefix() + stype.getLastPrefix()
				+ "_success", b);
		e.apply();
	}

	public boolean wasLastSubmissionSuccessful(NetApp napp, SubmissionType stype) {
		return prefs.getBoolean(napp.getSettingsPrefix()
				+ stype.getLastPrefix() + "_success", true);
	}

	public void setLastSubmissionInfo(NetApp napp, SubmissionType stype,
	                                  String s) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(napp.getSettingsPrefix() + stype.getLastPrefix() + "_info",
				s);
		e.apply();
	}

	public String getLastSubmissionInfo(NetApp napp, SubmissionType stype) {
		return prefs.getString(napp.getSettingsPrefix() + stype.getLastPrefix()
				+ "_info", "");
	}

	// number of submissions (scrobbles/nps)
	public void setNumberOfSubmissions(NetApp napp, SubmissionType stype, int i) {
		SharedPreferences.Editor e = prefs.edit();
		e.putInt(napp.getSettingsPrefix() + stype.getNumberOfPrefix(), i);
		e.apply();
	}

	public int getNumberOfSubmissions(NetApp napp, SubmissionType stype) {
		return prefs.getInt(napp.getSettingsPrefix()
				+ stype.getNumberOfPrefix(), 0);
	}

	public SortField getCacheSortField() {
		String s = prefs.getString(KEY_VIEW_CACHE_SORTFIELD,
				SortField.WHEN_DESC.name());

		SortField sf = SortField.WHEN_DESC;
		try {
			sf = SortField.valueOf(s);
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}

		return sf;
	}

	// view cache options
	public void setCacheSortField(SortField sf) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(KEY_VIEW_CACHE_SORTFIELD, sf.name());
		e.apply();
	}

	// scrobbling options
	public boolean isSubmissionsEnabled(SubmissionType stype, PowerOptions pow) {
		if (stype == SubmissionType.SCROBBLE) {
			return isScrobblingEnabled(pow);
		} else {
			return isNowPlayingEnabled(pow);
		}
	}

	private void setNotifyEnabled(PowerOptions pow, boolean b) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(KEY_NOTIFY_ENABLE + pow.getSettingsPath(), b);
		e.apply();
	}

	public boolean isNotifyEnabled(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_NOTIFY_ENABLE + pow.getSettingsPath(),
				getAdvancedOptions(pow).isNotifyEnabled());
	}

	private void setScrobblingEnabled(PowerOptions pow, boolean b) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(KEY_SCROBBLING_ENABLE + pow.getSettingsPath(), b);
		e.apply();
	}

	public boolean isScrobblingEnabled(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_SCROBBLING_ENABLE + pow.getSettingsPath(),
				getAdvancedOptions(pow).isScrobblingEnabled());
	}

	private void setNowPlayingEnabled(PowerOptions pow, boolean b) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(KEY_NOWPLAYING_ENABLE + pow.getSettingsPath(), b);
		e.apply();
	}

	public boolean isNowPlayingEnabled(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_NOWPLAYING_ENABLE + pow.getSettingsPath(),
				getAdvancedOptions(pow).isNpEnabled());
	}

	public int getScrobblePoint() {
		return prefs.getInt(KEY_SCROBBLE_POINT, 50);
	}

	public void setScrobblePoint(int sp) {
		SharedPreferences.Editor e = prefs.edit();
		e.putInt(KEY_SCROBBLE_POINT, sp);
		e.apply();
	}

	public void setAdvancedOptions(PowerOptions pow, AdvancedOptions ao) {
		boolean found = false;
		for (AdvancedOptions aof : pow.getApplicableOptions()) {
			if (aof == ao) {
				found = true;
				break;
			}
		}
		if (!found) {
			throw new IllegalArgumentException(
					"Bad option for this power setting: " + ao + ", " + pow);
		}

		SharedPreferences.Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS + pow.getSettingsPath(), ao
				.getSettingsVal());
		e.apply();
		if (ao != AdvancedOptions.CUSTOM
				&& ao != AdvancedOptions.SAME_AS_BATTERY) {
			setNotifyEnabled(pow, ao.isNotifyEnabled());
			setScrobblingEnabled(pow, ao.isScrobblingEnabled());
			setNowPlayingEnabled(pow, ao.isNpEnabled());
			setAdvancedOptionsWhen(pow, ao.getWhen());
			setAdvancedOptionsAlsoOnComplete(pow, ao.getAlsoOnComplete());
			setNetworkOptions(pow, ao.getNetworkOptions());
			setSubmitOnRoaming(pow, ao.getRoaming());
		}
	}

	/**
	 * Wow, I apologize for this mess. I'll clean it up when I figure out how.
	 *
	 * @param pow
	 * @return
	 */
	private AdvancedOptions getAdvancedOptions_raw(PowerOptions pow) {
		String s = prefs.getString(
				KEY_ADVANCED_OPTIONS + pow.getSettingsPath(), null);
		if (s == null) {
			if (pow == PowerOptions.PLUGGED_IN)
				return AdvancedOptions.SAME_AS_BATTERY;
			return AdvancedOptions.STANDARD;
		} else {
			return AdvancedOptions.fromSettingsVal(s);
		}
	}

	private AdvancedOptions getAdvancedOptions(PowerOptions pow) {
		AdvancedOptions ao = getAdvancedOptions_raw(pow);
		// if we have said that we don't want custom settings for plugged in
		if (pow == PowerOptions.PLUGGED_IN
				&& ao == AdvancedOptions.SAME_AS_BATTERY) {
			// return the advanced settings used for battery
			return getAdvancedOptions_raw(PowerOptions.BATTERY);
		}
		return ao;
	}

	private void setAdvancedOptionsWhen(PowerOptions pow, AdvancedOptionsWhen aow) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS_WHEN + pow.getSettingsPath(), aow
				.getSettingsVal());
		e.apply();
	}

	public AdvancedOptionsWhen getAdvancedOptionsWhen(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		String s = prefs.getString(KEY_ADVANCED_OPTIONS_WHEN
				+ pow.getSettingsPath(), null);
		if (s == null) {
			return getAdvancedOptions(pow).getWhen();
		} else {
			return AdvancedOptionsWhen.fromSettingsVal(s);
		}
	}

	private void setAdvancedOptionsAlsoOnComplete(PowerOptions pow, boolean b) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE
				+ pow.getSettingsPath(), b);
		e.apply();
	}

	public boolean getAdvancedOptionsAlsoOnComplete(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_ADVANCED_OPTIONS_ALSO_ON_COMPLETE
				+ pow.getSettingsPath(), getAdvancedOptions(pow)
				.getAlsoOnComplete());
	}

	private void setNetworkOptions(PowerOptions pow, NetworkOptions no) {
		SharedPreferences.Editor e = prefs.edit();
		e.putString(KEY_ADVANCED_OPTIONS_NETWORK + pow.getSettingsPath(), no
				.getSettingsVal());
		e.apply();

		mCtx.sendBroadcast(new Intent(ACTION_NETWORK_OPTIONS_CHANGED));
	}

	public NetworkOptions getNetworkOptions(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		String s = prefs.getString(KEY_ADVANCED_OPTIONS_NETWORK
				+ pow.getSettingsPath(), null);
		if (s == null) {
			return NetworkOptions.ANY;
		} else {
			return NetworkOptions.fromSettingsVal(s);
		}
	}

	private void setSubmitOnRoaming(PowerOptions pow, boolean b) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(KEY_ADVANCED_OPTIONS_ROAMING + pow.getSettingsPath(), b);
		e.apply();

		mCtx.sendBroadcast(new Intent(ACTION_NETWORK_OPTIONS_CHANGED));
	}

	public boolean getSubmitOnRoaming(PowerOptions pow) {
		if (pow == PowerOptions.PLUGGED_IN
				&& getAdvancedOptions_raw(PowerOptions.PLUGGED_IN) == AdvancedOptions.SAME_AS_BATTERY) {
			pow = PowerOptions.BATTERY;
		}

		return prefs.getBoolean(KEY_ADVANCED_OPTIONS_ROAMING
				+ pow.getSettingsPath(), getAdvancedOptions(pow).getRoaming());
	}

	public boolean getWidgetAlsoDisableNP() {
		return prefs.getBoolean(KEY_WIDGET_ALSO_DISABLE_NP, false);
	}

	// Widget stuff
	public void setWidgetAlsoDisableNP(boolean b) {
		SharedPreferences.Editor e = prefs.edit();
		e.putBoolean(KEY_WIDGET_ALSO_DISABLE_NP, b);
		e.apply();
	}

	private SecretKey getSecKey() {
		try {
			DESKeySpec keySpec = new DESKeySpec(getSecret2().getBytes("UTF8"));
			SecretKeyFactory keyFactory = SecretKeyFactory.getInstance("DES");
			SecretKey key = keyFactory.generateSecret(keySpec);
			return key;
		} catch (Exception ex) {
			Crashlytics.logException(ex);
			return null;
		}
	}

	public String cnvK(String inStr) {
		try {
			byte[] cleartext = inStr.getBytes("UTF8");
			Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
			cipher.init(Cipher.ENCRYPT_MODE, getSecKey());
			String encryptedPwd = myBase64.encodeToString(cipher.doFinal(cleartext), myBase64.DEFAULT);
			return encryptedPwd;
		} catch (Exception ex) {
			Crashlytics.logException(ex);
			return "";
		}
	}

	public String rcnvK(String inStr) {
		try {
			byte[] encrypedPwdBytes = myBase64.decode(inStr, myBase64.DEFAULT);
			Cipher cipher = Cipher.getInstance("DES"); // cipher is not thread safe
			cipher.init(Cipher.DECRYPT_MODE, getSecKey());
			byte[] plainTextPwdBytes = (cipher.doFinal(encrypedPwdBytes));
			String outPut = new String(plainTextPwdBytes);
			return outPut;
		} catch (Exception e) {
			e.getStackTrace();
			return "";
		}
	}
}
