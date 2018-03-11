package com.muziko.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import com.acrcloud.rec.sdk.ACRCloudClient;
import com.acrcloud.rec.sdk.ACRCloudConfig;
import com.acrcloud.rec.sdk.IACRCloudListener;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;
import com.muziko.BuildConfig;
import com.muziko.api.AcrCloud.AcrArtistFour;
import com.muziko.api.AcrCloud.AcrCloudModel;
import com.muziko.api.AcrCloud.Music;
import com.muziko.api.AcrCloud.TrackModel;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.firebase.MostPlayed;
import com.muziko.common.models.firebase.TrackFingerprint;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.ACREvent;
import com.muziko.helpers.ArtworkHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.GsonManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.oasisfeng.condom.CondomContext;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import hugo.weaving.DebugLog;
import pl.tajchert.buswear.EventBus;

/**
 * Created by dev on 25/08/2016.
 */
public class MuzikoFingerprintService extends Service implements IACRCloudListener, ArtworkHelper.ACRArtworkTaskListener {

    public static final String ACTION_ACR = "ACR";
    public static final String ACTION_ACR_CUSTOM = "ACR_CUSTOM";
    private final int trackExists = 9001;
    private ACRCloudClient mClient;
    private boolean initState = false;
    private boolean mProcessing = false;
    private boolean acrIdentifyJob = false;
    private HandlerThread mHandlerThread;
    private ServiceHandler mServiceHandler;
    private ArtworkHelper artworkHelper;
    private MostPlayed creatorShare;
    private MostPlayed mostPlayed;
    private TrackModel trackModel;
    private AcrCloudModel acrCloudModel;

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();

        if (mHandlerThread == null || !mHandlerThread.isAlive()) {
            mHandlerThread = new HandlerThread("MuzikoFingerprintService", Process.THREAD_PRIORITY_LOWEST);
            mHandlerThread.start();

            Looper mServiceLooper = mHandlerThread.getLooper();
            // start the service using the background handler
            mServiceHandler = new ServiceHandler(mServiceLooper);
        }
    }

    @DebugLog
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {

                if (action.equalsIgnoreCase(ACTION_ACR_CUSTOM)) {
                    acrIdentifyJob = true;

                    Message message = mServiceHandler.obtainMessage();
                    message.arg1 = startId;
                    mServiceHandler.sendMessage(message);

                } else if (action.equalsIgnoreCase(ACTION_ACR)) {

                    QueueItem queueItem = TrackRealmHelper.getTrack(PlayerConstants.QUEUE_SONG.data);

                    if (!Utils.isEmptyString(queueItem.getAcrid())) {

                        Message message = mServiceHandler.obtainMessage();
                        message.arg1 = trackExists;
                        mServiceHandler.sendMessage(message);

                    } else {
                        Message message = mServiceHandler.obtainMessage();
                        message.arg1 = startId;
                        mServiceHandler.sendMessage(message);
                    }
                }

            }
        }
        return START_STICKY;
    }

    @DebugLog
    @Override
    public void onDestroy() {
        if (this.mClient != null) {
            this.mClient.release();
            this.initState = false;
            this.mClient = null;
        }

    }

    @DebugLog
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @DebugLog
    private void startACRCloud() {

        if (!this.initState) {

            ACRCloudConfig mConfig = new ACRCloudConfig();
            mConfig.acrcloudListener = this;
            mConfig.context = CondomContext.wrap(this, "ACRCloud");
            mConfig.host = BuildConfig.acrCloudHost;
            mConfig.accessKey = BuildConfig.acrCloudKey;
            mConfig.accessSecret = BuildConfig.acrCloudSecret;
            mConfig.reqMode = ACRCloudConfig.ACRCloudRecMode.REC_MODE_REMOTE;
            mClient = new ACRCloudClient();
            initState = mClient.initWithConfig(mConfig);
        }

        if (!mProcessing) {
            mProcessing = true;
            if (this.mClient == null || !this.mClient.startRecognize()) {
                stopACRCloud();
            }
        }
    }

    @DebugLog
    private void stopACRCloud() {

        if (mProcessing && this.mClient != null) {
            this.mClient.stopRecordToRecognize();
        }
        TrackModel previousTrackModel = new TrackModel();
        TrackModel newTrackModel = new TrackModel();
        mProcessing = false;
    }

    @DebugLog
    @Override
    public void onCoverArtDownloadSuccess(TrackModel trackModel) {

        if (acrIdentifyJob) {
            acrIdentifyJob = false;
            EventBus.getDefault(this).postLocal(new ACREvent(true, trackModel));
            stopACRCloud();
        } else {
            PlayerConstants.QUEUE_SONG.setAcrid(trackModel.getAcrid());
            TrackRealmHelper.updateACRKey(PlayerConstants.QUEUE_SONG);
            saveFingerprints(this);

            creatorShare =
                    new MostPlayed(
                            trackModel.getAcrid(),
                            trackModel.getTitle(),
                            trackModel.getArtist_name(),
                            trackModel.getAlbum_name(),
                            trackModel.getDuration(),
                            trackModel.getDate(),
                            trackModel.getVideoId(),
                            trackModel.getCoverUrl(),
                            1);
            FirebaseManager.Instance().getMostPlayedRef().child(trackModel.getAcrid()).runTransaction(new Transaction.Handler() {
                @Override
                public Transaction.Result doTransaction(MutableData mutableData) {
                    //Post p = mutableData.getValue(Post.class);
                    // above line didn't work again, so used the same workaround
                    MostPlayed mostPlayed = mutableData.getValue(MostPlayed.class);
                    if (mostPlayed == null) {
                        mutableData.setValue(creatorShare);
                    } else {
                        mostPlayed.setPlayed(mostPlayed.getPlayed() + 1);
                        mutableData.setValue(mostPlayed);
                    }

                    return Transaction.success(mutableData);
                }

                @Override
                public void onComplete(DatabaseError databaseError, boolean b,
                                       DataSnapshot dataSnapshot) {
                    // Transaction completed
                    if (databaseError != null) {
                        Crashlytics.logException(databaseError.toException());
                    }
                    stopACRCloud();
                }
            });
        }
    }

    @DebugLog
    @Override
    public void onCoverArtDownloadError(TrackModel trackModel) {

        if (acrIdentifyJob) {
            acrIdentifyJob = false;
            EventBus.getDefault(this).postLocal(new ACREvent(true, trackModel));
            stopACRCloud();
        } else {
            PlayerConstants.QUEUE_SONG.setAcrid(trackModel.getAcrid());
            TrackRealmHelper.updateACRKey(PlayerConstants.QUEUE_SONG);

            saveFingerprints(this);

            creatorShare =
                    new MostPlayed(
                            trackModel.getAcrid(),
                            trackModel.getTitle(),
                            trackModel.getArtist_name(),
                            trackModel.getAlbum_name(),
                            trackModel.getDuration(),
                            trackModel.getDate(),
                            trackModel.getVideoId(),
                            trackModel.getCoverUrl(),
                            1);
            FirebaseManager.Instance().getMostPlayedRef()
                    .child(trackModel.getAcrid())
                    .runTransaction(
                            new Transaction.Handler() {
                                @Override
                                public Transaction.Result doTransaction(MutableData mutableData) {
                                    //Post p = mutableData.getValue(Post.class);
                                    // above line didn't work again, so used the same workaround
                                    mostPlayed = mutableData.getValue(MostPlayed.class);
                                    if (mostPlayed == null) {
                                        mutableData.setValue(creatorShare);
                                    } else {
                                        mostPlayed.setPlayed(mostPlayed.getPlayed() + 1);
                                        mutableData.setValue(mostPlayed);
                                    }

                                    return Transaction.success(mutableData);
                                }

                                @Override
                                public void onComplete(
                                        DatabaseError databaseError,
                                        boolean b,
                                        DataSnapshot dataSnapshot) {
                                    // Transaction completed
                                    if (databaseError != null) {
                                        Crashlytics.logException(databaseError.toException());
                                    }
                                    stopACRCloud();
                                }
                            });
        }
    }

    @DebugLog
    private void saveFingerprints(Context context) {

        try {

            ArrayList<TrackFingerprint> trackFingerprints = TrackRealmHelper.getTrackFingerprints();
            if (trackFingerprints != null) {

                FirebaseManager.Instance().getTracksRef()
                        .child(AppController.Instance().getAndroidID())
                        .setValue(
                                trackFingerprints,
                                (error, firebase) -> {
                                    if (error != null) {
                                        AppController.toast(context, "Network connection failed");
                                    }
                                });
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    @Override
    public void onResult(String result) {

        if (this.mClient != null) {
            this.mClient.cancel();
            mProcessing = false;
        }

        trackModel = new TrackModel();

        try {
            JSONObject j = new JSONObject(result);
            JSONObject j1 = j.getJSONObject("status");
            int j2 = j1.getInt("code");
            if (j2 == 0) {

                acrCloudModel = GsonManager.Instance().getGson().fromJson(result, AcrCloudModel.class);
                List<Music> musicList;
                List<AcrArtistFour> artists;

                musicList = acrCloudModel.getMetadata().getMusic();
                Music music = musicList.get(0);
                artists = music.getArtists();
                AcrArtistFour artist_ = artists.get(0);


                trackModel.setAcrid(music.getAcrid());
                trackModel.setTitle(music.getTitle());
                trackModel.setArtist_name(artist_.getName());
                trackModel.setAlbum_name(music.getAlbum().getName());
                trackModel.setDuration(Integer.parseInt(music.getDurationMs()));
                trackModel.setPosition(music.getPlayOffsetMs());
                if (music.getExternalMetadata().getYoutube() != null) {
                    trackModel.setVideoId(music.getExternalMetadata().getYoutube().getVid());
                }
            }
        } catch (JSONException e) {
            Crashlytics.logException(e);
            EventBus.getDefault(this).postLocal(new ACREvent(false, trackModel));
        }

        if (trackModel.title != null) {

            PlayerConstants.QUEUE_SONG.setAcrid(trackModel.getAcrid());
            TrackRealmHelper.updateACRKey(PlayerConstants.QUEUE_SONG);

            artworkHelper = new ArtworkHelper();
            artworkHelper.autoPickAlbumArtTrackModel(this, trackModel, this);


        } else {
            stopACRCloud();
            EventBus.getDefault(this).postLocal(new ACREvent(false, trackModel));
            Answers.getInstance()
                    .logCustom(
                            new CustomEvent("ACRCloud Event")
                                    .putCustomAttribute("Message", result));
        }
    }

    @Override
    public void onVolumeChanged(double v) {

    }

    @DebugLog
    private final class ServiceHandler extends Handler {

        public ServiceHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            // Well calling mServiceHandler.sendMessage(message); from onStartCommand,
            // this method will be called.

            if (msg.arg1 == trackExists) {
                QueueItem queueItem = TrackRealmHelper.getTrack(PlayerConstants.QUEUE_SONG.data);

                FirebaseManager.Instance().getMostPlayedRef().child(queueItem.getAcrid()).runTransaction(new Transaction.Handler() {
                    @Override
                    public Transaction.Result doTransaction(MutableData mutableData) {

                        MostPlayed mostPlayed = mutableData.getValue(MostPlayed.class);
                        if (mostPlayed != null) {
                            mostPlayed.setPlayed(mostPlayed.getPlayed() + 1);
                            mutableData.setValue(mostPlayed);
                        }

                        return Transaction.success(mutableData);
                    }

                    @Override
                    public void onComplete(DatabaseError databaseError, boolean b,
                                           DataSnapshot dataSnapshot) {
                        // Transaction completed
                        if (databaseError != null) {
                            Crashlytics.logException(databaseError.toException());
                        }
                        stopACRCloud();
                    }
                });
            } else {
                startACRCloud();
            }
            // Add your cpu-blocking activity here
        }
    }
}