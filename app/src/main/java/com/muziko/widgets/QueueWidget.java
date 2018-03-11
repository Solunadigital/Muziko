package com.muziko.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.RemoteViews;

import com.arasthel.asyncjob.AsyncJob;
import com.muziko.MyApplication;
import com.muziko.R;
import com.muziko.activities.splash.LoaderActivity;
import com.muziko.common.models.QueueItem;
import com.muziko.events.LoadQueueEvent;
import com.muziko.manager.AppController;
import com.muziko.manager.PrefsManager;
import com.muziko.mediaplayer.PlayerConstants;
import com.muziko.service.SongService;
import com.squareup.picasso.Picasso;

import pl.tajchert.buswear.EventBus;

/**
 * Created by dev on 23/08/2016.
 */

public class QueueWidget extends AppWidgetProvider {

	private boolean updating;

	/**
	 * Returns number of cells needed for given size of the widget.
	 *
	 * @param size Widget size in dp.
	 * @return Size in number of cells.
	 */
	private int getCellsForSize(int size) {
		int n = 2;
		while (70 * n - 30 < size) {
			++n;
		}
		return n - 1;
	}

	private PendingIntent getPendingSelfIntent(Context context, String action) {
		// An explicit intent directed at the current class (the "self").
		Intent intent = new Intent(context, getClass());
		intent.setAction(action);
		return PendingIntent.getBroadcast(context, 0, intent, 0);
	}

	/**
	 * Determine appropriate view based on width provided.
	 *
	 * @param minWidth
	 * @param minHeight
	 * @return
	 */
	private RemoteViews getRemoteViews(Context context, int minWidth,
	                                   int minHeight) {
		// First find out rows and columns based on width provided.
		int rows = getCellsForSize(minHeight);
		int columns = getCellsForSize(minWidth);

		if (columns <= 3) {
			return new RemoteViews(context.getPackageName(),
					R.layout.widget_queue_narrow);
		} else {
			return new RemoteViews(context.getPackageName(),
					R.layout.widget_queue);
		}
	}

    private void setListeners(Context context, RemoteViews view) {

        Intent previous = new Intent(SongService.NOTIFY_PREVIOUS);
        Intent delete = new Intent(SongService.NOTIFY_DELETE);
        Intent pause = new Intent(SongService.NOTIFY_PAUSE);
        Intent next = new Intent(SongService.NOTIFY_NEXT);
        Intent play = new Intent(SongService.NOTIFY_PLAY_WIDGET);
        Intent repeat = new Intent(SongService.NOTIFY_REPEAT);
        Intent shuffle = new Intent(SongService.NOTIFY_SHUFFLE);
        Intent playsong = new Intent(SongService.NOTIFY_PLAY_QUEUE_SONG);
        Intent myIntent = new Intent(context, LoaderActivity.class);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        myIntent.setAction(Intent.ACTION_MAIN);
        myIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.imageViewAlbumArt, pendingIntent);

        PendingIntent pDelete = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnDelete, pDelete);

        PendingIntent pPrevious = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

        PendingIntent pNext = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnNext, pNext);

        PendingIntent pPause = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnPause, pPause);

        if (PlayerConstants.QUEUE_LIST.size() == 0) {

            PendingIntent pplaysong = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, playsong, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.btnPlay, pplaysong);
        } else {
            PendingIntent pPlay = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
            view.setOnClickPendingIntent(R.id.btnPlay, pPlay);
        }


        PendingIntent prepeat = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, repeat, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnRepeat, prepeat);

        PendingIntent pshuffle = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, shuffle, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setOnClickPendingIntent(R.id.btnShuffle, pshuffle);

        //ListView
        PendingIntent pplaysong = PendingIntent.getBroadcast(MyApplication.getInstance().getApplicationContext(), 0, playsong, PendingIntent.FLAG_UPDATE_CURRENT);
        view.setPendingIntentTemplate(R.id.listViewWidget, pplaysong);

    }

    /**
     * A general technique for calling the onUpdate method,
     * requiring only the context parameter.
     *
     * @author John Bentley, based on Android-er code.
     * @see <a href="http://android-er.blogspot.com
     * .au/2010/10/update-widget-in-onreceive-method.html">
     * Android-er > 2010-10-19 > Update Widget in onReceive() method</a>
     */
    public void onUpdate(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance
                (context);

        // Uses getClass().getName() rather than MyWidget.class.getName() for
        // portability into any App Widget Provider Class
        ComponentName thisAppWidgetComponentName =
                new ComponentName(context.getPackageName(), getClass().getName()
                );
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                thisAppWidgetComponentName);
        onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

	}

	@Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager,
	                     final int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		// Loop for every App Widget instance that belongs to this provider.
		// Noting, that is, a user might have multiple instances of the same
		// widget on
		// their home screen.

		if (updating) {
			return;

		}
		updating = true;

		if (!AppController.Instance().isMyServiceRunning(SongService.class)) {
			context.startService(new Intent(context, SongService.class));
		}

		for (final int appWidgetID : appWidgetIds) {

			Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetID);
			int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
			int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

			final RemoteViews remoteViews = getRemoteViews(context, minWidth, minHeight);

			QueueItem queueItem = new QueueItem();
			boolean updateList = false;

            if (PrefsManager.Instance().getQueueWidgetChange()) {
                updateList = true;
                PrefsManager.Instance().setQueueWidgetChange(false);
            }

			if (PlayerConstants.QUEUE_LIST.size() == 0) {
                EventBus.getDefault(context).postLocal(new LoadQueueEvent(0));
            }
			queueItem = PlayerConstants.QUEUE_SONG;

//			if (!PlayerConstants.QUEUE_SONG.data.isEmpty()) {
//
//				queueItem = PlayerConstants.QUEUE_SONG;
//				PrefsManager.Instance().setQueueWidgetCurrentSong(context, queueItem.data);
//			} else {
//
//				String data = PrefsManager.Instance().getQueueWidgetCurrentSong(context);
//				if (Utils.isEmptyString(data)) {
//					ArrayList<QueueItem> queueItems = new ArrayList<>();
//					queueItems.addAll(TrackRealmHelper.getTracks(0).values());
//					queueItem = queueItems.get(0);
//					PrefsManager.Instance().setQueueWidgetCurrentSong(context, queueItem.data);
//				} else {
//					queueItem = TrackRealmHelper.getTrack(data);
//				}
//			}

			String songName = queueItem.title;
			String albumName = queueItem.album_name;
			String songData = queueItem.data;
			final String url = "content://media/external/audio/albumart/" + queueItem.album;

			String songInfo = "";

			QueueItem nextItem = nextSong(context);

			if (PlayerConstants.QUEUE_LIST.size() > 0)
				songInfo = String.format("%d/%d %s", PlayerConstants.QUEUE_INDEX + 1, PlayerConstants.QUEUE_LIST.size(), (nextItem == null ? "" : String.format("Next: %s - %s ", nextItem.title, nextItem.artist_name)));

			if (PlayerConstants.QUEUE_STATE != PlayerConstants.QUEUE_STATE_PLAYING) {
				remoteViews.setViewVisibility(R.id.btnPause, View.GONE);
				remoteViews.setViewVisibility(R.id.btnPlay, View.VISIBLE);

			} else {
				remoteViews.setViewVisibility(R.id.btnPause, View.VISIBLE);
				remoteViews.setViewVisibility(R.id.btnPlay, View.GONE);
			}

            switch (PrefsManager.Instance().getPlayRepeat()) {
                case PlayerConstants.REPEAT_OFF:
					remoteViews.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_gray_48dp);
					break;

				case PlayerConstants.REPEAT_ALL:
					remoteViews.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_white_48dp);
					break;

				case PlayerConstants.REPEAT_ONE:
					remoteViews.setImageViewResource(R.id.btnRepeat, R.drawable.ic_repeat_one_white_48dp);
					break;
			}

            if (PrefsManager.Instance().getPlayShuffle(context)) {
                remoteViews.setImageViewResource(R.id.btnShuffle, R.drawable.ic_shuffle_white_48dp);
			} else {
				remoteViews.setImageViewResource(R.id.btnShuffle, R.drawable.ic_shuffle_gray_48dp);
			}

			remoteViews.setTextViewText(R.id.textSongName, songName);
			remoteViews.setTextViewText(R.id.textAlbumName, albumName);
			remoteViews.setTextViewText(R.id.textInfo, songInfo);
			remoteViews.setViewVisibility(R.id.textInfo, songInfo.length() > 0 ? View.VISIBLE : View.GONE);

			remoteViews.setProgressBar(R.id.progressBar, Integer.parseInt(queueItem.duration), PlayerConstants.QUEUE_TIME, false);
			AsyncJob.doOnMainThread(() -> Picasso.with(context).load(url).resize(MyApplication.IMAGE_SMALL_SIZE, MyApplication.IMAGE_SMALL_SIZE).centerInside().error(R.mipmap.placeholder).into(remoteViews, R.id.imageViewAlbumArt, appWidgetIds));

			//RemoteViews Service needed to provide adapter for ListView
			Intent svcIntent = new Intent(context, WidgetService.class);
			//passing app widget id to that RemoteViews Service
			svcIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetID);
			//setting a unique Uri to the intent
			//don't know its purpose to me right now
			svcIntent.setData(Uri.parse(
					svcIntent.toUri(Intent.URI_INTENT_SCHEME)));
			//setting adapter to listview of the widget
			remoteViews.setRemoteAdapter(appWidgetID, R.id.listViewWidget,
					svcIntent);
			//setting an empty view in case of no data
			remoteViews.setEmptyView(R.id.listViewWidget, R.id.empty_view);

			setListeners(context, remoteViews);

			if (updateList) {
				appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetID, R.id.listViewWidget);
			}

			appWidgetManager.updateAppWidget(appWidgetID, remoteViews);

		}

        updating = false;
    }

    @Override
    public void onAppWidgetOptionsChanged(Context context,
                                          AppWidgetManager appWidgetManager, int appWidgetId,
                                          Bundle newOptions) {

        // See the dimensions and
        Bundle options = appWidgetManager.getAppWidgetOptions(appWidgetId);
        int minWidth = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH);
        int minHeight = options.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT);

        // Obtain appropriate widget and update it.
        appWidgetManager.updateAppWidget(appWidgetId,
                getRemoteViews(context, minWidth, minHeight));

        onUpdate(context);

        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId,
                newOptions);

    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
//		Toast.makeText(context, "All widgets removed id(s):" + appWidgetIds, Toast.LENGTH_SHORT).show();
        super.onDeleted(context, appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
        onUpdate(context);
    }

    @Override
    public void onDisabled(Context context) {
//		Toast.makeText(context, "last widget instance removed", Toast.LENGTH_SHORT).show();

        super.onDisabled(context);
    }

	private QueueItem nextSong(Context context) {
		QueueItem item = null;

		if (PlayerConstants.QUEUE_LIST.size() > 0) {
            int repeat = PrefsManager.Instance().getPlayRepeat();
            if (repeat == PlayerConstants.REPEAT_ONE) {
				item = PlayerConstants.QUEUE_SONG;
			} else {
                if (PrefsManager.Instance().getPlayShuffle(context)) {

				} else {
					int index = PlayerConstants.QUEUE_INDEX + 1;
					if (index >= PlayerConstants.QUEUE_LIST.size()) {
						if (repeat == PlayerConstants.REPEAT_OFF) {
							item = PlayerConstants.QUEUE_LIST.get(0);
						} else    //repeat all
						{
							if (index > PlayerConstants.QUEUE_LIST.size() - 1) {
								item = PlayerConstants.QUEUE_LIST.get(PlayerConstants.QUEUE_LIST.size() - 1);
							} else {
								item = PlayerConstants.QUEUE_LIST.get(index);
							}
						}
					} else {
						item = PlayerConstants.QUEUE_LIST.get(index);
					}
				}
			}
		}

		return item;
	}
}