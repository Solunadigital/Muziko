package com.muziko.manager;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaScannerConnection;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.multidex.MultiDexApplication;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.util.Pair;
import android.view.View;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.arasthel.asyncjob.AsyncJob;
import com.badoo.mobile.util.WeakHandler;
import com.crashlytics.android.Crashlytics;
import com.evernote.android.job.JobManager;
import com.muddzdev.styleabletoastlibrary.StyleableToast;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.MultiTagActivity;
import com.muziko.activities.PlayerListActivity;
import com.muziko.activities.ShareWifiActivity;
import com.muziko.activities.StorageActivity;
import com.muziko.activities.TagsActivity;
import com.muziko.common.models.PlaylistItem;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.dialogs.PlaylistAdd;
import com.muziko.dialogs.PlaylistMerge;
import com.muziko.dialogs.RemoveAfter;
import com.muziko.dialogs.RemoveAfterExisting;
import com.muziko.dialogs.Share;
import com.muziko.dialogs.UpgradeStorage;
import com.muziko.helpers.MD5;
import com.muziko.helpers.Utils;
import com.muziko.jobs.MuzikoJobCreator;
import com.muziko.mediaplayer.MuzikoExoPlayer;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.models.MuzikoSubscriptionType;
import com.muziko.service.LyricsDownloaderService;
import com.muziko.service.MuzikoFingerprintService;
import com.muziko.service.SongService;
import com.muziko.tasks.MD5Updater;
import com.muziko.tasks.TrackDelete;
import com.muziko.tasks.TrackGrouper;
import com.oasisfeng.condom.CondomContext;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hugo.weaving.DebugLog;
import io.realm.Realm;

/**
 * Created by Bradley on 14/05/2017.
 */
public class AppController implements MultiDexApplication.ActivityLifecycleCallbacks {
    public static final String INTENT_QUEUE_STOPPED = "com.muziko.intent.queue.stopped";
    public static final String INTENT_QUEUE_CLEARED = "com.muziko.intent.queue.cleared";
    public static final String INTENT_PREMIUM_CHANGED = "com.muziko.intent.premium.changed";
    public static final String INTENT_PLAYLIST_CHANGED = "com.muziko.intent.playlist.changed";
    public static final String INTENT_FAVOURITE_CHANGED = "com.muziko.intent.favourite.changed";
    public static final String INTENT_TRACK_CHANGED = "com.muziko.intent.track.changed";
    public static final String INTENT_TRACK_CHANGED_HOME = "com.muziko.intent.track.changed_home";
    public static final String INTENT_TRACK_DELETED = "com.muziko.intent.track.deleted";
    public static final String INTENT_TRACK_DELETED_HOME = "com.muziko.intent.track.deleted_home";
    public static final String INTENT_TRACK_EDITED = "com.muziko.intent.track.edited";
    public static final String INTENT_TRACK_EDITED_HOME = "com.muziko.intent.track.edited_home";
    public static final String INTENT_TRACK_SEEKED = "com.muziko.intent.track.seeked";
    public static final String INTENT_TRACK_SHUFFLE = "com.muziko.intent.track.shuffle";
    public static final String INTENT_TRACK_REPEAT = "com.muziko.intent.track.repeat";
    public static final String INTENT_MOST_CHANGED = "com.muziko.intent.most.changed";
    public static final String INTENT_RECENT_CHANGED = "com.muziko.intent.recent.changed";
    public static final String INTENT_CLEAR = "com.muziko.intent.clear";
    public static final String INTENT_EXIT = "com.muziko.intent.exit";
    public static final String INTENT_DOWNLOAD_PROGRESS = "com.muziko.intent.download.progress";
    public static final String INTENT_SHARE_DOWNLOADED = "com.muziko.intent.share.downloaded";
    public static final String INTENT_QUEUE_CHANGED = "com.muziko.intent.queue.changed";
    public static final String ACTION_UPDATE_FIREBASE = "UPDATE_FIREBASE";
    public static final String ACTION_FIREBASE_OVERLIMIT = "FIREBASE_OVERLIMIT";
    public static final String ACTION_FIREBASE_OVERLIMIT_NOW = "FIREBASE_OVERLIMIT_NOW";
    public static final String ACTION_DOWNLOAD = "DOWNLOAD";
    public static final String ACTION_DOWNLOAD_LIBRARY = "ACTION_DOWNLOAD_LIBRARY";
    public static final String ACTION_UPLOAD = "UPLOAD";
    public static final String ACTION_REFRESH_LIBRARY = "ACTION_REFRESH_LIBRARY";
    public static final String ACTION_UPLOAD_FULL_LIBRARY = "ACTION_UPLOAD_FULL_LIBRARY";
    public static final String ACTION_UPLOAD_LIBRARY = "ACTION_UPLOAD_LIBRARY";
    public static final String ACTION_DELETE_LIBRARY = "ACTION_DELETE_LIBRARY";
    public static final String NOTIFY_CANCEL_DOWNLOAD = "com.muziko.sharing.cancelDownload";
    public static final String NOTIFY_CANCEL_UPLOAD = "com.muziko.sharing.cancelUpload";
    public static final String NOTIFY_CANCEL_FIREBASE_DOWNLOAD = "com.muziko.firebase.cancelDownload";
    public static final String NOTIFY_CANCEL_FIREBASE_UPLOAD = "com.muziko.firebase.cancelUpload";
    public static final String NOTIFY_REMOVE_FIREBASE_LIBRARY_UPLOAD = "com.muziko.firebase.removelibrary";
    public static final String NOTIFY_REMOVE_FIREBASE_FAV_UPLOAD = "com.muziko.firebase.removefav";
    public static final String NOTIFY_CANCEL_WIFI_DOWNLOAD = "com.muziko.wifisharing.cancelDownload";
    public static final String NOTIFY_CANCEL_WIFI_UPLOAD = "com.muziko.wifisharing.cancelUpload";
    public static final String ARG_ITEM = "data";
    public static final String ARG_PEOPLE = "people";
    public static final String ARG_VERSION = "ARG_VERSION";
    private static final String TAG = AppController.class.getName();
    public static boolean isBuffering;
    public static MD5Updater md5Updater;
    private static AppController instance;
    private static MediaScannerConnection scanner = null;
    private static StyleableToast styleableToast;
    private Activity currentActivity;
    private Context mContext;
    private String androidID;
    private MaterialDialog progressDialog;
    private WeakHandler handler = new WeakHandler();
    private boolean isCloud = false;
    private int[] rates = null;

    //no outer class can initialize this class's object
    private AppController() {
    }

    public static AppController Instance() {
        //if no instance is initialized yet then create new instance
        //else return stored instance
        if (instance == null) {
            instance = new AppController();
        }
        return instance;
    }

    public static void toast(Context context, String message) {

        try {
            if (styleableToast != null) {
                styleableToast.cancel();
            }

            styleableToast = new StyleableToast
                    .Builder(context)
                    .text(message)
                    .textColor(Color.WHITE)
                    .typeface(Typeface.createFromAsset(context.getAssets(), "fonts/Roboto-Regular.ttf"))
                    .backgroundColor(ContextCompat.getColor(context, R.color.normal_blue))
                    .build();

            styleableToast.show();

        } catch (Exception ex) {
            Crashlytics.logException(ex);
        }
    }

    public MD5Updater getMd5Updater() {
        return md5Updater;
    }

    public void startMd5Updater() {
        if (AppController.md5Updater == null) {
            AppController.md5Updater = new MD5Updater(mContext);
            AppController.md5Updater.executeOnExecutor(ThreadManager.Instance().getMuzikoBackgroundThreadPool());
        }
    }

    public void cancelMd5Updater() {
        if (AppController.md5Updater != null) {
            AppController.md5Updater.cancel(true);
            AppController.md5Updater = null;
        }
    }

    public String getAndroidID() {
        return androidID;
    }

    public Activity getCurrentActivity() {
        return currentActivity;
    }

    public void init(Context context) {
        mContext = context;
        androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        PrefsManager.Instance().init(CondomContext.wrap(mContext, "PrefsManager"));
        SettingsManager.Instance().init(CondomContext.wrap(mContext, "Settings"));
        GsonManager.Instance().init();
        MediaHelper.Instance().init(CondomContext.wrap(mContext, "MediaHelper"));
        NotificationController.Instance().init(CondomContext.wrap(mContext, "Notifications"));
        ImageManager.Instance().init(CondomContext.wrap(mContext, "ImageManager"));
        CloudManager.Instance().init(CondomContext.wrap(mContext, "CloudManager"));
        ThreadManager.Instance().init();
        MuzikoExoPlayer.Instance();
        FirebaseManager.Instance().init(CondomContext.wrap(mContext, "Firebase"));
        try {
            JobManager.create(CondomContext.wrap(mContext, "JobManager")).addJobCreator(new MuzikoJobCreator());
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @DebugLog
    public void removeAfterExisting(final Context context, final int index) {
        RemoveAfterExisting removeAfterExisting = new RemoveAfterExisting();
        removeAfterExisting.open(context, index);
    }

    @DebugLog
    public void scanMedia() {

        MediaHelper.Instance().loadMusicWrapper(false);
    }

    @DebugLog
    public void CompactMuzikoDB() {

        AsyncJob.doInBackground(
                () -> {
                    try {
                        Realm.compactRealm(MyApplication.realmConfiguration);

                    } catch (Exception e) {
                        Crashlytics.logException(e);
                    }
                }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());
    }

    @DebugLog
    public void addToPlaylist(
            Context ctx, ArrayList<QueueItem> queue, boolean showOverride) {
        PlayerConstants.PLAYLIST_QUEUE.clear();
        PlayerConstants.PLAYLIST_QUEUE.addAll(queue);

        PlaylistAdd pl = new PlaylistAdd(showOverride);
        pl.open(ctx);
    }

    @DebugLog
    public void addToQueue(Context context, ArrayList<QueueItem> queue, boolean next) {

        PlayerConstants.QUEUE_TYPE = 0;

        int index = PlayerConstants.QUEUE_INDEX;
        if (index >= PlayerConstants.QUEUE_LIST.size()) index = PlayerConstants.QUEUE_LIST.size();
        else index++;

        for (QueueItem q : queue) {
            QueueItem item = new QueueItem();
            item.copyQueue(q);
            item.hash();

            if (next) {
                PlayerConstants.QUEUE_LIST.add(index, item);
                index++;
            } else {
                PlayerConstants.QUEUE_LIST.add(item);
            }
        }

        if (PlayerConstants.QUEUE_SONG.title.length() == 0) {
            PrefsManager.Instance().setPlayPosition(0);
            PlayerConstants.QUEUE_INDEX = 0;
            PlayerConstants.QUEUE_TIME = 0;
            PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(0);
        }

        if (PlayerConstants.QUEUE_LIST.size() == 1) {
            PlayerConstants.QUEUE_INDEX = 0;
            PlayerConstants.QUEUE_TIME = 0;
            Instance().servicePlay(false);
        }

        context.sendBroadcast(new Intent(INTENT_QUEUE_CHANGED));

        if (next)
            toast(
                    context,
                    String.format(
                            Locale.ENGLISH,
                            "%d song%s will play next in queue!",
                            queue.size(),
                            queue.size() != 1 ? "s" : ""));
        else
            toast(
                    context,
                    String.format(
                            Locale.ENGLISH,
                            "%d song%s added to queue!",
                            queue.size(),
                            queue.size() != 1 ? "s" : ""));

        Instance().serviceDirty();
    }

    @DebugLog
    public void refreshMusicData(final String path) {

        scanner =
                new MediaScannerConnection(
                        mContext,
                        new MediaScannerConnection.MediaScannerConnectionClient() {

                            public void onMediaScannerConnected() {
                                scanner.scanFile(path, "audio/*");
                            }

                            public void onScanCompleted(String path, Uri uri) {
                                scanner.disconnect();
                            }
                        });

        scanner.connect();
    }

    @DebugLog
    public void removeAfter(final Context context, final QueueItem queueItem) {

        RemoveAfter removeAfter = new RemoveAfter();
        removeAfter.open(context, queueItem);
    }

    @DebugLog
    public void ringtone(Context context, String type, QueueItem ringtone) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, ringtone.data);
        values.put(MediaStore.MediaColumns.TITLE, ringtone.title);
        values.put(MediaStore.MediaColumns.MIME_TYPE, type);
        values.put(MediaStore.Audio.Media.ARTIST, ringtone.artist_name);
        values.put(MediaStore.Audio.Media.IS_RINGTONE, true);
        values.put(MediaStore.Audio.Media.DURATION, ringtone.duration);
        values.put(MediaStore.Audio.Media.IS_NOTIFICATION, true);
        values.put(MediaStore.Audio.Media.IS_ALARM, true);
        values.put(MediaStore.Audio.Media.IS_MUSIC, true);
        //TODO values.put(MediaStore.Audio.Media.IS_MUSIC, false);
        //TODO values.put(MediaStore.MediaColumns.SIZE, 1024);

        try {
            Uri uri = MediaStore.Audio.Media.getContentUriForPath(ringtone.data);
            context.getContentResolver()
                    .delete(uri, MediaStore.MediaColumns.DATA + "=\"" + ringtone.data + "\"", null);
            Uri newUri = context.getContentResolver().insert(uri, values);

            RingtoneManager.setActualDefaultRingtoneUri(
                    context, RingtoneManager.TYPE_RINGTONE, newUri);

            toast(context, "Ringtone set");

        } catch (Throwable t) {

            toast(context, "Unable to set ringtone!");
        }
    }

    @DebugLog
    public void details(final Activity context, final QueueItem queue) {

        if (queue.storage != 0 && queue.storage != 1 && queue.storage != 2) {
            isCloud = true;
            progressDialog = new MaterialDialog.Builder(context)
                    .title("Reading track data")
                    .content("Please wait")
                    .cancelable(false)
                    .progress(true, 0)
                    .build();
            progressDialog.show();
        }


        QueueItem queueItem = TrackRealmHelper.getTrack(queue.data);

        final File file = new File(queueItem.data);

        AsyncJob.doInBackground(() -> {
            MediaExtractor mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(queueItem.data);
                int numTracks = mediaExtractor.getTrackCount();
                for (int i = 0; i < numTracks; i++) {
                    MediaFormat format = mediaExtractor.getTrackFormat(i);
                    if (format == null) continue;

                    String mime = format.getString(MediaFormat.KEY_MIME);

                    int bitRate = format.containsKey(android.media.MediaFormat.KEY_BIT_RATE) ? format.getInteger(MediaFormat.KEY_BIT_RATE) : 0;
                    int sampleRate = format.getInteger(MediaFormat.KEY_SAMPLE_RATE);
                    long duration = format.containsKey(android.media.MediaFormat.KEY_DURATION) ? format.getLong(android.media.MediaFormat.KEY_DURATION) / 1000 : 0;
                    rates = new int[]{bitRate, sampleRate, (int) duration};
                }
                mediaExtractor.release();
            } catch (Exception e) {
                Crashlytics.logException(e);
            }
            final boolean result = true;
            AsyncJob.doOnMainThread(() -> {
                int duration = 0;
                if (rates != null) {
//        int duration = Utils.getInt(queue.duration, 0);
                    duration = rates[2];

                    if (duration == 0) {
                        int bitrate = rates[0] / 1000;
                        duration = (int) ((queueItem.size * 8 / 1000) / bitrate) * 1000;
                    }
                }

                String msg = msg = "<b>Title</b>:  " + queueItem.title;
                msg += "<br/><br/><b>Artist</b>:  " + queueItem.artist_name;
                msg += "<br/><br/><b>Album</b>: " + queueItem.album_name;
                msg += "<br/><br/><b>Genre</b>: " + queueItem.genre_name;
                msg += "<br/><br/><b>Duration</b>: " + Utils.getDuration(duration);
                msg += "<br/><br/><b>Size</b>: " + Utils.getByteFormat(queueItem.size > 0 ? queueItem.size : file.length());

                if (rates != null) {
                    msg += "<br/><br/><b>Bitrate</b>: " + String.format(Locale.ENGLISH, "%dkbps", rates[0] / 1000);
                    msg += "<br/><br/><b>Sampling rate</b>: " + String.format(Locale.ENGLISH, "%dHz", rates[1]);
                }
                if (queueItem.md5 == null) {
                    queueItem.md5 = MD5.calculateMD5(new File(queueItem.data));
                    TrackRealmHelper.updateMD5Hash(queueItem);
                }
                msg += "<br/><br/><b>MD5</b>: " + queueItem.md5;

                msg += "<br/><br/><b>Path</b>: " + queueItem.data;
                final String message = msg;

                //msg += "\n\nTrack: " + queue.track;
                //msg += "\n\nYear: " + queue.year;

                handler.postDelayed(() -> {

                    if (isCloud) {
                        if (progressDialog != null) {
                            progressDialog.dismiss();
                        }

                    }
                    new MaterialDialog.Builder(context)
                            .theme(Theme.LIGHT)
                            .titleColorRes(R.color.normal_blue)
                            .negativeColorRes(R.color.dialog_negetive_button)
                            .positiveColorRes(R.color.normal_blue)
                            .title("Details")
                            .content(Utils.fromHtml(message))
                            .positiveText("OK")
                            .onPositive((dialog, which) -> {
                            })
                            .neutralText("Go to Path")
                            .onNeutral(
                                    (dialog, which) -> {
                                        Intent folderIntent = new Intent(context, StorageActivity.class);
                                        folderIntent.putExtra(MyApplication.ARG_DATA, queue.data);
                                        ActivityCompat.startActivity(context, folderIntent, null);
                                    })
                            .show();
                }, isCloud ? 2000 : 0);
            });
        });
    }

    @DebugLog
    public void cutSong(QueueItem queue) {
        try {

            Intent intent = new Intent(Intent.ACTION_EDIT, Uri.parse(queue.data));
            intent.putExtra("SONG_NAME", queue.title);
            intent.putExtra("was_get_content_intent", false);
            intent.setClassName(
                    "com.muziko", "com.muziko.cutter.ringtone_lib.RingdroidEditActivity");
            mContext.startActivity(intent);

        } catch (Exception ex) {
            System.out.println("Could not start editor");

            toast(mContext, "Unable to start mp3 edit!");
            Crashlytics.logException(ex);
        }
    }

    @DebugLog
    public void addToPlaylist(Context ctx, QueueItem queue) {
        PlayerConstants.PLAYLIST_QUEUE.clear();
        PlayerConstants.PLAYLIST_QUEUE.add(queue);

        PlaylistAdd pl = new PlaylistAdd(false);
        pl.open(ctx);
    }

    @DebugLog
    public void mergePlayList(Context ctx, ArrayList<QueueItem> queueItems) {

        ArrayList<PlaylistItem> playlistItems = new ArrayList<>();

        for (QueueItem queueItem : queueItems) {
            PlaylistItem item = PlaylistItem.copyQueue(queueItem);
            playlistItems.add(item);
        }

        PlaylistMerge pl = new PlaylistMerge(playlistItems);
        pl.open(ctx);
    }

    @DebugLog
    public void addToQueue(Context context, QueueItem queue, boolean next) {
        QueueItem item = new QueueItem();
        item.copyQueue(queue);
        item.hash();

        PlayerConstants.QUEUE_TYPE = 0;
        if (next) {
            int index = PlayerConstants.QUEUE_INDEX;
            if (index >= PlayerConstants.QUEUE_LIST.size())
                index = PlayerConstants.QUEUE_LIST.size();
            else index++;

            PlayerConstants.QUEUE_LIST.add(index, item);
        } else {
            PlayerConstants.QUEUE_LIST.add(item);
        }

        if (PlayerConstants.QUEUE_SONG.title.length() == 0) {
            PrefsManager.Instance().setPlayPosition(0);
            PlayerConstants.QUEUE_INDEX = 0;
            PlayerConstants.QUEUE_TIME = 0;
            PlayerConstants.QUEUE_SONG = PlayerConstants.QUEUE_LIST.get(0);
        }

        if (PlayerConstants.QUEUE_LIST.size() == 1) {
            PlayerConstants.QUEUE_INDEX = 0;
            PlayerConstants.QUEUE_TIME = 0;
            Instance().servicePlay(false);
        }

        context.sendBroadcast(new Intent(INTENT_QUEUE_CHANGED));

        if (next) toast(context, "Song will play next in queue!");
        else toast(context, "Song added to queue!");

        Instance().serviceDirty();
    }

    @DebugLog
    public void multiTagEdit(final Context context, final ArrayList<QueueItem> queueItems) {
        MyApplication.multiTagList.clear();

        boolean hasSD = false;

        MyApplication.multiTagList.addAll(queueItems);

        if (hasSD && MyApplication.multiTagList.size() > 0) {
            new MaterialDialog.Builder(context)
                    .theme(Theme.LIGHT)
                    .titleColorRes(R.color.normal_blue)
                    .negativeColorRes(R.color.dialog_negetive_button)
                    .positiveColorRes(R.color.normal_blue)
                    .title("Multi Tag Edit")
                    .content(
                            MyApplication.getInstance()
                                    .getApplicationContext()
                                    .getString(R.string.multi_tagedit_warning))
                    .positiveText("OK")
                    .onPositive(
                            (dialog, which) -> {
                                if (MyApplication.multiTagList.size() > 1) {
                                    Intent intent = new Intent(context, MultiTagActivity.class);
                                    context.startActivity(intent);
                                } else {
                                    QueueItem queueItem = new QueueItem();
                                    queueItem = MyApplication.multiTagList.get(0);

                                    Intent intent = new Intent(context, TagsActivity.class);
                                    intent.putExtra("tag", TAG);
                                    intent.putExtra("item", queueItem);
                                    intent.putExtra("index", 0);
                                    context.startActivity(intent);
                                }
                            })
                    .negativeText("Cancel")
                    .show();
        } else if (hasSD && MyApplication.multiTagList.size() == 0) {

            new MaterialDialog.Builder(context)
                    .theme(Theme.LIGHT)
                    .titleColorRes(R.color.normal_blue)
                    .negativeColorRes(R.color.dialog_negetive_button)
                    .positiveColorRes(R.color.normal_blue)
                    .title("Multi Tag Edit")
                    .content(
                            MyApplication.getInstance()
                                    .getApplicationContext()
                                    .getString(R.string.multi_tagedit_error))
                    .positiveText("OK")
                    .show();
        } else {
            if (MyApplication.multiTagList.size() > 1) {
                Intent intent = new Intent(context, MultiTagActivity.class);
                context.startActivity(intent);
            } else {
                QueueItem queueItem = new QueueItem();
                queueItem = MyApplication.multiTagList.get(0);

                Intent intent = new Intent(context, TagsActivity.class);
                intent.putExtra("tag", TAG);
                intent.putExtra("item", queueItem);
                intent.putExtra("index", 0);
                context.startActivity(intent);
            }
        }
    }

    public void showFirebaseOverlimitDialog(Activity activity) {

        UpgradeStorage upgradeStorage = new UpgradeStorage();
        upgradeStorage.open(activity);

//            new MaterialDialog.Builder(activity).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue)
//                    .title("Storage Limit Reached")
//                    .content("Your cloud storage is full " + FirebaseManager.Instance().getFirebaseTrackCount() + " of " + FirebaseManager.Instance().getSubscriptionType().getSongLimit() + " Please upgrade to a plan that suits your needs")
//                    .negativeText("Cancel")
//                    .positiveText("Upgrade")
//                    .onPositive(new MaterialDialog.SingleButtonCallback() {
//                        @Override
//                        public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                            Intent activityIntent = new Intent(activity, SubscriptionUpgradeActivity.class);
//                            activity.startActivity(activityIntent);
//                            activity.overridePendingTransition(R.anim.slide_up, R.anim.slide_down);
//                        }
//                    }).show();
    }

    public void showMultipleSubscriptionsDialog(Context context, List<String> subs) {

        ArrayList<String> subscriptions = new ArrayList<>();
        for (String sub : subs) {
            for (MuzikoSubscriptionType subscriptionType : FirebaseManager.Instance().getSubscriptionTypes()) {
                if (sub.equals(subscriptionType.getSubscriptionTypeID())) {
                    subscriptions.add(sub + "-" + subscriptionType.getSubscriptionName());
                }
            }
        }

        new MaterialDialog.Builder(context).theme(Theme.LIGHT).titleColorRes(R.color.normal_blue).negativeColorRes(R.color.dialog_negetive_button).positiveColorRes(R.color.normal_blue)
                .title("Storage Subscriptions")
                .content("You currently have multiple subsriptions active. Please cancel one in the Play Store App")
                .items(subscriptions)
                .negativeText("Not Now")
                .positiveText("Take me there")
                .onPositive((dialog, which) -> {
                    final String appPackageName = context.getPackageName();
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                }).show();
    }

    @DebugLog
    public void clearAddToQueue(final Context context, final ArrayList<QueueItem> queue) {

        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .positiveColorRes(R.color.normal_blue)
                .title("Play multiple tracks")
                .content(
                        "Current queue will be cleared. Would you still like to play the selected songs?")
                .positiveText("OK")
                .onPositive(
                        (dialog, which) -> {
                            PlayerConstants.QUEUE_TYPE = 0;
                            PlayerConstants.QUEUE_LIST.clear();

                            for (QueueItem q : queue) {
                                QueueItem item = new QueueItem();
                                item.copyQueue(q);
                                item.hash();

                                PlayerConstants.QUEUE_LIST.add(item);
                            }

                            PlayerConstants.QUEUE_INDEX = 0;
                            PlayerConstants.QUEUE_TIME = 0;

                            Instance().servicePlay(false);

                            context.sendBroadcast(new Intent(INTENT_QUEUE_CHANGED));

                            toast(
                                    context,
                                    String.format(Locale.ENGLISH,
                                            "%d song%s added to queue!",
                                            queue.size(), queue.size() != 1 ? "s" : ""));

                            Instance().serviceDirty();
                        })
                .negativeText("Cancel")
                .show();
    }

    @DebugLog
    public void playAll() {
        ArrayList<QueueItem> list = new ArrayList<>(TrackRealmHelper.getTracks(0).values());
        Instance().play(0, 0, list);
    }

    @DebugLog
    public void playCurrentSong(QueueItem queueItem) {

        if (PlayerConstants.QUEUE_LIST.size() == 0) {
            PlayerConstants.QUEUE_LIST.add(queueItem);
            PlayerConstants.QUEUE_INDEX = 0;
        } else {
            PlayerConstants.QUEUE_LIST.add(PlayerConstants.QUEUE_INDEX + 1, queueItem);
            PlayerConstants.QUEUE_INDEX = PlayerConstants.QUEUE_INDEX + 1;
        }

        mContext.sendBroadcast(new Intent(INTENT_QUEUE_CHANGED));

        servicePlay(false);

        serviceDirty();
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager =
                (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service :
                manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @DebugLog
    public void editSong(
            Activity activity, final String src, final int position, final QueueItem queue) {

        Intent intent = new Intent(mContext, TagsActivity.class);
        intent.putExtra("tag", src);
        intent.putExtra("item", queue);
        intent.putExtra("index", position);
        activity.startActivity(intent);
    }

    @DebugLog
    public void editRating(
            final Context context, final String src, final int position, final QueueItem queue) {

        new MaterialDialog.Builder(context)
                .theme(Theme.LIGHT)
                .titleColorRes(R.color.normal_blue)
                .negativeColorRes(R.color.dialog_negetive_button)
                .positiveColorRes(R.color.normal_blue)
                .title("Rating")
                .customView(R.layout.dialog_rating, false)
                .positiveText("Proceed")
                .onPositive(
                        (dialog, which) -> {
                            Intent intent = new Intent(context, TagsActivity.class);
                            intent.putExtra("tag", src);
                            intent.putExtra("item", queue);
                            intent.putExtra("index", position);
                            context.startActivity(intent);
                        })
                .negativeText("Cancel")
                .show();
    }

    @DebugLog
    public void gotoArtist(Activity context, QueueItem item, View v) {
        QueueItem artist = TrackRealmHelper.getArtists().get(item.artist_name);
        if (artist == null) {
            toast(context, "Artist not found!");
            return;
        }

        Intent arIntent = new Intent(context, PlayerListActivity.class);
        arIntent.putExtra(MyApplication.ARG_ID, artist.id);
        arIntent.putExtra(MyApplication.ARG_ART, artist.album);
        arIntent.putExtra(MyApplication.ARG_NAME, artist.title);
        arIntent.putExtra(MyApplication.ARG_TYPE, PlayerConstants.QUEUE_TYPE_ARTISTS);
        arIntent.putExtra(MyApplication.ARG_DATA, artist.title);
        arIntent.putExtra(MyApplication.ARG_DURATION, artist.duration);
        arIntent.putExtra(MyApplication.ARG_SONGS, artist.songs);

        if (v != null) {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            context,
                            new Pair<>(
                                    v,
                                    context.getString(R.string.transition_name_coverart)
                                            + MyApplication.transitionPosition));
            ActivityCompat.startActivity(context, arIntent, options.toBundle());
        } else {
            ActivityCompat.startActivity(context, arIntent, null);
        }

    }

    @DebugLog
    public void sendTracks(Context context, ArrayList<QueueItem> queueItems) {

        Share share = new Share();
        share.open(context, queueItems);
    }

    @DebugLog
    public void sendTracksWifi(Activity context, ArrayList<QueueItem> queueItems) {

        String json = GsonManager.Instance().getGson().toJson(queueItems);
        Intent shareIntent = new Intent(context, ShareWifiActivity.class);
        shareIntent.putExtra(MyApplication.ARG_DATA, json);
        ActivityCompat.startActivity(context, shareIntent, null);
    }

    @DebugLog
    public void gotoAlbum(Activity context, QueueItem item, View v) {
        QueueItem album = TrackRealmHelper.getAlbums().get(item.album_name);
        if (album == null) {
            toast(context, "Artist not found!");
            return;
        }

        Intent alIntent = new Intent(context, PlayerListActivity.class);
        alIntent.putExtra(MyApplication.ARG_ID, album.id);
        alIntent.putExtra(MyApplication.ARG_ART, album.album);
        alIntent.putExtra(MyApplication.ARG_NAME, album.title);
        alIntent.putExtra(MyApplication.ARG_TYPE, PlayerConstants.QUEUE_TYPE_ALBUMS);
        alIntent.putExtra(MyApplication.ARG_DATA, album.title);
        alIntent.putExtra(MyApplication.ARG_DURATION, album.duration);
        alIntent.putExtra(MyApplication.ARG_SONGS, album.songs);

        if (v != null) {
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(
                            context,
                            new Pair<>(
                                    v,
                                    context.getString(R.string.transition_name_coverart)
                                            + MyApplication.transitionPosition));
            ActivityCompat.startActivity(context, alIntent, options.toBundle());
        } else {
            ActivityCompat.startActivity(context, alIntent, null);
        }

    }

    @DebugLog
    public void actionItem(final Activity context, int position, QueueItem item, int type, final int action) {
        final ProgressDialog progressDialog = new ProgressDialog(context);
        progressDialog.setTitle("Loading songs");
        progressDialog.setMessage("Please wait...");
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

        TrackGrouper task = new TrackGrouper(context, type, item.title, new TrackGrouper.TrackGrouperListener() {
            @Override
            public void onTrackGrouperStarted() {
                progressDialog.show();
            }

            @Override
            public void onTrackGrouperCompleted(ArrayList<QueueItem> list) {
                switch (action) {
                    case PlayerConstants.QUEUE_ACTION_DELETE:
                        deleteItem(context, list);
                        break;

                    case PlayerConstants.QUEUE_ACTION_QUEUE:
                        addToQueue(context, list, false);
                        break;

                    case PlayerConstants.QUEUE_ACTION_NEXT:
                        addToQueue(context, list, true);
                        break;

                    case PlayerConstants.QUEUE_ACTION_SAVE:
                        addToPlaylist(context, list, true);
                        break;

                    case PlayerConstants.QUEUE_ACTION_SHARE:
                        shareItems(context, list, "");
                        break;
                }

                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        });
        task.execute();
    }

    @DebugLog
    private void shareItems(Context context, ArrayList<QueueItem> list, String subject) {
        try {
            Intent share = new Intent(Intent.ACTION_SEND_MULTIPLE);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_TEXT, subject);
            share.putExtra(Intent.EXTRA_SUBJECT, subject);

            ArrayList<Uri> files = new ArrayList<>();
            for (QueueItem item : list) {
                File file = new File(item.data);
                Uri uri = Uri.fromFile(file);
                files.add(uri);
            }
            share.putParcelableArrayListExtra(Intent.EXTRA_STREAM, files);
            context.startActivity(Intent.createChooser(share, "Share music via..."));

        } catch (Exception ex) {
            toast(context, "Error sharing songs! Contact Developer");
            Crashlytics.logException(ex);
        }
    }

    @DebugLog
    private void deleteItem(final Activity context, final ArrayList<QueueItem> list) {
        Utils.askDelete(
                context,
                "Delete Songs",
                String.format(
                        "This will delete song%s permanently from this device, do you want to proceed ?",
                        list.size() != 1 ? "s" : ""),
                () -> {
                    TrackDelete tr = new TrackDelete(context, PlayerConstants.QUEUE_TYPE_TRACKS, () -> toast(context, String.format("Song%s deleted from device", list.size() != 1 ? "s" : "")));
                    tr.execute(list);
                });
    }

    @DebugLog
    public boolean shouldShowAd() {
        boolean premium = PrefsManager.Instance().getPremium();
        boolean freeWeek =
                (PrefsManager.Instance().getRegisterTime() + MuzikoConstants.weekMilliseconds
                        > System.currentTimeMillis());

//        return !premium && !freeWeek;
        return false;
    }

    @DebugLog
    public void openUrl(String url) {
        try {
            Intent launchBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            mContext.startActivity(launchBrowser);
        } catch (Exception ex) {
            toast(mContext, "Unable to open url!");
            Crashlytics.logException(ex);
        }
    }

    @DebugLog
    public void search(String query) {
        try {
            Intent intent = new Intent(Intent.ACTION_WEB_SEARCH);
            intent.putExtra(SearchManager.QUERY, query); // query contains search string
            mContext.startActivity(intent);
        } catch (Exception ex) {
            toast(mContext, "Unable to start search!");
            Crashlytics.logException(ex);
        }
    }

    @DebugLog
    public void updateQueueIndex() {
        for (int i = 0; i < PlayerConstants.QUEUE_LIST.size(); i++) {
            if (PlayerConstants.QUEUE_LIST.get(i).hash.equals(PlayerConstants.QUEUE_SONG.hash)) {
                PlayerConstants.QUEUE_INDEX = i;
                break;
            }
        }
    }

    @DebugLog
    public void shareApp() {
        //Utils.shareSong(MuzikoActivity.this);
        String share =
                "Download Muziko Music Player http://play.google.com/store/apps/details?id="
                        + mContext.getPackageName();

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, share);
        Intent openin = Intent.createChooser(intent, "Share To...");
        mContext.startActivity(openin);
    }

    @DebugLog
    public void shareSong(Activity activity, QueueItem item) {
        try {
            Intent share = new Intent(Intent.ACTION_SEND);
            share.setType("audio/*");
            share.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + item.data));
            share.putExtra(Intent.EXTRA_SUBJECT, item.title);
            share.putExtra(Intent.EXTRA_TEXT, item.title);
            activity.startActivity(Intent.createChooser(share, "Share music via..."));
        } catch (Exception ex) {
            toast(mContext, "Error sharing file! Contact Developer");
            Crashlytics.logException(ex);
        }
    }

    @DebugLog
    public void shareSongs(ArrayList<QueueItem> queuelist) {

        ArrayList<Uri> arrayUri = new ArrayList<>();
        for (QueueItem queue : queuelist) {
            arrayUri.add(Uri.parse("file://" + queue.data));
        }

        Intent intentShareFile = new Intent();
        intentShareFile.setAction(Intent.ACTION_SEND_MULTIPLE);
        intentShareFile.putParcelableArrayListExtra(Intent.EXTRA_STREAM, arrayUri);
        intentShareFile.setType("audio/*");
        mContext.startActivity(intentShareFile);
    }

    @DebugLog
    public void exit() {
        serviceExit();
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(homeIntent);
    }

    @DebugLog
    public void exitKeepPlaying() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mContext.startActivity(homeIntent);
    }

    @DebugLog
    public void scanMedia(Activity activity, CoordinatorLayout coordinatorLayout) {
        MediaHelper.Instance().scanMediaFiles(activity, coordinatorLayout);
    }

    @DebugLog
    public void serviceThumb() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_THUMB);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceACRFingerPrint() {
        Intent intent = new Intent(mContext, MuzikoFingerprintService.class);
        intent.setAction(MuzikoFingerprintService.ACTION_ACR_CUSTOM);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceACR() {
        Intent intent = new Intent(mContext, MuzikoFingerprintService.class);
        intent.setAction(MuzikoFingerprintService.ACTION_ACR);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceResume(boolean widget) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_RESUME);
        intent.putExtra(SongService.ARG_WIDGET, widget);
        mContext.startService(intent);
    }

    @DebugLog
    public void servicePause() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_PAUSE);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceSeek(int seek) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_SEEK);
        intent.putExtra(SongService.ARG_INDEX, seek);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceUnqueue(String hash) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_UNQUEUE);
        intent.putExtra(SongService.ARG_DATA, hash);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceStop() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_STOP);
        mContext.startService(intent);
    }

    @DebugLog
    private void serviceExit() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_EXIT);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceClear() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_CLEAR);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceIndex(int index) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_INDEX);
        intent.putExtra(SongService.ARG_INDEX, index);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceBack() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_BACK);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceNext() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_NEXT);
        mContext.startService(intent);
    }

    @DebugLog
    public void servicePrev() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_PREV);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceDelete(long type, String hash, String data) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_DELETE);
        intent.putExtra(SongService.ARG_TYPE, type);
        intent.putExtra(SongService.ARG_DATA, data);
        intent.putExtra(SongService.ARG_HASH, hash);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceEqualizer(int state) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_EQUALIZER);
        intent.putExtra(SongService.ARG_INDEX, state);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceNotification(int updateType) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_NOTIFICATION);
        intent.putExtra(SongService.ARG_NOTIFICATION_UPDATE_TYPE, updateType);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceLockScreen() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_LOCKSCREEN);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceGaplessNext() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_GAPLESS);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceStopLyrics() {
        Intent intent = new Intent(mContext, LyricsDownloaderService.class);
        intent.setAction(MyApplication.ACTION_CANCEL_UPDATE_LYRICS);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceUpdateCache(String data) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(MyApplication.ACTION_UPDATE_CACHE);
        intent.putExtra(SongService.ARG_DATA, data);
        mContext.startService(intent);
    }

    public void serviceToggle() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_TOGGLE);
        mContext.startService(intent);
    }

    @DebugLog
    public void serviceDirty() {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_DIRTY);
        mContext.startService(intent);
    }

    public void play(long type, int position, ArrayList<QueueItem> list) {
        AsyncJob.doInBackground(() -> {
            ArrayList<QueueItem> items = new ArrayList<>(list);

            PlayerConstants.QUEUE_TYPE = type;
            PlayerConstants.QUEUE_LIST.clear();
            PlayerConstants.QUEUE_LIST.addAll(items);
            PlayerConstants.QUEUE_INDEX = position;
            PlayerConstants.QUEUE_TIME = 0;

            servicePlay(false);

            serviceDirty();
        }, ThreadManager.Instance().getMuzikoBackgroundThreadPool());

    }

    public void servicePlay(boolean widget) {
        Intent intent = new Intent(mContext, SongService.class);
        intent.setAction(SongService.ACTION_PLAY);
        intent.putExtra(SongService.ARG_WIDGET, widget);
        mContext.startService(intent);
    }

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {

    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivity = activity;
    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {

    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
