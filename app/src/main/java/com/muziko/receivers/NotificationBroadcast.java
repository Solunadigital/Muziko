package com.muziko.receivers;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.KeyEvent;

import com.muziko.MyApplication;
import com.muziko.common.events.FirebaseRefreshEvent;
import com.muziko.common.models.QueueItem;
import com.muziko.database.TrackRealmHelper;
import com.muziko.manager.AppController;
import com.muziko.manager.CloudManager;
import com.muziko.manager.FirebaseManager;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.salut.SalutFileDownloader;
import com.muziko.salut.SalutFileUploader;
import com.muziko.service.SongService;
import com.muziko.tasks.CloudDownloadTask;
import com.muziko.tasks.CloudUploadTask;
import com.muziko.tasks.CoverArtDownloader;
import com.muziko.tasks.FirebaseDownloadTask;
import com.muziko.tasks.FirebaseUploadTask;
import com.muziko.tasks.ShareTrackDownloader;
import com.muziko.tasks.ShareTrackUploader;
import com.muziko.widgets.QueueWidget;
import com.muziko.widgets.StandardWidget;

import org.greenrobot.eventbus.EventBus;


public class NotificationBroadcast extends BroadcastReceiver {
    public String TAG = "NotificationBroadcast";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) return;

        if (action.equals(Intent.ACTION_MEDIA_BUTTON)) {
            KeyEvent keyEvent = (KeyEvent) intent.getExtras().get(Intent.EXTRA_KEY_EVENT);
            if (keyEvent.getAction() != KeyEvent.ACTION_DOWN) return;

            switch (keyEvent.getKeyCode()) {
                case KeyEvent.KEYCODE_HEADSETHOOK:
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                    AppController.Instance().serviceToggle();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PLAY:
                    AppController.Instance().servicePlay(false);
                    break;

                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    AppController.Instance().servicePause();
                    break;

                case KeyEvent.KEYCODE_MEDIA_STOP:
                    AppController.Instance().serviceStop();
                    break;

                case KeyEvent.KEYCODE_MEDIA_NEXT:
                    AppController.Instance().serviceNext();
                    break;

                case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                    AppController.Instance().servicePrev();
                    break;
            }
        } else {

            switch (action) {
                case SongService.NOTIFY_PLAY:
                    if (PlayerConstants.QUEUE_STATE == PlayerConstants.QUEUE_STATE_PAUSED)
                        AppController.Instance().serviceResume(false);
                    else AppController.Instance().servicePlay(false);

                    break;
                case SongService.NOTIFY_PLAY_WIDGET:
                    AppController.Instance().serviceResume(true);

                    break;
                case SongService.NOTIFY_PAUSE:
                    AppController.Instance().servicePause();

                    break;
                case SongService.NOTIFY_NEXT:
                    AppController.Instance().serviceNext();

                    break;
                case SongService.NOTIFY_PREVIOUS:
                    AppController.Instance().servicePrev();

                    break;
                case SongService.NOTIFY_DELETE:
                    AppController.Instance().exit();

                    break;
                case SongService.NOTIFY_REPEAT: {
                    int repeat = PrefsManager.Instance().getPlayRepeat();
                    repeat++;
                    if (repeat >= PlayerConstants.REPEAT_TOTAL) repeat = 0;

                    PrefsManager.Instance().setPlayRepeat(repeat);

                    StandardWidget standardWidget = new StandardWidget();
                    standardWidget.onUpdate(context);

                    QueueWidget queueWidget = new QueueWidget();
                    queueWidget.onUpdate(context);

                    Intent repeatIntent = new Intent(AppController.INTENT_TRACK_REPEAT);
                    context.sendBroadcast(repeatIntent);

                    AppController.Instance().serviceNotification(SongService.NOTIFICATION_REPEAT);
                    break;
                }
                case SongService.NOTIFY_SHUFFLE: {
                    PrefsManager.Instance().setPlayShuffle(!PrefsManager.Instance().getPlayShuffle(context));

                    StandardWidget standardWidget = new StandardWidget();
                    standardWidget.onUpdate(context);

                    QueueWidget queueWidget = new QueueWidget();
                    queueWidget.onUpdate(context);

                    Intent shuffleIntent = new Intent(AppController.INTENT_TRACK_SHUFFLE);
                    context.sendBroadcast(shuffleIntent);

                    AppController.Instance().serviceNotification(SongService.NOTIFICATION_SHUFFLE);
                    break;
                }
                case SongService.NOTIFY_PLAY_SONG: {
                    StandardWidget standardWidget = new StandardWidget();
                    standardWidget.onUpdate(context);

                    QueueWidget queueWidget = new QueueWidget();
                    queueWidget.onUpdate(context);

                    break;
                }
                case SongService.NOTIFY_PLAY_QUEUE_SONG: {
                    String data = intent.getStringExtra("data");
                    int position = 0;

                    for (int i = 0; i < PlayerConstants.QUEUE_LIST.size(); i++) {
                        QueueItem queue = new QueueItem();
                        queue = PlayerConstants.QUEUE_LIST.get(i);
                        if (queue.data.equals(data)) {
                            position = i;
                        }
                    }
                    AppController.Instance()
                            .play(PlayerConstants.QUEUE_TYPE_QUEUE, position, PlayerConstants.QUEUE_LIST);

                    StandardWidget standardWidget = new StandardWidget();
                    standardWidget.onUpdate(context);

                    QueueWidget queueWidget = new QueueWidget();
                    queueWidget.onUpdate(context);
                    break;
                }

                case AppController.NOTIFY_CANCEL_DOWNLOAD: {
                    String shareUrl = intent.getStringExtra("shareUrl");
                    ShareTrackDownloader shareTrackDownloader = FirebaseManager.firebaseShareDownloaderTasks.get(shareUrl);

                    if (shareTrackDownloader != null) {
                        shareTrackDownloader.cancelDownload();
                    }
                    FirebaseManager.firebaseShareDownloaderTasks.remove(shareUrl);
                    EventBus.getDefault().post(new FirebaseRefreshEvent(0));
                    break;
                }

                case AppController.NOTIFY_CANCEL_UPLOAD: {
                    String data = intent.getStringExtra("data");
                    ShareTrackUploader shareTrackUploader = FirebaseManager.firebaseShareUploaderTasks.get(data);

                    if (shareTrackUploader != null) {
                        shareTrackUploader.cancelUpload();
                    }
                    FirebaseManager.firebaseShareUploaderTasks.remove(data);
                    EventBus.getDefault().post(new FirebaseRefreshEvent(0));

                    break;
                }

                case AppController.NOTIFY_CANCEL_WIFI_DOWNLOAD: {
                    String data = intent.getStringExtra("data");
                    SalutFileDownloader salutFileDownloader = MyApplication.shareDownloaderList.get(data);

                    if (salutFileDownloader != null) {
                        salutFileDownloader.cancel(true);
                    }

                    MyApplication.shareDownloaderList.remove(data);

                    Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
                    progressIntent.putExtra("url", data);
                    progressIntent.putExtra("progress", -1);
                    context.sendBroadcast(progressIntent);

                    break;
                }

                case AppController.NOTIFY_CANCEL_WIFI_UPLOAD: {
                    String data = intent.getStringExtra("data");
                    SalutFileUploader salutFileUploader = MyApplication.shareUploaderList.get(data);

                    if (salutFileUploader != null) {
                        salutFileUploader.cancel(true);
                    }

                    MyApplication.shareUploaderList.remove(data);

                    Intent progressIntent = new Intent(AppController.INTENT_DOWNLOAD_PROGRESS);
                    progressIntent.putExtra("url", data);
                    progressIntent.putExtra("progress", -1);
                    context.sendBroadcast(progressIntent);

                    break;
                }

                case MyApplication.NOTIFY_CANCEL_COVERART_DOWNLOAD: {
                    if (MyApplication.coverArtDownloaders.size() > 0) {
                        CoverArtDownloader coverArtDownloader = MyApplication.coverArtDownloaders.get(0);
                        coverArtDownloader.cancel(true);
                    }

                    break;
                }

                case MyApplication.NOTIFY_CANCEL_LYRICS_DOWNLOAD: {
                    AppController.Instance().serviceStopLyrics();

                    break;
                }

                case MyApplication.NOTIFY_CANCEL_HASH: {
                    AppController.Instance().cancelMd5Updater();

                    break;
                }

                case SongService.NOTIFY_CANCEL_CLOUD_DOWNLOAD: {
                    String downloadPath = intent.getStringExtra("path");
                    CloudDownloadTask cloudDownloadTask = CloudManager.cloudDownloadList.get(downloadPath);

                    if (cloudDownloadTask != null) {
                        cloudDownloadTask.cancelDownload();
                        cloudDownloadTask.cancel(true);
                    }
                    CloudManager.cloudDownloadList.remove(downloadPath);
                    EventBus.getDefault().post(new FirebaseRefreshEvent(0));
                    break;
                }

                case SongService.NOTIFY_CANCEL_CLOUD_UPLOAD: {
                    String uploadPath = intent.getStringExtra("path");
                    CloudUploadTask cloudUploadTask = CloudManager.cloudUploaderList.get(uploadPath);

                    if (cloudUploadTask != null) {
                        cloudUploadTask.cancelUpload();
                        cloudUploadTask.cancel(true);
                    }
                    CloudManager.cloudUploaderList.remove(uploadPath);
                    EventBus.getDefault().post(new FirebaseRefreshEvent(0));
                    break;
                }

                case SongService.UPDATER_NOTIFY_DISMISS:
                    NotificationManager nManager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    nManager.cancel(intent.getIntExtra("id", 0));
                    break;

                case SongService.UPDATER_NOTIFY_DISMISS_ALWAYS:
                    NotificationManager manager =
                            (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                    manager.cancel(intent.getIntExtra("id", 0));
                    PrefsManager.Instance().setDontShowUpdates(true);
                    break;

                case SongService.UPDATER_NOTIFY_UPDATE:
                    final String appPackageName = context.getPackageName();
                    try {
                        context.startActivity(
                                new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + appPackageName)));
                    } catch (android.content.ActivityNotFoundException anfe) {
                        context.startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("http://play.google.com/store/apps/details?id=" + appPackageName)));
                    }
                    break;

                case AppController.NOTIFY_CANCEL_FIREBASE_DOWNLOAD: {
                    String shareUrl = intent.getStringExtra("shareUrl");
                    FirebaseDownloadTask firebaseDownloadTask =
                            FirebaseManager.Instance().getFirebaseDownloadTasks().get(shareUrl);

                    if (firebaseDownloadTask != null) {
                        firebaseDownloadTask.cancelDownload();
                        firebaseDownloadTask.cancel(true);
                    }
                    FirebaseManager.Instance().getFirebaseDownloadTasks().remove(shareUrl);
                    EventBus.getDefault().post(new FirebaseRefreshEvent(0));
                    break;
                }

                case AppController.NOTIFY_CANCEL_FIREBASE_UPLOAD: {
                    String data = intent.getStringExtra("data");
                    FirebaseUploadTask firebaseUploadTask = FirebaseManager.Instance().getFirebaseUploadTasks().get(data);
                    FirebaseManager.firebaseCancelledUploads.put(data, System.currentTimeMillis());

                    if (firebaseUploadTask != null) {
                        firebaseUploadTask.cancelUpload();
                        firebaseUploadTask.cancel(true);
                    }
                    FirebaseManager.Instance().setUploadRunning(false);
                    FirebaseManager.Instance().getFirebaseUploadTasks().remove(data);
                    EventBus.getDefault().post(new FirebaseRefreshEvent(0));

                    break;
                }

                case AppController.NOTIFY_REMOVE_FIREBASE_LIBRARY_UPLOAD: {
                    String data = intent.getStringExtra("data");
                    QueueItem queueItem = TrackRealmHelper.getTrack(data);
                    FirebaseUploadTask firebaseUploadTask = FirebaseManager.Instance().getFirebaseUploadTasks().get(data);
                    FirebaseManager.firebaseCancelledUploads.put(data, System.currentTimeMillis());

                    if (firebaseUploadTask != null) {
                        firebaseUploadTask.cancelUpload();
                        firebaseUploadTask.cancel(true);
                    }
                    FirebaseManager.Instance().getFirebaseRemovedList().add(queueItem);
                    FirebaseManager.Instance().deleteLibrary(queueItem);
                    FirebaseManager.Instance().setUploadRunning(false);
                    FirebaseManager.Instance().getFirebaseUploadTasks().remove(data);

                    break;
                }

                case AppController.NOTIFY_REMOVE_FIREBASE_FAV_UPLOAD: {
                    String data = intent.getStringExtra("data");
                    FirebaseUploadTask firebaseUploadTask = FirebaseManager.Instance().getFirebaseUploadTasks().get(data);
                    FirebaseManager.firebaseCancelledUploads.put(data, System.currentTimeMillis());

                    if (firebaseUploadTask != null) {
                        firebaseUploadTask.cancelUpload();
                        firebaseUploadTask.cancel(true);
                    }
                    FirebaseManager.Instance().getFirebaseUploadTasks().remove(data);
                    QueueItem queueItem = TrackRealmHelper.getTrack(data);
                    if (queueItem != null) {
                        TrackRealmHelper.toggleSync(queueItem, false);
                    }
                    EventBus.getDefault().post(new FirebaseRefreshEvent(0));

                    break;
                }
            }
        }
    }

    public String ComponentName() {
        return this.getClass().getName();
    }
}
