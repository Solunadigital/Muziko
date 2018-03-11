package com.muziko.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.bluelinelabs.logansquare.LoganSquare;
import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.answers.Answers;
import com.crashlytics.android.answers.CustomEvent;
import com.github.pwittchen.reactivenetwork.library.rx2.ReactiveNetwork;
import com.google.gson.reflect.TypeToken;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.MainActivity;
import com.muziko.api.LastFM.Utils.AppSettings;
import com.muziko.api.LastFM.Utils.InternalTrackTransmitter;
import com.muziko.api.LastFM.Utils.MusicAPI;
import com.muziko.api.LastFM.Utils.Track;
import com.muziko.api.LastFM.services.ScrobblingService;
import com.muziko.common.CommonConstants;
import com.muziko.common.controls.MuzikoArrayList;
import com.muziko.common.events.NetworkEvent;
import com.muziko.common.events.PlayerPreparedEvent;
import com.muziko.common.events.ProgressEvent;
import com.muziko.common.events.RefreshEvent;
import com.muziko.common.events.buswear.QueueFullUpdateEvent;
import com.muziko.common.events.buswear.QueueProgressEvent;
import com.muziko.common.events.buswear.QueueUpdateEvent;
import com.muziko.common.events.buswear.RequestQueueUpdateEvent;
import com.muziko.common.events.buswear.WearActionEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.Queuelist;
import com.muziko.controls.RecursiveFileObserver;
import com.muziko.database.CloudAccountRealmHelper;
import com.muziko.database.TrackRealmHelper;
import com.muziko.events.AudioEvent;
import com.muziko.events.BufferingEvent;
import com.muziko.events.LoadQueueEvent;
import com.muziko.events.StreamProgressEvent;
import com.muziko.helpers.FileHelper;
import com.muziko.helpers.QueueHelper;
import com.muziko.helpers.Utils;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.GsonManager;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.MuzikoConstants;
import com.muziko.manager.NotificationController;
import com.muziko.manager.PrefsManager;
import com.muziko.manager.SettingsManager;
import com.muziko.manager.ThreadManager;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.models.CloudAccount;
import com.muziko.receivers.BluetoothReceiver;
import com.muziko.receivers.HeadsetReceiver;
import com.muziko.receivers.NotificationBroadcast;
import com.muziko.tasks.FirebaseDownloadTask;
import com.muziko.tasks.FirebaseUploadTask;
import com.muziko.tasks.RecentAdd;
import com.muziko.tasks.ThumbLoader;
import com.muziko.widgets.QueueWidget;
import com.muziko.widgets.StandardWidget;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import hugo.weaving.DebugLog;
import io.reactivex.schedulers.Schedulers;
import pl.tajchert.buswear.EventBus;
import pl.tajchert.buswear.events.ImageAssetEvent;

import static android.os.FileObserver.CREATE;
import static android.os.FileObserver.DELETE_SELF;
import static com.muziko.MyApplication.IMAGE_LARGE_SIZE;
import static com.muziko.MyApplication.hasWifi;
import static com.muziko.MyApplication.networkState;
import static com.muziko.MyApplication.notificationActive;
import static com.muziko.manager.MuzikoConstants.FIREBASE_READY_INTERVAL;
import static com.muziko.manager.MuzikoConstants.PROGRESS_UPDATE_INTERVAL;
import static com.muziko.manager.MuzikoConstants.REFRESH_CLOUD_DRIVES_INTERVAL;
import static com.muziko.manager.MuzikoConstants.SCAN_DOWNLOADS_INTERVAL;
import static com.muziko.manager.MuzikoConstants.extensions;

public class SongService extends Service implements AudioManager.OnAudioFocusChangeListener, NotificationController.onNoticationCacheListener, RecursiveFileObserver.EventListener, SensorEventListener {
    public static final String ARG_INDEX = "index";
    public static final String ARG_TYPE = "type";
    public static final String ARG_DATA = "data";
    public static final String ARG_HASH = "hash";
    public static final String ARG_WIDGET = "widget";
    public static final String ARG_NOTIFICATION_UPDATE_TYPE = "notify_update_type";
    public static final String ACTION_SEEK = "seek";
    public static final String ACTION_UNQUEUE = "unqueue";
    public static final String ACTION_EQUALIZER = "equalizer";
    public static final String ACTION_LOCKSCREEN = "lockscreen";
    public static final String ACTION_NOTIFICATION = "notification";
    public static final String ACTION_LOADED = "app_loaded";
    public static final String ACTION_NOTIFICATION_CACHE = "ACTION_NOTIFICATION_CACHE";
    public static final String ACTION_THUMB = "thumb";
    public static final String ACTION_TOGGLE = "toggle";
    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_RESUME = "resume";
    public static final String ACTION_STOP = "stop";
    public static final String ACTION_EXIT = "exit";
    public static final String ACTION_BACK = "back";
    public static final String ACTION_NEXT = "next";
    public static final String ACTION_PREV = "prev";
    public static final String ACTION_DIRTY = "dirty";
    public static final String ACTION_DELETE = "delete";
    public static final String ACTION_SAVE = "save";
    public static final String ACTION_CLEAR = "clear";
    public static final String ACTION_INDEX = "index";
    public static final String ACTION_GAPLESS = "gapless";
    public static final String ACTION_LISTEN_ACR = "LISTEN_ACR";
    public static final String NOTIFY_PREVIOUS = "com.muziko.audioplayer.previous";
    public static final String NOTIFY_DELETE = "com.muziko.audioplayer.delete";
    public static final String NOTIFY_PAUSE = "com.muziko.audioplayer.pause";
    public static final String NOTIFY_PLAY = "com.muziko.audioplayer.play";
    public static final String NOTIFY_PLAY_WIDGET = "com.muziko.audioplayer.playwidget";
    public static final String NOTIFY_NEXT = "com.muziko.audioplayer.next";
    public static final String NOTIFY_REPEAT = "com.muziko.audioplayer.repeat";
    public static final String NOTIFY_SHUFFLE = "com.muziko.audioplayer.shuffle";
    public static final String NOTIFY_PLAY_SONG = "com.muziko.audioplayer.playsong";
    public static final String NOTIFY_PLAY_QUEUE_SONG = "com.muziko.audioplayer.playqueuesong";
    public static final String UPDATER_NOTIFY_DISMISS = "com.muziko.updater.dismiss";
    public static final String UPDATER_NOTIFY_DISMISS_ALWAYS = "com.muziko.updater.dismissalways";
    public static final String UPDATER_NOTIFY_UPDATE = "com.muziko.updater.update";
    public static final String SLEEP_TIMER_STOP = "STOP_TIMER";
    public static final String ACTION_SLEEP_TIME = "TIME";
    public static final String ACTION_SLEEP_STOP = "STOP";
    public static final int NOTIFICATION_SHUFFLE = 1;
    public static final int NOTIFICATION_REPEAT = 2;
    public static final LinkedHashMap<String, Notification> notificationCache = new LinkedHashMap<>();
    public static final String NOTIFY_CONNECT = "NOTIFY_CONNECT";
    public static final String NOTIFY_CANCEL_CLOUD_DOWNLOAD = "com.muziko.cloud.cancelDownload";
    public static final String NOTIFY_CANCEL_CLOUD_UPLOAD = "com.muziko.cloud.cancelUpload";
    private static final String TAG = SongService.class.getName();
    private static final int NOTIFICATION_ID = 1111;
    private static final String ACTION_FOCUS = "focus";
    private static final String ACTION_TIMER = "timer";
    public static boolean currentVersionSupportBigNotification = false;
    public static NotificationCompat.Builder notiBuilder;
    private static RemoteControlClient remoteControlClient = null;
    private static boolean currentVersionSupportLockScreenControls = false;
    private final List<File> mfiles = new ArrayList<>();
    private final WeakHandler handler = new WeakHandler();
    private final int MAX_COUNT_GZ_CHANGE = 10;
    private final boolean wearConnected = false;
    private NotificationBroadcast notificationBroadcast;
    private final Runnable queueSaverRunnable =
            () -> {
                try {
                    if (PlayerConstants.QUEUE_SAVING) return;

                    long start = System.currentTimeMillis();
                    PlayerConstants.QUEUE_SAVING = true;
                    long level = PrefsManager.Instance().getQueueLevel() + 1;
                    PrefsManager.Instance().setQueueLevel(level);

                    QueueHelper.saveQueue(SongService.this);

                    PlayerConstants.QUEUE_SAVING = false;
                    PlayerConstants.QUEUE_DIRTY = 0;

                    long end = System.currentTimeMillis();
                    long duration = end - start;
                    Log.i(TAG, "QueueSaver completed in " + String.valueOf(duration) + " ms");
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private final Runnable connectCloudDrivesRunnable =
            () -> {
                try {
                    Log.i(TAG, "connectCloudDrivesRunnable started");
                    if (CloudAccountRealmHelper.getCloudAccounts().size() > 0) {
                        for (CloudAccount cloudAccount : CloudAccountRealmHelper.getCloudAccounts()) {
                            CloudManager.Instance().getCloudDrive(SongService.this, cloudAccount.getCloudAccountId());
                        }
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private final Runnable refreshCloudDrivesRunnable =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (((MyApplication) getApplication()).isForeground()) {
                            Log.i(TAG, "refreshCloudDrivesRunnable started");
                            CloudManager.Instance().refreshCloudDrives();
                        }
                        ThreadManager.Instance().submitToContinuousThreadPool(this, REFRESH_CLOUD_DRIVES_INTERVAL);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }
            };
    private final Runnable checkforTransfersRunnable =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (((MyApplication) getApplication()).isForeground()) {
                            Log.i(TAG, "checkforTransfersRunnable started");
                            FirebaseManager.Instance().checkforTransfers();

                            if (!FirebaseManager.Instance().isDownloadRunning()) {
                                for (FirebaseDownloadTask firebaseDownloadTask : FirebaseManager.Instance().getFirebaseDownloadTasks().values()) {
                                    if (firebaseDownloadTask.getStatus() == AsyncTask.Status.PENDING) {
                                        firebaseDownloadTask.executeOnExecutor(ThreadManager.Instance().getMuzikoTransferPool());
                                        break;
                                    }
                                }
                            }

                            if (!FirebaseManager.Instance().isUploadRunning()) {
                                for (FirebaseUploadTask firebaseUploadTask : FirebaseManager.Instance().getFirebaseUploadTasks().values()) {
                                    if (FirebaseManager.Instance().checkShouldUpload(firebaseUploadTask.getQueueItem()) && firebaseUploadTask.getStatus() == AsyncTask.Status.PENDING)
                                        firebaseUploadTask.executeOnExecutor(ThreadManager.Instance().getMuzikoTransferPool());
                                    break;
                                }
                            }
                        }
                        ThreadManager.Instance().submitToContinuousThreadPool(this, FIREBASE_READY_INTERVAL);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }
            };
    private final Runnable updateWidgetsRunnable =
            () -> {
                try {
                    StandardWidget standardWidget = new StandardWidget();
                    standardWidget.onUpdate(getApplicationContext());
                    QueueWidget queueWidget = new QueueWidget();
                    queueWidget.onUpdate(getApplicationContext());
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private final Runnable queueLoaderRunnable =
            () -> {
                try {
                    Log.i(TAG, "queueLoaderRunnable started");
                    if (PlayerConstants.QUEUE_LIST.size() == 0) {
                        QueueHelper.loadQueue(this);
                    }

                    if (PlayerConstants.QUEUE_LIST.size() == 0) {
                        PlayerConstants.QUEUE_INDEX = 0;
                        PlayerConstants.QUEUE_TIME = 0;
                        PlayerConstants.QUEUE_SONG = new QueueItem();
                    } else {
                        PlayerConstants.QUEUE_TIME = PrefsManager.Instance().getPlayPosition();
                        PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PAUSED;

                        //			ArrayList<RecentItem> recents = RecentItem.loadAll(0, 1);
                        if (TrackRealmHelper.getMostRecentlyPlayed() != null) {
                            //				RecentItem item = recents.get(0);
                            QueueItem queue = TrackRealmHelper.getMostRecentlyPlayed();

                            PlayerConstants.QUEUE_SONG = queue;
                            PlayerConstants.QUEUE_DURATION = Utils.getInt(queue.duration, 0);

                        } else {
                            PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(0);
                        }

                        for (int i = 0; i < PlayerConstants.QUEUE_LIST.size(); i++) {
                            if (PlayerConstants.QUEUE_LIST.get(i).data.equals(PlayerConstants.QUEUE_SONG.data)) {
                                PlayerConstants.QUEUE_INDEX = i;
                                PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(i);
                                break;
                            }
                        }
                    }

                    if (!PlayerConstants.QUEUE_SONG.data.isEmpty()) {
                        if (PlayerConstants.QUEUE_SONG.storage == 1
                                || PlayerConstants.QUEUE_SONG.storage == 2) {
                            MuzikoExoPlayer.Instance().setDataSource(PlayerConstants.QUEUE_SONG.data);
                        }
                        //            MuzikoExoPlayer.Instance().prepare();
                        if (PlayerConstants.QUEUE_TIME > 0
                                && PlayerConstants.QUEUE_TIME < PlayerConstants.QUEUE_DURATION) {
                            MuzikoExoPlayer.Instance().seekTo(PlayerConstants.QUEUE_TIME);
                        }
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private final Runnable queueLoaderFirstTimeRunnable =
            () -> {
                try {
                    Log.i(TAG, "queueLoaderFirstTimeRunnable started");
                    if (PlayerConstants.QUEUE_LIST.size() == 0) {
                        QueueHelper.loadQueue(this);
                    }

                    if (PlayerConstants.QUEUE_LIST.size() == 0) {
                        PlayerConstants.QUEUE_INDEX = 0;
                        PlayerConstants.QUEUE_TIME = 0;
                        PlayerConstants.QUEUE_SONG = new QueueItem();
                    } else {
                        PlayerConstants.QUEUE_TIME = PrefsManager.Instance().getPlayPosition();
                        PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PAUSED;

                        //			ArrayList<RecentItem> recents = RecentItem.loadAll(0, 1);
                        if (TrackRealmHelper.getMostRecentlyPlayed() != null) {
                            //				RecentItem item = recents.get(0);
                            QueueItem queue = TrackRealmHelper.getMostRecentlyPlayed();

                            PlayerConstants.QUEUE_SONG = queue;
                            PlayerConstants.QUEUE_DURATION = Utils.getInt(queue.duration, 0);

                        } else {
                            PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(0);
                        }

                        for (int i = 0; i < PlayerConstants.QUEUE_LIST.size(); i++) {
                            if (PlayerConstants.QUEUE_LIST.get(i).data.equals(PlayerConstants.QUEUE_SONG.data)) {
                                PlayerConstants.QUEUE_INDEX = i;
                                PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(i);
                                break;
                            }
                        }
                    }

                    if (!PlayerConstants.QUEUE_SONG.data.isEmpty()) {
                        if (PlayerConstants.QUEUE_SONG.storage == 1
                                || PlayerConstants.QUEUE_SONG.storage == 2) {
                            MuzikoExoPlayer.Instance().setDataSource(PlayerConstants.QUEUE_SONG.data);
                        }
                        //            MuzikoExoPlayer.Instance().prepare();
                        if (PlayerConstants.QUEUE_TIME > 0
                                && PlayerConstants.QUEUE_TIME < PlayerConstants.QUEUE_DURATION) {
                            MuzikoExoPlayer.Instance().seekTo(PlayerConstants.QUEUE_TIME);
                        }
                        sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private final Runnable updateNotificationRunnable =
            () -> {
                try {
                    if (!notificationActive) {
                        return;
                    }

                    if (PlayerConstants.QUEUE_LIST.size() > 0) {
                        Notification savedNotification = notificationCache.get(PlayerConstants.QUEUE_SONG.data);
                        if (savedNotification == null) {
                            savedNotification = NotificationController.Instance().getNotificationNotInCache(PlayerConstants.QUEUE_INDEX, PlayerConstants.QUEUE_SONG);
                        }
                        Notification newNotification = NotificationController.Instance().updateNotification(PlayerConstants.QUEUE_INDEX, PlayerConstants.QUEUE_SONG, savedNotification);
                        startForeground(NOTIFICATION_ID, newNotification);
                    } else {
                        stopForeground(true);
                    }
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private final Runnable notificationCacheRunnable =
            () -> {
                try {
                    long start = System.currentTimeMillis();
                    NotificationController.Instance().createNotificationCache(this);
                    long end = System.currentTimeMillis();
                    long duration = end - start;
                    Log.i(TAG, "NotificationUpdater completed in " + String.valueOf(duration) + " ms");
                    Answers.getInstance()
                            .logCustom(
                                    new CustomEvent(getString(R.string.notification_cache))
                                            .putCustomAttribute("Songs", PlayerConstants.QUEUE_LIST.size())
                                            .putCustomAttribute("Time", duration));
                } catch (Exception e) {
                    Crashlytics.logException(e);
                }
            };
    private SensorManager mSensorManager;
    private float mGZ = 0; //gravity acceleration along the z axis
    private int mEventCountSinceGZChanged = 0;
    private String prefFaceDown;
    private String prefFaceUp;
    private boolean shouldIdentify = false;
    private boolean change = false;
    private final Runnable fileScannerRunnable =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (((MyApplication) getApplication()).isForeground()) {
                            change = false;
                            mfiles.clear();
                            mfiles.addAll(
                                    FileUtils.listFiles(
                                            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
                                            MuzikoConstants.extensions,
                                            true));
                            for (final File file : mfiles) {
                                QueueItem existingTrack = TrackRealmHelper.getTrack(file.getAbsolutePath());
                                if (existingTrack == null) {
                                    change = true;
                                    MediaHelper.Instance().loadMusicFromTrackAsync(file.getAbsolutePath(), false);
                                }
                            }

                            if (!MyApplication.pauseDeletingTempRingtone) {
                                final String outPath =
                                        FileHelper.makeRingtoneFilename(
                                                SongService.this, SongService.this.getString(R.string.au_clip));
                                final File deleteOldFile = new File(outPath);
                                if (deleteOldFile.exists()) {
                                    deleteOldFile.delete();
                                }
                            }
                            if (change) {
                                change = false;
                                EventBus.getDefault(SongService.this).postLocal(new RefreshEvent(1000));
                            }
                        }
                        ThreadManager.Instance().submitToContinuousThreadPool(this, SCAN_DOWNLOADS_INTERVAL);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }
            };
    private android.app.NotificationManager mNotificationManager;
    private AudioManager audioManager;
    private int FOCUS_LOSS_TYPE = 0;
    private boolean hasFocus = false;
    private boolean playInterrupt = false;
    private SongServiceMetadataTarget metadataTarget = null;
    private SongServicePhoneStateListener phoneListener = null;
    private TelephonyManager mTelephony;
    private ComponentName remoteComponentName;
    private MainReceiver mainReceiver;
    private HeadsetReceiver headsetReceiver;
    private BluetoothReceiver bluetoothReceiver;
    private boolean wearUpdating = false;
    private MediaObserver mediaObserver;
    private Bitmap PicassoBitmap;
    private final Target target =
            new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    // loading of the bitmap was a success
                    AsyncJob.doInBackground(
                            () -> {
                                EventBus.getDefault(SongService.this)
                                        .postRemote(new ImageAssetEvent(bitmap, PlayerConstants.QUEUE_SONG.data));
                            });
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    // loading of the bitmap failed
                    AsyncJob.doInBackground(
                            () -> {
                                PicassoBitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.placeholder);
                                EventBus.getDefault(SongService.this)
                                        .postRemote(
                                                new ImageAssetEvent(PicassoBitmap, PlayerConstants.QUEUE_SONG.data));
                            });
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                }
            };
    private MuzikoArrayList<QueueItem> lastQueue = new MuzikoArrayList<>();
    private final Runnable mediaProgressRunnable =
            new Runnable() {
                @Override
                public void run() {
                    try {
                        if (MuzikoExoPlayer.Instance().isPlaying() && PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
                            sendMsgToUI(PlayerConstants.QUEUE_INDEX, MuzikoExoPlayer.Instance().getCurrentPosition(), MuzikoExoPlayer.Instance().getDuration());
                            PlayerConstants.QUEUE_TIME = MuzikoExoPlayer.Instance().getCurrentPosition();

//                            if (shouldIdentify && MuzikoExoPlayer.Instance().getCurrentPosition() > 15000) {
//                                shouldIdentify = false;
//                                AppController.Instance().serviceACR();
//                            }

                            if (SettingsManager.Instance().getPrefs().getBoolean(SettingsManager.prefGapless, false)) {
                                if (PlayerConstants.QUEUE_DURATION - PlayerConstants.QUEUE_TIME < 2000
                                        && !MuzikoExoPlayer.Instance().hasNextMediaPlayer()) {
                                    MuzikoExoPlayer.Instance().createNextMediaPlayer();
                                }
                            }

                            songSleeper();

                            if (AppController.Instance().getCurrentActivity() instanceof MainActivity) {
                                EventBus.getDefault(getApplicationContext()).postLocal(new AudioEvent(true));
                            }

                            if (!lastQueue.equals(PlayerConstants.QUEUE_LIST)) {
                                lastQueue.clear();
                                lastQueue.addAll(PlayerConstants.QUEUE_LIST);

                                if (PlayerConstants.QUEUE_DIRTY != 0
                                        && System.currentTimeMillis() > PlayerConstants.QUEUE_DIRTY) {
                                    ThreadManager.Instance().submitToBackgroundThreadPool(queueSaverRunnable);
                                }
                            }

                        } else {

                            sendMsgToUI(PlayerConstants.QUEUE_INDEX, PlayerConstants.QUEUE_TIME, Integer.valueOf(PlayerConstants.QUEUE_SONG.duration));

                            if (audioManager != null && AppController.Instance().getCurrentActivity() instanceof MainActivity) {
                                if (audioManager.isMusicActive()) {
                                    EventBus.getDefault(getApplicationContext()).postLocal(new AudioEvent(true));
                                } else {
                                    EventBus.getDefault(getApplicationContext())
                                            .postStickyLocal(new AudioEvent(false));
                                }
                            }
                        }

                        wearProgressUpdate();

                        ThreadManager.Instance().submitToContinuousThreadPool(this, PROGRESS_UPDATE_INTERVAL);
                    } catch (Exception e) {
                        Crashlytics.logException(e);
                        ThreadManager.Instance().submitToContinuousThreadPool(this, PROGRESS_UPDATE_INTERVAL);
                    }
                }
            };
    private NetworkInfo.State oldNetworkState;

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();

        if (!MyApplication.loaded) {
            MyApplication.getInstance().load(this);
        }

        mNotificationManager =
                (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (android.os.Build.VERSION.SDK_INT>= Build.VERSION_CODES.O) {
            CharSequence name = "Muziko";
            int importance = NotificationManager.IMPORTANCE_LOW;

            NotificationChannel mChannel = new NotificationChannel("Muziko", name, importance);
            mNotificationManager.createNotificationChannel(mChannel);
        }




        notiBuilder = new NotificationCompat.Builder(getApplicationContext(), "Muziko");


        currentVersionSupportBigNotification = Utils.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = Utils.currentVersionSupportLockScreenControls();


        // Initialize the intent filter and each action
        final IntentFilter filter = new IntentFilter();
        filter.addAction(NOTIFY_DELETE);
        filter.addAction(NOTIFY_PAUSE);
        filter.addAction(NOTIFY_NEXT);
        filter.addAction(NOTIFY_PLAY);
        filter.addAction(NOTIFY_PLAY_WIDGET);
        filter.addAction(NOTIFY_PREVIOUS);
        filter.addAction(NOTIFY_REPEAT);
        filter.addAction(NOTIFY_SHUFFLE);
        filter.addAction(NOTIFY_PLAY_SONG);
        filter.addAction(NOTIFY_PLAY_QUEUE_SONG);
        filter.addAction(Intent.ACTION_MEDIA_BUTTON);

        filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        try{
            notificationBroadcast=new NotificationBroadcast();
            // Attach the broadcast listener
            registerReceiver(notificationBroadcast, filter);

        }catch (Exception e){

        }



        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        audioManager.setSpeakerphoneOn(true);

        remoteComponentName =
                new ComponentName(getApplicationContext(), notificationBroadcast.ComponentName());

        phoneListener = new SongServicePhoneStateListener();

        mTelephony = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);

        registerRemoteClient();

        IntentFilter filters = new IntentFilter();
        filter.addAction(AppController.INTENT_TRACK_EDITED);

        mainReceiver = new MainReceiver();
        registerReceiver(mainReceiver, filters);





        //        PlayerConstants.QUEUE_LIST.setChangeListener(this);

        mediaObserver = new MediaObserver(ThreadManager.Instance().getMuzikoMediaObserverHandler());

        IntentFilter headsetfilter = new IntentFilter();
        headsetfilter.addAction(Intent.ACTION_HEADSET_PLUG);
        headsetReceiver = new HeadsetReceiver();
        registerReceiver(headsetReceiver, headsetfilter);

        IntentFilter bluetoothfilter = new IntentFilter();
        bluetoothfilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        bluetoothfilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        bluetoothfilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        bluetoothReceiver = new BluetoothReceiver();
        registerReceiver(bluetoothReceiver, bluetoothfilter);

        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_UI,
                ThreadManager.Instance().getMuzikoMediaObserverHandler());

        observeNetworkConnectivity();
try {
    ThreadManager.Instance().submitToAppStartThreadPool(queueLoaderFirstTimeRunnable);
    ThreadManager.Instance().submitToAppStartThreadPool(connectCloudDrivesRunnable);
    ThreadManager.Instance().submitToAppStartThreadPool(notificationCacheRunnable);
    ThreadManager.Instance().submitToAppStartThreadPool(updateNotificationRunnable);
    ThreadManager.Instance().shutdownAppStartPool();

    ThreadManager.Instance().submitToContinuousThreadPool(mediaProgressRunnable, 5000);
    ThreadManager.Instance().submitToContinuousThreadPool(fileScannerRunnable, 20000);
    ThreadManager.Instance().submitToContinuousThreadPool(refreshCloudDrivesRunnable, 30000);
    ThreadManager.Instance().submitToContinuousThreadPool(checkforTransfersRunnable, 10000);
    getContentResolver()
            .registerContentObserver(
                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, true, mediaObserver);

    EventBus.getDefault(this).register(this);
}catch (Exception e){
    e.printStackTrace();
}
    }

    @DebugLog
    @SuppressLint("NewApi")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if (intent != null) {
            String action = intent.getAction();
            if (action != null) {

                if (action.equalsIgnoreCase(ACTION_THUMB)) {
                    thumbLoad();
                } else if (action.equalsIgnoreCase(ACTION_PLAY)) {
                    boolean widget = intent.getBooleanExtra(ARG_WIDGET, false);
                    if (PlayerConstants.QUEUE_INDEX < 0
                            || PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size()
                            || PlayerConstants.QUEUE_LIST.size() == 0) {
                        if (widget) {
                            ThreadManager.Instance().submitToBackgroundThreadPool(queueLoaderRunnable);
                        }
                    }
                    songPlay(true, false,false);
                } else if (action.equalsIgnoreCase(ACTION_RESUME)) {
                    boolean widget = intent.getBooleanExtra(ARG_WIDGET, false);

                    if (PlayerConstants.QUEUE_INDEX < 0
                            || PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size()
                            || PlayerConstants.QUEUE_LIST.size() == 0) {
                        if (widget) {
                            ThreadManager.Instance().submitToBackgroundThreadPool(queueLoaderRunnable);
                        }
                    }
                    if (widget) {
                        songPlay(false, false,false);
                    } else {
                        if (!songResume()) {
                            songPlay(false, false,false);
                        }
                    }
                } else if (action.equalsIgnoreCase(ACTION_PAUSE)) {
                    songPause();
                } else if (action.equalsIgnoreCase(ACTION_TOGGLE)) {
                    songToggle();
                } else if (action.equalsIgnoreCase(ACTION_SEEK)) {
                    int index = intent.getIntExtra(ARG_INDEX, -1);
                    if (index != -1) {
                        songSeek(index);
                    }
                } else if (action.equalsIgnoreCase(ACTION_PREV)) {
                    songPrev();
                } else if (action.equalsIgnoreCase(ACTION_BACK)) {
                    songBack();
                } else if (action.equalsIgnoreCase(ACTION_NEXT)) {
                    songNext();
                } else if (action.equalsIgnoreCase(ACTION_INDEX)) {
                    int index = intent.getIntExtra(ARG_INDEX, 0);
                    songIndex(index);
                } else if (action.equalsIgnoreCase(ACTION_FOCUS)) {
                    songFocus();
                } else if (action.equalsIgnoreCase(ACTION_STOP)) {
                    songStop();
                } else if (action.equalsIgnoreCase(ACTION_DELETE)) {
                    long type = intent.getLongExtra(ARG_TYPE, 0);
                    String hash = intent.getStringExtra(ARG_HASH);
                    String data = intent.getStringExtra(ARG_DATA);

                    songDelete(hash, data);
                } else if (action.equalsIgnoreCase(ACTION_DIRTY)) {
                    songDirty();
                } else if (action.equalsIgnoreCase(ACTION_CLEAR)) {
                    songClear();
                } else if (action.equalsIgnoreCase(ACTION_EXIT)) {
                    songExit();
                } else if (action.equalsIgnoreCase(ACTION_UNQUEUE)) {
                    String hash = intent.getStringExtra(ARG_DATA);

                    songUnqueue(hash);
                } else if (action.equalsIgnoreCase(ACTION_EQUALIZER)) {
                    int index = intent.getIntExtra(ARG_INDEX, 100);
                    if (index == 1) {
                        MuzikoExoPlayer.Instance().equalizerOn();
                    } else if (index == -1) {
                        MuzikoExoPlayer.Instance().equalizerOff();
                    } else if (index == 0) {
                        MuzikoExoPlayer.Instance().equalizerUpdate();
                    }
                } else if (action.equalsIgnoreCase(ACTION_NOTIFICATION)) {
                    ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
                } else if (action.equalsIgnoreCase(SongService.NOTIFY_CONNECT)) {
                    ThreadManager.Instance().submitToBackgroundThreadPool(connectCloudDrivesRunnable);
                } else if (action.equalsIgnoreCase(ACTION_LOADED)) {
                    //                    ThreadManager.Instance().submitToBackgroundThreadPool(queueLoaderRunnable);
                    ////                    ThreadManager.Instance()
                    ////                            .getMuzikoNotificationCacheHandler()
                    ////                            .postDelayed(notificationCacheRunnable, 10000);
                    //                    ThreadManager.Instance()
                    //                            .submitToBackgroundThreadPool(connectCloudDrivesRunnable);
                    //                    ThreadManager.Instance()
                    //                            .submitToContinuousThreadPool(mediaProgressRunnable, 5000);
                    //                    ThreadManager.Instance()
                    //                            .submitToContinuousThreadPool(fileScannerRunnable, 10000);
                    //                    getContentResolver()
                    //                            .registerContentObserver(
                    //                                    android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    //                                    true,
                    //                                    mediaObserver);
                } else if (action.equalsIgnoreCase(ACTION_LOCKSCREEN)) {
                    updateLockScreen(PlayerConstants.QUEUE_SONG, RemoteControlClient.PLAYSTATE_PLAYING);
                } else if (action.equalsIgnoreCase(ACTION_TIMER)) {

                    //					mediaPlayerObserverThread.interrupt();
                    //					if (mediaPlayerObserverThread == null || !mediaPlayerObserverThread.isAlive()) {
                    //						mediaPlayerObserverThread = new Thread(mediaPlayerObserver);
                    //						mediaPlayerObserverThread.start();
                    //					}

                } else if (action.equalsIgnoreCase(MyApplication.ACTION_UPDATE_CACHE)) {
                    String data = intent.getStringExtra(ARG_DATA);
                    ArrayList<QueueItem> queueItems =
                            GsonManager.Instance()
                                    .getGson()
                                    .fromJson(data, new TypeToken<List<QueueItem>>() {
                                    }.getType());
                    AsyncJob.doInBackground(
                            () -> {
                                try {
                                    NotificationController.Instance()
                                            .updateNotificationCacheforTracks(queueItems, this);
                                } catch (Exception e) {
                                    Crashlytics.logException(e);
                                }
                            });

                } else if (action.equalsIgnoreCase(ACTION_GAPLESS)) {
                    gaplessNext();
                } else {
                    songPlay(false, false,false);
                }
            }
        }
        return START_STICKY;
    }

    @DebugLog
    @Override
    public void onDestroy() {
        System.out.println("SERVICE STOP!");
        audioManager.requestAudioFocus(null, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        audioManager = null;

        if (mTelephony != null) {
            mTelephony.listen(new PhoneStateListener(), PhoneStateListener.LISTEN_NONE);
        }

        unregisterRemoteClient();

        songStop();

        if (PlayerConstants.QUEUE_DIRTY > 0) {
            songSave();
        }

        playClose();

        MyApplication.loaded = false;

        unregisterReceiver(notificationBroadcast);

        unregisterReceiver(mainReceiver);
        unregisterReceiver(headsetReceiver);
        unregisterReceiver(bluetoothReceiver);

        getContentResolver().unregisterContentObserver(mediaObserver);

        EventBus.getDefault(this).unregister(this);

        super.onDestroy();
    }

    @DebugLog
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    //@Override
    public void onTaskRemoved(Intent rootIntent) {
        //called when service closed with minimize. Don't want to close music player then

        //		cancelNotification();
        //        super.onTaskRemoved(rootIntent);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
    }

    @DebugLog
    private void observeNetworkConnectivity() {

        ReactiveNetwork.observeNetworkConnectivity(this)
                .subscribeOn(Schedulers.from(ThreadManager.Instance().getMuzikoBackgroundThreadPool()))
                .observeOn(Schedulers.from(ThreadManager.Instance().getMuzikoBackgroundThreadPool()))
                .subscribe(
                        connectivity -> {
                            oldNetworkState = networkState;
                            networkState = connectivity.getState();
                            int netType = connectivity.getType();
                            hasWifi = connectivity.getType() == ConnectivityManager.TYPE_WIFI;

                            NetworkEvent networkEvent = new NetworkEvent(0, netType);
                            EventBus.getDefault(SongService.this).postLocal(networkEvent);

                            if (networkState != NetworkInfo.State.CONNECTED && MuzikoExoPlayer.Instance().isStreaming()) {
                                BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.network_lost), false);
                                EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                            } else if (networkState == NetworkInfo.State.CONNECTED && MuzikoExoPlayer.Instance().isStreaming()) {
                                BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.network_lost), true);
                                EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                            }

                            if (oldNetworkState != networkState) {
                                FirebaseManager.Instance().checkforTransfers();
                            }
                        });
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onWearActionEvent(WearActionEvent wearActionEvent) {

        AppController.toast(SongService.this, wearActionEvent.getName());
        QueueItem queueItem = null;

        switch (wearActionEvent.getName()) {
            case CommonConstants.ACTION_WEAR_UPDATE:
                wearFullUpdate();
                break;
            case CommonConstants.ACTION_WEAR_TOGGLE:
                songToggle();
                break;
            case CommonConstants.ACTION_WEAR_PREV:
                songPrev();
                break;
            case CommonConstants.ACTION_WEAR_NEXT:
                songNext();
                break;
            case CommonConstants.ACTION_WEAR_REPEAT:
                PrefsManager.Instance().setPlayRepeat(wearActionEvent.getPosition());
                AppController.Instance().serviceNotification(SongService.NOTIFICATION_REPEAT);
                break;
            case CommonConstants.ACTION_WEAR_SHUFFLE:
                PrefsManager.Instance().setPlayShuffle(wearActionEvent.getPosition() != 0);
                AppController.Instance().serviceNotification(SongService.NOTIFICATION_SHUFFLE);
                break;
            case CommonConstants.ACTION_WEAR_SEEK:
                songSeek(wearActionEvent.getPosition());
                break;
            case CommonConstants.ACTION_WEAR_ADD_TO_QUEUE:
                queueItem = PlayerConstants.QUEUE_LIST.get(wearActionEvent.getPosition());
                AppController.Instance().addToQueue(SongService.this, queueItem, true);
                break;
            case CommonConstants.ACTION_WEAR_DELETE:
                queueItem = PlayerConstants.QUEUE_LIST.get(wearActionEvent.getPosition());
                PlayerConstants.QUEUE_TYPE = 0;
                PlayerConstants.QUEUE_LIST.remove(wearActionEvent.getPosition());
                AppController.Instance().serviceUnqueue(queueItem.hash);
                AppController.Instance().serviceDirty();
                break;
            case CommonConstants.ACTION_WEAR_PLAY:
                AppController.Instance().serviceIndex(wearActionEvent.getPosition());
                break;
            case CommonConstants.ACTION_WEAR_PLAY_NEXT:
                queueItem = PlayerConstants.QUEUE_LIST.get(wearActionEvent.getPosition());
                AppController.Instance().addToQueue(SongService.this, queueItem, true);
                break;
            case CommonConstants.ACTION_WEAR_SHUFFLE_ALL:
                ArrayList<QueueItem> tracks = new ArrayList<>();
                tracks.addAll(TrackRealmHelper.getTracks(0).values());
                long seed = System.nanoTime();
                Collections.shuffle(tracks, new Random(seed));
                AppController.Instance().play(PlayerConstants.QUEUE_TYPE_TRACKS, 0, tracks);
                break;
        }
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRequestUpdateEvent(RequestQueueUpdateEvent requestQueueUpdateEvent) {

        wearFullUpdate();
    }

    @DebugLog
    private void wearStateUpdate() {

        if (wearConnected) {
            EventBus.getDefault(SongService.this)
                    .postRemote(
                            new QueueUpdateEvent(
                                    PlayerConstants.QUEUE_INDEX,
                                    PlayerConstants.QUEUE_STATE,
                                    PrefsManager.Instance().getPlayShuffle(this) ? 1 : 0,
                                    PrefsManager.Instance().getPlayRepeat()));
            sendCoverArt();
        }
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBufferingEvent(BufferingEvent event) {
        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
    }


    @DebugLog
    private void sendCoverArt() {

        Picasso.with(this)
                .load("content://media/external/audio/albumart/" + PlayerConstants.QUEUE_SONG.album)
                .resize(MyApplication.IMAGE_LARGE_SIZE, MyApplication.IMAGE_LARGE_SIZE)
                .centerCrop()
                .into(target);
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPlayerPreparedEvent(PlayerPreparedEvent event) {

        songNotify();
        scrobbleTrack();
    }

    private void wearProgressUpdate() {

        if (wearConnected) {
            AsyncJob.doOnMainThread(
                    () -> {
                        EventBus.getDefault(SongService.this)
                                .postRemote(
                                        new QueueProgressEvent(
                                                PlayerConstants.QUEUE_TIME, PlayerConstants.QUEUE_DURATION));
                    });
        }
    }

    @DebugLog
    private void wearFullUpdate() {

        if (wearConnected) {
            if (wearUpdating) return;
            wearUpdating = true;

            AsyncJob.doOnMainThread(
                    () -> {
                        ArrayList<QueueItem> tempQueue = new ArrayList<>();
                        tempQueue.addAll(PlayerConstants.QUEUE_LIST);

                        ArrayList<QueueItem> queueItems = new ArrayList<>();
                        Queuelist queuelist = new Queuelist();

                        int packets = 0;
                        while (tempQueue.size() > 0) {
                            QueueItem queueItem = tempQueue.get(0);
                            queueItems.add(queueItem);
                            tempQueue.remove(0);
                            if (queueItems.size() == 10 || tempQueue.size() == 0) {
                                queuelist.setQueueItems(queueItems);
                                String json = null;
                                try {
                                    json = LoganSquare.serialize(queuelist);
                                } catch (IOException e) {
                                    Crashlytics.logException(e);
                                }
                                queueItems.clear();
                                EventBus.getDefault(SongService.this)
                                        .postRemote(
                                                new QueueFullUpdateEvent(
                                                        json,
                                                        PlayerConstants.QUEUE_INDEX,
                                                        PlayerConstants.QUEUE_STATE,
                                                        packets));
                                packets++;
                            }
                        }
                        AsyncJob.doOnMainThread(() -> wearUpdating = false);
                    });
        }
    }

    @DebugLog
    @Override
    public void onAudioFocusChange(int focusChange) {

        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                System.out.println("AUDIOFOCUS GAIN");
                if (FOCUS_LOSS_TYPE == 0) {
                    return;
                } else if (FOCUS_LOSS_TYPE == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK
                        || FOCUS_LOSS_TYPE == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT) {
                }
                hasFocus = true;
                FOCUS_LOSS_TYPE = 0;
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
                System.out.println("AUDIOFOCUS LOSS");
                FOCUS_LOSS_TYPE = AudioManager.AUDIOFOCUS_LOSS;
                hasFocus = false;
                if (audioManager == null) return;

                //songPause();

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                System.out.println("AUDIOFOCUS LOSS TRANSIENT");
                FOCUS_LOSS_TYPE = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT;
                hasFocus = false;

                //songPause();

                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                System.out.println("AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK");
                FOCUS_LOSS_TYPE = AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK;
                hasFocus = false;

                //songPause();

                break;
        }
    }

    @DebugLog
    private void thumbLoad() {
        new ThumbLoader(this, null).executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    private void songStart(boolean userChange,boolean ispreview) {
        if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
            songPlay(userChange, false,ispreview);
        } else {
            songShow(userChange);
        }
    }

    @DebugLog
    private void gaplessNext() {
        //		mediaPlayerObserverThread.interrupt();
        //		if (mediaPlayerObserverThread == null || !mediaPlayerObserverThread.isAlive()) {
        //			mediaPlayerObserverThread = new Thread(mediaPlayerObserver);
        //			mediaPlayerObserverThread.start();
        //		}
        playInterrupt = false;
        PrefsManager.Instance().setQueueWidgetChange(true);
        PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PLAYING;
        songNotify();
        PlayerConstants.QUEUE_DURATION = MuzikoExoPlayer.Instance().getDuration();
        updateRecent(PlayerConstants.QUEUE_SONG);
        sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
        if (!hasFocus) {
            songFocus();
        }
        scrobbleTrack();
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onStreamProgressEvent(StreamProgressEvent streamProgressEvent) {
        if (streamProgressEvent.getAction() == 0) {
            PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PLAYING;
            sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
        } else if (streamProgressEvent.getAction() == 1) {
            songPlay(false, true,false);
        }
    }

    @DebugLog
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onLoadQueueEvent(LoadQueueEvent loadQueueEvent) {
        ThreadManager.Instance().submitToBackgroundThreadPool(queueLoaderRunnable);
    }

    @DebugLog
    private void songPlay(boolean userChange, boolean streamPlay,boolean isPre) {

        Log.i(TAG, "songPlay");

        playStop();

        if (PlayerConstants.QUEUE_INDEX < 0 || PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size() || PlayerConstants.QUEUE_LIST.size() == 0) {
            return;
        }

        try {

            playInterrupt = false;

            PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(PlayerConstants.QUEUE_INDEX);
            PrefsManager.Instance().setQueueWidgetChange(true);

            if (PlayerConstants.QUEUE_SONG.album == CloudManager.BOX && !streamPlay) {
                MuzikoExoPlayer.Instance().setStreaming(true);
                BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.buffering_song), false);
                AppController.isBuffering = true;
                EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                CloudManager.Instance().streamBoxSong(this, PlayerConstants.QUEUE_SONG);
                return;
            }

            if (MuzikoExoPlayer.Instance() == null) {
                playReady();
            }

            PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PLAYING;

            MuzikoExoPlayer.Instance().reset();

            if (networkState != NetworkInfo.State.CONNECTED
                    && (PlayerConstants.QUEUE_SONG.album == CloudManager.BOX
                    || PlayerConstants.QUEUE_SONG.album == CloudManager.DROPBOX
                    || PlayerConstants.QUEUE_SONG.album == CloudManager.GOOGLEDRIVE
                    || PlayerConstants.QUEUE_SONG.album == CloudManager.ONEDRIVE
                    || PlayerConstants.QUEUE_SONG.album == CloudManager.FIREBASE
                    || PlayerConstants.QUEUE_SONG.album == CloudManager.AMAZON)) {

                AppController.toast(this, getString(R.string.no_network_connection));
                if (isPre)
                AppController.Instance().servicePrev();
                else
                AppController.Instance().serviceNext();
                return;
            } else {
                if (PlayerConstants.QUEUE_SONG.album == CloudManager.BOX && streamPlay) {
                    MuzikoExoPlayer.Instance().setStreaming(true);
                    MuzikoExoPlayer.Instance().setDataSource(FileHelper.getStreamSaveFolderString(this, PlayerConstants.QUEUE_SONG.data));
                } else if (PlayerConstants.QUEUE_SONG.album == CloudManager.DROPBOX) {
                    MuzikoExoPlayer.Instance().setStreaming(true);
                    MuzikoExoPlayer.Instance().setDataSourceExpiring(this, PlayerConstants.QUEUE_SONG);
                    if (SettingsManager.Instance().getPrefDownloadCloudWhenStreaming()) {
                        CloudManager.Instance().downloadTrack(PlayerConstants.QUEUE_SONG);
                    }
                    BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.buffering_song), false);
                    AppController.isBuffering = true;
                    EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                } else if (PlayerConstants.QUEUE_SONG.album == CloudManager.GOOGLEDRIVE) {
                    MuzikoExoPlayer.Instance().setStreaming(true);
                    MuzikoExoPlayer.Instance().setDataSource(PlayerConstants.QUEUE_SONG.data);
                    if (SettingsManager.Instance().getPrefDownloadCloudWhenStreaming()) {
                        CloudManager.Instance().downloadTrack(PlayerConstants.QUEUE_SONG);
                    }
                    BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.buffering_song), false);
                    AppController.isBuffering = true;
                    EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                } else if (PlayerConstants.QUEUE_SONG.album == CloudManager.ONEDRIVE) {
                    MuzikoExoPlayer.Instance().setStreaming(true);
                    MuzikoExoPlayer.Instance().setDataSourceExpiring(this, PlayerConstants.QUEUE_SONG);
                    if (SettingsManager.Instance().getPrefDownloadCloudWhenStreaming()) {
                        CloudManager.Instance().downloadTrack(PlayerConstants.QUEUE_SONG);
                    }
                    BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.buffering_song), false);
                    AppController.isBuffering = true;
                    EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                } else if (PlayerConstants.QUEUE_SONG.album == CloudManager.AMAZON) {
                    MuzikoExoPlayer.Instance().setStreaming(true);
                    MuzikoExoPlayer.Instance().setDataSourceExpiring(this, PlayerConstants.QUEUE_SONG);
                    if (SettingsManager.Instance().getPrefDownloadCloudWhenStreaming()) {
                        CloudManager.Instance().downloadTrack(PlayerConstants.QUEUE_SONG);
                    }
                    BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.buffering_song), false);
                    AppController.isBuffering = true;
                    EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                } else if (PlayerConstants.QUEUE_SONG.album == CloudManager.FIREBASE) {
                    MuzikoExoPlayer.Instance().setStreaming(true);
                    MuzikoExoPlayer.Instance().setDataSource(PlayerConstants.QUEUE_SONG.data);
                    BufferingEvent bufferingEvent = new BufferingEvent(getString(R.string.buffering_song), false);
                    AppController.isBuffering = true;
                    EventBus.getDefault(getApplicationContext()).postLocal(bufferingEvent);
                } else {
                    MuzikoExoPlayer.Instance().setStreaming(false);
                    MuzikoExoPlayer.Instance().setDataSource(PlayerConstants.QUEUE_SONG.data);
                }
            }


            MuzikoExoPlayer.Instance().prepare();

            PlayerConstants.QUEUE_DURATION = MuzikoExoPlayer.Instance().getDuration();

            QueueItem queueItem = TrackRealmHelper.getTrack(PlayerConstants.QUEUE_SONG.data);

            if (!userChange
                    && PlayerConstants.QUEUE_TIME > 0
                    && PlayerConstants.QUEUE_TIME < PlayerConstants.QUEUE_DURATION) {
                MuzikoExoPlayer.Instance().seekTo(PlayerConstants.QUEUE_TIME);
            } else if (queueItem.startFrom > 0) {
                MuzikoExoPlayer.Instance().seekTo(queueItem.startFrom);
            }

            updateRecent(PlayerConstants.QUEUE_SONG);

            if (userChange) {
                Intent trackEditIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
                trackEditIntent.putExtra("userChange", userChange);
                sendBroadcast(trackEditIntent);
            } else {
                sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
            }

            if (!hasFocus) {
                songFocus();
            }

//            songNotify();
//            scrobbleTrack();
//            ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
        } catch (Exception e) {
            Crashlytics.logException(e);
            Log.i(TAG, "songPlay Exception: " + e.getMessage());

            PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_STOPPED;

            sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
        }
    }

    @DebugLog
    private void scrobbleTrack() {

        AsyncJob.doInBackground(
                () -> {
                    boolean prefScrobbling =
                            SettingsManager.Instance().getPrefs().getBoolean("prefScrobbling", false);

                    if (!prefScrobbling) {
                        return;
                    }

                    MusicAPI mMusicAPI = null;
                    Intent mService = null;
                    Track mTrack = null;

                    // we must be logged in to scrobble
                    AppSettings settings = new AppSettings(getApplicationContext());
                    if (!settings.isAnyAuthenticated()) {
                        Log.d(TAG, "The user has not authenticated, won't propagate the submission request");
                        return;
                    }

                    mService = new Intent(getApplicationContext(), ScrobblingService.class);
                    mService.setAction(ScrobblingService.ACTION_PLAYSTATECHANGED);

                    mMusicAPI =
                            MusicAPI.fromReceiver(getApplicationContext(), "muziko", "com.muziko", null, true);

                    try {
                        Track.Builder b = new Track.Builder();
                        b.setMusicAPI(mMusicAPI);
                        b.setArtist(PlayerConstants.QUEUE_SONG.artist_name);
                        b.setTrack(PlayerConstants.QUEUE_SONG.title);
                        b.setAlbum(PlayerConstants.QUEUE_SONG.album_name);
                        int duration = Integer.parseInt(PlayerConstants.QUEUE_SONG.duration) / 1000;
                        if (duration != 0) {
                            b.setDuration(duration);
                        }
                        b.setWhen(System.currentTimeMillis());

                        mTrack = b.build();
                        // parseIntent must have called setMusicAPI and setTrack
                        // with non-null values
                        if (mMusicAPI == null) {
                            throw new IllegalArgumentException("null music api");
                        }
                        if (mTrack == null) {
                            throw new IllegalArgumentException("null track");
                        }

                        // submit track for the ScrobblingService
                        InternalTrackTransmitter.appendTrack(mTrack);
                        // start/call the Scrobbling Service
                        startService(mService);
                    } catch (IllegalArgumentException e) {
                        Crashlytics.logException(e);
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    private void songNotify() {
        notificationActive = true;
        updateLockScreen(PlayerConstants.QUEUE_SONG, RemoteControlClient.PLAYSTATE_PLAYING);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
    }

    @DebugLog
    private void songIndex(int index) {
        PlayerConstants.QUEUE_INDEX = index;
        PlayerConstants.QUEUE_TIME = 0;

        songStart(true,false);
    }

    @DebugLog
    private void songShow(boolean userChange) {
        playStop();

        if (PlayerConstants.QUEUE_INDEX < 0
                || PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size()) {
            return;
        }

        PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(PlayerConstants.QUEUE_INDEX);
        PlayerConstants.QUEUE_TIME = 0;
        PlayerConstants.QUEUE_DURATION = Utils.getInt(PlayerConstants.QUEUE_SONG.duration, 0);

        MuzikoExoPlayer.Instance().reset();
        MuzikoExoPlayer.Instance().setDataSource(PlayerConstants.QUEUE_SONG.data);
        MuzikoExoPlayer.Instance().prepare();

        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
        updateLockScreen(PlayerConstants.QUEUE_SONG, RemoteControlClient.PLAYSTATE_PLAYING);

        if (userChange) {
            Intent trackEditIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
            trackEditIntent.putExtra("userChange", userChange);
            sendBroadcast(trackEditIntent);
        } else {
            sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
        }
    }

    @DebugLog
    private boolean songResume() {
        if (PlayerConstants.QUEUE_TIME == 0) {
            return false;
        }

        try {

            MuzikoExoPlayer.Instance().start();
            PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PLAYING;
            notificationActive = true;
            ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
            ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
            updateLockScreen(PlayerConstants.QUEUE_SONG, RemoteControlClient.PLAYSTATE_PLAYING);
            Intent pauseIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
            pauseIntent.putExtra("userChange", true);
            sendBroadcast(pauseIntent);

            return true;
        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }

        return false;
    }

    @DebugLog
    private void songToggle() {
        if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
            songPause();
        } else if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PAUSED) {
            if (!songResume()) {
                songPlay(false, false,false);
            }
        } else {
            songPlay(false, false,false);
        }
    }

    @DebugLog
    private void songPause() {
        MuzikoExoPlayer.Instance().pause();
        PlayerConstants.QUEUE_TIME = MuzikoExoPlayer.Instance().getCurrentPosition();
        PrefsManager.Instance().setPlayPosition(PlayerConstants.QUEUE_TIME);
        PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_PAUSED;
        Intent pauseIntent = new Intent(AppController.INTENT_QUEUE_CHANGED);
        pauseIntent.putExtra("userChange", true);
        sendBroadcast(pauseIntent);

        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
        updateLockScreen(PlayerConstants.QUEUE_SONG, RemoteControlClient.PLAYSTATE_PAUSED);
    }

    @DebugLog
    private void songNext() {

        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            songStop();
        } else {
            PlayerConstants.QUEUE_TIME = 0;

            int repeat = PrefsManager.Instance().getPlayRepeat();

            if (repeat == PlayerConstants.REPEAT_ONE) {
                songPlay(false, false,false);
                Log.e(TAG, "REPEAT_ONE");
            } else if (PrefsManager.Instance().getPlayShuffle(SongService.this)) {

                Random rand = new Random();
                int randomNum = rand.nextInt(((PlayerConstants.QUEUE_LIST.size() - 1)) + 1);
                PlayerConstants.QUEUE_INDEX = randomNum;
                songStart(false,false);
                Log.e(TAG, "SHUFFLE");
            } else {

                int index = PlayerConstants.QUEUE_INDEX;
                index++;
                if (index < PlayerConstants.QUEUE_LIST.size()) {

                    Log.e(TAG, "CURRENT INDEX = " + PlayerConstants.QUEUE_INDEX + " NEXT INDEX " + index);
                    PlayerConstants.QUEUE_INDEX = index;
                    songStart(false,false);
                } else {
                    if (repeat == PlayerConstants.REPEAT_OFF) {
                        Log.e(TAG, "REPEAT_OFF - MOVING TO START OF QUEUE");
                        songBack();
                    } else //repeat all
                    {
                        index = 0;
                        Log.e(TAG, "MOVING TO START OF QUEUE");
                        PlayerConstants.QUEUE_INDEX = index;
                        songStart(false,false);
                    }
                }
            }
        }
    }

    @DebugLog
    private void songPrev() {
        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            songStop();
        } else {
            PlayerConstants.QUEUE_TIME = 0;
            int repeat = PrefsManager.Instance().getPlayRepeat();
            if (repeat == PlayerConstants.REPEAT_ONE) {
                songPlay(false, false,true);
            } else if (PlayerConstants.QUEUE_INDEX > 0) {
                PlayerConstants.QUEUE_INDEX--;
            } else {
                PlayerConstants.QUEUE_INDEX = PlayerConstants.QUEUE_LIST.size() - 1;
            }

            songStart(false,true);
        }
    }

    @DebugLog
    private void songSeek(int progress) {
        PlayerConstants.QUEUE_TIME = progress;

        PrefsManager.Instance().setPlayPosition(PlayerConstants.QUEUE_TIME);

        MuzikoExoPlayer.Instance().seekTo(PlayerConstants.QUEUE_TIME);

        sendBroadcast(new Intent(AppController.INTENT_TRACK_SEEKED));
    }

    @DebugLog
    private void songFocus() {
        audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
        hasFocus = true;
    }

    @DebugLog
    private void songBack() {
        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            songStop();
            return;
        }

        PlayerConstants.QUEUE_TIME = 0;
        PrefsManager.Instance().setPlayPosition(PlayerConstants.QUEUE_TIME);

        playStop();

        PlayerConstants.QUEUE_INDEX = 0;
        PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(0);

        playInterrupt = false;
        PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_STOPPED;
        updateLockScreen(PlayerConstants.QUEUE_SONG, RemoteControlClient.PLAYSTATE_STOPPED);

        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
        sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
    }

    @DebugLog
    private void songStop() {
        PlayerConstants.QUEUE_TIME = MuzikoExoPlayer.Instance().getCurrentPosition();
        PrefsManager.Instance().setPlayPosition(PlayerConstants.QUEUE_TIME);

        playStop();

        cancelNotification();

        playInterrupt = false;
        PlayerConstants.QUEUE_STATE = PlayerConstants.QUEUE_STATE_STOPPED;
        sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
    }

    @DebugLog
    private void songClear() {
        songStop();

        AsyncJob.doInBackground(
                () -> {
                    PrefsManager.Instance().setQueueLevel(0);
                    PlayerConstants.QUEUE_DIRTY = 0;

                    PlayerConstants.QUEUE_LIST.clear();
                    PlayerConstants.QUEUE_INDEX = 0;
                    PlayerConstants.QUEUE_TYPE = 0;
                    PlayerConstants.QUEUE_TIME = 0;
                    PrefsManager.Instance().setPlayPosition(0);
                    PlayerConstants.QUEUE_SONG = new QueueItem();

                    songSave();
                });

        ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
        notificationActive = false;
        stopForeground(true);
        sendBroadcast(new Intent(AppController.INTENT_QUEUE_CLEARED));
        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
    }

    @DebugLog
    private void songExit() {
        songStop();

        ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
        notificationActive = false;
        stopForeground(true);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
    }

    @DebugLog
    private void songDelete(String hash, String data) {
        TrackRealmHelper.deleteTrack(data);

        ArrayList<QueueItem> delete = new ArrayList<>();
        for (QueueItem track : PlayerConstants.QUEUE_LIST) {
            if (data.equals(track.data)) {
                delete.add(track);
            }
        }
        PlayerConstants.QUEUE_LIST.removeAll(delete);
        PlayerConstants.QUEUE_TYPE = 0;
        songDirty();

        if ((PlayerConstants.QUEUE_SONG.hash.length() > 0
                && PlayerConstants.QUEUE_SONG.hash.equals(hash))
                || (PlayerConstants.QUEUE_SONG.data.length() > 0
                && PlayerConstants.QUEUE_SONG.data.equals(data))) {

            playStop();

            PlayerConstants.QUEUE_TIME = 0;

            int repeat = PrefsManager.Instance().getPlayRepeat();
            if (repeat == PlayerConstants.REPEAT_ONE) {
                songBack();
            } else if (repeat == PlayerConstants.REPEAT_OFF) {
                if (delete.size() > 0) {

                    if (PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size()) {
                        songBack();
                    } else if (PlayerConstants.QUEUE_LIST.size() > 0) {
                        songStart(false,false);
                    } else {
                        songClear();
                    }
                } else {
                    songBack();
                }
            } else {
                if (delete.size() > 0) {
                    if (PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size()) {
                        songNext();
                    } else if (PlayerConstants.QUEUE_LIST.size() > 0) {
                        songStart(false,false);
                    } else {
                        songClear();
                    }
                } else {
                    songNext();
                }
            }
        } else {
            AppController.Instance().updateQueueIndex();
        }

        sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
        sendBroadcast(new Intent(AppController.INTENT_RECENT_CHANGED));
        sendBroadcast(new Intent(AppController.INTENT_FAVOURITE_CHANGED));
        sendBroadcast(new Intent(AppController.INTENT_PLAYLIST_CHANGED));
        sendBroadcast(new Intent(AppController.INTENT_TRACK_DELETED));
        sendBroadcast(new Intent(AppController.INTENT_TRACK_DELETED_HOME));

    }

    @DebugLog
    private void songUnqueue(String hash) {
        if (PlayerConstants.QUEUE_SONG.hash.equals(hash)) {

            PlayerConstants.QUEUE_TIME = 0;

            int repeat = PrefsManager.Instance().getPlayRepeat();
            if (repeat == PlayerConstants.REPEAT_ONE) {
                if (PlayerConstants.QUEUE_LIST.size() > 0) {
                    songBack();
                } else {
                    songClear();
                }
            } else if (repeat == PlayerConstants.REPEAT_OFF) {
                if (PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size()) {
                    if (PlayerConstants.QUEUE_LIST.size() > 0) {
                        songBack();
                    } else {
                        songClear();
                    }
                } else if (PlayerConstants.QUEUE_LIST.size() > 0) {
                    songStart(false,false);
                } else {
                    songClear();
                }
            } else {
                if (PlayerConstants.QUEUE_INDEX >= PlayerConstants.QUEUE_LIST.size()) {
                    if (PlayerConstants.QUEUE_LIST.size() > 0) {
                        songNext();
                    } else {
                        songClear();
                    }
                } else if (PlayerConstants.QUEUE_LIST.size() > 0) {
                    songStart(false,false);
                } else {
                    songClear();
                }
            }
        } else {
            AppController.Instance().updateQueueIndex();
        }

        sendBroadcast(new Intent(AppController.INTENT_QUEUE_CHANGED));
    }

    @DebugLog
    private void songDirty() {
        if (PlayerConstants.QUEUE_DIRTY == 0) {
            int duration;
            if (PlayerConstants.QUEUE_LIST.size() > 250) {
                duration = 2500;
            } else if (PlayerConstants.QUEUE_LIST.size() > 500) {
                duration = 5000;
            } else if (PlayerConstants.QUEUE_LIST.size() > 1000) {
                duration = 7500;
            } else if (PlayerConstants.QUEUE_LIST.size() > 2000) {
                duration = 1000;
            } else {
                duration = 2000;
            }

            PlayerConstants.QUEUE_DIRTY = System.currentTimeMillis() + duration;
        }
    }

    @DebugLog
    private void songSave() {
        if (PlayerConstants.QUEUE_SAVING) return;

        PlayerConstants.QUEUE_SAVING = true;
        Log.e(TAG, "queue save start");

        AsyncJob.doInBackground(
                () -> {
                    long level = PrefsManager.Instance().getQueueLevel() + 1;
                    PrefsManager.Instance().setQueueLevel(level);
                    QueueHelper.saveQueue(SongService.this);
                });

        PlayerConstants.QUEUE_SAVING = false;
        PlayerConstants.QUEUE_DIRTY = 0;
        Log.e(TAG, "queue save done");
    }

    private void songSleeper() {
        if (PlayerConstants.SLEEP_TIMER == 0) return;

        if (System.currentTimeMillis() > PlayerConstants.SLEEP_TIMER) {

            if (MuzikoExoPlayer.Instance().isPlaying() && PrefsManager.Instance().getSleepTimeLastSong()) {

                MyApplication.sleepLastSong = true;
            } else {

                sendBroadcast(new Intent(NOTIFY_DELETE));

                PlayerConstants.SLEEP_TIMER = 0;
                Intent bIntent = new Intent(SLEEP_TIMER_STOP);
                bIntent.putExtra(ACTION_SLEEP_STOP, "1");
                sendBroadcast(bIntent);
            }
        } else {
            long timeDiff = PlayerConstants.SLEEP_TIMER - System.currentTimeMillis();
            Intent timeIntent = new Intent(SLEEP_TIMER_STOP);
            timeIntent.putExtra(ACTION_SLEEP_TIME, timeDiff);

            sendBroadcast(timeIntent);
        }
    }

    @DebugLog
    private void playReady() {

        MuzikoExoPlayer.Instance();
    }

    @DebugLog
    private void playStop() {
        try {
            if (MuzikoExoPlayer.Instance().isPlaying()) {
                MuzikoExoPlayer.Instance().stop();
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    private void playClose() {
        try {
            MuzikoExoPlayer.Instance().release();
        } catch (IllegalStateException e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    private void cancelNotification() {

        if (mNotificationManager == null) {
            mNotificationManager =
                    (android.app.NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        mNotificationManager.cancel(NOTIFICATION_ID);
    }

    @DebugLog
    private void updateRecent(QueueItem queue) {

        RecentAdd ra = new RecentAdd(this);
        ra.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool(), queue);
    }

    @DebugLog
    @SuppressLint("NewApi")
    private void updateLockScreen(QueueItem data, int state) {
        if (!currentVersionSupportLockScreenControls) return;

        if (remoteControlClient == null) return;

        MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);

        boolean prefLockScreen =
                SettingsManager.Instance().getPrefs().getBoolean("prefLockScreen", false);

        if (prefLockScreen) {
            remoteControlClient.setPlaybackState(state);
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ALBUM, data.album_name);
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.artist_name);
            metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.title);
        }


        AsyncJob.doOnMainThread(() -> {
            if (metadataTarget != null)
                Picasso.with(getApplicationContext()).cancelRequest(metadataTarget);
            metadataTarget = new SongServiceMetadataTarget(metadataEditor);
        });
        String url = "";
        boolean prefArtworkLock = SettingsManager.Instance().getPrefs().getBoolean("prefArtworkLock", false);
        if (prefArtworkLock) {

            AsyncJob.doOnMainThread(() -> {
                switch ((int) PlayerConstants.QUEUE_SONG.album) {
                    case CloudManager.GOOGLEDRIVE:

                        Picasso.with(getApplicationContext())
                                .load(R.drawable.drive_large)
                                .error(R.mipmap.placeholder)
                                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                .centerCrop()
                                .into(metadataTarget);
                        break;

                    case CloudManager.DROPBOX:
                        Picasso.with(getApplicationContext())
                                .load(R.drawable.dropbox_large)
                                .error(R.mipmap.placeholder)
                                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                .centerCrop()
                                .into(metadataTarget);
                        break;

                    case CloudManager.BOX:
                        Picasso.with(getApplicationContext())
                                .load(R.drawable.box_large)
                                .error(R.mipmap.placeholder)
                                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                .centerCrop()
                                .into(metadataTarget);
                        break;

                    case CloudManager.ONEDRIVE:
                        Picasso.with(getApplicationContext())
                                .load(R.drawable.onedrive_large)
                                .error(R.mipmap.placeholder)
                                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                .centerCrop()
                                .into(metadataTarget);
                        break;

                    case CloudManager.FIREBASE:
                        Picasso.with(getApplicationContext())
                                .load(R.drawable.firebase_large)
                                .error(R.mipmap.placeholder)
                                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                .centerCrop()
                                .into(metadataTarget);
                        break;

                    case CloudManager.AMAZON:
                        Picasso.with(getApplicationContext())
                                .load(R.drawable.amazon_large)
                                .error(R.mipmap.placeholder)
                                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                .centerCrop()
                                .into(metadataTarget);
                        break;

                    default:
                        Picasso.with(getApplicationContext())
                                .load("content://media/external/audio/albumart/" + PlayerConstants.QUEUE_SONG.album)
                                .error(R.mipmap.placeholder)
                                .resize(IMAGE_LARGE_SIZE, IMAGE_LARGE_SIZE)
                                .centerCrop()
                                .into(metadataTarget);
                }
            });


        } else {
            metadataEditor.clear();
            metadataEditor.apply();
        }
    }

    @DebugLog
    @SuppressLint("NewApi")
    private void registerRemoteClient() {
        try {
            audioManager.registerMediaButtonEventReceiver(remoteComponentName);

            Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
            mediaButtonIntent.setComponent(remoteComponentName);
            PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);

            remoteControlClient = new RemoteControlClient(mediaPendingIntent);
            audioManager.registerRemoteControlClient(remoteControlClient);

            remoteControlClient.setTransportControlFlags(
                    RemoteControlClient.FLAG_KEY_MEDIA_PLAY
                            | RemoteControlClient.FLAG_KEY_MEDIA_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE
                            | RemoteControlClient.FLAG_KEY_MEDIA_STOP
                            | RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS
                            | RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    @DebugLog
    private void unregisterRemoteClient() {
        try {
            if (remoteControlClient != null) {
                audioManager.unregisterMediaButtonEventReceiver(remoteComponentName);
                audioManager.unregisterRemoteControlClient(remoteControlClient);

                remoteControlClient = null;
            }
        } catch (Exception e) {
            Crashlytics.logException(e);
        }
    }

    //handles all the background threads things for you
    private void sendMsgToUI(int position, int progress, int duration) {
        if (EventBus.getDefault(SongService.this).hasSubscriberForEvent(ProgressEvent.class)) {
            ProgressEvent progressEvent = new ProgressEvent(position, progress, duration);
            EventBus.getDefault(SongService.this).postLocal(progressEvent);
        }
    }

    @DebugLog
    @Override
    public void onfirstItemReady() {
        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
    }

    @DebugLog
    @Override
    public void onComplete() {
        stopForeground(true);
        ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
    }

    @DebugLog
    @Override
    public void onFileEvent(int event, File file) {
        String extension = FilenameUtils.getExtension(file.getName());
        for (int i = 0; i < extensions.length; i++) {
            String ext = extensions[i];
            if (ext.equalsIgnoreCase(extension)) {
                switch (event) {
                    case DELETE_SELF:
                        AppController.toast(this, "DELETE_SELF " + file.getAbsolutePath());
                        break;
                    case CREATE:
                        AppController.toast(this, "CREATE " + file.getAbsolutePath());
                        break;
                }
            }
        }
    }

    //    @Override
    //    public void onQueueListChanged() {
    //
    //    }
    //
    //    @Override
    //    public void onRemove(int index) {
    //        if (wearConnected) {
    //            EventBus.getDefault(SongService.this).postRemote(new QueueRemoveEvent(index));
    //        }
    //    }
    //
    //    @Override
    //    public void onAdd(int index, QueueItem queueItem) {
    //        if (wearConnected) {
    //            AsyncJob.doInBackground(() -> {
    //                ArrayList<QueueItem> queueItemsToSend = new ArrayList<>();
    //                queueItemsToSend.add(queueItem);
    //
    //                Queuelist queuelist = new Queuelist();
    //
    //                queuelist.setQueueItems(queueItemsToSend);
    //                String json = null;
    //                try {
    //                    json = LoganSquare.serialize(queuelist);
    //                } catch (IOException e) {
    //                    Crashlytics.logException(e);
    //                }
    //                EventBus.getDefault(SongService.this).postRemote(new QueueAddEvent(json, index));
    //            });
    //
    //        }
    //    }
    //
    //    @Override
    //    public void onAddAll(int index, ArrayList<QueueItem> queueItems) {
    //        if (wearConnected) {
    //            AsyncJob.doInBackground(() -> {
    //                if (queueItems.size() > 30) {
    //                    wearFullUpdate();
    //                } else {
    //                    ArrayList<QueueItem> queueItemsToSend = new ArrayList<>();
    //                    queueItemsToSend.addAll(queueItems);
    //
    //                    Queuelist queuelist = new Queuelist();
    //
    //                    queuelist.setQueueItems(queueItems);
    //                    String json = null;
    //                    try {
    //                        json = LoganSquare.serialize(queuelist);
    //                    } catch (IOException e) {
    //                        Crashlytics.logException(e);
    //                    }
    //                    EventBus.getDefault(SongService.this).postRemote(new QueueAddEvent(json, index));
    //                }
    //            });
    //        }
    //
    //    }
    //
    //    @Override
    //    public void onClear() {
    //        if (wearConnected) {
    //            EventBus.getDefault(SongService.this).postRemote(new WearActionEvent(CommonConstants.ACTION_HANDHELD_CLEAR, 0));
    //        }
    //    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        prefFaceDown =
                SettingsManager.Instance().getPrefs().getString(SettingsManager.prefFaceDown, "");
        prefFaceUp = SettingsManager.Instance().getPrefs().getString(SettingsManager.prefFaceUp, "");

        float z = event.values[2];
        if (z > 9 && z < 10) {

        } else if (z > -10 && z < -9) {
            if (prefFaceDown.equals(getString(R.string.pause))) {
                if (MuzikoExoPlayer.Instance().isPlaying()) {
                    songPause();
                }
            } else if (prefFaceDown.equals(getString(R.string.play))) {
                if (MuzikoExoPlayer.Instance() != null
                        && PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED) {
                    AppController.Instance().serviceResume(false);
                }
            }
        }

        int type = event.sensor.getType();
        if (type == Sensor.TYPE_ACCELEROMETER) {
            float gz = event.values[2];
            if (mGZ == 0) {
                mGZ = gz;
            } else {
                if ((mGZ * gz) < 0) {
                    mEventCountSinceGZChanged++;
                    if (mEventCountSinceGZChanged == MAX_COUNT_GZ_CHANGE) {
                        mGZ = gz;
                        mEventCountSinceGZChanged = 0;
                        if (gz > 0) {
                            Log.d(TAG, "now screen is facing up.");

                            if (prefFaceUp.equals(getString(R.string.pause))) {
                                if (MuzikoExoPlayer.Instance().isPlaying()) {
                                    songPause();
                                }
                            } else if (prefFaceUp.equals(getString(R.string.play))) {
                                if (MuzikoExoPlayer.Instance() != null
                                        && PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_STOPPED) {
                                    AppController.Instance().serviceResume(false);
                                }
                            }
                        } else if (gz < 0) {

                        }
                    }
                } else {
                    if (mEventCountSinceGZChanged > 0) {
                        mGZ = gz;
                        mEventCountSinceGZChanged = 0;
                    }
                }
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
    }

    @DebugLog
    private class SongServicePhoneStateListener extends PhoneStateListener {
        @Override
        public void onCallStateChanged(int state, String incomingNumber) {
            Log.i(TAG, "PhoneStateListener state: " + state + " incoming no: " + incomingNumber);

            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    if (playInterrupt) {
                        if (!songResume()) {
                            songPlay(false, false,false);
                        }

                        playInterrupt = false;
                    }
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                case TelephonyManager.CALL_STATE_RINGING:
                    if (!playInterrupt
                            && PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PLAYING) {
                        songPause();

                        playInterrupt = true;
                    }
                    break;
            }
        }
    }

    private class SongServiceMetadataTarget implements Target {
        final MetadataEditor metadataEditor;

        public SongServiceMetadataTarget(MetadataEditor metadataEditor) {
            this.metadataEditor = metadataEditor;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            metadataEditor.putBitmap(
                    RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, Utils.stopRecyclingBitmap(bitmap));
            metadataEditor.apply();
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            //metadataEditor.putBitmap(RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK, stopRecyclingBitmap(((BitmapDrawable) getResources().getDrawable(R.mipmap.placeholder)).getBitmap()));

            metadataEditor.putBitmap(
                    RemoteControlClient.MetadataEditor.BITMAP_KEY_ARTWORK,
                    Utils.stopRecyclingBitmap(((BitmapDrawable) errorDrawable).getBitmap()));
            metadataEditor.apply();
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
        }
    }

    @DebugLog
    public class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();

                if (action.equals(AppController.INTENT_TRACK_EDITED)) {
                    int index = intent.getIntExtra("index", -1);
                    String tag = intent.getStringExtra("tag");
                    QueueItem item = (QueueItem) intent.getSerializableExtra("item");
                    if (item != null) {
                        if (PlayerConstants.QUEUE_SONG.data.equals(item.data)) {
                            ThreadManager.Instance().submitToBackgroundThreadPool(updateNotificationRunnable);
                            ThreadManager.Instance().submitToBackgroundThreadPool(updateWidgetsRunnable);
                        }
                    }
                }
            }
        }
    }

    @DebugLog
    class MediaObserver extends ContentObserver {
        public MediaObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            MediaHelper.Instance().loadMusicWrapper(true);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            MediaHelper.Instance().loadMusicWrapper(true);
        }
    }
}
