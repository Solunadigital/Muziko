package com.muziko.api.LastFM.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.muziko.R;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.InternalTrackTransmitter;
import com.muziko.api.LastFM.Utils.ScrobblesDatabase;
import com.muziko.api.LastFM.Utils.Track;
import com.muziko.api.LastFM.Utils.Util;
import com.muziko.api.LastFM.Utils.enums.AdvancedOptionsWhen;
import com.muziko.api.LastFM.Utils.enums.PowerOptions;

/**
 * Created by dev on 28/08/2016.
 */
public class ScrobblingService extends Service {

	public static final String ACTION_AUTHENTICATE = "com.muziko.service.authenticate";
	public static final String ACTION_CLEARCREDS = "com.muziko.service.clearcreds";
	public static final String ACTION_JUSTSCROBBLE = "com.muziko.service.justscrobble";
	public static final String ACTION_PLAYSTATECHANGED = "com.muziko.service.playstatechanged";
	public static final String ACTION_HEART = "com.muziko.service.heart";
	public static final String ACTION_COPY = "com.muziko.service.copy";
	public static final String BROADCAST_ONAUTHCHANGED = "com.muziko.service.bcast.onauth";
	public static final String BROADCAST_ONSTATUSCHANGED = "com.muziko.service.bcast.onstatus";
	private static final String TAG = "ScrobblingService";
	private static final long MIN_LISTENING_TIME = 30 * 1000;
	private static final long UPPER_SCROBBLE_MIN_LIMIT = 240 * 1000;
	private static final long MAX_PLAYTIME_DIFF_TO_SCROBBLE = 3000;
	Context mCtx = this;
	private AppSettings settings;
	private ScrobblesDatabase mDb;
	private NetworkerManager mNetManager;
	private Track mCurrentTrack = null;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		settings = new AppSettings(this);
		mDb = new ScrobblesDatabase(this);
		mDb.open();
		mNetManager = new NetworkerManager(this, mDb);
	}

	@Override
	public void onDestroy() {
		mDb.close();
	}

	@Override
	public int onStartCommand(Intent i, int flags, int startId) {
		handleCommand(i);

		return Service.START_STICKY;
	}

	//Note this function is deprecated starting at API level 5
	@Override
	public void onStart(Intent i, int startId) {
		handleCommand(i);
	}

	private void handleCommand(Intent i) {
		if (i == null) {
			Log.e(TAG, "got null intent");
			return;
		}
		String action = i.getAction();
		Bundle extras = i.getExtras();
		switch (action) {
			case ACTION_CLEARCREDS:
				if (extras.getBoolean("clearall", false)) {
					mNetManager.launchClearAllCreds();
				} else {
					String snapp = extras.getString("netapp");
					if (snapp != null) {
						mNetManager.launchClearCreds(NetApp.valueOf(snapp));
					} else
						Log.e(TAG, "launchClearCreds got null napp");
				}
				break;
			case ACTION_AUTHENTICATE:
				String snapp = extras.getString("netapp");
				if (snapp != null)
					mNetManager.launchAuthenticator(NetApp.valueOf(snapp));
				else {
					Log.e(TAG, "launchHandshaker got null napp");
					mNetManager.launchHandshakers();
				}
				break;
			case ACTION_JUSTSCROBBLE:
				if (extras.getBoolean("scrobbleall", false)) {
					Log.d(TAG, "Scrobble All TRUE");
					mNetManager.launchAllScrobblers();
				} else {
					Log.e(TAG, "Scrobble All False");
					snapp = extras.getString("netapp");
					if (snapp != null) {
						mNetManager.launchScrobbler(NetApp.valueOf(snapp));
					} else
						Log.e(TAG, "launchScrobbler got null napp");
				}
				break;
			case ACTION_PLAYSTATECHANGED:
//			if (extras == null) {
//				Log.e(TAG, "Got null extras on playstatechange");
//				return;
//			}
				Track.State state = Track.State.START; //Track.State.valueOf(extras.getString("state"));


				Track track = InternalTrackTransmitter.popTrack();

				if (track == null) {
					Log.e(TAG, "A null track got through!! (Ignoring it)");
					return;
				}

				onPlayStateChanged(track, state);

				break;
			case ACTION_HEART:
				if (!settings.getUsername(NetApp.LASTFM).equals("")) {
					if (mCurrentTrack != null && mCurrentTrack.hasBeenQueued()) {
						try {
							if (mDb.fetchRecentTrack() == null) {
								Toast.makeText(this, this.getString(R.string.no_heart_track),
										Toast.LENGTH_LONG).show();
							} else {
								mDb.loveRecentTrack();
								Toast.makeText(this, this.getString(R.string.song_is_ready), Toast.LENGTH_SHORT).show();
								Log.d(TAG, "Love Track Rating!");
							}
						} catch (Exception ex) {
							Crashlytics.logException(ex);
						}
					} else if (mCurrentTrack != null) {
						mCurrentTrack.setRating();
						Toast.makeText(this, this.getString(R.string.song_is_ready), Toast.LENGTH_SHORT).show();
						Log.d(TAG, "Love Track Rating!");
					} else {
						Toast.makeText(this, this.getString(R.string.no_current_track),
								Toast.LENGTH_SHORT).show();
					}
				} else {
					Toast.makeText(this, this.getString(R.string.no_lastFm),
							Toast.LENGTH_LONG).show();
				}
				break;
			case ACTION_COPY:
				if (mCurrentTrack != null && mCurrentTrack.hasBeenQueued()) {
					try {
						Log.e(TAG, mDb.fetchRecentTrack().toString());
						Track tempTrack = mDb.fetchRecentTrack();
						int sdk = Build.VERSION.SDK_INT;
						if (sdk < Build.VERSION_CODES.HONEYCOMB) {
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(tempTrack.getTrack() + " by " + tempTrack.getArtist() + ", " + tempTrack.getAlbum());
						} else {
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("Track", tempTrack.getTrack() + " by " + tempTrack.getArtist() + ", " + tempTrack.getAlbum());
							clipboard.setPrimaryClip(clip);
						}
						Log.d(TAG, "Copy Track!");
					} catch (Exception ex) {
						Toast.makeText(this, this.getString(R.string.no_copy_track),
								Toast.LENGTH_LONG).show();
						Crashlytics.logException(ex);
					}
				} else if (mCurrentTrack != null) {
					try {
						int sdk = Build.VERSION.SDK_INT;
						if (sdk < Build.VERSION_CODES.HONEYCOMB) {
							android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							clipboard.setText(mCurrentTrack.getTrack() + " by " + mCurrentTrack.getArtist() + ", " + mCurrentTrack.getAlbum());
						} else {
							android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
							android.content.ClipData clip = android.content.ClipData.newPlainText("Track", mCurrentTrack.getTrack() + " by " + mCurrentTrack.getArtist() + ", " + mCurrentTrack.getAlbum());
							clipboard.setPrimaryClip(clip);
						}
						Log.d(TAG, "Copy Track!");
					} catch (Exception ex) {
						Toast.makeText(this, this.getString(R.string.no_copy_track),
								Toast.LENGTH_LONG).show();
						Crashlytics.logException(ex);
					}
				} else {
					Toast.makeText(this, this.getString(R.string.no_current_track),
							Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				Log.e(TAG, "Weird action in Start: " + action);
				break;
		}
	}

	private synchronized void onPlayStateChanged(Track track, Track.State state) {
		Log.d(TAG, "State: " + state.name());
		if (track == Track.SAME_AS_CURRENT) {
			// this only happens for apps implementing Scrobble Droid's API
			Log.d(TAG, "Got a SAME_AS_CURRENT track");
			if (mCurrentTrack != null) {
				track = mCurrentTrack;
			} else {
				Log.e(TAG, "Got a SAME_AS_CURRENT track, but current was null!");
				return;
			}
		}

		if (state == Track.State.START || state == Track.State.RESUME) { // start/resume
			if (mCurrentTrack != null) {
				mCurrentTrack.updateTimePlayed();
				tryQueue(mCurrentTrack);
				if (track.equals(mCurrentTrack)) {
					return;
				} else {
					tryScrobble();
				}
			}

			mCurrentTrack = track;
			mCurrentTrack.updateTimePlayed();
//			tryNotifyNP(mCurrentTrack);

			// TODO: maybe give notifications it's own service
			// TODO: work around for permanent notification
		} else if (state == Track.State.PAUSE) { // pause
			// TODO: test this state
			if (mCurrentTrack == null) {
				// just ignore the track
			} else {
				if (!track.equals(mCurrentTrack)) {
					Log.e(TAG, "PStopped track doesn't equal currentTrack!");
					Log.e(TAG, "t: " + track);
					Log.e(TAG, "c: " + mCurrentTrack);
				} else {
					mCurrentTrack.updateTimePlayed();
					// below: to be set on RESUME
					mCurrentTrack.stopCountingTime();

					tryQueue(mCurrentTrack);
				}
			}
		} else if (state == Track.State.COMPLETE) { // "complete"
			// TODO test this state
			if (mCurrentTrack == null) {
				// just ignore the track
			} else {
				if (!track.equals(mCurrentTrack)) {
					Log.e(TAG, "CStopped track doesn't equal currentTrack!");
					Log.e(TAG, "t: " + track);
					Log.e(TAG, "c: " + mCurrentTrack);
				} else {
					mCurrentTrack.updateTimePlayed();
					tryQueue(mCurrentTrack);
					tryScrobble();
					mCurrentTrack = null;
				}
			}
		} else if (state == Track.State.PLAYLIST_FINISHED) { // playlist end
			if (mCurrentTrack == null) {
				tryQueue(track); // TODO: this can't succeed (time played = 0)
				tryScrobble(true);
			} else {
				if (!track.equals(mCurrentTrack)) {
					Log.e(TAG, "PFStopped track doesn't equal currentTrack!");
					Log.e(TAG, "t: " + track);
					Log.e(TAG, "c: " + mCurrentTrack);
				} else {
					mCurrentTrack.updateTimePlayed();
					tryQueue(mCurrentTrack);
					tryScrobble(true);
				}
			}

			mCurrentTrack = null;
		} else if (state == Track.State.UNKNOWN_NONPLAYING) {
			// similar to PAUSE, but might scrobble if close enough
			if (mCurrentTrack == null) {
				// just ignore the track
			} else {
				mCurrentTrack.updateTimePlayed();
				// below: to be set on RESUME
				mCurrentTrack.stopCountingTime();

				tryQueue(mCurrentTrack);
				if (!mCurrentTrack.hasUnknownDuration()) {
					long diff = Math.abs(mCurrentTrack.getDuration() * 1000
							- mCurrentTrack.getTimePlayed());
					if (diff < MAX_PLAYTIME_DIFF_TO_SCROBBLE) {
						tryScrobble();
					}
				}
			}
		} else {
			Log.e(TAG, "Unknown track state: " + state.toString());
		}
	}

	/**
	 * Launches a Now Playing notification of <code>track</code>, if we're
	 * authenticated and Now Playing is enabled.
	 *
	 * @param track the currently playing track
	 */
	private void tryNotifyNP(Track track) {
		PowerOptions pow = Util.checkPower(this);

		if (!settings.isAnyAuthenticated()
				|| !settings.isNowPlayingEnabled(pow)) {
			Log.d(TAG, "Won't notify NP, unauthed or disabled");
			return;
		}

		mNetManager.launchNPNotifier(track);
	}

	private void tryQueue(Track track) {
		if (!settings.isAnyAuthenticated()
				|| !settings.isScrobblingEnabled(Util.checkPower(this))) {
			Log.d(TAG, "Won't prepare scrobble, unauthed or disabled");
			return;
		}

		double sp = settings.getScrobblePoint() / (double) 100;
		sp -= 0.01; // to be safe
		long mintime = (long) (sp * 1000 * track.getDuration());
		//Log.e(TAG,"mintime:" +Long.toString(mintime));

		if (track.hasBeenQueued()) {
			Log.d(TAG, "Trying to queue a track that already has been queued");
			// Log.d(TAG, track.toString());
			return;
		}
		if (track.hasUnknownDuration() || mintime < MIN_LISTENING_TIME) {
			mintime = MIN_LISTENING_TIME;
		} else if (mintime > UPPER_SCROBBLE_MIN_LIMIT) {
			mintime = UPPER_SCROBBLE_MIN_LIMIT;
		}
		if (track.getTimePlayed() >= mintime) {
			Log.d(TAG, "Will try to queue track, played: "
					+ track.getTimePlayed() + " vs " + mintime);
			queue(mCurrentTrack);
		} else {
			Log.d(TAG, "Won't queue track, not played long enough: "
					+ track.getTimePlayed() + " vs " + mintime);
			Log.d(TAG, track.toString());
		}
	}

	/**
	 * Only to be called by tryQueue(Track track).
	 *
	 * @param track
	 */
	private void queue(Track track) {
		long rowId = mDb.insertTrack(track);
		if (rowId != -1) {
			track.setQueued();
			Log.d(TAG, "queued track after playtime: " + track.getTimePlayed());
			Log.d(TAG, track.toString());

			// now set up scrobbling rels
			for (NetApp napp : NetApp.values()) {
				if (settings.isAuthenticated(napp)) {
					Log.d(TAG, "inserting scrobble: " + napp.getName());
					mDb.insertScrobble(napp, rowId);

					// tell interested parties
					Intent i = new Intent(
							ScrobblingService.BROADCAST_ONSTATUSCHANGED);
					i.putExtra("netapp", napp.toString());
					sendBroadcast(i);
				}
			}
		} else {
			Log.e(TAG, "Could not insert scrobble into the db");
			Log.e(TAG, track.toString());
		}
	}

	private void tryScrobble() {
		tryScrobble(false);
	}

	private void tryScrobble(boolean playbackComplete) {

		if (!settings.isAnyAuthenticated()
				|| !settings.isScrobblingEnabled(Util.checkPower(this))) {
			Log.d(TAG, "Won't prepare scrobble, unauthed or disabled");
			return;
		}

		scrobble(playbackComplete);
	}

	/**
	 * Only to be called by tryScrobble(...).
	 *
	 * @param playbackComplete
	 */
	private void scrobble(boolean playbackComplete) {

		PowerOptions pow = Util.checkPower(this);

		boolean aoc = settings.getAdvancedOptionsAlsoOnComplete(pow);
		if (aoc && playbackComplete) {
			Log.d(TAG, "Launching scrobbler because playlist is finished");
			mNetManager.launchAllScrobblers();
			return;
		}

		AdvancedOptionsWhen aow = settings.getAdvancedOptionsWhen(pow);
		for (NetApp napp : NetApp.values()) {
			int numInCache = mDb.queryNumberOfScrobbles(napp);
			if (numInCache >= aow.getTracksToWaitFor()) {
				mNetManager.launchScrobbler(napp);
			}
		}
	}
}