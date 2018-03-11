package com.muziko;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;

import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.JobRequest;
import com.facebook.stetho.Stetho;
import com.github.shiraji.butai.Butai;
import com.github.shiraji.butai.ButaiDelegate;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.FirebaseDatabase;
import com.muziko.common.models.QueueItem;
import com.muziko.common.models.ReverbItem;
import com.muziko.common.models.TabModel;
import com.muziko.common.models.firebase.Contact;
import com.muziko.common.models.firebase.Person;
import com.muziko.common.models.firebase.Share;
import com.muziko.jobs.CoverArtJob;
import com.muziko.jobs.LyricsJob;
import com.muziko.jobs.MD5Job;
import com.muziko.manager.AppController;
import com.muziko.manager.MediaHelper;
import com.muziko.manager.OkHttpManager;
import com.muziko.manager.PrefsManager;
import com.muziko.models.EqualizerItem;
import com.muziko.receivers.MuzikoUpdateReceiver;
import com.muziko.salut.SalutFileDownloader;
import com.muziko.salut.SalutFileUploader;
import com.muziko.tasks.CoverArtDownloader;
import com.oasisfeng.condom.CondomContext;
import com.oasisfeng.condom.CondomProcess;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;
import com.yayandroid.theactivitymanager.TheActivityManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import hugo.weaving.DebugLog;
import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

//import com.muziko.cloud.Drive.DriveApi;

//import com.muziko.cloud.Drive.DriveApi;

public class MyApplication extends MultiDexApplication implements Butai {

    public static final String TRACKHEADER = "Track_Header";
    public static final String ARTISTHEADER = "Artists_Header";
    public static final String ALBUMHEADER = "Albums_Header";
    public static final int ACTIVITY_PAGE_PLAYED = 0;
    public static final int ACTIVITY_PAGE_MOST = 1;
    public static final int ACTIVITY_PAGE_ADDED = 2;

    public static final int SHARING_RECEIVED = 1;
    public static final int SHARING_SENT = 0;
    public static final int SHARING_UNKNOWN = 2;

    public static final String ARG_ID = "ID";
    public static final String ARG_TITLE = "TITLE";
    public static final String ARG_FAV = "FAVORITE";
    public static final String ARG_IGNORE = "IGNORE";
    public static final String ARG_ART = "ART";
    public static final String ARG_NAME = "NAME";
    public static final String ARG_TYPE = "TYPE";
    public static final String ARG_DATA = "DATA";
    public static final String ARG_DURATION = "DURATION";
    public static final String ARG_SONGS = "SONGS";
    public static final String HOME = "Home";
    public static final String TRACKS = "Tracks";
    public static final String ARTISTS = "Artists";
    public static final String ALBUMS = "Albums";
    public static final String GENRES = "Genres";
    public static final String FOLDERS = "Folders";
    public static final String LAST_OPENED = "Last Opened";
    public static final int IMAGE_SMALL_SIZE = 100;
    public static final int IMAGE_MEDIUM_SIZE = 300;
    public static final int IMAGE_LARGE_SIZE = 800;
    public static final int MAX_ACTIVITY_ITEMS = 20;
    public static final int JPEG_COMPRESS = 40;
    public static final ArrayList<QueueItem> multiTagList = new ArrayList<>();
    public static final String NOTIFY_CANCEL_COVERART_DOWNLOAD = "com.muziko.coverart.cancelUpload";
    public static final String NOTIFY_CANCEL_LYRICS_DOWNLOAD = "com.muziko.lyrics.cancelUpload";
    public static final String NOTIFY_CANCEL_HASH = "com.muziko.hash.cancel";
    public static final String ACTION_UPDATE_LYRICS = "UPDATE_LYRICS";
    public static final String ACTION_CANCEL_UPDATE_LYRICS = "CANCEL_UPDATE_LYRICS";
    public static final String ACTION_UPDATE_CACHE = "UPDATE_CACHE";
    private static final int MuzikoUpdateReceiver = 6666;
    private static final String TAG = MyApplication.class.getSimpleName();
    public static ArrayList<String> favorites = new ArrayList<>();
    public static ArrayList<EqualizerItem> presets = new ArrayList<>();
    public static EqualizerItem preset = new EqualizerItem();
    public static ArrayList<ReverbItem> reverbs = new ArrayList<>();

    public static HashMap<String, Person> fullUerList = new HashMap<>();
    public static HashMap<String, Person> userList = new HashMap<>();
    public static ArrayList<Person> phoneContactList = new ArrayList<>();
    public static ArrayList<Contact> firebaseContactList = new ArrayList<>();
    public static ArrayList<Share> shareList = new ArrayList<>();
    public static HashMap<String, SalutFileDownloader> shareDownloaderList = new HashMap<>();
    public static HashMap<String, SalutFileUploader> shareUploaderList = new HashMap<>();
    public static ArrayList<CoverArtDownloader> coverArtDownloaders = new ArrayList<>();
    public static boolean loaded = false;
    public static boolean imageCache = false;
    public static int imageCount = 0;
    public static int imageTotal = 0;
    public static int IMAGE_WIDTH;
    public static int IMAGE_HEIGHT;
    public static int IMAGE_SIZE;
    public static RealmConfiguration realmConfiguration;
    public static boolean sleepLastSong;
    public static int transitionPosition;
    public static boolean queueLast;
    public static String versionName;
    public static boolean ignoreNextMediaScan = false;
    public static String searchString = "";
    public static long playerListLastScrollChange = 0;
    public static boolean tabsChanged = false;
    public static QueueItem currentTrack = new QueueItem();
    public static boolean isHost = false;
    public static boolean showArtwork = true;
    public static ArrayList<TabModel> tabModels = new ArrayList<>();
    public static boolean notificationActive = false;
    public static boolean pauseDeletingTempRingtone = false;
    public static ArrayList<Integer> localDrives = new ArrayList<>();
    public static NetworkInfo.State networkState;
    public static boolean hasWifi;
    private static boolean loading = false;
    private static MyApplication instance;

    public static MyApplication getInstance() {
        return instance;
    }


    @DebugLog
    public void load(Context context) {

        if (ContextCompat.checkSelfPermission(context, "android.permission.READ_EXTERNAL_STORAGE")
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        if (MyApplication.loading || MyApplication.loaded) {
            return;
        }
        loading = true;

        MediaHelper.Instance().loadMusicWrapper(false);

        loading = false;
        MyApplication.loaded = true;
    }

    @DebugLog
    private void showUnhandledToast(final String text) {

        new WeakHandler(Looper.getMainLooper())
                .post(() -> AppController.toast(getApplicationContext(), "Sorry, something went wrong"));
    }

    @DebugLog
    @Override
    public void onCreate() {
        try {
            super.onCreate();

            instance = this;
            CondomProcess.installExceptDefaultProcess(this);
            if (Build.VERSION.SDK_INT >= 24) {
                try {
                    Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                    m.invoke(null);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            Realm.init(CondomContext.wrap(this, "Realm"));
            realmConfiguration =
                    new RealmConfiguration.Builder()
                            .name(Realm.DEFAULT_REALM_NAME)
                            .schemaVersion(0)
                            .deleteRealmIfMigrationNeeded()
                            .build();
            Realm.setDefaultConfiguration(realmConfiguration);

            registerActivityLifecycleCallbacks(AppController.Instance());

            ButaiDelegate.init(this);

//        NeverCrash.init(
//                (t, e) -> {
//                    Crashlytics.logException(e);
//                });

//        if (BuildConfig.DEBUG) {
//
//            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
//                    .detectAll()
//                    .penaltyLog()
//                    .build());
//            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
//                    .detectLeakedSqlLiteObjects()
//                    .detectLeakedClosableObjects()
//                    .penaltyLog()
//                    //					.penaltyDeath()
//                    .build());
//
//            BlockCanary.install(this, new AppBlockCanaryContext()).start();
//
            Stetho.initialize(
                    Stetho.newInitializerBuilder(this)
                            .enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
                            .enableWebKitInspector(
                                    RealmInspectorModulesProvider.builder(this).build())
                            .build());
//        }

            versionName = BuildConfig.VERSION_NAME;

            OkHttpManager.Instance().init();

            FirebaseApp.initializeApp(CondomContext.wrap(this, "Firebase"));
            FirebaseDatabase.getInstance().setPersistenceEnabled(true);

            Fabric.with(CondomContext.wrap(this, "Fabric"), new Crashlytics());

            CalligraphyConfig.initDefault(
                    new CalligraphyConfig.Builder()
                            .setDefaultFontPath("fonts/Roboto-Regular.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build());

            localDrives.add(1);
            localDrives.add(2);

            TheActivityManager.getInstance().configure(this);

            AppController.Instance().init(CondomContext.wrap(this, "Muziko"));

            if (AppController.Instance().shouldShowAd()) {
                if (PrefsManager.Instance().getAdLastShown() == 0) {
                    PrefsManager.Instance().setAdLastShown(1);
                } else {
                    PrefsManager.Instance().setShowLaunchAd(true);
                }
            } else {
                PrefsManager.Instance().setShowLaunchAd(false);
                PrefsManager.Instance().setShowAd(false);
            }

            setupJobs();

            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            prefs.edit().putBoolean("prefGapless", false).apply();
            scheduleUpdateAlarm();

//        PrefsManager.Instance().setSubscription(STORAGE_SUBSCRIPTION_LEVEL_FREE);
//        PrefsManager.Instance().setLastFirebaseOverlimitWarning(0);
        }catch (Exception e){

        }
    }

    @DebugLog
    @Override
    public void onTerminate() {
        super.onTerminate();

        AppController.Instance().CompactMuzikoDB();

        MyApplication.loaded = false;
    }

    @DebugLog
    @Override
    public void onLowMemory() {
        super.onLowMemory();
    }

    @DebugLog
    private void setupJobs() {
        try {
//        Set<JobRequest> compactJobRequests = JobManager.instance().getAllJobRequestsForTag(CompactRealmJob.TAG);
//        if (compactJobRequests.size() == 0) {
//            CompactRealmJob compactRealmJob = new CompactRealmJob();
//            compactRealmJob.scheduleJob();
//        }

            Set<JobRequest> coverArtJobRequests = JobManager.instance().getAllJobRequestsForTag(CoverArtJob.TAG);
            if (coverArtJobRequests.size() == 0) {
                CoverArtJob coverArtJob = new CoverArtJob();
                coverArtJob.scheduleJob();
            }

            Set<JobRequest> lyricsJobRequests = JobManager.instance().getAllJobRequestsForTag(LyricsJob.TAG);
            if (lyricsJobRequests.size() == 0) {
                LyricsJob lyricsJob = new LyricsJob();
                lyricsJob.scheduleJob();
            }

            Set<JobRequest> md5JobRequests = JobManager.instance().getAllJobRequestsForTag(MD5Job.TAG);
            if (md5JobRequests.size() == 0) {
                MD5Job md5Job = new MD5Job();
                md5Job.scheduleJob();
            }
        }catch (Exception e){

        }
    }

    @DebugLog
    public void scheduleUpdateAlarm() {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(getApplicationContext(), MuzikoUpdateReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent =
                PendingIntent.getBroadcast(
                        this, MuzikoUpdateReceiver, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // Setup periodic alarm every 5 seconds
        long firstMillis = System.currentTimeMillis(); // alarm is set right away
        AlarmManager alarm = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
        // First parameter is the type: ELAPSED_REALTIME, ELAPSED_REALTIME_WAKEUP, RTC_WAKEUP
        // Interval can be INTERVAL_FIFTEEN_MINUTES, INTERVAL_HALF_HOUR, INTERVAL_HOUR, INTERVAL_DAY

        alarm.setRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime(),
                86399999,
                pIntent);
    }


    @Override
    public boolean isReturnedFromBackground() {
        return ButaiDelegate.isReturnedFromBackground();
    }

    @Override
    public boolean isBackground() {
        return ButaiDelegate.isBackground();
    }

    @Override
    public boolean isForeground() {
        return ButaiDelegate.isForeground();
    }
}
