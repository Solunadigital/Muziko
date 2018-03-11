package com.muziko.mediaplayer;

import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.preference.PreferenceManager;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.muziko.MyApplication;
import com.muziko.common.models.QueueItem;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;

import java.io.IOException;
import java.util.Random;

/**
 * Created by dev on 11/09/2016.
 */

public class MuzikoPlayer implements MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

	private static final String TAG = MuzikoPlayer.class.getSimpleName();
	private int nextSongIndex = 0;
	private int mCounter = 1;
	private MediaPlayer mCurrentPlayer = null;
	private MediaPlayer mNextPlayer = null;

	private MuzikoPlayer() {

		try {

			mCurrentPlayer = new MediaPlayer();
            mCurrentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mCurrentPlayer.setOnErrorListener(this);
			mCurrentPlayer.setOnCompletionListener(this);

		} catch (Exception e) {
			Crashlytics.logException(e);
		}

	}

	public static MuzikoPlayer create() {
		return new MuzikoPlayer();
	}

	public void createNextMediaPlayer() {

		try {
			mNextPlayer = new MediaPlayer();
			mNextPlayer.setDataSource(getNextSong());
			mNextPlayer.prepare();
			mNextPlayer.setOnErrorListener(this);
			mNextPlayer.setOnCompletionListener(this);
			mCurrentPlayer.setNextMediaPlayer(mNextPlayer);
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}

	private String getNextSong() {
		QueueItem nextSong = new QueueItem();
		if (PlayerConstants.QUEUE_LIST.size() > 0) {
            int repeat = PrefsManager.Instance().getPlayRepeat();
            if (repeat == PlayerConstants.REPEAT_ONE) {
				nextSong = PlayerConstants.QUEUE_SONG;
				nextSongIndex = PlayerConstants.QUEUE_INDEX;
			} else {
                if (PrefsManager.Instance().getPlayShuffle(MyApplication.getInstance().getBaseContext())) {
                    Random rand = new Random();
					nextSongIndex = rand.nextInt(((PlayerConstants.QUEUE_LIST.size() - 1)) + 1);
					nextSong = PlayerConstants.QUEUE_LIST.get(nextSongIndex);
				} else {
					nextSongIndex = PlayerConstants.QUEUE_INDEX;
					nextSongIndex++;
					if (nextSongIndex >= PlayerConstants.QUEUE_LIST.size()) {
						if (repeat == PlayerConstants.REPEAT_OFF) {
							nextSong = PlayerConstants.QUEUE_LIST.get(0);
							nextSongIndex = 0;
						} else    //repeat all
						{
							nextSong = PlayerConstants.QUEUE_LIST.get(0);
							nextSongIndex = 0;
						}
					} else {
						nextSong = PlayerConstants.QUEUE_LIST.get(nextSongIndex);
					}
				}
			}
		}
		Log.e(TAG, "Next song is: " + nextSong.data);

		return nextSong.data;
	}

	public boolean hasNextMediaPlayer() {

		return mNextPlayer != null;
	}

	public void killNextMediaPlayer() {

		mNextPlayer = null;
		if (mCurrentPlayer != null && mCurrentPlayer.isPlaying()) {
			mCurrentPlayer.setNextMediaPlayer(null);
		}
	}

	// code-read additions:
	public boolean isPlaying() {

		try {
			return mCurrentPlayer.isPlaying();
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
		return false;
	}

	public void setVolume(float leftVolume, float rightVolume) {
		mCurrentPlayer.setVolume(leftVolume, rightVolume);
	}

	public void start() {
		try {
			if (mCurrentPlayer != null) {
				mCurrentPlayer.start();

//				SharedPreferences current = mContext.getSharedPreferences("current_music", Context.MODE_PRIVATE);
//				SharedPreferences.Editor editor = current.edit();
//				long currentTime = System.currentTimeMillis();
//				editor.putLong("startTime", currentTime).apply();
			}
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}

	public void stop() throws IllegalStateException {
		mCurrentPlayer.stop();
	}

	public void pause() {
		if (mCurrentPlayer != null) {
			if (mCurrentPlayer.isPlaying()) {
				mCurrentPlayer.pause();
			}
		}

//		if (mNextPlayer != null) {
//			if (mNextPlayer.isPlaying()) {
//				mNextPlayer.pause();
//			}
//		}
	}

	public void release() {
		try {
			mCurrentPlayer.release();
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}

	public void reset() {

		try {
			mCurrentPlayer.reset();
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}

	public int getCurrentPosition() {

		try {
			return mCurrentPlayer.getCurrentPosition();
		} catch (Exception e) {
			Crashlytics.logException(e);
			return 0;
		}
	}

	public int getDuration() {

		try {
			return mCurrentPlayer.getDuration();
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
		return 0;
	}

	public void seekTo(int position) {

		mCurrentPlayer.seekTo(position);
	}

	public int getAudioSessionId() {

		return mCurrentPlayer.getAudioSessionId();
	}

	public void setAuxEffectSendLevel(float aux) {

		mCurrentPlayer.setAuxEffectSendLevel(aux);
	}

	public void prepare() {

		try {
			mCurrentPlayer.prepare();
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
	}

	public void setDataSource(String data) {
		try {
			mCurrentPlayer.setDataSource(data);
		} catch (IOException e) {
			Crashlytics.logException(e);
		}
	}

	private boolean updatePlayed() {
		boolean remove = false;
		try {
			QueueItem queueItem = PlayerConstants.QUEUE_LIST.get(PlayerConstants.QUEUE_INDEX);

			if (queueItem.played + 1 == queueItem.removeafter) {
				queueItem.removeafter = 0;
				queueItem.played = 0;
				remove = true;
			} else {
				queueItem.played = queueItem.played + 1;
			}
		} catch (Exception e) {
			Crashlytics.logException(e);
		}
		return remove;
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		String msg;
		switch (extra) {
			case 0:
				return true;

			case MediaPlayer.MEDIA_ERROR_UNKNOWN:
				msg = "Unknown error!";
				break;
			case MediaPlayer.MEDIA_ERROR_IO:
				msg = "I/O Error!";
				break;
			case MediaPlayer.MEDIA_ERROR_MALFORMED:
				msg = "Malformed media!";
				break;
			case MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK:
				msg = "No valid for progressive playback!";
				break;
			case MediaPlayer.MEDIA_ERROR_UNSUPPORTED:
				msg = "Unsupported media!";
				break;
			case MediaPlayer.MEDIA_ERROR_SERVER_DIED:
				msg = "Server died!";
				break;
			case MediaPlayer.MEDIA_ERROR_TIMED_OUT:
				msg = "Media timed out!";
				break;
			default:
				msg = "Unable to play stream!";
				break;
		}

        Log.i(TAG, "onCoverArtDownloadError... " + msg);

		if (PlayerConstants.QUEUE_LIST.size() < 2) {
			AppController.Instance().serviceBack();
		} else {
			AppController.Instance().serviceNext();
		}

		return true;
	}

	@Override
	public void onCompletion(MediaPlayer mediaPlayer) {

		boolean removed = updatePlayed();
		if (removed) {
			PlayerConstants.QUEUE_LIST.remove(PlayerConstants.QUEUE_INDEX);

			AppController.Instance().serviceUnqueue(PlayerConstants.QUEUE_SONG.hash);
			AppController.Instance().serviceDirty();
		}


		if (MyApplication.sleepLastSong) {
            PrefsManager.Instance().setSleepTimeLastSong(false);
            MyApplication.sleepLastSong = false;
			mCurrentPlayer.stop();

			return;
		}

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(MyApplication.getInstance().getBaseContext());
		boolean prefGapless = prefs.getBoolean(SettingsManager.prefGapless, false);
		if (prefGapless && mNextPlayer != null) {
			mediaPlayer.release();
			mCurrentPlayer = mNextPlayer;
			mCurrentPlayer.setOnCompletionListener(this);
			PlayerConstants.QUEUE_INDEX = nextSongIndex;
			PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(nextSongIndex);
			PlayerConstants.QUEUE_TIME = 0;
			PlayerConstants.QUEUE_DURATION = Utils.getInt(PlayerConstants.QUEUE_SONG.duration, 0);
			AppController.Instance().serviceGaplessNext();
			Log.d(TAG, String.format("Loop #%d", ++mCounter));
		} else {

			if (removed) {
				AppController.Instance().servicePlay(false);
				Intent trackEditIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
				trackEditIntent.putExtra("userChange", true);
				MyApplication.getInstance().getBaseContext().sendBroadcast(trackEditIntent);
			} else {
				AppController.Instance().serviceNext();
				Intent trackEditIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
				trackEditIntent.putExtra("userChange", true);
				MyApplication.getInstance().getBaseContext().sendBroadcast(trackEditIntent);
			}
		}

	}
}