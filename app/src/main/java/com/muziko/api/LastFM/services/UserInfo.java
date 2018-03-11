package com.muziko.api.LastFM.services;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.AuthStatus;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;

public class UserInfo extends NetRunnable {

	private static final String TAG = "UserInfo";

	private AppSettings settings;
	private Context mCtx;


	public UserInfo(NetApp napp, Context ctx, Networker net, AppSettings settings) {
		super(napp, ctx, net);
		this.settings = settings;
		this.mCtx = ctx;
	}

	public final void run() {
		NetApp netApp = getNetApp();
		String netAppName = netApp.getName();

		try {
			String response = getAllTimeScrobbles();

			JSONObject jObject = new JSONObject(response);
			if (jObject.has("user")) {
				settings.setTotalScrobbles(netApp, jObject.getJSONObject("user").getString("playcount"));
				Log.i(TAG, "Get user info success: " + netAppName);
			} else if (jObject.has("error")) {
				int code = jObject.getInt("error");
				if (code == 26 || code == 10) {
					Log.e(TAG, "Get user info failed: client banned: " + netAppName);
					settings.setSessionKey(netApp, "");
					throw new AuthStatus.ClientBannedException("Now Playing failed because of client banned");
				} else if (code == 9) {
					Log.i(TAG, "Get user info: bad auth: " + netAppName);
					settings.setSessionKey(netApp, "");
					throw new AuthStatus.BadSessionException("Now Playing failed because of badsession");
				} else {
					Log.e(TAG, "Get user info fails: FAILED " + response + ": " + netAppName);
					//settings.setSessionKey(netApp, "");
					throw new AuthStatus.TemporaryFailureException("Now playing failed because of " + response);
				}

			} else {
				Log.d(TAG, "Failed to get user info.");
			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}
	}

	private String getAllTimeScrobbles() throws IOException, NullPointerException {
		URL url;
		HttpURLConnection conn = null;
		try {
			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && getNetApp() == NetApp.LIBREFM) {
				url = new URL("http://libre.fm/2.0/");
			} else if (getNetApp() == NetApp.LASTFM) {
				url = new URL("https://ws.audioscrobbler.com/2.0/");
			} else {
				url = new URL("https://libre.fm/2.0/");
			}
			conn = (HttpURLConnection) url.openConnection();

			// set Timeout and method
			conn.setReadTimeout(7000);
			conn.setConnectTimeout(7000);
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

			conn.setDoInput(true);
			conn.setDoOutput(true);

			Map<String, Object> params = new LinkedHashMap<>();
			params.put("method", "user.getInfo");
			params.put("user", settings.getUsername(getNetApp()));
			params.put("api_key", settings.rcnvK(settings.getAPIkey()));
			params.put("format", "json");

			StringBuilder postData = new StringBuilder();
			for (Map.Entry<String, Object> param : params.entrySet()) {
				if (postData.length() != 0) postData.append('&');
				postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
				postData.append('=');
				postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
			}
			byte[] postDataBytes = postData.toString().getBytes("UTF-8");
			conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));

			conn.getOutputStream().write(postDataBytes);
			//Log.i(TAG, params.toString());

			conn.connect();

			int resCode = conn.getResponseCode();
			Log.d(TAG, "Response code: " + this.getNetApp().getName() + ": " + resCode);
			BufferedReader r;
			if (resCode == -1) {
				return "";
			} else if (resCode == 200) {
				r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			} else {
				r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
			}
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				stringBuilder.append(line).append('\n');
			}
			String response = stringBuilder.toString();
			Log.d(TAG, response);
			return response;
		} catch (IOException | NullPointerException e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return "";
	}
}

