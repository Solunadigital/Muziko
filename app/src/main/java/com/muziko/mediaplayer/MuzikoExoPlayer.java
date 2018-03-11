package com.muziko.mediaplayer;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.audiofx.BassBoost;
import android.media.audiofx.Equalizer;
import android.media.audiofx.LoudnessEnhancer;
import android.media.audiofx.PresetReverb;
import android.media.audiofx.Virtualizer;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arasthel.asyncjob.AsyncJob;
import com.crashlytics.android.Crashlytics;
import com.devbrackets.android.exomedia.AudioPlayer;
import com.devbrackets.android.exomedia.core.listener.MetadataListener;
import com.devbrackets.android.exomedia.listener.OnCompletionListener;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.google.android.exoplayer2.metadata.Metadata;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.common.events.PlayerPreparedEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.ReverbItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.BufferingEvent;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.models.EqualizerItem;
import com.oasisfeng.condom.CondomContext;

import java.util.ArrayList;
import java.util.Random;

import hugo.weaving.DebugLog;
import pl.tajchert.buswear.EventBus;

/**
 * Created by dev on 11/09/2016.
 */
public class MuzikoExoPlayer
        implements OnPreparedListener, OnCompletionListener, OnErrorListener, MetadataListener {

    private static final String TAG = MuzikoExoPlayer.class.getSimpleName();
    public static Equalizer equalizer;
    private static MuzikoExoPlayer instance;
    private static BassBoost bassBoost;
    private static Virtualizer virtualizer;
    private static LoudnessEnhancer loudnessEnhancer;
    private static PresetReverb reverb;
    private int nextSongIndex = 0;
    private int mCounter = 1;
    private AudioPlayer mCurrentPlayer = null;
    private AudioPlayer mNextPlayer = null;
    private int audioSessionId = 0;
    private boolean prepareCalled = false;
    private boolean playerReady = false;
    private boolean streaming = false;
    private Context context;

    @DebugLog
    private MuzikoExoPlayer() {
        try {
            this.context = MyApplication.getInstance();
            mCurrentPlayer = new AudioPlayer(CondomContext.wrap(context, "ExoMedia"));
            mCurrentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mCurrentPlayer.setOnPreparedListener(this);
            mCurrentPlayer.setOnErrorListener(this);
            mCurrentPlayer.setOnCompletionListener(this);
            mCurrentPlayer.setMetadataListener(this);

            loadEqualizer(context, true);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public static MuzikoExoPlayer Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new MuzikoExoPlayer();
        }
        return instance;
    }


    @DebugLog
    public boolean isStreaming() {
        return streaming;
    }

    @DebugLog
    public void setStreaming(boolean streaming) {
        this.streaming = streaming;
    }

    public boolean isPlayerReady() {
        return playerReady;
    }

    @DebugLog
    public void setPlayerReady(boolean playerReady) {
        this.playerReady = playerReady;
    }

    @DebugLog
    public void createNextMediaPlayer() {
        try {
            mNextPlayer = new AudioPlayer(context);
            mNextPlayer.setDataSource(Uri.parse(getNextSong()));
            mCurrentPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mCurrentPlayer.setOnPreparedListener(this);
            mCurrentPlayer.setOnErrorListener(this);
            mCurrentPlayer.setOnCompletionListener(this);
            mCurrentPlayer.setMetadataListener(this);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
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
                        } else //repeat all
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

    @DebugLog
    public boolean hasNextMediaPlayer() {

        return mNextPlayer != null;
    }

    @DebugLog
    public void killNextMediaPlayer() {

        mNextPlayer = null;
        if (mCurrentPlayer != null && mCurrentPlayer.isPlaying()) {
            //			mCurrentPlayer.setNextMediaPlayer(null);
        }
    }

    public boolean isPlaying() {

        try {
            return mCurrentPlayer.isPlaying();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return false;
    }

    @DebugLog
    public void setVolume(float leftVolume, float rightVolume) {
        mCurrentPlayer.setVolume(leftVolume, rightVolume);
    }

    @DebugLog
    public void start() {
        AsyncJob.doInBackground(
                () -> {
                    try {
                        if (mCurrentPlayer != null) {
                            mCurrentPlayer.start();
                        }
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                });
    }

    @DebugLog
    public void stop() throws IllegalStateException {
        mCurrentPlayer.stopPlayback();
    }

    @DebugLog
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

    @DebugLog
    public void release() {
        try {
            mCurrentPlayer.release();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void reset() {

        try {
            mCurrentPlayer.reset();
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    public int getCurrentPosition() {

        try {
            return (int) mCurrentPlayer.getCurrentPosition();
        } catch (Exception e) {
            Crashlytics.logException(e);
            return 0;
        }
    }

    public int getDuration() {

        try {
            long duration = mCurrentPlayer.getDuration();
            if (duration < 1000) {
                if (Long.parseLong(PlayerConstants.QUEUE_SONG.duration) == 0) {
                    int[] rates = Utils.getRates(PlayerConstants.QUEUE_SONG.data);
                    duration = rates[2];

                    if (duration == 0) {
                        int bitrate = rates[0] / 1000;
                        duration = (int) ((PlayerConstants.QUEUE_SONG.size * 8 / 1000) / bitrate) * 1000;
                    }
                    PlayerConstants.QUEUE_SONG.duration = String.valueOf(duration);
                    TrackRealmHelper.updateDuration(PlayerConstants.QUEUE_SONG.data, String.valueOf(duration));
                } else {
                    duration = Long.parseLong(PlayerConstants.QUEUE_SONG.duration);
                }
            } else if (Long.parseLong(PlayerConstants.QUEUE_SONG.duration) == 0) {
                PlayerConstants.QUEUE_SONG.duration = String.valueOf(duration);
                TrackRealmHelper.updateDuration(PlayerConstants.QUEUE_SONG.data, String.valueOf(duration));
            }
            return (int) duration;
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
        return 0;
    }

    @DebugLog
    public void seekTo(int position) {

        mCurrentPlayer.seekTo(position);
    }

    @DebugLog
    private int getAudioSessionId() {

        return mCurrentPlayer.getAudioSessionId();
    }

    @DebugLog
    private void setAuxEffectSendLevel(float aux) {

        //		mCurrentPlayer.setAuxEffectSendLevel(aux);
    }

    @DebugLog
    public void prepare() {

        try {
            playerReady = false;
            prepareCalled = true;
            mCurrentPlayer.prepareAsync();
            PlayerPreparedEvent preparedEvent = new PlayerPreparedEvent(0);
            EventBus.getDefault(context).postLocal(preparedEvent);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void setDataSource(String data) {
        mCurrentPlayer.setDataSource(Uri.parse(data));
    }

    @DebugLog
    public void setDataSourceExpiring(Context context, QueueItem queueItem) {
        final String[] url = {""};

        AsyncJob.doInBackground(
                () -> {
                    try {
                        url[0] = CloudManager.Instance().getCloudFileUrl(context, queueItem);
                        // Create a fake result (MUST be final)
                        final boolean result = true;

                        // Send the result to the UI thread and show it on a Toast
                        AsyncJob.doOnMainThread(
                                () -> mCurrentPlayer.setDataSource(Uri.parse(url[0])));
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                });
    }

    @DebugLog
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

    @DebugLog
    @Override
    public void onCompletion() {
        AsyncJob.doInBackground(
                () -> {
                    boolean removed = updatePlayed();
                    if (removed) {
                        PlayerConstants.QUEUE_LIST.remove(PlayerConstants.QUEUE_INDEX);

                        AppController.Instance().serviceUnqueue(
                                PlayerConstants.QUEUE_SONG.hash);
                        AppController.Instance().serviceDirty();
                    }

                    if (MyApplication.sleepLastSong) {
                        PrefsManager.Instance().setSleepTimeLastSong(
                                false);
                        MyApplication.sleepLastSong = false;
                        mCurrentPlayer.stopPlayback();

                        return;
                    }

                    SharedPreferences prefs =
                            PreferenceManager.getDefaultSharedPreferences(
                                    MyApplication.getInstance().getBaseContext());
                    boolean prefGapless = prefs.getBoolean(SettingsManager.prefGapless, false);
                    if (prefGapless && mNextPlayer != null) {
                        mCurrentPlayer.release();
                        mCurrentPlayer = mNextPlayer;
                        mCurrentPlayer.setOnCompletionListener(this);
                        PlayerConstants.QUEUE_INDEX = nextSongIndex;
                        PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(nextSongIndex);
                        PlayerConstants.QUEUE_TIME = 0;
                        PlayerConstants.QUEUE_DURATION =
                                Utils.getInt(PlayerConstants.QUEUE_SONG.duration, 0);
                        AppController.Instance().serviceGaplessNext(
                        );
                        Log.d(TAG, String.format("Loop #%d", ++mCounter));
                    } else {

                        if (removed) {
                            AppController.Instance().servicePlay(
                                    false);
                            Intent trackEditIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
                            trackEditIntent.putExtra("userChange", true);
                            MyApplication.getInstance()
                                    .getBaseContext()
                                    .sendBroadcast(trackEditIntent);
                        } else {
                            AppController.Instance().serviceNext();
                            Intent trackEditIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
                            trackEditIntent.putExtra("userChange", true);
                            MyApplication.getInstance()
                                    .getBaseContext()
                                    .sendBroadcast(trackEditIntent);
                        }
                    }
                });
    }

    @DebugLog
    @Override
    public boolean onError(Exception e) {
        Crashlytics.logException(e);
        if (PlayerConstants.QUEUE_LIST.size() < 2) {
            AppController.Instance().serviceBack();
        } else {
            AppController.Instance().serviceNext();
        }

        return false;
    }

    @DebugLog
    @Override
    public void onPrepared() {
        AsyncJob.doInBackground(
                () -> {
                    if (mCurrentPlayer != null && prepareCalled) {
                        audioSessionId = mCurrentPlayer.getAudioSessionId();
                        playerReady = true;
                        if (PrefsManager.Instance().getEqualizer()) equalizerOn();
                        mCurrentPlayer.start();
                        BufferingEvent bufferingEvent = new BufferingEvent(context.getString(R.string.network_lost), true);
                        AppController.isBuffering = false;
                        EventBus.getDefault(context).postLocal(bufferingEvent);
                    }
                });
    }

    @DebugLog
    public void loadEqualizer(Context context, boolean refresh) {

        MyApplication.reverbs = new ArrayList<>();
        MyApplication.reverbs.add(new ReverbItem(PresetReverb.PRESET_NONE, "None"));
        MyApplication.reverbs.add(new ReverbItem(PresetReverb.PRESET_SMALLROOM, "Small Room"));
        MyApplication.reverbs.add(new ReverbItem(PresetReverb.PRESET_MEDIUMROOM, "Medium Room"));
        MyApplication.reverbs.add(new ReverbItem(PresetReverb.PRESET_LARGEROOM, "Large Room"));
        MyApplication.reverbs.add(new ReverbItem(PresetReverb.PRESET_MEDIUMHALL, "Medium Hall"));
        MyApplication.reverbs.add(new ReverbItem(PresetReverb.PRESET_LARGEHALL, "Large Hall"));
        MyApplication.reverbs.add(new ReverbItem(PresetReverb.PRESET_PLATE, "Plate"));

        if (MyApplication.presets == null || MyApplication.presets.size() == 0 || refresh) {
            if (equalizer==null){
                equalizer = new Equalizer(0, audioSessionId);
            }
            MyApplication.presets = Utils.loadEqualizer(equalizer);
        }

        if (MyApplication.preset == null) {
            MyApplication.preset = new EqualizerItem();
            int preset = PrefsManager.Instance().getEqualizerPreset();
            setEqualizer(context, preset);
        }
    }

    @DebugLog
    public void setEqualizer(Context context, int preset) {

        try {
            if (preset < 0 || preset > MyApplication.presets.size()) {
                return;
            }

            MyApplication.preset = MyApplication.presets.get(preset);
            equalizerUpdate();
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }

    @DebugLog
    public void equalizerOn() {

        try {
            if (MyApplication.preset != null && audioSessionId != -1) {

                equalizer = new Equalizer(0, audioSessionId);

                int number = equalizer.getNumberOfBands();
                short r[] = equalizer.getBandLevelRange();

                equalizer.setBandLevel((short) 0, (short) (MyApplication.preset.band1));
                equalizer.setBandLevel((short) 1, (short) (MyApplication.preset.band2));
                equalizer.setBandLevel((short) 2, (short) (MyApplication.preset.band3));
                equalizer.setBandLevel((short) 3, (short) (MyApplication.preset.band4));
                equalizer.setBandLevel((short) 4, (short) (MyApplication.preset.band5));

                bassBoost =
                        new BassBoost(
                                0, MuzikoExoPlayer.Instance().getAudioSessionId());
                bassBoost.setStrength((short) MyApplication.preset.bass);
                bassBoost.setEnabled(true);

                virtualizer =
                        new Virtualizer(
                                0, MuzikoExoPlayer.Instance().getAudioSessionId());
                virtualizer.setStrength((short) MyApplication.preset.threed);
                virtualizer.setEnabled(true);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    loudnessEnhancer =
                            new LoudnessEnhancer(
                                    MuzikoExoPlayer.Instance().getAudioSessionId());
                    loudnessEnhancer.setTargetGain(MyApplication.preset.loudness);
                    loudnessEnhancer.setEnabled(true);
                }

                reverb =
                        new PresetReverb(
                                0, MuzikoExoPlayer.Instance().getAudioSessionId());
                reverb.setPreset((short) MyApplication.preset.reverb);
                reverb.setEnabled(true);

                MuzikoExoPlayer.Instance().setAuxEffectSendLevel(1.0f);

                equalizer.setEnabled(true);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void equalizerOff() {

        try {
            if (equalizer != null) {
                if (bassBoost != null) bassBoost.setEnabled(false);

                if (virtualizer != null) virtualizer.setEnabled(false);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (loudnessEnhancer != null) loudnessEnhancer.setEnabled(false);
                }

                if (reverb != null) reverb.setEnabled(false);

                equalizer.setEnabled(false);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    public void equalizerUpdate() {

        try {
            if (equalizer != null) {
                boolean state = equalizer.getEnabled();
                equalizer.setEnabled(false);

                equalizer.setBandLevel((short) 0, (short) (MyApplication.preset.band1));
                equalizer.setBandLevel((short) 1, (short) (MyApplication.preset.band2));
                equalizer.setBandLevel((short) 2, (short) (MyApplication.preset.band3));
                equalizer.setBandLevel((short) 3, (short) (MyApplication.preset.band4));
                equalizer.setBandLevel((short) 4, (short) (MyApplication.preset.band5));

                if (bassBoost != null) {
                    bassBoost.setStrength((short) MyApplication.preset.bass);
                }

                if (virtualizer != null) {
                    virtualizer.setStrength((short) MyApplication.preset.threed);
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    if (loudnessEnhancer != null) {
                        loudnessEnhancer.setTargetGain(MyApplication.preset.loudness);
                    }
                }

                if (reverb != null) {
                    reverb.setPreset((short) MyApplication.preset.reverb);
                }

                equalizer.setEnabled(state);
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    @Override
    public void onMetadata(Metadata metadata) {
        if (metadata != null) {
            Metadata.Entry data = metadata.get(0);
        }
    }
}

