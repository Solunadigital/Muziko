package com.muziko.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.muziko.R;
import com.muziko.common.models.QueueItem;
import com.muziko.mediaplayer.PlayerConstants;

import java.util.ArrayList;

/**
 * If you are familiar with Adapter of ListView,this is the same as adapter
 * with few changes
 */
class WidgetAdapter implements RemoteViewsService.RemoteViewsFactory {
	private final ArrayList<QueueItem> queueItems = new ArrayList();
	private Context context = null;

	public WidgetAdapter(Context context, Intent intent) {
		this.context = context;
		int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
				AppWidgetManager.INVALID_APPWIDGET_ID);
	}

	@Override
	public void onCreate() {

	}

	@Override
	public void onDataSetChanged() {

		queueItems.clear();

		int index = PlayerConstants.QUEUE_LIST.indexOf(PlayerConstants.QUEUE_SONG);

		int length = 20;
		int total = 0;
		if (PlayerConstants.QUEUE_LIST.size() < length) {
			queueItems.addAll(PlayerConstants.QUEUE_LIST);
		} else {
			if (index + length > PlayerConstants.QUEUE_LIST.size()) {
				total = PlayerConstants.QUEUE_LIST.size();
			} else {
				total = index + length;
			}

			for (int i = index; i < total; i++) {
				QueueItem queueItem = new QueueItem();
				queueItem = PlayerConstants.QUEUE_LIST.get(i);
				queueItems.add(queueItem);
			}
		}
	}

	@Override
	public void onDestroy() {

	}

	@Override
	public int getCount() {
		return queueItems.size();
	}

	/*
	*Similar to getView of Adapter where instead of View
	*we return RemoteViews
	*
	*/
	@Override
	public RemoteViews getViewAt(int position) {
		final RemoteViews remoteView = new RemoteViews(
				context.getPackageName(), R.layout.widget_adapter_list);
		QueueItem item = queueItems.get(position);

		remoteView.setTextViewText(R.id.textTitle, item.title);
		remoteView.setTextViewText(R.id.textDesc, item.artist_name);

//		final String uri = "content://media/external/audio/albumart/" + item.album;
//		remoteView.setImageViewUri(R.id.imageThumb, Uri.parse(uri));

		Intent playsong = new Intent();
		playsong.putExtra("data", item.data);
		remoteView.setOnClickFillInIntent(R.id.widgetrow, playsong);

		if (PlayerConstants.QUEUE_SONG.data.equals(item.data)) {
			remoteView.setViewVisibility(R.id.imageState, View.VISIBLE);
		} else {
			remoteView.setViewVisibility(R.id.imageState, View.GONE);
		}

		return remoteView;
	}

	@Override
	public RemoteViews getLoadingView() {
		return null;
	}

	@Override
	public int getViewTypeCount() {
		return 1;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }
}
