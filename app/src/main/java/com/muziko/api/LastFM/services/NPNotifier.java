package com.muziko.api.LastFM.services;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.muziko.R;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.AuthStatus;
import com.muziko.api.LastFM.Utils.MD5;
import com.muziko.api.LastFM.Utils.Track;
import com.muziko.api.LastFM.Utils.Util;
import com.muziko.api.LastFM.Utils.enums.SubmissionType;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.TreeMap;


public class NPNotifier extends AbstractSubmitter {

	private static final String TAG = "NPNotifier";

	private final Track mTrack;

	private final Context mCtx;

	private final AppSettings settings;

	public NPNotifier(NetApp napp, Context ctx, Networker net, Track track) {
		super(napp, ctx, net);
		mTrack = track;
		mCtx = ctx;
		settings = new AppSettings(ctx);
	}

	@Override
	protected boolean doRun(Handshaker.HandshakeResult hInfo) {
		boolean ret;
		try {
			notifyNowPlaying(mTrack, hInfo);

			// status stuff
			notifySubmissionStatusSuccessful(mTrack, 1);

			ret = true;
		} catch (AuthStatus.BadSessionException e) {
			Log.i(TAG, "BadSession: " + e.getMessage() + ": "
					+ getNetApp().getName());
			settings.setSessionKey(getNetApp(), "");
			getNetworker().launchHandshaker();
			relaunchThis();
			notifySubmissionStatusFailure(getContext().getString(
					R.string.auth_just_error));
			Util.myNotify(mCtx, getNetApp().getName(),
					mCtx.getString(R.string.auth_bad_auth), 39201);
			ret = true;
		} catch (AuthStatus.TemporaryFailureException e) {
			Log.i(TAG, "Tempfail: " + e.getMessage() + ": "
					+ getNetApp().getName());
			notifySubmissionStatusFailure(getContext().getString(
					R.string.auth_network_error_retrying));
			ret = false;
		} catch (AuthStatus.ClientBannedException e) {
			Log.e(TAG, "This version of the client has been banned!!" + ": "
					+ getNetApp().getName());
			Log.e(TAG, e.getMessage());
			// TODO: what??  notify user
			notifyAuthStatusUpdate(AuthStatus.AUTHSTATUS_CLIENTBANNED);
			Util.myNotify(mCtx, getNetApp().getName(),
					mCtx.getString(R.string.auth_client_banned), 39201);
			e.getStackTrace();
			ret = true;
		} catch (AuthStatus.UnknownResponseException e) {
			if (Util.checkForOkNetwork(getContext()) != Util.NetworkStatus.OK) {
				// no more sleeping, network down
				Log.e(TAG, "Network status: " + Util.checkForOkNetwork(getContext()));
				getNetworker().resetSleeper();
				getNetworker().launchNetworkWaiter();
				relaunchThis();
			} else {
				getNetworker().launchSleeper();
				relaunchThis();
			}
			e.getStackTrace();
			ret = false;
		}
		return ret;
	}

	@Override
	protected void relaunchThis() {
		getNetworker().launchNPNotifier(mTrack);
	}

	private void notifySubmissionStatusFailure(String reason) {
		super.notifySubmissionStatusFailure(SubmissionType.NP, reason);
	}

	private void notifySubmissionStatusSuccessful(Track track, int statsInc) {
		super.notifySubmissionStatusSuccessful(SubmissionType.NP, track,
				statsInc);
	}

	private void notifyAuthStatusUpdate(int st) {
		settings.setAuthStatus(getNetApp(), st);
		Intent i = new Intent(ScrobblingService.BROADCAST_ONAUTHCHANGED);
		i.putExtra("netapp", getNetApp().getIntentExtraValue());
		getContext().sendBroadcast(i);
	}

	/**
	 * Connects to Last.fm servers and requests a Now Playing notification of
	 * <code>track</code>. If an error occurs, exceptions are thrown.
	 *
	 * @param track the track to send as notification
	 * @throws BadSessionException       means that a new handshake is needed
	 * @throws TemporaryFailureException
	 * @throws UnknownResponseException  {@link UnknownResponseException}
	 */
	private void notifyNowPlaying(Track track, Handshaker.HandshakeResult hInfo)
			throws AuthStatus.BadSessionException, AuthStatus.TemporaryFailureException, AuthStatus.ClientBannedException, AuthStatus.UnknownResponseException {
		NetApp netApp = getNetApp();
		String netAppName = netApp.getName();
		URL url;
		HttpURLConnection conn = null;

// handle Exception

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB && netApp == NetApp.LIBREFM) {

			try {
				url = new URL(hInfo.nowPlayingUri);
				// Log.d(TAG,url.toString());
				Map<String, Object> params = new TreeMap<>();
				params.put("s", hInfo.sessionId);
				params.put("a", track.getArtist());
				params.put("b", track.getAlbum());
				params.put("t", track.getTrack());
				params.put("i", Long.toString(track
						.getWhen()));
				params.put("o", track.getSource());
				params.put("l", Integer.toString(track
						.getDuration()));
				params.put("n", track.getTrackNr());
				params.put("m", track.getMbid());
				params.put("r", track.getRating());

				StringBuilder postData = new StringBuilder();
				for (Map.Entry<String, Object> param : params.entrySet()) {
					if (postData.length() != 0) postData.append('&');
					postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
					postData.append('=');
					if (param.getValue() == null) {
						postData.append(URLEncoder.encode("", "UTF-8"));
					} else {
						postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
					}
				}
				byte[] postDataBytes = postData.toString().getBytes("UTF-8");

				conn = (HttpURLConnection) url.openConnection();
				// Log.d(TAG,conn.toString());

				conn.setReadTimeout(7000);
				conn.setConnectTimeout(7000);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.getOutputStream().write(postDataBytes);
				//Log.i(TAG, params.toString());

				int resCode = conn.getResponseCode();
				Log.d(TAG, "Response code: " + resCode);
				BufferedReader r;
				if (resCode == -1) {
					throw new AuthStatus.UnknownResponseException("Empty response");
				} else if (resCode == 200) {
					r = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				} else {
					r = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
				}
				StringBuilder rsponse = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					rsponse.append(line).append('\n');
				}
				r.close();
				String response = rsponse.toString();
				// some redundancy here ?
				String[] lines = response.split("\n");
				//Log.d(TAG, "NPNotifier Result: " + lines.length + " : " + response);

				if (response.startsWith("OK")) {
					Log.i(TAG, "Now playing success: " + getNetApp().getName());
				} else if (response.startsWith("BADSESSION")) {
					throw new AuthStatus.BadSessionException("Now Playing failed because of badsession");
				} else if (response.startsWith("FAILED")) {
					String reason = lines[0].substring(7);
					throw new AuthStatus.TemporaryFailureException("Now Playing failed: " + reason);
				} else {
					throw new AuthStatus.TemporaryFailureException("Now Playing failed weirdly: " + response);
				}

			} catch (IOException | NullPointerException e) {
				throw new AuthStatus.TemporaryFailureException(TAG + ": " + e.getMessage());
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		} else {
			try {

				if (netApp == NetApp.LASTFM) {
					url = new URL("https://ws.audioscrobbler.com/2.0/");
				} else if (netApp == NetApp.LIBREFM) {
					url = new URL("https://libre.fm/2.0/");
				} else {    // for custom GNU FM server
					url = new URL("");
				}

				Map<String, Object> params = new TreeMap<>();
				if (track.getAlbum() != null) {
					params.put("album", track.getAlbum());
				}
				params.put("api_key", settings.rcnvK(settings.getAPIkey()));
				params.put("artist", track.getArtist());
				if (track.getDuration() != 180) {
					params.put("duration", Integer.toString(track.getDuration()));
				}
				if (track.getMbid() != null) {
					params.put("mbid", track.getMbid());
				}
				params.put("method", "track.updateNowPlaying");
				params.put("sk", settings.getSessionKey(netApp));
				params.put("track", track.getTrack());
				if (track.getTrackNr() != null) {
					params.put("trackNumber", track.getTrackNr());
				}

				String sign = "";
				for (Map.Entry<String, Object> param : params.entrySet()) {
					sign += param.getKey() + String.valueOf(param.getValue());
				}

				String signature = MD5.getHashString(sign + settings.rcnvK(settings.getSecret()));
				params.put("api_sig", signature);
				params.put("format", "json");

				StringBuilder postData = new StringBuilder();
				for (Map.Entry<String, Object> param : params.entrySet()) {
					if (postData.length() != 0) postData.append('&');
					postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
					postData.append('=');
					postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
				}
				byte[] postDataBytes = postData.toString().getBytes("UTF-8");

				conn = (HttpURLConnection) url.openConnection();

				conn.setReadTimeout(10000);
				conn.setConnectTimeout(10000);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.getOutputStream().write(postDataBytes);
				conn.connect();
				int resCode = conn.getResponseCode();
				Log.d(TAG, "Response code: " + resCode);
				BufferedReader r;
				if (resCode == -1) {
					throw new AuthStatus.UnknownResponseException("Empty response");
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
				if (response.equals("")) {
					throw new AuthStatus.UnknownResponseException("Empty response");
				}
				JSONObject jObject = new JSONObject(response);
				if (jObject.has("nowplaying")) {
					Log.i(TAG, "Now Playing success: " + netAppName);
				} else if (jObject.has("error")) {
					int code = jObject.getInt("error");
					if (code == 26 || code == 10) {
						Log.e(TAG, "Now Playing failed: client banned: " + netAppName);
						settings.setSessionKey(netApp, "");
						throw new AuthStatus.ClientBannedException("Now Playing failed because of client banned");
					} else if (code == 9) {
						Log.i(TAG, "Now Playing failed: bad auth: " + netAppName);
						settings.setSessionKey(netApp, "");
						throw new AuthStatus.BadSessionException("Now Playing failed because of badsession");
					} else {
						Log.e(TAG, "Now Playing fails: FAILED " + response + ": " + netAppName);
						//settings.setSessionKey(netApp, "");
						throw new AuthStatus.TemporaryFailureException("Now playing failed because of " + response);
					}
				}
			} catch (IOException | JSONException e) {
				throw new AuthStatus.TemporaryFailureException("Now Playing failed weirdly: " + e.getMessage());
			} finally {
				if (conn != null) {
					conn.disconnect();
				}
			}
		}
	}
}
