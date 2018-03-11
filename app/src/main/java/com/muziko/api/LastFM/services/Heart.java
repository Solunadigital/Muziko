package com.muziko.api.LastFM.services;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.MD5;
import com.muziko.api.LastFM.Utils.Track;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Created by Debugs on 7/9/2016.
 *
 * @author Debugs
 * @since 1.5.0
 */
public class Heart extends NetRunnable {

	private static final String TAG = "Heart";

	private Track hearTrack;
	private AppSettings settings;
	private Context mCtx;


	public Heart(NetApp napp, Context ctx, Networker net, Track hearTrack, AppSettings settings) {
		super(napp, ctx, net);
		this.hearTrack = hearTrack;
		this.settings = settings;
		this.mCtx = ctx;
	}

	public final void run() {

// can't heart track

		String sigText = "api_key"
				+ settings.rcnvK(settings.getAPIkey())
				+ "artist" + hearTrack.getArtist()
				+ "methodtrack.lovesk"
				+ settings.getSessionKey(getNetApp())
				+ "track" + hearTrack.getTrack()
				+ settings.rcnvK(settings.getSecret());

		String signature = MD5.getHashString(sigText);

		try {
			String response = postHeartTrack(hearTrack, settings.rcnvK(settings.getAPIkey()), signature, settings.getSessionKey(getNetApp()));
			// TODO: ascertain if string is Json
			if (response.equals("okSuccess")) {
				Handler h = new Handler(mCtx.getMainLooper());
				h.post(() -> Toast.makeText(mCtx, mCtx.getString(R.string.loved_track) + " " + getNetApp().getName(), Toast.LENGTH_SHORT).show());
				Log.d(TAG, "Successful heart track: " + getNetApp().getName());
			} else {
				JSONObject jObject = new JSONObject(response);
				if (jObject.has("error")) {
					int code = jObject.getInt("error");
					if (code == 6) {
						// store hearTrack in database or allow failure.
						// settings.setSessionKey(getNetApp(), "");
						Log.d(TAG, "Failed heart track.");
					}
				} else {
					Log.d(TAG, "Failed heart track.");
				}
			}
		} catch (Exception ex) {
			Crashlytics.logException(ex);
		}
	}

	private String postHeartTrack(Track track, String testAPI, String signature, String
			sessionKey) {
		URL url;
		HttpURLConnection conn = null;

		try {

			if (getNetApp() == NetApp.LASTFM) {
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
			params.put("method", "track.love");
			params.put("track", track.getTrack());
			params.put("artist", track.getArtist());
			params.put("api_key", testAPI);
			params.put("api_sig", signature);
			params.put("sk", sessionKey);
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

			//Log.d(TAG,"Love track post: "+postData.toString());

			conn.getOutputStream().write(postDataBytes);
			//Log.i(TAG, params.toString());

			conn.connect();

			int resCode = conn.getResponseCode();
			Log.d(TAG, "Response code: " + resCode);
			BufferedReader r;
			String out;
			if (resCode == -1) {
				return "";
			} else if (resCode == 200) {
				out = "okSuccess";
			} else {
				r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					stringBuilder.append(line).append('\n');
				}
				out = stringBuilder.toString();
			}
			return out;
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
